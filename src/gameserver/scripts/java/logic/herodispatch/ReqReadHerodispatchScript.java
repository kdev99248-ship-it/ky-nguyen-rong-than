package logic.herodispatch;

import org.apache.log4j.Logger;

import Message.C2SHerodispatchMsg.ReadHerodispatchReq;
import Message.C2SHerodispatchMsg.ReadHerodispatchReqID;
import game.core.pub.script.IScript;
import game.server.logic.herodispatch.HerodispatchManager;
import game.server.logic.herodispatch.HerodispatchService;
import game.server.logic.herodispatch.bean.Herodispatch;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;

/**
 * 阅读派遣信息
 * @author tzyx
 *
 */
public class ReqReadHerodispatchScript implements IScript{

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
		ReadHerodispatchReq req = (ReadHerodispatchReq) args.get(Key.ARG1);
		reqReadHerodispatch(player, req);
		return null;
	}

	public void reqReadHerodispatch(Player player, ReadHerodispatchReq req) {
		HerodispatchService service = HerodispatchService.getInstance();
		// 检查功能开放
		if (service.herodispatchFunctionOpen(player, false)) 
			return;
		// 检查任务有效性
		int index = req.getIndex();
		HerodispatchManager manager = player.getHerodispatchManager();
		if (index >= manager.getDispatchList().size()) {
			logger.info("错误的派遣任务");
			return;
		}
		Herodispatch herodispatch = manager.getDispatchList().get(index);
		if (herodispatch.getStatus() != 1) {
			logger.info("派遣任务状态错误");
			return;
		}
		herodispatch.setRead(true);
		// 记录动作    派遣配置id
		LogService.getInstance().logPlayerAction(player, ReadHerodispatchReqID.ReadHerodispatchReqMsgID_VALUE,
				herodispatch.getDispatchId());
	}
}
