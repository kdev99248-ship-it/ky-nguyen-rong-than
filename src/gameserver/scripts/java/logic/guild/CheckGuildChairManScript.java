package logic.guild;

import game.core.pub.script.IScript;
import game.server.logic.constant.ModelMsg;
import game.server.logic.guild.bean.Guild;
import game.server.logic.guild.bean.GuildMember;
import game.server.logic.guild.bean.Position;
import game.server.logic.guild.bean.history.GuildHistory;
import game.server.logic.player.RoleView;
import game.server.logic.player.RoleViewService;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;

/**
 * 检测联盟会长登录情况
 * 
 * 长期不登录会禅让会长位置
 * 
 * @author liuwei
 * @date 2018年10月10日
 */
public class CheckGuildChairManScript implements IScript {

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
		ScriptArgs args = (ScriptArgs) arg;
		Guild guild = (Guild) args.get(Key.ARG1);

		long oldChairManId = guild.getChairman().getPlayerId();
		RoleView roleView = RoleViewService.getRoleById(oldChairManId);
		int day = BeanTemplet.getGlobalBean(311).getInt_value();// 多少天未上线系统尝试禅让盟主
		if (System.currentTimeMillis() - roleView.getLastLoginTime() > day * 24 * 60 * 60 * 1000L) {//
			// 尝试禅让盟主
			GuildMember oldMember = guild.getMemberByPlayerId(oldChairManId);
			GuildMember member = guild.getAllMembers().stream().filter(m -> checkLoginTime(m, oldChairManId))
					.sorted((m, n) -> m.compareTo(n)).findFirst().orElse(null);
			if (member != null) {
				// 把新盟主移除旧集合
				if (member.getPosition() == Position.VICE_CHAIRMAN.value()) {
					guild.getViceChairmen().remove(member);
				} else {
					guild.getNormalMembers().remove(member);
				}
				// 设置新盟主
				member.setPosition(Position.CHAIRMAN.value());
				guild.setChairman(member);
				// 禅让的盟主设置为普通成员
				oldMember.setPosition(Position.NORMAL.value());
				guild.getNormalMembers().add(oldMember);
				// 生成联盟日志
				RoleView newRoleView = RoleViewService.getRoleById(member.getPlayerId());
				long leftDay = (System.currentTimeMillis() - roleView.getLastLoginTime()) / (24 * 60 * 60 * 1000L);
				GuildHistory his = new GuildHistory(ModelMsg.GUILD_CHAIRMAN_CHANGE.value(), roleView.getName(),
						leftDay + "", newRoleView.getName());
				guild.addGuildHistory(his);
			}
		}

		return null;
	}

	/**
	 * 判断上线情况
	 * 
	 * @param guildMember
	 * @param oldChairManId
	 * @return
	 */
	private boolean checkLoginTime(GuildMember guildMember, long oldChairManId) {
		if (guildMember.getPlayerId() == oldChairManId) {
			return false;
		}
		RoleView roleView = RoleViewService.getRoleById(guildMember.getPlayerId());
		int day = BeanTemplet.getGlobalBean(312).getInt_value();// 未上线天数限制
		if (System.currentTimeMillis() - roleView.getLastLoginTime() > day * 24 * 60 * 60 * 1000L) {
			return false;
		} else {
			return true;
		}
	}
}
