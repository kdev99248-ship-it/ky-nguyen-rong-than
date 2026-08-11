package logic.guildwar;


import org.apache.log4j.Logger;

import Message.S2CGuildwarMsg.GetGuildwarFormationRsp;
import Message.S2CGuildwarMsg.GetGuildwarFormationRspID;
import Message.S2CGuildwarMsg.GuildwarPlayerMsg;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.guild.GuildService;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.guildwar.bean.GuildwarPlayer;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: GetGuildwarFormationScript 
 * @Description: 查看阵容信息 
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class GetGuildwarFormationScript implements IScript {
     private static Logger LOGGER = Logger.getLogger(GetGuildwarFormationScript.class);

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
        int type = (int) argsMap.get(ScriptArgs.Key.ARG1);
        // 活动是否可参与
        GuildwarService service = GuildwarService.getInstance();
		if (!service.guildwarOpen(player))
        	return null;
        LOGGER.info("查看阵容信息");
        // 判断是否加入联盟
        if (null == GuildService.getInstance().getPlayerGuild(player.getPlayerId())) {
        	LOGGER.warn("玩家未加入联盟");
        	MessageUtils.sendPrompt(player, PromptType.ERROR, 1257);
        	return null;
        }
        // 判断是否比赛日
        if (service.getRaceDay() == 4) {
        	LOGGER.warn("今天不是比赛日");
        	MessageUtils.sendPrompt(player, PromptType.ERROR, 1261);
        	return null;
        }
        // 判断报名时间段是否正确
        if (!service.inApplyTime()) {
        	LOGGER.warn("不在报名时间内,不能调整阵容");
        	MessageUtils.sendPrompt(player, PromptType.ERROR, 1265);
        	return null;
        }
        GetGuildwarFormationRsp.Builder builder = GetGuildwarFormationRsp.newBuilder();
        GuildwarPlayerMsg.Builder b = GuildwarPlayerMsg.newBuilder();
        // 判断玩家是否已经报名
        GuildwarPlayer appliedPlayer = service.getAppliedPlayer(player.getPlayerId());
        int[] formation;
        int heroId;
        b.setPlayerId(player.getPlayerId());
        b.setPlayerName(player.getPlayerName());
        b.setScore(0);
		if (null == appliedPlayer) {
			if (type == 1) {
	        	LOGGER.warn("玩家未报名");
	        	MessageUtils.sendPrompt(player, PromptType.ERROR, 1266); //TODO
	        	return null;
			}
			formation = player.getFormationManager().getFormation();
			for (int i = 0; i < formation.length; i++) {
				heroId = formation[i];
				b.addFormation(formation[i]);
				if (heroId != 0) {
					b.addHeros(player.getHeroManager().getHero(heroId).getBuilder());
				}
			}
        } else {
			formation = player.getFormationManager().getFormation();
			for (int i = 0; i < formation.length; i++) {
				heroId = formation[i];
				b.addFormation(formation[i]);
				if (heroId != 0) {
					b.addHeros(player.getHeroManager().getHero(heroId).getBuilder());
				}
			}
        	a:for (int i = 0; i < appliedPlayer.getFormation().getBackupHeros().size(); i++) {
        		Integer backupId = appliedPlayer.getFormation().getBackupHeros().get(i);
        		for (int j = 0; j < formation.length; j++) {
        			if (backupId == formation[j]) {
        				continue a;
        			}
				}
				b.addHeros(appliedPlayer.getHeroById(backupId).getBuilder());
        		b.addBackupHeros(backupId);
        	}
        }
		builder.setPlayer(b);
		builder.setType(type);
		MessageUtils.send(player, player.getFactory().fetchSMessage(
				GetGuildwarFormationRspID.GetGuildwarFormationRspMsgID_VALUE, builder.build().toByteArray()));
		return null;
	}

}
