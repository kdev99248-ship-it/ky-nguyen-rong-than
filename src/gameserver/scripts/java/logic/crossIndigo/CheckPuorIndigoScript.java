package logic.crossIndigo;

import java.util.ArrayList;
import java.util.List;

import Message.S2CBackpackMsg.PropInfo;
import Message.S2CPlayerMsg.PromptType;
import Message.Inner.GRCrossIndigo.GRPourIndigoRsp;
import Message.Inner.GRCrossIndigo.GRPourSuccessIndigoReq;
import Message.Inner.InnerServer.ServerType;
import game.core.mina.message.SMessage;
import game.core.pub.script.IScript;
import game.server.cross.CrossServer;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

public class CheckPuorIndigoScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs args = (ScriptArgs) arg;
		GRPourIndigoRsp grPour = (GRPourIndigoRsp) args.get(ScriptArgs.Key.ARG1);
		String orderId = grPour.getPourOrderId();
		long playerId = grPour.getPlayerId();
		long pourId = grPour.getPouredId();
		int type = grPour.getType();

		Player player = PlayerManager.getPlayerByPlayerId(playerId);
		if (null == player)
			return null;
		List<Item> items = new ArrayList<>();
		List<PropInfo> prop = grPour.getItemsList();
		for (int i = 0; i < prop.size(); i++) {
			List<Item> item = BeanFactory.createProps(prop.get(i).getId(), prop.get(i).getNum());
			items.addAll(item);
		}
		if (!player.getBackpackManager().isItemNumEnough(items)) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 293);// 下注不足
			return null;
		}
		player.getBackpackManager().removeItems(items, false, Reason.INDOGO_POUR, "跨服石英大会下注扣除");// 先扣除不刷新
		GRPourSuccessIndigoReq.Builder builder = GRPourSuccessIndigoReq.newBuilder();
		builder.setPlayerId(playerId);
		builder.setPouredId(pourId);
		builder.setPourOrderId(orderId);
		builder.setType(type);
		CrossServer.getInstance().send(ServerType.ROUTE_SERVER_VALUE,
				new SMessage(GRPourSuccessIndigoReq.MsgID.eMsgID_VALUE, builder.build().toByteArray()));

		return null;
	}

}
