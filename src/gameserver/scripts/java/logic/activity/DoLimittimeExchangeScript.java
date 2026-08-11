package logic.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.C2SActivityMsg.DoLimittimeExchange;
import Message.C2SActivityMsg.DoLimittimeExchangeID;
import Message.S2CActivityMsg.ActivityDoRsp;
import Message.S2CActivityMsg.ActivityDoRspID;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.LimittimeExchangeConfig;
import game.server.logic.activity.bean.LimittimeExchangeItem;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: DoLimittimeExchangeScript
 * @Description: 兑换限时兑换
 * @author tzyx
 * @date 2018年7月19日 下午1:10:04
 */
public class DoLimittimeExchangeScript implements IScript {
	private static Logger LOGGER = Logger.getLogger(DoLimittimeExchangeScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		Player player = (Player) script.get(Key.PLAYER);
		DoLimittimeExchange req = (DoLimittimeExchange) script.get(Key.ARG1);
		doLimitExchange(player, req);
		return null;
	}

	private void doLimitExchange(Player player, DoLimittimeExchange req) {
		int num = req.getNum();
		if (num < 0) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 23);
			LOGGER.info("[ error!!! : 玩家修改数据，试图兑换负数道具。玩家：" + player.getPlayerId() + " 玩家名称： " + player.getPlayerName()
					+ " 数据体：num : " + num + "]");
			return;
		}
		ActivityService service = ActivityService.getInstance();
		Map<String, LimittimeExchangeConfig> map = service.getAllLimittimeExchange();
		LimittimeExchangeConfig config = map.get(req.getActivityId());
		if (null == config) {
			LOGGER.error("不存在的活动! id:" + req.getActivityId());
			MessageUtils.sendPrompt(player, PromptType.ERROR, 262);
			return;
		}
		if (config.getOpen() != 1 || config.getActive() != 1) {
			LOGGER.error("活动暂未开启!");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 262);
			return;
		}
		List<Item> use = new ArrayList<>();
		List<Item> reward = new ArrayList<>();
		for (LimittimeExchangeItem item : config.getItems()) {
			if (item.getId().equals(req.getId())) {
				// 是否有总可用次数
				if (item.getAllTimes() <= item.getAllUsedTimes() + num) {
					LOGGER.error("购买次数不足!");
					MessageUtils.sendPrompt(player, PromptType.ERROR, 26);
					return;
				}
				StringBuilder sb = new StringBuilder();
				sb.append(item.getAllUsedTimes()).append("/").append(item.getAllTimes()).append("/");
				// 个人最大
				int personMax = item.getPersonMaxTimes();
				// 个人已用
				int personNum = 0;
				if (item.getUsedTimes().containsKey(player.getPlayerId())) {
					personNum = item.getUsedTimes().get(player.getPlayerId());
				}
				// 是否有个人购买次数
				if (personMax <= personNum + num) {
					LOGGER.error("购买次数不足!");
					MessageUtils.sendPrompt(player, PromptType.ERROR, 26);
					return;
				}
				sb.append(personNum).append("/").append(personMax).append("/").append(num);
				for (Item it : item.getExchanges()) {
					use.addAll(BeanFactory.createProps(it.getId(), it.getNum() * num));
				}
				for (Item it : item.getAwards()) {
					reward.addAll(BeanFactory.createProps(it.getId(), it.getNum() * num));
				}
				// 检查兑换道具
				if (!player.getBackpackManager().isItemNumEnough(use)) {
					LOGGER.error("道具不足!");
					MessageUtils.sendPrompt(player, PromptType.ERROR, 332);
					return;
				}
				ActivityDoRsp.Builder rsp = ActivityDoRsp.newBuilder();
				rsp.setActivityId(config.getId());
				StringBuilder sb1 = new StringBuilder();
				for (Item item2 : reward) {
					sb1.append(item2.getId()).append("_").append(item2.getNum()).append(",");
					rsp.addItemList(item2.genBuilder());
				}
				player.getBackpackManager().addItems(reward, true, false, Reason.ACTIVITY_LIMITTIME_EXCHANGE, "");
				// 增加次数
				item.setAllUsedTimes(item.getAllUsedTimes() + num);
				item.getUsedTimes().put(player.getPlayerId(), personNum + num);
				service.limittimeExchangeNotify(player);
				MessageUtils.send(player, player.getFactory().fetchSMessage(ActivityDoRspID.ActivityDoRspMsgID_VALUE,
						rsp.build().toByteArray()));
				// 记录动作 活动id 全局使用/总次数/个人次数/个人最大/当前使用次数 明细id 获取的道具
				LogService.getInstance().logPlayerAction(player, DoLimittimeExchangeID.DoLimittimeExchangeMsgID_VALUE,
						config.getId(), sb.substring(0, sb.length() - 1), item.getId(),
						sb1.substring(0, sb1.length() - 1));
				break;
			}
		}
	}
}
