package logic.bug;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Message.S2CSnatchTerritoryMsg.SnatchTerritoryType;
import game.core.pub.script.IScript;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.player.RoleViewService;
import game.server.logic.snatchTerritory.SnatchTerritoryService;
import game.server.logic.snatchTerritory.bean.SnatchPlayer;
import game.server.logic.snatchTerritory.bean.SnatchTerritory;

public class CheckPlayerGymInfo implements IScript {

	private Logger logger = LoggerFactory.getLogger(CheckPlayerGymInfo.class);

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
		List<Long> allPlayerId = RoleViewService.getAllPlayerId();
		for (Long playerId : allPlayerId)
		{
			Map<Long, SnatchPlayer> map = SnatchTerritoryService.getInstance().getSnatchPlayerMap();
			if (map.containsKey(playerId)) {
				logger.info("玩家 ：[ " + playerId + "  ] ，道馆数据异常！ ");
			}
			
			Player player = PlayerManager.getOffLinePlayerByPlayerId(playerId);
			if (null != player) {
				logger.info("2 玩家 ：[ " + playerId + " , name " + player.getPlayerName() + " ]！ ");
				Map<Integer, SnatchTerritory> normal = player.getSnatchTerritoryManager().getNormalTerritoryMap();
				Iterator<Entry<Integer, SnatchTerritory>> iter = normal.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<Integer, SnatchTerritory> ent = iter.next();
					SnatchTerritory ter = ent.getValue();
					if (!ter.getOffenseList().isEmpty()) {
						ter.getOffenseList().clear();
						logger.info(" 2 玩家 ：[ " + playerId + " , normal ] ，道馆数据异常！ ");
					}
					
				}
				normal = player.getSnatchTerritoryManager().getSeniorTerritoryMap();
				iter = normal.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<Integer, SnatchTerritory> ent = iter.next();
					SnatchTerritory ter = ent.getValue();
					if (!ter.getOffenseList().isEmpty()) {
						ter.getOffenseList().clear();
						logger.info("2 玩家 ：[ " + playerId + " , senior ] ，道馆数据异常！ ");
					}
					
				}
				normal = player.getSnatchTerritoryManager().getHighesTterritoryMap();
				iter = normal.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<Integer, SnatchTerritory> ent = iter.next();
					SnatchTerritory ter = ent.getValue();
					if (!ter.getOffenseList().isEmpty()) {
						ter.getOffenseList().clear();
						logger.info("2 玩家 ：[ " + playerId + " , highest ] ，道馆数据异常！ ");
					}
				}
				player.offLineSave();
			}
		}

		return null;
	}

}
