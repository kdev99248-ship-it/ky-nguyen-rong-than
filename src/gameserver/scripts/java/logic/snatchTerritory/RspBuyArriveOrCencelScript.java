package logic.snatchTerritory;

import java.util.List;

import Message.C2SSnatchTerritoryMsg.ReqAutoArriveID;
import Message.C2SSnatchTerritoryMsg.ReqCancelAttackID;
import Message.S2CPlayerMsg.PromptType;
import Message.S2CSnatchTerritoryMsg.RspNullID;
import Message.S2CSnatchTerritoryMsg.RspNullMsg;
import Message.S2CSnatchTerritoryMsg.SnatchTerritoryType;
import data.bean.t_roadPavilionBean;
import data.bean.t_wildMonsterBean;
import game.core.pub.script.IScript;
import game.fight.bean.CampType;
import game.server.logic.constant.ItemType;
import game.server.logic.constant.Reason;
import game.server.logic.constant.TaskConditionType;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.snatchTerritory.SnatchTerritoryService;
import game.server.logic.snatchTerritory.bean.JoinSnatchPlayer;
import game.server.logic.snatchTerritory.bean.SnatchPlayer;
import game.server.logic.snatchTerritory.bean.SnatchTerritory;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.thread.delay.DelayTaskProcessor;
import game.server.util.MessageUtils;

public class RspBuyArriveOrCencelScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		RspNullMsg.Builder returnBuilder = RspNullMsg.newBuilder();
		ScriptArgs script = (ScriptArgs) arg;
		Player player = (Player) script.get(ScriptArgs.Key.PLAYER);
		int commond = (int) script.get(ScriptArgs.Key.ARG1);// 1一键到达 2取消派遣
		int gymId = (int) script.get(ScriptArgs.Key.ARG2);
		long playerId = player.getPlayerId();
		if (commond == 1) {
			SnatchPlayer sp = null;
			SnatchTerritory st = SnatchTerritoryService.getInstance().getTerritoryMap().get(gymId);
			if (null == st) {
				st = player.getSnatchTerritoryManager().getSnatchTerritoryById(gymId);
				sp = player.getSnatchTerritoryManager().getSnatchPlayerById(playerId);
				if (null == sp) {
					sp = SnatchTerritoryService.getInstance().getSnatchPlayerMap().get(playerId);
				}
				if (null != sp) {
					// 野生怪物id除1000
					t_wildMonsterBean bean = BeanTemplet.getWildMonsterBean(gymId / 1000);
					if (null != bean) {
						int number = bean.getMasonry_number();
						List<Item> items = BeanFactory.createProps(ItemType.DIAMOND.value(), number);
						if (!player.getBackpackManager().isItemNumEnough(items)) {
							MessageUtils.sendPrompt(player, PromptType.ERROR, 1371);
							return null;
						}
						player.getBackpackManager().removeItems(items, true, Reason.GYM_BUYARRIVE, "购买到达时间");
						sp.setArriveTime(System.currentTimeMillis());
						if (sp.getStatus() == SnatchTerritoryType.ATTACK_VALUE) {
							sp.setStatus(SnatchTerritoryType.ALREADYREACHED.getNumber());

							DelayTaskProcessor.getInstance().removeCommand("JoinSnatchPlayer_" + sp.getPlayerId());
							// 立即执行任务
							JoinSnatchPlayer join = new JoinSnatchPlayer(sp);
							DelayTaskProcessor.getInstance().addCommand("JoinSnatchPlayer_" + sp.getPlayerId(), join,
									0);

							SnatchTerritoryService.getInstance().addSnatchPlayerCommand(sp, player, true);
							MessageUtils.sendPrompt(player, PromptType.ERROR, 1361);
						} else if (sp.getStatus() == SnatchTerritoryType.RETURN_VALUE) {
							sp.setTerritroyId(-1);
							// 删除记录
							SnatchTerritoryService.getInstance().removeSnatchPlayer(playerId, player, true);
							player.getSnatchTerritoryManager().removeSnatchPlayerToGym(gymId, sp);
							MessageUtils.sendPrompt(player, PromptType.ERROR, 1359);
						}

						returnBuilder.setClose(true);
						MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
								returnBuilder.build().toByteArray()));
						// 更新任务
						player.getTaskManager().updateTaskCondition(false, TaskConditionType.ONE_KEY_ARRIVE, 1);

						// 玩家操作日志
						LogService.getInstance().logPlayerAction(player, ReqAutoArriveID.ReqAutoArriveMsgID_VALUE,
								gymId, number);
					}
				} else {
					MessageUtils.sendPrompt(player, PromptType.ERROR, 1359);
					returnBuilder.setClose(true);
					MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
							returnBuilder.build().toByteArray()));
				}
				return null;
			}
			sp = st.getSnatchPlayerById(playerId);
			if (null != sp) {
				t_roadPavilionBean bean = BeanTemplet.getRoadPavilionBean(gymId);
				if (null != bean) {
					int number = bean.getMasonry_number();
					List<Item> items = BeanFactory.createProps(ItemType.DIAMOND.value(), number);
					if (!player.getBackpackManager().isItemNumEnough(items)) {
						MessageUtils.sendPrompt(player, PromptType.ERROR, 1371);
						return null;
					}
					player.getBackpackManager().removeItems(items, true, Reason.GYM_BUYARRIVE, "购买到达时间");
					sp.setArriveTime(System.currentTimeMillis());
					if (sp.getStatus() == SnatchTerritoryType.ATTACK_VALUE) {
						sp.setStatus(SnatchTerritoryType.ALREADYREACHED.getNumber());
						st.addSnatchPlayer(sp, sp.getCamp() == CampType.ATT.value());
						SnatchTerritoryService.getInstance().addSnatchPlayerCommand(sp, player, true);
						MessageUtils.sendPrompt(player, PromptType.ERROR, 1360);
					} else if (sp.getStatus() == SnatchTerritoryType.RETURN_VALUE) {
						// sp.setStatus(SnatchTerritoryType.NOACTION.getNumber());
						sp.setTerritroyId(-1);
						// 删除所在道馆里的数据
						SnatchTerritoryService.getInstance().removeSnatchPlayerByGym(gymId, sp);
						SnatchTerritoryService.getInstance().removeSnatchPlayer(playerId, player, false);
						MessageUtils.sendPrompt(player, PromptType.ERROR, 1359);
					}
					SnatchTerritoryService.getInstance().notifyOneGymClientAll(st);
					returnBuilder.setClose(true);
					MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
							returnBuilder.build().toByteArray()));
					// 更新任务
					player.getTaskManager().updateTaskCondition(false, TaskConditionType.ONE_KEY_ARRIVE, 1);

					// 玩家操作日志
					LogService.getInstance().logPlayerAction(player, ReqAutoArriveID.ReqAutoArriveMsgID_VALUE, gymId,
							number);
				}
			} else {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1359);
				returnBuilder.setClose(true);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
			}
		} else if (commond == 2) {
			// 取消派遣
			SnatchPlayer sp = null;
			SnatchTerritory st = SnatchTerritoryService.getInstance().getTerritoryMap().get(gymId);
			if (null == st) {
				sp = player.getSnatchTerritoryManager().getSnatchPlayerById(playerId);
				returnBuilder.setClose(false);
				if (null != sp) {
					if (sp.getStatus() == SnatchTerritoryType.RETURN_VALUE) {
						//MessageUtils.sendPrompt(player, PromptType.ERROR, 1346);
						returnBuilder.setClose(true);
						MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
								returnBuilder.build().toByteArray()));
						return null;
					}
					// 删除之前的任务
					DelayTaskProcessor.getInstance().removeCommand("JoinSnatchPlayer_" + sp.getPlayerId());
					DelayTaskProcessor.getInstance().removeCommand("RunSnatchPlayer_" + sp.getPlayerId());
					sp.setCencelArriveTime();
					sp.setStatus(SnatchTerritoryType.RETURN.getNumber());
					SnatchTerritoryService.getInstance().addSnatchPlayerCommand(sp, player, false);
					player.getSnatchTerritoryManager().addSnatchPlayerToGym(sp.getTerritroyId(), sp);
					player.getSnatchTerritoryManager().changeSnatchTerritoryStatusById(sp.getTerritroyId());

					// st =
					// player.getSnatchTerritoryManager().getSnatchTerritoryById(gymId);
					// if (null == st) {
					// SnatchTerritoryService.getInstance().addSnatchPlayerCommand(sp,
					// player, true);
					// } else {
					// SnatchTerritoryService.getInstance().addSnatchPlayerCommand(sp,
					// player, true);
					// }

					returnBuilder.setClose(true);
					MessageUtils.sendPrompt(player, PromptType.ERROR, 1372);
				} else {
					MessageUtils.sendPrompt(player, PromptType.ERROR, 1359);
					returnBuilder.setClose(true);
				}
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				// 玩家操作日志
				LogService.getInstance().logPlayerAction(player, ReqCancelAttackID.ReqCancelAttackMsgID_VALUE, gymId);
				return null;
			}
			sp = st.getSnatchPlayerById(playerId);
			if (null != sp) {
				if (sp.getStatus() == SnatchTerritoryType.RETURN_VALUE) {
					returnBuilder.setClose(true);
					MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
							returnBuilder.build().toByteArray()));
					return null;
				}
				// 删除之前的任务
				DelayTaskProcessor.getInstance().removeCommand("JoinSnatchPlayer_" + sp.getPlayerId());
				DelayTaskProcessor.getInstance().removeCommand("RunSnatchPlayer_" + sp.getPlayerId());
				sp.setCencelArriveTime();
				sp.setStatus(SnatchTerritoryType.RETURN.getNumber());
				st.addSnatchPlayer(sp, sp.getCamp() == CampType.ATT.value());

				SnatchTerritoryService.getInstance().addSnatchPlayerCommand(sp, player, false);

				SnatchTerritoryService.getInstance().notifyOneGymClientAll(st);
				returnBuilder.setClose(true);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1372);

				// 玩家操作日志
				LogService.getInstance().logPlayerAction(player, ReqCancelAttackID.ReqCancelAttackMsgID_VALUE, gymId);
			} else {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1346);
				returnBuilder.setClose(true);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
			}
		}
		return null;
	}

}
