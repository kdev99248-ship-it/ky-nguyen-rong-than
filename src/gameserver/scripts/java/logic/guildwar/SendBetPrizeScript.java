package logic.guildwar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import data.bean.t_union_rankawardBean;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.guild.GuildService;
import game.server.logic.guild.bean.Guild;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.mail.MailService;
import game.server.logic.util.BeanTemplet;

/**
 * 
 * @ClassName: SendBetPrizeScript 
 * @Description: 发放联盟战结束后的助威奖励
 * @author tzyx 
 * @date 2018年7月23日 下午3:45:32
 */
public class SendBetPrizeScript implements IScript{
    private static Logger LOGGER = Logger.getLogger(SendBetPrizeScript.class);

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
		GuildwarService service = GuildwarService.getInstance();
		LOGGER.info("发放联盟战结束后的助威奖励");
		Long playerId;
		Long betGuild;
		List<String> content = new ArrayList<>();
		Guild win = GuildService.getInstance().getGuilds().get(service.getWinnerGuildId());
		String winName = null == win ? "公会已解散" : win.getName();
		Guild fail;
		String failName;
		t_union_rankawardBean t_union_rankawardBean = BeanTemplet.getAllGuildwarRankingPrize().get(0);
		String[] split = t_union_rankawardBean.getCheer_award().split(";");
		String[] spli;
		// 助威邮件模版没有附件,可以使用同一个附件map
		Map<Integer, Integer> adjunctMap = new HashMap<>();
		for (int i = 0; i < split.length; i++) {
			spli = split[i].split(",");
			adjunctMap.put(Integer.valueOf(spli[0]), Integer.valueOf(spli[1]));
		}
		for (Entry<Long, Long> en : service.getPunters().entrySet()) {
			playerId = en.getKey();
			betGuild = en.getValue();
			if (betGuild == service.getWinnerGuildId()) {
				content.clear();
				content.add(winName);
				MailService.getInstance().sendSysMail2Player(playerId, 38, content,
						Reason.GUILDWAR_BET_WIN, "", null, System.currentTimeMillis(), adjunctMap);
			} else {
				fail = GuildService.getInstance().getGuilds().get(betGuild);
				failName = null == fail ? "公会已解散" : fail.getName();
				content.clear();
				content.add(failName);
				content.add(winName);
				MailService.getInstance().sendSysMail2Player(playerId, 39, content,
						Reason.GUILDWAR_BET_FAIL, "", null, System.currentTimeMillis(), null);
			}
		}
		return null;
	}

}
