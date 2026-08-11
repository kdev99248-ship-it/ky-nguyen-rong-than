package logic.herodispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.C2SHerodispatchMsg.CompleteHerodispatchReq;
import Message.S2CHerodispatchMsg.CompleteHerodispatchRsp;
import Message.S2CHerodispatchMsg.CompleteHerodispatchRspId;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.constant.TaskConditionType;
import game.server.logic.guild.GuildService;
import game.server.logic.herodispatch.HerodispatchManager;
import game.server.logic.herodispatch.HerodispatchService;
import game.server.logic.herodispatch.bean.Herodispatch;
import game.server.logic.herodispatch.bean.HerodispatchConfig;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 完成派遣
 * @author tzyx
 *
 */
public class ReqCompleteHerodispatchScript implements IScript{

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void init() {
		
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs args = (ScriptArgs) arg;
		Player player = (Player) args.get(Key.PLAYER);
		CompleteHerodispatchReq req = (CompleteHerodispatchReq) args.get(Key.ARG1);
		reqCompleteHerodispatch(player, req);
		return null;
	}

	public void reqCompleteHerodispatch(Player player, CompleteHerodispatchReq req) {
		HerodispatchService service = HerodispatchService.getInstance();
		// 检查功能开放
		if (service.herodispatchFunctionOpen(player, true)) 
			return;
		// 计算任务
		service.calcHerodispatch(player);
		// 检查任务有效性
		int index = req.getIndex();
		HerodispatchManager manager = player.getHerodispatchManager();
		if (index >= manager.getDispatchList().size()) {
			logger.info("错误的派遣任务");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5113);
			return;
		}
		Herodispatch herodispatch = manager.getDispatchList().remove(index);
		if (herodispatch.getStatus() != 3) {
			logger.info("派遣任务状态错误");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5112);
			manager.getDispatchList().add(index, herodispatch);
			return;
		}
		// 根据上阵英雄计算奖励
		HerodispatchConfig config = service.getConfigMap().get(herodispatch.getDispatchId());
		List<Item> addItems = new ArrayList<>(config.getItems());
		List<Item> exItems = new ArrayList<>();
		boolean moreItem = true;
		for (Integer id : config.getRecommender()) {
			if (!herodispatch.getHeroIdList().contains(id)) {
				moreItem = false;
				break;
			}
		}
		// 额外奖励
		if (moreItem) {
			exItems.addAll(new ArrayList<>(config.getRecommend_items()));
		}
		Long guildId = GuildService.getInstance().getPlayerGuildId(player.getPlayerId());
		player.getBackpackManager().addItems(addItems, Reason.HERODISPATCH_COMPLETE, "" + herodispatch.getDispatchId());
		player.getBackpackManager().addItems(exItems, Reason.HERODISPATCH_COMPLETE, "" + herodispatch.getDispatchId());
		Map<Long, Integer> scoreMap = service.getScoreMap();
		if (scoreMap.containsKey(guildId)) {
			scoreMap.put(guildId, scoreMap.get(guildId) + config.getActive_value());
		} else {
			scoreMap.put(guildId, config.getActive_value());
		}
		// 清除精灵占用
		for (Integer id : herodispatch.getHeroIdList()) {
			manager.getUsedHeroIdList().remove(id);
		}
		player.getTaskManager().updateTaskCondition(TaskConditionType.FINISH_HERODISPATCH, 1);
		// 初始化任务
		service.initHerodispatch(player);
		// 推送给玩家
		service.notifyHerodispatch(player);
		CompleteHerodispatchRsp.Builder builder = CompleteHerodispatchRsp.newBuilder();
		for (Item item : addItems) {
			builder.addItems(item.genBuilder());
		}
		for (Item item : exItems) {
			builder.addExItems(item.genBuilder());
		}
		
		// 推送获取物品信息
		MessageUtils.send(player, player.getFactory().fetchSMessage(CompleteHerodispatchRspId.CompleteHerodispatchRspMsgID_VALUE,
				builder.build().toByteArray()));
		// 记录动作    派遣配置id,英雄id列表,获取的道具列表
		LogService.getInstance().logPlayerAction(player, CompleteHerodispatchRspId.CompleteHerodispatchRspMsgID_VALUE,
				herodispatch.getDispatchId(), herodispatch.getHeroIdList(), addItems);
	}
}
