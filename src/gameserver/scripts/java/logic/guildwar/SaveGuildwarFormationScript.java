package logic.guildwar;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.C2SGuildwarMsg.SaveGuildwarFormationReqID;
import Message.S2CGuildwarMsg.GuildwarPlayerMsg;
import Message.S2CGuildwarMsg.SaveGuildwarFormationRsp;
import Message.S2CGuildwarMsg.SaveGuildwarFormationRspID;
import Message.S2CPlayerMsg.PromptType;
import game.constant.PropertyType;
import game.core.pub.script.IScript;
import game.core.pub.script.ScriptManager;
import game.server.logic.constant.TaskConditionType;
import game.server.logic.guild.GuildService;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.guildwar.bean.GuildwarFormation;
import game.server.logic.guildwar.bean.GuildwarGuild;
import game.server.logic.guildwar.bean.GuildwarPlayer;
import game.server.logic.guildwar.bean.GuildwarRaceRecord;
import game.server.logic.hero.bean.Hero;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: SaveGuildwarFormationScript 
 * @Description: 报名联盟战 
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class SaveGuildwarFormationScript implements IScript {
     private static Logger LOGGER = Logger.getLogger(SaveGuildwarFormationScript.class);

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Object call(String scriptName, Object arg) {
        ScriptArgs argsMap = (ScriptArgs) arg;
        Player player = (Player) argsMap.get(ScriptArgs.Key.PLAYER);
        @SuppressWarnings("unchecked")
		List<Integer> heroIds = (List<Integer>) argsMap.get(ScriptArgs.Key.ARG1);
        // 活动是否可参与
        GuildwarService service = GuildwarService.getInstance();
		if (!service.guildwarOpen(player))
        	return null;
        // 判断是否加入联盟
        if (null == GuildService.getInstance().getPlayerGuild(player.getPlayerId())) {
        	LOGGER.warn("玩家未加入联盟");
        	MessageUtils.sendPrompt(player, PromptType.ERROR, 1257);
        	return null;
        }
        // 判断报名时间段是否正确
        if (!service.inApplyTime()) {
        	LOGGER.warn("不在报名时间内");
        	MessageUtils.sendPrompt(player, PromptType.ERROR, 1256);
        	return null;
        }
        // 判断是否比赛日
        Long guildId = GuildService.getInstance().getPlayerGuildId(player.getPlayerId());
		switch (service.getRaceDay()) {
		case 1: // 积分赛随便报名
			break;
		case 2: // 淘汰赛根据资格报名
			if (!service.getKnockoutGuild().contains(guildId)) {
				LOGGER.warn("您所在的联盟没有参与淘汰赛的资格");
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1259);
				return null;
			}
			break;
		case 3: // 决赛赛根据资格报名
			if (!service.getFinalGuild().contains(guildId)) {
				LOGGER.warn("您所在的联盟没有参与决赛的资格");
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1260);
				return null;
			}
			break;
		default:
			LOGGER.warn("今天不是比赛日");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 1261);
			return null;
		}
		// 玩家报名
		// 如果联盟还没有注册到联盟战,则先将联盟注册到联盟战
		GuildwarGuild guildwarGuild = service.getGuildMap().get(guildId);
		if (null == guildwarGuild) {
			guildwarGuild = new GuildwarGuild();
			guildwarGuild.setGuildId(guildId);
			service.getGuildMap().put(guildId, guildwarGuild);
			service.getGuildList().add(guildId);
		}
		GuildwarFormation f = new GuildwarFormation();
		GuildwarPlayerMsg.Builder b = GuildwarPlayerMsg.newBuilder();
		b.setPlayerId(player.getPlayerId());
		b.setPlayerName(player.getPlayerName());
		b.setScore(0);
		int heroId;
		Hero hero;
		GuildwarPlayer p;
		List<Hero> heros = new ArrayList<>();
		// 如果玩家没有报过名
		if (!service.getAppliedPlayers().containsKey(player.getPlayerId())) {
			p = new GuildwarPlayer();
			p.setPlayerId(player.getPlayerId());
			p.setPlayerName(player.getPlayerName());
			p.setLvl(player.getLevel());
			p.setGuildId(guildId);
		} else {
			p = service.getAppliedPlayer(player.getPlayerId());
		}
		StringBuilder sb1 = new StringBuilder();
		for (int i = 0; i < player.getFormationManager().getFormation().length; i++) {
			heroId = player.getFormationManager().getFormation()[i];
			if (heroId == 0) {
				LOGGER.warn("阵容不满");
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1268);
				return null;
			}
			sb1.append(heroId).append("_");
			f.getFormation()[i] = heroId;
			b.addFormation(player.getFormationManager().getFormation()[i]);
			b.addHeros(player.getHeroManager().getHero(heroId).getBuilder());
			hero = new Hero();
			hero.fromJsonWithPros(player.getHeroManager().getHero(heroId).toJsonWithPros());
			// 设置当前血量和最大血量
			int maxHp = hero.getProperty().getFinalProperty(PropertyType.HP.value());
			p.setHeroMaxHp(hero.getId(), maxHp);
			p.setHeroHp(hero.getId(), maxHp);
			heros.add(hero);
		}
		StringBuilder sb2 = new StringBuilder();
		for (int i = 0; i < heroIds.size(); i++) {
			heroId = heroIds.get(i);
			if (null == player.getHeroManager().getHero(heroId)) {
				LOGGER.warn("不存在的替补");
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1269);
				return null;
			}
			for (int j = 0; j < f.getFormation().length; j++) {
				if (heroId == f.getFormation()[j]) {
					LOGGER.warn("阵容错误:替补英雄已上阵");
					MessageUtils.sendPrompt(player, PromptType.ERROR, 1270);
					return null;
				}
			}
			sb2.append(heroId).append("_");
			hero = new Hero();
			hero.fromJsonWithPros(player.getHeroManager().getHero(heroId).toJsonWithPros());
			b.addHeros(player.getHeroManager().getHero(heroId).getBuilder());
			b.addBackupHeros(heroId);
			f.getBackupHeros().add(heroId);
			// 设置当前血量和最大血量
			int maxHp = hero.getProperty().getFinalProperty(PropertyType.HP.value());
			p.setHeroMaxHp(hero.getId(), maxHp);
			p.setHeroHp(hero.getId(), maxHp);
			heros.add(hero);
		}
		p.setHeros(heros);
		p.setFormation(f);
		// 遍历所有战斗记录,将该玩家的战斗记录放到报名信息上
		Map<String, GuildwarRaceRecord> allRecord = service.getAllRecord();
		for (GuildwarRaceRecord record : allRecord.values()) {
			if (record.getBlueId() == p.getPlayerId() || record.getRedId() == p.getPlayerId()) {
				String key = String.valueOf(record.getRaceDay() == 1 ? record.getRaceDay() + record.getRaceDayIndex() - 1 : record.getRaceDay() + 3);
				if (p.getAllRace().containsKey(key)) {
					p.getAllRace().get(key).add(record.getId());
				} else {
					List<String> l = new ArrayList<>();
					l.add(record.getId());
					p.getAllRace().put(key, l);
				}
			}
		}
		// 参赛联盟信息添加报名玩家
		if (!guildwarGuild.getParter().contains(player.getPlayerId())) {
			guildwarGuild.getParter().add(player.getPlayerId());
		}
		// 所有报名信息添加报名玩家
		service.getAppliedPlayers().put(player.getPlayerId(), p);
		service.getAppliedPlayerList().add(player.getPlayerId());
		SaveGuildwarFormationRsp.Builder builder = SaveGuildwarFormationRsp.newBuilder();
		builder.setPlayer(b);
		MessageUtils.send(player, player.getFactory().fetchSMessage(
				SaveGuildwarFormationRspID.SaveGuildwarFormationRspMsgID_VALUE, builder.build().toByteArray()));
		// 推送给客户端刷新界面
		ScriptArgs args = new ScriptArgs();
		List<Long> l = new ArrayList<>();
		l.add(player.getPlayerId());
		args.put(ScriptArgs.Key.ARG1, l);
		ScriptManager.getInstance().call("logic.guildwar.NotifyRaceCountdownScript", args);
		// 任务更新
		player.getTaskManager().updateTaskCondition(TaskConditionType.APPLY_GUILD_WAR, 1);
		// 记录动作   联盟id,阵容id,替补id
		if (sb2.length() > 0) {
			LogService.getInstance().logPlayerAction(player, SaveGuildwarFormationReqID.SaveGuildwarFormationReqMsgID_VALUE
					, guildId, sb1.substring(0, sb1.length() - 1)
					, sb2.substring(0, sb2.length() - 1));
		} else {
			LogService.getInstance().logPlayerAction(player, SaveGuildwarFormationReqID.SaveGuildwarFormationReqMsgID_VALUE
					, guildId, sb1.substring(0, sb1.length() - 1));
		}
		return null;
	}

}
