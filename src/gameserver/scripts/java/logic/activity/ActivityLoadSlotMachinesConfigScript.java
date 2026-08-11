package logic.activity;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.hgame.gm_api.bean.SlotMachinesBean;

import game.core.pub.script.IScript;
import game.core.pub.util.DateUtils;
import game.server.db.game.dao.SlotMachinesConfigDao;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.SlotMachinesBoxConfig;
import game.server.logic.activity.bean.SlotMachinesConfig;
import game.server.logic.activity.bean.SlotMachinesGachaConfig;
import game.server.logic.activity.bean.SlotMachinesRankConfig;
import game.server.logic.util.TimeUtils;

/**
 * 
 * @ClassName: ActivityLoadExtraDropConfigScript 
 * @Description: 加载额外掉落配置
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class ActivityLoadSlotMachinesConfigScript implements IScript {
     private static Logger LOGGER = Logger.getLogger(ActivityLoadSlotMachinesConfigScript.class);

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Object call(String scriptName, Object arg) {
    	ActivityService service = ActivityService.getInstance();
		List<SlotMachinesBean> all = SlotMachinesConfigDao.selectAll();
		Map<String, SlotMachinesConfig> map = new ConcurrentHashMap<>();
		SlotMachinesConfig config = null;
		Date startTime;
		Date endTime;
		try {
			for (SlotMachinesBean bean : all) {
				SlotMachinesConfig parseConfig = parseConfig(bean);
				map.put(bean.getId(), parseConfig);
				startTime = TimeUtils.stringToDate(bean.getStartTime());
				endTime = TimeUtils.stringToDate(bean.getEndTime());
				if (null == config && bean.getOpen() == 1 && DateUtils.afterNow(startTime) && DateUtils.beforeNow(endTime)) {
					config = parseConfig;
				}
			}
			synchronized (service.getAllSlotMachinesConfig()) {
				service.setAllSlotMachinesConfig(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("加载探险训练家配置失败",e);
		}
		// 判断是否老数据
		if (config != null) {
			SlotMachinesConfig old = service.getSlotMachinesConfig();
			if (null != old) {
				if (!old.getId().equals(config.getId())) {
					// 配置变了,进行结算
					service.slotMachinesEnd();
				}
			}
		}
		if (null != service.getSlotMachinesConfig() && config == null) {
			// 配置变了,进行结算
			service.slotMachinesEnd();
		}
		service.setSlotMachinesConfig(config);
		return null;
	}

    private SlotMachinesConfig parseConfig(SlotMachinesBean bean) throws Exception {
    	SlotMachinesConfig config = new SlotMachinesConfig();
    	config.setId(bean.getId());
    	config.setStartTime(TimeUtils.stringToDate(bean.getStartTime()).getTime());
    	config.setEndTime(TimeUtils.stringToDate(bean.getEndTime()).getTime());
    	config.setScore(bean.getScore());
    	config.setScore10(bean.getScore10());
    	config.setConsume(bean.getConsume());
    	config.setConsume10(bean.getConsume10());
    	config.setBanner(bean.getBanner());
    	config.setFreeTime(bean.getFreeTime());
    	config.setOpen(bean.getOpen());
		// 加载奖池
		config.setGachaList(parseGachaConfig(bean.getGacha()));
		// 加载宝箱
		config.setBoxConfigList(parseBoxConfig(bean.getBoxConfigs()));
		// 加载排行榜
		config.setRankConfigList(parseRankConfig(bean.getRankConfigs()));
    	return config;
    }
    
    /**
     * 格子索引排序,显示类型（格子区分、特效区分）,奖励类型,道具,数量,1概率,10概率,消息模板id,跑马灯模板id|格子索引排序,显示类型（格子区分、特效区分）,奖励类型,道具,数量,1概率,10概率,消息模板id,跑马灯模板id
     */
    private List<SlotMachinesGachaConfig> parseGachaConfig(String str) {
    	String[] split = str.split("\\|");
    	List<SlotMachinesGachaConfig> l = new ArrayList<>();
    	for (int i = 0; i < split.length; i++) {
    		SlotMachinesGachaConfig config = new SlotMachinesGachaConfig();
    		String[] gachaStr = split[i].split(",");
			config.setIndex(Integer.valueOf(gachaStr[0]));
			config.setShowType(Integer.valueOf(gachaStr[1]));
			config.setType(Integer.valueOf(gachaStr[2]));
			config.setItemId(Integer.valueOf(gachaStr[3]));
			config.setNum(Integer.valueOf(gachaStr[4]));
			config.setOdds(Integer.valueOf(gachaStr[5]));
			config.setOdds10(Integer.valueOf(gachaStr[6]));
			config.setInfoId(Integer.valueOf(gachaStr[7]));
			config.setBroadcastId(Integer.valueOf(gachaStr[8]));
			l.add(config);
		}
    	return l;
    }
    
    /** 
     * 道具,数量,累计次数;道具,数量,累计次数
     */
    private List<SlotMachinesBoxConfig> parseBoxConfig(String str) {
    	String[] split = str.split("\\;");
    	List<SlotMachinesBoxConfig> l = new ArrayList<>();
    	for (int i = 0; i < split.length; i++) {
    		SlotMachinesBoxConfig config = new SlotMachinesBoxConfig();
    		String[] boxStr = split[i].split(",");
			config.setIndex(i);
			int itemLength = (boxStr.length - 1) / 2;
			int[] itemIds = new int[itemLength];
			int[] itemNums = new int[itemLength];
			config.setIds(itemIds);
			config.setNums(itemNums);
			for (int j = 0; j < boxStr.length; j++) {
				int temp = Integer.valueOf(boxStr[j]).intValue();
				if (j == boxStr.length - 1) {
					config.setNeedTimes(temp);
					break;
				}
				if (j % 2 == 0) {
					itemIds[j / 2] = temp;
				} else {
					itemNums[(j - 1) / 2] = temp;
				}
			}
			l.add(config);
		}
    	return l;
    }
    
    /**
     * SlotMachinesRankConfig的json
     * 排名,道具id,数量,道具id,数量|排名,道具id,数量,道具id,数量
     */
    private List<SlotMachinesRankConfig> parseRankConfig(String str) {
    	String[] split = str.split("\\|");
    	List<SlotMachinesRankConfig> l = new ArrayList<>();
    	for (int i = 0; i < split.length; i++) {
    		SlotMachinesRankConfig config = new SlotMachinesRankConfig();
    		String[] rankStr = split[i].split(",");
			config.setIndex(i);
			config.setRank(rankStr[0]);
			String[] split2 = config.getRank().split("-");
			if (split2.length == 1) {
				config.setStartRank(Integer.valueOf(config.getRank()));
				config.setEndRank(Integer.valueOf(config.getRank()));
			} else if (split2.length == 2) {
				config.setStartRank(Integer.valueOf(split2[0]));
				config.setEndRank(Integer.valueOf(split2[1]));
			}
			int itemLength = (rankStr.length - 1) / 2;
			int[] itemIds = new int[itemLength];
			int[] itemNums = new int[itemLength];
			config.setIds(itemIds);
			config.setNums(itemNums);
			for (int j = 1; j < rankStr.length; j++) {
				int temp = Integer.valueOf(rankStr[j]).intValue();
				if ((j - 1) % 2 == 0) {
					itemIds[(j - 1) / 2] = temp;
				} else {
					itemNums[(j - 2) / 2] = temp;
				}
			}
			l.add(config);
		}
    	return l;
    }
}
