package logic.rune;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.C2SRuneMsg.HeroPutDownRuneReq;
import Message.C2SRuneMsg.HeroPutDownRuneReqID;
import Message.S2CPlayerMsg.PromptType;
import Message.S2CRuneMsg.HeroPutOnPutDownRuneRsp;
import Message.S2CRuneMsg.HeroPutOnPutDownRuneRspID;
import Message.S2CRuneMsg.RunePropertyMsg;
import data.bean.t_runeBean;
import game.core.pub.script.IScript;
import game.server.logic.hero.bean.Hero;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.rune.RuneManager;
import game.server.logic.rune.RuneService;
import game.server.logic.rune.bean.HeroRuneInfo;
import game.server.logic.rune.bean.Rune;
import game.server.logic.rune.bean.RuneProperty;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 拆卸符文
 * @author tzyx
 *
 */
public class ReqHeroPutDownRuneScript implements IScript {

	private final Logger logger = Logger.getLogger(ReqHeroPutDownRuneScript.class);
	
	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		HeroPutDownRuneReq req = (HeroPutDownRuneReq) script.get(ScriptArgs.Key.ARG1);
		Player player = (Player) script.get(ScriptArgs.Key.PLAYER);
		putDownRune(req, player);
		return null;
	}

	private void putDownRune(HeroPutDownRuneReq req, Player player) {
		// 检查系统开放
		if (RuneService.getInstance().runeFunctionOpen(player)) {
			return;
		}
		Hero hero = player.getHeroManager().getHero(req.getHeroId());
		if (null == hero) {
			logger.info("不存在的英雄");
			MessageUtils.sendPrompt(player, PromptType.ERROR, "不存在的英雄");
			return;
		}
		HeroRuneInfo heroRuneInfo = hero.getHeroRuneInfo();
		List<Long> runeIdsList = req.getRuneIdsList();
		RuneManager runeManager = player.getRuneManager();
		Map<Long, Long> runeIdsMap = new HashMap<>();
		List<Integer> putDownSeat = new ArrayList<>();
		Long runeId;
		Rune rune;
		t_runeBean runeBean;
		StringBuilder ids = new StringBuilder();
		// 遍历要拆卸的符文检查有效性
		for (int i = 0; i < runeIdsList.size(); i++) {
			runeId = runeIdsList.get(i);
			ids.append(runeId).append("_");
			if (null != runeId) {
				if (runeIdsMap.containsKey(runeId)) {
					logger.info("选择了重复的符文");
					MessageUtils.sendPrompt(player, PromptType.ERROR, "选择了重复的符文");
					return;
				} else {
					runeIdsMap.put(runeId, runeId);
				}
				rune = runeManager.getRuneMap().get(runeId);
				if (null != rune) {
					if (rune.getHeroId() == 0) {
						logger.info("不能拆卸未装备的符文");
						MessageUtils.sendPrompt(player, PromptType.ERROR, "不能拆卸未装备的符文");
						return;
					}
					if (rune.getHeroId() != hero.getId()) {
						logger.info("拆卸的符文不属于该英雄");
						MessageUtils.sendPrompt(player, PromptType.ERROR, "拆卸的符文不属于该英雄");
						return;
					}
					runeBean = BeanTemplet.getRuneBean(rune.getModelId());
					putDownSeat.add(runeBean.getSeat());
				} else {
					logger.info("玩家没有指定的符文");
					MessageUtils.sendPrompt(player, PromptType.ERROR, "玩家没有指定的符文");
					return;
				}
			} else {
				logger.info("不存在的符文");
				MessageUtils.sendPrompt(player, PromptType.ERROR, "不存在的符文");
				return;
			}
		}
		List<Long> changeId = new ArrayList<>();
		// 符文属性变化
		Map<Integer, RuneProperty> propMap = new HashMap<>();
		for (int i = 0; i < putDownSeat.size(); i++) {
			runeManager.putDownRune(hero, putDownSeat.get(i), propMap, changeId);
		}
		// 计算英雄符文套装
		RuneService.getInstance().calcRuneSuit(runeManager, hero, propMap);
		// 推送符文改变
		RuneService.getInstance().notifyRuneChange(player, changeId);
		// 推送战力和属性变化
		RuneService.getInstance().notifyFightAndPropertiey(player, hero);
		
		// 响应
		HeroPutOnPutDownRuneRsp.Builder builder = HeroPutOnPutDownRuneRsp.newBuilder();
		builder.setType(2);
		builder.setHeroId(hero.getId());
		builder.setHeroRune(heroRuneInfo.getBuilder());
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
		MessageUtils.send(player, player.getFactory().fetchSMessage(HeroPutOnPutDownRuneRspID.HeroPutOnPutDownRuneRspMsgID_VALUE,
				builder.build().toByteArray()));
		// 记录动作    英雄id,符文id列表
		LogService.getInstance().logPlayerAction(player, HeroPutDownRuneReqID.HeroPutDownRuneReqMsgID_VALUE,
				req.getHeroId(), ids.substring(0, ids.length() - 1));
	}
}
