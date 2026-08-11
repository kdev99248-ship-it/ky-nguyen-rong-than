package logic.activity;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityManager;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.LimittimeGift;
import game.server.logic.activity.bean.LimittimeGiftConfig;
import game.server.logic.backpack.BackpackService;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.player.Player;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;

/**
 * 
 * @ClassName: BuyLimittimeGiftScript 
 * @Description: 购买礼包,发送给玩家
 * @author tzyx 
 */
public class BuyLimittimeGiftScript implements IScript {

	private final Logger LOGGER = Logger.getLogger(BuyLimittimeGiftScript.class);

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
		String orderId = (String) script.get(Key.ARG1);
		return buyLimittimeGift(player, orderId);
	}
    
    private boolean buyLimittimeGift(Player player, String orderId) {
    	ActivityManager activityManager = player.getActivityManager();
    	// 功能未开启
    	if (!activityManager.isLimittimeGiftOpen()) {
    		LOGGER.error("限时礼包购买失败,功能未开启,playerId:" + player.getPlayerId() + ",orderId:" + orderId);
    		return false;
    	}
    	LimittimeGift limittimeGift = activityManager.getLimittimeGift();
    	// 未触发限时礼包
    	if (limittimeGift.getType() == 0) {
    		LOGGER.error("限时礼包购买失败,未触发限时礼包,playerId:" + player.getPlayerId() + ",orderId:" + orderId);
    		return false;
    	}
    	LimittimeGiftConfig config = ActivityService.getInstance().getAllLimittimeGift().get(limittimeGift.getType());
    	if (null == config) {
    		LOGGER.error("限时礼包购买失败,玩家限时礼包配置id不存在,playerId:" + player.getPlayerId() + ",type:" + limittimeGift.getType() + ",orderId:" + orderId);
    		return false;
    	}
    	// 添加道具到背包
    	List<Item> l = new ArrayList<>(config.getItems());
    	if (limittimeGift.getType() == 1) {
    		l.get(0).setId(BeanTemplet.getHeroBean(limittimeGift.getHeroId()).getFragment_id());
    	}
    	player.getBackpackManager().addItems(l, Reason.ACTIVITY_LIMITTIME_GIFT_REWARD, limittimeGift.getType() + "," + orderId);
    	// 推送消息到客户端
    	BackpackService.getInstance().getRewardNotify(player, config.getItems());
    	// 购买完成,重置数据并增加CD时间
    	limittimeGift.setType(0);
    	limittimeGift.setEndTime(0);
    	limittimeGift.setHeroId(0);
    	limittimeGift.setCdTime((BeanTemplet.getGlobalBean(419).getInt_value() * 1000) + System.currentTimeMillis());
    	// 推送到客户端
    	ActivityService.getInstance().notifyLimittimeGift(player);
    	LOGGER.error("限时礼包购买成功,playerId:" + player.getPlayerId() + ",type:" + limittimeGift.getType() + ",orderId:" + orderId + ",gift:" + limittimeGift.toJson().toJSONString());
    	return true;
    }
}
