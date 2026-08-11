package logic.bug;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Message.S2CSnatchTerritoryMsg.SnatchTerritoryStatus;
import game.core.pub.script.IScript;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.snatchTerritory.bean.SnatchTerritory;

public class PlayerSnatchTerritoryError implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		List<Player> lists = PlayerManager.getAllPlayers();
		for (Player player : lists) {
			if (player.getLevel() >= 28) {
				Map<Integer, SnatchTerritory> map = player.getSnatchTerritoryManager().getSeniorTerritoryMap();
				Iterator<Entry<Integer, SnatchTerritory>> iterator = map.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<Integer, SnatchTerritory> entry = iterator.next();
					SnatchTerritory bean = entry.getValue();
					if (bean.getStatus() == SnatchTerritoryStatus.MONSTERDEAD_VALUE) {
						if (null == bean.getOffenseList() || bean.getOffenseList().size() <= 0) {
							iterator.remove();
							System.err.println("-------------->  null == 进攻列表 ！   --> player " + player.getPlayerName()
									+ "  , id : " + player.getPlayerId());
						} else if (bean.getOffenseList().isEmpty()
								|| (bean.getOffenseList().get(0).getArriveTime() < System.currentTimeMillis())) {
							iterator.remove();
						}
						continue;
					}
				}
			}
		}
		return null;
	}

}
