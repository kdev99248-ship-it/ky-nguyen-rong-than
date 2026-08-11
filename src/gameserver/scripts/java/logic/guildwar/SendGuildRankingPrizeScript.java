package logic.guildwar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import data.bean.t_union_rankawardBean;
import game.core.pub.script.IScript;
import game.server.config.ServerConfig;
import game.server.db.log.bean.PlayerRankLogBean;
import game.server.logic.constant.RankTypes;
import game.server.logic.constant.Reason;
import game.server.logic.guild.GuildService;
import game.server.logic.guild.bean.GuildMember;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.guildwar.bean.GuildwarGuild;
import game.server.logic.log.LogService;
import game.server.logic.log.handler.LogPlayerRankHandler;
import game.server.logic.mail.MailService;
import game.server.logic.player.RoleView;
import game.server.logic.player.RoleViewService;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.thread.BackLogProcessor;

/**
 * 
 * @ClassName: SendGuildRankingPrizeScript 
 * @Description: 发放联盟战结束后的公会排名奖励奖励
 * @author tzyx 
 * @date 2018年8月8日 上午12:46:19
 */
public class SendGuildRankingPrizeScript implements IScript{
    private static Logger LOGGER = Logger.getLogger(SendGuildRankingPrizeScript.class);

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
		LOGGER.info("发放联盟战结束后的公会排名奖励奖励");
        ScriptArgs argsMap = (ScriptArgs) arg;
        @SuppressWarnings("unchecked")
		List<Long> l = (List<Long>) argsMap.get(ScriptArgs.Key.ARG1);
        GuildwarService service = GuildwarService.getInstance();
        int mailId = (int) argsMap.get(ScriptArgs.Key.ARG2);
        int index = (int) argsMap.get(ScriptArgs.Key.ARG3);
        if (index != 0) {
    		GuildwarGuild guildwarGuild;
    		int rank;
        	for (int i = 0; i < l.size(); i++) {
				guildwarGuild = service.getGuild(l.get(i));
				rank = guildwarGuild.getFinalRank();
				// 没有积分,跳过
				if (rank == 0) {
					continue;
				}
				t_union_rankawardBean config = getRankConfig(rank);
        		sendPrize(config, rank, mailId, guildwarGuild.getGuildId());
			}
        } else {
        	sendGuildRankingPrize(l, mailId);
        }
		return null;
	}
	
	private t_union_rankawardBean getRankConfig(int rank) {
		List<t_union_rankawardBean> allGuildwarRankingPrize = BeanTemplet.getAllGuildwarRankingPrize();
		for (int i = 0; i < allGuildwarRankingPrize.size(); i++) {
			t_union_rankawardBean t_union_rankawardBean = allGuildwarRankingPrize.get(i);
			if (t_union_rankawardBean.getHigh_rank() <= rank
					&& t_union_rankawardBean.getLow_rank() >= rank) {
				return t_union_rankawardBean;
			}
		}
		return null;
	}
	
	/** 发放联盟战结束后的公会排名奖励奖励 */
	private void sendGuildRankingPrize(List<Long> l, int mailId) {
		t_union_rankawardBean config;
		GuildwarGuild guildwarGuild;
		Long guildId = null;
		int rank = 0;
		GuildwarService service = GuildwarService.getInstance();
		for (int i = 0; i < BeanTemplet.getAllGuildwarRankingPrize().size(); i++) {
			config = BeanTemplet.getAllGuildwarRankingPrize().get(i);
			// 遍历参赛联盟
			for (int j = 0; j < l.size(); j++) {
				guildwarGuild = service.getGuild(l.get(j));
				rank = guildwarGuild.getFinalRank();
				// 没有积分,移除
				if (rank == 0) {
					guildId = l.get(j);
					break;
				}
				// 积分符合配置,发送邮件
				if (rank >= config.getLow_rank() && rank <= config.getHigh_rank()) {
					guildId = l.get(j);
					sendPrize(config, rank, mailId, guildId);
					break;
				}
			}
			if (null != guildId) {
				l.remove(guildId);
				guildId = null;
			}
		}
	}
	private void sendPrize(t_union_rankawardBean config, int rank, int mailId, long guildId) {
		List<String> content;
		Map<Integer, Integer> adjunctMap;
		List<GuildMember> allMembers;
		String[] split;
		String[] spli;
		RoleView roleView;
		String strb;
		allMembers = GuildService.getInstance().getGuilds().get(guildId).getAllMembers();
		adjunctMap = new HashMap<>();
		split = config.getRank_award().split(";");
		for (int k = 0; k < split.length; k++) {
			spli = split[k].split(",");
			adjunctMap.put(Integer.valueOf(spli[0]), Integer.valueOf(spli[1]));
		}
		content = new ArrayList<>();
		content.add(String.valueOf(rank));
		String date = LocalDate.now().toString();
		long fightPower = GuildService.getInstance().getGuilds().get(guildId).getFightPower();
		List<PlayerRankLogBean> l = new ArrayList<>();
		for (int k = 0; k < allMembers.size(); k++) {
			MailService.getInstance().sendSysMail2Player(allMembers.get(k).getPlayerId(), mailId, content,
					Reason.GUILDWAR_RANK_PRIZE, "", null, System.currentTimeMillis(), adjunctMap);
			//添加排行榜日志
			roleView = RoleViewService.getRoleById(allMembers.get(k).getPlayerId());
			if(roleView == null) {
				continue;
			}
			if(roleView.isRobot()) {
				continue;
			}
			strb = "fightPower:"+String.valueOf(fightPower);
			l.add(LogService.getInstance().logPlayeRank(RankTypes.GUILDWAR_RANK_LOG.value(), date, roleView.getPlayerId(),rank,roleView.getName(),ServerConfig.getInstance().getServerId(),0,strb));
		}
		if (!l.isEmpty()) {
			BackLogProcessor.getInstance().addCommand(new LogPlayerRankHandler(l));
		}
	}
}
