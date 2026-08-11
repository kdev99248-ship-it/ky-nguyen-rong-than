package logic.activity;

import java.util.List;

import Message.S2CActivityMsg.SuperSupplyMsg;
import Message.S2CActivityMsg.SuperSupplyNotify;
import Message.S2CActivityMsg.SuperSupplyNotifyID;
import game.core.inter.ICommand;
import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.SuperSupplyConfig;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.timeOpen.TimeOpenProcessor;
import game.server.logic.util.TimeUtils;
import game.server.util.MessageUtils;

/**
 * 推送超级补给状态
 * @author tzyx
 *
 */
public class SuperSupplyNotifyAllScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		superSupplyNotify();
		return null;
	}

	private void superSupplyNotify() {
		TimeOpenProcessor.getInstance().addCommand(new ICommand() {
			@Override
			public void clear() {
				
			}
			
			@Override
			public void action() {
				SuperSupplyNotify.Builder builder = SuperSupplyNotify.newBuilder();
				SuperSupplyMsg.Builder b;
				ActivityService service = ActivityService.getInstance();
				SuperSupplyConfig config = service.getSuperSupplyConfig();
				List<Player> players = PlayerManager.getAllPlayers();
				long end;
				// 判断当前是否过了5点,超过了用下一天判断 没超过用当天判断
				if (TimeUtils.getTodayRunTime() > 5 * 60 * 60 * 1000) {
					end = TimeUtils.addDayOfMonth(1, 5, 0, 0);
				} else {
					end = TimeUtils.addDayOfMonth(0, 5, 0, 0);
				}
				for (Player player : players) {
					if (player != null && player.isOnline()) {
						b = SuperSupplyMsg.newBuilder();
						service.genSuperSupply(b, config, player.getPlayerId(), end);
						builder.setGift(b);
						MessageUtils.send(player, player.getFactory().fetchSMessage(SuperSupplyNotifyID.SuperSupplyNotifyMsgID_VALUE,
								builder.build().toByteArray()));
					}
				}
			}
		});
	}
}
