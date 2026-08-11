package logic.journey;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Message.S2CPlayerMsg.PromptType;
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
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 旅程领取钻石
 * @author tzyx
 *
 */
public class GetJourneyDiamondsScript implements IScript{
	
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
		getDiamonds(player, type);
		return null;
	}

	private void getDiamonds(Player player, int type) {
		JourneyManager journeyManager = player.getJourneyManager();
		if (!journeyManager.isOpen()) {
			logger.info("玩家的旅程功能关闭: " + player.getPlayerId());
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5164);
			return;
		}
		// 检查资格是否满足
		if (journeyManager.getRightsType() < 1) {
			logger.info("资格不满足: " + type + ", player: " + player.getPlayerId());
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5163);
			return;
		}
		int[] diamonds = journeyManager.calcDiamonds();
		
		Reason reason = Reason.JOURNEY_DIAMOND_NORMAL;
		if (journeyManager.getRightsType() == 1) {
			type = 0;
			reason = Reason.JOURNEY_DIAMOND_NORMAL;
		}
		if (journeyManager.getRightsType() == 2) {
			type = 1;
			reason = Reason.JOURNEY_DIAMOND_SPECIAL;
		}
		
		int diamond = diamonds[type];
		if (diamond == 0) {
			logger.info("钻石已领取: " + journeyManager.getRightsType() + ", player: " + player.getPlayerId());
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5161);
			return;
		}
		List<Item> reward = BeanFactory.createProps(-2, diamond);
		// 领取所有钻石,修改状态
		Map<Integer, JourneyBean> journeyMap = journeyManager.getJourneyMap();
		for (JourneyBean bean : journeyMap.values()) {
			if (bean.isFinfish() && (bean.isGotNormal() || bean.isGotSpecial())) {
				if (type == 0) {
					bean.setGotNormalDiamonds(true);
				} else if (type == 1) {
					bean.setGotSpecialDiamonds(true);
				}
			}
		}
		// 推送消息到客户端
		BackpackService.getInstance().getRewardNotify(player, reward);
    	player.getBackpackManager().addItems(reward, reason, journeyManager.getMonth() + "," + journeyManager.getRightsType() + "," + diamond);
    	// 推送更新消息
    	JourneyService.getInstance().notifyJourneyBaseInfo(player);
		// 记录动作    领取的哪一类累积钻石   领取的钻石数
		LogService.getInstance().logPlayerAction(player, reason.value(), type, diamond);
	}
}
