package logic.activity;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Message.S2CActivityMsg.LimittimeGiftMsg;
import Message.S2CActivityMsg.LimittimeGiftNotify;
import Message.S2CActivityMsg.LimittimeGiftNotifyID;
import Message.S2CBackpackMsg.PropInfo;
import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityManager;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.LimittimeGift;
import game.server.logic.activity.bean.LimittimeGiftConfig;
import game.server.logic.item.bean.Item;
import game.server.logic.player.Player;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: LimittimeGiftNotifyScript 
 * @Description: 限时礼包推送
 * @author tzyx 
 */
public class LimittimeGiftNotifyScript implements IScript {
    private static Logger LOGGER = Logger.getLogger(LimittimeGiftNotifyScript.class);

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
		ActivityManager activityManager = player.getActivityManager();
    	// 功能未开启
    	if (!activityManager.isLimittimeGiftOpen())
    		return null;
		LimittimeGift gift = activityManager.getLimittimeGift();
		LimittimeGiftNotify.Builder builder = LimittimeGiftNotify.newBuilder();
		LimittimeGiftMsg.Builder b = LimittimeGiftMsg.newBuilder();
		b.setEndTime(gift.getEndTime());
		b.setType(gift.getType());
		LimittimeGiftConfig config = ActivityService.getInstance().getAllLimittimeGift().get(gift.getType());
		if (null != config) {
			List<Item> items = new ArrayList<>(config.getItems());
			if (gift.getType() == 1) {
				items.get(0).setId(BeanTemplet.getHeroBean(gift.getHeroId()).getFragment_id());
			}
            PropInfo.Builder propInfo;
			for (Item item : items) {
				propInfo = PropInfo.newBuilder();
				propInfo.setId(item.getId());
				propInfo.setNum(item.getNum());
				b.addAwards(propInfo);
			}
		}
		builder.setGift(b);
		MessageUtils.send(player, player.getFactory().fetchSMessage(LimittimeGiftNotifyID.LimittimeGiftNotifyMsgID_VALUE,
				builder.build().toByteArray()));
		LOGGER.info("推送限时礼包到玩家:" + player.getPlayerId() + ",gift:" + gift.toJson().toJSONString());
		return null;
	}
}
