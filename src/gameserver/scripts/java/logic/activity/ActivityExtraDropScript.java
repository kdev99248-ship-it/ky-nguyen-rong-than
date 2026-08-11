package logic.activity;


import java.util.ArrayList;
import java.util.List;

import Message.S2CActivityMsg.ExtraDropNotifyID;
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
 * @ClassName: ActivityExtraDropScript 
 * @Description: 额外掉落
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class ActivityExtraDropScript implements IScript {
//    private static Logger LOGGER = Logger.getLogger(ActivityExtraDropScript.class);

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
		int sectionType = (int) script.get(Key.ARG1);
		return tryExtraDrop(player, sectionType);
	}
	
	/** 尝试触发额外掉落 */
	private List<Item> tryExtraDrop(Player player, int sectionType) {
		ActivityService service = ActivityService.getInstance();
		List<Item> l = new ArrayList<>();
		List<String> conf = new ArrayList<>();
		for (ExtraDropConfig config : service.getAllExtraDropConfig().values()) {
			if (config.getOpen() == 1 && config.getActive() == 1) {
				for (ExtraDropItemConfig item : config.getItems()) {
					if (item.getSectionType() == sectionType) {
						if (!conf.contains(config.getId())) {
							conf.add(config.getId());
						}
						l.addAll(BeanFactory.createProps(item.getItemId(), item.getNum()));
					}
				}
			}
		}
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		if (l.size() > 0) {
			for (String string : conf) {
				sb1.append(string).append(",");
			}
			for (Item item : l) {
				sb2.append(item.getId()).append("_").append(item.getNum()).append(",");
			}
			// 记录动作  触发额外掉落  触发的额外掉落id 关卡类型  掉落的道具
			LogService.getInstance().logPlayerAction(player, ExtraDropNotifyID.ExtraDropNotifyMsgID_VALUE,
					1, sb1.substring(0, sb1.length() - 1), sectionType, sb2.substring(0, sb2.length() - 1));
		}
		return l;
	}
}
