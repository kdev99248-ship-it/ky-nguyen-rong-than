package logic.crossIndigo;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Message.S2CFormationMsg.PositionStatus;
import Message.S2CIndigoMsg.HeroPosMsg;
import Message.S2CIndigoMsg.SaveHeroesRsp;
import Message.S2CIndigoMsg.SaveHeroesRspID;
import game.core.pub.script.IScript;
import game.server.logic.hero.HeroService;
import game.server.logic.hero.bean.Hero;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 获取参战英雄和阵型
 * 
 * @date 2018年10月10日
 */
public class GetCrossIndigoFightHeroScript implements IScript {
	private static final Logger LOGGER = Logger.getLogger(GetCrossIndigoFightHeroScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		SaveHeroesRsp rankList = (SaveHeroesRsp) script.get(Key.ARG1);
		Player player = (Player) script.get(Key.PLAYER);
		List<HeroPosMsg> listHero = new ArrayList<>();
		// 中心没有发数据,自行拼接
		SaveHeroesRsp.Builder sendBuilder = SaveHeroesRsp.newBuilder();
		if (rankList.getHeroCount() == 0) {
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
			sendBuilder.addAllHero(listHero);
		} else {
			sendBuilder = rankList.toBuilder();
		}
		MessageUtils.send(player, player.getFactory().fetchSMessage(SaveHeroesRspID.SaveHeroesRspMsgID_VALUE,
				sendBuilder.build().toByteArray()));
		return null;
	}
}
