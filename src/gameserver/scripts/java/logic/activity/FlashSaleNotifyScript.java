package logic.activity;


import org.apache.log4j.Logger;

import Message.S2CActivityMsg.FlashSaleNotify;
import Message.S2CActivityMsg.FlashSaleNotifyID;
import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.FlashSaleConfig;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: FlashSaleNotifyScript 
 * @Description: 限时购买推送
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class FlashSaleNotifyScript implements IScript {
    private static Logger LOGGER = Logger.getLogger(FlashSaleNotifyScript.class);

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
		ActivityService service = ActivityService.getInstance();
		FlashSaleNotify.Builder builder = FlashSaleNotify.newBuilder();
		for (FlashSaleConfig config : service.getAllFlashSale().values()) {
			builder.addList(config.genBuilder(player));
		}
		MessageUtils.send(player, player.getFactory().fetchSMessage(FlashSaleNotifyID.FlashSaleNotifyMsgID_VALUE,
				builder.build().toByteArray()));
		return null;
	}
}
