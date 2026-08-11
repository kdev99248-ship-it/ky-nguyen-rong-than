package logic.recharge;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

import Message.S2CPlayerMsg.PlayerProperty;
import Message.S2CRechargeMsg.MoonCardMsg;
import Message.S2CRechargeMsg.MoonCardType;
import Message.S2CRechargeMsg.RechargeSuccessNotify;
import Message.S2CRechargeMsg.RechargeSuccessNotifyID;
import data.bean.t_globalBean;
import data.bean.t_rechargeBean;
import game.core.pub.command.Handler;
import game.core.pub.script.IScript;
import game.server.db.LogFactory;
import game.server.db.game.bean.RechargeBean;
import game.server.db.game.dao.RechargeDao;
import game.server.db.log.bean.PlayerChargeLogBean;
import game.server.logic.activity.ActivityService;
import game.server.logic.backpack.BackpackService;
import game.server.logic.constant.GlobalID;
import game.server.logic.constant.ItemType;
import game.server.logic.constant.Reason;
import game.server.logic.constant.TaskConditionType;
import game.server.logic.global.GameGlobalService;
import game.server.logic.item.bean.Item;
import game.server.logic.journey.JourneyService;
import game.server.logic.log.LogService;
import game.server.logic.operateActivity.OperateActivityService;
import game.server.logic.player.Player;
import game.server.logic.player.RoleViewService;
import game.server.logic.recharge.RechargeManager;
import game.server.logic.recharge.RechargeService;
import game.server.logic.recharge.bean.MoonCard;
import game.server.logic.recharge.bean.OrderBean.OrderState;
import game.server.logic.recharge.bean.OrderBean.PayType;
import game.server.logic.recharge.handler.InnerRechargeOffline;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.thread.RechargeProcessor;
import game.server.util.MessageUtils;

/** 充值处理逻辑 */
public class RechargeFinishScript implements IScript {

	private Logger logger = LoggerFactory.getLogger(RechargeFinishScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs parm = (ScriptArgs) arg;
		Player player = (Player) parm.get(ScriptArgs.Key.PLAYER);
		RechargeBean bean = (RechargeBean) parm.get(ScriptArgs.Key.ARG1);
		boolean isOffline = (boolean) parm.get(ScriptArgs.Key.ARG2);
		int productId = bean.getProductId(); // 商品id
		int payType = bean.getType(); // 充值类型
		String orderId = bean.getOrderId();// sdk订单id

		try {
			PlayerChargeLogBean logbean = LogFactory.createPlayerChargeLog(player, bean);// 日志记录
			logger.error("收到充值商品:" + productId + " 订单信息:[ 支付类型:" + payType + " , 订单号: " + orderId + ", 充值金额: "
					+ bean.getSingleMount() + " ]");
			RechargeManager rechargeManager = player.getRechargeManager();
			t_rechargeBean rechargeBean = BeanTemplet.getRechargeBean(productId);
			if (rechargeBean == null) {
				logger.error("充值商品:" + productId + "不存在!");
				return false;
			}

			// 当日首冲
			if (rechargeManager.getDayRechargeNum() == 0)
				player.getTaskManager().updateTaskCondition(TaskConditionType.rechargeDays, 1);
			// 判断连续充值
			if (rechargeManager.getLastRechargeTimes() > 0) {
				long days = game.core.pub.util.DateUtils.getDiffDays(rechargeManager.getLastRechargeTimes(),
						System.currentTimeMillis());
				if (days == 1) {
					rechargeManager.setContinueRechargeDays(rechargeManager.getContinueRechargeDays() + 1);
				} else if (days > 1) {
					rechargeManager.setContinueRechargeDays(0);
				}
				player.getTaskManager().updateTaskCondition(TaskConditionType.continue_recharge_day,
						rechargeManager.getContinueRechargeDays());
			}
			// 更新充值时间
			rechargeManager.setLastRechargeTimes(System.currentTimeMillis());
			// 总累积充值金额
			rechargeManager.setAllRechargeNum(rechargeManager.getAllRechargeNum() + rechargeBean.getDiamonds());
			if (payType != PayType.TEST.getVal() && payType != PayType.INNER.getVal()) {
				rechargeManager.setRechargeNumStat(rechargeManager.getRechargeNumStat() + rechargeBean.getDiamonds());
			}
			// 更新充值返利数据 测试和内部充值不统计
			player.getRechargeRebateManager().updateRebateInfo(player, bean.getSingleMount(),
					rechargeBean.getDiamonds());

			// 增加VIP经验 顺序不能改
			player.getVipManager().addVipExp(rechargeBean.getDiamonds());

			boolean success = true; // 这笔充值是否成功发放道具
			/**
			 * 发送商品
			 */
			// String[] supply =
			// BeanTemplet.getGlobalBean(326).getStr_value().split(",");
			// 超级补给
			t_globalBean supply = BeanTemplet.getGlobalBean(326);
			int proId = 0;
			if (null != supply && !supply.getStr_value().trim().equals("")) {
				String[] supplys = supply.getStr_value().split(",");
				for (String supp : supplys) {
					if (productId == Integer.valueOf(supp)) {
						proId = productId;
						break;
					}
				}
			}
			// 礼包
			t_globalBean gift = BeanTemplet.getGlobalBean(246);
			String[] gifts;
			if (null == gift || gift.getStr_value().trim().equals("")) {
				gifts = new String[] { "0", "0", "0" };
			} else {
				gifts = BeanTemplet.getGlobalBean(246).getStr_value().split(";");
			}
			if (JourneyService.getInstance().buyJourneyCard(player, productId)) {
				// 购买旅程卡
				success = true;
				RechargeService.getInstance().sendRechargeSuccess(player, isOffline, orderId);
			} else if (proId != 0) {
				// 超级补给特殊处理
				success = ActivityService.getInstance().superSupplyRecharge(player, productId);
				RechargeService.getInstance().sendRechargeSuccess(player, isOffline, orderId);
			} else if (productId == Integer.valueOf(gifts[0]) || productId == Integer.valueOf(gifts[1])) {
				// 绝版礼包特殊处理
				success = OperateActivityService.getInstance().tryTickGiftBag(player, productId);
				RechargeService.getInstance().sendRechargeSuccess(player, isOffline, orderId);
			} else if (productId == BeanTemplet.getGlobalBean(418).getInt_value()) {
				// 限时礼包特殊处理
				success = ActivityService.getInstance().buyLimittimeGift(player, orderId);
				RechargeService.getInstance().sendRechargeSuccess(player, isOffline, orderId);
			} else if (productId >= 200 && GameGlobalService.getInstance()
					.findById(GlobalID.AUDIT_ENVIRONMENT.getValue()).getIntVal() != 1) {// 购买自定义商品
				success = OperateActivityService.getInstance().sendProduct(player, productId);
			} else {// 钻石类商品
				int type = 0;
				// 月卡操作
				for (MoonCard moonCard : rechargeManager.getMoonCardMap().values()) {
					// 如果激活 type+1
					if (moonCard.isActive())
						type++;
					if (!moonCard.isActive()) {
						if (addActiveNum(rechargeBean.getDiamonds(), moonCard)) {
							RoleViewService.updateMoonCard(player.getPlayerId(), true);
							type++;
						}
					}
				}
				// 沒有激活时再推
				if (type >= 2 && !rechargeManager.isDoubleCard())
					rechargeManager.checkDoubleMoonCard();// 检测双月卡激活

				// 判断连续充值
				if (rechargeManager.getLastRechargeTimes() > 0) {
					long days = game.core.pub.util.DateUtils.getDiffDays(rechargeManager.getLastRechargeTimes(),
							System.currentTimeMillis());
					if (days == 1) {
						rechargeManager.setContinueRechargeDays(rechargeManager.getContinueRechargeDays() + 1);
					} else if (days > 1) {
						rechargeManager.setContinueRechargeDays(0);
					}
					player.getTaskManager().updateTaskCondition(TaskConditionType.continue_recharge_day,
							rechargeManager.getContinueRechargeDays());
				}

				// 当日累积充值金额
				rechargeManager.setDayRechargeNum(rechargeManager.getDayRechargeNum() + rechargeBean.getDiamonds());
				player.getTaskManager().updateTaskCondition(TaskConditionType.N_DAY_M_YUAN,
						rechargeManager.getDayRechargeNum(), 1);

				// 运营活动推送月卡相关
				OperateActivityService.getInstance().mooncardActivityNotify(player);

				diamondProduct(player, productId, rechargeManager, rechargeBean, isOffline, orderId);

				// 绝版礼包修改
				// 触发礼包
				// OperateActivityService.getInstance().tryTickGiftBag(player);
				// OperateActivityService.getInstance().giftBagNotify(player,
				// null);

				List<Item> l = new ArrayList<>();
				// 发送首充奖励 顺序不能改 [ 礼包不算首充 ]
				if (!rechargeManager.isFirstCharge()) {
					l.addAll(OperateActivityService.getInstance().firstRechargeGift(player));
					rechargeManager.setFirstCharge(true);
					logger.error("[ 充值商品:" + productId + " ] 为玩家" + bean.getPlayerId() + " 首充！");
				}
				l.addAll(OperateActivityService.getInstance().firstRichRechargeGift(player));
				// 获取物品推送到客户端
				if (!l.isEmpty()) {
					BackpackService.getInstance().getRewardNotify(player, l);
				}
			}

			//////////// 日志相关 ////////////////
			if (success) {
				bean.setState(OrderState.FINISH.getDesc());
				LogService.getInstance().logPlayerCharge(player, logbean);// 充值完成
				logger.error("[ 玩家：" + bean.getPlayerId() + "的充值商品:" + productId + " ] 充值完成，订单状态修改为"
						+ OrderState.FINISH.getDesc() + "！");
			} else {
				bean.setState(OrderState.FAIL.getDesc());
			}
			// 存数据库
			RechargeProcessor.getInstance().addCommand(new Handler() {
				@Override
				public void action() {
					if (0 == RechargeDao.updateSingleOne(bean)) {
						logger.error("充值状态修改失败: " + bean.getOrderId() + "当前状态：" + bean.getState());
					}
				}
			});
			return success;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("[ 玩家 " + bean.getPlayerId() + " 的充值商品:" + productId + " 订单号： " + bean.getOrderId()
					+ " ] 充值失败！ 已将放入离线充值列表!");
			RechargeProcessor.getInstance().addCommand(new InnerRechargeOffline(bean));
		}
		return false;
	}

	/**
	 * 钻石类商品发送
	 * 
	 * @param player
	 * @param productId
	 * @param rechargeManager
	 * @param rechargeBean
	 */
	private void diamondProduct(Player player, int productId, RechargeManager rechargeManager,
			t_rechargeBean rechargeBean, boolean isOffline, String orderId) {
		int diamond = rechargeBean.getDiamonds();
		int extraDiamond = 0;
		Integer num = rechargeManager.getRechargeMap().get(productId);
		RechargeBean bean = RechargeDao.select(orderId);
		if (num == 0) {
			rechargeManager.getRechargeMap().put(productId, 1);
			// 首次
			extraDiamond += rechargeBean.getFirst_award();
		} else {
			rechargeManager.getRechargeMap().put(productId, num + 1);
			// 额外
			extraDiamond += rechargeBean.getExtra_award();
		}
		if (extraDiamond > 0) {
			List<Item> extra = BeanFactory.createProps(ItemType.DIAMOND.value(), extraDiamond);
			player.getBackpackManager().addItems(extra, Reason.RECHARGE_FREEGIFT, "");
		}
		List<Item> normal = BeanFactory.createProps(ItemType.DIAMOND.value(), diamond);
		player.getBackpackManager().addItems(normal, Reason.RECHARGE, "");
		// 加钻石
		// player.setRechargeDiamond(player.getRechargeDiamond() + diamond);
		player.setTotalRechargeDiamond(player.getTotalRechargeDiamond() + diamond);

		// 更新任务
		player.getTaskManager().updateTaskCondition(TaskConditionType.rechargeDiamond, diamond);
		player.getTaskManager().updateTaskCondition(TaskConditionType.buyProductId, productId, 1);
		// 运营活动推送成长计划数字变化
		OperateActivityService.getInstance().grownPlanNotify(player);

		// 通知前端刷新
		player.sendPlayerPropertyChanged(PlayerProperty.DIAMOND, player.getDiamond());
		// 推送充值成功信息
		RechargeSuccessNotify.Builder builder = RechargeSuccessNotify.newBuilder();
		builder.setId(productId);
		builder.setDiamond(diamond);
		builder.setExtraDiamond(extraDiamond);
		builder.setVipLevel(player.getVipManager().getVipLevel());
		builder.setVipExp(player.getVipManager().getVipExp());
		builder.setNum(rechargeManager.getRechargeMap().get(productId));
		builder.setOffline(isOffline);
		if (null != bean && !StringUtils.isEmpty(bean.getExtinfo())) {
			JSONObject extinfo = JSONObject.parseObject(bean.getExtinfo());
			if (extinfo.containsKey("payType")) {
				builder.setPayType(getPayType(extinfo.getInteger("payType")));
			}
			if (extinfo.containsKey("payAmount")) {
				builder.setPayAmount(extinfo.getString("payAmount"));
			}
			if (extinfo.containsKey("payCurrency")) {
				builder.setPayCurrency(extinfo.getString("payCurrency"));
			}
		}
		String[] item = StringUtils.split(rechargeBean.getExtra_redpacket(), ",");
		if (item.length >= 2) {
			List<Item> items = BeanFactory.createProps(Integer.parseInt(item[0]), Integer.parseInt(item[1]));
			player.getBackpackManager().addItems(items, Reason.RECHARGE, rechargeBean.getId() + "");
			builder.setItem(items.get(0).genBuilder());
		}
		for (MoonCard moonCard : rechargeManager.getMoonCardMap().values()) {
			MoonCardMsg.Builder mcBuilder = MoonCardMsg.newBuilder();
			mcBuilder.setType(moonCard.getType());
			mcBuilder.setActive(moonCard.isActive());
			builder.addMooncard(mcBuilder);
		}
		player.getRechargeManager().notifyRechargeNumStat();
		MessageUtils.send(player.getSession(), player.getFactory().fetchSMessage(
				RechargeSuccessNotifyID.RechargeSuccessNotifyMsgID_VALUE, builder.build().toByteArray()));
	}

	/**
	 * 增加月卡激活点
	 * 
	 * @param price
	 *            充值消耗的钱
	 * @param moonCard
	 */
	public boolean addActiveNum(int price, MoonCard moonCard) {
		moonCard.setActiveNum(moonCard.getActiveNum() + price);
		if (moonCard.getType() == MoonCardType.NORMAL_MOONCARD_VALUE) {
			// 单位：元
			int normal = Integer.parseInt(BeanTemplet.getGlobalBean(420).getStr_value().split(";")[0]);// 普通月卡
			if (normal <= moonCard.getActiveNum()) {
				activeMoonCard(moonCard);
				return true;
			}
		} else if (moonCard.getType() == MoonCardType.ADVANCED_MOONCARD_VALUE) {
			// 单位：元
			int advanced = Integer.parseInt(BeanTemplet.getGlobalBean(420).getStr_value().split(";")[1]);// 高级月卡
			if (advanced <= moonCard.getActiveNum()) {
				activeMoonCard(moonCard);
				return true;
			}
		}
		return false;
	}

	/**
	 * 激活月卡
	 * 
	 * @param moonCard
	 */
	private void activeMoonCard(MoonCard moonCard) {
		moonCard.setActiveNum(0);
		moonCard.setActiveTime(System.currentTimeMillis());
		moonCard.setActive(true);
	}

	// 睿德互动SDK接入 通过payType判断支付类型
	private int getPayType(Integer type) {
		if (type == 29 || type == 83 || type == 84 || type == 85 || type == 86 || type == 87) {
			return 3;
		}
		if (type == 30) {
			return 1;
		}
		if (type == 8) {
			return 2;
		}
		return 0;
	}

}
