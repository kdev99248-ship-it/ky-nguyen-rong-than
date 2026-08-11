package logic.login;

import java.util.List;

import data.bean.t_leader_geneBean;
import data.bean.t_leader_moleculeBean;
import game.core.pub.script.IScript;
import game.server.logic.gene.GeneManager;
import game.server.logic.hero.bean.Hero;
import game.server.logic.hero.bean.HeroRelate;
import game.server.logic.hero.bean.HeroSkill;
import game.server.logic.player.Player;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
/**
 * 基因奖励BUG修复
 * (由于配置问题，前期部分老账号解锁基因技能未修改)
 * /由于英雄加载会重新检测技能，基因替换技能优先级最高，所以放在最后
 * @author liuwei
 * @date 2018年8月13日
 */
public class GeneRewardBugFixScript implements IScript {

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
		ScriptArgs script = (ScriptArgs) arg;
		Player player = (Player) script.get(ScriptArgs.Key.PLAYER);
		GeneManager geneManager = player.getGeneManager();
		Hero leader = player.getHeroManager().getLeader();
		List<t_leader_moleculeBean> genes = BeanTemplet.getLeaderMoleculeBeanList();
		for (t_leader_moleculeBean t_leader_moleculeBean : genes) {
			if (t_leader_moleculeBean.getId() > geneManager.getCurMolecule())
				continue;
			if(t_leader_moleculeBean.getFollow_id() == 0) {//分子最后一个，基因奖励
				int curGeneId = t_leader_moleculeBean.getId() / 100;
				// 发送基因奖励
				t_leader_geneBean geneBean = BeanTemplet.getLeaderGeneBean(curGeneId);
				if(geneBean.getReward_type() == 5) {
					String[] newSkills = geneBean.getReward().split(";");
					for (String skill : newSkills) {
						int skillType = Integer.parseInt(skill.split(",")[0]);
						int skillCid = Integer.parseInt(skill.split(",")[1]);
						HeroSkill heroSkill = null;
						if (skillType == 1) {
							heroSkill = leader.getSkillMap().get(0);
						} else if (skillType == 2) {
							heroSkill = leader.getSkillMap().get(1);
						}
						heroSkill.setId(skillCid);
					}
				}else if(geneBean.getReward_type() == 4) {
					String[] relates = geneBean.getReward().split(",");
					for (String relate : relates) {
						int relateId = Integer.parseInt(relate);
						HeroRelate heroRelate = leader.getRelateById(relateId);
						if (heroRelate == null) {
							continue;
						}
						heroRelate.setUnlock(true);
					}
				}
			}
		}
		return null;
	}

}
