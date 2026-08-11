package logic.guildwar;

import org.apache.log4j.Logger;

import Message.S2CGuildwarMsg.GetGuildwarApplyListRsp;
import Message.S2CGuildwarMsg.GetGuildwarApplyListRspID;
import Message.S2CGuildwarMsg.GuildwarApplyMsg;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.guild.GuildService;
import game.server.logic.guild.bean.Guild;
import game.server.logic.guild.bean.GuildMember;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.guildwar.bean.GuildwarGuild;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: GetGuildwarApplyListScript
 * @Description: 获取联盟战公会报名信息
 * @author tzyx
 * @date 2018年7月19日 下午1:10:04
 */
public class GetGuildwarApplyListScript implements IScript {
	private static Logger LOGGER = Logger.getLogger(GetGuildwarApplyListScript.class);

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
		LOGGER.info("获取联盟战公会报名信息");
		long guildId = GuildService.getInstance().getPlayerGuildId(player.getPlayerId());
		GetGuildwarApplyListRsp.Builder builder = GetGuildwarApplyListRsp.newBuilder();
		GuildwarGuild guildwarGuild = GuildwarService.getInstance().getGuild(guildId);
		if (null == guildwarGuild) {
        	LOGGER.warn("联盟尚未参战");
        	MessageUtils.sendPrompt(player, PromptType.ERROR, 1264);
        	return null;
		}
		Guild playerGuild = GuildService.getInstance().getPlayerGuild(player.getPlayerId());
		long chairman = playerGuild.getChairman().getPlayerId();
		Player member;
		GuildwarApplyMsg.Builder apply;
		GuildMember guildMember;
		long id;
		for (int i = 0; i < playerGuild.getAllMembers().size(); i++) {
			id = playerGuild.getAllMembers().get(i).getPlayerId();
			apply = GuildwarApplyMsg.newBuilder();
			member = PlayerManager.getOffLinePlayerByPlayerId(id);
			guildMember = playerGuild.getAllMembers().get(i);
			apply.setApplied(guildwarGuild.getParter().contains(Long.valueOf(id)));
			apply.setImage(member.getImage());
			apply.setImageFrame(member.getImageFrame());
			apply.setImgBg(member.getImgBg());
			apply.setPlayerId(member.getPlayerId());
			apply.setPlayerName(member.getPlayerName());
			apply.setLevel(member.getLevel());
			apply.setVipLevel(member.getVipLevel());
			if (guildMember.getPlayerId() == chairman) {
				builder.setChairman(apply);
			} else {
				builder.addMembers(apply);
			}
		}
		MessageUtils.send(player.getSession(), player.getFactory().fetchSMessage(
				GetGuildwarApplyListRspID.GetGuildwarApplyListRspMsgID_VALUE, builder.build().toByteArray()));
		return null;
	}

}
