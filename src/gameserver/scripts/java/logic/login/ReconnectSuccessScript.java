package logic.login;

import org.apache.mina.core.session.IoSession;

import Message.S2CLoginMsg.LoginResult;
import game.core.inter.ISession;
import game.core.mina.net.codec.ExternalProtocolDecoder;
import game.core.pub.script.IScript;
import game.server.logic.login.LoginService;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;

/**
 * 断线重连成功处理脚本
 * 
 * @author liujiang
 * @date 2017年10月18日 下午4:11:49
 *
 */
public class ReconnectSuccessScript implements IScript {
    @Override
    public void init() {}

    @Override
    public void destroy() {}

    @Override
    public Object call(String scriptName, Object arg) {
		ScriptArgs argsMap = (ScriptArgs) arg;
		Player player = (Player) argsMap.get(ScriptArgs.Key.PLAYER);
		ISession session = (ISession) argsMap.get(ScriptArgs.Key.ARG1);
		// 断线重连成功后移除时间戳验证属性，因为客户端会在断线重连后重置时间戳，期间可能会有误差，所以这里做一次清理。跟客户端核对过，其他消息只会在重连成功后发送
		if (!session.isNetty()) {
			ExternalProtocolDecoder.removeTimeVerifyAttribute((IoSession) session.getSession());
		}else{
			if(null != session.getSession()){
				session.removeAttribute("LAST_RECEIVED_TIME");
			}
		}
		if(null != session.getSession())
			LoginService.getInstance().resLogin(session, LoginResult.SUCCESS_VALUE, player.getToken().getValue());
		return null;
    }
    
    
}
