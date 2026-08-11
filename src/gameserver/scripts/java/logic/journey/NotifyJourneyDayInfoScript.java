package logic.journey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Message.S2CJourneyMsg.NotifyJourneyDayInfo;
import Message.S2CJourneyMsg.NotifyJourneyDayInfoID;
import game.core.pub.script.IScript;
import game.server.logic.journey.JourneyManager;
import game.server.logic.journey.bean.JourneyBean;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 推送旅程信息到玩家
 * @author tzyx
 *
 */
public class NotifyJourneyDayInfoScript implements IScript{
	
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
		int configId = (int) script.get(Key.ARG1);
		notify(player, configId);
		return null;
	}
	
	private void notify(Player player, int configId) {
		JourneyManager journeyManager = player.getJourneyManager();
		JourneyBean journeyBean = journeyManager.getJourneyByConfigId(configId);
		NotifyJourneyDayInfo.Builder builder = NotifyJourneyDayInfo.newBuilder();
		builder.setDayInfo(journeyBean.geneMsg());
		if (player.isOnline()) {
			MessageUtils.send(player, player.getFactory().fetchSMessage(NotifyJourneyDayInfoID.NotifyJourneyDayInfoMsgID_VALUE,
					builder.build().toByteArray()));
		}
	}
}
