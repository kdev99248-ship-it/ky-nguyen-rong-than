package logic.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.C2SOperateActivityMsg.SlotMachinesRollReq;
import Message.C2SOperateActivityMsg.SlotMachinesRollReqID;
import Message.S2CBackpackMsg.PropInfo;
import Message.S2COperateActivityMsg.RewardBox;
import Message.S2COperateActivityMsg.SlotMachinesRoll;
import Message.S2COperateActivityMsg.SlotMachinesRollRsp;
import Message.S2COperateActivityMsg.SlotMachinesRollRspID;
import Message.S2CPlayerMsg.PromptType;
import data.bean.t_language_modelBean;
import game.core.pub.script.IScript;
import game.core.pub.util.MathUtils;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.SlotMachines;
import game.server.logic.activity.bean.SlotMachinesBoxConfig;
import game.server.logic.activity.bean.SlotMachinesConfig;
import game.server.logic.activity.bean.SlotMachinesGachaConfig;
import game.server.logic.chat.service.ChatService;
import game.server.logic.constant.ItemType;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 获取探险训练家排行榜
 * 
 * @author tzyx
 *
 */
public class SlotMachinesRollScript implements IScript {
	private static Logger LOGGER = Logger.getLogger(SlotMachinesRollScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		Player player = (Player) script.get(Key.PLAYER);
		// 活动正在结算
		if (ActivityService.getInstance().isSmCheckEnd()) {
			LOGGER.error("活动未开启");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 209);
			return null;
		}
		SlotMachinesRollReq req = (SlotMachinesRollReq) script.get(Key.ARG1);
		slotMachinesRoll(player, req);
		return null;
	}

	private void slotMachinesRoll(Player player, SlotMachinesRollReq req) {
		ActivityService service = ActivityService.getInstance();
		if (service.getSlotMachinesConfig() == null || service.getSlotMachinesConfig().getOpen() == 0) {
			LOGGER.error("活动未开启");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 209);
			return;
		}
		SlotMachinesConfig config = service.getSlotMachinesConfig();
		if (!service.getSlotMachinesData().containsKey(player.getPlayerId())) {
			SlotMachines slotMachines = new SlotMachines();
			slotMachines.setPlayerId(player.getPlayerId());
			slotMachines.setScore(0);
			slotMachines.setUsedTimes(0);
			slotMachines.setFreeTimes(config.getFreeTime());
			for (int i = 0; i < config.getBoxConfigList().size(); i++) {
				slotMachines.getBoxes().add(Integer.valueOf(0));
			}
			service.getSlotMachinesData().put(slotMachines.getPlayerId(), slotMachines);
		}
		SlotMachines slotMachines = service.getSlotMachinesData().get(player.getPlayerId());
		// 检查消耗
		List<Item> consumeList = new ArrayList<>();
		List<Item> rewardList = new ArrayList<>();
		if (req.getType() == 1) {
			// 没有免费次数时消耗道具
			if (slotMachines.getFreeTimes() <= 0) {
				consumeList.addAll(BeanFactory.createProps(ItemType.DIAMOND.value(), config.getConsume()));
			} else {
				slotMachines.setFreeTimes(slotMachines.getFreeTimes() - 1 <= 0 ? 0 : slotMachines.getFreeTimes() - 1);
			}
		} else if (req.getType() == 2) {
			consumeList.addAll(BeanFactory.createProps(ItemType.DIAMOND.value(), config.getConsume10()));
		}
		if (!consumeList.isEmpty()) {
			if (!player.getBackpackManager().isItemNumEnough(consumeList)) {
				LOGGER.error("道具不足!");
				MessageUtils.sendPrompt(player, PromptType.ERROR, 332);
				return;
			}
			// 扣除消费
			service.setJackpot(service.getJackpot() + (consumeList.get(0).getNum() * 20 / 100));
			player.getBackpackManager().removeItems(consumeList, false, Reason.SLOT_MACHINES_ROLL, "");
		}
		SlotMachinesRollRsp.Builder builder = SlotMachinesRollRsp.newBuilder();
		// 抽奖
		long oldJackpot = service.getJackpot();
		List<Integer> indexList = roll(config.getGachaList(), req.getType());
		for (Integer index : indexList) {
			int num = 0;
			int id = 0;
			SlotMachinesGachaConfig slotMachinesGachaConfig = config.getGachaList().get(index);
			SlotMachinesRoll.Builder b = SlotMachinesRoll.newBuilder();
			PropInfo.Builder roll = PropInfo.newBuilder();
			b.setIndex(index + 1);
			if (slotMachinesGachaConfig.getType() == 1) {
				id = slotMachinesGachaConfig.getItemId();
				num = slotMachinesGachaConfig.getNum();
			} else if (slotMachinesGachaConfig.getType() == 2) {
				id = ItemType.DIAMOND.value();
				num = getJackpot(oldJackpot,
						BeanTemplet.getItemBean(slotMachinesGachaConfig.getItemId()).getInt_param());
				oldJackpot -= num;
			}
			rewardList.addAll(BeanFactory.createProps(id, num));
			roll.setId(id);
			roll.setNum(num);
			b.addReward(roll);
			builder.addRollList(b);
			addHistory(player, id, num, slotMachinesGachaConfig);
		}
		if (req.getType() == 1) {
			player.getBackpackManager().addItems(rewardList, Reason.SLOT_MACHINES_ROLL, "");
		} else {
			player.getBackpackManager().addItems(rewardList, Reason.SLOT_MACHINES_ROLL_10, "");
		}
		// 添加积分调整排行榜
		addScoreAndRanking(player, req.getType());
		// 设置奖池
		service.setJackpot(oldJackpot);
		builder.setRanking(service.getSlotMachinesRankMap().get(player.getPlayerId()));
		builder.setScore(slotMachines.getScore());
		builder.setJackpot(service.getJackpot());
		builder.setUsedTimes(slotMachines.getUsedTimes());
		builder.setFreeTimes(slotMachines.getFreeTimes());
		for (int i = 0; i < config.getBoxConfigList().size(); i++) {
			RewardBox.Builder box = config.getBoxConfigList().get(i).genRewardBox();
			if (null != slotMachines) {
				box.setStatus(slotMachines.getBoxes().get(i));
			} else {
				box.setStatus(0);
			}
			builder.addTimesBoxs(box);
		}
		MessageUtils.send(player, player.getFactory()
				.fetchSMessage(SlotMachinesRollRspID.SlotMachinesRollRspMsgID_VALUE, builder.build().toByteArray()));
		// 记录动作 活动id,类型
		LogService.getInstance().logPlayerAction(player, SlotMachinesRollReqID.SlotMachinesRollReqMsgID_VALUE,
				config.getId(), req.getType());
	}

	/**
	 * 增加玩家积分并进行积分排序
	 */
	private void addScoreAndRanking(Player player, int type) {
		ActivityService service = ActivityService.getInstance();
		Map<Long, SlotMachines> slotMachinesData = service.getSlotMachinesData();
		SlotMachines slotMachines = slotMachinesData.get(player.getPlayerId());
		SlotMachinesConfig config = service.getSlotMachinesConfig();
		if (null == slotMachines) {
			slotMachines = new SlotMachines();
			slotMachines.setPlayerId(player.getPlayerId());
			slotMachines.setScore((type == 1 ? config.getScore() : config.getScore10()));
			for (int i = 0; i < config.getGachaList().size(); i++) {
				slotMachines.getBoxes().add(0);
			}
			slotMachines.setUsedTimes((type == 1 ? 1 : 10));
			slotMachinesData.put(slotMachines.getPlayerId(), slotMachines);
		} else {
			slotMachines.setUsedTimes(slotMachines.getUsedTimes() + (type == 1 ? 1 : 10));
			slotMachines.setScore(slotMachines.getScore() + (type == 1 ? config.getScore() : config.getScore10()));
		}
		// 重设玩家宝箱状态
		for (int i = 0; i < config.getBoxConfigList().size(); i++) {
			// 判断箱子之前状态
			Integer box = slotMachines.getBoxes().get(i);
			// 未完成的时候检查是否变成完成
			if (box == 0) {
				SlotMachinesBoxConfig slotMachinesBoxConfig = config.getBoxConfigList().get(i);
				if (slotMachines.getUsedTimes() >= slotMachinesBoxConfig.getNeedTimes()) {
					slotMachines.getBoxes().set(i, 1);
				}
			}
		}
		service.slotMachinesRankSort();
	}

	/** 增加抽奖的历史记录和跑马灯 */
	private void addHistory(Player player, int id, int num, SlotMachinesGachaConfig config) {
		ActivityService service = ActivityService.getInstance();
		t_language_modelBean languageModelBean = BeanTemplet.getLanguageModelBean(config.getInfoId());
		if (null != languageModelBean) {
			String info = languageModelBean.getValue().replaceFirst("\\{p1\\}", player.getPlayerName());
			info = info.replaceFirst("\\{p2\\}", BeanTemplet.getItemBean(id).getName());
			info = info.replaceFirst("\\{p3\\}", String.valueOf(num));
			service.getSlotMachinesHistory().addFirst(info);
			if (service.getSlotMachinesHistory().size() > 60) {
				service.getSlotMachinesHistory().removeLast();
			}
		}
		// 发送跑马灯
		if (config.getBroadcastId() != 0) {
			languageModelBean = BeanTemplet.getLanguageModelBean(config.getBroadcastId());
			if (null != languageModelBean) {
				ChatService.getInstance().sendMarqueeMessage(languageModelBean.getId(), player.getPlayerName(),
						BeanTemplet.getItemBean(id).getName(), String.valueOf(num));
			}
		}
	}

	/** roll点 */
	private List<Integer> roll(List<SlotMachinesGachaConfig> gachaList, int type) {
		List<Integer> l = new ArrayList<>();
		int[] odds = new int[gachaList.size()];
		for (int i = 0; i < gachaList.size(); i++) {
			if (type == 1) {
				odds[i] = gachaList.get(i).getOdds();
			} else if (type == 2) {
				odds[i] = gachaList.get(i).getOdds10();
			}
		}
		if (type == 1) {
			l.add(MathUtils.weightCalculate(odds));
		}
		if (type == 2) {
			for (int i = 0; i < 10; i++) {
				l.add(MathUtils.weightCalculate(odds));
			}
		}
		return l;
	}

	/** 根据倍率获取奖池中的钻石 */
	private int getJackpot(long jackpotNum, int multiple) {
		return (int) (jackpotNum * multiple / 10000);
	}
}
