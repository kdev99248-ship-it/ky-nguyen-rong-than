package logic.guildwar;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.C2SGuildwarMsg.ExchangeGuildwarAwardReqID;
import Message.S2CBackpackMsg.PropInfo;
import Message.S2CGuildwarMsg.ExchangeGuildwarAwardRsp;
import Message.S2CGuildwarMsg.ExchangeGuildwarAwardRspID;
import Message.S2CPlayerMsg.PromptType;
import data.bean.t_unionstoreBean;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 
 * @ClassName: ExchangeAwardScript 
 * @Description: 联盟战商店兑换奖励
 * @author tzyx 
 * @date 2018年8月7日 下午8:24:35
 */
public class ExchangeAwardScript implements IScript {
     private static Logger LOGGER = Logger.getLogger(ExchangeAwardScript.class);

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Object call(String scriptName, Object arg) {
    	LOGGER.info("联盟战商店兑换奖励");
        ScriptArgs argsMap = (ScriptArgs) arg;
        Player player = (Player) argsMap.get(ScriptArgs.Key.PLAYER);
        int id = (int) argsMap.get(ScriptArgs.Key.ARG1);
        int num = (int) argsMap.get(ScriptArgs.Key.ARG2);
        // 检查兑换物是否存在
        Map<Integer, t_unionstoreBean> allGuildwarStore = BeanTemplet.getAllGuildwarStore();
        if (!allGuildwarStore.containsKey(Integer.valueOf(id))) {
        	LOGGER.info("请求参数异常");
			MessageUtils.sendPrompt(player, PromptType.WARNING, 129);
        	return null;
        }
        t_unionstoreBean t_unionstoreBean = allGuildwarStore.get(Integer.valueOf(id));
        // 检查消耗是否满足
        String[] split = t_unionstoreBean.getProp_id().split(",");
        List<Item> cost = BeanFactory.createProps(Integer.valueOf(split[0]), Integer.valueOf(split[1]) * num);
		if (!player.getBackpackManager().isItemNumEnough(cost)) {
			LOGGER.error("道具不足!");
			MessageUtils.sendPrompt(player, PromptType.ERROR, 1278);
			return null;
		}
		// 扣除消耗
		player.getBackpackManager().removeItems(cost, true, Reason.GUILDWAR_EXCHANGE_COST, "");
        // 发送物品
		split = t_unionstoreBean.getExchange_hero().split(";");
		String[] spli;
		List<Item> add = new ArrayList<>();
		ExchangeGuildwarAwardRsp.Builder builder = ExchangeGuildwarAwardRsp.newBuilder();
		PropInfo.Builder prop;
		for (int i = 0; i < split.length; i++) {
			spli = split[i].split(",");
			prop = PropInfo.newBuilder();
			prop.setId(Integer.valueOf(spli[0]));
			prop.setNum(Integer.valueOf(spli[1]) * num);
			add.addAll(BeanFactory.createProps(prop.getId(), prop.getNum()));
			builder.addItems(prop);
		}
		builder.setId(id);
		builder.setNum(num);
		player.getBackpackManager().addItems(add, true, false, Reason.GUILDWAR_EXCHANGE, "");
		MessageUtils.send(player, player.getFactory().fetchSMessage(
				ExchangeGuildwarAwardRspID.ExchangeGuildwarAwardRspMsgID_VALUE, builder.build().toByteArray()));
		// 记录动作   物品id,数量
		LogService.getInstance().logPlayerAction(player, ExchangeGuildwarAwardReqID.ExchangeGuildwarAwardReqMsgID_VALUE
				, id, num);
		return null;
	}

}
