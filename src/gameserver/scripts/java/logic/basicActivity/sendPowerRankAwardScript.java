package logic.basicActivity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import Message.S2CRankMsg.RankType;
import game.core.pub.script.IScript;
import game.server.config.ServerConfig;
import game.server.db.log.bean.PlayerRankLogBean;
import game.server.logic.arena.RobotService;
import game.server.logic.chat.handler.InnerPublishMarqueeMsgHandler;
import game.server.logic.constant.GlobalID;
import game.server.logic.constant.ModelMsg;
import game.server.logic.constant.RankTypes;
import game.server.logic.constant.Reason;
import game.server.logic.drop.DropService;
import game.server.logic.global.GameGlobalService;
import game.server.logic.item.bean.Item;
import game.server.logic.line.handler.PlayerUpdateBean;
import game.server.logic.log.LogService;
import game.server.logic.log.handler.LogPlayerRankHandler;
import game.server.logic.mail.MailService;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.player.RoleViewService;
import game.server.logic.rank.RankProcessor;
import game.server.logic.rank.bean.BaseRankInfo;
import game.server.logic.util.BeanTemplet;
import game.server.thread.BackLogProcessor;
import game.server.thread.LogicProcessor;
import game.server.thread.PlayerRestoreProcessor;
import game.server.thread.dboperator.handler.ReqUpdatePlayerHandler;

public class sendPowerRankAwardScript implements IScript {

	private Logger logger = LoggerFactory.getLogger(sendPowerRankAwardScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		// 取排行榜
		List<BaseRankInfo> rankList = RankProcessor.getInstance().getRankListByType(RankType.POWER_RANK);
		List<BaseRankInfo> noticeList = new ArrayList<>();
		int max = BeanTemplet.getGlobalBean(153).getInt_value();
		int size = rankList.size() < max ? rankList.size() : max;
		String award = BeanTemplet.getGlobalBean(262).getStr_value();// 取全局变量表
		String[] awards = award.split(";");
		String date = LocalDate.now().toString();
		String strb;
		logger.info(" 战力比拼发奖开始----------------");
		List<PlayerRankLogBean> l = new ArrayList<>();
		for (int i = 0; i < size; i++) {
			BaseRankInfo baseRankInfo = rankList.get(i);
			// 前10 并且超过战力限制 才有奖励 BeanTemplet.getGlobalBean(264).getInt_value()
			if (i + 1 <= awards.length && baseRankInfo.getParam1() > BeanTemplet.getGlobalBean(264).getInt_value()) {
				if (noticeList.isEmpty() || noticeList.size() < 3)// 前三名公告
					noticeList.add(baseRankInfo);
				// 邮件发放奖励
				Map<Integer, Integer> adjunctMap = new HashMap<Integer, Integer>();
				String[] str = StringUtils.split(awards[i], ",");
				if (null == str)
					continue;
				String[] strs = str[1].split("_");
				logger.info(" 战力比拼发奖  ，第" + (i + 1) + "名 玩家id： " + baseRankInfo.getPlayerId() + " 奖励发放， 掉落表道具 ："
						+ awards[i]);
				Player player = PlayerManager.getOffLinePlayerByPlayerId(baseRankInfo.getPlayerId());
				List<Item> items = DropService.getDropItems(player.getDropManager(), Integer.parseInt(strs[0]));
				for (int j = 0; j < items.size(); j++) {
					Item item = items.get(j);
					adjunctMap.put(item.getId(), item.getNum());
				}
				List<String> contentParams = new ArrayList<>();
				contentParams.add(String.valueOf(i + 1));
				MailService.getInstance().sendSysMail2Player(baseRankInfo.getPlayerId(), 34, contentParams,
						Reason.POWER_RANK_AWARD, String.valueOf(i + 1), null, System.currentTimeMillis(), adjunctMap);
				if (!PlayerManager.isPlayerOnline(player.getPlayerId())) {// 在线
					player.offLineSave();
				}
				if (RobotService.isRobot(baseRankInfo.getPlayerId())) {
					continue;
				}
				// 添加排行榜日志
				strb = "power:" + String.valueOf(baseRankInfo.getParam1());
				l.add(LogService.getInstance().logPlayeRank(RankTypes.BASE_RANK_LOG.value(), date,
						baseRankInfo.getPlayerId(), i + 1, player.getPlayerName(),
						ServerConfig.getInstance().getServerId(), 0, strb));
			}
			if (i + 1 >= BeanTemplet.getGlobalBean(153).getInt_value()) {
				break;
			}
		}
		
		String[] powerStr = BeanTemplet.getGlobalBean(265).getStr_value().split(";");

		// 发战力奖励
		for (int i = 0; i < rankList.size(); i++) {
			BaseRankInfo baseRankInfo = rankList.get(i);
			int player_power = (int) baseRankInfo.getParam1();
			String[] str = checkPowerAward(player_power, powerStr);
			if (null != str && !str[1].trim().equals("")) {
				Map<Integer, Integer> adjunctMap = new HashMap<Integer, Integer>();
				String[] strs = str[1].split("_");
				Player player = PlayerManager.getOffLinePlayerByPlayerId(baseRankInfo.getPlayerId());
				logger.info(" 战力比拼发奖  ，战力奖励 ：" + (player_power) + "玩家id： " + baseRankInfo.getPlayerId()
						+ "  奖励发放， 掉落表道具 ：" + str[1]);
				List<Item> items = DropService.getDropItems(player.getDropManager(), Integer.parseInt(strs[0]));
				for (int j = 0; j < items.size(); j++) {
					Item item = items.get(j);
					adjunctMap.put(item.getId(), item.getNum());
				}
				List<String> contentParams = new ArrayList<>();
				contentParams.add(str[0]);
				MailService.getInstance().sendSysMail2Player(baseRankInfo.getPlayerId(), 35, contentParams,
						Reason.POWER_RANK_AWARD, str[0], null, System.currentTimeMillis(), adjunctMap);
				if (!PlayerManager.isPlayerOnline(player.getPlayerId())) {// 在线
																			// 提交到GameLine处理
																			// 回存
					PlayerRestoreProcessor.getInstance().submitRequest(new ReqUpdatePlayerHandler(-1,
							new PlayerUpdateBean(player.toAccountBean(), player.toPlayerBean(), false)));
				}
				if (RobotService.isRobot(baseRankInfo.getPlayerId())) {
					continue;
				}
				// 添加排行榜日志
				strb = "power:" + String.valueOf(baseRankInfo.getParam1());
				l.add(LogService.getInstance().logPlayeRank(RankTypes.BASE_RANK_LOG.value(), date,
						baseRankInfo.getPlayerId(), i + 1, player.getPlayerName(),
						ServerConfig.getInstance().getServerId(), 0, strb));
			}
		}
		if (!l.isEmpty()) {
			BackLogProcessor.getInstance().addCommand(new LogPlayerRankHandler(l));
		}
		logger.info(" 战力比拼发奖结束----------------");
		/**
		 * 前3名跑马灯通知
		 */
		// 拼接公告字符串
		boolean notice = true;
		String rankSizeStr = "";
		String noticeStr = "";
		switch (noticeList.size()) {
		case 1:
			noticeStr = RoleViewService.getRoleById(noticeList.get(0).getPlayerId()).getName();
			rankSizeStr = "一";
			break;
		case 2:
			noticeStr = RoleViewService.getRoleById(noticeList.get(0).getPlayerId()).getName() + "、"
					+ RoleViewService.getRoleById(noticeList.get(1).getPlayerId()).getName();
			rankSizeStr = "二";
		case 3:
			noticeStr = RoleViewService.getRoleById(noticeList.get(0).getPlayerId()).getName() + "、"
					+ RoleViewService.getRoleById(noticeList.get(1).getPlayerId()).getName() + "、"
					+ RoleViewService.getRoleById(noticeList.get(2).getPlayerId()).getName();
			rankSizeStr = "三";
			break;

		default:
			notice = false;
			break;
		}
		if (notice)
			LogicProcessor.getInstance().addCommand(
					new InnerPublishMarqueeMsgHandler(ModelMsg.POWER_RANK_TOP1.value(), noticeStr, rankSizeStr));

		GameGlobalService.getInstance().modify(GlobalID.POWER_RANK_AWARD.getValue(), 1, null); // 奖励已发放
		return null;
	}

	private String[] checkPowerAward(int player_power, String[] powerStr) {
		String[] awardStr = null;
		int power = 0;// 取达到最大的战力
		for (int i = 0; i < powerStr.length; i++) {
			String[] powerAwards = powerStr[i].split(",");
			if (player_power >= Integer.parseInt(powerAwards[0]) && power < Integer.parseInt(powerAwards[0])) {
				power = Integer.parseInt(powerAwards[0]);
			}
		}
		if (power == 0)
			return null;
		awardStr = new String[2];
		awardStr[0] = power + "";
		for (int i = 0; i < powerStr.length; i++) {
			String[] powerAwards = powerStr[i].split(",");
			if (power == Integer.parseInt(powerAwards[0])) {
				awardStr[1] = powerAwards[1];
				break;
			}
		}
		return awardStr;
	}
}
