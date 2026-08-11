package logic.guildwar;

import org.apache.log4j.Logger;

import Message.S2CGuildwarMsg.GuildwarNotify;
import Message.S2CGuildwarMsg.GuildwarNotifyID;
import game.core.pub.script.IScript;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: NotifyGuildwarScript
 * @Description: 登陆时联盟战推送
 * @author tzyx
 * @date 2018年8月6日 下午9:17:18
 */
public class NotifyGuildwarScript implements IScript {
	private static Logger LOGGER = Logger.getLogger(NotifyGuildwarScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		LOGGER.info("登陆时联盟战推送");
		GuildwarService service = GuildwarService.getInstance();
		if (!service.guildwarOpen())
			return null;
		ScriptArgs argsMap = (ScriptArgs) arg;
		Player player = (Player) argsMap.get(ScriptArgs.Key.PLAYER);
		GuildwarNotify.Builder builder = GuildwarNotify.newBuilder();
		builder.setGuildwarType(service.getRaceDay());
		builder.setGuildwarStatus(service.getGuildwarStatus());
		builder.setApplyed(service.checkAppliedByPlayerId(player.getPlayerId()));
		MessageUtils.send(player.getSession(), player.getFactory()
				.fetchSMessage(GuildwarNotifyID.GuildwarNotifyMsgID_VALUE, builder.build().toByteArray()));
		return null;
	}

}
