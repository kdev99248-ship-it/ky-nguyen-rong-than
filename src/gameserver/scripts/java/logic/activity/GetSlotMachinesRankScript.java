package logic.activity;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.S2COperateActivityMsg.SlotMachinesRankInfo;
import Message.S2COperateActivityMsg.SlotMachinesRankInfo.Builder;
import Message.S2COperateActivityMsg.SlotMachinesRankRsp;
import Message.S2COperateActivityMsg.SlotMachinesRankRspID;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.SlotMachines;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 获取探险训练家排行榜
 * @author tzyx
 *
 */
public class GetSlotMachinesRankScript implements IScript {
	private static Logger LOGGER = Logger.getLogger(GetSlotMachinesRankScript.class);

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
		getSlotMachinesRank(player);
		return null;
	}

	private void getSlotMachinesRank(Player player) {
		ActivityService service = ActivityService.getInstance();
		if (service.getSlotMachinesConfig() == null || service.getSlotMachinesConfig().getOpen() == 0) {
			LOGGER.error("活动未开启");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5006);
			return;
		}
		List<Long> rankList = service.getSlotMachinesRankList();
		Map<Long, SlotMachines> slotMachinesData = service.getSlotMachinesData();
		SlotMachinesRankRsp.Builder builder = SlotMachinesRankRsp.newBuilder();
		int num = 1;
		int ownRank = 0;
		for (Long id : rankList) {
			// 超过50条不生成协议
			if (num <= 50) {
				SlotMachinesRankInfo.Builder b = slotMachinesData.get(id).genRankMsg(num);
				if (b != null) {
					builder.addRankList(b);
				}
			}
			// 玩家自己的排序
			if (id == player.getPlayerId()) {
				ownRank = num;
			}
			num++;
		}
		Builder ownMsg = null;
		if (ownRank != 0) {
			ownMsg = slotMachinesData.get(player.getPlayerId()).genRankMsg(ownRank);
		}
		if (ownMsg == null) {
			ownMsg = SlotMachinesRankInfo.newBuilder();
			ownMsg.setRank(0);
			ownMsg.setPlayerId(player.getPlayerId());
			ownMsg.setPlayerName(player.getPlayerName());
			ownMsg.setImage(player.getImage());
			ownMsg.setImageFrame(player.getImageFrame());
			ownMsg.setImgBg(player.getImgBg());
			ownMsg.setLevel(player.getLevel());
			ownMsg.setVipLevel(player.getVipLevel());
			ownMsg.setTrainLvl(player.getHeroManager().getTrainLvl());
			ownMsg.setScore(0);
		}
		
		builder.setOwnRank(ownMsg);
		MessageUtils.send(player, player.getFactory().fetchSMessage(SlotMachinesRankRspID.SlotMachinesRankRspMsgID_VALUE,
				builder.build().toByteArray()));
	}
}
