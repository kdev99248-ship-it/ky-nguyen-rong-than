package logic.activity;


import org.apache.log4j.Logger;

import Message.S2CActivityMsg.ExtraDropNotify;
import Message.S2CActivityMsg.ExtraDropNotifyID;
import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.ExtraDropConfig;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: ExtraDropNotifyScript 
 * @Description: 额外掉落推送
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class ExtraDropNotifyScript implements IScript {
    private static Logger LOGGER = Logger.getLogger(ExtraDropNotifyScript.class);

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
		limittimeExchangeNotify(player);
		return null;
	}
	
	private void limittimeExchangeNotify(Player player) {
		ActivityService service = ActivityService.getInstance();
		ExtraDropNotify.Builder builder = ExtraDropNotify.newBuilder();
		for (ExtraDropConfig config : service.getAllExtraDropConfig().values()) {
			builder.addList(config.genBuilder());
		}
		MessageUtils.send(player, player.getFactory().fetchSMessage(ExtraDropNotifyID.ExtraDropNotifyMsgID_VALUE,
				builder.build().toByteArray()));
	}
}
