package logic.hero;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Message.C2SHeroMsg.HeroEsotericaLevelUpReqID;
import Message.S2CHeroMsg.HeroEsotericaLevelUpRsp;
import Message.S2CHeroMsg.HeroEsotericaLevelUpRspID;
import Message.S2CHeroMsg.HeroEsotericaRsp;
import Message.S2CHeroMsg.HeroEsotericaRspID;
import Message.S2CHeroMsg.HeroRsp;
import Message.S2CHeroMsg.HeroRspID;
import Message.S2CPlayerMsg.PromptType;
import data.bean.t_heroBean;
import data.bean.t_uniqueBean;
import data.bean.t_uniquemodelBean;
import game.core.pub.script.IScript;
import game.core.pub.script.ScriptManager;
import game.server.logic.constant.ItemType;
import game.server.logic.constant.Reason;
import game.server.logic.constant.TaskConditionType;
import game.server.logic.hero.HeroService;
import game.server.logic.hero.bean.Hero;
import game.server.logic.hero.bean.HeroEsoterica;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 请求英雄秘技解锁或升级
 */
public class ReqHeroEsotericaLevelUpScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs argsMap = (ScriptArgs) arg;
		Player player = (Player) argsMap.get(ScriptArgs.Key.PLAYER);
		int heroId = (int) argsMap.get(ScriptArgs.Key.ARG1);
		int id = (int) argsMap.get(ScriptArgs.Key.ARG2);
		Hero hero = player.getHeroManager().getHero(heroId);
		if (null == hero) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 3000);
			return null;
		}
		// 协议
		HeroEsotericaLevelUpRsp.Builder builder = HeroEsotericaLevelUpRsp.newBuilder();
		HeroEsoterica he = hero.getHeroEsotericaById(id);
		if (!he.isActive()) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 3001);
			return null;
		}
		t_uniqueBean bean = BeanTemplet.getUniqueBean(id);
		String[] costs = bean.getModel().split(",");
		int len = he.getStar();
		if (he.getStar() > costs.length) {
			len = costs.length;// 已经大于配置大小了
		}
		if (len == he.getMaxStar()) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 3002);
			return null;
		}
		List<Item> deducts = new ArrayList<Item>();
		for (int j = len; j < len + 1; j++) {
			t_uniquemodelBean model = BeanTemplet.getUniqueMoldelBean(Integer.parseInt(costs[j]));
			if (null != model) {
				String[] cost = model.getComsume().split("\\|");// 取当前星数的消耗
																// 数组开始为0
				if (cost.length > 2) {
					t_heroBean heroBean = BeanTemplet.getHeroBean(heroId);
					// 第一位是碎片
					if (null != heroBean)
						deducts.addAll(BeanFactory.createProps(heroBean.getFragment_id(), Integer.parseInt(cost[0])));
					// 第二位是额外道具
					// String[] other = cost[1].split(";");
					for (int i = 1; i < cost.length - 1; i++) {
						if (cost[i].split("_").length < 2)
							continue;
						int itemId = Integer.parseInt(cost[i].split("_")[0]);
						int itemNum = Integer.parseInt(cost[i].split("_")[1]);
						deducts.addAll(BeanFactory.createProps(itemId, itemNum));
					}
					// 第三位是金币
					deducts.addAll(
							BeanFactory.createProps(ItemType.GOLD.value(), Integer.parseInt(cost[cost.length - 1])));
				}
			}
		}
		if (!player.getBackpackManager().isItemNumEnough(deducts)) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 3003);
			return null;
		}
		builder.setBeforePower(hero.getPower());
		// 升级之前扣除属性
		Map<Integer, Integer> deductMap = he.getPropertyMap();
		for (Integer proId : deductMap.keySet()) {
			hero.getProperty().addProperty(proId, -deductMap.get(proId));
		}

		player.getBackpackManager().removeItems(deducts, true, Reason.HEROESOTERICA_LEVELUP, "秘技升级");
		he.levelUp(builder, hero.getId());// 升级
		hero.calculateHeroEsotericaProperty(id);// 属性技能替换到英雄上
		// 计算战力 计算战力时把完整属性加到英雄身上
		HeroService.getInstance().calculatePower(player, hero, true, true);
		HeroService.getInstance().pushHeroBaseProperty(player, hero);// 推送属性
		// 推送
		builder.setAfterPower(hero.getPower());
		builder.setStar(he.getStar());// 成功

		MessageUtils.send(player.getSession(), player.getFactory().fetchSMessage(
				HeroEsotericaLevelUpRspID.HeroEsotericaLevelUpRspMsgID_VALUE, builder.build().toByteArray()));

		// 秘技任务
		player.getTaskManager().updateTaskCondition(TaskConditionType.ESOTERICA_STAR, he.getStar(),
				sumEsotericaStar(player, he.getStar()));

		// 顺便刷新一下秘技情况
		List<Hero> list = player.getHeroManager().getAllHero();
		for (Hero heroBean : list) {
			if (null == heroBean)
				continue;
			heroBean.checkHeroEsoterica(player);// 先检测激活条件
		}
		ScriptArgs script = new ScriptArgs();
		script.put(ScriptArgs.Key.PLAYER, player);
		ScriptManager.getInstance().call("logic.hero.ReqHeroEsotericaInfoScript", script);
		
		if (builder.getHasSkill()) {
			// 有技能变化 推送英雄数据
			HeroRsp.Builder heroBuilder = HeroRsp.newBuilder();
			heroBuilder.addHeroList(player.getHeroManager().getHero(heroId).getBuilder());
			MessageUtils.send(player.getSession(),
					player.getFactory().fetchSMessage(HeroRspID.HeroRspMsgID_VALUE, heroBuilder.build().toByteArray()));
		}

		// 训练值检查
		HeroService.getInstance().calcTrainExpAndNotify(player);
		
		// 玩家操作日志
		LogService.getInstance().logPlayerAction(player, HeroEsotericaLevelUpReqID.HeroEsotericaLevelUpReqMsgID_VALUE,
				heroId, id, he.getStar());
		return null;
	}

	private int sumEsotericaStar(Player player, int star) {
		int sum = 0;
		List<Hero> list = player.getHeroManager().getAllHero();
		for (Hero hero : list) {
			if (null == hero)
				continue;
			List<HeroEsoterica> esotericas = hero.getAllHeroEsoterica();
			for (HeroEsoterica heroEsoterica : esotericas) {
				if (null == heroEsoterica)
					continue;
				if (heroEsoterica.getStar() >= star)
					sum++;
			}
		}
		return sum;
	}

}
