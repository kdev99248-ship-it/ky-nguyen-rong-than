package logic.journey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Message.S2CJourneyMsg.JourneyBaseMsg;
import Message.S2CJourneyMsg.NotifyJourneyBaseInfo;
import Message.S2CJourneyMsg.NotifyJourneyBaseInfoID;
import game.core.pub.script.IScript;
import game.server.logic.journey.JourneyManager;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 推送旅程信息到玩家
 * @author tzyx
 *
 */
public class NotifyJourneyBaseInfoScript implements IScript{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init() {
		
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		Player player = (Player) script.get(Key.PLAYER);
		notify(player);
		return null;
	}
	
	private void notify(Player player) {
		JourneyManager journeyManager = player.getJourneyManager();
		JourneyBaseMsg.Builder genBaseMsg = journeyManager.genBaseMsg();
		NotifyJourneyBaseInfo.Builder builder = NotifyJourneyBaseInfo.newBuilder();
		builder.setBaseInfo(genBaseMsg);
		if (player.isOnline()) {
			MessageUtils.send(player, player.getFactory().fetchSMessage(NotifyJourneyBaseInfoID.NotifyJourneyBaseInfoMsgID_VALUE,
					builder.build().toByteArray()));
		}
	}
}
