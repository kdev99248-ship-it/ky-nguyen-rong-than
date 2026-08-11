package logic.herodispatch;

import org.apache.log4j.Logger;

import game.core.pub.script.IScript;
import game.server.logic.herodispatch.HerodispatchManager;
import game.server.logic.herodispatch.HerodispatchService;
import game.server.logic.herodispatch.bean.Herodispatch;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;

/**
 * 计算玩家的精灵派遣任务
 * @author tzyx
 *
 */
public class CalcHerodispatchScript implements IScript{

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
		calcHerodispatch(player);
		return null;
	}

	private void calcHerodispatch(Player player) {
		HerodispatchService service = HerodispatchService.getInstance();
		// 检查功能开放
		if (service.herodispatchFunctionOpen(player, false))
			return;
		long now = System.currentTimeMillis();
		HerodispatchManager manager = player.getHerodispatchManager();
		// 初始化派遣任务
		service.initHerodispatch(player);
		for (Herodispatch dis : manager.getDispatchList()) {
			if (dis.getStatus() == 2 && now >= dis.getEndTime()) {
				dis.setStatus(3);
				dis.setEndTime(0);
			}
		}
	}
}
