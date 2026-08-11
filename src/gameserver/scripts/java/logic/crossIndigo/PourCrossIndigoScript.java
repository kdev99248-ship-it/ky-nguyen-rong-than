package logic.crossIndigo;

import Message.Inner.GRCrossIndigo.GRPourIndigoReq;
import Message.Inner.InnerServer.ServerType;
import game.core.mina.message.SMessage;
import game.core.pub.script.IScript;
import game.server.cross.CrossServer;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;

public class PourCrossIndigoScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs args = (ScriptArgs) arg;
		Player player = (Player) args.get(ScriptArgs.Key.PLAYER);
		long pouredId = (long) args.get(ScriptArgs.Key.ARG1);
		int type = (int) args.get(ScriptArgs.Key.ARG2);
		// 转发
		GRPourIndigoReq.Builder builder = GRPourIndigoReq.newBuilder();
		builder.setPlayerId(player.getPlayerId());
		builder.setPouredId(pouredId);
		builder.setType(type);
		CrossServer.getInstance().send(ServerType.ROUTE_SERVER_VALUE,
				new SMessage(GRPourIndigoReq.MsgID.eMsgID_VALUE, builder.build().toByteArray()));
		return null;
	}

}
