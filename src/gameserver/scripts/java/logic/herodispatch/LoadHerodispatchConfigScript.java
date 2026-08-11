package logic.herodispatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import data.bean.t_dispatchBean;
import data.bean.t_globalBean;
import game.core.pub.script.IScript;
import game.server.logic.herodispatch.HerodispatchService;
import game.server.logic.herodispatch.bean.HerodispatchBoxConfig;
import game.server.logic.herodispatch.bean.HerodispatchConfig;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;

/**
 * 推送精灵派遣信息
 * @author tzyx
 *
 */
public class LoadHerodispatchConfigScript implements IScript{

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void init() {
		
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public Object call(String scriptName, Object arg) {
		HerodispatchService service = HerodispatchService.getInstance();
		List<HerodispatchBoxConfig> list = loadBoxConfig();
		Map<Integer, HerodispatchConfig> map = loadConfig();
		if (null != list && null != map && !list.isEmpty() && !map.isEmpty()) {
			service.setOpen(true);
			service.setBoxConfigList(list);
			service.setConfigMap(map);
		} else {
			service.setOpen(false);
		}
		return null;
	}

	private List<HerodispatchBoxConfig> loadBoxConfig() {
		// 宝箱配置
		t_globalBean globalBean = BeanTemplet.getGlobalBean(600);
		if (null == globalBean) {
			return null;
		}
		List<HerodispatchBoxConfig> guildBoxList = new ArrayList<>();
		String[] split = globalBean.getStr_value().split("\\|");
		for (int i = 0; i < split.length; i++) {
			HerodispatchBoxConfig con = new HerodispatchBoxConfig();
			String[] split2 = split[i].split("_");
			con.setNeedScore(Integer.valueOf(split2[0]));
			String[] split3 = split2[1].split(";");
			for (String str : split3) {
				String[] split4 = str.split(",");
				con.getRewardList().addAll(BeanFactory.createProps(Integer.valueOf(split4[0]), Integer.valueOf(split4[1])));
			}
			guildBoxList.add(con);
		}
		return guildBoxList;
	}
	
	private Map<Integer, HerodispatchConfig> loadConfig() {
		List<t_dispatchBean> list = BeanTemplet.getAllHerodispatch();
		if (null == list || list.isEmpty()) {
			return null;
		}
		Map<Integer, HerodispatchConfig> map = new HashMap<>();
		for (t_dispatchBean bean : list) {
			HerodispatchConfig config = new HerodispatchConfig();
			config.setDispatchId(bean.getId());
			config.setType(bean.getType());
			config.setTask_conditions1(bean.getTask_conditions1());
			// 任务消耗
			String[] split = bean.getTask_conditions2().split(";");
			for (String str : split) {
				String[] split2 = str.split(",");
				if (split2.length == 2)
					config.getTask_conditions2().addAll(BeanFactory.createProps(Integer.valueOf(split2[0]), Integer.valueOf(split2[1])));
			}
			// 消耗时间
			config.setTask_conditions3(bean.getTask_conditions3());
			// 推荐精灵
			String[] split1 = bean.getRecommender().split(";");
			for (String string : split1) {
				config.getRecommender().add(Integer.valueOf(string));
			}
			config.setActive_value(bean.getActive_value());
			String[] split3 = bean.getItems().split(";");
			for (String str : split3) {
				String[] split2 = str.split(",");
				config.getItems().addAll(BeanFactory.createProps(Integer.valueOf(split2[0]), Integer.valueOf(split2[1])));
			}
			String[] split4 = bean.getRecommend_items().split(";");
			for (String str : split4) {
				String[] split2 = str.split(",");
				config.getRecommend_items().addAll(BeanFactory.createProps(Integer.valueOf(split2[0]), Integer.valueOf(split2[1])));
			}
			config.setProbability(bean.getProbability());
			map.put(config.getDispatchId(), config);
		}
		return map;
	}
}
