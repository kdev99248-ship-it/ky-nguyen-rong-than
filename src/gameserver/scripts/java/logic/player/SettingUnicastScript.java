package logic.player;

import Message.S2CPlayerMsg.SettingUnicastRsp;
import Message.S2CPlayerMsg.SettingUnicastRspID;
import game.core.pub.script.IScript;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 设置是否接收友盟单播(unicast)
 * 
 * @author liuwei
 * @date 2018年8月15日
 */
public class SettingUnicastScript implements IScript {

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
		int unicast = (int) argsMap.get(ScriptArgs.Key.ARG1);
		player.setUnicast(unicast == 1 ? true : false);
		
		//推送
		SettingUnicastRsp.Builder builder = SettingUnicastRsp.newBuilder();
		builder.setUnicast(unicast);
		MessageUtils.send(player, player.getFactory().fetchSMessage(SettingUnicastRspID.SettingUnicastRspMsgID_VALUE,
				builder.build().toByteArray()));
		
		return null;
	}

}
