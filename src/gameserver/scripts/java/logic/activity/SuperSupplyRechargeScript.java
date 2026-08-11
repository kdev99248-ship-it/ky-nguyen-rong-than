package logic.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.SuperSupplyConfig;
import game.server.logic.backpack.BackpackService;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;

/**
 * 超级补给充值
 * @author tzyx
 *
 */
public class SuperSupplyRechargeScript implements IScript {

	private Logger logger = LoggerFactory.getLogger(SuperSupplyRechargeScript.class);
	
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
		int productId = (int) script.get(Key.ARG1);
		return superSupplyRecharge(player, productId);
	}

	private boolean superSupplyRecharge(Player player, int productId) {
		ActivityService service = ActivityService.getInstance();
		SuperSupplyConfig config = service.getSuperSupplyConfig();
		Map<String, List<Long>> data = service.getSuperSupplyData();
		List<Item> props = new ArrayList<>(config.getItems());
		synchronized (data) {
			if (data.containsKey(config.getId())) {
				List<Long> list = data.get(config.getId());
				if (list.contains(player.getPlayerId())) {
					logger.error("玩家重复购买了超级补给:" + productId + ",playerId:" + player.getPlayerId());
				} else {
					data.get(config.getId()).add(player.getPlayerId());
				}
			} else {
				List<Long> l = new ArrayList<>();
				l.add(player.getPlayerId());
				data.put(config.getId(), l);
			}
		}
		player.getBackpackManager().addItems(props, true, false, Reason.SUPER_SUPPLY_BUY, productId + "");
		BackpackService.getInstance().getRewardNotify(player, props);
		service.superSupplyNotify(player);
		return true;
	}
}
