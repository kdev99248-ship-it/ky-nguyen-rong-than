package logic.journey;

import java.util.List;

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
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

public class GetJourneyRewardScript implements IScript{
	
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
		int type = (int) script.get(Key.ARG2); // 0 
		getReward(player, configId, type);
		return null;
	}
	
	private void getReward(Player player, int configId, int type) {
		JourneyManager journeyManager = player.getJourneyManager();
		if (!journeyManager.isOpen()) {
			logger.info("玩家的旅程功能关闭: " + player.getPlayerId());
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5164);
			return;
		}
		// 检查配置是否存在
		JourneyBean journeyBean = journeyManager.getJourneyByConfigId(configId);
		if (null == journeyBean) {
			logger.info("不存在对应的配置: " + configId + ", player: " + player.getPlayerId());
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5164);
			return;
		}
		// 检查是否完成
		if (!journeyBean.isFinfish()) {
			logger.info("旅程未完成: " + configId + ", player: " + player.getPlayerId());
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5163);
			return;
		}
		// 检查资格是否满足
		if (type > journeyManager.getRightsType()) {
			logger.info("资格不满足: " + type + ", player: " + player.getPlayerId());
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5163);
			return;
		}
		// 检查重复领取
		if (type == 0) {
			if (journeyBean.isGotNormal()) {
				logger.info("奖励已领取: " + type + ", configId:" + configId + ", player: " + player.getPlayerId());
				MessageUtils.sendPrompt(player, PromptType.ERROR, 5162);
				return;
			}
		} else if (type == 1) {
			if (journeyBean.isGotSpecial()) {
				logger.info("奖励已领取: " + type + ", day:" + configId + ", player: " + player.getPlayerId());
				MessageUtils.sendPrompt(player, PromptType.ERROR, 5162);
				return;
			}
		} else {
			logger.info("类型参数错误: " + type + ", player: " + player.getPlayerId());
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5164);
			return;
		}
		List<Item> dayReward = journeyManager.getDayReward(configId, type);
		if (null == dayReward || dayReward.isEmpty()) {
			logger.info("领取奖励错误,,没有奖励,configId:" + configId + ",type:" + type + ", player: " + player.getPlayerId());
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5164);
			return;
		}
		// 推送消息到客户端
		player.getBackpackManager().addItems(dayReward, Reason.JOURNEY_REWARD, journeyManager.getMonth() + "," + configId + "," + type);
		BackpackService.getInstance().getRewardNotify(player, dayReward);
    	// 推送更新消息
    	JourneyService.getInstance().notifyJourneyDayInfo(player, configId);
    	JourneyService.getInstance().notifyJourneyBaseInfo(player);
		// 记录动作    领取类型,领取的配置id
		LogService.getInstance().logPlayerAction(player, Reason.JOURNEY_REWARD.value(), type, configId);
	}
}
