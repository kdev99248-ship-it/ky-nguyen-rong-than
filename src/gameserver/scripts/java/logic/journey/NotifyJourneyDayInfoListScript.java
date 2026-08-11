package logic.journey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Message.S2CJourneyMsg.NotifyJourneyDayInfoList;
import Message.S2CJourneyMsg.NotifyJourneyDayInfoListID;
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
public class NotifyJourneyDayInfoListScript implements IScript{
	
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
		Map<Integer, JourneyBean> journeyMap = journeyManager.getJourneyMap();
		NotifyJourneyDayInfoList.Builder builder = NotifyJourneyDayInfoList.newBuilder();
		List<JourneyBean> l = new ArrayList<>();
		for (Integer configId : journeyMap.keySet()) {
			l.add(journeyMap.get(configId));
		}
		l.sort((JourneyBean o1, JourneyBean o2) -> {
			if (o1.getConfigId() < o2.getConfigId()) {
				return -1;
			} else if (o1.getConfigId() > o2.getConfigId()) {
				return 1;
			} else {
				return 0;
			}
		});
		for (JourneyBean journeyBean : l) {
			builder.addDayInfos(journeyBean.geneMsg());
		}
		if (player.isOnline()) {
			MessageUtils.send(player, player.getFactory().fetchSMessage(NotifyJourneyDayInfoListID.NotifyJourneyDayInfoListMsgID_VALUE,
					builder.build().toByteArray()));
		}
	}
}
