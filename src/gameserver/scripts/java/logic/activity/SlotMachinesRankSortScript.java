package logic.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.SlotMachines;

/**
 * 探险训练家积分排序
 * @author tzyx
 *
 */
public class SlotMachinesRankSortScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		slotMachinesRankSort();
		return null;
	}

	private void slotMachinesRankSort() {
		ActivityService service = ActivityService.getInstance();
		Map<Long, SlotMachines> slotMachinesData = service.getSlotMachinesData();
		List<SlotMachines> l = new ArrayList<>();
		for (SlotMachines sm : slotMachinesData.values()) {
			l.add(sm);
		}
		for (int i = 0; i < l.size() - 1; i++) {// 外层循环控制排序趟数
			for (int j = 0; j < l.size() - 1 - i; j++) {// 内层循环控制每一趟排序多少次
				if (l.get(j).getScore() < l.get(j + 1).getScore()) {
					SlotMachines temp = l.get(j);
					l.set(j, l.get(j + 1));
					l.set(j + 1, temp);
				} else if (l.get(j).getScore() == l.get(j + 1).getScore()) {
					if (l.get(j).getLastRollTime() > l.get(j + 1).getLastRollTime()) {
						SlotMachines temp = l.get(j);
						l.set(j, l.get(j + 1));
						l.set(j + 1, temp);
					}
				}
			}
		}
		List<Long> rankList = new ArrayList<>();
		Map<Long, Integer> rankMap = new HashMap<>();
		for (int i = 0; i < l.size(); i++) {
			SlotMachines sm = l.get(i);
			rankMap.put(sm.getPlayerId(), i);
			rankList.add(sm.getPlayerId());
		}
		service.setSlotMachinesRankList(rankList);
		service.setSlotMachinesRankMap(rankMap);
	}
}
