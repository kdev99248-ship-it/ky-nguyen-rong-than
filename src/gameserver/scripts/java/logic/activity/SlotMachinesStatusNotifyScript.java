package logic.activity;

import Message.S2COperateActivityMsg.SlotMachinesStatusNotify;
import Message.S2COperateActivityMsg.SlotMachinesStatusNotifyID;
import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.SlotMachines;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 推送探险训练家活动状态
 * @author tzyx
 *
 */
public class SlotMachinesStatusNotifyScript implements IScript {

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
		slotMachinesStatusNotify(player);
		return null;
	}

	private void slotMachinesStatusNotify(Player player) {
		SlotMachinesStatusNotify.Builder builder = SlotMachinesStatusNotify.newBuilder();
		ActivityService service = ActivityService.getInstance();
		if (service.getSlotMachinesConfig() == null || service.getSlotMachinesConfig().getOpen() == 0) {
			builder.setStatus(0);
			builder.setFreeTimes(0);
		} else {
			builder.setStatus(1);
			SlotMachines slotMachines = service.getSlotMachinesData().get(player.getPlayerId());
			if (null != slotMachines) {
				builder.setFreeTimes(slotMachines.getFreeTimes());
			} else {
				builder.setFreeTimes(service.getSlotMachinesConfig().getFreeTime());
			}
		}
		MessageUtils.send(player, player.getFactory().fetchSMessage(SlotMachinesStatusNotifyID.SlotMachinesStatusNotifyMsgID_VALUE,
				builder.build().toByteArray()));
	}
}
