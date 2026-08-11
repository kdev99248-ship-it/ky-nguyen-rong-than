package logic.backpack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Message.C2SBackpackMsg.EquipStarUpReqID;
import Message.S2CBackpackMsg.EquipStarUpRsp;
import Message.S2CBackpackMsg.EquipStarUpRspID;
import Message.S2CBackpackMsg.EquipUpdateRsp;
import Message.S2CBackpackMsg.EquipUpdateRspID;
import Message.S2CPlayerMsg.PromptType;
import data.bean.t_equipBean;
import data.bean.t_evolveBean;
import game.core.pub.script.IScript;
import game.server.logic.backpack.bean.Grid;
import game.server.logic.backpack.bean.GridType;
import game.server.logic.constant.ItemType;
import game.server.logic.constant.Reason;
import game.server.logic.hero.HeroService;
import game.server.logic.hero.HeroWearService;
import game.server.logic.hero.bean.Hero;
import game.server.logic.item.bean.Equipment;
import game.server.logic.item.bean.Item;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.util.MessageUtils;

public class reqEquipStarUpScript implements IScript {

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		String functionName = (String) script.get(Key.FUNCTION_NAME);
		switch (functionName) {
		case "reqEquipStarUp":
			reqEquipStarUp((Integer) script.get(Key.ARG1), (Player) script.get(Key.PLAYER));
			break;
		default:
			break;
		}
		return null;
	}

	/**
	 * 请求装备升星
	 * 
	 * @param player
	 * @param gridId
	 */
	public void reqEquipStarUp(int gridId, Player player) {
		Map<Integer, Grid> map = player.getBackpackManager().getMapForGridType(GridType.EQUIP);
		List<Item> needItems = new ArrayList<>();
		Grid grid = map.get(gridId);
		if (grid == null || grid.isEmpty()) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 129);// TODO替换语言包id
			return;
		}

		Equipment item = (Equipment) grid.getItem();
		int star = item.getStar();// 当前星级
		t_equipBean bean = BeanTemplet.getEquipBean(item.getId());
		int starId = bean.getEvolveid() * 1000 + star + 1;
		t_evolveBean evolveBean = BeanTemplet.getEquipStarUpBean(starId);
		// 等级限制
		if (evolveBean.getEquip_level() > item.getLevel()) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 167);// TODO替换语言包id
			return;
		}
		// 判断金币是否足够
		if (evolveBean.getNeed_gold() > player.getGold()) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 33);// TODO替换语言包id
			return;
		}
		needItems.addAll(BeanFactory.createProps(ItemType.GOLD.value(), evolveBean.getNeed_gold()));

		String items = evolveBean.getNeed_item1_id();
		for (String need_item : items.split(";")) {
			needItems.addAll(BeanFactory.createProps(Integer.parseInt(need_item.split("_")[0]),
					Integer.parseInt(need_item.split("_")[1])));
		}
		// 检查精练碎片
		String fragId = bean.getFrag_id().split(",")[0];
		needItems.addAll(BeanFactory.createProps(Integer.parseInt(fragId), evolveBean.getNeed_self()));
		// 检查材料是否足够
		if (!player.getBackpackManager().isItemNumEnough(needItems)) {
			MessageUtils.sendPrompt(player, PromptType.ERROR, 32);// TODO替换语言包id
			return;
		}
		player.getBackpackManager().removeItems(needItems, true, Reason.EQUIP_STAR_UP, item.toJson().toJSONString());
		item.setStar(star + 1);

		EquipStarUpRsp.Builder starBuilder = EquipStarUpRsp.newBuilder();
		// 发送装备升星属性信息
		starBuilder.setRefineLevel(item.getRefineLevel());
		starBuilder.setStarId(starId);
		starBuilder.setGrideId(gridId);
		MessageUtils.send(player, player.getFactory().fetchSMessage(EquipStarUpRspID.EquipStarUpRspMsgID_VALUE,
				starBuilder.build().toByteArray()));

		// 通知客户端刷新装备
		EquipUpdateRsp.Builder builder = EquipUpdateRsp.newBuilder();
		builder.addEquipGrids(grid.buildEquipGridInfo());
		MessageUtils.send(player, player.getFactory().fetchSMessage(EquipUpdateRspID.EquipUpdateRspMsgID_VALUE,
				builder.build().toByteArray()));
		// 通知客户端刷新格子信息
		player.getBackpackManager().notifyClientBackpackUpdate(GridType.EQUIP, grid);
		// 属性加成
		if (item.getHeroId() != 0) {
			// 附加属性加成
			// proChangeByExtraStarUp(player, evolveBean, item, bean);
			// 升星加成
			proChangeByEquipStarUp(player, evolveBean, item, bean);
			//// 强化大师检测
			HeroWearService.getInstance().checkPartStrengthen(player, item.getHeroId(), true);
		}

		// 日志记录
		LogService.getInstance().logPlayerAction(player, EquipStarUpReqID.EquipStarUpReqMsgID_VALUE, item.getId(),
				item.getStar());
	}

	/**
	 * 装备升星后属性改变
	 * 
	 * @param player
	 * @param hero
	 */
	private void proChangeByEquipStarUp(Player player, t_evolveBean evolveBean, Equipment equipment, t_equipBean bean) {
		int heroId = equipment.getHeroId();
		Hero hero = player.getHeroManager().getHero(heroId);

		// int proId = Integer.parseInt(bean.getBase_pro().split(",")[0]);
		//
		// int proValue = 0;
		// if (proId == PropertyType.ATT_ADD.value()) {
		// proValue += evolveBean.getAtt();
		// } else if (proId == PropertyType.PDEF_ADD.value()) {
		// proValue += evolveBean.getPdef();
		// } else if (proId == PropertyType.MDEF_ADD.value()) {
		// proValue += evolveBean.getMdef();
		// } else if (proId == PropertyType.HP_ADD.value()) {
		// proValue += evolveBean.getHp();
		// }

		// int oldValue = 0;
		// if(equipment.getStar() - 1 != 0) {
		// int starId = bean.getEvolveid() * 1000 + equipment.getStar() - 1;
		// t_evolveBean oldEvolveBean = BeanTemplet.getEquipStarUpBean(starId);
		// if (proId == PropertyType.ATT_ADD.value()) {
		// oldValue = oldEvolveBean.getAtt();
		// } else if (proId == PropertyType.PDEF_ADD.value()) {
		// oldValue = oldEvolveBean.getPdef();
		// } else if (proId == PropertyType.MDEF_ADD.value()) {
		// oldValue = oldEvolveBean.getMdef() ;
		// } else if (proId == PropertyType.HP_ADD.value()) {
		// oldValue = oldEvolveBean.getHp();
		// }
		// }
		// proValue = proValue - oldValue;
		// hero.getProperty().addProperty(proId, proValue);
		// 战力计算
		HeroService.getInstance().calculatePower(player, hero, true, true);
		// 推送基础属性
		// HeroService.getInstance().pushHeroBaseProperty(player, hero);
	}

	/**
	 * 装备升星附加属性加成
	 */
	private void proChangeByExtraStarUp(Player player, t_evolveBean evolveBean, Equipment equipment, t_equipBean bean) {
		int heroId = equipment.getHeroId();
		Hero hero = player.getHeroManager().getHero(heroId);
		int star = equipment.getStar();
		// 减去旧的
		if (star - 1 != 0) {
			int starId = bean.getEvolveid() * 1000 + star - 1;
			t_evolveBean oldBean = BeanTemplet.getEquipStarUpBean(starId);
			String[] oldItems = oldBean.getAttach().split(";");
			for (String oldItem : oldItems) {
				int id = Integer.parseInt(oldItem.split("_")[0]);
				int num = Integer.parseInt(oldItem.split("_")[1]);
				hero.getProperty().addProperty(id, -num);
			}
		}
		// 加上新的附加属性
		String[] newItems = evolveBean.getAttach().split(";");
		for (String newItem : newItems) {
			int id = Integer.parseInt(newItem.split("_")[0]);
			int num = Integer.parseInt(newItem.split("_")[1]);
			hero.getProperty().addProperty(id, num);
		}

	}
}
