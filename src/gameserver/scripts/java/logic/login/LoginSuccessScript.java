package logic.login;

import game.core.pub.script.IScript;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.thread.delay.DelayTaskProcessor;

/**
 * 登录成功预留处理脚本,方便修复各种线上bug
 */
public class LoginSuccessScript implements IScript {
    // private static final Logger logger = Logger.getLogger(LoginSuccessScript.class);

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Object call(String scriptName, Object arg) {
         ScriptArgs argsMap = (ScriptArgs) arg;
         Player player = (Player)argsMap.get(ScriptArgs.Key.PLAYER);
         //删除体力回复满的离线通知
         DelayTaskProcessor.getInstance().removeCommand(player.getPlayerId()+"");
         
         return null;
    }
}
