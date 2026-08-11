package logic.snatchTerritory;

import Message.C2SSnatchTerritoryMsg.ReqUseAirshipID;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.snatchTerritory.SnatchTerritoryService;
import game.server.logic.snatchTerritory.bean.SnatchPlayer;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

public class RspUseAirshipScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		Player player = (Player) script.get(ScriptArgs.Key.PLAYER);
		int itemId = (int) script.get(ScriptArgs.Key.ARG1);
		SnatchPlayer sp = SnatchTerritoryService.getInstance().getSnatchPlayerMap().get(player.getPlayerId());
		if (null != sp) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 1379);
			return null;
		}
		if (player.getSnatchTerritoryManager().containsId(itemId)) {
			player.getSnatchTerritoryManager().setAirShipId(itemId);
			MessageUtils.sendPrompt(player, PromptType.ERROR, 1373);
			// 刷新一下商店
			SnatchTerritoryService.getInstance().notifyGymShopToClientByGuildId(player);
			LogService.getInstance().logPlayerAction(player, ReqUseAirshipID.ReqUseAirshipMsgID_VALUE, itemId);
		} else {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 195);
		}
		// 玩家操作日志

		return null;
	}

}
