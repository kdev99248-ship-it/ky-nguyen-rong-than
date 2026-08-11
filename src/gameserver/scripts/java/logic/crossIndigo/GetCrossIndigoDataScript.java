package logic.crossIndigo;

import java.util.List;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ProtocolStringList;

import Message.C2SIndigoMsg.RegAndGetDataReqID;
import Message.S2CCrossIndigoMsg.NotifyServerIds;
import Message.S2CCrossIndigoMsg.NotifyServerIdsID;
import Message.S2CIndigoMsg.FightStage;
import Message.S2CIndigoMsg.IndigoStage;
import Message.S2CIndigoMsg.LastRaceMsg;
import Message.S2CIndigoMsg.RaceStatus;
import Message.S2CIndigoMsg.RegAndGetDataRsp;
import Message.S2CIndigoMsg.RegAndGetDataRspID;
import Message.S2CIndigoMsg.SingleRankMsg;
import Message.Inner.GRCrossIndigo.CrossIndigoDayStatus;
import Message.Inner.GRCrossIndigo.GRGetDataRsp;
import game.core.pub.script.IScript;
import game.server.logic.crossIndigo.CrossIndigoService;
import game.server.logic.crossIndigo.bean.CrossFormation;
import game.server.logic.crossIndigo.bean.CrossParticipant;
import game.server.logic.crossIndigo.bean.CrossSingleRace;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

public class GetCrossIndigoDataScript implements IScript {

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
		GRGetDataRsp getData = (GRGetDataRsp) args.get(ScriptArgs.Key.ARG1);
		int raceDay = (int) args.get(ScriptArgs.Key.ARG2);
		CrossIndigoService.getInstance().indigoStageNotify(player);// 先推送一次状态
		// 封装协议准备下发数据
		RegAndGetDataRsp.Builder builder = RegAndGetDataRsp.newBuilder();
		if (CrossIndigoService.getInstance().getIndigoStage() == IndigoStage.INDIGO_FIGHT_VALUE) {
			builder.setFightStage(CrossIndigoService.getInstance().genFightStageMsg());
			CrossParticipant participant = CrossIndigoService.getInstance().getParticipantMap()
					.get(player.getPlayerId());
			if (null != participant && participant.getMyRaces().size() >= 0) {// 是否参与比赛
				LastRaceMsg.Builder last = LastRaceMsg.newBuilder();
				for (CrossSingleRace singleRace : participant.getMyRaces()) {
					last.addMyRace(singleRace.genBuilderMsg(player.getPlayerId()));
				}
				last.setWinNum(participant.getWinNum());
				last.setLoseNum(participant.getLoseNum());
				last.setIntegral(participant.getIntegral());
				last.setRank(participant.getRank());
				builder.setLastRace(last);
			}
		} else {
			// 上轮比赛
			LastRaceMsg.Builder lBuilder = CrossIndigoService.getInstance().genLastRaceBuilder(player);
			if (lBuilder.getMyRaceList().size() >= 1) {
				LastRaceMsg.Builder a = LastRaceMsg.newBuilder();
				a.setWinNum(lBuilder.getWinNum());
				a.setLoseNum(lBuilder.getLoseNum());
				a.setIntegral(lBuilder.getIntegral());
				a.setRank(lBuilder.getRank());
				for (int i = 0; i < 10; i++) {
					if (lBuilder.getFinalRaceCount() - 1 - i >= 0) {
						a.addFinalRace(lBuilder.getFinalRace(lBuilder.getFinalRaceCount() - 1 - i));
					}
				}
				a.addAllMyRace(lBuilder.getMyRaceList());
				builder.setLastRace(a);
			}
		}
		builder.setApplyNum(CrossIndigoService.getInstance().getApplyNum());
		if (raceDay == CrossIndigoDayStatus.PRELIMINARY_DAY_VALUE) {
			// 初赛 些许数据不取本地服数据
			CrossParticipant participant = CrossIndigoService.getInstance().getParticipantMap()
					.get(player.getPlayerId());
			if (participant != null) {
				for (CrossFormation formation : participant.getPreFormations().values()) {
					builder.addFormation(formation.genBuilder());
				}
				for (CrossFormation formation : participant.getFinalFormations().values()) {
					builder.addFormation(formation.genBuilder());
				}
				builder.setMyRace(participant.genMyRaceBuilder());
				for (CrossFormation formation : participant.getRecomFor()) {
					builder.addRecommendFormation(formation.genBuilder());
				}
			}
			// 初赛前三
			int num = 0;
			for (SingleRankMsg singleRankMsg : CrossIndigoService.getInstance().getRankList()) {
				builder.addTop3(CrossIndigoService.getInstance().updateRankName(singleRankMsg));
				num++;
				if (num > 3) {
					break;
				}
			}
			for (List<CrossSingleRace> races : CrossIndigoService.getInstance().getFinalRaceMap().values()) {
				if (races == null) {
					continue;
				}
				for (CrossSingleRace singleRace : races) {
					builder.addFinalRace(singleRace.genFinalBuilder());
				}
			}

		} else {
			// 取决赛阵型信息
			CrossParticipant participant = CrossIndigoService.getInstance().getParticipantMap()
					.get(player.getPlayerId());
			if (participant != null) {
				builder.setMyRace(participant.genMyRaceBuilder());
			}
			builder.addAllFormation(getData.getFormationList());
			builder.addAllRecommendFormation(getData.getRecommendFormationList());
			// 决赛信息
			if (CrossIndigoService.getInstance().getFightStage() == FightStage.Indigo_final_VALUE
					&& CrossIndigoService.getInstance().getRaceStatus() != RaceStatus.RACE_END_VALUE) {
				for (int i = 1; i <= CrossIndigoService.getInstance().getSession(); i++) {
					List<CrossSingleRace> races = CrossIndigoService.getInstance().getFinalRaceMap().get(i);
					if (races == null) {
						continue;
					}
					for (CrossSingleRace singleRace : races) {
						builder.addFinalRace(singleRace.genFinalBuilder());
					}
				}
			} else {
				for (List<CrossSingleRace> races : CrossIndigoService.getInstance().getFinalRaceMap().values()) {
					if (races == null) {
						continue;
					}
					for (CrossSingleRace singleRace : races) {
						builder.addFinalRace(singleRace.genFinalBuilder());
					}
				}
			}

			// 前3名
			int num = 0;
			for (SingleRankMsg singleRankMsg : getData.getTop3List()) {
				builder.addTop3(CrossIndigoService.getInstance().updateRankName(singleRankMsg));
				num++;
				if (num >= 3) {
					break;
				}
			}
		}

		// 比赛文字信息
		int raceSize = CrossIndigoService.getInstance().getRaceInfoList().size();
		int start = raceSize < 100 ? 0 : raceSize - 100;
		for (int i = start; i < raceSize; i++) {
			builder.addRaceInfo(CrossIndigoService.getInstance().getRaceInfoList().get(i));
		}
		if (getData.getHerosCount() > 0) {
			builder.addAllHeroList(getData.getHerosList());
		}
		// 当逻辑走到这一步说明服务器可以参与跨服石英大会
		builder.setServerQualification(true);
		// 推送
		MessageUtils.send(player, player.getFactory().fetchSMessage(RegAndGetDataRspID.RegAndGetDataRspMsgID_VALUE,
				builder.build().toByteArray()));

		// 服务器列表
		NotifyServerIds.Builder serverNames = NotifyServerIds.newBuilder();
		ProtocolStringList list = getData.getServerIdsList();
		serverNames.addAllServerNames(list);
		// 推送
		MessageUtils.send(player, player.getFactory().fetchSMessage(NotifyServerIdsID.NotifyServerIdsMsgID_VALUE,
				serverNames.build().toByteArray()));
		// 记录动作
		LogService.getInstance().logPlayerAction(player, RegAndGetDataReqID.RegAndGetDataReqMsgID_VALUE);
		return null;

	}

}
