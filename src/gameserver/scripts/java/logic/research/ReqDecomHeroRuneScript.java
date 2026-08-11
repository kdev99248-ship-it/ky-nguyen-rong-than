package logic.research;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Message.C2SResearchMsg.DecomRuneReq;
import Message.S2CBackpackMsg.PropInfo;
import Message.S2CPlayerMsg.PromptType;
import Message.S2CResearchMsg.DecomRuneRsp;
import Message.S2CResearchMsg.DecomRuneRspID;
import data.bean.t_recyclingBean;
import data.bean.t_runeBean;
import game.core.pub.script.IScript;
import game.core.pub.script.ScriptManager;
import game.server.logic.constant.Reason;
import game.server.logic.constant.TaskConditionType;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.research.ResearchService;
import game.server.logic.rune.bean.Rune;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 分解符文
 * 
 * @author liuwei
 * @date 2018年9月3日
 */
public class ReqDecomHeroRuneScript implements IScript {

	private final Logger logger = Logger.getLogger(ReqDecomHeroRuneScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		DecomRuneReq req = (DecomRuneReq) script.get(ScriptArgs.Key.ARG1);
		Player player = (Player) script.get(ScriptArgs.Key.PLAYER);
		decomposeRune(player, req.getRuneIdList(), req.getIsPreview());
		return null;
	}

	/**
	 * 分解符文
	 * 
	 * @param player
	 * @param runeId
	 * @param isPreview
	 */
	public void decomposeRune(Player player, List<Long> runeIdList, boolean isPreview) {
		List<Item> items = new ArrayList<>();
		List<Integer> list = new ArrayList<>();
		for (long runeId : runeIdList) {
			Rune rune = player.getRuneManager().getRuneMap().get(runeId);
			if (rune == null) {
				MessageUtils.sendPrompt(player, PromptType.ERROR, 39);// TODO替换语言包id
				return;
			}
			if (rune.getHeroId() != 0) {// 已穿戴不能重生
				MessageUtils.sendPrompt(player, PromptType.ERROR, 40);// TODO替换语言包id
				return;
			}
			t_runeBean runeBean = BeanTemplet.getRuneBean(rune.getModelId());
			t_recyclingBean recyclingBean = ResearchService.getInstance().getRecyclingBean(6, runeBean.getQua_lvl(), 0);
			// 培养过先重生
			if (rune.getLevel() > 1) {
				ScriptArgs script = new ScriptArgs();
				script.put(ScriptArgs.Key.PLAYER, player);
				script.put(ScriptArgs.Key.ARG1, 2);// 分解逻辑调用
				script.put(ScriptArgs.Key.ARG2, rune);
				script.put(ScriptArgs.Key.ARG3, runeBean);
				script.put(ScriptArgs.Key.ARG4, items);
				ScriptManager.getInstance().call("logic.research.ReqRebirthRuneScript", script);
			}
			// 分解奖励道具
			for (String r : recyclingBean.getGoods_id_num().split(";")) {
				items.addAll(
						BeanFactory.createProps(Integer.parseInt(r.split(",")[0]), Integer.parseInt(r.split(",")[1])));
			}
			list.add(rune.getModelId());
		}
		if (isPreview) {
			genDecomRuneRsp(player, isPreview, items,runeIdList,list);
			return;
		}
		// 背包空间是否充足
		if (!player.getBackpackManager().isCapacityEnough(items)) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 41);// TODO替换语言包id
			return;
		}
		// 发送奖励
		player.getBackpackManager().addItems(items, true, false, Reason.DECOMPOSE, "");
		// 删除
		for (long runeId : runeIdList) {
			Rune rune = player.getRuneManager().getRuneMap().get(runeId);
			t_runeBean runeBean = BeanTemplet.getRuneBean(rune.getModelId());
			// 更新任务
			player.getTaskManager().updateTaskCondition(TaskConditionType.DECOMPOSE_RUNE_NUM, 1);
			player.getTaskManager().updateTaskCondition(TaskConditionType.DECOMPOSE_RUNE_QUALITY_NUM,
					runeBean.getQuality(), 1);
			player.getRuneManager().getRuneMap().remove(runeId);
		}
		// 更新符文品质数量任务
		player.getRuneManager().calcQualityNumAndUpdateTask();
		// 推送
		genDecomRuneRsp(player, isPreview, items,runeIdList,list);
	}

	private void genDecomRuneRsp(Player player, boolean isPreview, List<Item> items, List<Long> runeIdList,List<Integer> list) {
		DecomRuneRsp.Builder builder = DecomRuneRsp.newBuilder();
		builder.setIsPreview(isPreview);
		BeanFactory.combineItemList(items);
		for (Item item : items) {
			PropInfo.Builder pBuilder = PropInfo.newBuilder();
			pBuilder.setId(item.getId());
			pBuilder.setNum(item.getNum());
			builder.addItemList(pBuilder);
		}
		for (Long runeId : runeIdList) {
			builder.addRuneId(runeId);
		}
		MessageUtils.send(player.getSession(), player.getFactory().fetchSMessage(DecomRuneRspID.DecomRuneRspMsgID_VALUE,
				builder.build().toByteArray()));
		// 玩家操作日志
		StringBuilder sBuilder = new StringBuilder();
		list.stream().forEach(m -> sBuilder.append("," + m));
		LogService.getInstance().logPlayerAction(player, DecomRuneRspID.DecomRuneRspMsgID_VALUE, isPreview,sBuilder.substring(1));
	}
}
