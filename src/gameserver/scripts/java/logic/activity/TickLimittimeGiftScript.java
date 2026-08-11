package logic.activity;


import Message.C2SActivityMsg.TickLimittimeGiftID;
import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityManager;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.LimittimeGift;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;

/**
 * 
 * @ClassName: TickLimittimeGiftScript 
 * @Description: 激活限时礼包,开始倒计时
 * @author tzyx 
 */
public class TickLimittimeGiftScript implements IScript {

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
		activeLimittimeGift(player);
		return null;
	}
    
    private void activeLimittimeGift(Player player) {
    	ActivityManager activityManager = player.getActivityManager();
    	// 功能未开启
    	if (!activityManager.isLimittimeGiftOpen()) {
    		return;
    	}
    	LimittimeGift limittimeGift = activityManager.getLimittimeGift();
    	// 未触发限时礼包
    	if (limittimeGift.getType() == 0) {
    		return;
    	}
    	// 已开始倒计时
    	if (limittimeGift.getEndTime() > 0) {
    		return;
    	}
    	// 设置倒计时结束时间
    	limittimeGift.setEndTime((BeanTemplet.getGlobalBean(417).getInt_value() * 1000) + System.currentTimeMillis());
    	// 推送到客户端
    	ActivityService.getInstance().notifyLimittimeGift(player);
		// 记录动作 礼包类型
		LogService.getInstance().logPlayerAction(player, TickLimittimeGiftID.TickLimittimeGiftMsgID_VALUE, limittimeGift.getType());
    }
}
