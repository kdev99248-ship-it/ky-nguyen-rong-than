package logic.journey;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.bean.t_globalBean;
import game.core.pub.script.IScript;
import game.server.logic.backpack.BackpackService;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.journey.JourneyManager;
import game.server.logic.journey.JourneyService;
import game.server.logic.journey.bean.JourneyBean;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;

/**
 * 激活旅程卡
 * @author tzyx
 *
 */
public class BuyJourneyCardScript implements IScript{
	
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
		int type = (int) script.get(Key.ARG1); // 0 
		return buyCard(player, type);
	}

	private boolean buyCard(Player player, int productId) {
		JourneyManager journeyManager = player.getJourneyManager();
		List<Item> rewards = new ArrayList<>();
		int globalId = 0;
		int type = 0;
		if (productId == 100) {
			type = 1;
			globalId = 429;
		} else if (productId == 101) {
			type = 2;
			globalId = 430;
		}
		if (type == 0) {
			return false;
		}
		t_globalBean globa = BeanTemplet.getGlobalBean(globalId);
		if (null == globa) {
			logger.error("旅程买卡错误,没有对应的全局配置,playerId:" +player.getPlayerId());
			return false;
		}
		String[] split = globa.getStr_value().split("\\|");
		// 当前月超过了全局配置奖励
		if (split.length < journeyManager.getMonth()) {
			logger.error("旅程买卡错误,当前月超过了全局配置奖励,playerId:" +player.getPlayerId() + ", month:" + journeyManager.getMonth() + ", productId" + productId);
			return false;
		}
		String[] strs = split[journeyManager.getMonth() - 1].split(";");
		for (String str : strs) {
			String[] spl = str.split("\\,");
			rewards.addAll(BeanFactory.createProps(Integer.valueOf(spl[0]), Integer.valueOf(spl[1])));
		}

		// 推送消息到客户端
		Reason reason = type == 1 ? Reason.JOURNEY_BUY_NORMAL : Reason.JOURNEY_BUY_SPECIAL;
    	player.getBackpackManager().addItems(rewards, reason, journeyManager.getMonth() + "," + journeyManager.getDay() + "," + productId);
    	BackpackService.getInstance().getRewardNotify(player, rewards);
		
		journeyManager.setRightsType(type);
		// 获取对应卡的奖励
		// 天王卡,往后激活N天
		int n = 0;
		if (type == 2) {
			t_globalBean globalBean = BeanTemplet.getGlobalBean(428);
			int num = 10;
			if (null != globalBean) {
				num = globalBean.getInt_value();
			}
			int day = journeyManager.getDay();
			while (num > 0) {
				JourneyBean journeyByDay = journeyManager.getJourneyByDay(journeyManager.getDay() + n++);
				// 没有配置了 跳出
				if (null == journeyByDay) {
					break;
				}
				if (!journeyByDay.isFinfish()) {
					journeyByDay.setFinfish(true);
					day++;
					num--;
				}
			}
			journeyManager.setDay(day);
			logger.error(player.getPlayerId() + ":玩家购买了天王卡卡,新增天数:" + n);
		}
		logger.error(player.getPlayerId() + ":玩家购买了旅程卡:" + type);
		// 推送消息到客户端
		JourneyService.getInstance().notifyJourneyBaseInfo(player);
		JourneyService.getInstance().notifyJourneyDayInfoList(player);
		// 记录动作    档位id, 解锁的天数
		LogService.getInstance().logPlayerAction(player, reason.value(), productId, n);
		return true;
	}
}
