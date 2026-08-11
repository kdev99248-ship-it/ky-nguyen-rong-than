package logic.guildwar;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.S2CGuildwarMsg.GuildwarGuildMsg;
import Message.S2CGuildwarMsg.GuildwarHistoryRankRsp;
import Message.S2CGuildwarMsg.GuildwarHistoryRankRspID;
import game.core.pub.script.IScript;
import game.server.logic.guild.GuildService;
import game.server.logic.guild.bean.Guild;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.player.Player;
import game.server.logic.player.RoleView;
import game.server.logic.player.RoleViewService;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: GuildwarHistoryRankScript
 * @Description: 请求联盟战历史排行榜 
 * @author tzyx
 * @date 2018年7月19日 下午1:10:04
 */
public class GuildwarHistoryRankScript implements IScript {
	private static Logger LOGGER = Logger.getLogger(GuildwarHistoryRankScript.class);

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
		LOGGER.info("请求联盟战历史排行榜 ");
		GuildwarHistoryRankRsp.Builder builder = GuildwarHistoryRankRsp.newBuilder();
		GuildwarService service = GuildwarService.getInstance();
		GuildService guildService = GuildService.getInstance();
		Map<Long, Integer> history = service.getHistoryWinRanking();
		List<Long> rankingList = service.getHistoryWinRankingList();
		long lastWinnerGuildId = service.getLastWinnerGuildId();
		GuildwarGuildMsg.Builder b;
		Guild guild;
		// 是否存在上届冠军
		if (0 == lastWinnerGuildId) {
			builder.setStatus(1);
		} else {
			guild = guildService.getGuilds().get(lastWinnerGuildId);
			if (null != guild) {
				builder.setStatus(1);
				b = GuildwarGuildMsg.newBuilder();
				b.setGuildId(guild.getId());
				b.setGuildName(guild.getName());
				b.setFlag(guild.getFlag());
				RoleView chairman = RoleViewService.getRoleById(guild.getChairman().getPlayerId());
				b.setChairmanName(chairman.getName());
				b.setGuildLvl(guild.getLevel());
				b.setTotalFight(guild.getFightPower());
				builder.setChairmanLvl(chairman.getLevel());
				builder.setChairmanImage(chairman.getImage());
				builder.setChairmanImageFrame(chairman.getImageFrame());
				builder.setChairmanImgBg(chairman.getImgbg());
				builder.setChairmanFight(chairman.getMaxPower());
				builder.setLastWinner(b);
			}
		}
		// 设置自己公会的信息
		Long playerGuildId = guildService.getPlayerGuildId(player.getPlayerId());
		if (null != playerGuildId) {
			guild = guildService.getGuilds().get(playerGuildId);
			b = GuildwarGuildMsg.newBuilder();
			b.setGuildId(guild.getId());
			b.setGuildName(guild.getName());
			b.setFlag(guild.getFlag());
			RoleView chairman = RoleViewService.getRoleById(guild.getChairman().getPlayerId());
			b.setChairmanName(chairman.getName());
			b.setGuildLvl(guild.getLevel());
			b.setTotalFight(guild.getFightPower());
			builder.setOwnGuild(b);
			builder.setOwnGuildRank(0);
		}
		// 设置历史记录
		int k = 1;
		for (int i = 0; i < rankingList.size(); i++) {
			Long guildId = rankingList.get(i);
			guild = guildService.getGuilds().get(guildId);
			if (null != guild) {
				builder.setStatus(1);
				b = GuildwarGuildMsg.newBuilder();
				b.setGuildId(guild.getId());
				b.setGuildName(guild.getName());
				b.setFlag(guild.getFlag());
				RoleView chairman = RoleViewService.getRoleById(guild.getChairman().getPlayerId());
				b.setChairmanName(chairman.getName());
				b.setGuildLvl(guild.getLevel());
				b.setTotalFight(guild.getFightPower());
				b.setWinTimes(history.get(guildId));
				builder.addGuildList(b);
				if (null != playerGuildId && playerGuildId == guildId) {
					builder.setOwnGuildRank(k);
				}
				k++;
			}
		}
		MessageUtils.send(player.getSession(), player.getFactory().fetchSMessage(
				GuildwarHistoryRankRspID.GuildwarHistoryRankRspMsgID_VALUE, builder.build().toByteArray()));
		return null;
	}

}
