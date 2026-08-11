package logic.herodispatch;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.S2CHerodispatchMsg.HerodispatchNotify;
import Message.S2CHerodispatchMsg.HerodispatchNotifyID;
import Message.S2CPlayerMsg.PromptType;
import game.core.pub.script.IScript;
import game.server.logic.guild.GuildService;
import game.server.logic.hero.bean.Hero;
import game.server.logic.herodispatch.HerodispatchManager;
import game.server.logic.herodispatch.HerodispatchService;
import game.server.logic.herodispatch.bean.Herodispatch;
import game.server.logic.herodispatch.bean.HerodispatchBox;
import game.server.logic.herodispatch.bean.HerodispatchBoxConfig;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

/**
 * 推送精灵派遣信息
 * @author tzyx
 *
 */
public class NotifyHerodispatchScript implements IScript{

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
		
		notifyHerodispatch(player);
		return null;
	}

	public void notifyHerodispatch(Player player) {
		HerodispatchService service = HerodispatchService.getInstance();
		service.initHerodispatch(player);
		if (service.herodispatchFunctionOpen(player, true)) 
			return;
		Long guildId = GuildService.getInstance().getPlayerGuildId(player.getPlayerId());
		// 派遣信息
		HerodispatchNotify.Builder builder = HerodispatchNotify.newBuilder();
		HerodispatchManager manager = player.getHerodispatchManager();
		List<Herodispatch> dispatchList = manager.getDispatchList();
		for (Herodispatch dis : dispatchList) {
			builder.addDispatchList(dis.genBuilder());
		}
		// 公会积分
		Map<Long, Integer> scoreMap = service.getScoreMap();
		int score = 0;
		if (scoreMap.containsKey(guildId)) {
			score = scoreMap.get(guildId);
		}
		builder.setGuildScore(score);
		// 宝箱信息
		List<HerodispatchBoxConfig> boxConfigList = service.getBoxConfigList();
		List<HerodispatchBox> boxData = service.getBoxDataByPlayer(player.getPlayerId());
		if (null == boxData) {
			logger.info("玩家未加入联盟");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 1257);
			return;
		}
		for (int i = 0; i < boxData.size(); i++) {
			HerodispatchBox box = boxData.get(i);
			builder.addBoxList(box.genBuilder(player, score, boxConfigList, i, box));
		}
		Map<Integer, Hero> heroMap = player.getHeroManager().getHeroMap();
		List<Integer> usedHeroIdList = manager.getUsedHeroIdList();
		for (Integer id : heroMap.keySet()) {
			if (usedHeroIdList.contains(id)) {
				builder.addUsedHeros(id);
			} else {
				builder.addFreeHeros(id);
			}
		}
		if (usedHeroIdList.contains(player.getHeroManager().getLeader().getId())) {
			builder.addUsedHeros(player.getHeroManager().getLeader().getId());
		} else {
			builder.addFreeHeros(player.getHeroManager().getLeader().getId());
		}
		// 推送
		MessageUtils.send(player, player.getFactory().fetchSMessage(HerodispatchNotifyID.HerodispatchNotifyMsgID_VALUE,
				builder.build().toByteArray()));
	}
}
