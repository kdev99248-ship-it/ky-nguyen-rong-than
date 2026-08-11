package logic.snatchTerritory;

import java.util.ArrayList;
import java.util.List;

import Message.C2SSnatchTerritoryMsg.ReqGetPublicProgressAwardID;
import Message.S2CPlayerMsg.PromptType;
import data.bean.t_worldTask_boxBean;
import game.core.pub.script.IScript;
import game.server.db.game.bean.GameGlobalBean;
import game.server.logic.backpack.BackpackService;
import game.server.logic.constant.GlobalID;
import game.server.logic.constant.Reason;
import game.server.logic.global.GameGlobalService;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/** 领取进度奖励 */
public class RspGetPublicProgressAwardScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		Player player = (Player) script.get(ScriptArgs.Key.PLAYER);
		int progress = (int) script.get(ScriptArgs.Key.ARG1);
		String award = null;
		// t_globalBean global = BeanTemplet.getGlobalBean(303);
		// String[] awards = global.getStr_value().split(";");
		// for (int i = 0; i < awards.length; i++) {
		// if (awards[i].split("_")[0].equals(progress + "")) {
		// award = awards[i].split("_")[1];
		// break;
		// }
		// }
		if (player.getTaskManager().getpAwardFinished().contains(progress)) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 105);
			return null;
		}
		t_worldTask_boxBean bean = BeanTemplet.getGymTaskProgressBean(progress);
		if (null != bean) {
			GameGlobalBean globalBean = GameGlobalService.getInstance()
					.findById(GlobalID.GYM_PUB_TASK_PROGRESS.getValue());
			if (null != globalBean && bean.getExperience() <= globalBean.getIntVal())
				award = bean.getReward();
		}
		if (null != award) {
			String[] str = award.split(";");
			List<Item> items = new ArrayList<Item>();
			for (int i = 0; i < str.length; i++) {
				int id = Integer.parseInt(str[i].split(",")[0]);
				int num = Integer.parseInt(str[i].split(",")[1]);
				items.addAll(BeanFactory.createProps(id, num));
			}
			player.getBackpackManager().addItems(items, true, false, Reason.GYM_PUBTASK, "道馆公共任务进度奖励");
			player.getTaskManager().addFinishedAwardProgress(progress);

			// 发送奖励
			BackpackService.getInstance().getRewardNotify(player, items);

			player.getTaskManager().notifyClientPublicTaskUpdate(1);// 道馆任务

			// 玩家操作日志
			LogService.getInstance().logPlayerAction(player,
					ReqGetPublicProgressAwardID.ReqGetPublicProgressAwardMsgID_VALUE, progress);
		}else
		{
			MessageUtils.sendPrompt(player, PromptType.ERROR, 105);
		}
		return null;
	}

}
