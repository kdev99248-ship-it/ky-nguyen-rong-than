package logic.rune;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.C2SRuneMsg.RuneBreakReq;
import Message.C2SRuneMsg.RuneBreakReqID;
import Message.S2CPlayerMsg.PromptType;
import Message.S2CRuneMsg.RuneBreakRsp;
import Message.S2CRuneMsg.RuneBreakRspID;
import Message.S2CRuneMsg.RunePropertyMsg;
import data.bean.t_runeBean;
import data.bean.t_rune_updateBean;
import game.core.pub.script.IScript;
import game.core.pub.util.StringUtils;
import game.server.logic.constant.Reason;
import game.server.logic.hero.bean.Hero;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.rune.RuneManager;
import game.server.logic.rune.RuneService;
import game.server.logic.rune.bean.Rune;
import game.server.logic.rune.bean.RuneProperty;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 符文重生
 * @author tzyx
 *
 */
public class ReqRuneBreakScript implements IScript {

	private final Logger logger = Logger.getLogger(ReqRuneBreakScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		RuneBreakReq req = (RuneBreakReq) script.get(ScriptArgs.Key.ARG1);
		Player player = (Player) script.get(ScriptArgs.Key.PLAYER);
		runeBreak(req, player);
		return null;
	}

	private void runeBreak(RuneBreakReq req, Player player) {
		// 检查系统开放
		if (RuneService.getInstance().runeFunctionOpen(player)) {
			return;
		}
		long runeId = req.getRuneId();
		Rune rune = player.getRuneManager().getRuneMap().get(runeId);
		// 检查符文是否存在
		if (null == rune) {
			logger.info("不存在的符文");
			MessageUtils.sendPrompt(player, PromptType.ERROR, "不存在的符文");
			return;
		}
		// 检查符文可否突破
		// 检查有没有对应的配置
		t_rune_updateBean runeUpdateBean = BeanTemplet.getRuneUpdateBean(rune.getLevel());
		if (null == runeUpdateBean || StringUtils.isEmpty(runeUpdateBean.getBreakcost())) {
			logger.info("符文不可突破");
			MessageUtils.sendPrompt(player, PromptType.ERROR, "符文不可突破");
			return;
		}
		// 如果有突破消耗,检查是否突破
		if (rune.isBreaked()) {
			logger.info("符文已突破");
			MessageUtils.sendPrompt(player, PromptType.ERROR, "符文已突破");
			return;
		}
		// 获取消耗
		List<Item> needItems = RuneService.getInstance().getRuneBreakNeedItems(rune.getLevel());
		if (!player.getBackpackManager().isItemNumEnough(needItems)) {
			logger.error("道具不足!");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 332);
			return;
		}
		// 符文属性变化
		Map<Integer, RuneProperty> propMap = new HashMap<>();
		// 符文突破
		runeBreak(player, rune, propMap);
		// 扣除消耗
		if (!needItems.isEmpty()) {
			player.getBackpackManager().removeItems(needItems, true, Reason.RUNE_BREAK, "");
		}
		// 响应
		RuneBreakRsp.Builder builder = RuneBreakRsp.newBuilder();
		builder.setHeroId(rune.getHeroId());
		builder.setRune(rune.getBuilder());
		RunePropertyMsg.Builder p;
		for (RuneProperty prop : propMap.values()) {
			if (prop.getNum() != 0) {
				p = RunePropertyMsg.newBuilder();
				p.setId(prop.getId());
				p.setNum(prop.getNum());
				builder.addPropers(p);
			}
		}
		// 推送
		MessageUtils.send(player, player.getFactory().fetchSMessage(RuneBreakRspID.RuneBreakRspMsgID_VALUE,
				builder.build().toByteArray()));
		// 记录动作    符文id
		LogService.getInstance().logPlayerAction(player, RuneBreakReqID.RuneBreakReqMsgID_VALUE,
				req.getRuneId());
	}

	/**
	 * 符文突破
	 */
	public void runeBreak(Player player, Rune rune, Map<Integer, RuneProperty> propMap) {
		RuneService service = RuneService.getInstance();
		if (rune.getHeroId() != 0) {
			Hero hero = player.getHeroManager().getHero(rune.getHeroId());
			RuneManager runeManager = player.getRuneManager();
			t_runeBean runeBean = BeanTemplet.getRuneBean(rune.getModelId());
			// 脱下符文
			runeManager.putDownRune(hero, runeBean.getSeat(), propMap, null);
			// 增加符文等级
			rune.setBreaked(true);
			// 穿上符文
			runeManager.putOnRune(hero, runeBean.getSeat(), rune.getId(), propMap, null);
			// 推送符文变化
			List<Long> runeIds = new ArrayList<>();
			runeIds.add(rune.getId());
			service.notifyRuneChange(player, runeIds);
			// 推送属性变化
			service.notifyFightAndPropertiey(player, hero);
		} else {
			rune.setBreaked(true);
			// 推送符文变化
			List<Long> runeIds = new ArrayList<>();
			runeIds.add(rune.getId());
			service.notifyRuneChange(player, runeIds);
		}
	}
}
