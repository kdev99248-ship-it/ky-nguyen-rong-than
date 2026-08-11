package logic.guildwar;

import java.util.List;

import org.apache.log4j.Logger;

import Message.S2CGuildwarMsg.GetGuildwarKnockoutRsp;
import Message.S2CGuildwarMsg.GetGuildwarKnockoutRspID;
import Message.S2CGuildwarMsg.KnockoutGroup;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.guildwar.bean.GuildwarGroup;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: GetGuildwarKnockoutScript 
 * @Description: 获取巅峰对决消息
 * @author tzyx 
 * @date 2018年7月23日 下午3:45:32
 */
public class GetGuildwarKnockoutScript implements IScript{
    private static Logger LOGGER = Logger.getLogger(GetGuildwarKnockoutScript.class);

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
        ScriptArgs argsMap = (ScriptArgs) arg;
        Player player = (Player) argsMap.get(ScriptArgs.Key.PLAYER);
        GuildwarService service = GuildwarService.getInstance();
        LOGGER.info("请求巅峰对决消息");
		if (service.isRaceCancel()) {
			LOGGER.error("比赛已取消");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 1267);
			return null;
		}
		if (service.getKnockoutGuild().size() == 0) {
			LOGGER.error("巅峰对决未开启");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 1267);
			return null;
		}
		GetGuildwarKnockoutRsp.Builder builder = GetGuildwarKnockoutRsp.newBuilder();
		List<GuildwarGroup> raceGroup = service.getRaceGroup();
		GuildwarGroup guildwarGroup;
		KnockoutGroup.Builder kb;
		Long guildId;
		for (int i = 0; i < raceGroup.size(); i++) {
			guildwarGroup = raceGroup.get(i);
			kb = KnockoutGroup.newBuilder();
			kb.setKnockoutWinner(guildwarGroup.getKnockoutWinner());
			kb.setFinalWinner(guildwarGroup.getFinalWinner());
			for (int j = 0; j < guildwarGroup.getGuilds().size(); j++) {
				guildId = guildwarGroup.getGuilds().get(j);
				kb.addGuilds(service.genGuildwarGuild(guildId));
			}
			builder.addGroups(kb);
		}
		builder.setPlayEff(service.getEffedIds().containsKey(player.getPlayerId())); // 是否播放特效
		builder.setType(0); // 0:比赛未结束 1:淘汰赛结束 2:决赛结束
		MessageUtils.send(player, player.getFactory().fetchSMessage(
				GetGuildwarKnockoutRspID.GetGuildwarKnockoutRspMsgID_VALUE, builder.build().toByteArray()));
		return null;
	}

}
