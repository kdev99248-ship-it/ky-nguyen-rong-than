package logic.player;

import java.util.List;

import Message.S2CPlayerMsg.MainLayerRewardRsp;
import Message.S2CPlayerMsg.MainLayerRewardRspID;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.drop.DropService;
import game.server.logic.item.bean.Item;
import game.server.logic.player.Player;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 主界面交互奖励
 * 
 * @author liuwei
 * @date 2018年8月9日
 */
public class MainLayerRewardScript implements IScript {

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

		if (player.getMainLayerRewardNum() > BeanTemplet.getGlobalBean(289).getInt_value()) {
			MessageUtils.sendPrompt(player, PromptType.NORMAL, 106);// TODO替换语言包id
			return null;
		}
		//发送奖励
		int dropId = BeanTemplet.getGlobalBean(288).getInt_value();
		List<Item> items = DropService.getDropItems(player.getDropManager(), dropId);
		BeanFactory.combineItemList(items);
		player.getBackpackManager().addItems(items, Reason.MAIN_LAYER_REWARD, "");
		//记录领取次数
		player.setMainLayerRewardNum(player.getMainLayerRewardNum() + 1);
		//推送
		MainLayerRewardRsp.Builder builder = MainLayerRewardRsp.newBuilder();
		builder.setNum(player.getMainLayerRewardNum());
		for (Item item : items) {
			builder.addItemList(item.genBuilder());
		}
		MessageUtils.send(player, player.getFactory().fetchSMessage(MainLayerRewardRspID.MainLayerRewardRspMsgID_VALUE,
				builder.build().toByteArray()));
		
		return null;
	}

}
