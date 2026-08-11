package logic.util;

import org.apache.log4j.Logger;

import Message.S2CPlayerMsg.PlayerType;
import Message.S2CPlayerMsg.PromptType;
import Message.Inner.GRUtil.SendPrompt2Player;
import game.core.pub.script.IScript;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 转发大服务器的提示信息
 * 
 * @author tzyx
 *
 */
public class GRSendPrompt2PlayerScript implements IScript {

	private final Logger logger = Logger.getLogger(GRSendPrompt2PlayerScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		SendPrompt2Player req = (SendPrompt2Player) script.get(ScriptArgs.Key.ARG1);
		Player player = PlayerManager.getPlayerByPlayerId(req.getPlayerId());
		if (null != player && player.isOnline() && player.getPlayerType() == PlayerType.PLAYER_VALUE) {
			if (req.hasLanguageId()) {
				MessageUtils.sendPrompt(player, PromptType.valueOf(req.getType()), req.getLanguageId());
				return null;
			}
			if (req.hasInfo()) {
				MessageUtils.sendPrompt(player, PromptType.valueOf(req.getType()), req.getInfo());
				return null;
			}
		}
		return null;
	}

}
