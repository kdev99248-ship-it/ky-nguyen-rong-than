package logic.login;

import game.core.pub.script.IScript;
import game.server.db.game.bean.PlayerBean;
import game.server.info.RoleInfoReportManager;
import game.server.logic.constant.TaskConditionType;
import game.server.logic.line.handler.PlayerUpdateBean;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.player.RoleViewService;
import game.server.logic.util.ScriptArgs;
import game.server.thread.PlayerRestoreProcessor;
import game.server.thread.dboperator.handler.ReqUpdatePlayerHandler;
import game.server.thread.delay.DelayTaskProcessor;

/**
 * 用户断开连接的逻辑处理
 * 
 * @author liujiang
 * @date 2017年2月21日 下午5:41:13
 *
 */
public class ConnectionCloseScript implements IScript {
    // private static Logger LOG = Logger.getLogger(ConnectionCloseScript.class);

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
        player.setSession(null);
        player.setDetachState();
        player.setSyMinuteTime(0);
        long now = System.currentTimeMillis();
        long onlineTime = (now - player.getLastLoginTime()) / 1000;// 本次在线时长 ，秒
        player.setOnlineTimeSum(player.getOnlineTimeSum() + onlineTime);
		// 更新任务
		player.getTaskManager().updateTaskCondition(TaskConditionType.online_time, (int)(player.getOnlineTimeSum()/60));
		//
        player.setOfflineTime(now);
        RoleViewService.putRoleView(player.toRoleView());
        PlayerBean playerBean = player.toPlayerBean();
        player.setDirtyKey(playerBean);
        // 离线回存
        PlayerRestoreProcessor.getInstance().submitRequest(
                new ReqUpdatePlayerHandler(player.getLineId(), new PlayerUpdateBean(player
                        .toAccountBean(), playerBean, true)));
        // 邮件回存
        player.getMailManager().save();
        // 角色数据上报给登录中心服
        RoleInfoReportManager.getDefault().addPlayerInfo(player);
        //日志记录
        LogService.getInstance().logPlayerLogout(player);
        player.getMiniGameManager().logOutCheck();
        //提供体力回复满提醒通知
        DelayTaskProcessor.getInstance().offlinePhyFullNotify(player);
        return null;
    }

}
