package logic.activity;


import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityManager;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.LimittimeGift;
import game.server.logic.player.Player;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;

/**
 * 
 * @ClassName: CheckLimittimeGiftScript 
 * @Description: 限时礼包检查
 * @author tzyx 
 */
public class CheckLimittimeGiftScript implements IScript {

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
    	if (!activityManager.isLimittimeGiftOpen()) {
    		return null;
    	}
		LimittimeGift gift = player.getActivityManager().getLimittimeGift();
		ActivityService service = ActivityService.getInstance();
		long now = System.currentTimeMillis();
		// 玩家身上的活动与配置不符
		if (gift.getType() != 0 && !service.getAllLimittimeGift().containsKey(gift.getType())) {
			gift.setType(0);
			gift.setEndTime(0);
			gift.setCdTime(0);
			gift.setHeroId(0);
			return null;
		}
		// 触发了活动并且活动超时
		if (gift.getEndTime() > 0 && gift.getEndTime() <= now) {
			// 清除结束时间,清除礼包类型,设置CD时间,
			gift.setType(0);
			gift.setEndTime(0);
			gift.setHeroId(0);
			gift.setCdTime(now + (BeanTemplet.getGlobalBean(419).getInt_value() * 1000));
		}
		// CD到了,清空
		if (gift.getCdTime() > 0 && gift.getCdTime() <= now) {
			gift.setCdTime(0);
		}
		return null;
	}
}
