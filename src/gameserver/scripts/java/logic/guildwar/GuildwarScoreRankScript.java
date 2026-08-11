package logic.guildwar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.S2CGuildwarMsg.GuildwarGuildMsg;
import Message.S2CGuildwarMsg.GuildwarScoreRankRsp;
import Message.S2CGuildwarMsg.GuildwarScoreRankRspID;
import game.core.pub.script.IScript;
import game.server.logic.guild.GuildService;
import game.server.logic.guild.bean.Guild;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.guildwar.bean.GuildwarGuild;
import game.server.logic.player.Player;
import game.server.logic.player.RoleView;
import game.server.logic.player.RoleViewService;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: GuildwarScoreRankScript
 * @Description: 请求联盟战积分排行榜
 * @author tzyx
 * @date 2018年7月19日 下午1:10:04
 */
public class GuildwarScoreRankScript implements IScript {
	private static Logger LOGGER = Logger.getLogger(GuildwarScoreRankScript.class);

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
		LOGGER.info("请求联盟战积分排行榜");
		GuildwarScoreRankRsp.Builder builder = GuildwarScoreRankRsp.newBuilder();
		GuildwarService service = GuildwarService.getInstance();
		Map<Long, GuildwarGuild> guildMap = service.getGuildMap();
		List<Long> guildList = new ArrayList<>(service.getGuildList());
		GuildService guildService = GuildService.getInstance();
		Long playerGuildId = guildService .getPlayerGuildId(player.getPlayerId());
		Guild guild;
		GuildwarGuildMsg.Builder b;
		GuildwarGuild guildwarGuild;
		RoleView chairman;

		builder.setOwnGuildRank(0);
		if (null != playerGuildId) {
			// 判断自己公会是否报名
			// 设置自己公会的信息
			guild = guildService.getGuilds().get(playerGuildId);
			b = GuildwarGuildMsg.newBuilder();
			if (guildMap.containsKey(playerGuildId)) {
				guildwarGuild = guildMap.get(playerGuildId);
				b.setTotalScore(guildwarGuild.getScore());
				builder.setOwnGuildRank(guildwarGuild.getScoreRank());
			} else {
				b.setTotalScore(0);
				builder.setOwnGuildRank(0);
			}
			b.setGuildId(guild.getId());
			b.setGuildName(guild.getName());
			b.setFlag(guild.getFlag());
			chairman = RoleViewService.getRoleById(guild.getChairman().getPlayerId());
			b.setChairmanName(chairman.getName());
			b.setGuildLvl(guild.getLevel());
			b.setTotalFight(guild.getFightPower());
			builder.setOwnGuild(b);
		}
		for (int i = 0; i < guildList.size(); i++) {
			Long guildId = guildList.get(i);
			guild = guildService.getGuilds().get(guildId);
			b = GuildwarGuildMsg.newBuilder();
			b.setGuildId(guild.getId());
			b.setGuildName(guild.getName());
			b.setFlag(guild.getFlag());
			chairman = RoleViewService.getRoleById(guild.getChairman().getPlayerId());
			b.setChairmanName(chairman.getName());
			b.setGuildLvl(guild.getLevel());
			b.setTotalFight(guild.getFightPower());
			guildwarGuild = guildMap.get(guildId);
			b.setTotalScore(guildwarGuild.getScore());
			if (guildwarGuild.getScore() < 1) {
				continue;
			}
			builder.addRankList(b);
		}
		
		MessageUtils.send(player.getSession(), player.getFactory().fetchSMessage(
				GuildwarScoreRankRspID.GuildwarScoreRankRspMsgID_VALUE, builder.build().toByteArray()));
		return null;
	}

}
