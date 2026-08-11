package logic.guildwar;


import java.util.List;

import org.apache.log4j.Logger;

import Message.S2CGuildwarMsg.GuildwarDayReportMsg.Builder;
import Message.S2CGuildwarMsg.GuildwarRaceRsp;
import Message.S2CGuildwarMsg.GuildwarRaceRspID;
import game.core.pub.script.IScript;
import game.server.logic.guild.GuildService;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.guildwar.bean.GuildwarGuild;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: GuildwarRaceScript 
 * @Description: 请求联盟战战况 
 * @author tzyx 
 * @date 2018年7月20日 下午3:53:39
 */
public class GuildwarRaceScript implements IScript {
     private static Logger LOGGER = Logger.getLogger(GuildwarRaceScript.class);

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
        int type = (int) argsMap.get(ScriptArgs.Key.INTEGER_VALUE);
        // 活动是否可参与
        GuildwarService service = GuildwarService.getInstance();
		if (!service.guildwarOpen(player))
        	return null;
        LOGGER.info("请求联盟战战况");
		GuildwarRaceRsp.Builder builder = GuildwarRaceRsp.newBuilder();
		Long guildId = GuildService.getInstance().getPlayerGuildId(player.getPlayerId());
		GuildwarGuild guildwarGuild = service.getGuildMap().get(guildId);
		if (type == 0) {// 自己所有战况
			List<Builder> l = service.genGuildwarDayReportList(player.getPlayerId(), 0);
			for (int i = 0; i < l.size(); i++) {
				builder.addRankList(l.get(i));
			}
		} else {// 公会所有战况
			if (null != guildwarGuild) {
				List<Builder> l = service.genGuildwarDayReportList(guildId, 1);
				for (int i = 0; i < l.size(); i++) {
					builder.addRankList(l.get(i));
				}
			}
		}
		builder.setType(type);
		MessageUtils.send(player, player.getFactory().fetchSMessage(
				GuildwarRaceRspID.GuildwarRaceRspMsgID_VALUE, builder.build().toByteArray()));
		return null;
	}

}
