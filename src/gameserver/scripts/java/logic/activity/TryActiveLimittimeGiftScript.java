package logic.activity;


import java.util.Map;
import java.util.Random;

import Message.C2SActivityMsg.ActiveLimittimeGiftID;
import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityManager;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.LimittimeGift;
import game.server.logic.activity.bean.LimittimeGiftConfig;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;

/**
 * 
 * @ClassName: TryActiveLimittimeGiftScript 
 * @Description: 触发限时礼包
 * @author tzyx 
 */
public class TryActiveLimittimeGiftScript implements IScript {

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
		int type = (int) script.get(Key.ARG1);
		String str = (String) script.get(Key.ARG2);
		if (tryActiveLimittimeGift(player, type)) {
			ActivityService.getInstance().notifyLimittimeGift(player);
		}
		// 记录动作 英雄id/功能开放类型 0/关卡id
		LogService.getInstance().logPlayerAction(player, ActiveLimittimeGiftID.ActiveLimittimeGiftMsgID_VALUE,
				type,str);
		return null;
	}
    
    private boolean tryActiveLimittimeGift(Player player, int type) {
    	ActivityManager activityManager = player.getActivityManager();
    	// 功能未开启
    	if (!activityManager.isLimittimeGiftOpen()) {
    		return false;
    	}
    	LimittimeGift limittimeGift = activityManager.getLimittimeGift();
    	// 已触发限时礼包
    	if (limittimeGift.getType() != 0) {
    		return false;
    	}
    	// CD未冷却
    	if (limittimeGift.getCdTime() > 0 && System.currentTimeMillis() <= limittimeGift.getCdTime()) {
    		return false;
    	}
    	Map<Integer, LimittimeGiftConfig> allLimittimeGift = ActivityService.getInstance().getAllLimittimeGift();
    	LimittimeGiftConfig limittimeGiftConfig;
    	Random r = new Random();
    	Integer odd;
    	// 英雄整卡
    	if (type > 50000 && type < 60000) {
    		// 检查是否存在对应的配置
    		limittimeGiftConfig = allLimittimeGift.get(Integer.valueOf(1));
    		if (null == limittimeGiftConfig)
    			return false;
    		// 概率
    		odd = limittimeGiftConfig.getConditions().get(Integer.valueOf(type));
    		// 没有对应的触发概率
    		if (null == odd)
    			return false;
    		// Roll点
    		if (odd > r.nextInt(10000)) {
    			limittimeGift.setType(1);
    			limittimeGift.setEndTime(-1);
    			limittimeGift.setHeroId(type);
				return true;
    		}
    	} else {
    		// PVE
    		limittimeGiftConfig = allLimittimeGift.get(Integer.valueOf(2));
    		if (null != limittimeGiftConfig) {
        		// 概率
        		odd = limittimeGiftConfig.getConditions().get(Integer.valueOf(type));
    			if (null != odd && odd > r.nextInt(10000)) {
    				limittimeGift.setType(2);
    				limittimeGift.setEndTime(-1);
    				limittimeGift.setHeroId(0);
    				return true;
    			}
    	    	return false;
    		}
    		// PVP
    		limittimeGiftConfig = allLimittimeGift.get(Integer.valueOf(3));
    		if (null != limittimeGiftConfig) {
        		// 概率
        		odd = limittimeGiftConfig.getConditions().get(Integer.valueOf(type));
    			if (null != odd && odd > r.nextInt(10000)) {
    				limittimeGift.setType(3);
    				limittimeGift.setEndTime(-1);
    				limittimeGift.setHeroId(0);
    				return true;
    			}
    	    	return false;
    		}
    	}
    	return false;
    }
}
