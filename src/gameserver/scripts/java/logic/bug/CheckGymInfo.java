package logic.bug;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import data.bean.t_roadPavilionBean;
import game.core.pub.script.IScript;
import game.server.logic.snatchTerritory.SnatchTerritoryService;
import game.server.logic.snatchTerritory.bean.SnatchTerritory;
import game.server.logic.util.BeanTemplet;

public class CheckGymInfo implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		Map<Integer, SnatchTerritory> map = SnatchTerritoryService.getInstance().getTerritoryMap();
		Iterator<Entry<Integer, SnatchTerritory>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, SnatchTerritory> entry = iterator.next();
			System.err.println("   -----> id " + entry.getKey());
			System.err.println("   -----> info : ");
			SnatchTerritory tory = entry.getValue();
			t_roadPavilionBean road = BeanTemplet.getRoadPavilionBean(entry.getKey());
			System.err.println("   ----------> name : " + road.getName());
			System.err.println("   ----------> leaderName : "
					+ (null == tory.getGymLeader() ? "没有馆主" : tory.getGymLeader().getName()));
			System.err.println("   ----------> 进攻联盟id : " + tory.getOffenseGuildId());
			System.err.println("   ----------> 宣战者id : " + tory.getAttackPlayerId());
			System.err.println("   ----------> 防守联盟id : " + tory.getDefendGuildId());
			System.err.println("   ----------> 进攻方人数 : " + tory.getOffenseList().size());
			System.err.println("   ----------> 防守方人数 : " + tory.getDefendList().size());
			System.err.println("   ----------> 状态时间 : " + new Date(tory.getCountdown()).toString());
			System.err.println("   ----------> 状态 : " + tory.getStatus());
			if (tory.getCountdown() < System.currentTimeMillis()) {
				SnatchTerritoryService.getInstance().snatchTerritoryStartFight(tory);
			}

		}
		return null;
	}

}
