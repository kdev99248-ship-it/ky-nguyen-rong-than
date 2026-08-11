package logic.crossIndigo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Message.S2CCrossIndigoMsg.CrossIndigoFightFormation;
import Message.S2CCrossIndigoMsg.NotifyFormationInfo;
import Message.S2CCrossIndigoMsg.NotifyFormationInfoID;
import Message.S2CHeroMsg.HeroIdInfo;
import Message.S2CIndigoMsg.IndigoStage;
import Message.S2CPlayerMsg.PromptType;
import Message.Inner.GRCrossIndigo.GRGetIndigoFightReportReq;
import Message.Inner.InnerServer.ServerType;
import game.core.mina.message.SMessage;
import game.core.pub.script.IScript;
import game.fight.bean.FightReport;
import game.server.cross.CrossServer;
import game.server.logic.crossIndigo.CrossIndigoService;
import game.server.logic.crossIndigo.bean.CrossFormation;
import game.server.logic.crossIndigo.bean.CrossHeroPos;
import game.server.logic.crossIndigo.bean.CrossSingleRace;
import game.server.logic.fight.cache.FightReportCacheService;
import game.server.logic.hero.HeroService;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 获取跨服石英大会战报 获取相关场次的战报记录
 */
public class GetCrossIndigoFightResultDataScript implements IScript {

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
		int session = (int) args.get(ScriptArgs.Key.ARG1);
		String raceId = (String) args.get(ScriptArgs.Key.ARG2);
		Map<String, CrossSingleRace> map = new HashMap<>();
		try {
			if (CrossIndigoService.getInstance().getIndigoStage() == IndigoStage.INDIGO_FIGHT_VALUE) {
				// 获取战报
				Map<Integer, Map<String, CrossSingleRace>> preRaceMap = CrossIndigoService.getInstance()
						.getPreRaceMap();
				if (preRaceMap.containsKey(session)) {
					map = preRaceMap.get(session);
				}
			} else {
				List<CrossSingleRace> lastRaces = player.getCrossIndigoManager().getLastRace();
				List<CrossSingleRace> lastfinalRaces = player.getCrossIndigoManager().getLastfinalRace();
				for (CrossSingleRace crossSingleRace : lastRaces) {
					map.put(crossSingleRace.getId(), crossSingleRace);
				}
				for (CrossSingleRace crossSingleRace : lastfinalRaces) {
					map.put(crossSingleRace.getId(), crossSingleRace);
				}
			}
			if (!map.containsKey(raceId)) {
				Map<Integer, List<CrossSingleRace>> finalRaceMap = CrossIndigoService.getInstance().getFinalRaceMap();
				if (finalRaceMap.containsKey(session)) {
					List<CrossSingleRace> list = finalRaceMap.get(session);

					for (CrossSingleRace crossSingleRace : list) {
						map.put(crossSingleRace.getId(), crossSingleRace);
					}
				}
			}
			if (map.containsKey(raceId)) {
				CrossSingleRace race = map.get(raceId);
				FightReport report = FightReportCacheService.getInstance().getFightReport(race.getReplayerId());
				if (report == null) {
					// MessageUtils.sendPrompt(player, PromptType.ERROR, 169);//
					// 战报不存在
					GRGetIndigoFightReportReq.Builder grFight = GRGetIndigoFightReportReq.newBuilder();
					grFight.setRaceId(raceId);
					grFight.setSession(session);
					grFight.setPlayerId(player.getPlayerId());
					grFight.setReplayerId(race.getReplayerId());
					CrossServer.getInstance().send(ServerType.ROUTE_SERVER_VALUE,
							new SMessage(GRGetIndigoFightReportReq.MsgID.eMsgID_VALUE, grFight.build().toByteArray()));
					return null;
				}
				NotifyFormationInfo.Builder builder = NotifyFormationInfo.newBuilder();
				// 攻方
				List<CrossFormation> formation = race.getRedFormations();
				for (CrossFormation crossFormation : formation) {
					CrossIndigoFightFormation.Builder fight = CrossIndigoFightFormation.newBuilder();
					List<CrossHeroPos> heroPos = crossFormation.getInfoList();
					for (CrossHeroPos crossHeroPos : heroPos) {
						HeroIdInfo.Builder heroId = HeroIdInfo.newBuilder();
						heroId.setId(crossHeroPos.getHeroId());
						heroId.setSkinId(HeroService.getInstance().checkUnlockSkinId(crossHeroPos.getHeroId(),
								crossHeroPos.getStar()));
						fight.addHeroIds(heroId);
					}
					builder.addAttack(fight);
				}
				// 守方
				formation = race.getBlueFormations();
				for (CrossFormation crossFormation : formation) {
					CrossIndigoFightFormation.Builder fight = CrossIndigoFightFormation.newBuilder();
					List<CrossHeroPos> heroPos = crossFormation.getInfoList();
					for (CrossHeroPos crossHeroPos : heroPos) {
						HeroIdInfo.Builder heroId = HeroIdInfo.newBuilder();
						heroId.setId(crossHeroPos.getHeroId());
						heroId.setSkinId(HeroService.getInstance().checkUnlockSkinId(crossHeroPos.getHeroId(),
								crossHeroPos.getStar()));
						fight.addHeroIds(heroId);
					}
					builder.addDefend(fight);
				}
				builder.setFightResult(report.buildFullInfo());

				MessageUtils.send(player, player.getFactory().fetchSMessage(
						NotifyFormationInfoID.NotifyFormationInfoMsgID_VALUE, builder.build().toByteArray()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageUtils.sendPrompt(player, PromptType.ERROR, 169);
		}

		return null;
	}

}
