package logic.crossIndigo;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Message.S2CPlayerMsg.PlayerType;
import data.bean.t_arena_aitempletBean;
import game.core.pub.script.IScript;
import game.server.db.game.bean.PlayerBean;
import game.server.db.game.dao.PlayerDao;
import game.server.logic.arena.RobotService;
import game.server.logic.crossIndigo.CrossIndigoService;
import game.server.logic.player.Player;
import game.server.logic.player.RoleViewService;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;

/**
 * 跨服石英大会机器人
 * 
 * @author liuwei
 * @date 2018年10月8日
 */
public class CrossIndigoRobotScript implements IScript {
	private static final Logger LOGGER = Logger.getLogger(CrossIndigoRobotScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {

		ScriptArgs args = (ScriptArgs) arg;

		int level = (int) args.get(ScriptArgs.Key.ARG1);
		int num = (int) args.get(ScriptArgs.Key.ARG2);

		List<PlayerBean> list = new ArrayList<>();
		t_arena_aitempletBean aitempletBean = BeanTemplet
				.getArenaAitempletBean(BeanTemplet.getGlobalBean(410).getInt_value());// 机器人模板ID
		RobotService.generateRobot(list, aitempletBean, PlayerType.CROSS_INDIGO_ROBOT_VALUE, num, level);
		// 批量保存
		PlayerDao.batchInsert(list);
		for (PlayerBean playerBean : list) {
			Player player = new Player();
			player.loadInitialize(null, playerBean);
			if (player.getPlayerId() == 0) {
				LOGGER.info("生成 =====跨服===石英决赛机器人------player :" + player.getPlayerId());
				continue;
			}
			RoleViewService.putRoleView(player.toRoleView());
			// 报名 跨服决赛

			// 决赛机器人报名
			CrossIndigoService.getInstance().applyRobot(player);

		}
		LOGGER.info("生成 =====跨服===石英决赛机器人完毕------数量:" + num);
		return null;
	}

}
