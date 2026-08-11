package logic.crossIndigo;

import java.util.List;

import org.apache.log4j.Logger;

import Message.S2CIndigoMsg.GetCrossIndigoRankRsp;
import Message.S2CIndigoMsg.GetCrossIndigoRankRspID;
import Message.S2CIndigoMsg.GetIndigoRankRsp;
import Message.S2CIndigoMsg.IndigoGuildRankMsg;
import Message.S2CIndigoMsg.SingleRankMsg;
import game.core.pub.script.IScript;
import game.server.config.ServerConfig;
import game.server.logic.crossIndigo.CrossIndigoManager;
import game.server.logic.crossIndigo.CrossIndigoService;
import game.server.logic.guild.GuildService;
import game.server.logic.guild.bean.Guild;
import game.server.logic.hero.bean.Hero;
import game.server.logic.player.Player;
import game.server.logic.player.RoleViewService;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 获取排行榜
 * 
 * @date 2018年10月10日
 */
public class GetCrossIndigoRankAllScript implements IScript {
	private static final Logger LOGGER = Logger.getLogger(GetCrossIndigoRankAllScript.class);

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		@SuppressWarnings("unchecked")
		List<SingleRankMsg> rankList = (List<SingleRankMsg>) script.get(Key.ARG1);
		Player player = (Player) script.get(Key.PLAYER);
		GetIndigoRankRsp.Builder firstbuilder = GetIndigoRankRsp.newBuilder();
		GetIndigoRankRsp.Builder fianlBuilder = GetIndigoRankRsp.newBuilder();
		GetIndigoRankRsp.Builder fianlQualificationBuilder = GetIndigoRankRsp.newBuilder();
		GetCrossIndigoRankRsp.Builder rankBuilder = GetCrossIndigoRankRsp.newBuilder();
		for (int i = 0; i < rankList.size(); i++) {
			SingleRankMsg rmsg = rankList.get(i);
			SingleRankMsg.Builder bui = SingleRankMsg.newBuilder(rmsg);
			Guild guild = GuildService.getInstance().getPlayerGuild(bui.getPlayerId());
			if (guild != null) {
				bui.setGuildName(guild.getName());
				Long guildId = GuildService.getInstance().getPlayerGuildId(bui.getPlayerId());
				bui.setGuildId(guildId);
			}
			firstbuilder.addRankList(bui);
			// 初赛个人排行榜
			if (rmsg.getPlayerId() == player.getPlayerId()) {
				firstbuilder.setSelfRank(bui);
			}
		}
		// 初赛个人没上榜
		if (!firstbuilder.hasSelfRank()) {
			SingleRankMsg.Builder sBuider = getPersonMsg(player);
			firstbuilder.setSelfRank(sBuider);
		}
		// 初赛联盟排行榜
		List<IndigoGuildRankMsg> guildBuilder = CrossIndigoService.getInstance().getCrossIndigoGuildRank();
		for (int i = 0; i < guildBuilder.size(); i++) {
			IndigoGuildRankMsg rmsg = guildBuilder.get(i);
			firstbuilder.addGuildRankList(rmsg);
			Long guildId = GuildService.getInstance().getPlayerGuildId(player.getPlayerId());
			// 联盟个人排行榜
			if (guildId != null && rmsg.getGuildId() == guildId) {
				firstbuilder.setSelfguildRank(rmsg);
			}

		}
		// 个人所属联盟
		if (!firstbuilder.hasSelfguildRank()) {
			String name = GuildService.getInstance().getPlayerGuildName(player.getPlayerId());
			if (name == null) {
				IndigoGuildRankMsg.Builder gBuilder = IndigoGuildRankMsg.newBuilder();
				gBuilder.setRank(0);
				gBuilder.setGuildName("");
				gBuilder.setGuildId(0);
				gBuilder.setName("");
				gBuilder.setFlag(0);
				gBuilder.setServerName("");
				gBuilder.setWinNum(0);
				gBuilder.setGrade(0);
				firstbuilder.setSelfguildRank(gBuilder);
			} else {
				IndigoGuildRankMsg.Builder buider = getGuildMsg(player);
				firstbuilder.setSelfguildRank(buider);
			}
		}
		rankBuilder.setRankList(firstbuilder);

		// 决赛排行榜
		GetIndigoRankRsp notify = (GetIndigoRankRsp) script.get(Key.ARG2);
		GetIndigoRankRsp.Builder not = GetIndigoRankRsp.newBuilder(notify);
		List<SingleRankMsg> singList = not.getRankListList();
		for (int i = 0; i < singList.size(); i++) {
			SingleRankMsg rmsg = singList.get(i);
			SingleRankMsg.Builder bui = SingleRankMsg.newBuilder(rmsg);
			Guild guild = GuildService.getInstance().getPlayerGuild(bui.getPlayerId());
			if (guild != null) {
				bui.setGuildName(guild.getName());
				Long guildId = GuildService.getInstance().getPlayerGuildId(bui.getPlayerId());
				bui.setGuildId(guildId);
			}
			fianlBuilder.addRankList(bui);
			// 决赛个人排行榜
			if (rmsg.getPlayerId() == player.getPlayerId()) {
				fianlBuilder.setSelfRank(bui);
			}
		}
		// 决赛个人没上榜
		if (!fianlBuilder.hasSelfRank()) {
			SingleRankMsg.Builder sBuider = getPersonMsg(player);
			fianlBuilder.setSelfRank(sBuider);
		}
		//决赛联盟排行榜
		List<IndigoGuildRankMsg> guildMsg = not.getGuildRankListList();
		for (int i = 0; i < guildMsg.size(); i++) {
			IndigoGuildRankMsg rmsg = guildMsg.get(i);
			fianlBuilder.addGuildRankList(rmsg);
			Long guildId = GuildService.getInstance().getPlayerGuildId(player.getPlayerId());
			// 联盟个人排行榜
			if (guildId != null && rmsg.getGuildId() == guildId) {
				fianlBuilder.setSelfguildRank(rmsg);
			}

		}
		// 个人所属联盟
		if (!fianlBuilder.hasSelfguildRank()) {
			String name = GuildService.getInstance().getPlayerGuildName(player.getPlayerId());
			if (name == null) {
				IndigoGuildRankMsg.Builder gBuilder = IndigoGuildRankMsg.newBuilder();
				gBuilder.setRank(0);
				gBuilder.setGuildName("");
				gBuilder.setGuildId(0);
				gBuilder.setName("");
				gBuilder.setFlag(0);
				gBuilder.setServerName(ServerConfig.getInstance().getServerId() + "");
				gBuilder.setWinNum(0);
				gBuilder.setGrade(0);
				fianlBuilder.setSelfguildRank(gBuilder);
			} else {
				IndigoGuildRankMsg.Builder buider = getGuildMsg(player);
				fianlBuilder.setSelfguildRank(buider);
			}
		}
		rankBuilder.setFinalRankList(fianlBuilder);

		// 如果有数据,设置初赛总排行榜
		if (!CrossIndigoService.getInstance().getPreFinalRankList().isEmpty()) {
			for (SingleRankMsg rank : CrossIndigoService.getInstance().getPreFinalRankList()) {
				fianlQualificationBuilder.addRankList(rank);
				if (rank.getPlayerId() == player.getPlayerId()) {
					fianlQualificationBuilder.setSelfRank(rank);
				}
			}
			// 决赛资格个人没上榜
			if (!fianlQualificationBuilder.hasSelfRank()) {
				SingleRankMsg.Builder sBuider = getPersonMsg(player);
				fianlQualificationBuilder.setSelfRank(sBuider);
			}
			rankBuilder.setFinalQualification(fianlQualificationBuilder);
		}
		
		MessageUtils.send(player, player.getFactory().fetchSMessage(
				GetCrossIndigoRankRspID.GetCrossIndigoRankRspMsgID_VALUE, rankBuilder.build().toByteArray()));
		LOGGER.info("发送跨服石英大会排行榜");
		return null;
	}

	// 获取个人排行榜拼接协议
	public SingleRankMsg.Builder getPersonMsg(Player player) {
		CrossIndigoManager crossManager = player.getCrossIndigoManager();
		String name = GuildService.getInstance().getPlayerGuildName(player.getPlayerId());
		Hero hero = player.getHeroManager().getLeader();
		SingleRankMsg.Builder buil = SingleRankMsg.newBuilder();
		buil.setPlayerId(player.getPlayerId());
		buil.setName(player.getPlayerName());
		buil.setRank(0);
		buil.setWinNum(crossManager.getWinNum());
		buil.setLoseNum(crossManager.getLoseNum());
		buil.setIntegral(crossManager.getIntegral());
		buil.setStar(hero.getStar());
		buil.setLvl(hero.getLevel());
		buil.setHeroId(hero.genHeroIdInfo());
		if (name != null) {
			buil.setGuildName(name);
		}
		int serverName = ServerConfig.getInstance().getServerId();
		buil.setServerName(ServerConfig.getInstance().getServerName());
		buil.setServerId(serverName);
		return buil;
	}

	// 获取个人排行榜拼接协议
	public IndigoGuildRankMsg.Builder getGuildMsg(Player player) {
		String name = GuildService.getInstance().getPlayerGuildName(player.getPlayerId());
		CrossIndigoManager crossManager = player.getCrossIndigoManager();
		IndigoGuildRankMsg.Builder builder = IndigoGuildRankMsg.newBuilder();
		builder.setGuildName(name);
		// 获取社长名称
		Guild guild = GuildService.getInstance().getPlayerGuild(player.getPlayerId());
		builder.setRank(0);
		builder.setGuildId(guild.getId());
		long id = guild.getChairman().getPlayerId();
		String leaderName = RoleViewService.getRoleNameById(id);
		builder.setName(leaderName);
		builder.setFlag(guild.getFlag());
		builder.setServerName(ServerConfig.getInstance().getServerName());
		builder.setWinNum(crossManager.getWinNum());
		builder.setGrade(crossManager.getIntegral());
		return builder;
	}
}
