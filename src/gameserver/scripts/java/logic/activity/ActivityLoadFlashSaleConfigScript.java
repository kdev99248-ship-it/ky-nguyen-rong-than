package logic.activity;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hgame.gm_api.bean.FlashSaleBean;

import game.core.pub.script.IScript;
import game.server.db.game.dao.FlashSaleDao;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.FlashSaleConfig;
import game.server.logic.activity.bean.FlashSaleItem;
import game.server.logic.util.BeanTemplet;

/**
 * 
 * @ClassName: ActivityLoadFlashSaleConfigScript 
 * @Description: 加载限时抢购配置
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class ActivityLoadFlashSaleConfigScript implements IScript {
     private static Logger LOGGER = Logger.getLogger(ActivityLoadFlashSaleConfigScript.class);

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Object call(String scriptName, Object arg) {
    	Map<String, FlashSaleConfig> m = new ConcurrentHashMap<>();
		List<FlashSaleBean> all = FlashSaleDao.selectAll();
		FlashSaleConfig config;
		try {
			for (FlashSaleBean bean : all) {
				config = parseConfig(bean);
				m.put(config.getId(), config);
			}
		} catch (Exception e) {
			LOGGER.error(e);
		}
		FlashSaleConfig temp1;
		FlashSaleConfig temp2;
		ActivityService service = ActivityService.getInstance();
		Map<String, FlashSaleConfig> allFlashSale = service.getAllFlashSale();
		int allTime;
		for (String key : allFlashSale.keySet()) {
			temp1 = allFlashSale.get(key);
			if (m.containsKey(key)) {
				temp2 = m.get(key);
				for (FlashSaleItem item1 : temp1.getItems()) {
					for (FlashSaleItem item2 : temp2.getItems()) {
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
		if (!m.isEmpty()) {
			service.setAllFlashSale(m);
		}
		return null;
	}

    private FlashSaleConfig parseConfig(FlashSaleBean bean) throws Exception {
    	FlashSaleConfig config = new FlashSaleConfig();
    	config.setId(bean.getId());
    	config.setName(bean.getName());
    	config.setRemark(bean.getRemark());
    	config.setTimeType(bean.getTimeType());
    	// 解析自然时间
    	String[] split = bean.getActiveTime().split(",");
    	if (split.length % 3 != 0) {
    		throw new Exception("限时抢购自然时间格式错误");
    	}
    	long s1;
    	long s2;
    	long s3;
    	for (int i = 0; i < split.length; i+=3) {
    		s1 = Long.valueOf(split[i]);
    		s2 = Long.valueOf(split[i + 1]);
    		s3 = Long.valueOf(split[i + 2]);
			// 开始时间不能大于结束时间
			if (s1 >= s2) {
				throw new Exception("限时抢购开始时间不能大于抢购时间");
			}
			if (s2 >= s3) {
				throw new Exception("限时抢购抢购时间不能大于结束时间");
			}
			config.getActiveTime().add(s1);
			config.getActiveTime().add(s2);
			config.getActiveTime().add(s3);
		}
    	// 解析开服时间
    	List<Integer> openServerDay = new ArrayList<>();
    	List<Integer> openServerTime = new ArrayList<>();
    	String[] split3 = bean.getOpenServerTime().split(";");
    	for (int i = 0; i < split3.length; i++) {
    		String str = split3[i];
			String[] split2 = str.split(",");
	    	if (split2.length != 3) {
	    		throw new Exception("限时抢购时间配置错误,错误的开服时间格式");
	    	}
    		paseOpenServerTime(split2, openServerDay, openServerTime);
		}
    	config.setOpenServerDay(openServerDay);
    	config.setOpenServerTime(openServerTime);
    	config.setWeight(bean.getWeight());
    	for (String str : bean.getImgUrl().split(",")) {
    		config.getImgUrls().add(str);
		}
    	config.setItems(parseItemConfig(bean.getItemStr()));
    	config.setOpen(bean.getOpen());
    	return config;
    }
    
    private void paseOpenServerTime(String[] str, List<Integer> openServerDay,List<Integer> openServerTime) throws Exception {
    	int[] dayArry = new int[] {0, 0, 0};
    	int crossDayTime = BeanTemplet.getGlobalBean(196).getInt_value() * 60 * 60;
    	int[] timeArry = new int[] {crossDayTime, crossDayTime, crossDayTime};
    	try {
    		for (int i = 0; i < str.length; i++) {
    			parseTimeStr(str[i], i, dayArry, timeArry);
    		}
		} catch (Exception e) {
			LOGGER.error(e);
			LOGGER.error("限时抢购时间配置错误,错误的开服时间格式");
    		throw new Exception("限时抢购时间配置错误,错误的开服时间格式");
		}
    	long l1;
    	long l2;
    	long l3;
    	l1 = dayArry[0] * 24 * 60 * 60 + timeArry[0];
    	l2 = dayArry[1] * 24 * 60 * 60 + timeArry[1];
    	l3 = dayArry[2] * 24 * 60 * 60 + timeArry[2];
    	if (l1 > l2 || l2 > l3 || l1 > l3) {
    		throw new Exception("限时抢购时间配置错误,错误的开服时间格式");
    	}
    	openServerDay.add(dayArry[0]);
    	openServerDay.add(dayArry[1]);
    	openServerDay.add(dayArry[2]);
    	openServerTime.add(timeArry[0]);
    	openServerTime.add(timeArry[1]);
    	openServerTime.add(timeArry[2]);
    }
    
    private void parseTimeStr(String str, int index, int[] dayArry, int[] timeArry) throws Exception {
    	int day = 0;
    	int time = 0;
    	String[] split = str.split("\\.");
    	day = Integer.valueOf(split[0]);
    	int temp1;
    	int temp2;
    	if (split.length == 3) {
    		temp1 = 0;
    		temp2 = 0;
    		try {
    			temp1 = Integer.valueOf(split[1]);
    			if (temp1 < 0 || temp1 > 23) {
    				throw new Exception();
    			}
			} catch (Exception e) {
				throw new Exception("限时抢购时间配置错误,错误的开服时间格式,小时数错误");
			}
    		try {
    			temp2 = Integer.valueOf(split[2]);
    			if (temp2 < 0 || temp2 > 59) {
    				throw new Exception();
    			}
			} catch (Exception e) {
				throw new Exception("限时抢购时间配置错误,错误的开服时间格式,分钟数错误");
			}
    		time = (temp1 * 60 * 60) + temp2;
    	}
    	if (split.length == 2) {
    		try {
    			temp1 = Integer.valueOf(split[1]);
    			if (temp1 < 0 || temp1 > 23) {
    				throw new Exception();
    			}
    		} catch (Exception e) {
    			throw new Exception("限时抢购时间配置错误,错误的开服时间格式,小时数错误");
    		}
    		time = temp1 * 60 * 60;
    	}
    	dayArry[index] = day;
    	if (split.length > 1) {
    		timeArry[index] = time;
    	}
    }
    
    /** ExtraDropItemConfig的json字符串 */
    private List<FlashSaleItem> parseItemConfig(String str) {
    	JSONArray jsonArray = JSONArray.parseArray(str);
    	List<FlashSaleItem> l = new ArrayList<>();
    	FlashSaleItem config;
    	JSONObject json;
    	for (int i = 0; i < jsonArray.size(); i++) {
    		json = jsonArray.getJSONObject(i);
    		config = new FlashSaleItem();
    		config.fromJson(json);
    		l.add(config);
		}
    	return l;
    }
}
