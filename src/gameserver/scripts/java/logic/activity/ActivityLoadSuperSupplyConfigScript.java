package logic.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.hgame.gm_api.bean.SuperSupplyBean;

import game.core.pub.script.IScript;
import game.core.pub.util.DateUtils;
import game.server.db.game.dao.SuperSupplyConfigDao;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.SuperSupplyConfig;
import game.server.logic.item.bean.Item;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.TimeUtils;

/**
 * 加载超级补给配置
 * 
 * @author tzyx
 *
 */
public class ActivityLoadSuperSupplyConfigScript implements IScript {
	private static Logger LOGGER = Logger.getLogger(ActivityLoadSuperSupplyConfigScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ActivityService service = ActivityService.getInstance();
		List<SuperSupplyBean> all = SuperSupplyConfigDao.selectAll();
		Map<String, SuperSupplyConfig> map = new ConcurrentHashMap<>();
		List<SuperSupplyConfig> l = new ArrayList<>();
		SuperSupplyConfig config = null;
		Date startTime;
		Date endTime;
		try {
			for (SuperSupplyBean bean : all) {
				SuperSupplyConfig parseConfig = parseConfig(bean);
				map.put(bean.getId(), parseConfig);
				l.add(parseConfig);
				if (null == config && bean.getOpen() == 1) {
					startTime = new Date(parseConfig.getStartTime());
					endTime = new Date(parseConfig.getEndTime());
					if (DateUtils.afterNow(startTime) && DateUtils.beforeNow(endTime)) {
						config = parseConfig;
					}
				}
			}
			// 检查活动的时间是否有重叠
			for (int i = 0; i < l.size() - 1; i++) {// 外层循环控制排序趟数
				for (int j = 0; j < l.size() - 1 - i; j++) {// 内层循环控制每一趟排序多少次
					if (l.get(j).getStartTime() > l.get(j + 1).getStartTime()) {
						SuperSupplyConfig temp = l.get(j);
						l.set(j, l.get(j + 1));
						l.set(j + 1, temp);
					}
				}
			}
			long end = 0;
			for (SuperSupplyConfig temp : l) {
				if (temp.getOpen() == 0) {
					continue;
				}
				if (end == 0) {
					end = temp.getEndTime();
					continue;
				}
				// 时间交叉了
				if (end > temp.getStartTime()) {
					throw new Exception("超级礼包时间配置有重叠");
				}
				end = temp.getEndTime();
			}
			synchronized (service.getAllSuperSupplyConfig()) {
				service.setAllSuperSupplyConfig(map);
			}
			// 当前生效的配置发生改变,推送所有在线玩家
			boolean flag = false;
			if (service.getSuperSupplyConfig() != null && config != null
					&& !config.equals(service.getSuperSupplyConfig())) {
				flag = true;
				service.getSuperSupplyData().remove(config.getId());
			}
			service.setSuperSupplyConfig(config);
			if (flag) {
				service.superSupplyNotifyAll();
			}
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("加载超级礼包配置失败", e);
			return false;
		}
		return true;
	}

	private SuperSupplyConfig parseConfig(SuperSupplyBean bean) throws Exception {
		SuperSupplyConfig config = new SuperSupplyConfig();
		config.setId(bean.getId());
		config.setTimeType(bean.getTimeType());
		config.setProductId(bean.getProductId());
		config.setStartTime(TimeUtils.stringToDate(bean.getStartDate()).getTime());
		config.setEndTime(TimeUtils.stringToDate(bean.getEndDate()).getTime());
		String[] split = bean.getItems().split(";");
		List<Item> l = new ArrayList<>();
		for (String string : split) {
			String[] spl = string.split("_");
			l.addAll(BeanFactory.createProps(Integer.valueOf(spl[0]), Integer.valueOf(spl[1])));
		}
		BeanFactory.combineItemList(l);
		config.setItems(l);
		config.setOpen(bean.getOpen());
		return config;
	}
}
