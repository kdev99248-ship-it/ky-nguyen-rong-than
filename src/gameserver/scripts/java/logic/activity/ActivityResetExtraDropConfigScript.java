package logic.activity;


import java.util.Map;

import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.ExtraDropConfig;
import game.server.logic.activity.bean.ExtraDropItemConfig;

/**
 * 
 * @ClassName: ActivityLoadExtraDropConfigScript 
 * @Description: 重新加载额外掉落配置
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class ActivityResetExtraDropConfigScript implements IScript {
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
    	Map<String, ExtraDropConfig> map = service.getAllExtraDropConfig();
    	for (ExtraDropConfig config : map.values()) {
    		if (config.getActive() == 1 && config.getOpen() == 1) {
    			for (ExtraDropItemConfig item : config.getItems()) {
    				if (item.getItemType() == 2) {
    					item.setUsedTimes(0);
    				}
				}
    		}
		}
		return null;
	}
}
