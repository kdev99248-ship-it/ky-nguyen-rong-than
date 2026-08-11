package logic.snatchTerritory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Message.C2SSnatchTerritoryMsg.ReqAuctionGymLeaderID;
import Message.S2CPlayerMsg.PromptType;
import Message.S2CSnatchTerritoryMsg.Auctioner;
import Message.S2CSnatchTerritoryMsg.RspAuctioneID;
import Message.S2CSnatchTerritoryMsg.RspAuctioneMsg;
import game.core.pub.script.IScript;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.player.RoleView;
import game.server.logic.player.RoleViewService;
import game.server.logic.snatchTerritory.SnatchTerritoryService;
import game.server.logic.snatchTerritory.bean.SnatchTerritory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

//查看竞拍详情
public class RspAuctionGymLeaderScript implements IScript {
	private static Logger logger = LoggerFactory.getLogger(RspAuctionGymLeaderScript.class);
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
		String str = BeanTemplet.getGlobalBean(292).getStr_value();
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
			if (time <= startTime) {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1363);
				return null;
			}
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
			if (time >= endTime) {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 1364);
				return null;
			}
			SnatchTerritory st = SnatchTerritoryService.getInstance().getTerritoryMap().get(gymId);
			if (null != st) {
				int rank = 1;
				RspAuctioneMsg.Builder builder = RspAuctioneMsg.newBuilder();
				builder.setGymId(gymId);
				Map<Long, Integer> costMap = st.getAuctionCost();
				if(null == costMap){
					costMap = new ConcurrentHashMap<Long, Integer>();
					st.setAuctionCost(costMap);
				}
				// 操作完 然后排一次序
				List<Map.Entry<Long, Integer>> list = new ArrayList<Map.Entry<Long, Integer>>(costMap.entrySet());
				Collections.sort(list, (p1, p2) -> p2.getValue().compareTo(p1.getValue()));
				logger.info("排序竞拍列表.............");

				for (Entry<Long, Integer> ent : list) {
					if (null == ent)
						continue;
					long playerId = ent.getKey();
					Auctioner.Builder erBuilder = Auctioner.newBuilder();
					erBuilder.setRank(rank);
					erBuilder.setId(playerId);
					erBuilder.setCost(costMap.get(playerId));
					RoleView role = RoleViewService.getRoleById(playerId);
					erBuilder.setName(null == role ? "" : role.getName());
					erBuilder.setPower(null == role ? 0l : role.getPower());
					erBuilder.setVipLevel(null == role ? 0 : role.getVipLevel());
					erBuilder.setIsDoubleMoonCard(null == role ? false : role.isDoubleMoonCard());
					builder.addRanks(erBuilder);
					if (player.getPlayerId() == playerId) {
						builder.setMine(erBuilder);
					}
					rank++;
				}
				if (!builder.hasMine()) {
					Auctioner.Builder erBuilder = Auctioner.newBuilder();
					erBuilder.setRank(-1);
					erBuilder.setId(player.getPlayerId());
					erBuilder
							.setCost(null == costMap.get(player.getPlayerId()) ? 0 : costMap.get(player.getPlayerId()));
					RoleView role = RoleViewService.getRoleById(player.getPlayerId());
					erBuilder.setName(null == role ? "" : role.getName());
					erBuilder.setPower(null == role ? 0l : role.getPower());
					erBuilder.setVipLevel(null == role ? 0 : role.getVipLevel());
					erBuilder.setIsDoubleMoonCard(null == role ? false : role.isDoubleMoonCard());
					builder.setMine(erBuilder);
				}
				builder.setOverTime(endTime);
				MessageUtils.send(player, player.getFactory().fetchSMessage(RspAuctioneID.RspAuctioneMsgID_VALUE,
						builder.build().toByteArray()));

				// 玩家操作日志
				LogService.getInstance().logPlayerAction(player, ReqAuctionGymLeaderID.ReqAuctionGymLeaderMsgID_VALUE,
						gymId);

			}
		}

		return null;
	}

}
