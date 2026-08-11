package logic.activity;

import org.apache.log4j.Logger;

import Message.S2COperateActivityMsg.SlotMachinesHistoryRsp;
import Message.S2COperateActivityMsg.SlotMachinesHistoryRspID;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 获取探险训练家历史信息
 * @author tzyx
 *
 */
public class GetSlotMachinesHistoryScript implements IScript {
	private static Logger LOGGER = Logger.getLogger(GetSlotMachinesHistoryScript.class);

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
		getSlotMachinesHistory(player);
		return null;
	}

	private void getSlotMachinesHistory(Player player) {
		ActivityService service = ActivityService.getInstance();
		if (service.getSlotMachinesConfig() == null || service.getSlotMachinesConfig().getOpen() == 0) {
			LOGGER.error("活动未开启");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 209);
			return;
		}
		SlotMachinesHistoryRsp.Builder builder = SlotMachinesHistoryRsp.newBuilder();
		int i = 0;
		for (String info : service.getSlotMachinesHistory()) {
			if (i++ >= 50) {
				break;
			}
			builder.addInfoList(info);
		}
		MessageUtils.send(player, player.getFactory().fetchSMessage(SlotMachinesHistoryRspID.SlotMachinesHistoryRspMsgID_VALUE,
				builder.build().toByteArray()));
	}
}
