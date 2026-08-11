package logic.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Message.S2CBackpackMsg.PropInfo;
import Message.Inner.GRUtil.SendItem2Player;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.item.handler.AddItemsHandler;
import game.server.logic.line.GameLineManager;
import game.server.logic.line.handler.PlayerUpdateBean;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.ScriptArgs;
import game.server.thread.PlayerRestoreProcessor;
import game.server.thread.dboperator.handler.ReqUpdatePlayerHandler;

/**
 * route服务器发送道具给玩家
 * @author tzyx
 *
 */
public class GRSendItem2PlayerScript implements IScript {

	private final Logger logger = Logger.getLogger(GRSendItem2PlayerScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		SendItem2Player req = (SendItem2Player) script.get(ScriptArgs.Key.ARG1);
		List<Item> items = new ArrayList<>();
		PropInfo prop;
		for (int i = 0; i < req.getAdjunctMapCount(); i++) {
			prop = req.getAdjunctMap(i);
			items.addAll(BeanFactory.createProps(prop.getId(), prop.getNum()));
		}
		BeanFactory.combineItemList(items);
		Player player = PlayerManager.getOffLinePlayerByPlayerId(req.getPlayerId());
		if (player.isOnline()) {// 在线，则抛给玩家线程
            GameLineManager.getInstance().addCommand(player.getLineId(),new AddItemsHandler(
            				player, items, req.getIsNotifyClient(), req.getIsClientTip(),
            				Reason.valueOf(req.getReason()), req.getReasonExtra()));
        } else {
            // 离线，则直接回存
            player.getBackpackManager().addItems(items, Reason.valueOf(req.getReason()), req.getReasonExtra());
            PlayerRestoreProcessor.getInstance().submitRequest(
                    new ReqUpdatePlayerHandler(-1, new PlayerUpdateBean(player
                            .toAccountBean(), player.toPlayerBean(), false)));
        }
		return null;
	}

}
