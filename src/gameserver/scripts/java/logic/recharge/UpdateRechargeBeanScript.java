package logic.recharge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.core.pub.command.Handler;
import game.core.pub.script.IScript;
import game.core.pub.script.ScriptManager;
import game.server.db.game.bean.RechargeBean;
import game.server.db.game.dao.RechargeDao;
import game.server.logic.line.GameLine;
import game.server.logic.line.GameLineManager;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.util.ScriptArgs;
import game.server.thread.RechargeProcessor;

/** 充值 */
public class UpdateRechargeBeanScript implements IScript {

	private Logger logger = LoggerFactory.getLogger(UpdateRechargeBeanScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs parm = (ScriptArgs) arg;
		RechargeBean bean = (RechargeBean) parm.get(ScriptArgs.Key.ARG1);
		if (0 == RechargeDao.updateSingleOne(bean)) {
			logger.error("充值状态修改失败: " + bean.getOrderId() + "当前状态：" + bean.getState());
		}
		boolean offlineRecharge = false;
		Player player = PlayerManager.getPlayerByPlayerId(bean.getPlayerId());
		if (null != player) {
			GameLine line = GameLineManager.getInstance().getLine(player.getLineId());
			if (null == line) {
				// 什么情况下会出现角色不能分线?
				logger.error("角色[" + player.getPlayerName() + "]未分线，离线充值: " + bean.getOrderId());
				offlineRecharge = true;
			} else {
				offlineRecharge = !rechargeFinish(player, bean, false);
			}
		} else {
			logger.error("角色不在线，离线充值: " + bean.getOrderId());
			offlineRecharge = true;
		}
		if (offlineRecharge) {
			offlineRecharge(bean);
		}
		return null;
	}

	/**
	 * 加入离线充值订单列表
	 * 
	 * @param bean
	 */
	private void offlineRecharge(RechargeBean bean) {

		// 存数据库
		RechargeProcessor.getInstance().addCommand(new Handler() {
			@Override
			public void action() {

				RechargeProcessor.getInstance().reqOfflineRecharge(bean);
			}
		});
	}

	/** 充值完成准备发放道具流程 */
	private boolean rechargeFinish(Player player, RechargeBean bean, boolean offline) {
		if (null == player) {
			logger.error(" [ 充值成功信息，玩家不在线，将加入到离线充值列表！     订单号: " + bean.getOrderId() + "]");
			return false;
		} else {
			ScriptArgs args = new ScriptArgs();
			args.put(ScriptArgs.Key.PLAYER, player);
			args.put(ScriptArgs.Key.ARG1, bean);
			args.put(ScriptArgs.Key.ARG2, offline);
			ScriptManager.getInstance().call("logic.recharge.RechargeFinishScript", args);
			logger.error(
					" [ 充值成功信息，继续脚本发放道具流程！     订单号: " + bean.getOrderId() + " ， 玩家 : " + player.getPlayerId() + " ]");
			return true;
		}
	}

}
