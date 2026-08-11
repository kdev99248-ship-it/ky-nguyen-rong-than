package logic.guildwar;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.S2CGuildwarMsg.GuildwarType;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.mail.MailService;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;

/**
 * 
 * @ClassName: SendJoinBetPrizeScript 
 * @Description: 发放助威参与奖 
 * @author tzyx 
 * @date 2018年8月8日 上午12:37:28
 */
public class SendJoinBetPrizeScript implements IScript{
    private static Logger LOGGER = Logger.getLogger(SendJoinBetPrizeScript.class);

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
		LOGGER.info("发放助威参与奖");
        ScriptArgs argsMap = (ScriptArgs) arg;
        Player player = (Player) argsMap.get(ScriptArgs.Key.PLAYER);
        int type = (int) argsMap.get(ScriptArgs.Key.ARG1);
        GuildwarService service = GuildwarService.getInstance();

		Map<Integer, Integer> adjunctMap1 = new HashMap<>();
		// 积分赛助威奖励
		for (int i = 0; i < service.getReward1().size(); i++) {
			adjunctMap1.put(service.getReward1().get(i).getId(), service.getReward1().get(i).getNum());
		}
		Map<Integer, Integer> adjunctMap2 = new HashMap<>();
		// 淘汰赛决赛助威奖励
		for (int i = 0; i < service.getReward2().size(); i++) {
			adjunctMap2.put(service.getReward2().get(i).getId(), service.getReward2().get(i).getNum());
		}
		Reason reason;
		if (type == GuildwarType.SCORE_VALUE) {
			reason = Reason.GUILDWAR_BET_SCORE_PRIZE;
			MailService.getInstance().sendSysMail2Player(player.getPlayerId(), 40, null,
					reason, "", null, System.currentTimeMillis(), adjunctMap1);
		} else {
			reason = Reason.GUILDWAR_BET_FINAL_PRIZE;
			MailService.getInstance().sendSysMail2Player(player.getPlayerId(), 40, null,
					reason, "", null, System.currentTimeMillis(), adjunctMap2);
		}
		return null;
	}

}
