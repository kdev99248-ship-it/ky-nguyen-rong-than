package logic.research;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Message.C2SResearchMsg.RebirthRuneReq;
import Message.S2CBackpackMsg.PropInfo;
import Message.S2CPlayerMsg.PromptType;
import Message.S2CResearchMsg.RebirthRuneRsp;
import Message.S2CResearchMsg.RebirthRuneRspID;
import data.bean.t_runeBean;
import game.core.pub.script.IScript;
import game.server.logic.constant.ItemType;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.research.ResearchService;
import game.server.logic.rune.RuneService;
import game.server.logic.rune.bean.Rune;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 符文重生
 * 
 * @author liuwei
 * @date 2018年9月3日
 */
public class ReqRebirthRuneScript implements IScript {

	private final Logger logger = Logger.getLogger(ReqRebirthRuneScript.class);

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
		int type = (int) script.get(ScriptArgs.Key.ARG1);
		if (type == 1) {// 重生请求
			RebirthRuneReq req = (RebirthRuneReq) script.get(ScriptArgs.Key.ARG2);
			rebirthRune(player, req.getRuneId(), req.getIsPreview());
		} else if (type == 2) {// 分解逻辑调用
			Rune rune = (Rune) script.get(ScriptArgs.Key.ARG2);
			t_runeBean runeBean = (t_runeBean) script.get(ScriptArgs.Key.ARG3);
			List<Item> items = (List<Item>) script.get(ScriptArgs.Key.ARG4);
			calEquipRebirth(player, rune, runeBean, items);
		}

		return null;
	}

	/**
	 * 符文重生
	 * 
	 * @param player
	 * @param runeId
	 * @param isPreview
	 */
	public void rebirthRune(Player player, long runeId, boolean isPreview) {
		List<Item> items = new ArrayList<>();
		int consumeDaimond = 0;
		Rune rune = player.getRuneManager().getRuneMap().get(runeId);
		if (rune == null) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 39);// TODO替换语言包id
			return;
		}
		if (rune.getHeroId() != 0) {// 已穿戴不能重生
			MessageUtils.sendPrompt(player, PromptType.ERROR, 40);// TODO替换语言包id
			return;
		}
		if (rune.getLevel() <= 1) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 96);// TODO替换语言包id
			return;
		}
		t_runeBean runeBean = BeanTemplet.getRuneBean(rune.getModelId());
		consumeDaimond += ResearchService.getInstance().getRebornBean(4, runeBean.getQua_lvl()).getCost();
		calEquipRebirth(player, rune, runeBean, items);
		if (isPreview) {
			// 添加符文到展示的物品列表
			items.addAll(BeanFactory.createProps(rune.getModelId(), 1));
			genRebirthRuneRsp(player, isPreview, items, rune);
			return;
		}
		// 背包空间是否充足
		if (!player.getBackpackManager().isCapacityEnough(items)) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 41);// TODO替换语言包id
			return;
		}
		if (consumeDaimond > player.getDiamond()) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 10);// TODO替换语言包id
			return;
		}
		// 扣除钻石
		List<Item> needItems = new ArrayList<>();
		needItems.addAll(BeanFactory.createProps(ItemType.DIAMOND.value(), consumeDaimond));
		player.getBackpackManager().removeItems(needItems, true, Reason.REBIRTH, "");
		// 发送材料
		if (items.size() > 0) {
			player.getBackpackManager().addItems(items, true, false, Reason.REBIRTH, "");
		}
		// 还原
		restoreEquip(player, rune);
		// 添加符文到展示的物品列表
		items.addAll(BeanFactory.createProps(rune.getModelId(), 1));
		;
		// 推送
		genRebirthRuneRsp(player, isPreview, items, rune);
	}

	/**
	 * 生成协议
	 * 
	 * @param player
	 * @param isPreview
	 * @param items
	 * @param rune
	 */
	private void genRebirthRuneRsp(Player player, boolean isPreview, List<Item> items, Rune rune) {
		RebirthRuneRsp.Builder builder = RebirthRuneRsp.newBuilder();
		builder.setIsPreview(isPreview);
		builder.setRuneId(rune.getId());
		BeanFactory.combineItemList(items);
		for (Item item : items) {
			PropInfo.Builder pBuilder = PropInfo.newBuilder();
			pBuilder.setId(item.getId());
			pBuilder.setNum(item.getNum());
			builder.addItemList(pBuilder);
		}
		MessageUtils.send(player.getSession(), player.getFactory()
				.fetchSMessage(RebirthRuneRspID.RebirthRuneRspMsgID_VALUE, builder.build().toByteArray()));
		// 玩家操作日志
		LogService.getInstance().logPlayerAction(player, RebirthRuneRspID.RebirthRuneRspMsgID_VALUE, isPreview,
				rune.getModelId(), rune.getLevel());
	}

	/**
	 * 计算重生符文 （此方法不删除，只计算返回材料）
	 * 
	 * @param player
	 * @param rune
	 * @param runeBean
	 * @param items
	 */
	private void calEquipRebirth(Player player, Rune rune, t_runeBean runeBean, List<Item> items) {
		// 等级还原
		if (rune.getLevel() > 1) {
			for (int i = 1; i < rune.getLevel(); i++) {
				// 升级
				List<Item> levelItems = RuneService.getInstance().getRuneLevelupNeedItems(i, runeBean.getQua_lvl());
				if (levelItems != null) {
					items.addAll(levelItems);
				}
				// 突破
				List<Item> breakItems = RuneService.getInstance().getRuneBreakNeedItems(i);
				if (breakItems != null) {
					items.addAll(breakItems);
				}
			}
			if (rune.isBreaked()) {
				// 当前等级突破
				List<Item> breakItems = RuneService.getInstance().getRuneBreakNeedItems(rune.getLevel());
				if (breakItems != null) {
					items.addAll(breakItems);
				}
			}
		}
	}

	/**
	 * 还原符文
	 * 
	 * @param player
	 * @param rune
	 */
	private void restoreEquip(Player player, Rune rune) {
		rune.setLevel(1);
		rune.setBreaked(false);
		// 推送
		List<Long> runeList = new ArrayList<>();
		runeList.add(rune.getId());
		RuneService.getInstance().notifyRuneChange(player,runeList);
	}
}
