package logic.rune;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.C2SRuneMsg.RuneOneKeyLevelUpReq;
import Message.C2SRuneMsg.RuneOneKeyLevelUpReqID;
import Message.S2CPlayerMsg.PromptType;
import Message.S2CRuneMsg.RuneOneKeyLevelUpRsp;
import Message.S2CRuneMsg.RuneOneKeyLevelUpRspID;
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
 * 符文一键升级
 * 
 * @author tzyx
 *
 */
public class ReqRuneOneKeyLevelUpScript implements IScript {

	private final Logger logger = Logger.getLogger(ReqRuneOneKeyLevelUpScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		RuneOneKeyLevelUpReq req = (RuneOneKeyLevelUpReq) script.get(ScriptArgs.Key.ARG1);
		Player player = (Player) script.get(ScriptArgs.Key.PLAYER);
		runeOneKeyLevelup(req, player);
		return null;
	}

	private void runeOneKeyLevelup(RuneOneKeyLevelUpReq req, Player player) {
		// 检查系统开放
		if (RuneService.getInstance().runeFunctionOpen(player)) {
			return;
		}
		List<Long> runeIdsList = req.getRuneIdsList();
		Rune rune;
		RuneManager runeManager = player.getRuneManager();
		StringBuilder ids = new StringBuilder();
		int heroId = 0;
		for (int i = 0; i < runeIdsList.size(); i++) {
			rune = runeManager.getRuneMap().get(runeIdsList.get(i));
			ids.append(runeIdsList.get(i)).append("_");
			// 检查符文是否存在
			if (null == rune) {
				logger.info("不存在的符文");
				MessageUtils.sendPrompt(player, PromptType.ERROR, "不存在的符文");
				return;
			}
			heroId = rune.getHeroId();
		}
		// 符文属性变化
		StringBuilder rb = new StringBuilder();
		StringBuilder sb = new StringBuilder();
		Map<Integer, RuneProperty> propMap = new HashMap<>();
		for (int i = 0; i < runeIdsList.size(); i++) {
			rune = runeManager.getRuneMap().get(runeIdsList.get(i));
			rb.append(rune.getLevel()).append("_");
		}
		List<Long> changedList = new ArrayList<>(); //响应只返回有升级的符文信息
		while (true) {
			boolean key = false;
			for (int i = 0; i < runeIdsList.size(); i++) {
				boolean flag = true;
				rune = runeManager.getRuneMap().get(runeIdsList.get(i));
				// 检查有没有对应的配置
				t_rune_updateBean runeUpdateBean = BeanTemplet.getRuneUpdateBean(rune.getLevel());
				if (null == runeUpdateBean) {
					logger.info("一键升级符文,没有找到升级配置");
					flag = false;
				}
				// 如果有突破消耗,检查是否突破
				if (null != runeUpdateBean && !StringUtils.isEmpty(runeUpdateBean.getBreakcost()) && !rune.isBreaked()) {
					logger.info("一键升级符文,符文未突破,跳过");
					flag = false;
				}
				// 获取消耗
				t_runeBean runeBean = BeanTemplet.getRuneBean(rune.getModelId());
				List<Item> needItems = new ArrayList<>();
				if (null != runeBean) {
					needItems = RuneService.getInstance().getRuneLevelupNeedItems(rune.getLevel(),
							runeBean.getQua_lvl());
					if (needItems == null || !player.getBackpackManager().isItemNumEnough(needItems)) {
						logger.info("一键升级符文,道具不足,跳过!");
						flag = false;
					}
				} else {
					logger.info("没有找到符文的配置,跳过一键升级!");
					flag = false;
				}
				if (flag) {
					// 符文升级
					RuneService.getInstance().runeLevelUp(player, rune, 1, false, propMap);
					// 扣除消耗
					player.getBackpackManager().removeItems(needItems, true, Reason.RUNE_ONEKEY_LVLUP, "");
					key = true;
					if (!changedList.contains(rune.getId()))
						changedList.add(rune.getId());
				}
			}
			if (!key) {
				break;
			}
		}
		RuneOneKeyLevelUpRsp.Builder builder = RuneOneKeyLevelUpRsp.newBuilder();
		for (int i = 0; i < runeIdsList.size(); i++) {
			rune = runeManager.getRuneMap().get(runeIdsList.get(i));
			sb.append(rune.getLevel()).append("_");
			builder.addRunes(rune.getBuilder());
			if (changedList.contains(rune.getId()))
				builder.addRunes(rune.getBuilder());
		}
		// 推送英雄属性变化
		if (heroId != 0) {
			Hero hero = player.getHeroManager().getHero(heroId);
			if (null != hero) {
				RuneService.getInstance().notifyFightAndPropertiey(player, hero);
			}
		}
		builder.setHeroId(heroId);
		RunePropertyMsg.Builder p;
		for (RuneProperty prop : propMap.values()) {
			if (prop.getNum() != 0) {
				p = RunePropertyMsg.newBuilder();
				p.setId(prop.getId());
				p.setNum(prop.getNum());
				builder.addPropers(p);
			}
		}
		// 推送符文变化
		RuneService.getInstance().notifyRuneChange(player, runeIdsList);
		// 推送
		MessageUtils.send(player, player.getFactory()
				.fetchSMessage(RuneOneKeyLevelUpRspID.RuneOneKeyLevelUpRspMsgID_VALUE, builder.build().toByteArray()));
		// 记录动作 符文id列表 符文升级前等级 符文升级后等级
		LogService.getInstance().logPlayerAction(player, RuneOneKeyLevelUpReqID.RuneOneKeyLevelUpReqMsgID_VALUE,
				ids.substring(0, ids.length() - 1), rb.substring(0, rb.length() - 1), sb.substring(0, sb.length() - 1));
	}
}
