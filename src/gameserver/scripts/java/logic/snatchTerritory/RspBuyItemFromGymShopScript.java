package logic.snatchTerritory;

import java.util.ArrayList;
import java.util.List;

import Message.C2SSnatchTerritoryMsg.ReqBuyItemFromGymShopID;
import Message.S2CPlayerMsg.PromptType;
import Message.S2CSnatchTerritoryMsg.RspBuyItemID;
import Message.S2CSnatchTerritoryMsg.RspBuyItemMsg;
import data.bean.t_roadPavilion_shopBean;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.constant.TaskConditionType;
import game.server.logic.guild.GuildService;
import game.server.logic.item.bean.Airship;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.snatchTerritory.SnatchTerritoryService;
import game.server.logic.snatchTerritory.bean.SnatchTerritoryShopItem;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 道馆商店购买
 */
public class RspBuyItemFromGymShopScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		Player player = (Player) script.get(ScriptArgs.Key.PLAYER);
		int tab = (int) script.get(ScriptArgs.Key.ARG1);
		int id = (int) script.get(ScriptArgs.Key.ARG2);
		int num = (int) script.get(ScriptArgs.Key.ARG3);
		int taskNum = num;// 更新任务用
		long guildId = GuildService.getInstance().getPlayerGuildId(player.getPlayerId());
		List<SnatchTerritoryShopItem> list = SnatchTerritoryService.getInstance().getShops().get(guildId);
		RspBuyItemMsg.Builder builder = RspBuyItemMsg.newBuilder();
		if (null == list || list.isEmpty()) {
			builder.setResult(3);
			MessageUtils.send(player, player.getFactory().fetchSMessage(RspBuyItemID.RspBuyItemMsgID_VALUE,
					builder.build().toByteArray()));
			return null;
		}
		int canBuy = 0;
		buy: for (SnatchTerritoryShopItem item : list) {
			if (null == item)
				continue;
			if (item.getTabId() == tab && item.getId() == id) {
				t_roadPavilion_shopBean bean = BeanTemplet.getRoadPavilionShopBean(id);
				if (null == bean) {
					if (item.getType() == 2) {
						List<game.server.logic.item.bean.Item> items = new ArrayList<game.server.logic.item.bean.Item>();
						String[] price = item.getPresent_price().split(",");
						for (int i = 0; i < price.length; i++) {
							if (price[i].split("_").length < 2) {
								// 配置错误 不让买
								canBuy = 2;
								break buy;
							}
							int itemId = Integer.parseInt(price[i].split("_")[0]);
							int itemNum = Integer.parseInt(price[i].split("_")[1]);
							items.addAll(BeanFactory.createProps(itemId, itemNum * num));
						}
						if ((item.isDayLimit()
								&& player.getSnatchTerritoryManager().getBuyCountById(id) >= item.getItemLimitNum())
								|| num > item.getItemLimitNum()) {
							canBuy = 4;
							MessageUtils.sendPrompt(player, PromptType.ERROR, 1383);// 次数不够
							break;
						}
						if (item.getItemNum() >= num && item.getItemNum() > 0) {
							if (!player.getBackpackManager().isItemNumEnough(items)) {
								canBuy = 3;// 消耗不够
								break buy;
							}
							if (!item.isSingle()
									&& !SnatchTerritoryService.getInstance().buyShopItem(guildId, tab, id, num)) {
								canBuy = 1;// 捷足先登
								break buy;
							}

							player.getBackpackManager().removeItems(items, true, Reason.GYM_SHOP_BUY, "道馆商店购买商品消耗");
							if (item.isDayLimit() || item.isSingle())
								player.getSnatchTerritoryManager().addBuyCountById(id, num);// 添加指定商品购买次数
							items.clear();
							if (item.getItemShopNum() > 0) {
								num *= item.getItemShopNum();
							}
							builder.setItemId(item.getItemId());
							builder.setItemNum(num);
							items.addAll(BeanFactory.createProps(item.getItemId(), num));

							// 更新任务
							player.getTaskManager().updateTaskCondition(false, TaskConditionType.GYM_SHOP_BUY_NUM,
									taskNum);
							player.getBackpackManager().addItems(items, Reason.GYM_SHOP_BUY, "道馆商店购买商品");
							break buy;
						} else {
							if (item.getItemNum() <= 0) {
								if (!item.isDayLimit()) {
									canBuy = 4;
									MessageUtils.sendPrompt(player, PromptType.ERROR, 1384);
								} else {
									canBuy = 4;
									MessageUtils.sendPrompt(player, PromptType.ERROR, 1383);// 次数不够
								}
							} else
								canBuy = 1;// 捷足先登
						}
					}
					break buy;
				}
				if (bean.getNumber() > 0 && (player.getSnatchTerritoryManager().getBuyCountById(id) >= bean.getNumber()
						|| num > bean.getNumber())) {
					canBuy = 4;
					MessageUtils.sendPrompt(player, PromptType.ERROR, 21);
					break;
				}
				List<game.server.logic.item.bean.Item> items = new ArrayList<game.server.logic.item.bean.Item>();
				String[] price = bean.getPresent_price().split(";");
				for (int i = 0; i < price.length; i++) {
					if (price[i].split("_").length < 2) {
						// 配置错误 不让买
						canBuy = 2;
						break buy;
					}
					int itemId = Integer.parseInt(price[i].split("_")[0]);
					int itemNum = Integer.parseInt(price[i].split("_")[1]);
					items.addAll(BeanFactory.createProps(itemId, itemNum * num));
				}
				if (item.getItemNum() >= num) {
					if (!player.getBackpackManager().isItemNumEnough(items)) {
						canBuy = 3;// 消耗不够
						break buy;
					}
					if (item.getTabId() == 1
							&& !SnatchTerritoryService.getInstance().buyShopItem(guildId, tab, id, num)) {
						canBuy = 1;// 捷足先登
						break buy;
					}
					player.getBackpackManager().removeItems(items, true, Reason.GYM_SHOP_BUY, "道馆商店购买商品消耗");
					player.getSnatchTerritoryManager().addBuyCountById(id, num);// 添加指定商品购买次数
					items.clear();
					if (bean.getShop_number() > 0) {
						num *= bean.getShop_number();
					}
					builder.setItemId(bean.getItem_id());
					builder.setItemNum(num);
					items.addAll(BeanFactory.createProps(bean.getItem_id(), num));
					// 更新任务
					Item buyItem = items.get(0);
					if (null != buyItem && buyItem instanceof Airship) {
						player.getTaskManager().updateTaskCondition(false, TaskConditionType.BUY_GYM_SHOP, 1);
						player.getSnatchTerritoryManager().addAirshipId(bean.getItem_id());// 飞艇不生成道具放入背包
					} else {
						player.getTaskManager().updateTaskCondition(false, TaskConditionType.GYM_SHOP_BUY_NUM, taskNum);
						player.getBackpackManager().addItems(items, Reason.GYM_SHOP_BUY, "道馆商店购买商品");
					}
					break buy;
				} else {
					canBuy = 4;
					break buy;
				}
			}
		}
		builder.setResult(canBuy);
		MessageUtils.send(player,
				player.getFactory().fetchSMessage(RspBuyItemID.RspBuyItemMsgID_VALUE, builder.build().toByteArray()));
		if (canBuy == 0)
			SnatchTerritoryService.getInstance().addNotifyGymShopToClientAll(guildId);

		// 玩家操作日志
		LogService.getInstance().logPlayerAction(player, ReqBuyItemFromGymShopID.ReqBuyItemFromGymShopMsgID_VALUE,
				canBuy, guildId, id, num);
		return null;
	}

}
