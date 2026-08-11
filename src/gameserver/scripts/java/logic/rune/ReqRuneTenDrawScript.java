package logic.rune;

import java.util.ArrayList;
import java.util.List;

import Message.C2SRuneMsg.RuneTenDrawReq;
import Message.S2CPlayerMsg.PromptType;
import Message.S2CRuneMsg.RuneTenDrawRsp;
import Message.S2CRuneMsg.RuneTenDrawRspID;
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
 * 符文十连抽
 * 
 * @author liuwei
 * @date 2018年9月5日
 */
public class ReqRuneTenDrawScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		RuneTenDrawReq req = (RuneTenDrawReq) script.get(ScriptArgs.Key.ARG1);
		Player player = (Player) script.get(ScriptArgs.Key.PLAYER);
		tenDraw(req, player);
		return null;
	}

	private void tenDraw(RuneTenDrawReq req, Player player) {
		// 检查系统开放
		if (RuneService.getInstance().runeFunctionOpen(player)) {
			return;
		}
		RuneManager runeManager = player.getRuneManager();
		// 消耗道具
		List<Item> needItems = new ArrayList<>();
		if (req.getType() == 1) {// 金币单抽
			// 消耗
			String[] consumStr = BeanTemplet.getGlobalBean(301).getStr_value().split(";")[1].split(",");
			needItems.addAll(BeanFactory.createProps(Integer.parseInt(consumStr[0]), Integer.parseInt(consumStr[1])));
		} else {// 钻石单抽
			String[] consumStr = BeanTemplet.getGlobalBean(302).getStr_value().split(";")[1].split(",");
			needItems.addAll(BeanFactory.createProps(Integer.parseInt(consumStr[0]), Integer.parseInt(consumStr[1])));
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

		RuneTenDrawRsp.Builder builder = RuneTenDrawRsp.newBuilder();
		StringBuilder sBuilder = new StringBuilder();
		List<Item> items = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			int runeModelId = RuneService.getInstance().singleDrawCard(req.getType(), runeManager);
			// 添加符文
			List<Item> createProps = BeanFactory.createProps(runeModelId, 1);
			if (null != createProps) {
				items.addAll(createProps);
			}
		}
		// 添加到背包并推送
		player.getBackpackManager().addItems(items, Reason.RUNE_TEN_DRAW, "");
		// 更新符文品质数量任务
		player.getRuneManager().calcQualityNumAndUpdateTask();
		for (int i = 0; i < items.size(); i++) {
			Item item = items.get(i);
			builder.addItems(item.genBuilder());
			sBuilder.append(",").append(item.getId()).append("_").append(item.getNum());
		}
		
		builder.setRuneShop(runeManager.genRuneShopBuilder());
		// 推送
		MessageUtils.send(player, player.getFactory().fetchSMessage(RuneTenDrawRspID.RuneTenDrawRspMsgID_VALUE,
				builder.build().toByteArray()));
		// 日志记录
		LogService.getInstance().logPlayerAction(player, RuneTenDrawRspID.RuneTenDrawRspMsgID_VALUE,
				sBuilder.substring(1));
	}
}
