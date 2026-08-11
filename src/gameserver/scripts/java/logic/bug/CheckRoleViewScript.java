package logic.bug;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Message.S2CPlayerMsg.PlayerType;
import game.core.pub.script.IScript;
import game.server.logic.arena.RobotService;
import game.server.logic.guild.GuildService;
import game.server.logic.guild.bean.Guild;
import game.server.logic.guild.bean.GuildMember;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.player.RoleViewService;

public class CheckRoleViewScript implements IScript {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {

		List<Long> list = RoleViewService.getAllPlayerId();
		for (Long id : list) {
			if (null == id)
				continue;
			if (RobotService.isRobot(id))
				continue;
			Player player = PlayerManager.getOffLinePlayerByPlayerId(id);
			if (null == player)
				continue;
			if (player.getPlayerType() != PlayerType.PLAYER_VALUE)
				continue;
			boolean save = false;
			List<Long> friends = player.getFriendManager().getFriendList();
			Iterator<Long> iterator = friends.iterator();
			while (iterator.hasNext()) {
				Long fid = iterator.next();
				if (null == RoleViewService.getRoleById(fid)) {
					iterator.remove();
					save = true;
				}
			}
			if (save) {
				logger.info("player : [ " + player.getPlayerName() + " , id : " + player.getPlayerId() + " ] 删除好友！");
				player.offLineSave();
			}
		}

		Map<Long, Guild> guilds = GuildService.getInstance().getGuilds();
		for (Guild guild : guilds.values()) {
			if (null == guild)
				continue;
			Iterator<GuildMember> iterator = guild.getNormalMembers().iterator();
			while (iterator.hasNext()) {
				GuildMember member = iterator.next();
				if (null == RoleViewService.getRoleById(member.getPlayerId())) {
					iterator.remove();
					logger.info("guild : [ " + guild.getName() + " , id : " + guild.getId() + " ] 删除成员: "
							+ member.getPlayerId());
				}
			}
			iterator = guild.getViceChairmen().iterator();
			while (iterator.hasNext()) {
				GuildMember member = iterator.next();
				if (null == RoleViewService.getRoleById(member.getPlayerId())) {
					iterator.remove();
					logger.info("guild : [ " + guild.getName() + " , id : " + guild.getId() + " ] 删除成员: "
							+ member.getPlayerId());
				}
			}
			if (null == RoleViewService.getRoleById(guild.getChairman().getPlayerId())) {
				if (guild.getViceChairmen().size() > 0) {
					guild.setChairman(guild.getViceChairmen().get(0));
					guild.getViceChairmen().remove(0);
					logger.info("guild : [ " + guild.getName() + " , id : " + guild.getId() + " ] 会长副会长顶替: "
							+ guild.getChairman().getPlayerId());
				} else if (guild.getNormalMembers().size() > 0) {
					guild.setChairman(guild.getNormalMembers().get(0));
					guild.getNormalMembers().remove(0);
					logger.info("guild : [ " + guild.getName() + " , id : " + guild.getId() + " ] 会长普通成员顶替: "
							+ guild.getChairman().getPlayerId());
				}else{
					GuildService.getInstance().dissolveGuild(guild);
					logger.info("guild : [ " + guild.getName() + " , id : " + guild.getId() + " ] 无成员  解散 ");
				}
			}
		}
		GuildService.getInstance().saveAll();
		logger.info("------ 处理完成！ ");
		return null;
	}

}
