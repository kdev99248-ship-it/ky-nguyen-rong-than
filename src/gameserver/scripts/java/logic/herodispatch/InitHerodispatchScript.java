package logic.herodispatch;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import data.bean.t_dispatchBean;
import data.bean.t_dispatch_quantityBean;
import game.core.pub.script.IScript;
import game.core.pub.util.MathUtils;
import game.server.logic.guild.GuildService;
import game.server.logic.herodispatch.HerodispatchManager;
import game.server.logic.herodispatch.HerodispatchService;
import game.server.logic.herodispatch.bean.Herodispatch;
import game.server.logic.player.Player;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;

/**
 * 初始化玩家的精灵派遣任务
 * @author tzyx
 *
 */
public class InitHerodispatchScript implements IScript{

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void init() {
		
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs args = (ScriptArgs) arg;
		Player player = (Player) args.get(Key.PLAYER);
		initPlayerHerodispatch(player);
		sortTask(player);
		return null;
	}
	
	// 任务排序
	private void sortTask(Player player) {
		List<Herodispatch> dispatchList = player.getHerodispatchManager().getDispatchList();
		Herodispatch temp;
		for (int i = 0; i < dispatchList.size() - 1; i++) {// 外层循环控制排序趟数
			for (int j = 0; j < dispatchList.size() - 1 - i; j++) {// 内层循环控制每一趟排序多少次
				boolean change = false;
				// 状态相同按id顺序排列
				if (dispatchList.get(j).getStatus() == dispatchList.get(j + 1).getStatus()) {
					if (dispatchList.get(j).getDispatchId() > dispatchList.get(j + 1).getDispatchId()) {
						change = true;
					}
				} else {
					// 未领取默认排前面
					// 已派遣排最后
					if (dispatchList.get(j).getStatus() == 2) {
						change = true;
					} else if (dispatchList.get(j).getStatus() == 1) {
						// 未派遣大于已派遣
						if (dispatchList.get(j + 1).getStatus() == 3) {
							change = true;
						}
					}
				}
				if (change) {
					temp = dispatchList.get(j);
					dispatchList.set(j, dispatchList.get(j + 1));
					dispatchList.set(j + 1, temp);
				}
			}
		}
		for (int i = 0; i < dispatchList.size(); i++) {
			dispatchList.get(i).setIndex(i);
		}
	}

	public void initPlayerHerodispatch(Player player) {
		HerodispatchService service = HerodispatchService.getInstance();
		// 检查功能开放
		if (service.herodispatchFunctionOpen(player, false))
			return;
		List<t_dispatch_quantityBean> quantity = BeanTemplet.getAllHerodispatchQuantity();
		int index = -1;
		for (int i = 0; i < quantity.size(); i++) {
			t_dispatch_quantityBean bean = quantity.get(i);
			if (bean.getPlayer_level() <= player.getLevel()) {
				index = i;
			} else {
				break;
			}
		}
		// 等级不足,不能初始化任务
		if (index == -1) {
			return;
		}
		t_dispatch_quantityBean bean = quantity.get(index);
		initTask(player, bean);
	}
	
	private void initTask(Player player, t_dispatch_quantityBean bean) {
		HerodispatchManager manager = player.getHerodispatchManager();
		int guildLvl = GuildService.getInstance().getPlayerGuild(player.getPlayerId()).getLevel();
		int n1 = 0;
		int n2 = 0;
		int n3 = 0;
		List<Herodispatch> dispatchList = manager.getDispatchList();
		for (Herodispatch dis : dispatchList) {
			if (BeanTemplet.getHerodispatch(dis.getDispatchId()).getType() == 1) {
				n1++;
			} else if (BeanTemplet.getHerodispatch(dis.getDispatchId()).getType() == 2) {
				n2++;
			} else if (BeanTemplet.getHerodispatch(dis.getDispatchId()).getType() == 3) {
				n3++;
			}
		}
		List<t_dispatchBean> allHerodispatch = BeanTemplet.getAllHerodispatch();
		List<t_dispatchBean> d1 = new ArrayList<>();
		List<t_dispatchBean> d2 = new ArrayList<>();
		List<t_dispatchBean> d3 = new ArrayList<>();
		for (t_dispatchBean b : allHerodispatch) {
			String[] lvl = b.getLevel_unlock().split("\\_");
			if (player.getLevel() < Integer.valueOf(lvl[0]) || player.getLevel() > Integer.valueOf(lvl[1])) {
				continue;
			}
			if (b.getType() == 1) {
				d1.add(b);
			} else if (b.getType() == 2) {
				d2.add(b);
			} else if (b.getType() == 3) {
				d3.add(b);
			}
		}
		for (int i = 0; i < bean.getType1_quantity() - n1; i++) {
			manager.getDispatchList().add(copyConfig(randomTask(d1, manager, guildLvl)));
		}
		for (int i = 0; i < bean.getType2_quantity() - n2; i++) {
			manager.getDispatchList().add(copyConfig(randomTask(d2, manager, guildLvl)));
		}
		for (int i = 0; i < bean.getType3_quantity() - n3; i++) {
			manager.getDispatchList().add(copyConfig(randomTask(d3, manager, guildLvl)));
		}
	}
	
	private t_dispatchBean randomTask(List<t_dispatchBean> list, HerodispatchManager manager, int guildLvl) {
		List<t_dispatchBean> l = new ArrayList<>();
		a : for (t_dispatchBean bean : list) {
			for (Herodispatch dis : manager.getDispatchList()) {
				// 已经存在
				if (dis.getDispatchId() == bean.getId()) {
					continue a;
				}
			}
			l.add(bean);
		}
		int[] odds = new int[l.size()];
		for (int i = 0; i < l.size(); i++) {
			odds[i] = l.get(i).getProbability() + (l.get(i).getAdditional_probability() * guildLvl);
		}
		return l.get(MathUtils.weightCalculate(odds));
	}
	
	private Herodispatch copyConfig(t_dispatchBean bean) {
		Herodispatch dis = new Herodispatch();
		dis.setDispatchId(bean.getId());
		dis.setStatus(1);
		return dis;
	}
}
