package logic.journey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.bean.t_journeyBean;
import game.core.pub.script.IScript;
import game.server.logic.journey.JourneyManager;
import game.server.logic.journey.JourneyService;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.thread.PlayerRestoreProcessor;
import game.server.thread.dboperator.handler.ReqSaveCrossDayPlayerHandler;

public class JourneyCrossDayScript implements IScript{
	
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
		crossDay(player);
		return null;
	}
	
	private void crossDay(Player player) {
		JourneyManager journeyManager = player.getJourneyManager();
		if (journeyManager.isTodayFinish()) {
			t_journeyBean current = BeanTemplet.getJourney(journeyManager.getTodayConcfig());
			// 配置丢失
			if (null == current) {
				journeyManager.setOpen(false);
				return;
			}
			// 判断是否配置月的最后一天
			t_journeyBean next = BeanTemplet.getJourney(journeyManager.getNextDayConcfig());
			if (null == next) {
				// 执行跨月操作
				JourneyService.getInstance().crossJourneyMonth(player);
			} else {
				// 不是最后一天,day+1
				journeyManager.setDay(journeyManager.getDay() + 1);
				journeyManager.clearTodayFinish();
			}
		}

		if (!PlayerManager.isPlayerOnline(player.getPlayerId())) {
			// 回存
			PlayerRestoreProcessor.getInstance().submitRequest(new ReqSaveCrossDayPlayerHandler(-1,
					player.toPlayerBean()));
		} else {
			// 在线推送重新推送
			JourneyService.getInstance().notifyJourneyBaseInfo(player);
			JourneyService.getInstance().notifyJourneyDayInfoList(player);
		}
	}
}
