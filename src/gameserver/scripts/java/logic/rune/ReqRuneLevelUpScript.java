package logic.rune;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.C2SRuneMsg.RuneLevelUpReq;
import Message.C2SRuneMsg.RuneLevelUpReqID;
import Message.S2CPlayerMsg.PromptType;
import Message.S2CRuneMsg.RuneOneKeyLevelUpRsp;
import Message.S2CRuneMsg.RuneOneKeyLevelUpRspID;
import Message.S2CRuneMsg.RunePropertyMsg;
import data.bean.t_runeBean;
import data.bean.t_rune_updateBean;
import game.core.pub.script.IScript;
import game.core.pub.util.StringUtils;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.rune.RuneService;
import game.server.logic.rune.bean.Rune;
import game.server.logic.rune.bean.RuneProperty;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 符文升级
 * @author tzyx
 *
 */
public class ReqRuneLevelUpScript implements IScript {

	private final Logger logger = Logger.getLogger(ReqRuneLevelUpScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		RuneLevelUpReq req = (RuneLevelUpReq) script.get(ScriptArgs.Key.ARG1);
		Player player = (Player) script.get(ScriptArgs.Key.PLAYER);
		runeLevelup(req, player);
		return null;
	}

	private void runeLevelup(RuneLevelUpReq req, Player player) {
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
		// 检查符文可否升级
		// 检查当前等级是否大于等于玩家等级
		if (rune.getLevel() >= player.getLevel()) {
			logger.info("玩家等级不足,请提升玩家等级");
			MessageUtils.sendPrompt(player, PromptType.ERROR, "玩家等级不足,请提升玩家等级");
			return;
		}
		// 检查有没有对应的配置
		t_rune_updateBean runeUpdateBean = BeanTemplet.getRuneUpdateBean(rune.getLevel());
		if (null == runeUpdateBean) {
			logger.info("符文不可升级");
			MessageUtils.sendPrompt(player, PromptType.ERROR, "符文不可升级");
			return;
		}
		// 如果有突破消耗,检查是否突破
		if (!StringUtils.isEmpty(runeUpdateBean.getBreakcost()) && !rune.isBreaked()) {
			logger.info("符文未突破,不可升级");
			MessageUtils.sendPrompt(player, PromptType.ERROR, "符文未突破,不可升级");
			return;
		}
		// 获取消耗
		t_runeBean runeBean = BeanTemplet.getRuneBean(rune.getModelId());
		List<Item> needItems = RuneService.getInstance().getRuneLevelupNeedItems(rune.getLevel(), runeBean.getQua_lvl());
		if (!player.getBackpackManager().isItemNumEnough(needItems)) {
			logger.error("道具不足!");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 332);
			return;
		}
		// 符文属性变化
		Map<Integer, RuneProperty> propMap = new HashMap<>();
		// 符文升一级
		int old = rune.getLevel();
		RuneService.getInstance().runeLevelUp(player, rune, 1, true, propMap);
		// 扣除消耗
		if (!needItems.isEmpty()) {
			player.getBackpackManager().removeItems(needItems, true, Reason.RUNE_LVLUP, "");
		}
		// 响应
		RuneOneKeyLevelUpRsp.Builder builder = RuneOneKeyLevelUpRsp.newBuilder();
		builder.setHeroId(rune.getHeroId());
		builder.addRunes(rune.getBuilder());
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
		MessageUtils.send(player, player.getFactory().fetchSMessage(RuneOneKeyLevelUpRspID.RuneOneKeyLevelUpRspMsgID_VALUE,
				builder.build().toByteArray()));
		// 记录动作    符文id 原等级 现等级
		LogService.getInstance().logPlayerAction(player, RuneLevelUpReqID.RuneLevelUpReqMsgID_VALUE,
				req.getRuneId(), old, rune.getLevel());
	}
}
