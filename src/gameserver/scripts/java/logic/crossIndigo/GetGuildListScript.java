package logic.crossIndigo;

import game.core.pub.script.IScript;
/** 请求联盟数据更新 */
public class GetGuildListScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
//		GRSimpleGuildInfoListReq.Builder guildRankRsp = GRSimpleGuildInfoListReq.newBuilder();
//		List<Guild> list = GuildService.getInstance().getRanks();
//		for (Guild g : list) {
//			SimpleGuildInfo.Builder builder = SimpleGuildInfo.newBuilder();
//			builder.setGuildId(g.getId());
//			builder.setFlag(g.getFlag());
//			builder.setGuildName(g.getName());
//			builder.setServerId(ServerConfig.getInstance().getServerId());
//			builder.setName(RoleViewService.getRoleById(g.getChairman().getPlayerId()).getName());
//			guildRankRsp.addGuilds(builder);
//		}
//		CrossServer.getInstance().send(ServerType.ROUTE_SERVER_VALUE,
//				new SMessage(GRSimpleGuildInfoListReq.MsgID.eMsgID_VALUE, guildRankRsp.build().toByteArray()));
		return null;
	}

}
