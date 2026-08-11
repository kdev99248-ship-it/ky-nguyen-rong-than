package logic.crossIndigo;

import java.util.List;
import java.util.Map;

import Message.Inner.GRCrossIndigo.GRGetGuildInfoReq;
import Message.Inner.GRCrossIndigo.GRGetGuildInfoRsp;
import Message.Inner.GRCrossIndigo.GRGuildMsg;
import Message.Inner.InnerServer.ServerType;
import game.core.mina.message.SMessage;
import game.core.pub.script.IScript;
import game.server.cross.CrossServer;
import game.server.logic.guild.GuildService;
import game.server.logic.guild.bean.Guild;
import game.server.logic.player.RoleView;
import game.server.logic.player.RoleViewService;
import game.server.logic.util.ScriptArgs;

/**
 * 响应跨服查询公会信息
 * @author tzyx
 *
 */
public class GetCrossIndigoGuildInfoScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs args = (ScriptArgs) arg;
		GRGetGuildInfoReq getData = (GRGetGuildInfoReq) args.get(ScriptArgs.Key.ARG1);
		List<Long> guildIdsList = getData.getGuildIdsList();
		GRGetGuildInfoRsp.Builder builder = GRGetGuildInfoRsp.newBuilder();
		Map<Long, Guild> guilds = GuildService.getInstance().getGuilds();
		for (Long guildId : guildIdsList) {
			Guild guild = guilds.get(guildId);
			if (null != guild) {
				GRGuildMsg.Builder b = GRGuildMsg.newBuilder();
				b.setGuildId(guild.getId());
				b.setGuildName(guild.getName());
				RoleView chairman = RoleViewService.getRoleById(guild.getChairman().getPlayerId());
				b.setChairManName(chairman.getName());
				b.setFlag(guild.getFlag());
				builder.addGuilds(b);
			}
		}
		CrossServer.getInstance().send(ServerType.ROUTE_SERVER_VALUE,
				new SMessage(GRGetGuildInfoRsp.MsgID.eMsgID_VALUE, builder.build().toByteArray()));
		return null;

	}

}
