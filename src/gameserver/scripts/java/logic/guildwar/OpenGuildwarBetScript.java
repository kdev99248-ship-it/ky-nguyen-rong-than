package logic.guildwar;


import java.util.List;

import org.apache.log4j.Logger;

import Message.S2CGuildwarMsg.BetGuildwarRsp;
import Message.S2CGuildwarMsg.OpenGuildwarBetRspID;
import game.core.pub.script.IScript;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.guildwar.bean.GuildwarGuild;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: OpenGuildwarBetScript 
 * @Description: 打开助威界面
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class OpenGuildwarBetScript implements IScript {
     private static Logger LOGGER = Logger.getLogger(OpenGuildwarBetScript.class);

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
		LOGGER.info("打开助威界面");
        GuildwarService service = GuildwarService.getInstance();
		if (!service.guildwarOpen(player))
        	return null;
        long playerId = player.getPlayerId();
		BetGuildwarRsp.Builder builder = BetGuildwarRsp.newBuilder();
		List<Long> guildList = service.getGuildList();
		for (int i = 0; i < guildList.size(); i++) {
			GuildwarGuild guildwarGuild = service.getGuildMap().get(guildList.get(i));
			if (guildwarGuild.getParter().size() > 0) {
				builder.addGuilds(service.genGuildwarGuild(guildwarGuild));
			}
		}
		Long betGuild = service.getPunters().get(playerId);
		builder.setGuildId(null == betGuild ? 0 : betGuild);
		MessageUtils.send(player, player.getFactory().fetchSMessage(
				OpenGuildwarBetRspID.OpenGuildwarBetRspMsgID_VALUE, builder.build().toByteArray()));
		return null;
	}

}
