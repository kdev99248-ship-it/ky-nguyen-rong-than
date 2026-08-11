package logic.rune;

import org.apache.log4j.Logger;

import Message.C2SRuneMsg.GetRuneShopReq;
import game.core.pub.script.IScript;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;

/**
 * 分解符文
 * @author tzyx
 *
 */
public class GetRuneShopScript implements IScript {

	private final Logger logger = Logger.getLogger(GetRuneShopScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		GetRuneShopReq req = (GetRuneShopReq) script.get(ScriptArgs.Key.ARG1);
		Player player = (Player) script.get(ScriptArgs.Key.PLAYER);
		getRuneShop(req, player);
		return null;
	}

	private void getRuneShop(GetRuneShopReq req, Player player) {
		//TODO
	}
}
