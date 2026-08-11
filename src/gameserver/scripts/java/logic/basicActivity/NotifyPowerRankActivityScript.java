package logic.basicActivity;

import org.apache.commons.lang.StringUtils;

import Message.S2CBackpackMsg.PropInfo;
import Message.S2CBasicActivityMsg;
import Message.S2CBasicActivityMsg.NotifyPowerRankActivityInfo;
import Message.S2CBasicActivityMsg.NotifyPowerRankActivityInfoRspID;
import Message.S2CBasicActivityMsg.PowerRankActivityAwardInfo;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.db.game.bean.GameGlobalBean;
import game.server.logic.constant.GlobalID;
import game.server.logic.global.GameGlobalService;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.rank.RankProcessor;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

public class NotifyPowerRankActivityScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		int type = (int) script.get(ScriptArgs.Key.ARG1);
		Player player = (Player) script.get(ScriptArgs.Key.ARG2);
		execute(type, player);
		return null;
	}

	public void execute(int type, Player player) {
		if (!player.getBasicActivityManager().isPowerRankOpen()) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 261);
			return;
		}
		S2CBasicActivityMsg.NotifyPowerRankActivityInfo.Builder notifyBuilder = NotifyPowerRankActivityInfo
				.newBuilder();
		notifyBuilder.setType(type);
		RankProcessor.getInstance().getPowerRankListInfo(player, notifyBuilder);
		if (type != 2) {
			// 计算倒计时
			GameGlobalBean end_bean = GameGlobalService.getInstance()
					.findById(GlobalID.POWER_RANK_ACTIVITY_OVER.getValue());
			long time = System.currentTimeMillis();
			long end_time = (null == end_bean.getStrVal() ? 0 : Long.parseLong(end_bean.getStrVal())); // 结束时间
			int countdown = (int) (end_time - time < 0 ? 0 : end_time - time);
			notifyBuilder.setCountdown(countdown);
		}
		String award = BeanTemplet.getGlobalBean(262).getStr_value();// 取全局变量表
		String[] awards = award.split(";");
		for (int i = 0; i < awards.length; i++) {

			PowerRankActivityAwardInfo.Builder award_builder = PowerRankActivityAwardInfo.newBuilder();
			String[] str = StringUtils.split(awards[i], ",");
			if (null == str)
				continue;
			award_builder.setIndex(Integer.parseInt(str[0])); // rank排名
			String[] awardstr = str[1].split("_");
			PropInfo.Builder pBuilder = PropInfo.newBuilder();
			pBuilder.setId(Integer.parseInt(awardstr[0]));
			pBuilder.setNum(Integer.parseInt(awardstr[1]));
			award_builder.addAwards(pBuilder);
			// for (int j = 0; j < awardstr.length; j++) {
			// String[] temp = StringUtils.split(str[j], "_");
			// }
			notifyBuilder.addAwardInfo(award_builder);
		}
		String[] powerStr = BeanTemplet.getGlobalBean(265).getStr_value().split(";");
		for (int i = 0; i < powerStr.length; i++) {

			PowerRankActivityAwardInfo.Builder award_builder = PowerRankActivityAwardInfo.newBuilder();
			String[] str = StringUtils.split(powerStr[i], ",");
			if (null == str)
				continue;
			award_builder.setIndex(Integer.parseInt(str[0]));// 战力要求
			String[] awardstr = str[1].split("_");
			PropInfo.Builder pBuilder = PropInfo.newBuilder();
			pBuilder.setId(Integer.parseInt(awardstr[0]));
			pBuilder.setNum(Integer.parseInt(awardstr[1]));
			award_builder.addAwards(pBuilder);
			// for (int j = 0; j < str.length; j++) {
			// String[] temp = StringUtils.split(str[j], "_");
			// PropInfo.Builder pBuilder = PropInfo.newBuilder();
			// pBuilder.setId(Integer.parseInt(temp[0]));
			// pBuilder.setNum(Integer.parseInt(temp[1]));
			// award_builder.addAwards(pBuilder);
			// }
			notifyBuilder.addPowerAwardInfo(award_builder);
		}
		MessageUtils.send(player,
				player.getFactory().fetchSMessage(
						NotifyPowerRankActivityInfoRspID.NotifyPowerRankActivityInfoRspMsgID_VALUE,
						notifyBuilder.build().toByteArray()));
		// 日志记录
		LogService.getInstance().logPlayerAction(player,
				NotifyPowerRankActivityInfoRspID.NotifyPowerRankActivityInfoRspMsgID_VALUE);
	}

}
