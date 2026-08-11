package logic.hero;

import java.util.List;

import Message.S2CHeroMsg.HeroEsotericaRsp;
import Message.S2CHeroMsg.HeroEsotericaRspID;
import Message.S2CRuneMsg.RuneBagNotify;
import Message.S2CRuneMsg.RuneBagNotifyID;
import game.core.pub.script.IScript;
import game.server.logic.hero.bean.Hero;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 请求获得秘技信息
 */
public class ReqHeroEsotericaInfoScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs argsMap = (ScriptArgs) arg;
		Player player = (Player) argsMap.get(ScriptArgs.Key.PLAYER);
		HeroEsotericaRsp.Builder sendBuilder = HeroEsotericaRsp.newBuilder();
		List<Hero> l = player.getHeroManager().getAllHero();
		if(!player.isOnline())
			return null;
		Hero hero;
		int j = 1;
		int limit = 10;
		int k = l.size() % limit > 0 ? l.size() / limit + 1 : l.size() / limit;
		for (int i = 0; i < l.size(); i++) {
			hero = l.get(i);
			hero.checkHeroEsoterica(player);// 先检测激活条件
			sendBuilder.addHeros(hero.genHeroEsotericaBuilder());

			if (i == l.size() - 1) {
				sendBuilder.setUpdateType(j);
				if (j == k) 
					sendBuilder.setUpdateType(-1);
				if (j == 1)
					sendBuilder.setUpdateType(-2);
				// 推送
				MessageUtils.send(player, player.getFactory().fetchSMessage(HeroEsotericaRspID.HeroEsotericaRspMsgID_VALUE,
						sendBuilder.build().toByteArray()));
				break;
			}
			if ((i + 1) % limit == 0) {
				sendBuilder.setUpdateType(j);
				if (j == k) 
					sendBuilder.setUpdateType(-1);
				// 推送
				MessageUtils.send(player, player.getFactory().fetchSMessage(HeroEsotericaRspID.HeroEsotericaRspMsgID_VALUE,
						sendBuilder.build().toByteArray()));
				sendBuilder = HeroEsotericaRsp.newBuilder();;
				j++;
			}
		}

//		List<Hero> list = player.getHeroManager().getAllHero();
//		for (Hero hero1 : list) {
//			if (null == hero1)
//				continue;
//			hero1.checkHeroEsoterica(player);// 先检测激活条件
//			sendBuilder.addHeros(hero1.genHeroEsotericaBuilder());
//		}
//		if(player.isOnline()) {
//			MessageUtils.send(player.getSession(), player.getFactory()
//					.fetchSMessage(HeroEsotericaRspID.HeroEsotericaRspMsgID_VALUE, sendBuilder.build().toByteArray()));
//		}
		return null;
	}

}
