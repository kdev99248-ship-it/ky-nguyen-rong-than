package logic.activity;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Message.C2SBasicActivityMsg.RepairSignReqID;
import Message.S2CBackpackMsg.PropInfo;
import Message.S2CBasicActivityMsg.DaySignRsp;
import Message.S2CBasicActivityMsg.DaySignRspID;
import Message.S2CHeroMsg.HeroIdInfo;
import Message.S2CPlayerMsg.PromptType;
import Message.S2CRechargeMsg.MoonCardType;
import data.bean.t_checkBean;
import data.bean.t_globalBean;
import game.core.pub.script.IScript;
import game.core.pub.util.DateUtils;
import game.server.logic.basicActivity.BasicActivityManager;
import game.server.logic.constant.GlobalID;
import game.server.logic.constant.ItemType;
import game.server.logic.constant.Reason;
import game.server.logic.global.GameGlobalService;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

public class RepairDaySignScript implements IScript {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

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
		int index = (int) args.get(ScriptArgs.Key.ARG1);

		BasicActivityManager basicActivityManager = player.getBasicActivityManager();
		long serverOpenTime = Long
				.parseLong(GameGlobalService.getInstance().findById(GlobalID.OPENSERVER_TIME.getValue()).getStrVal());
		int crossDay = (int) DateUtils.getAcrossDays(serverOpenTime, System.currentTimeMillis(), 5) + 1;
		int curMonth = crossDay / 30 + 1 > 12 ? 1 : crossDay / 30 + 1;
		int day = crossDay % 30 == 0 ? 30 : crossDay % 30;
		if (index > day) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5008);// 还不能签到
			return null;
		}
		if (basicActivityManager.getRule(index)) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 260);// 重复签到
			return null;
		}
		t_globalBean bean = BeanTemplet.getGlobalBean(325);
		if (null == bean) {
			// MessageUtils.sendPrompt(player, PromptType.ERROR, 260);// 金钱配置错误
			logger.info("waring --- 补签消耗配置错误 ！ ");
			return null;
		}

		if (player.getDiamond() < bean.getInt_value()) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 10);
			return null;
		}
		List<Item> costs = BeanFactory.createProps(ItemType.DIAMOND.value(), bean.getInt_value());
		player.getBackpackManager().removeItems(costs, true, Reason.REPAIR_DAY_SIGN, "补签消耗");

		basicActivityManager.setSignNum(basicActivityManager.getSignNum() + 1);
		basicActivityManager.setSignTotal(basicActivityManager.getSignTotal() + 1);
		// basicActivityManager.setDaySign(true);
		basicActivityManager.addRule(index); // 今天加领取标记
		// 奖励发送
		t_checkBean checkBean = BeanTemplet.getCheckBean(curMonth * 100 + index);
		if (null == checkBean) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 260);// 重复签到
			return null;
		}
		int itemId = Integer.parseInt(checkBean.getAward().split(",")[0]);
		if (12001 <= itemId && itemId < 13001) {// 英雄碎片,要做服务器是否指定英雄判断
			HeroIdInfo.Builder builder = basicActivityManager.checkSignHero();
			if (builder != null) {
				itemId = BeanTemplet.getHeroBean(builder.getId()).getFragment_id();
			}
		}
		int itemNum = Integer.parseInt(checkBean.getAward().split(",")[1]);
		if (checkBean.getCondition() == 1) {// vip等级
			if (player.getVipLevel() >= checkBean.getParameter()) {
				itemNum += checkBean.getAward_extra();
			}
		} else if (checkBean.getCondition() == 2) {// 高级月卡
			if (player.getRechargeManager().getMoonCardMap().get(MoonCardType.ADVANCED_MOONCARD_VALUE).isActive()) {
				itemNum += checkBean.getAward_extra();
			}
		}

		List<Item> items = new ArrayList<>();
		items.addAll(BeanFactory.createProps(itemId, itemNum));
		player.getBackpackManager().addItems(items, true, true, Reason.DAY_SIGN, "");
		// 推送
		DaySignRsp.Builder builder = DaySignRsp.newBuilder();
		builder.setDaySign(basicActivityManager.getRule(index));
		builder.setIndex(index);
		builder.setSignNum(basicActivityManager.getSignNum());
		builder.setSignTotal(basicActivityManager.getSignTotal());
		for (Item item : items) {
			PropInfo.Builder pBuilder = PropInfo.newBuilder();
			pBuilder.setId(item.getId());
			pBuilder.setNum(item.getNum());
			builder.addItems(pBuilder);
		}
		MessageUtils.send(player,
				player.getFactory().fetchSMessage(DaySignRspID.DaySignRspMsgID_VALUE, builder.build().toByteArray()));
		// 日志记录
		LogService.getInstance().logPlayerAction(player, RepairSignReqID.RepairSignReqMsgID_VALUE,
				basicActivityManager.getSignNum() + "_补签");

		return null;
	}

}
