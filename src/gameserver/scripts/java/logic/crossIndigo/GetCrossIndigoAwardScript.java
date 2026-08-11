package logic.crossIndigo;

import java.util.ArrayList;
import java.util.List;

import Message.S2CBackpackMsg.PropInfo;
import Message.S2CIndigoMsg.FightStage;
import Message.S2CIndigoMsg.IndigoStage;
import Message.S2CIndigoMsg.RaceStatus;
import Message.S2CIndigoMsg.RewardRaceRsp;
import Message.S2CIndigoMsg.RewardRaceRspID;
import Message.S2CPlayerMsg.PromptType;
import Message.Inner.GRCrossIndigo.GRRewardRaceRsp;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.crossIndigo.CrossIndigoService;
import game.server.logic.crossIndigo.bean.CrossParticipant;
import game.server.logic.crossIndigo.bean.CrossSingleRace;
import game.server.logic.item.bean.Item;
import game.server.logic.player.Player;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/***
 * 获取跨服石英奖励
 * 
 */
public class GetCrossIndigoAwardScript implements IScript {

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
		GRRewardRaceRsp getData = (GRRewardRaceRsp) args.get(ScriptArgs.Key.ARG1);
		String raceId = getData.getRaceId();
		CrossParticipant participant = CrossIndigoService.getInstance().getParticipantMap().get(player.getPlayerId());
		int indigoStage = CrossIndigoService.getInstance().getIndigoStage();
		if (participant == null) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 296);// 未报名参赛
			return null;
		}
		if (indigoStage == IndigoStage.INDIGO_APPLAY_VALUE || indigoStage == IndigoStage.INDIGO_READY_VALUE) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 295);// 不是比赛阶段无法领取奖励
			return null;
		}
		if (indigoStage == IndigoStage.INDIGO_IDLE_VALUE) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 297);// 未领取奖励在比赛结束已通过邮件发送
			return null;
		}
		if (!participant.getMyRaceList().contains(raceId)) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 298);// 自己不存在该场比赛
			return null;
		}
		int index = 0;
		for (String rId : participant.getMyRaceList()) {
			if (rId.equals(raceId)) {
				break;
			}
			index++;
		}
		int fightStage = FightStage.Indigo_preliminary_VALUE;
		int session = index + 1;
		CrossSingleRace singleRace = CrossIndigoService.getInstance().findCurRaceById(raceId, session, fightStage);
		if (singleRace.getStatus() != RaceStatus.RACE_END_VALUE) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 291);// 比赛未结束，不能领取奖励
			return null;
		}
		boolean canReward = false;
		if (player.getPlayerId() == singleRace.getRedId()) {
			canReward = singleRace.isRedReward();
			if (canReward) {
				singleRace.setRedReward(false);
			}
		} else if (player.getPlayerId() == singleRace.getBlueId()) {
			canReward = singleRace.isBlueReward();
			if (canReward) {
				singleRace.setBlueReward(false);
			}
		}
		if (!canReward) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 299);// 已领取
			return null;
		}
		List<PropInfo> list = getData.getItemsList();
		List<Item> items = new ArrayList<>();
		for (PropInfo item : list) {
			items.addAll(BeanFactory.createProps(item.getId(), item.getNum()));
		}
		player.getBackpackManager().addItems(items, Reason.INDOGO_RACE_REWARD, "跨服石英大会领取比赛奖励");
		RewardRaceRsp.Builder sendBuilder = RewardRaceRsp.newBuilder();
		sendBuilder.setRaceId(getData.getRaceId());
		sendBuilder.addAllItems(getData.getItemsList());
		// 推送领取消息
		MessageUtils.send(player, player.getFactory().fetchSMessage(RewardRaceRspID.RewardRaceRspMsgID_VALUE,
				sendBuilder.build().toByteArray()));
		return null;
	}

}
