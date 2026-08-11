package logic.bug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.core.pub.script.IScript;
import game.core.pub.script.ScriptManager;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.util.ScriptArgs;

/**
 * @author duwei
 *
 * 2019年9月26日
 */
public class TestGymScript  implements IScript {
	private static Logger logger = LoggerFactory.getLogger(TestGymScript.class);
	@Override
	public void init() {
		
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs args = new ScriptArgs();
		Player player = PlayerManager.getOffLinePlayerByPlayerId(536334041157l);
		args.put(ScriptArgs.Key.PLAYER, player);
		args.put(ScriptArgs.Key.ARG1, 1);
		
		ScriptManager.getInstance().call("logic.snatchTerritory.RspAuctionGymLeaderScript", args);
		logger.info("读取脚本执行完成");
		return null;
	}

}
