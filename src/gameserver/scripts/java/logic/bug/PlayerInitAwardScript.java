package logic.bug;

import java.util.Calendar;
import java.util.List;

import data.bean.t_globalBean;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.player.RoleViewService;
import game.server.logic.util.BeanFactory;
import game.util.BeanTemplet;

public class PlayerInitAwardScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		Calendar cal = Calendar.getInstance();
		cal.set(2019, 2, 5, 10, 0, 0);

		List<Long> ids = RoleViewService.getAllPlayerId();
		for (Long id : ids) {
			Player player = PlayerManager.getOffLinePlayerByPlayerId(id);
			if (null == player)
				continue;
			if (player.getCreateTime() > cal.getTimeInMillis()) {
				t_globalBean bean = BeanTemplet.getGlobalBean(314);
				if (null != bean) {
					String[] costs = bean.getStr_value().split(";");
					for (int i = 0; i < costs.length; i++) {
						int itemid = Integer.parseInt(costs[i].split(",")[0]);
						int num = Integer.parseInt(costs[i].split(",")[1]);
						List<Item> items = BeanFactory.createProps(itemid, num);
						player.getBackpackManager().addItems(items, false, false, Reason.BT_INIT_AWARD, "BT版初始奖励");
					}
				}
			}
		}
		return null;
	}

}
