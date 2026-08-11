package logic.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Message.C2SOperateActivityMsg.SlotMachinesBoxReq;
import Message.C2SOperateActivityMsg.SlotMachinesBoxReqID;
import Message.S2CBackpackMsg.PropInfo;
import Message.S2COperateActivityMsg.RewardBox;
import Message.S2COperateActivityMsg.SlotMachinesBoxRsp;
import Message.S2COperateActivityMsg.SlotMachinesBoxRspID;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.SlotMachines;
import game.server.logic.activity.bean.SlotMachinesBoxConfig;
import game.server.logic.activity.bean.SlotMachinesConfig;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 探险训练家领取宝箱
 * @author tzyx
 *
 */
public class SlotMachinesBoxScript implements IScript {
	private static Logger LOGGER = Logger.getLogger(SlotMachinesBoxScript.class);

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
		// 活动正在结算
		if (ActivityService.getInstance().isSmCheckEnd()) {
			LOGGER.error("活动未开启");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 209);
			return null;
		}
		SlotMachinesBoxReq req = (SlotMachinesBoxReq) script.get(Key.ARG1);
		slotMachinesBox(player, req);
		return null;
	}

	private void slotMachinesBox(Player player, SlotMachinesBoxReq req) {
		ActivityService service = ActivityService.getInstance();
		SlotMachinesConfig config = service.getSlotMachinesConfig();
		if (config == null || config.getOpen() == 0) {
			LOGGER.error("活动未开启");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 209);
			return;
		}
		SlotMachines slotMachines = service.getSlotMachinesData().get(player.getPlayerId());
		// 未参与活动或宝箱不是可领取状态
		int index = req.getIndex() - 1;
		if (null == slotMachines || slotMachines.getBoxes().get(index) != 1) {
			LOGGER.error("宝箱不能开启");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5007);
			return;
		}
		SlotMachinesBoxRsp.Builder builder = SlotMachinesBoxRsp.newBuilder();
		List<Item> rewardList = new ArrayList<>();
		SlotMachinesBoxConfig boxConfig = config.getBoxConfigList().get(index);
		for (int i = 0; i < boxConfig.getIds().length; i++) {
			PropInfo.Builder prop = PropInfo.newBuilder();
			prop.setId(boxConfig.getIds()[i]);
			prop.setNum(boxConfig.getNums()[i]);
			builder.addRewardList(prop);
			rewardList.addAll(BeanFactory.createProps(prop.getId(), prop.getNum()));
		}
		slotMachines.getBoxes().set(index, 2);
		player.getBackpackManager().addItems(rewardList, Reason.SLOT_MACHINES_BOX, "");
		for (int i = 0; i < config.getBoxConfigList().size(); i++) {
			RewardBox.Builder box = config.getBoxConfigList().get(i).genRewardBox();
			if (null != slotMachines) {
				box.setStatus(slotMachines.getBoxes().get(i));
			} else {
				box.setStatus(0);
			}
			builder.addTimesBoxs(box);
		}
		MessageUtils.send(player, player.getFactory().fetchSMessage(SlotMachinesBoxRspID.SlotMachinesBoxRspMsgID_VALUE,
				builder.build().toByteArray()));
		// 记录动作 活动id,索引
		LogService.getInstance().logPlayerAction(player,
				SlotMachinesBoxReqID.SlotMachinesBoxReqMsgID_VALUE, config.getId(), index);
	}
}
