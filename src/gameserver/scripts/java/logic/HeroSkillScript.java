package logic;

import java.util.Map;

import game.core.pub.script.IScript;
import game.server.logic.hero.bean.Hero;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;

public class HeroSkillScript implements IScript {

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
		@SuppressWarnings("unchecked")
		Map<String, String> param = (Map<String, String>)arg;
		long playerId = Long.parseLong(param.get("playerId"));
		int skillIndex = Integer.parseInt(param.get("skillIndex"));
		int skillId = Integer.parseInt(param.get("skillId"));
		Player player = PlayerManager.getOffLinePlayerByPlayerId(playerId);
		
		Hero  hero = null;
		if(param.containsKey("heroId")) {
			int heroId = Integer.parseInt(param.get("heroId"));
			hero = player.getHeroManager().getHero(heroId);
		}else {
			hero = player.getHeroManager().getLeader();
		}
		hero.getSkillMap().get(skillIndex).setId(skillId);
		
		//回存
		player.offLineSave();
		
		return null;
	}

}
