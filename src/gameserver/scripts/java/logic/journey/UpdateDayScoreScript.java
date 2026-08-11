package logic.journey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.bean.t_journeyBean;
import game.core.pub.script.IScript;
import game.server.logic.journey.JourneyManager;
import game.server.logic.journey.JourneyService;
import game.server.logic.journey.bean.JourneyBean;
import game.server.logic.player.Player;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;

/**
 * 激活旅程卡
 * @author tzyx
 *
 */
public class UpdateDayScoreScript implements IScript{
	
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
		int score = (int) script.get(Key.ARG1); // 0 
		updateDayScore(player, score);
		return null;
	}

	private void updateDayScore(Player player, int score) {
		JourneyManager journeyManager = player.getJourneyManager();
		if (!journeyManager.isOpen()) {
			logger.error("玩家的旅程功能关闭:" + player.getPlayerId());
			return;
		}
		if (journeyManager.isTodayFinish()) {
			return;
		}
		int configId = journeyManager.getTodayConcfig();
		t_journeyBean config = BeanTemplet.getJourney(configId);
		JourneyBean bean = journeyManager.getJourneyByConfigId(configId);
		if (null == bean || config == null) {
			journeyManager.setOpen(false);
			logger.error("没有对应的旅程配置或玩家无对应的旅程数据:" + configId + "," + player.getPlayerId());
			return;
		}
		JourneyService.getInstance().notifyJourneyBaseInfo(player);
		if (config.getActivity_target() <= score) {
			bean.setFinfish(true);
			journeyManager.setTodayFinish(true);
			JourneyService.getInstance().notifyJourneyDayInfo(player, configId);
		}		
	}
}
