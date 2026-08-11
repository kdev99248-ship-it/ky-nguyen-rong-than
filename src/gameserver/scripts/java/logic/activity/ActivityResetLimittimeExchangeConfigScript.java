package logic.activity;


import java.util.Map;

import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.LimittimeExchangeConfig;
import game.server.logic.activity.bean.LimittimeExchangeItem;

/**
 * 
 * @ClassName: ActivityResetLimittimeExchangeConfigScript 
 * @Description: 重新加载限时兑换配置
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class ActivityResetLimittimeExchangeConfigScript implements IScript {
//     private static Logger LOGGER = Logger.getLogger(ActivityResetExtraDropConfigScript.class);

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Object call(String scriptName, Object arg) {
    	ActivityService service = ActivityService.getInstance();
    	Map<String, LimittimeExchangeConfig> map = service.getAllLimittimeExchange();
    	for (LimittimeExchangeConfig config : map.values()) {
    		if (config.getActive() == 1 && config.getOpen() == 1) {
    			for (LimittimeExchangeItem item : config.getItems()) {
					item.getUsedTimes().clear();
				}
    		}
		}
		return null;
	}
}
