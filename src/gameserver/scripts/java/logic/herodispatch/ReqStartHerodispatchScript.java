package logic.herodispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.C2SHerodispatchMsg.StartHerodispatchReq;
import Message.C2SHerodispatchMsg.StartHerodispatchReqID;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.hero.bean.Hero;
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
 * 开始派遣
 * @author tzyx
 *
 */
public class ReqStartHerodispatchScript implements IScript{

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
		StartHerodispatchReq req = (StartHerodispatchReq) args.get(Key.ARG1);
		reqStartHerodispatch(player, req);
		return null;
	}

	public void reqStartHerodispatch(Player player, StartHerodispatchReq req) {
		HerodispatchService service = HerodispatchService.getInstance();
		// 检查功能开放
		if (service.herodispatchFunctionOpen(player, true)) 
			return;
		// 检查任务有效性
		int index = req.getIndex();
		HerodispatchManager manager = player.getHerodispatchManager();
		if (index >= manager.getDispatchList().size()) {
			logger.info("错误的派遣任务");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5113);
			return;
		}
		Herodispatch herodispatch = manager.getDispatchList().get(index);
		if (herodispatch.getStatus() != 1) {
			logger.info("派遣任务状态错误");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5112);
			return;
		}
		// 检查英雄数量
		HerodispatchConfig config = service.getConfigMap().get(herodispatch.getDispatchId());
		if (req.getHeroIdListCount() != config.getTask_conditions1()) {
			logger.info("精灵不足,无法派遣");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5103);
			return;
		}
		// 检查精灵是否存在
		// 检查精灵占用情况
		List<Integer> idList = req.getHeroIdListList();
		Map<Integer, Hero> heroMap = player.getHeroManager().getHeroMap();
		for (Integer id : idList) {
			if (!heroMap.containsKey(id) && player.getHeroManager().getLeader().getId() != id) {
				logger.info("玩家没有该精灵:" + id);
				MessageUtils.sendPrompt(player, PromptType.ERROR, 5111);
				return;
			}
			if (manager.getUsedHeroIdList().contains(id)) {
				logger.info("精灵已使用");
				MessageUtils.sendPrompt(player, PromptType.ERROR, 5110);
				return;
			}
			
		}
		// 检查资源
		List<Item> needItems = new ArrayList<>(config.getTask_conditions2());
		if (!player.getBackpackManager().isItemNumEnough(needItems)) {
			logger.error("所需道具不足!");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5101);
			return;
		}
		player.getBackpackManager().removeItems(needItems, true, Reason.HERODISPATCH, "" + herodispatch.getDispatchId());
		service.startDispatch(manager, herodispatch, config, idList);
		// 推送给玩家
		service.notifyHerodispatch(player);
		// 记录动作    派遣配置id,英雄id列表
		LogService.getInstance().logPlayerAction(player, StartHerodispatchReqID.StartHerodispatchReqMsgID_VALUE,
				herodispatch.getDispatchId(), herodispatch.getHeroIdList());
	}
}
