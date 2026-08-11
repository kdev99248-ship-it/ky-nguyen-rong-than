package logic.rune;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import Message.C2SRuneMsg.RuneSingleDrawReq;
import Message.S2CPlayerMsg.PromptType;
import Message.S2CRuneMsg.RuneSingleDrawRsp;
import Message.S2CRuneMsg.RuneSingleDrawRspID;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.rune.RuneManager;
import game.server.logic.rune.RuneService;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 符文单抽
 * 
 * @author liuwei
 * @date 2018年9月5日
 */
public class ReqRuneSingleDrawScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		RuneSingleDrawReq req = (RuneSingleDrawReq) script.get(ScriptArgs.Key.ARG1);
		Player player = (Player) script.get(ScriptArgs.Key.PLAYER);
		singleDraw(req, player);
		return null;
	}

	private void singleDraw(RuneSingleDrawReq req, Player player) {
		// 检查系统开放
		if (RuneService.getInstance().runeFunctionOpen(player)) {
			return;
		}
		RuneManager runeManager = player.getRuneManager();
		// 消耗道具
		List<Item> needItems = new ArrayList<>();
		if (req.getType() == 1) {// 金币单抽
			// 消耗
			// 免费次数判断
			if (runeManager.getDayGoldFreeNum() < BeanTemplet.getGlobalBean(306).getInt_value()
					&& runeManager.getLastGoldFreeTime()
							+ BeanTemplet.getGlobalBean(299).getInt_value() * 60 * 1000L <= Instant.now()
									.toEpochMilli()) {
				runeManager.setLastGoldFreeTime(Instant.now().toEpochMilli());
				runeManager.setDayGoldFreeNum(runeManager.getDayGoldFreeNum() + 1);
			} else {
				String[] consumStr = BeanTemplet.getGlobalBean(301).getStr_value().split(";")[0].split(",");
				needItems.addAll(
						BeanFactory.createProps(Integer.parseInt(consumStr[0]), Integer.parseInt(consumStr[1])));
			}
		} else {// 钻石单抽
			// 免费次数判断
			if (runeManager.getDayFreeNum() < BeanTemplet.getGlobalBean(298).getInt_value()
					&& runeManager.getLastFreeTime()
							+ BeanTemplet.getGlobalBean(299).getInt_value() * 60 * 1000L <= Instant.now()
									.toEpochMilli()) {
				runeManager.setLastFreeTime(Instant.now().toEpochMilli());
				runeManager.setDayFreeNum(runeManager.getDayFreeNum() + 1);
			} else {
				String[] consumStr = BeanTemplet.getGlobalBean(302).getStr_value().split(";")[0].split(",");
				needItems.addAll(
						BeanFactory.createProps(Integer.parseInt(consumStr[0]), Integer.parseInt(consumStr[1])));
			}
		}
		if (needItems.size() > 0) {// 判断消耗
			if (player.getBackpackManager().isItemNumEnough(needItems)) {
				// 消耗道具
				player.getBackpackManager().removeItems(needItems, true, Reason.RUNE_DRAWCARD, "");
			} else {
				MessageUtils.sendPrompt(player, PromptType.WARNING, 5);
				return;
			}
		}
		int itemId = RuneService.getInstance().singleDrawCard(req.getType(), runeManager);
		List<Item> createProps = BeanFactory.createProps(itemId, 1);
		// 添加到背包并推送
		player.getBackpackManager().addItems(createProps, Reason.RUNE_SINGLE_DRAW, "");
		// 更新符文品质数量任务
		player.getRuneManager().calcQualityNumAndUpdateTask();
		// 推送
		RuneSingleDrawRsp.Builder builder = RuneSingleDrawRsp.newBuilder();
		builder.setItem(createProps.get(0).genBuilder());
		builder.setRuneShop(runeManager.genRuneShopBuilder());
		MessageUtils.send(player, player.getFactory().fetchSMessage(RuneSingleDrawRspID.RuneSingleDrawRspMsgID_VALUE,
				builder.build().toByteArray()));
		// 日志记录
		LogService.getInstance().logPlayerAction(player, RuneSingleDrawRspID.RuneSingleDrawRspMsgID_VALUE,
				itemId);
	}

}
