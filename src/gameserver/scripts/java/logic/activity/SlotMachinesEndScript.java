package logic.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.SlotMachines;
import game.server.logic.activity.bean.SlotMachinesBoxConfig;
import game.server.logic.activity.bean.SlotMachinesConfig;
import game.server.logic.activity.bean.SlotMachinesRankConfig;
import game.server.logic.constant.Reason;
import game.server.logic.mail.MailService;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;

/**
 * 探险训练家积分排序
 * 
 * @author tzyx
 *
 */
public class SlotMachinesEndScript implements IScript {

	private static Logger LOGGER = Logger.getLogger(SlotMachinesEndScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		// 开始结算 关闭数据改动
		ActivityService.getInstance().setSmCheckEnd(true);
		slotMachinesEnd();
		// 结算完成
		ActivityService.getInstance().setSmCheckEnd(false);
		return null;
	}

	private void slotMachinesEnd() {
		ActivityService service = ActivityService.getInstance();
		SlotMachinesConfig config = service.getSlotMachinesConfig();
		Map<Long, Integer> slotMachinesRankMap = service.getSlotMachinesRankMap();
		for (Entry<Long, Integer> en : slotMachinesRankMap.entrySet()) {
			Player player = PlayerManager.getOffLinePlayerByPlayerId(en.getKey());
			int index = getRank(en.getValue() + 1);
			if (index != -1) {
				SlotMachinesRankConfig slotMachinesRankConfig = service.getSlotMachinesConfig().getRankConfigList()
						.get(index);
				sendRankMail(player, slotMachinesRankConfig, en.getValue() + 1,
						service.getSlotMachinesData().get(player.getPlayerId()).getScore());
			}
		}
		// 检查补发宝箱
		for (SlotMachines sm : service.getSlotMachinesData().values()) {
			Map<Integer, Integer> adjunctMap = new HashMap<>();// 附件(奖励列表)
			for (int i = 0; i < sm.getBoxes().size(); i++) {
				// 需要补发
				if (sm.getBoxes().get(i) == 1) {
					SlotMachinesBoxConfig boxConfig = config.getBoxConfigList().get(i);
					for (int j = 0; j < boxConfig.getIds().length; j++) {
						if (adjunctMap.containsKey(boxConfig.getIds()[j])) {
							adjunctMap.put(boxConfig.getIds()[j],
									adjunctMap.get(boxConfig.getIds()[j]) + boxConfig.getNums()[j]);
						} else {
							adjunctMap.put(boxConfig.getIds()[j], boxConfig.getNums()[j]);
						}
					}
				}
			}
			if (!adjunctMap.isEmpty()) {
				sendBoxMail(sm.getPlayerId(), adjunctMap);
			}
		}
		LOGGER.info("探险训练家结算,最后剩余奖池金额:" + service.getJackpot());
		service.setSlotMachinesConfig(null);
		service.getSlotMachinesData().clear();
		service.getSlotMachinesHistory().clear();
		service.setJackpot(0);
		service.getSlotMachinesRankList().clear();
		service.getSlotMachinesRankMap().clear();
	}

	private void sendBoxMail(long playerId, Map<Integer, Integer> adjunctMap) {
		LOGGER.info("探险训练家补发宝箱奖励,playerId[" + playerId + "]");
		MailService.getInstance().sendSysMail2Player(playerId, 61, null, Reason.SLOT_MACHINES_BOX_MAIL, "", null,
				System.currentTimeMillis(), adjunctMap);
	}

	private void sendRankMail(Player player, SlotMachinesRankConfig config, int rank, int score) {
		Map<Integer, Integer> adjunctMap = new HashMap<>();// 附件(奖励列表)
		for (int j = 0; j < config.getIds().length; j++) {
			if (adjunctMap.containsKey(config.getIds()[j])) {
				adjunctMap.put(config.getIds()[j], adjunctMap.get(config.getIds()[j]) + config.getNums()[j]);
			} else {
				adjunctMap.put(config.getIds()[j], config.getNums()[j]);
			}
		}
		LOGGER.info("探险训练家发放第[" + rank + "]名,playerId[" + player.getPlayerId() + "]奖励");
		List<String> contentParams = new ArrayList<>();
		contentParams.add(String.valueOf(score));
		contentParams.add(String.valueOf(rank));
		MailService.getInstance().sendSysMail2Player(player.getPlayerId(), 60, contentParams, Reason.SLOT_MACHINES_RANK,
				"", null, System.currentTimeMillis(), adjunctMap);
	}

	private int getRank(int rank) {
		ActivityService service = ActivityService.getInstance();
		List<SlotMachinesRankConfig> rankConfigList = service.getSlotMachinesConfig().getRankConfigList();
		for (int i = 0; i < rankConfigList.size(); i++) {
			SlotMachinesRankConfig slotMachinesRankConfig = rankConfigList.get(i);
			if (slotMachinesRankConfig.getStartRank() <= rank && slotMachinesRankConfig.getEndRank() >= rank) {
				return i;
			}
		}
		return -1;
	}
}
