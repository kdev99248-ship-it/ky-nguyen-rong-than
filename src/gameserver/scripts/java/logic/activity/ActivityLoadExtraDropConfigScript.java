package logic.activity;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hgame.gm_api.bean.ExtraDropBean;

import game.core.pub.script.IScript;
import game.server.db.game.dao.ExtraDropDao;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.ExtraDropConfig;
import game.server.logic.activity.bean.ExtraDropItemConfig;

/**
 * 
 * @ClassName: ActivityLoadExtraDropConfigScript 
 * @Description: 加载额外掉落配置
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class ActivityLoadExtraDropConfigScript implements IScript {
     private static Logger LOGGER = Logger.getLogger(ActivityLoadExtraDropConfigScript.class);

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Object call(String scriptName, Object arg) {
    	Map<String, ExtraDropConfig> m = new ConcurrentHashMap<>();
		List<ExtraDropBean> all = ExtraDropDao.selectAll();
		ExtraDropConfig config;
		try {
			for (ExtraDropBean extraDropBean : all) {
				config = parseConfig(extraDropBean);
				m.put(config.getId(), config);
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}
		ExtraDropConfig temp1;
		ExtraDropConfig temp2;
		ActivityService service = ActivityService.getInstance();
		Map<String, ExtraDropConfig> allExtraDropConfig = service.getAllExtraDropConfig();
		for (String key : allExtraDropConfig.keySet()) {
			temp1 = allExtraDropConfig.get(key);
			if (m.containsKey(key)) {
				temp2 = m.get(key);
				for (ExtraDropItemConfig item1 : temp1.getItems()) {
					for (ExtraDropItemConfig item2 : temp2.getItems()) {
						if (item1.getId().equals(item2.getId())) {
							item1.setUsedTimes(item2.getUsedTimes());
						}
					}
				}
			}
		}
		service.setAllExtraDropConfig(m);
		return null;
	}

    private ExtraDropConfig parseConfig(ExtraDropBean bean) throws Exception {
    	ExtraDropConfig config = new ExtraDropConfig();
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
    				throw new Exception("额外掉落开始时间不能大于结束时间");
    			}
    			temp = 0;
    		} else {
    			temp = config.getActiveTime().get(config.getActiveTime().size() - 1);
    		}
		}
    	if (temp > 0) {
    		throw new Exception("额外掉落时间配置错误,没有结束时间");
    	}
    	// 解析开服时间
    	Integer start = 0;
    	Integer end = 0;
    	List<Integer> openServerTime = config.getOpenServerTime();
		for (String str : bean.getOpenServerTime().split(",")) {
			if (str.contains("-")) {
				start = Integer.valueOf(str.split("-")[0]);
				end = Integer.valueOf(str.split("-")[1]);
				if (start > end) {
					throw new Exception("额外掉落时间配置错误,错误的开服时间");
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
    	config.setWeight(bean.getWeight());
    	config.setImgUrl(bean.getImgUrl());
    	config.setItems(parseItemConfig(bean.getItemStr()));
    	config.setInfo(bean.getInfo());
    	config.setJumpId(bean.getJumpId());
    	config.setOpen(bean.getOpen());
    	return config;
    }
    
    /** ExtraDropItemConfig的json字符串 */
    private List<ExtraDropItemConfig> parseItemConfig(String str) {
    	JSONArray jsonArray = JSONArray.parseArray(str);
    	List<ExtraDropItemConfig> l = new ArrayList<>();
    	ExtraDropItemConfig config;
    	JSONObject json;
    	for (int i = 0; i < jsonArray.size(); i++) {
    		json = jsonArray.getJSONObject(i);
    		config = new ExtraDropItemConfig();
    		config.fromJson(json);
    		l.add(config);
		}
    	return l;
    }
}
