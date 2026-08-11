package logic.activity;

import Message.S2CActivityMsg.SuperSupplyMsg;
import Message.S2CActivityMsg.SuperSupplyNotify;
import Message.S2CActivityMsg.SuperSupplyNotifyID;
import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.SuperSupplyConfig;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.TimeUtils;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 推送超级补给状态
 * @author tzyx
 *
 */
public class SuperSupplyNotifyScript implements IScript {

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
		superSupplyNotify(player);
		return null;
	}

	private void superSupplyNotify(Player player) {
		SuperSupplyNotify.Builder builder = SuperSupplyNotify.newBuilder();
		SuperSupplyMsg.Builder b = SuperSupplyMsg.newBuilder();
		ActivityService service = ActivityService.getInstance();
		SuperSupplyConfig config = service.getSuperSupplyConfig();
		long end;
		// 判断当前是否过了5点,超过了用下一天判断 没超过用当天判断
		if (TimeUtils.getTodayRunTime() > 5 * 60 * 60 * 1000) {
			end = TimeUtils.addDayOfMonth(1, 5, 0, 0);
		} else {
			end = TimeUtils.addDayOfMonth(0, 5, 0, 0);
		}
		service.genSuperSupply(b, config, player.getPlayerId(), end);
		builder.setGift(b);
		MessageUtils.send(player, player.getFactory().fetchSMessage(SuperSupplyNotifyID.SuperSupplyNotifyMsgID_VALUE,
				builder.build().toByteArray()));
	}
}
