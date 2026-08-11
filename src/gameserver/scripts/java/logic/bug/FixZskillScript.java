package logic.bug;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Message.S2CPlayerMsg.PlayerType;
import data.bean.t_z_skillBean;
import game.core.pub.script.IScript;
import game.server.logic.arena.RobotService;
import game.server.logic.champion.ChampionManager;
import game.server.logic.comboSkill.bean.ComboSkill;
import game.server.logic.comboSkill.bean.UnlockCondType;
import game.server.logic.drawcard.DrawCardManager;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.player.RoleViewService;
import game.server.logic.section.SectionManager;
import game.server.logic.util.BeanTemplet;

/**
 * 修复充值异常脚本（主要是处理插入订单数据失败的情况）
 * 
 * @author liujiang
 * @date 2016-10-19
 */
public class FixZskillScript implements IScript {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {

		List<Long> l = RoleViewService.getAllPlayerId();
		for (Long pid : l) {
			if (null == pid)
				continue;
			if (RobotService.isRobot(pid))
				continue;
			Player player = PlayerManager.getOffLinePlayerByPlayerId(pid);
			if (null == player)
				continue;
			if (player.getPlayerType() != PlayerType.PLAYER_VALUE)
				continue;
			if (null != player) {
				// 检测解锁合体技
				checkUnlockSkill(player,UnlockCondType.TEAM_LEVEL, player.getLevel());					
				
				ChampionManager championManager = player.getChampionManager();
				// 检测解锁合体技
				checkUnlockSkill(player,UnlockCondType.CHAMPION, championManager.getMaxFloor());

				SectionManager sectionManager = player.getSectionManager();
				checkUnlockSkill(player,UnlockCondType.SECTION, sectionManager.getCurNormalSection());

				Map<Integer, ComboSkill> skillMap = player.getComboSkillManager().getSkillMap();
				for (ComboSkill skill : skillMap.values()) {
					skill.setLevel(player.getLevel());
				}
				DrawCardManager drawCardManager = player.getDrawCardManager();
				drawCardManager.setReward(new int[]{1,2,3});
				drawCardManager.setReceive(new boolean[]{false,false,false});
				player.offLineSave();
			}
		}
		return null;
	}

	public void checkUnlockSkill(Player player,UnlockCondType type, int value) {
		Map<Integer, ComboSkill> skillMap = player.getComboSkillManager().getSkillMap();
		List<t_z_skillBean> list = BeanTemplet.getAllComboSkillBean();
		ComboSkill skill = null;
		for (t_z_skillBean bean : list) {
			int id = bean.getId();
			skill = skillMap.get(id);
			if (skill != null) {
				continue;// 已解锁
			}
			String[] cond = StringUtils.split(bean.getCondition(), ",");
			int t = Integer.parseInt(cond[0]);
			if (t != type.value()) {
				continue;// 类型不匹配
			}
			int v = Integer.parseInt(cond[1]);
			if (v > value) {
				continue;// 条件不满足
			}
			skill = new ComboSkill();
			skill.setId(id);
			skill.setLevel(1);
			skillMap.put(id, skill);
		}
	}
}
