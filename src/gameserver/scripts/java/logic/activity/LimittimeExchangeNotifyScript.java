package logic.activity;


import org.apache.log4j.Logger;

import Message.S2CActivityMsg.LimittimeExchangeNotify;
import Message.S2CActivityMsg.LimittimeExchangeNotifyID;
import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.LimittimeExchangeConfig;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: LimittimeExchangeNotifyScript 
 * @Description: 限时兑换推送
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class LimittimeExchangeNotifyScript implements IScript {
    private static Logger LOGGER = Logger.getLogger(LimittimeExchangeNotifyScript.class);

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
		LimittimeExchangeNotify.Builder builder = LimittimeExchangeNotify.newBuilder();
		for (LimittimeExchangeConfig config : service.getAllLimittimeExchange().values()) {
			builder.addList(config.genMsg(player));
		}
		MessageUtils.send(player, player.getFactory().fetchSMessage(LimittimeExchangeNotifyID.LimittimeExchangeNotifyMsgID_VALUE,
				builder.build().toByteArray()));
	}
}
