package logic.guildwar;

import java.time.LocalTime;
import java.util.List;

import org.apache.log4j.Logger;

import Message.S2CGuildwarMsg.GuildwarDayReportMsg.Builder;
import Message.S2CGuildwarMsg.GuildwarRaceNotify;
import Message.S2CGuildwarMsg.GuildwarRaceNotifyID;
import game.core.pub.script.IScript;
import game.server.logic.guild.GuildService;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.guildwar.bean.GuildwarGuild;
import game.server.logic.guildwar.bean.GuildwarPlayer;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: NotifyRaceCountdownScript
 * @Description: 推送比赛倒计时
 * @author tzyx
 * @date 2018年7月30日 上午11:41:29
 */
public class NotifyRaceCountdownScript implements IScript {
	private static Logger LOGGER = Logger.getLogger(NotifyRaceCountdownScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		LOGGER.info("联盟战推送");
		GuildwarService service = GuildwarService.getInstance();
		if (!service.guildwarOpen())
			return null;
		ScriptArgs argsMap = (ScriptArgs) arg;
		@SuppressWarnings("unchecked")
		List<Long> notifyPlayers = (List<Long>) argsMap.get(ScriptArgs.Key.ARG1);
		argsMap.get(ScriptArgs.Key.ARG2);
		Player player;
		Long playerId;
		GuildwarPlayer appliedPlayer;
		GuildwarGuild guildwarGuild;
		GuildwarRaceNotify.Builder builder;
		GuildService serviceG = GuildService.getInstance();
		int startRaceTime = service.getStartRaceTime().toSecondOfDay();
		int raceDay = service.getRaceDay();
		int now = LocalTime.now().toSecondOfDay();
		for (int i = 0; i < notifyPlayers.size(); i++) {
			playerId = notifyPlayers.get(i);
			if (!PlayerManager.isPlayerOnline(playerId)) {
				continue;
			}
			player = PlayerManager.getOffLinePlayerByPlayerId(playerId);
			builder = GuildwarRaceNotify.newBuilder();
			guildwarGuild = service.getGuildByPlayerId(playerId);
			if (null == guildwarGuild) {
				if (raceDay == 1) {
					builder.setGuildApplyStatus(0);
				} else {
					builder.setGuildApplyStatus(-1);
				}
				builder.setTotalScore(0);
				builder.setBigWinner("");
				builder.setRank(0);
				builder.setWinTimes(0);
				builder.setApplyNum(0);
				builder.setLastNum(0);
				builder.setTotalNum(0);
				builder.setBetNum(0);
				builder.setApplied(false);
				builder.setMyTodayScore(0);
				builder.setMyTotalScore(0);
			} else {
				int guildApplyStatus = 1;
				switch (raceDay) {
				case 1: // 积分赛随便报名
					break;
				case 2: // 淘汰赛根据资格报名
					if (!service.getKnockoutGuild().contains(guildwarGuild.getGuildId()))
						guildApplyStatus = -1;
					break;
				case 3: // 决赛赛根据资格报名
					if (!service.getFinalGuild().contains(guildwarGuild.getGuildId()))
						guildApplyStatus = -1;
					break;
				default:
					break;
				}
				builder.setGuildApplyStatus(guildApplyStatus);
				builder.setTotalScore(guildwarGuild.getScore());
				GuildwarPlayer bigwin = service.getAppliedPlayer(guildwarGuild.getBigWinner());
				if (null == bigwin) {
					builder.setBigWinner("");
				} else {
					builder.setBigWinner(bigwin.getPlayerName());
				}
				builder.setRank(guildwarGuild.getScoreRank());
				appliedPlayer = service.getAppliedPlayer(playerId);
				if (null == appliedPlayer) {
					builder.setApplied(false);
					builder.setWinTimes(0);
					builder.setMyTodayScore(0);
				} else {
					builder.setApplied(true);
					builder.setWinTimes(appliedPlayer.getWinningStreak());
					builder.setMyTodayScore(appliedPlayer.getScore());
				}
				// 设置玩家获取的总积分
				if (guildwarGuild.getAllScore().containsKey(playerId)) {
					builder.setMyTotalScore(guildwarGuild.getAllScore().get(playerId));
				} else {
					builder.setMyTotalScore(0);
				}
				builder.setApplyNum(guildwarGuild.getParter().size());
				builder.setLastNum(guildwarGuild.getSurvivors().size());
				builder.setTotalNum(serviceG.getPlayerGuild(playerId).getAllMembers().size());
				builder.setBetNum(guildwarGuild.getPunters().size());

				// 处于开赛倒计时阶段
				if (now >= startRaceTime - service.getNextRaceCountdown() * 60 && now < startRaceTime) {
					List<Builder> l = service.genGuildwarDayReport(guildwarGuild.getGuildId(), startRaceTime - now);
					for (int j = 0; j < l.size(); j++) {
						builder.addReports(l.get(j));
					}
				} else {
					if (null != guildwarGuild) {
						List<Builder> l = service.genGuildwarDayReport(guildwarGuild.getGuildId(), (int) ((service.getNextRoundTime() - System.currentTimeMillis()) / 1000));
						for (int j = 0; j < l.size(); j++) {
							builder.addReports(l.get(j));
						}
					}
				}
			}
			int nums = 0;
			for (int j = 0; j < service.getGuildList().size(); j++) {
				GuildwarGuild guildwarGuild2 = service.getGuildMap().get(service.getGuildList().get(j));
				if (guildwarGuild2.getParter().size() > 0) {
					nums++;
					builder.addGuilds(service.genGuildwarGuild(guildwarGuild2));
				}
				if (nums >=9) {
					break;
				}
			}
			builder.setFinish(service.isTodayFinish());
			builder.setGuildwarType(raceDay);
			builder.setGuildwarStatus(service.getGuildwarStatus());
			builder.setRaceCancel(service.isRaceCancel());
			MessageUtils.send(player.getSession(), player.getFactory()
					.fetchSMessage(GuildwarRaceNotifyID.GuildwarRaceNotifyMsgID_VALUE, builder.build().toByteArray()));
		}
		return null;
	}

}
