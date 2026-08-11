package logic;

import org.apache.log4j.Logger;

import game.core.pub.script.IScript;
import game.core.pub.timer.SimpleTimerProcessor;
import game.core.pub.util.ExceptionEx;
import game.server.NettyGameServer;
import game.server.http.mina.GameHttpServerImpl;
import game.server.logic.activity.ActivityService;
import game.server.logic.crossIndigo.CrossIndigoService;
import game.server.logic.global.GameGlobalService;
import game.server.logic.guild.GuildService;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.herodispatch.HerodispatchService;
import game.server.logic.indigo.IndigoService;
import game.server.logic.line.GameLineManager;
import game.server.logic.mail.MailService;
import game.server.logic.operateActivity.OperateActivityService;
import game.server.logic.server.ServerStatusService;
import game.server.logic.snatchTerritory.SnatchTerritoryService;
import game.server.logic.teamHunt.TeamHuntService;
import game.server.thread.BackLogProcessor;
import game.server.thread.DispatchProcessor;
import game.server.thread.LogicProcessor;
import game.server.thread.LogicRestoreProcessor;
import game.server.thread.PlayerRestoreProcessor;
import game.server.thread.RechargeProcessor;
import game.server.thread.delay.DelayTaskProcessor;

/**
 *
 * @author liujiang
 * @date 2017-10-17
 */
public class ShutdownHookScript implements IScript, Runnable {
    private static final Logger logger = Logger.getLogger(ShutdownHookScript.class);

    @Override
    public void init() {}

    @Override
    public void destroy() {}

    @Override
    public Object call(String scriptName, Object arg) {
        if ("stop".equals(arg)) {
            run();// stop 表示外部在调用停服钩子了
        } else if ("reset".equals(arg)) {// 此处是方便解决未正常停服时，可以重新再启动停服钩子
            NettyGameServer.getInstance().removeShutdownHook();// 移除之前的hook
            NettyGameServer.getInstance().addShutdownHook(new Thread(this));// 添加新的hook
        }
        return null;
    }

    @Override
    public void run() {
        logger.info("ShutdownHookScript---停服脚本-----begin-------------");
        logger.info("JVM exit, call GameServer.stop");
        NettyGameServer.getInstance().setStopping(true);
        // 先改变服务器状态为维护状态并踢玩家下线,防止玩家再次登录
        ServerStatusService.getInstance().updateServerStatus(ServerStatusService.MAINTAIN, true);
        try {
            long begin = System.currentTimeMillis();
            logger.info("JVM exit, call GameServer.stop  threadSleepStart,now:" + begin);
            {
                // 停止所有的定时器
                logger.info("关闭timer!");
                SimpleTimerProcessor.getInstance().stop();
                // http服务器停止
                logger.info("stop and await GameHttpServerImpl...");
                GameHttpServerImpl.getInstance().stop(2);
                // 停止消息分发线程
                logger.info("stop and await DispatchProcessor...");
                DispatchProcessor.getInstance().stopAndAwaitStop(false);
                
                // 公会线程
                logger.info("GuildService save...");
                GuildService.getInstance().stopAndAwaitStop();
                
                // 精灵派遣
                logger.info("HerodispatchService save...");
                HerodispatchService.getInstance().stopAndAwaitStop();
                
                // 组队线程
                logger.info("TeamHuntService save...");
                TeamHuntService.getInstance().stopAndAwaitStop();
                // 石英大会线程
                logger.info("IndigoService save...");
                IndigoService.getInstance().stopAndAwaitStop();
                // 石英大会线程
                logger.info("CrossIndigoService save...");
                CrossIndigoService.getInstance().stopAndAwaitStop();
                //道馆数据回存
                logger.info("SnatchTerritoryService save...");
                SnatchTerritoryService.getInstance().stopAndAwaitStop();
                // 运营活动线程
                logger.info("OperateActivityService save...");
                OperateActivityService.getInstance().stopAndAwaitStop();
                // 活动线程
                logger.info("ActivityService save...");
                ActivityService.getInstance().stopAndAwaitStop();
                // 联盟战活动线程
                logger.info("GuildwarService save...");
                GuildwarService.getInstance().stopAndAwaitStop();
                // 充值线程
                logger.info("stop and await RechargeProcessor...");
                RechargeProcessor.getInstance().stopAndAwaitStop(false);
                // 逻辑线程
                logger.info("stop and await LogicProcessor...");
                LogicProcessor.getInstance().stopAndAwaitStop();
                // 全局配置回存
                logger.info("GameGlobalService save...");
                GameGlobalService.getInstance().save();
                // 停止GameLine线程
                logger.info("stop and await GameLineManager...");
                GameLineManager.getInstance().stopAndAwaitStop();
                //停止全网通知,由于是通知，这里可以立即停止
                DelayTaskProcessor.getInstance().stop(true);
                logger.info("stop immediately DelayTaskProcessor...");
            }
        } catch (Exception ex) {
            logger.error(ExceptionEx.e2s(ex));
        } finally {
            // 关闭邮件线程
            MailService.getInstance().stopAndAwaitStop();
            // 关闭逻辑回存线程
            logger.info("stop and await LogicCacheProcessor ...");
            LogicRestoreProcessor.getInstance().stopAndAwaitStop();
            // 关闭玩家数据回存线程
            logger.info("stop and await PlayerRestoreProcessor ...");
            PlayerRestoreProcessor.getInstance().stopAndAwaitStop();
        }

        { // 注意：日志处理线程需要最后关闭
            logger.info("stop and await BackLogProcessor...");
            BackLogProcessor.getInstance().stopAndAwaitStop(false);
        }
        // 运维的脚本逻辑有依赖这个打印,严禁修改
        logger.info("关闭服务器!");
        logger.info("ShutdownHookScript---------end------------");
        return;
    }
}
