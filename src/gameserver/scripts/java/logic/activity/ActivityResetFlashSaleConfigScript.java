package logic.activity;


import java.util.Map;

import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.FlashSaleConfig;
import game.server.logic.activity.bean.FlashSaleItem;

/**
 * 
 * @ClassName: ActivityResetFlashSaleConfigScript 
 * @Description: 重新加载限时抢购配置
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class ActivityResetFlashSaleConfigScript implements IScript {
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
    	Map<String, FlashSaleConfig> map = service.getAllFlashSale();
    	for (FlashSaleConfig config : map.values()) {
    		if (config.getActive() == 1 && config.getOpen() == 1) {
    			for (FlashSaleItem item : config.getItems()) {
					item.getUsedTimes().clear();
				}
    		}
		}
		return null;
	}
}
