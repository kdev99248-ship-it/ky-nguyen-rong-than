package logic.snatchTerritory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Message.C2SSnatchTerritoryMsg.ReqDefaultFormationUpID;
import Message.S2CHeroMsg.PropertyMsg;
import Message.S2CPlayerMsg.PromptType;
import Message.S2CSnatchTerritoryMsg.AddPropertyInfo;
import Message.S2CSnatchTerritoryMsg.RspFormationInfoID;
import Message.S2CSnatchTerritoryMsg.RspNullID;
import Message.S2CSnatchTerritoryMsg.RspNullMsg;
import Message.S2CSnatchTerritoryMsg.SnatchTerritoryStatus;
import game.core.pub.script.IScript;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.snatchTerritory.SnatchTerritoryService;
import game.server.logic.snatchTerritory.bean.SnatchPlayer;
import game.server.logic.snatchTerritory.bean.SnatchTerritory;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 请求更换获得阵型
 */
public class RspSetGymFormationScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@SuppressWarnings("unchecked")
	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		Player player = (Player) script.get(ScriptArgs.Key.PLAYER);
		int gymId = (int) script.get(ScriptArgs.Key.ARG1);
		boolean lineUp = (boolean) script.get(ScriptArgs.Key.ARG2);
		Message.S2CSnatchTerritoryMsg.FormationInfo.Builder builder = Message.S2CSnatchTerritoryMsg.FormationInfo
				.newBuilder();
		SnatchPlayer sp = null;
		if (lineUp) {
			List<Integer> formation = (List<Integer>) script.get(ScriptArgs.Key.ARG3);
			formation = new ArrayList<Integer>(formation);
			if (formation.size() < 6) {
				for (int i = formation.size(); i < 6; i++) {
					formation.add(0);
				}
			}
			List<Integer> extFormation = (List<Integer>) script.get(ScriptArgs.Key.ARG4);
			extFormation = new ArrayList<Integer>(extFormation);
			formation.addAll(extFormation);
			int[] ints = formation.stream().mapToInt(e -> e).toArray();
			// 设置阵型
			player.getSnatchTerritoryManager().setFormation(gymId, ints);
			sp = new SnatchPlayer();
			sp.setTerritroyId(gymId);
			sp.setFormation(player, ints);

		} else {
			// int[] formation =
			// player.getSnatchTerritoryManager().getFormations();

			int[] playerFormation = player.getFormationManager().getFormation();
			player.getSnatchTerritoryManager().setFormation(gymId, playerFormation);

			SnatchTerritory st = SnatchTerritoryService.getInstance().getTerritoryMap().get(gymId);
			if (null == st) {
				st = player.getSnatchTerritoryManager().getSnatchTerritoryById(gymId);
				if (null != st) {
					sp = st.getSnatchPlayerById(player.getPlayerId());
					if (st.getStatus() == SnatchTerritoryStatus.MONSTERDEAD_VALUE) {
						MessageUtils.sendPrompt(player, PromptType.ERROR, 1410);
						RspNullMsg.Builder returnBuilder = RspNullMsg.newBuilder();
						returnBuilder.setClose(true);
						MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
								returnBuilder.build().toByteArray()));
						return null;// 弹窗提示
					}
					if (null == sp) {
						sp = new SnatchPlayer();
						sp.setTerritroyId(gymId);
						sp.setFormation(player, player.getSnatchTerritoryManager().getFormations());
					}
				} else {
					// sp = new SnatchPlayer();
					// sp.setTerritroyId(gymId);
					// sp.setFormation(player,
					// player.getSnatchTerritoryManager().getFormations());
					RspNullMsg.Builder returnBuilder = RspNullMsg.newBuilder();
					MessageUtils.sendPrompt(player, PromptType.ERROR, 1355);
					returnBuilder.setClose(false);
					MessageUtils.send(player, player.getFactory().fetchSMessage(RspNullID.RspNullMsgID_VALUE,
							returnBuilder.build().toByteArray()));
					return null;
				}
			} else {
				sp = st.getSnatchPlayerById(player.getPlayerId());
				if (null == sp) {
					sp = new SnatchPlayer();
					sp.setTerritroyId(gymId);
					sp.setFormation(player, player.getSnatchTerritoryManager().getFormations());
				}
			}
		}
		builder.setGymId(gymId);
		if (null != sp) {
			builder.setPower(sp.getPower());
			builder.setExtPower(sp.getExtPower());
			List<Integer> ids = new ArrayList<Integer>();
			// 发送阵型
			int[] ints = sp.getFormation();
			for (int i = 0; i < ints.length; i++) {
				builder.addFormation(ints[i]);
				ids.add(ints[i]);
			}
			List<Integer> list = sp.getExtFormation();
			for (Integer id : list) {
				if (null == id)
					continue;
				builder.addExtFormation(id);
				ids.add(id);
			}
			// 发送添加的属性
			Map<Integer, Map<Integer, Integer>> map = sp.getHeroAdd();
			for (Integer key : map.keySet()) {
				if (null == key)
					continue;
				if (!ids.contains(key))
					continue;
				AddPropertyInfo.Builder addBuilder = AddPropertyInfo.newBuilder();
				addBuilder.setHeroId(key);
				Map<Integer, Integer> propertyMap = map.get(key);
				for (Integer proId : propertyMap.keySet()) {
					if (null == proId)
						continue;
					PropertyMsg.Builder proBuilder = PropertyMsg.newBuilder();
					proBuilder.setId(proId);
					proBuilder.setNum(propertyMap.get(proId));
					addBuilder.addProperty(proBuilder);
				}
				builder.addAddPropertyInfo(addBuilder);
			}
			MessageUtils.send(player.getSession(), player.getFactory()
					.fetchSMessage(RspFormationInfoID.RspFormationInfoMsgID_VALUE, builder.build().toByteArray()));

			// 玩家操作日志
			LogService.getInstance().logPlayerAction(player, ReqDefaultFormationUpID.ReqDefaultFormationUpMsgID_VALUE);
		}
		return null;
	}

}
