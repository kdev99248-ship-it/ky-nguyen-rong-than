package logic.activity;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import data.bean.t_timelimitedpackBean;
import game.core.pub.script.IScript;
import game.server.http.apiImpl.CheckGMParameter;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.LimittimeGiftConfig;
import game.server.logic.item.bean.Item;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;

/**
 * 加载限时礼包配置
 * @author tzyx 
 */
public class ActivityLoadLimittimeGiftConfigScript implements IScript {
     private static Logger LOGGER = Logger.getLogger(ActivityLoadLimittimeGiftConfigScript.class);

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Object call(String scriptName, Object arg) {
    	boolean replace = (boolean) arg;
    	Map<Integer, LimittimeGiftConfig> m = new ConcurrentHashMap<>();
    	List<t_timelimitedpackBean> allLimittimeGift = BeanTemplet.getAllLimittimeGift();
    	LimittimeGiftConfig config;
		try {
			for (t_timelimitedpackBean bean : allLimittimeGift) {
				config = parseConfig(bean);
				m.put(config.getId(), config);
			}
			synchronized (ActivityService.getInstance().getAllLimittimeExchange()) {
				if (replace) {
					ActivityService.getInstance().setAllLimittimeGift(m);
				}
			}
		} catch (Exception e) {
			LOGGER.error(e);
			return e.getMessage();
		}
		return null;
	}

    private LimittimeGiftConfig parseConfig(t_timelimitedpackBean bean) throws Exception {
    	LimittimeGiftConfig config = new LimittimeGiftConfig();
    	config.setId(bean.getID());
    	config.setName(bean.getName());
    	// 解析触发条件
    	String[] spl = bean.getCondition().split(";");
    	if (spl.length < 1)
			throw new Exception("限时礼包配置错误:错误的触发条件 ," + bean.getID() + ":" + bean.getName() + "," + bean.getCondition());
    	String[] split;
    	for (String str : spl) {
    		split = CheckGMParameter.getInstance().check_num_num(str,",");
    		if (null == split)
    			throw new Exception("限时礼包配置错误:错误的触发条件 ," + bean.getID() + ":" + bean.getName() + "," + bean.getCondition());
    		if (config.getConditions().containsKey(Integer.valueOf(split[0])))
    			throw new Exception("限时礼包配置错误:重复的触发条件 ," + bean.getID() + ":" + bean.getName() + "," + bean.getCondition());
			config.getConditions().put(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
		}
    	// 解析奖励
    	spl = bean.getReward().split(";");
    	if (spl.length < 1)
			throw new Exception("限时礼包配置错误:奖励不能为空 ," + bean.getID() + ":" + bean.getName() + "," + bean.getReward());
    	Item item;
    	for (String str : spl) {
    		split = CheckGMParameter.getInstance().check_itemId_num(str, ",");
    		if (null == split)
    			throw new Exception("限时礼包配置错误:错误的奖励配置 ," + bean.getID() + ":" + bean.getName() + "," + bean.getReward());
			item = BeanFactory.createProps(Integer.valueOf(split[0]), 1).get(0);
			item.setNum(Integer.valueOf(split[1]));
			config.getItems().add(item);
    	}
    	return config;
    }
}
