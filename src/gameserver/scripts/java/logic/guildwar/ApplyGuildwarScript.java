package logic.guildwar;


import org.apache.log4j.Logger;

import Message.S2CGuildwarMsg.ApplyGuildwarRsp;
import Message.S2CGuildwarMsg.ApplyGuildwarRspID;
import Message.S2CGuildwarMsg.GuildwarPlayerMsg;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.guild.GuildService;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: ApplyGuildwarScript 
 * @Description: 报名联盟战 
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class ApplyGuildwarScript implements IScript {
     private static Logger LOGGER = Logger.getLogger(ApplyGuildwarScript.class);

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
        // 活动是否可参与
        GuildwarService service = GuildwarService.getInstance();
		if (!service.guildwarOpen(player))
        	return null;
        // 判断是否已经报名
        if (service.checkAppliedByPlayerId(player.getPlayerId())) {
        	LOGGER.warn("玩家已报名联盟战");
        	MessageUtils.sendPrompt(player, PromptType.ERROR, 1258);
        	return null;
        }
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
		// 给玩家返回他的当前阵容
		ApplyGuildwarRsp.Builder builder = ApplyGuildwarRsp.newBuilder();
		GuildwarPlayerMsg.Builder b = GuildwarPlayerMsg.newBuilder();
		int heroId;
		b.setPlayerId(player.getPlayerId());
		b.setPlayerName(player.getPlayerName());
		b.setScore(0);
		for (int i = 0; i < player.getFormationManager().getFormation().length; i++) {
			heroId = player.getFormationManager().getFormation()[i];
			if (heroId != 0) {
				b.addHeros(player.getHeroManager().getHero(heroId).getBuilder());
			}
			b.addFormation(heroId);
		}
		builder.setPlayer(b);
		MessageUtils.send(player, player.getFactory().fetchSMessage(
				ApplyGuildwarRspID.ApplyGuildwarRspMsgID_VALUE, builder.build().toByteArray()));
	
		return null;
	}

}
