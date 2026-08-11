package logic.player;

import Message.S2CPlayerMsg.BindYMDeviceTokenRsp;
import Message.S2CPlayerMsg.BindYMDeviceTokenRspID;
import game.core.pub.script.IScript;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 绑定友盟消息推送服务对设备的唯一标识
 * 
 * @author liuwei
 * @date 2018年8月15日
 */
public class BindYMDeviceTokenScript implements IScript {

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
		ScriptArgs argsMap = (ScriptArgs) arg;
		Player player = (Player) argsMap.get(ScriptArgs.Key.PLAYER);
		String deviceToken = (String) argsMap.get(ScriptArgs.Key.ARG1);

		player.setDeviceToken(deviceToken);
		
		//推送
		BindYMDeviceTokenRsp.Builder builder = BindYMDeviceTokenRsp.newBuilder();
		builder.setDeviceToken(deviceToken);
		builder.setSuccess(true);
		MessageUtils.send(player, player.getFactory().fetchSMessage(BindYMDeviceTokenRspID.BindYMDeviceTokenRspMsgID_VALUE,
				builder.build().toByteArray()));
		
		return null;
	}

}
