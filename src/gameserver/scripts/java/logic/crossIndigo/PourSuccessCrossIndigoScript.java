package logic.crossIndigo;

import java.util.ArrayList;
import java.util.List;

import Message.S2CBackpackMsg.PropInfo;
import Message.Inner.GRCrossIndigo.GRPourSuccessIndigoRsp;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.ScriptArgs;

public class PourSuccessCrossIndigoScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs args = (ScriptArgs) arg;
		GRPourSuccessIndigoRsp grPour = (GRPourSuccessIndigoRsp) args.get(ScriptArgs.Key.ARG1);
		boolean result = grPour.getResult();
		long playerId = grPour.getPlayerId();
		Player player = PlayerManager.getPlayerByPlayerId(playerId);
		if (null == player)
			return null;
		if (!result) {
			// 报名失败
			List<Item> items = new ArrayList<>();
			List<PropInfo> prop = grPour.getItemsList();
			for (int i = 0; i < prop.size(); i++) {
				List<Item> item = BeanFactory.createProps(prop.get(i).getId(), prop.get(i).getNum());
				items.addAll(item);
			}
			player.getBackpackManager().addItems(items, Reason.INDOGO_POUR, "石英大会报名失败退还");
		} else {
			player.getBackpackManager().sendBackpackInfos();
		}
		return null;
	}

}
