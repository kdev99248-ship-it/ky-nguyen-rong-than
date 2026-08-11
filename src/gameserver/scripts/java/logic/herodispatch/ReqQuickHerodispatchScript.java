package logic.herodispatch;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Message.C2SHerodispatchMsg.QuickHerodispatchReq;
import Message.S2CHerodispatchMsg.QuickHerodispatchRsp;
import Message.S2CHerodispatchMsg.QuickHerodispatchRspID;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.hero.HeroManager;
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
 * 快速派遣
 * @author tzyx
 *
 */
public class ReqQuickHerodispatchScript implements IScript{

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
		QuickHerodispatchReq req = (QuickHerodispatchReq) args.get(Key.ARG1);
		reqQuickHerodispatch(player, req);
		return null;
	}

	public void reqQuickHerodispatch(Player player, QuickHerodispatchReq req) {
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
		HerodispatchConfig config = service.getConfigMap().get(herodispatch.getDispatchId());
		// 检查资源
		List<Item> needItems = new ArrayList<>(config.getTask_conditions2());
		if (!player.getBackpackManager().isItemNumEnough(needItems)) {
			logger.error("所需道具不足!");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5101);
			return;
		}
		List<Integer> recommender = config.getRecommender();
		// 检查空闲精灵数量是否足够        可上阵英雄数量 + 皮卡丘 > 配置所需数量 + 已派遣数量
		HeroManager heroManager = player.getHeroManager();
		if (heroManager.getHeroMap().size() + 1 < recommender.size() + manager.getUsedHeroIdList().size()) {
			logger.info("精灵不足,无法派遣");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5103);
			return;
		}
		List<Integer> idList = new ArrayList<>();
		// 按配置设置派遣
		for (Integer id : recommender) {
			if ((heroManager.getHeroMap().containsKey(id) || heroManager.getLeader().getId() == id) && !manager.getUsedHeroIdList().contains(id)) {
				if (!idList.contains(id)) {
					idList.add(id);
				}
			}
		}
		// 没有凑够数量的情况下,用其他精灵补充
		if (config.getTask_conditions1() > idList.size()) {
			// 优先用皮卡丘填充
			if (!manager.getUsedHeroIdList().contains(heroManager.getLeader().getId())) {
				if (!idList.contains(heroManager.getLeader().getId())) {
					idList.add(heroManager.getLeader().getId());
				}
			}
			// 皮卡丘上了还不够,用其他可上阵精灵填充,顺序随机
			int num = config.getTask_conditions1() - idList.size();
			for (int i = 0; i < num; i++) {
				a:for (Integer id : heroManager.getHeroMap().keySet()) {
					if (!manager.getUsedHeroIdList().contains(id) && !idList.contains(id)) {
						idList.add(id);
						break a;
					}
				}
			}
		}
		player.getBackpackManager().removeItems(needItems, true, Reason.HERODISPATCH_QUICK, "" + herodispatch.getDispatchId());
		service.startDispatch(manager, herodispatch, config, idList);
		herodispatch.setStatus(2);
		herodispatch.setEndTime(System.currentTimeMillis() + (config.getTask_conditions3() * 1000));
		// 推送给玩家
		service.notifyHerodispatch(player);
		MessageUtils.send(player, player.getFactory().fetchSMessage(QuickHerodispatchRspID.QuickHerodispatchRspMsgID_VALUE,
				QuickHerodispatchRsp.newBuilder().build().toByteArray()));
		// 记录动作    派遣配置id,英雄id列表
		LogService.getInstance().logPlayerAction(player, QuickHerodispatchRspID.QuickHerodispatchRspMsgID_VALUE,
				herodispatch.getDispatchId(), herodispatch.getHeroIdList());
	}
}
