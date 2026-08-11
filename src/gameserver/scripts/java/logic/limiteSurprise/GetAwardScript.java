package logic.limiteSurprise;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Message.S2CPlayerMsg.PromptType;
import Message.S2cLimiteSurpriseMsg.ReceiveAwardRsp;
import Message.S2cLimiteSurpriseMsg.ReceiveAwardRspID;
import Message.S2cLimiteSurpriseMsg.SurpriseNotifyRspID;
import data.bean.t_surpriseBean;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.limiteSurprise.LimiteSurpriseService;
import game.server.logic.limiteSurprise.bean.SurpriseBean;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 获得限时惊喜奖励
 */
public class GetAwardScript implements IScript {
	private final Logger log = Logger.getLogger(LimiteSurpriseService.class);

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
		ScriptArgs parm = (ScriptArgs) arg;
		Player player = (Player) parm.get(ScriptArgs.Key.PLAYER);
		int id = (int) parm.get(ScriptArgs.Key.ARG1);
		getSupriseAwards(player, id);
		return null;
	}

	// 获得限时惊喜奖励
	public void getSupriseAwards(Player player, int id) {
		SurpriseBean bean = player.getLimiteSurpriseManager().getSurpriseMap().get(id);
		List<Integer> finishList = player.getLimiteSurpriseManager().getFinishList();
		if (bean == null) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 2000);// TODO配置一下语言表
			log.warn("限时惊喜没有解锁");
			return;
		}
		List<Item> items = new ArrayList<>();

		// 限时惊喜没有完成
		if (!bean.isFinish()) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 2003);
			log.warn("限时惊喜没有完成");
			return;
		}
		// 奖励已经领取
		if (bean.isAward()) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 2002);
			log.warn("奖励已经领取");
			return;
		}
		t_surpriseBean surpriseBean = BeanTemplet.getLimiteSurpise(id);
		if (surpriseBean == null) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 2000);
			log.warn("限时惊喜配置已经更改");
			return;
		}
		String str = surpriseBean.getReward();
		// 发奖
		String[] rewards = str.split(";");
		for (String reward : rewards) {
			items.addAll(BeanFactory.createProps(Integer.parseInt(reward.split(",")[0]),
					Integer.parseInt(reward.split(",")[1])));
		}
		player.getBackpackManager().addItems(items, true, true, Reason.LIMITE_SURPRISE_AWARD, "");
		bean.setAward(true);
		finishList.add(bean.getId());
		player.getLimiteSurpriseManager().getSurpriseMap().remove(id);
		player.getLimiteSurpriseManager().notifyClient();
		StringBuilder sBuilder = new StringBuilder();
		// 发送协议
		ReceiveAwardRsp.Builder builder = ReceiveAwardRsp.newBuilder();
		builder.setId(id);
		for (Item item : items) {
			builder.addItemList(item.genBuilder());
			sBuilder.append(",").append(item.getId()).append("_").append(item.getNum());
		}
		sBuilder.append("|"+id);
		MessageUtils.send(player.getSession(), player.getFactory()
				.fetchSMessage(ReceiveAwardRspID.ReceiveAwardRspMsgID_VALUE, builder.build().toByteArray()));
		
		// 记录日志
		LogService.getInstance().logPlayerAction(player, ReceiveAwardRspID.ReceiveAwardRspMsgID_VALUE,
				sBuilder.substring(1));
	}

}
