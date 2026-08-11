package logic.herodispatch;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Message.C2SHerodispatchMsg.CancelHerodispatchReq;
import Message.C2SHerodispatchMsg.CancelHerodispatchReqID;
import Message.S2CHerodispatchMsg.CancelHerodispatchRsp;
import Message.S2CHerodispatchMsg.CancelHerodispatchRspID;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.herodispatch.HerodispatchManager;
import game.server.logic.herodispatch.HerodispatchService;
import game.server.logic.herodispatch.bean.Herodispatch;
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
public class ReqCancelHerodispatchScript implements IScript{

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
		CancelHerodispatchReq req = (CancelHerodispatchReq) args.get(Key.ARG1);
		reqStartHerodispatch(player, req);
		return null;
	}

	public void reqStartHerodispatch(Player player, CancelHerodispatchReq req) {
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
		if (herodispatch.getStatus() != 2) {
			logger.info("派遣任务状态错误");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5112);
			return;
		}
		herodispatch.setStatus(1);
		herodispatch.setEndTime(0);
		for (Integer id : herodispatch.getHeroIdList()) {
			manager.getUsedHeroIdList().remove(id);
		}
		List<Integer> ids = new ArrayList<>(herodispatch.getHeroIdList());
		herodispatch.getHeroIdList().clear();
		// 推送给玩家
		service.notifyHerodispatch(player);
		MessageUtils.send(player, player.getFactory().fetchSMessage(CancelHerodispatchRspID.CancelHerodispatchRspMsgID_VALUE,
				CancelHerodispatchRsp.newBuilder().build().toByteArray()));
		// 记录动作    派遣配置id,英雄id列表
		LogService.getInstance().logPlayerAction(player, CancelHerodispatchReqID.CancelHerodispatchReqMsgID_VALUE,
				herodispatch.getDispatchId(), ids);
	}
}
