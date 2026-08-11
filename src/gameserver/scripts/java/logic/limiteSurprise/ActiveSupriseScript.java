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
import game.server.db.game.bean.RechargeBean;
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
 * 激活限时惊喜
 *
 */

public class ActiveSupriseScript implements IScript {
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
		ActiveSuprise(player, id);
		return null;
	}

	// 激活限时惊喜
	public void ActiveSuprise(Player player, int id) {
		SurpriseBean bean = player.getLimiteSurpriseManager().getSurpriseMap().get(id);
		if (bean == null) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 2000);
			log.warn("限时惊喜没有解锁");
			return;
		}
		// 限时惊喜已经完成
		if (bean.isFinish()) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 2001);
			log.warn("限时惊喜已经完成");
			return;
		}
		if (System.currentTimeMillis() < bean.getCurrentTime() + bean.getLimiteTime()*1000) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 2004);
			log.warn("限时惊喜已经激活");
			return;
		}
		t_surpriseBean surpriseBean = BeanTemplet.getLimiteSurpise(id);
		if (surpriseBean == null) {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 2000);
			log.warn("限时惊喜配置已经更改");
			return;
		}
		List<Item> items = new ArrayList<>();
		String[] str = surpriseBean.getConsume().split(";");

		for (String consume : str) {
			items.addAll(BeanFactory.createProps(Integer.parseInt(consume.split(",")[0]),
					Integer.parseInt(consume.split(",")[1])));
		} 
		// 检查材料是否足够
		if (!player.getBackpackManager().isItemNumEnough(items)) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 32);// TODO替换语言包id
			return;
		}
		player.getBackpackManager().removeItems(items, true, Reason.LIMITE_SURPRISE_AWARD, "");
		// 更改激活状态
		bean.setActive(true);
		long time = System.currentTimeMillis();
		bean.setCurrentTime(time);
		//判断激活以后是否通关
		int normalSection = player.getSectionManager().getCurNormalSection();
		int hardSection = player.getSectionManager().getCurHardSection();
		int curNightmareSection = player.getSectionManager().getCurNightmareSection();
		//普通关卡,判断是否通过
		if(bean.getSection() < 2000000 && normalSection >= bean.getSection()) {
			bean.setFinish(true);
		}
		//精英关卡,判断是否通过
		if(bean.getSection() > 2000000 && bean.getSection() < 3000000 && hardSection >= bean.getSection()) {
			bean.setFinish(true);
		}
		if(bean.getSection() > 3000000 && curNightmareSection >= bean.getSection()) {
			bean.setFinish(true);
		}
		StringBuilder sBuilder = new StringBuilder();
		for (Item item : items) {
			sBuilder.append(",").append(item.getId()).append("_").append(-item.getNum());
		}
		sBuilder.append("|"+id);
		// 推送
		player.getLimiteSurpriseManager().notifyClient();
		// 记录日志
		LogService.getInstance().logPlayerAction(player, SurpriseNotifyRspID.SurpriseNotifyRspMsgID_VALUE,
				sBuilder.substring(1));
	}
}
