package logic.snatchTerritory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import Message.C2SSnatchTerritoryMsg.GymReqType;
import Message.C2SSnatchTerritoryMsg.ReqAttackOrDefendID;
import Message.C2SSnatchTerritoryMsg.ReqAttendGymID;
import Message.S2CPlayerMsg.PromptType;
import Message.S2CSnatchTerritoryMsg.RspNullID;
import Message.S2CSnatchTerritoryMsg.RspNullMsg;
import Message.S2CSnatchTerritoryMsg.SnatchTerritoryStatus;
import Message.S2CSnatchTerritoryMsg.SnatchTerritoryType;
import data.bean.t_itemBean;
import data.bean.t_roadPavilionBean;
import data.bean.t_wildMonsterBean;
import game.core.pub.script.IScript;
import game.fight.bean.CampType;
import game.server.logic.chat.service.ChatService;
import game.server.logic.constant.GlobalID;
import game.server.logic.constant.ModelMsg;
import game.server.logic.constant.Reason;
import game.server.logic.constant.TaskConditionType;
import game.server.logic.global.GameGlobalService;
import game.server.logic.guild.GuildService;
import game.server.logic.guild.bean.Guild;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.snatchTerritory.SnatchTerritoryService;
import game.server.logic.snatchTerritory.bean.SnatchPlayer;
import game.server.logic.snatchTerritory.bean.SnatchTerritory;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

public class JoinSnatchTerritoryScript implements IScript {

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
		int type = (int) script.get(ScriptArgs.Key.ARG1);
		int targetId = (int) script.get(ScriptArgs.Key.ARG2);
		switch (type) {
		case GymReqType.DECLAREWAR_VALUE:
			join(player, targetId);
			break;
		case GymReqType.ATTACK_VALUE:
			int[] format = player.getSnatchTerritoryManager().getFormations();
			attack(player, targetId, format);
			break;

		default:
			break;
		}
		return null;
	}

	// 玩家参与 发起宣战
	private void join(Player player, int targetId) {
		RspNullMsg.Builder returnBuilder = RspNullMsg.newBuilder();
		SnatchTerritoryService instence = SnatchTerritoryService.getInstance();
		Map<Integer, SnatchTerritory> territoryMap = instence.getTerritoryMap();// 道馆信息
		boolean ranfor = false;
		String str = BeanTemplet.getGlobalBean(304).getStr_value();// 竞拍馆主禁止宣战时间
		if (str.split("\\+").length > 1) {
			String start = str.split("\\+")[0];
			String end = str.split("\\+")[1];
			long time = System.currentTimeMillis();
			Calendar cal = Calendar.getInstance();
			if (start.indexOf(".") != -1) {
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(start.split("\\.")[0]));
				cal.set(Calendar.MINUTE, Integer.parseInt(start.split("\\.")[1]));
				cal.set(Calendar.SECOND, 0);
			} else {
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(start));
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
			}
			long startTime = cal.getTimeInMillis();
			if (end.indexOf(".") != -1) {
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(end.split("\\.")[0]));
				cal.set(Calendar.MINUTE, Integer.parseInt(end.split("\\.")[1]));
				cal.set(Calendar.SECOND, 0);
			} else {
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(end));
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
			}
			long endTime = cal.getTimeInMillis();
			if (time >= startTime && time < endTime) {
				ranfor = true;
			}
		}
		if (ranfor) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 1345);
			returnBuilder.setClose(false);
			MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
					returnBuilder.build().toByteArray()));
			return;
		}
		SnatchTerritory scTerritory = territoryMap.get(targetId);

		if (null == scTerritory) {
			scTerritory = player.getSnatchTerritoryManager().getSnatchTerritoryById(targetId);
			if (null == scTerritory) {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1346);
				returnBuilder.setClose(false);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				return;// 道馆不存在 弹窗提示
			}
		}
		if (0 != scTerritory.getOffenseGuildId()) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 1347);
			returnBuilder.setClose(false);
			MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
					returnBuilder.build().toByteArray()));
			return;// 已经被宣战 不能再次宣战
		}
		Guild guild = GuildService.getInstance().getPlayerGuild(player.getPlayerId());
		if (null == guild) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 1348);
			returnBuilder.setClose(false);
			MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
					returnBuilder.build().toByteArray()));
			return;// 没有加入道馆 弹窗提示
		}
		if (guild.getId() == scTerritory.getDefendGuildId()) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 1349);
			returnBuilder.setClose(false);
			MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
					returnBuilder.build().toByteArray()));
			return;// 没有加入道馆 弹窗提示
		}
		t_roadPavilionBean bean = BeanTemplet.getRoadPavilionBean(targetId);
		String[] strs = null;
		if (null == bean) {
			t_wildMonsterBean mosterBean = BeanTemplet.getWildMonsterBean(targetId / 1000);
			if (null == mosterBean) {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1346);
				returnBuilder.setClose(false);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				return;// 弹窗提示
			}
			// 无消耗？！
			SnatchPlayer scPlayer = player.getSnatchTerritoryManager().getSnatchPlayerById(player.getPlayerId());
			if (null != scPlayer) {
				returnBuilder.setClose(false);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				return;// 已经在当前道馆进攻或者防守中 不能发起宣战
			}
			scTerritory.join(guild.getId(), player.getPlayerId());
		} else {
			// 计算开放时间
			long openTime = Long
					.valueOf(GameGlobalService.getInstance().findById(GlobalID.OPENSERVER_TIME.getValue()).getStrVal());
			t_roadPavilionBean t_bean = BeanTemplet.getRoadPavilionBean(bean.getId());
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(openTime);
			if (null != t_bean)
				cal.add(Calendar.DAY_OF_YEAR, t_bean.getOpening_time() - 1);
			cal.set(Calendar.HOUR_OF_DAY, 5);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);

			if (cal.getTimeInMillis() > System.currentTimeMillis()) {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1376);
				returnBuilder.setClose(false);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				return;
			}
			strs = bean.getDeclareWar_consume().split(";");
			List<Item> items = new ArrayList<>();
			for (int i = 0; i < strs.length; i++) {
				int id = Integer.parseInt(strs[i].split("_")[0]);
				int num = Integer.parseInt(strs[i].split("_")[1]);
				items.addAll(BeanFactory.createProps(id, num));
			}
			// 计算消耗
			if (!player.getBackpackManager().isItemNumEnough(items)) {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1350);
				returnBuilder.setClose(false);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				return;// 弹窗提示
			}
			if (scTerritory.getProtectedTime() > System.currentTimeMillis()) {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1351);
				returnBuilder.setClose(false);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				return;// 还在保护时间内 不能发起宣战
			}
			SnatchPlayer scPlayer = scTerritory.getSnatchPlayerById(player.getPlayerId());
			if (null != scPlayer && scPlayer.getTerritroyId() != -1) {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1352);
				returnBuilder.setClose(false);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				return;// 已经在当前道馆进攻或者防守中 不能发起宣战
			}
			if (scTerritory.getDefendGuildId() != 0 && scTerritory.getOffenseGuildId() != 0
					&& !(scTerritory.getOffenseGuildId() == guild.getId()
							|| scTerritory.getDefendGuildId() == guild.getId())) {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1353);
				returnBuilder.setClose(false);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				return;// 当前道馆即将发生战斗 不是自己联盟 不能宣战
			}
			player.getBackpackManager().removeItems(items, true, Reason.GYM_REQATTACK, "进行道馆宣战");
			scTerritory.join(guild.getId(), player.getPlayerId());
			instence.notifyOneGymClientAll(scTerritory);// 推送
			returnBuilder.setClose(true);
			MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
					returnBuilder.build().toByteArray()));
			// 发消息
			Guild g = GuildService.getInstance().getGuilds().get(scTerritory.getDefendGuildId());
			String name = null;
			if (null == g) {
				name = bean.getAscription_NPC();
			} else {
				name = g.getName();
			}
			ChatService.getInstance().sendGuildSysMessage(guild.getId(), ModelMsg.GYM_ATTEND_CHAT.value(),
					player.getPlayerName(), name, scTerritory.getMonsterName());

			if (GuildService.getInstance().getGuilds().containsKey(scTerritory.getDefendGuildId())) {
				ChatService.getInstance().sendGuildSysMessage(scTerritory.getDefendGuildId(),
						ModelMsg.GYM_BEATTEND_CHAT.value(), scTerritory.getMonsterName(), guild.getName(),
						player.getPlayerName());
			}

			if (bean.getType() == 1) {
				// 更新任务
				player.getTaskManager().updateTaskCondition(false, TaskConditionType.ATE_GYM_NOR, 1);
			} else if (bean.getType() == 2) {
				// 更新任务
				player.getTaskManager().updateTaskCondition(false, TaskConditionType.ATE_GYM_SER, 1);
			} else if (bean.getType() == 3) {
				// 更新任务
				player.getTaskManager().updateTaskCondition(false, TaskConditionType.ATE_GYM_HEI, 1);
			}
			// 更新任务
			player.getTaskManager().updateTaskCondition(false, TaskConditionType.ATE_GYM_ALL, 1);

			// 玩家操作日志
			LogService.getInstance().logPlayerAction(player, ReqAttendGymID.ReqAttendGymMsgID_VALUE, targetId);
		}
	}

	/** 进攻 */
	private void attack(Player player, int targetId, int[] formation) {
		SnatchTerritoryService instence = SnatchTerritoryService.getInstance();
		Map<Integer, SnatchTerritory> territoryMap = instence.getTerritoryMap();// 道馆信息
		RspNullMsg.Builder returnBuilder = RspNullMsg.newBuilder();
		Guild guild = GuildService.getInstance().getPlayerGuild(player.getPlayerId());
		if (null == guild) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 1348);
			returnBuilder.setClose(false);
			MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
					returnBuilder.build().toByteArray()));
			return;// 没有加入公会 弹窗提示
		}
		boolean ranfor = false;
		String str = BeanTemplet.getGlobalBean(304).getStr_value();// 竞拍馆主禁止宣战时间
		if (str.split("\\+").length > 1) {
			String start = str.split("\\+")[0];
			String end = str.split("\\+")[1];
			long time = System.currentTimeMillis();
			Calendar cal = Calendar.getInstance();
			if (start.indexOf(".") != -1) {
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(start.split("\\.")[0]));
				cal.set(Calendar.MINUTE, Integer.parseInt(start.split("\\.")[1]));
				cal.set(Calendar.SECOND, 0);
			} else {
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(start));
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
			}
			long startTime = cal.getTimeInMillis();
			if (end.indexOf(".") != -1) {
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(end.split("\\.")[0]));
				cal.set(Calendar.MINUTE, Integer.parseInt(end.split("\\.")[1]));
				cal.set(Calendar.SECOND, 0);
			} else {
				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(end));
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
			}
			long endTime = cal.getTimeInMillis();
			if (time >= startTime && time < endTime) {
				ranfor = true;
			}
		}

		Map<Long, SnatchPlayer> snatchPlayerMap = instence.getSnatchPlayerMap();// 所有进攻玩家信息
		t_roadPavilionBean bean = BeanTemplet.getRoadPavilionBean(targetId);
		// 检查玩家在所有道馆的战争状态
		if (snatchPlayerMap.containsKey(player.getPlayerId())) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 1356);
			returnBuilder.setClose(false);
			MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
					returnBuilder.build().toByteArray()));
			return;// 已经在当前道馆进攻或者防守中 不能发起宣战
		}

		if (null == bean) {
			// 进攻野生怪物
			t_wildMonsterBean mosterBean = BeanTemplet.getWildMonsterBean(targetId / 1000);
			if (null == mosterBean) {
				returnBuilder.setClose(false);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1346);
				return;// 弹窗提示
			}
			SnatchTerritory scTerritory = player.getSnatchTerritoryManager().getSnatchTerritoryById(targetId);
			if (null == scTerritory) {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1355);
				returnBuilder.setClose(false);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				return;// 弹窗提示
			}
			if (scTerritory.getStatus() == SnatchTerritoryStatus.MONSTERDEAD_VALUE) {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1410);
				returnBuilder.setClose(false);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				return;// 弹窗提示
			}
			SnatchPlayer sp = snatchPlayerMap.get(player.getPlayerId());
			if (sp != null) {
				if (sp.getTerritroyId() != targetId && (sp.getStatus() == SnatchTerritoryType.ATTACK_VALUE
						|| sp.getStatus() == SnatchTerritoryType.RETURN_VALUE)) {
					// && sp.getStatus() !=
					// SnatchTerritoryType.ALREADYREACHED_VALUE
					MessageUtils.sendPrompt(player, PromptType.ERROR, 1356);
					returnBuilder.setClose(false);
					MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
							returnBuilder.build().toByteArray()));
					return;// 已经在进攻其他道馆
				}
			}
			// sp =
			// player.getSnatchTerritoryManager().getSnatchPlayerById(player.getPlayerId());
			// if (null != sp && sp.getTerritroyId() != -1) {
			// MessageUtils.sendPrompt(player, PromptType.ERROR, 1357);
			// returnBuilder.setClose(false);
			// MessageUtils.send(player,
			// player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
			// returnBuilder.build().toByteArray()));
			// return;
			// }
			int airShioId = player.getSnatchTerritoryManager().getAirShipId();
			t_itemBean itembean = BeanTemplet.getItemBean(airShioId);
			int deductTime = 0;
			if (null != itembean) {
				deductTime = itembean.getInt_param();
			}
			SnatchPlayer scPlayer = new SnatchPlayer(player.getPlayerId(), player.getLevel(), airShioId,
					SnatchTerritoryType.ATTACK.getNumber(), targetId, guild.getId(), System.currentTimeMillis()
							+ (mosterBean.getGo_time() - (int) (mosterBean.getGo_time() * deductTime / 100f)) * 1000);
			scPlayer.setFormation(player, formation);// 阵型

			// 加到进攻方
			scPlayer.setCamp(CampType.ATT.value());
			scTerritory.join(guild.getId(), player.getPlayerId());// 宣战中
			instence.addSnatchPlayerCommand(scPlayer, player, false); // 添加完成后会通知客户端刷新数据
			player.getSnatchTerritoryManager().addSnatchPlayerToGym(scPlayer.getTerritroyId(), scPlayer);
			returnBuilder.setClose(true);
			MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
					returnBuilder.build().toByteArray()));

			// 玩家操作日志
			LogService.getInstance().logPlayerAction(player, ReqAttackOrDefendID.ReqAttackOrDefendMsgID_VALUE,
					targetId);
		} else {
			if (ranfor) {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1354);
				returnBuilder.setClose(false);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				return;
			}
			// 进攻道馆
			SnatchTerritory scTerritory = territoryMap.get(targetId);
			if (null == scTerritory) {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1346);
				returnBuilder.setClose(false);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				return;// 道馆不存在 弹窗提示
			}
			SnatchPlayer sp = snatchPlayerMap.get(player.getPlayerId());
			if (sp != null) {
				if (sp.getTerritroyId() != targetId || sp.getTerritroyId() != targetId) {
					MessageUtils.sendPrompt(player, PromptType.ERROR, 1356);
					returnBuilder.setClose(false);
					MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
							returnBuilder.build().toByteArray()));
					return;// 已经在进攻其他道馆
				}
			}

			if (!(guild.getId() == scTerritory.getOffenseGuildId()
					|| guild.getId() == scTerritory.getDefendGuildId())) {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1358);
				returnBuilder.setClose(false);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				return;// 不是自己公会宣战道馆 不能进行挑战
			}
			int airShioId = player.getSnatchTerritoryManager().getAirShipId();
			t_itemBean itembean = BeanTemplet.getItemBean(airShioId);
			int deductTime = 0;
			if (null != itembean) {
				deductTime = itembean.getInt_param();
			}
			SnatchPlayer scPlayer = scTerritory.getSnatchPlayerById(player.getPlayerId());
			if (null != scPlayer) {
				if (scPlayer.getStatus() == SnatchTerritoryType.ATTACK_VALUE
						|| scPlayer.getStatus() == SnatchTerritoryType.ALREADYREACHED_VALUE) {
					MessageUtils.sendPrompt(player, PromptType.ERROR, 1357);
					returnBuilder.setClose(false);
					MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
							returnBuilder.build().toByteArray()));
				} else if (scPlayer.getStatus() == SnatchTerritoryType.NOACTION_VALUE
						|| scPlayer.getStatus() == SnatchTerritoryType.DEFENSIVELY_VALUE) {
					scPlayer.setArriveTime(System.currentTimeMillis()
							+ (bean.getGo_time() - (int) (bean.getGo_time() * deductTime / 100f)) * 1000);
					scPlayer.setTerritroyId(scTerritory.getId());
					scPlayer.setFormation(player, formation);// 阵型
					scPlayer.setCamp(CampType.DEF.value());
					scPlayer.setStatus(SnatchTerritoryType.ATTACK.getNumber());
					scTerritory.addSnatchPlayer(scPlayer, false);
					instence.addSnatchPlayerCommand(scPlayer, player, true);
					instence.notifyOneGymClientAll(scTerritory);// 推送
					returnBuilder.setClose(true);
					MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
							returnBuilder.build().toByteArray()));
					// 更新任务
					player.getTaskManager().updateTaskCondition(false, TaskConditionType.DISPATCH_GYM, 1);

					// 玩家操作日志
					LogService.getInstance().logPlayerAction(player, ReqAttackOrDefendID.ReqAttackOrDefendMsgID_VALUE,
							targetId);
				} else if (scPlayer.getStatus() == SnatchTerritoryType.RETURN_VALUE) {
					MessageUtils.sendPrompt(player, PromptType.ERROR, 1356);
					returnBuilder.setClose(false);
					MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
							returnBuilder.build().toByteArray()));
				}
				returnBuilder.setClose(true);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
						returnBuilder.build().toByteArray()));
				return;// 已经在当前道馆防守中 计算倒计时
			}
			scPlayer = new SnatchPlayer(player.getPlayerId(), player.getLevel(), airShioId,
					SnatchTerritoryType.ATTACK.getNumber(), targetId, guild.getId(), System.currentTimeMillis()
							+ (bean.getGo_time() - (int) (bean.getGo_time() * deductTime / 100f)) * 1000);
			scPlayer.setFormation(player, formation);// 阵型
			if (guild.getId() == scTerritory.getOffenseGuildId()) {
				// 加到进攻方
				scPlayer.setCamp(CampType.ATT.value());
				scTerritory.addSnatchPlayer(scPlayer, true);
			} else {
				// 加到防守方
				scPlayer.setCamp(CampType.DEF.value());
				scTerritory.addSnatchPlayer(scPlayer, false);
			}
			instence.addSnatchPlayerCommand(scPlayer, player, true);
			instence.notifyOneGymClientAll(scTerritory);// 推送给所有玩家
			returnBuilder.setClose(true);
			MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
					returnBuilder.build().toByteArray()));

			// 玩家操作日志
			LogService.getInstance().logPlayerAction(player, ReqAttackOrDefendID.ReqAttackOrDefendMsgID_VALUE,
					targetId);
		}
		// 更新任务
		player.getTaskManager().updateTaskCondition(false, TaskConditionType.DISPATCH_GYM, 1);
	}

}
