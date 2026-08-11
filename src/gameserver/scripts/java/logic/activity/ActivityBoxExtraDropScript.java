package logic.activity;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import Message.S2CActivityMsg.ExtraDropNotifyID;
import data.bean.t_itemBean;
import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.ExtraDropConfig;
import game.server.logic.activity.bean.ExtraDropItemConfig;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;

/**
 * 
 * @ClassName: ActivityBoxExtraDropScript 
 * @Description: 额外掉落
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class ActivityBoxExtraDropScript implements IScript {
    private static Logger LOGGER = Logger.getLogger(ActivityBoxExtraDropScript.class);

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		Player player = (Player) script.get(Key.PLAYER);
		t_itemBean itemBean = (t_itemBean) script.get(Key.ARG1);
		int times = (int) script.get(Key.ARG2);
		String str = itemBean.getText_param().trim();
		List<Item> l = new ArrayList<>();
		if (!StringUtils.isEmpty(str) && !"0".equals(str)) {
			for (int i = 0; i < times; i++) {
				l.addAll(tryBoxExtraDrop(player, itemBean));
			}
		}
		return l;
	}
	
	/** 尝试触发宝箱额外掉落 */
	private List<Item> tryBoxExtraDrop(Player player, t_itemBean itemBean) {
		ActivityService service = ActivityService.getInstance();
		List<Item> l = new ArrayList<>();
		Random r = new Random();
		List<String> ids = new ArrayList<>();
		for (String str : itemBean.getText_param().split(",")) {
			ids.add(str);
		}
		StringBuilder sb;
		List<String> strList = new ArrayList<>();
		for (ExtraDropConfig config : service.getAllExtraDropConfig().values()) {
			for (ExtraDropItemConfig item : config.getItems()) {
				if (ids.contains(item.getId()) && item.getGlobalTimes() - item.getUsedTimes() > 0) {
					int odd = r.nextInt(10000);
					if (odd < item.getOdds()) {
						for (Item it : item.getExtraItem()) {
							l.addAll(BeanFactory.createProps(it.getId(), it.getNum()));
						}
						sb = new StringBuilder();
						sb.append(item.getUsedTimes()).append("/");
						item.setUsedTimes(item.getUsedTimes() + 1);
						sb.append(item.getUsedTimes()).append("/").append(item.getGlobalTimes()).append("/")
						.append(odd).append("/").append(item.getOdds());
						strList.add(sb.toString());
					}
				}
			}
		}
		if (l.size() > 0) {
			sb = new StringBuilder();
			String s1;
			for (String string : strList) {
				sb.append(string).append(";");
			}
			s1 = sb.substring(0, sb.length() - 1);
			sb = new StringBuilder();
			for (Item item : l) {
				sb.append(item.getId()).append("_").append(item.getNum()).append(";");
			}
			// 记录动作  触发宝箱额外掉落  次数/之后次数/最大次数/roll点数/需要的点数  物品id 获取的道具
			LogService.getInstance().logPlayerAction(player, ExtraDropNotifyID.ExtraDropNotifyMsgID_VALUE,
					2, s1, itemBean.getId(), sb.substring(0, sb.length() - 1));
		}
		return l;
	}
}
