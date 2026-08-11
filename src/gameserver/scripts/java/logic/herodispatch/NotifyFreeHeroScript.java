package logic.herodispatch;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.S2CHerodispatchMsg.FreeHeroNotify;
import Message.S2CHerodispatchMsg.FreeHeroNotifyId;
import game.core.pub.script.IScript;
import game.server.logic.hero.bean.Hero;
import game.server.logic.herodispatch.HerodispatchManager;
import game.server.logic.herodispatch.HerodispatchService;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 推送精灵派遣信息
 * @author tzyx
 *
 */
public class NotifyFreeHeroScript implements IScript{

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
		notifyFreeHero(player);
		return null;
	}

	public void notifyFreeHero(Player player) {
		HerodispatchService service = HerodispatchService.getInstance();
		service.initHerodispatch(player);
		if (service.herodispatchFunctionOpen(player, false)) 
			return;
		// 推送
		FreeHeroNotify.Builder builder = FreeHeroNotify.newBuilder();
		HerodispatchManager manager = player.getHerodispatchManager();
		Map<Integer, Hero> heroMap = player.getHeroManager().getHeroMap();
		List<Integer> usedHeroIdList = manager.getUsedHeroIdList();
		for (Integer id : heroMap.keySet()) {
			if (!usedHeroIdList.contains(id)) {
				builder.addFreeHeros(id);
			}
		}
		if (!usedHeroIdList.contains(player.getHeroManager().getLeader().getId())) {
			builder.addFreeHeros(player.getHeroManager().getLeader().getId());
		}
		if (builder.getFreeHerosCount() > 0) {
			MessageUtils.send(player, player.getFactory().fetchSMessage(FreeHeroNotifyId.FreeHeroNotifyMsgID_VALUE,
					builder.build().toByteArray()));
		}
	}
}
