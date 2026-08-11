package logic.journey;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.bean.t_journeyBean;
import game.core.pub.script.IScript;
import game.server.logic.journey.JourneyManager;
import game.server.logic.journey.bean.JourneyBean;
import game.server.logic.player.Player;
import game.server.logic.timeOpen.TimeOpenProcessor;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;

public class InitJourneyInfoScript implements IScript{
	
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
		init(player);
		return null;
	}
	
	private void init(Player player) {
		// 检查系统开放
		JourneyManager journeyManager = player.getJourneyManager();
		if (!TimeOpenProcessor.getInstance().isFuntionOpen(player, 114)) {
			journeyManager.setOpen(false);
			return;
		}
		Map<Integer, JourneyBean> journeyMap = new HashMap<>();
		// 判断是否存在当月配置
		int configId = journeyManager.getTodayConcfig();
		// 获取对应月配置
		for (;; configId++) {
			t_journeyBean config = BeanTemplet.getJourney(configId);
			if (null == config) {
				break;
			} else {
				JourneyBean bean = JourneyBean.fromConfig(config);
				journeyMap.put(bean.getConfigId(), bean);
			}
		}
		if (journeyMap.isEmpty()) {
			journeyManager.setOpen(false);
		} else {
			if (!journeyManager.isOpen()) {
				journeyManager.setOpen(true);
			}
			journeyManager.setJourneyMap(journeyMap);
		}
	}
}
