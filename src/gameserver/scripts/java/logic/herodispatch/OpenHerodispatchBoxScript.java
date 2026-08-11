package logic.herodispatch;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Message.C2SHerodispatchMsg.OpenHerodispatchBoxReq;
import Message.C2SHerodispatchMsg.OpenHerodispatchBoxReqID;
import Message.S2CHerodispatchMsg.OpenHerodispatchBoxRsp;
import Message.S2CHerodispatchMsg.OpenHerodispatchBoxRspID;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.backpack.BackpackService;
import game.server.logic.constant.Reason;
import game.server.logic.guild.GuildService;
import game.server.logic.herodispatch.HerodispatchService;
import game.server.logic.herodispatch.bean.HerodispatchBox;
import game.server.logic.herodispatch.bean.HerodispatchBoxConfig;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 完成派遣
 * @author tzyx
 *
 */
public class OpenHerodispatchBoxScript implements IScript{

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void init() {
		
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs args = (ScriptArgs) arg;
		Player player = (Player) args.get(Key.PLAYER);
		OpenHerodispatchBoxReq req = (OpenHerodispatchBoxReq) args.get(Key.ARG1);
		reqCompleteHerodispatch(player, req);
		return null;
	}

	public void reqCompleteHerodispatch(Player player, OpenHerodispatchBoxReq req) {
		HerodispatchService service = HerodispatchService.getInstance();
		// 检查功能开放
		if (service.herodispatchFunctionOpen(player, true)) 
			return;
		// 检查箱子有效性
		int index = req.getIndex();
		List<HerodispatchBoxConfig> boxConfigList = service.getBoxConfigList();
		if (index >= boxConfigList.size()) {
			logger.info("不存在的宝箱");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5116);
			return;
		}
		HerodispatchBoxConfig config = boxConfigList.get(index);
		Long guildId = GuildService.getInstance().getPlayerGuildId(player.getPlayerId());
		int score = service.getScoreMap().get(guildId);
		if (score < config.getNeedScore()) {
			logger.info("积分不足,不能开启宝箱");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5114);
			return;
		}
		List<HerodispatchBox> boxList = service.getBoxDataByPlayer(player.getPlayerId());
		HerodispatchBox box = boxList.get(index);
		if (box.getGotIdList().contains(player.getPlayerId())) {
			logger.info("宝箱已领取");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 5115);
			return;
		}
		OpenHerodispatchBoxRsp.Builder builder = OpenHerodispatchBoxRsp.newBuilder();
		List<Item> addItems = new ArrayList<>(config.getRewardList());
		player.getBackpackManager().addItems(addItems, Reason.HERODISPATCH_BOX, "");
		// 添加玩家到已领取
		box.getGotIdList().add(player.getPlayerId());
		// 推送给玩家
		builder.setBox(box.genBuilder(player, score, boxConfigList, index, box));
		BackpackService.getInstance().getRewardNotify(player, addItems);
		MessageUtils.send(player, player.getFactory().fetchSMessage(OpenHerodispatchBoxRspID.OpenHerodispatchBoxRspMsgID_VALUE,
				builder.build().toByteArray()));
		// 记录动作    箱子索引,获取道具内容
		LogService.getInstance().logPlayerAction(player, OpenHerodispatchBoxReqID.OpenHerodispatchBoxReqMsgID_VALUE,
				index, addItems);
	}
}
