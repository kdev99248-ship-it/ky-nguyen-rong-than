package logic.snatchTerritory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Message.C2SSnatchTerritoryMsg.ReqGetGymAwardID;
import Message.S2CBackpackMsg.PropInfo;
import Message.S2CPlayerMsg.PromptType;
import Message.S2CSnatchTerritoryMsg.NotifyGymAwards;
import Message.S2CSnatchTerritoryMsg.NotifyGymAwardsID;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.guild.GuildService;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.snatchTerritory.SnatchTerritoryService;
import game.server.logic.snatchTerritory.bean.SnatchTerritory;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

public class RspGetGymAwardScript implements IScript {

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
		int gymId = (int) script.get(ScriptArgs.Key.ARG1);
		if (gymId == -1) {
			// -1 全部领取
			long guildId = GuildService.getInstance().getPlayerGuildId(player.getPlayerId());
			Map<Integer, SnatchTerritory> map = SnatchTerritoryService.getInstance().getTerritoryMap();
			boolean get = false;
			for (SnatchTerritory stBean : map.values()) {
				if (null == stBean)
					continue;
				if (stBean.getDefendGuildId() == guildId) {
					List<Item> items = new ArrayList<Item>();
					notifyGetGymAwards(player, stBean);
					for (Item item : stBean.getItems()) {
						if (null != item) {
							int realNum = player.getSnatchTerritoryManager().getAwardByTotalNum(stBean.getId(),
									item.getId(), item.getNum(), true);
							if (realNum > 0)
								items.addAll(BeanFactory.createProps(item.getId(), realNum));
						}
					}
					if (!items.isEmpty())
						get = true;
					player.getBackpackManager().addItems(items, true, true, Reason.GYM_GET_AWARD, "获取道馆基础奖励");
					player.getSnatchTerritoryManager().notifyOneGymToClient(stBean);
				}
			}
			if (!get) {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 110);
			}

			// 玩家操作日志
			LogService.getInstance().logPlayerAction(player, ReqGetGymAwardID.ReqGetGymAwardMsgID_VALUE, gymId, get);
		} else {
			SnatchTerritory st = SnatchTerritoryService.getInstance().getTerritoryMap().get(gymId);
			if (null != st) {
				// 获取奖励
				List<Item> items = new ArrayList<Item>();
				notifyGetGymAwards(player, st);
				for (Item item : st.getItems()) {
					if (null != item) {
						int realNum = player.getSnatchTerritoryManager().getAwardByTotalNum(gymId, item.getId(),
								item.getNum(), true);
						if (realNum > 0)
							items.addAll(BeanFactory.createProps(item.getId(), realNum));
					}
				}
				if (!items.isEmpty()) {
					player.getBackpackManager().addItems(items, true, true, Reason.GYM_GET_AWARD, "获取道馆基础奖励");
					// 刷新客户端
					player.getSnatchTerritoryManager().notifyOneGymToClient(st);
				}

				// 玩家操作日志
				LogService.getInstance().logPlayerAction(player, ReqGetGymAwardID.ReqGetGymAwardMsgID_VALUE, gymId);
			} else {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 110);
			}
		}
		return null;
	}

	// 推送领取道具
	private void notifyGetGymAwards(Player player, SnatchTerritory st) {
		NotifyGymAwards.Builder builder = NotifyGymAwards.newBuilder();
		builder.setGymId(st.getId());
		builder.setIsMine(true);
		for (Item item : st.getItems()) {
			int realNum = player.getSnatchTerritoryManager().getAwardByTotalNum(st.getId(), item.getId(), item.getNum(),
					false);
			if (realNum > 0) {
				PropInfo.Builder itemBuilder = PropInfo.newBuilder();
				itemBuilder.setId(item.getId());
				itemBuilder.setNum(realNum);
				builder.addAwards(itemBuilder);
			}
		}
		MessageUtils.send(player, player.getFactory().fetchSMessage(NotifyGymAwardsID.NotifyGymAwardsMsgID_VALUE,
				builder.build().toByteArray()));
		// 2018年9月15日14:14:15 策划需求 自己领取只给自己弹窗 不再给其他玩家弹窗
		// List<Player> players = PlayerManager.getAllPlayers();
		// for (Player allPlayer : players) {
		// if (null == allPlayer || !allPlayer.isOnline())
		// continue;
		// Guild guild =
		// GuildService.getInstance().getPlayerGuild(allPlayer.getPlayerId());
		// if (null == guild)
		// continue;
		// builder.setIsMine(allPlayer.getPlayerId() == player.getPlayerId());
		// MessageUtils.send(allPlayer, allPlayer.getFactory()
		// .fetchSMessage(NotifyGymAwardsID.NotifyGymAwardsMsgID_VALUE,
		// builder.build().toByteArray()));
		// }
	}

}
