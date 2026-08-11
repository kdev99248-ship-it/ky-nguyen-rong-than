package logic.activity;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hgame.gm_api.bean.LimittimeExchangeBean;

import game.core.pub.script.IScript;
import game.server.db.game.dao.LimittimeExchangeDao;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.LimittimeExchangeConfig;
import game.server.logic.activity.bean.LimittimeExchangeItem;

/**
 * 
 * @ClassName: ActivityLoadLimittimeExchangeConfigScript 
 * @Description: 加载限时兑换配置
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class ActivityLoadLimittimeExchangeConfigScript implements IScript {
     private static Logger LOGGER = Logger.getLogger(ActivityLoadLimittimeExchangeConfigScript.class);

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Object call(String scriptName, Object arg) {
    	Map<String, LimittimeExchangeConfig> m = new ConcurrentHashMap<>();
		List<LimittimeExchangeBean> all = LimittimeExchangeDao.selectAll();
		LimittimeExchangeConfig config;
		try {
			for (LimittimeExchangeBean extraDropBean : all) {
				config = parseConfig(extraDropBean);
				m.put(config.getId(), config);
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}
		LimittimeExchangeConfig temp1;
		LimittimeExchangeConfig temp2;
		ActivityService service = ActivityService.getInstance();
		Map<String, LimittimeExchangeConfig> allExtraDropConfig = service.getAllLimittimeExchange();
		int allTime;
		for (String key : allExtraDropConfig.keySet()) {
			temp1 = allExtraDropConfig.get(key);
			if (m.containsKey(key)) {
				temp2 = m.get(key);
				for (LimittimeExchangeItem item1 : temp1.getItems()) {
					for (LimittimeExchangeItem item2 : temp2.getItems()) {
						if (item1.getId().equals(item2.getId())) {
							item1.setUsedTimes(item2.getUsedTimes());
							allTime = 0;
							for (Integer time : item1.getUsedTimes().values()) {
								allTime += time;
							}
							item1.setAllUsedTimes(allTime);
						}
					}
				}
			}
		}
		service.setAllLimittimeExchange(m);
		return null;
	}

    private LimittimeExchangeConfig parseConfig(LimittimeExchangeBean bean) throws Exception {
    	LimittimeExchangeConfig config = new LimittimeExchangeConfig();
    	config.setId(bean.getId());
    	config.setName(bean.getName());
    	config.setRemark(bean.getRemark());
    	config.setTimeType(bean.getTimeType());
    	// 解析自然时间
    	long temp = 0;
    	for (String str : bean.getActiveTime().split(",")) {
    		config.getActiveTime().add(Long.valueOf(str));
    		if (temp != 0) {
    			// 开始时间不能大于结束时间
    			if (temp >= config.getActiveTime().get(config.getActiveTime().size() - 1)) {
    				throw new Exception("限时兑换开始时间不能大于结束时间");
    			}
    			temp = 0;
    		} else {
    			temp = config.getActiveTime().get(config.getActiveTime().size() - 1);
    		}
		}
    	if (temp > 0) {
    		throw new Exception("限时兑换时间配置错误,没有结束时间");
    	}
    	// 解析开服时间
    	Integer start = 0;
    	Integer end = 0;
    	List<Integer> openServerTime = config.getOpenServerTime();
    	if (!bean.getOpenServerTime().equals("-1")) {
    		for (String str : bean.getOpenServerTime().split(",")) {
    			if (str.contains("-")) {
    				start = Integer.valueOf(str.split("-")[0]);
    				end = Integer.valueOf(str.split("-")[1]);
    				if (start > end) {
    					throw new Exception("限时兑换时间配置错误,错误的开服时间");
    				}
    				for (int i = start; i < end; i++) {
    					if (!openServerTime.contains(i)) {
    						openServerTime.add(i);
    					}
    				}
    			} else {
    				if (!openServerTime.contains(Integer.valueOf(str))) {
    					openServerTime.add(Integer.valueOf(str));
    				}
    			}
    		}
    	} else {
    		openServerTime.clear();
    		openServerTime.add(-1);
    	}
    	config.setWeight(bean.getWeight());
    	for (String str : bean.getImgUrl().split(",")) {
    		config.getImgUrls().add(str);
		}
    	config.setItems(parseItemConfig(bean.getItemStr()));
    	config.setOpen(bean.getOpen());
    	return config;
    }
    
    /** ExtraDropItemConfig的json字符串 */
    private List<LimittimeExchangeItem> parseItemConfig(String str) {
    	JSONArray jsonArray = JSONArray.parseArray(str);
    	List<LimittimeExchangeItem> l = new ArrayList<>();
    	LimittimeExchangeItem config;
    	JSONObject json;
    	for (int i = 0; i < jsonArray.size(); i++) {
    		json = jsonArray.getJSONObject(i);
    		config = new LimittimeExchangeItem();
    		config.fromJson(json);
    		l.add(config);
		}
    	return l;
    }
}
