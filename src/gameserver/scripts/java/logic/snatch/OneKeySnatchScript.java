package logic.snatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Message.C2SSnatchMsg.OneKeySnatchReqID;
import Message.S2CArenaMsg.ArenaResult;
import Message.S2CArenaMsg.RewardItemMsg;
import Message.S2CPlayerMsg.PromptType;
import Message.S2CSnatchMsg.OneKeySnatchInfo;
import Message.S2CSnatchMsg.OneKeySnatchRsp;
import Message.S2CSnatchMsg.OneKeySnatchRspID;
import Message.S2CSnatchMsg.OpenProtectedRsp;
import Message.S2CSnatchMsg.OpenProtectedRspID;
import data.bean.t_magic_weaponBean;
import game.core.pub.script.IScript;
import game.core.pub.util.SimpleRandom;
import game.fight.bean.CampType;
import game.server.logic.arena.RobotService;
import game.server.logic.constant.ItemType;
import game.server.logic.constant.Reason;
import game.server.logic.constant.TaskConditionType;
import game.server.logic.drop.DropService;
import game.server.logic.fight.FightService;
import game.server.logic.fight.fightRoom.SnatchFightRoom;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.snatch.SnatchManager;
import game.server.logic.snatch.SnatchService;
import game.server.logic.snatch.bean.SnatchReport;
import game.server.logic.snatch.handler.InnerCaptureFragmentHandler;
import game.server.logic.snatch.handler.SnatchReportAddHandler;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.thread.LogicProcessor;
import game.server.util.MessageUtils;

/**
 * @author duwei
 * 
 *         <pre>
 *         一键夺宝
 * 
 *         <pre>
 *         2019年11月20日
 */
public class OneKeySnatchScript implements IScript {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs args = (ScriptArgs) arg;
		Player player = (Player) args.get(ScriptArgs.Key.PLAYER);
		int itemId = (int) args.get(ScriptArgs.Key.ARG1);

		int vipfun = BeanTemplet.getGlobalBean(820).getInt_value(); // 功能vip限制
		if (player.getVipLevel() < vipfun) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 1614);
			return null;
		}
		int consumeEnegy = BeanTemplet.getGlobalBean(98).getInt_value(); // 单次消耗精力
		if (player.getEnergy() < consumeEnegy) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 18);// 替换语言包id
			return null;
		}
		if (!canSantch(player, itemId)) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 20);// 替换语言包id
			return null;
		}
		SnatchManager snatchManager = player.getSnatchManager();
		// 取消自己的保护时间
		if (snatchManager.getProtectEndTime() > System.currentTimeMillis()) {
			snatchManager.setProtectEndTime(0);
			// 推送
			OpenProtectedRsp.Builder builder = OpenProtectedRsp.newBuilder();
			builder.setProtectEndTime(snatchManager.getProtectEndTime());
			MessageUtils.send(player.getSession(), player.getFactory()
					.fetchSMessage(OpenProtectedRspID.OpenProtectedRspMsgID_VALUE, builder.build().toByteArray()));
			// 玩家操作日志
			List<String> logStr = new ArrayList<String>();
			logStr.add("type:snatchFight");
			logStr.add("endTime:" + snatchManager.getProtectEndTime());
			LogService.getInstance().logPlayerAction(player, OpenProtectedRspID.OpenProtectedRspMsgID_VALUE,
					logStr.toArray());
		}

		t_magic_weaponBean magic_weaponBean = BeanTemplet.getMagicWeaponBean(itemId);
		String[] fragments = magic_weaponBean.getFragment().split(",");
		int count = 0, consume = 0;
		List<Item> extItems = new ArrayList<>();
		OneKeySnatchRsp.Builder builder = OneKeySnatchRsp.newBuilder();
		snatch: for (String id : fragments) {
			List<Item> items = BeanFactory.createProps(Integer.parseInt(id), 1);
			int snatchCount = 0;
			int index = 1;
			int lose = 0;
			while (!player.getBackpackManager().isItemNumEnough(items)) {
				// 抢夺
				if (player.getEnergy() < consumeEnegy) {
					MessageUtils.sendPrompt(player, PromptType.ERROR, 18);// 精力不足
					break snatch;
				}
				int status;
				if (0 == (status = snatch(player, snatchManager, Integer.parseInt(id), index, extItems))) {
					index = 1; // 对手刷新
				} else {
					// 消耗精力
					if (status == 2) {
						if (null != BeanTemplet.getIndianaBean(index + 1)) {
							index++;// 失败就挑选低概率对手
						}
						logger.info("挑战失败!");
						lose++;
						if (lose > 10) {
							logger.info("一键夺宝 ---- 连续挑战失败10次，强制退出!");
							break snatch;
						}
					} else {
						List<Item> needItems = new ArrayList<>();
						needItems.addAll(BeanFactory.createProps(ItemType.ENERGY.value(), consumeEnegy));
						consume += consumeEnegy;
						player.getBackpackManager().removeItems(needItems, true, Reason.SNATCH, "一键抢夺");
						index = 1; // 打赢对手 挑选概率最高
						snatchCount++;
						// 更新任务
						player.getTaskManager().updateTaskCondition(TaskConditionType.SnatchNum, 1);
					}
				}

			}
			if (snatchCount > 0) {
				OneKeySnatchInfo.Builder snatchInfo = OneKeySnatchInfo.newBuilder();
				snatchInfo.setSnatchNum(snatchCount);
				snatchInfo.setItemId(Integer.parseInt(id));
				builder.addResult(snatchInfo);
			}
			count++;
			// 事件结束
			if (count >= fragments.length) {
				break snatch;
			}
		}
		logger.info("一键抢夺 consume消耗精力 ：{}  ,player : {} {}", consume, player.getPlayerName(), player.getPlayerId());
		// 发放
		player.getBackpackManager().addItems(extItems, true, true, Reason.SNATCH, "一键抢夺");
		for (Item item : extItems) {
			RewardItemMsg.Builder itemInfo = RewardItemMsg.newBuilder();
			itemInfo.setItemId(item.getId());
			itemInfo.setItemNum(item.getNum());
			builder.addExtraItem(itemInfo);
		}

		builder.setConsumeEnegy(consume);// 消耗精力
		builder.setStatus(count == 0 ? 0 : 1);// 结束状态

		MessageUtils.send(player.getSession(), player.getFactory()
				.fetchSMessage(OneKeySnatchRspID.OneKeySnatchRspMsgID_VALUE, builder.build().toByteArray()));

		return null;
	}

	private int snatch(Player player, SnatchManager snatchManager, int itemId, int index, List<Item> items) {
		Map<Long, Integer> map = snatchManager.getSnatchMap();
		if (null == map || map.isEmpty()) {
			SnatchService.getInstance().refreshSnatchByOnekey(player, itemId);
			return 0;
		}
		ArrayList<Map.Entry<Long, Integer>> list = new ArrayList<Map.Entry<Long, Integer>>(map.entrySet());
		// list 冒泡排序
		for (int i = 0; i < list.size() - 1; i++) {// 外层循环控制排序趟数
			for (int j = 1; j < list.size() - i; j++) {// 内层循环控制每一趟排序多少次
				Map.Entry<Long, Integer> a1 = list.get(j - 1);
				Map.Entry<Long, Integer> a2 = list.get(j);
				Map.Entry<Long, Integer> a3;
				if (a2.getValue() > a1.getValue()) {
					a3 = list.get(j - 1);
					list.set(j - 1, a1);
					list.set(j, a3);
				}
			}
		}

		Entry<Long, Integer> o = list.get(index - 1);// 最初为0 失败加1
		Long playerId = o.getKey();
		if (null == playerId) {
			SnatchService.getInstance().refreshSnatchByOnekey(player, itemId);
			return 0;
		}
		Player snatcheder = null; // 被抢夺者
		if (RobotService.isRobot(playerId)) {
			snatcheder = SnatchService.getInstance().getSnatchRobotMap().get(playerId);
		} else {
			snatcheder = PlayerManager.getOffLinePlayerByPlayerId(playerId);
		}
		// 战斗
		CampType firstCamp = CampType.ATT;
		if (player.getFormationManager().getOffensive() < snatcheder.getFormationManager().getOffensive()) {
			firstCamp = CampType.DEF;
		}
		SnatchFightRoom snatchFightRoom = FightService.getInstance().createSnatchFight(player.getPlayerId(),
				snatcheder.getPlayerId(), player.getPlayerName(), snatcheder.getPlayerName(),
				player.getFormationManager().getFormation(), snatcheder.getFormationManager().getFormation(),
				player.getHeroManager(), snatcheder.getHeroManager(), player.getComboSkillManager().getSkills(),
				snatcheder.getComboSkillManager().getSkills(), player.getPower(), snatcheder.getPower(), firstCamp);
		List<Item> snatchItems = new ArrayList<>();
		int status = 2;// 默认挑战失败
		if (snatchFightRoom.getResult() == ArenaResult.ARENA_VICTORY_VALUE) {
			status = 1;
			String[] weightStr = BeanTemplet.getGlobalBean(120).getStr_value().split(",");
			int[] weightArray = Arrays.stream(weightStr).mapToInt(Integer::parseInt).toArray();
			int grade = snatchManager.getSnatchMap().get(playerId);
			// 掉落
			int probability = weightArray[grade - 1];
			snatchManager.setNoDropNum(snatchManager.getNoDropNum() + 1);
			if (snatchManager.getNoDropNum() == BeanTemplet.getGlobalBean(111).getInt_value()) {
				snatchManager.setNoDropNum(0);
				snatchItems.addAll(BeanFactory.createProps(itemId, 1));
				logger.info("一键抢夺 保底 ：{}  ,player : {} {}", itemId, player.getPlayerName(), player.getPlayerId());
			} else {
				SimpleRandom simpleRandom = new SimpleRandom();
				probability *= Integer.parseInt(BeanTemplet.getItemBean(itemId).getText_param());
				boolean success = simpleRandom.probability(probability / 1000000f);
				if (success) {
					snatchManager.setNoDropNum(0);
					snatchItems.addAll(BeanFactory.createProps(itemId, 1));
				}
			}
			if (snatchItems.size() > 0 && !RobotService.isRobot(snatcheder)) {// 玩家
				// 额外掉落
				// 玩家真实扣除被抢夺的碎片,更新被抢玩家背包
				if (PlayerManager.isPlayerOnline(snatcheder.getPlayerId())) {// 在线
																				// 提交到GameLine处理
					InnerCaptureFragmentHandler handler = new InnerCaptureFragmentHandler(snatcheder, itemId, true);
					MessageUtils.sendToPlayer(snatcheder, handler);
				} else {// 离线 提交到LogicProcessor处理
					InnerCaptureFragmentHandler handler = new InnerCaptureFragmentHandler(snatcheder, itemId, false);
					LogicProcessor.getInstance().addCommand(handler);
				}
				// 额外掉落
				t_magic_weaponBean magic_weaponBean = BeanTemplet
						.getMagicWeaponBean(BeanTemplet.getItemBean(itemId).getInt_param());
				List<Item> fragmentList = new ArrayList<>();
				for (String item : magic_weaponBean.getFragment().split(",")) {
					int fragmentId = Integer.parseInt(item);
					if (itemId == fragmentId) {
						continue;
					}
					fragmentList.addAll(BeanFactory.createProps(fragmentId, 1));
					if (!player.getBackpackManager().isItemNumEnough(fragmentList)) {
						snatchItems.addAll(BeanFactory.createProps(fragmentId, 1));
						break;
					}
				}

				// 战报
				if (!RobotService.isRobot(snatcheder)) {
					// 给敌人的战报
					SnatchReport snatchReport = new SnatchReport();
					snatchReport.setEnemyId(player.getPlayerId());
					snatchReport.setReplayId(snatchFightRoom.getFightRepot().getUid());
					snatchReport.setTime(System.currentTimeMillis());
					snatchReport.setResult(snatchFightRoom.getResult());
					snatchReport.setEnemyName(player.getPlayerName());
					snatchReport.setPower(player.getFormationManager().getPower());
					snatchReport.setImage(player.getImage());
					snatchReport.setImageFrame(player.getImageFrame());
					snatchReport.setImageBg(player.getImgBg());
					for (Item item : snatchItems) {
						snatchReport.getItemList().add(item);
					}
					// 更新被抢玩家战报列表
					if (PlayerManager.isPlayerOnline(snatcheder.getPlayerId())) {// 在线
																					// 提交到GameLine处理
						SnatchReportAddHandler handler = new SnatchReportAddHandler(snatcheder, snatchReport, true);
						MessageUtils.sendToPlayer(snatcheder, handler);
					} else {// 离线 提交到LogicProcessor处理
						SnatchReportAddHandler handler = new SnatchReportAddHandler(snatcheder, snatchReport, false);
						LogicProcessor.getInstance().addCommand(handler);
					}
				}
			}
		}

		int logDropId = 0;// 日志记录掉落碎片
		if (snatchFightRoom.getResult() == ArenaResult.ARENA_VICTORY_VALUE) {
			// 计算保底
			int consumeEnegy = BeanTemplet.getGlobalBean(98).getInt_value();// 单次夺宝消耗精力
			int snatchNum = player.getEnergy() / consumeEnegy;// 剩余夺宝次数
			int cdHour = BeanTemplet.getGlobalBean(233).getInt_value();// 保底CD时间（小时）
			int dropId = BeanTemplet.getGlobalBean(121).getInt_value();// 121正常掉落id
			if (snatchNum == 1 && snatchManager.getSnatchProTime() + (long) cdHour * 60 * 60 * 1000 <= System
					.currentTimeMillis()) {// 触发保底蓝色碎片
				dropId = BeanTemplet.getGlobalBean(232).getInt_value();// 特殊掉落id
				snatchManager.setSnatchProTime(System.currentTimeMillis());
			}
			logDropId = dropId;
			// 翻牌方式
			List<Item> dropItem = DropService.getDropItems(player.getDropManager(), dropId);
			items.add(dropItem.get(0));

		}
		logger.info("一键抢夺 logDropId ：{}  ,player : {} {}", logDropId, player.getPlayerName(), player.getPlayerId());
		// 发放
		player.getBackpackManager().addItems(snatchItems, true, true, Reason.SNATCH, "一键抢夺");

		// 玩家操作日志
		LogService.getInstance().logPlayerAction(player, OneKeySnatchReqID.OneKeySnatchReqMsgID_VALUE,
				RobotService.isRobot(snatcheder), snatchFightRoom.getResult(), logDropId);

		return status;
	}

	/** 是否满足抢夺条件 */
	private boolean canSantch(Player player, int itemId) {
		t_magic_weaponBean magic_weaponBean = BeanTemplet.getMagicWeaponBean(itemId);
		String[] fragments = magic_weaponBean.getFragment().split(",");
		int count = 0;
		for (String id : fragments) {
			List<Item> items = BeanFactory.createProps(Integer.parseInt(id), 1);
			if (player.getBackpackManager().isItemNumEnough(items)) {
				count++;
			}
		}
		if (count > 0 && count < fragments.length) {
			return true;
		} else {
			return false;
		}
	}

}
