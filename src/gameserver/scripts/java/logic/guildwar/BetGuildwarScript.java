package logic.guildwar;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Message.C2SGuildwarMsg.BetGuildwarReqID;
import Message.S2CGuildwarMsg.BetGuildwarRsp;
import Message.S2CGuildwarMsg.BetGuildwarRspID;
import Message.S2CGuildwarMsg.GuildwarType;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.core.pub.script.ScriptManager;
import game.server.logic.constant.ItemType;
import game.server.logic.constant.Reason;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.guildwar.bean.GuildwarGuild;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: BetGuildwarScript 
 * @Description: 联盟战助威
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class BetGuildwarScript implements IScript {
     private static Logger LOGGER = Logger.getLogger(BetGuildwarScript.class);

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
        long betGuildId = (long) argsMap.get(ScriptArgs.Key.ARG1);
        // 活动是否可参与
        GuildwarService service = GuildwarService.getInstance();
		if (!service.guildwarOpen(player))
        	return null;
        long playerId = player.getPlayerId();
		GuildwarGuild guild = service.getGuild(betGuildId);
		if (service.getPunters().containsKey(playerId)) {
			LOGGER.warn("玩家已助威");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 1262);
			return null;
		}
		// 检查钻石是否足够
		List<Item> consumeList = BeanFactory.createProps(ItemType.DIAMOND.value(), service.getBetDiamondCost());
		if (!player.getBackpackManager().isItemNumEnough(consumeList)) {
			LOGGER.error("钻石不足!");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 332);
			return null;
		}
		// 扣除消耗
		player.getBackpackManager().removeItems(consumeList, false, Reason.GUILDWAR_BET, "");
		guild.betThisGuild(playerId);
		BetGuildwarRsp.Builder builder = BetGuildwarRsp.newBuilder();
		List<Long> guildList = service.getGuildList();
		for (int i = 0; i < guildList.size(); i++) {
			GuildwarGuild guildwarGuild = service.getGuildMap().get(guildList.get(i));
			if (guildwarGuild.getParter().size() > 0) {
				builder.addGuilds(service.genGuildwarGuild(guildwarGuild));
			}
		}
		builder.setGuildId(betGuildId);
		MessageUtils.send(player, player.getFactory().fetchSMessage(
				BetGuildwarRspID.BetGuildwarRspMsgID_VALUE, builder.build().toByteArray()));
		// 发放助威奖励邮件
		int type = service.getRaceDay() == GuildwarType.SCORE_VALUE ? 1 : 0;
		ScriptArgs args = new ScriptArgs();
		args.put(ScriptArgs.Key.PLAYER, player);
		args.put(ScriptArgs.Key.ARG1, type);
		ScriptManager.getInstance().call("logic.guildwar.SendJoinBetPrizeScript", args);

		// 推送给客户端刷新界面
		ScriptArgs args1 = new ScriptArgs();
		List<Long> l = new ArrayList<>();
		l.add(player.getPlayerId());
		args1.put(ScriptArgs.Key.ARG1, l);
		ScriptManager.getInstance().call("logic.guildwar.NotifyRaceCountdownScript", args1);
		// 记录动作    助威的公会id
		LogService.getInstance().logPlayerAction(player, BetGuildwarReqID.BetGuildwarReqMsgID_VALUE
				, betGuildId);
		return null;
	}

}
