package logic.crossIndigo;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Message.S2CCrossIndigoMsg.GetFightHeroRsp;
import Message.S2CCrossIndigoMsg.GetFightHeroRspID;
import Message.S2CFormationMsg.PositionStatus;
import Message.S2CIndigoMsg.HeroPosMsg;
import game.core.pub.script.IScript;
import game.server.logic.hero.HeroService;
import game.server.logic.hero.bean.Hero;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 获取参战决赛英雄
 * 
 * @date 2018年10月31日
 */
public class GetCrossIndigoHeroScript implements IScript {
	private static final Logger LOGGER = Logger.getLogger(GetCrossIndigoHeroScript.class);

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		GetFightHeroRsp rankList = (GetFightHeroRsp) script.get(Key.ARG1);
		Player player = (Player) script.get(Key.PLAYER);
		List<HeroPosMsg> listHero = new ArrayList<>();
		// 中心没有发数据,自行拼接
		if (rankList.getHeroCount() == 0) {
			GetFightHeroRsp.Builder rankMsg = GetFightHeroRsp.newBuilder();
			int[] lineUp = player.getFormationManager().getLineUp();
			for (int i = 0; i < lineUp.length; i++) {
				if (lineUp[i] != PositionStatus.LOCK_VALUE && lineUp[i] != PositionStatus.VACANCY_VALUE) {
					Hero hero = player.getHeroManager().getHero(lineUp[i]);
					HeroPosMsg.Builder bui = HeroPosMsg.newBuilder();
					bui.setHeroId(HeroService.getInstance().genHeroIdInfo(hero.getId(), hero.getStar()));
					bui.setLvl(hero.getLevel());
					bui.setPosition(i + 1);
					bui.setPower(hero.getPower());
					bui.setStar(hero.getStar());
					listHero.add(bui.build());
				}
			}
			rankMsg.addAllHero(listHero);
			rankList = rankMsg.build();
		}
		// 获取参战英雄
		MessageUtils.send(player, player.getFactory().fetchSMessage(GetFightHeroRspID.GetFightHeroRspMsgID_VALUE,
				rankList.toByteArray()));
		return null;
	}
}
