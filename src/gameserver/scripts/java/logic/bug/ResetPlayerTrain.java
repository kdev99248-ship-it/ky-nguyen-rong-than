package logic.bug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.core.pub.script.IScript;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.player.RoleViewService;


/**
 * 重置训练评级
 * @author tzyx
 *
 */
public class ResetPlayerTrain implements IScript{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init() {
		
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public Object call(String scriptName, Object arg) {
		for (Long pid : RoleViewService.getAllPlayerId())
		{
			Player player = PlayerManager.getOffLinePlayerByPlayerId(pid);
			player.getHeroManager().setTrainLvl(0);
			player.getHeroManager().setTrainValueMax(0);
			logger.error("玩家:" + player.getPlayerId() + ",已重置训练评级数据");
			if (!PlayerManager.isPlayerOnline(pid)) {// 在线 提交到GameLine处理
				player.offLineSave();
			}
		}
		return null;
	}

}
