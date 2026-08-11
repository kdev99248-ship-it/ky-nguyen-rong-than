package logic.journey;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.bean.t_journeyBean;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.journey.JourneyManager;
import game.server.logic.journey.JourneyService;
import game.server.logic.mail.MailService;
import game.server.logic.player.Player;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;

public class JourneyCrossMonthScript implements IScript{
	
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
		crossMonth(player);
		return null;
	}
	
	private void crossMonth(Player player) {
		JourneyManager journeyManager = player.getJourneyManager();
		// 补发未领取的奖励
		Map<Integer, Integer> calcRewards = journeyManager.calcRewards();
		if (!calcRewards.isEmpty()) {
			MailService.getInstance().sendSysMail2Player(player.getPlayerId(), 62, null, Reason.JOURNEY_REWARD_MAIL, "", null,
					System.currentTimeMillis(), calcRewards);
		}
		// 补发未领取的钻石
		int[] calcDiamonds = journeyManager.calcDiamondsCrossMonth();
		// 购买过天王卡
		if (journeyManager.getRightsType() > 1) {
			int specialDiamonds = calcDiamonds[1];
			if (specialDiamonds > 0) {
				Map<Integer, Integer> adjunctMap = new HashMap<>();
				adjunctMap.put(-2, specialDiamonds);
				MailService.getInstance().sendSysMail2Player(player.getPlayerId(), 64, null, Reason.JOURNEY_DIAMOND_SPECIAL_MAIL, "", null,
						System.currentTimeMillis(), adjunctMap);
			}
		} else if (journeyManager.getRightsType() <= 1) {
			int diamonds = calcDiamonds[0];
			// 没有领取过普通钻石
			if (diamonds > 0) {
				Map<Integer, Integer> adjunctMap = new HashMap<>();
				adjunctMap.put(-2, diamonds);
				MailService.getInstance().sendSysMail2Player(player.getPlayerId(), 63, null, Reason.JOURNEY_DIAMOND_NORMAL_MAIL, "", null,
						System.currentTimeMillis(), adjunctMap);
			}
		}
		// 判断是否存在下个月配置
		int id = journeyManager.getNextMonthConcfig();
		t_journeyBean config = BeanTemplet.getJourney(id);
		// 重置日
		journeyManager.setDay(1);
		// 月数+1,
		journeyManager.setMonth(journeyManager.getMonth() + 1);
		// 重置权益
		journeyManager.setRightsType(0);
		journeyManager.clearTodayFinish();
		// 重置数据
		journeyManager.getJourneyMap().clear();
		if (null == config) {
			// 没有新配置,关闭功能
			journeyManager.setOpen(false);
		} else {
			// 初始化旅程信息
			JourneyService.getInstance().initJourneyInfo(player);
		}
	}
}
