package logic.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Message.S2CBackpackMsg.PropInfo;
import Message.S2COperateActivityMsg.RewardBox;
import Message.S2COperateActivityMsg.SlotMachinesMsg;
import Message.S2COperateActivityMsg.SlotMachinesNotify;
import Message.S2COperateActivityMsg.SlotMachinesNotifyID;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.SlotMachines;
import game.server.logic.activity.bean.SlotMachinesConfig;
import game.server.logic.activity.bean.SlotMachinesGachaConfig;
import game.server.logic.constant.ItemType;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 推送探险训练家信息
 * 
 * @author tzyx
 *
 */
public class SlotMachinesNotifyScript implements IScript {
	private static Logger LOGGER = Logger.getLogger(SlotMachinesNotifyScript.class);

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
		slotMachinesNotify(player);
		return null;
	}

	private void slotMachinesNotify(Player player) {
		ActivityService service = ActivityService.getInstance();
		SlotMachinesConfig config = service.getSlotMachinesConfig();
		if (config == null || config.getOpen() == 0) {
			LOGGER.error("活动未开启");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 209);
			return;
		}
		SlotMachinesMsg.Builder builder = SlotMachinesMsg.newBuilder();
		// 设置截止倒计时
		builder.setEndTime(config.getEndTime() - System.currentTimeMillis());
		// 设置抽奖消耗
		PropInfo.Builder p = PropInfo.newBuilder();
		p.setId(ItemType.DIAMOND.value());
		p.setNum(config.getConsume());
		builder.setConsumes(p);
		p = PropInfo.newBuilder();
		p.setId(ItemType.DIAMOND.value());
		p.setNum(config.getConsume10());
		builder.setConsumes10(p);
		// 设置抽奖奖励积分
		builder.setAddScore(config.getScore());
		builder.setAddScore10(config.getScore10());
		// 设置玩家数据
		SlotMachines slotMachines = service.getSlotMachinesData().get(player.getPlayerId());
		if (null != slotMachines) {
			builder.setScore(slotMachines.getScore());
			builder.setFreeTimes(slotMachines.getFreeTimes());
			builder.setUsedTimes(slotMachines.getUsedTimes());
			builder.setRanking(service.getSlotMachinesRankMap().get(player.getPlayerId()) + 1);
			if (config.getBoxConfigList().size() != slotMachines.getBoxes().size()) {
				List<Integer> boxes = new ArrayList<>();
				for (int i = 0; i < config.getBoxConfigList().size(); i++) {
					if (i + 1 <= slotMachines.getBoxes().size()) {
						boxes.add(slotMachines.getBoxes().get(i));
					} else {
						boxes.add(0);
					}
				}
				LOGGER.error("活动宝箱修复，新宝箱格子大小： " + boxes.size());
				slotMachines.setBoxes(boxes);
			}
		} else {
			builder.setScore(0);
			builder.setFreeTimes(config.getFreeTime());
			builder.setUsedTimes(0);
			builder.setRanking(0);
		}
		builder.setJackpot(service.getJackpot());

		for (SlotMachinesGachaConfig gacha : config.getGachaList()) {
			builder.addGridList(gacha.genLuckGridMsg());
		}
		for (int i = 0; i < config.getBoxConfigList().size(); i++) {
			RewardBox.Builder box = config.getBoxConfigList().get(i).genRewardBox();
			if (null != slotMachines) {
				box.setStatus(slotMachines.getBoxes().get(i));
			} else {
				box.setStatus(0);
			}
			builder.addTimesBoxs(box);
		}
		for (int i = 0; i < config.getRankConfigList().size(); i++) {
			builder.addRankShowList(config.getRankConfigList().get(i).genRank());
		}
		builder.setBanner(config.getBanner());
		SlotMachinesNotify.Builder b = SlotMachinesNotify.newBuilder();
		b.setSlotMachines(builder);
		MessageUtils.send(player, player.getFactory().fetchSMessage(SlotMachinesNotifyID.SlotMachinesNotifyMsgID_VALUE,
				b.build().toByteArray()));
	}
}
