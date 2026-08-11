package logic.guildwar;


import java.util.List;

import org.apache.log4j.Logger;

import Message.S2CGuildwarMsg.GetGuildInfoRsp;
import Message.S2CGuildwarMsg.GetGuildInfoRspID;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.guild.GuildService;
import game.server.logic.guild.bean.Guild;
import game.server.logic.guild.bean.GuildMember;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.guildwar.bean.GuildwarGuild;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: GetGuildInfoScript 
 * @Description: 获取联盟战公会信息
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class GetGuildInfoScript implements IScript {
     private static Logger LOGGER = Logger.getLogger(GetGuildInfoScript.class);

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
        long guildId = (long) argsMap.get(ScriptArgs.Key.ARG1);
        GetGuildInfoRsp.Builder builder = GetGuildInfoRsp.newBuilder();
        GuildwarService service = GuildwarService.getInstance();
        if (!service.getGuildList().contains(guildId)) {
			LOGGER.warn("联盟未参与活动或不存在的联盟");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 1263);
			return null;
        }
        GuildwarGuild guildwarGuild = service.getGuild(guildId);
		builder.setGuild(service.genGuildwarGuild(guildwarGuild));
        builder.setRank(guildwarGuild.getScoreRank());
        Guild guild = GuildService.getInstance().getGuilds().get(guildId);
        List<GuildMember> allMembers = guild.getAllMembers();
        GuildMember guildMember;
        for (int i = 0; i < allMembers.size(); i++) {
        	guildMember = allMembers.get(i);
        	builder.addMembers(guildMember.toMemberBuilder());
		}
		MessageUtils.send(player.getSession(), player.getFactory()
				.fetchSMessage(GetGuildInfoRspID.GetGuildInfoRspMsgID_VALUE, builder.build().toByteArray()));
		return null;
	}

}
