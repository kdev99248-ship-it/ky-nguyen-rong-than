package logic.guildwar;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.S2CGuildwarMsg.GuildwarDayReportMsg.Builder;
import Message.S2CGuildwarMsg.OpenGuildwarRsp;
import Message.S2CGuildwarMsg.OpenGuildwarRspID;
import game.core.pub.script.IScript;
import game.server.logic.guild.GuildService;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.guildwar.bean.GuildwarGuild;
import game.server.logic.guildwar.bean.GuildwarPlayer;
import game.server.logic.player.Player;
import game.server.logic.player.RoleViewService;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: OpenGuildwarScript
 * @Description: 打开联盟战界面
 * @author tzyx
 * @date 2018年7月19日 下午1:10:32
 */
public class OpenGuildwarScript implements IScript {
	private static Logger LOGGER = Logger.getLogger(OpenGuildwarScript.class);

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
		GuildwarService service = GuildwarService.getInstance();
		if (!service.guildwarOpen(player))
			return null;
		LOGGER.info("请求打开联盟战界面");

		OpenGuildwarRsp.Builder builder = OpenGuildwarRsp.newBuilder();
		Long guildId = GuildService.getInstance().getPlayerGuildId(player.getPlayerId());
		int raceDay = service.getRaceDay();
		Map<Long, GuildwarGuild> guildMap = service.getGuildMap();
		LocalTime raceTime1 = service.getStartRaceTime();
		LocalTime raceTime2 = service.getEndRaceTime();
		boolean todayFinish = service.isTodayFinish();

		int startRaceTime = raceTime1.toSecondOfDay();
		int now = LocalTime.now().toSecondOfDay();
		// 公会参赛状态
		int guildApplyStatus = 0;

		switch (raceDay) {
		case 1: // 积分赛随便报名
			break;
		case 2: // 淘汰赛根据资格报名
			if (!service.getKnockoutGuild().contains(guildId))
				guildApplyStatus = -1;
			break;
		case 3: // 决赛赛根据资格报名
			if (!service.getFinalGuild().contains(guildId))
				guildApplyStatus = -1;
			break;
		default:
			break;
		}
		if (guildMap.containsKey(guildId)) {
			guildApplyStatus = 1;
		}
		builder.setGuildApplyStatus(guildApplyStatus);
		builder.setGuildwarType(raceDay);
		builder.setGuildwarStatus(service.getGuildwarStatus());
		if (guildApplyStatus == 1) {
			GuildwarGuild guildwarGuild = guildMap.get(guildId);
			builder.setTotalScore(guildwarGuild.getScore());
			if (guildwarGuild.getBigWinner() != 0) {
				builder.setBigWinner(RoleViewService.getRoleById(guildwarGuild.getBigWinner()).getName());
			}
			builder.setRank(guildwarGuild.getScoreRank());
			builder.setApplyNum(guildwarGuild.getParter().size());
			builder.setLastNum(guildwarGuild.getSurvivors().size());
			// 设置玩家获取的总积分和当日积分
			if (guildwarGuild.getAllScore().containsKey(player.getPlayerId())) {
				builder.setMyTotalScore(guildwarGuild.getAllScore().get(player.getPlayerId()));
			} else {
				builder.setMyTotalScore(0);
			}
			GuildwarPlayer appliedPlayer = service.getAppliedPlayer(player.getPlayerId());
			if (null != appliedPlayer) {
				builder.setMyTodayScore(appliedPlayer.getScore());
				builder.setWinTimes(appliedPlayer.getWinningStreak());
			} else {
				builder.setMyTodayScore(0);
			}
		} else {
			// 设置玩家获取的总积分和当日积分
			builder.setMyTodayScore(0);
			builder.setMyTotalScore(0);
		}
		builder.setTotalNum(GuildService.getInstance().getPlayerGuild(player.getPlayerId()).getCurMemberNum());
		String applyTime = service.getApplyAndBetTime1().toString() + "-"
				+ service.getApplyAndBetTime2().toString();
		builder.setApplyTime(applyTime);
		String raceTime = raceTime1.toString() + "-" + raceTime2.toString();
		builder.setRaceTime(raceTime);

		int nums = 0;
		for (int j = 0; j < service.getGuildList().size(); j++) {
			GuildwarGuild guildwarGuild = service.getGuildMap().get(service.getGuildList().get(j));
			if (guildwarGuild.getParter().size() > 0) {
				nums++;
				builder.addGuilds(service.genGuildwarGuild(guildwarGuild));
			}
			if (nums >=9) {
				break;
			}
		}
		if (guildApplyStatus != -1) {
			builder.setFinish(todayFinish);
			GuildwarGuild guildwarGuild = guildMap.get(guildId);
			if (null == guildwarGuild) {
				builder.setBetNum(0);
			} else {
				builder.setBetNum(guildwarGuild.getPunters().size());
			}
			// 处于开赛倒计时阶段
			if (now >= startRaceTime - service.getNextRaceCountdown() * 60 && now < startRaceTime) {
				List<Builder> l = service.genGuildwarDayReport(guildId, startRaceTime - now);
				for (int i = 0; i < l.size(); i++) {
					builder.addReports(l.get(i));
				}
			} else {
				if (null != guildwarGuild) {
					List<Builder> l = service.genGuildwarDayReport(guildId, (int) ((service.getNextRoundTime() - System.currentTimeMillis()) / 1000));
					for (int i = 0; i < l.size(); i++) {
						builder.addReports(l.get(i));
					}
				}
			}
		}
		builder.setApplied(service.getAppliedPlayers().containsKey(player.getPlayerId()));
		builder.setRaceCancel(service.isRaceCancel());
		MessageUtils.send(player, player.getFactory().fetchSMessage(OpenGuildwarRspID.OpenGuildwarRspMsgID_VALUE,
				builder.build().toByteArray()));
		return null;
	}

}
