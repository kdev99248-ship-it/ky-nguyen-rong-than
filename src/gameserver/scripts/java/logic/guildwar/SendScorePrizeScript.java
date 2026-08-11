package logic.guildwar;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import data.bean.t_union_dailyawardBean;
import game.core.pub.script.IScript;
import game.server.config.ServerConfig;
import game.server.db.log.bean.PlayerRankLogBean;
import game.server.logic.constant.RankTypes;
import game.server.logic.constant.Reason;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.guildwar.bean.GuildwarGuild;
import game.server.logic.log.LogService;
import game.server.logic.log.handler.LogPlayerRankHandler;
import game.server.logic.mail.MailService;
import game.server.logic.player.RoleView;
import game.server.logic.player.RoleViewService;
import game.server.logic.util.BeanTemplet;
import game.server.thread.BackLogProcessor;

/**
 * 
 * @ClassName: SendScorePrizeScript 
 * @Description: 发放参赛者的积分奖励
 * @author tzyx 
 * @date 2018年8月8日 上午12:50:30
 */
public class SendScorePrizeScript implements IScript{
    private static Logger LOGGER = Logger.getLogger(SendScorePrizeScript.class);

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
		LOGGER.info("开始发放参赛者的积分奖励");
		sendScorePrize();
		return null;
	}
	
	/** 发放参赛者的积分奖励 */
	private void sendScorePrize() {
		GuildwarService service = GuildwarService.getInstance();
		Map<Integer, Integer> adjunctMap;
		List<String> content = new ArrayList<>();
		Long playerId;
		// 发放所有参赛选手积分奖励
		StringBuilder sb;
		RoleView roleView;
		String strb;
		String date = LocalDate.now().toString();
		int winNum;
		List<PlayerRankLogBean> l = new ArrayList<>();
		for (int i = 0; i < service.getAppliedPlayerList().size(); i++) {
			playerId = service.getAppliedPlayerList().get(i);
			int score = service.getAppliedPlayer(playerId).getScore();
			adjunctMap = getPrizeByScord(score);
			sb = new StringBuilder();
			sb.append("发送参赛者:" + playerId + "的积分奖励邮件,该玩家获取了" + score + "积分,");
			if (!adjunctMap.isEmpty()) {
				sb.append("邮件奖励为:");
				for (Integer id : adjunctMap.keySet()) {
					sb.append(id).append("_").append(adjunctMap.get(id)).append("|");
				}
			}
			LOGGER.info(sb.substring(0, sb.length() - 1));
			content.clear();
			content.add(String.valueOf(score));
			MailService.getInstance().sendSysMail2Player(playerId, 36, content,
					Reason.GUILDWAR_SCORE_PRIZE, "", null, System.currentTimeMillis(), adjunctMap);
			//添加排行榜日志
			roleView = RoleViewService.getRoleById(playerId);
			if(roleView == null) {
				continue;
			}
			if(roleView.isRobot()) {
				continue;
			}
			winNum = service.getAppliedPlayer(playerId).getWinningStreak();
			//连胜次数 积分
			strb = "winNum:"+String.valueOf(winNum)+"_score:"+String.valueOf(score);
			l.add(LogService.getInstance().logPlayeRank(RankTypes.GUILDWAR_DAY_SCORE_LOG.value(), date,playerId,score,roleView.getName(),ServerConfig.getInstance().getServerId(),0,strb));
		}
		if (!l.isEmpty()) {
			BackLogProcessor.getInstance().addCommand(new LogPlayerRankHandler(l));
		}
		service.setSendReward(true);
		// 清空所有公会的投注信息
		service.getPunters().clear();
		for (GuildwarGuild guild : service.getGuildMap().values()) {
			guild.getPunters().clear();
		}
	}
	/** 根据积分获取对应的积分奖励 */
	private Map<Integer, Integer> getPrizeByScord(int score) {
		Map<Integer, Integer> map = new HashMap<>();
		List<t_union_dailyawardBean> scorePrize = BeanTemplet.getAllGuildwarScorePrize();
		t_union_dailyawardBean bean = null;
		if (score == 0) {
			return map;
		}
		String[] split;
		String[] sp;
		for (int i = 0; i < scorePrize.size(); i++) {
			bean = scorePrize.get(i);
			// 是所需积分 终止循环
			if (bean.getPoints() == score) {
				break;
			}
		}
		// 没有所需积分,取最后一个
		if (null != bean) {
			split = bean.getPoint_award().split(";");
			for (int j = 0; j < split.length; j++) {
				sp = split[j].split(",");
				map.put(Integer.valueOf(sp[0]), Integer.valueOf(sp[1]));
			}
		}
		return map;
	}
}
