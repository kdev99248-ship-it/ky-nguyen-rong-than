package logic.crossIndigo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import Message.C2SIndigoMsg.FormationList;
import Message.S2CHeroMsg.PropertyMsg;
import Message.S2CPlayerMsg.PromptType;
import Message.Inner.GRCrossIndigo.GRHeroMsg;
import Message.Inner.GRCrossIndigo.GRSaveFormationReq;
import Message.Inner.GRCrossIndigo.GRSaveHeroesReq;
import Message.Inner.InnerServer.ServerType;
import data.bean.t_crosspvpBean;
import game.constant.PropertyType;
import game.core.mina.message.SMessage;
import game.core.pub.script.IScript;
import game.server.cross.CrossServer;
import game.server.logic.crossIndigo.CrossIndigoService;
import game.server.logic.guild.GuildService;
import game.server.logic.hero.bean.Hero;
import game.server.logic.hero.bean.HeroSkill;
import game.server.logic.player.Player;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

public class SaveHeroAndFormationScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs args = (ScriptArgs) arg;
		Player player = (Player) args.get(ScriptArgs.Key.PLAYER);
		GRSaveHeroesReq.Builder build = GRSaveHeroesReq.newBuilder();
		// 封装发送
		build.setPlayerId(player.getPlayerId());
		build.setLevel(player.getLevel());
		build.setPlayerName(player.getPlayerName());
		build.setOffensiveAdd(player.getFormationManager().getOffensive());
		Long guildId = GuildService.getInstance().getPlayerGuildId(player.getPlayerId());
		if (null != guildId)
			build.setGuildId(guildId);
		else
			build.setGuildId(0);
		String guildName = GuildService.getInstance().getPlayerGuildName(player.getPlayerId());
		if (null != guildName)
			build.setGuildName(guildName);
		else
			build.setGuildName("");
		// 跨服石英大会决赛期
		for (Hero hero : player.getCrossIndigoManager().getFormation()) {
			GRHeroMsg.Builder heroMsgBuilder = GRHeroMsg.newBuilder();
			heroMsgBuilder.setId(hero.getId());
			heroMsgBuilder.setSkinId(hero.getSkinId());
			heroMsgBuilder.setLevel(hero.getLevel());
			heroMsgBuilder.setStep(hero.getStep());
			heroMsgBuilder.setStar(hero.getStar());
			heroMsgBuilder.setPower(hero.getPower());
			for (HeroSkill skill : hero.getSkillMap().values()) {
				heroMsgBuilder.addSkillList(skill.getBuilder());
			}
			// 战斗所需数据
			Map<Integer, Integer> propertyMap = new HashMap<>();
			int value = hero.getProperty().getFinalProperty(PropertyType.HP.value());
			propertyMap.put(PropertyType.HP.value(), value);
			value = hero.getProperty().getFinalProperty(PropertyType.HP.value());
			propertyMap.put(PropertyType.HP_LIMIT.value(), value);
			value = hero.getProperty().getFinalProperty(PropertyType.ATT.value());
			propertyMap.put(PropertyType.ATT.value(), value);
			value = hero.getProperty().getFinalProperty(PropertyType.PDEF.value());
			propertyMap.put(PropertyType.PDEF.value(), value);
			value = hero.getProperty().getFinalProperty(PropertyType.MDEF.value());
			propertyMap.put(PropertyType.MDEF.value(), value);
			value = hero.getProperty().getFinalProperty(PropertyType.HURT_BOOST.value());
			propertyMap.put(PropertyType.HURT_BOOST.value(), value);
			value = hero.getProperty().getFinalProperty(PropertyType.HURT_REDUCE.value());
			propertyMap.put(PropertyType.HURT_REDUCE.value(), value);
			value = hero.getProperty().getFinalProperty(PropertyType.HIT.value());
			propertyMap.put(PropertyType.HIT.value(), value);
			value = hero.getProperty().getFinalProperty(PropertyType.DOD.value());
			propertyMap.put(PropertyType.DOD.value(), value);
			value = hero.getProperty().getFinalProperty(PropertyType.CRIT.value());
			propertyMap.put(PropertyType.CRIT.value(), value);
			value = hero.getProperty().getFinalProperty(PropertyType.PARRY.value());
			propertyMap.put(PropertyType.PARRY.value(), value);
			value = hero.getProperty().getFinalProperty(PropertyType.FIGHT_HURT_BOOST_PER.value());
			propertyMap.put(PropertyType.FIGHT_HURT_BOOST_PER.value(), value);
			value = hero.getProperty().getFinalProperty(PropertyType.FIGHT_HURT_REDUCE_PER.value());
			propertyMap.put(PropertyType.FIGHT_HURT_REDUCE_PER.value(), value);
			for (Integer proId : propertyMap.keySet()) {
				int proValue = propertyMap.get(proId);
				PropertyMsg.Builder proBuilder = PropertyMsg.newBuilder();
				proBuilder.setId(proId);
				proBuilder.setNum(proValue);
				heroMsgBuilder.addProList(proBuilder.build());
			}
			build.addHeroList(heroMsgBuilder);
		}

		// boolean success = autoFormation(player, send);
		// if (success)
		CrossServer.getInstance().send(ServerType.ROUTE_SERVER_VALUE,
				new SMessage(GRSaveHeroesReq.MsgID.eMsgID_VALUE, build.build().toByteArray()));
		return null;
	}

	private boolean autoFormation(Player player, GRSaveFormationReq.Builder builder) {
		int dayNum = CrossIndigoService.getInstance().getDayNum();
		t_crosspvpBean crossBean = BeanTemplet.getCrossBean(dayNum + 5);
		String str = null;
		String maxStr = null;
		if (null == crossBean) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 139);
			return false;
		}
		int index = 0;
		Map<Integer, Integer> useNum = new HashMap<>();
		List<Hero> heros = player.getCrossIndigoManager().getFormation();
		for (int i = 0; i < 3; i++) {
			if (i == 0) {
				String strAll = StringUtils.split(crossBean.getFirst(), "|")[0];
				str = strAll.split("_")[0];
				maxStr = strAll.split("_")[1];
			} else if (i == 1) {
				String strAll = StringUtils.split(crossBean.getSecond(), "|")[0];
				str = strAll.split("_")[0];
				maxStr = strAll.split("_")[1];
			} else if (i == 2) {
				String strAll = StringUtils.split(crossBean.getThird(), "|")[0];
				str = strAll.split("_")[0];
				maxStr = strAll.split("_")[1];
			}
			int needSize = Integer.parseInt(str);
			int max = Integer.parseInt(maxStr);
			FormationList.Builder listMsg = FormationList.newBuilder();
			if (index >= heros.size())
				index = 0;
			int usedNum = 0;
			for (int j = index; j < heros.size(); j++) {
				if (heros.isEmpty() || (needSize < usedNum && max <= usedNum)) {
					break;
				}
				Hero hero = heros.get(j);
				int heroId = hero.getId();
				if (useNum.containsKey(heroId) && useNum.get(heroId) >= crossBean.getTimes()) {
					continue;
				}
				if (j % 6 == 0) {
					listMsg.setHeroId1(heroId);
				} else if (j % 6 == 1) {
					listMsg.setHeroId2(heroId);
				} else if (j % 6 == 2) {
					listMsg.setHeroId3(heroId);
				} else if (j % 6 == 3) {
					listMsg.setHeroId4(heroId);
				} else if (j % 6 == 4) {
					listMsg.setHeroId5(heroId);
				} else if (j % 6 == 5) {
					listMsg.setHeroId6(heroId);
				}
				usedNum++;
				useNum.merge(heroId, 1, (n, m) -> n + m);
			}
			// 检查参数有效性
			if (!listMsg.hasHeroId2()) {
				listMsg.setHeroId2(0);
			}
			if (!listMsg.hasHeroId3()) {
				listMsg.setHeroId3(0);
			}
			if (!listMsg.hasHeroId4()) {
				listMsg.setHeroId4(0);
			}
			if (!listMsg.hasHeroId5()) {
				listMsg.setHeroId5(0);
			}
			if (!listMsg.hasHeroId6()) {
				listMsg.setHeroId6(0);
			}
			index += usedNum;
			if (listMsg.hasHeroId1())
				builder.addFormation(listMsg);
		}
		return true;
	}

}
