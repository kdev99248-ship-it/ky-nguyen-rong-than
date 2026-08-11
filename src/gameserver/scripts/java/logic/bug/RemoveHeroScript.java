package logic.bug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Message.S2CPlayerMsg.PlayerType;
import data.bean.t_exclusiveBean;
import data.bean.t_heroBean;
import data.bean.t_hero_characterBean;
import data.bean.t_hero_starBean;
import data.bean.t_hero_stepBean;
import data.bean.t_itemBean;
import data.bean.t_runeBean;
import data.bean.t_skillBean;
import game.core.pub.script.IScript;
import game.server.db.game.bean.PlayerBean;
import game.server.logic.backpack.bean.Grid;
import game.server.logic.constant.ItemType;
import game.server.logic.constant.Reason;
import game.server.logic.hero.HeroService;
import game.server.logic.hero.bean.Hero;
import game.server.logic.hero.bean.HeroCharacter;
import game.server.logic.hero.bean.HeroEsoterica;
import game.server.logic.hero.bean.HeroSkill;
import game.server.logic.item.bean.Equipment;
import game.server.logic.item.bean.Item;
import game.server.logic.item.bean.MagicWeapon;
import game.server.logic.line.handler.PlayerUpdateBean;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.player.RoleViewService;
import game.server.logic.rune.RuneManager;
import game.server.logic.rune.RuneService;
import game.server.logic.rune.bean.Rune;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.thread.PlayerRestoreProcessor;

/**
 * @author duwei
 *
 *         2019年9月27日
 */
public class RemoveHeroScript implements IScript {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		String before = "536334052025";
		String after = "536334052025";
		Map<Long, Integer> map = new HashMap<Long, Integer>();
		map.put(536334052025l, 120);
		int priceId = 12125;
		int heroId = 50125;
		String[] playerIds = after.split(",");
		List<PlayerUpdateBean> savePlayers = new ArrayList<>();
		for (String p : playerIds) {
			long pid = Long.parseLong(p);
			Player player = PlayerManager.getOffLinePlayerByPlayerId(pid);
			if (null != player && player.getPlayerType() == PlayerType.PLAYER_VALUE) {
				logger.info("玩家 ： " + player.getPlayerName() + " , 玩家id ： " + player.getPlayerId() + " 开始处理！");
				Hero hero = player.getHeroManager().getHero(heroId);
				int[] lineUp = player.getFormationManager().getLineUp();
				for (int i = 0; i < lineUp.length; i++) {
					if (lineUp[i] == heroId) {
						lineUp[i] = 0;
					}
				}
				int[] formation = player.getFormationManager().getFormation();
				for (int i = 0; i < formation.length; i++) {
					if (formation[i] == heroId) {
						formation[i] = 0;
					}
				}
				logger.info("下阵处理................！");
				if (null != hero) {
					List<Item> items = new ArrayList<>();
					calRebirthHero(player, hero, items);
					logger.info("计算需要退还材料................！");
					// 拆卸所有符文,因为只有非上阵英雄才能分解,所以不考虑属性变化
					logger.info("准备卸下装备法器................！");
					if (null != hero.getHeroWear()) {
						int[] wearIds = hero.getHeroWear().getWearArray();
						for (int i = 0; i < wearIds.length; i++) {
							if (wearIds[i] != 0) {
								if (i < 4) {
									Grid grid = player.getBackpackManager().getEquipMap().get(wearIds[i]);
									if (null != grid) {
										((Equipment) grid.getItem()).setHeroId(0);
										logger.info("清除装备装备状态................" + wearIds[i] + " i " + i);
									}
								} else {

									Grid grid = player.getBackpackManager().getMagicWeaponMap().get(wearIds[i]);
									if (null != grid) {
										((MagicWeapon) grid.getItem()).setHeroId(0);
										logger.info("清除法器装备状态................" + wearIds[i] + " i " + i);
									}
								}
							}
						}
						if (player.isOnline())
							player.getBackpackManager().sendBackpackInfos();
					}
					logger.info("装备法器卸下完成................！");

					long[] runeId = hero.getHeroRuneInfo().getRuneId();
					RuneManager runeManager = player.getRuneManager();
					logger.info("准备卸下符文................！");
					Rune rune;
					t_runeBean runeBean;
					List<Long> changeIds = new ArrayList<>();
					for (int i = 0; i < runeId.length; i++) {
						if (runeId[i] != 0) {
							rune = runeManager.getRuneMap().get(Long.valueOf(runeId[i]));
							runeBean = BeanTemplet.getRuneBean(rune.getModelId());
							runeManager.putDownRune(hero, runeBean.getSeat(), null, changeIds);
						}
					}
					if (player.isOnline())
						RuneService.getInstance().notifyRuneChange(player, changeIds);
					// 发送奖励
					player.getBackpackManager().addItems(items, true, false, Reason.DECOMPOSE, "");
					logger.info("还原道具................！");

					RoleViewService.removePlayerHeroCache(player.getPlayerId(), hero.getId());
					// 分解 检测激活
					if (player.isOnline())
						HeroService.getInstance().checkRalateByHeroDown(player, hero, true);
					logger.info("删除羁绊激活状态................！");
					// //删除英雄及推送
					player.getHeroManager().removeHero(hero);
					logger.info("删除精灵................！");
					if (before.contains(pid + "") || map.containsKey(pid)) {
						int num = map.get(pid);
						logger.info("删除指定数量 指定道具......... priceId " + priceId + " , num " + num);
						List<Item> itemLs = BeanFactory.createProps(priceId, num);
						player.getBackpackManager().removeItems(itemLs, player.isOnline(), Reason.ITEM_USE, "删除错误道具");
					} else {
						logger.info("删除指定道具全部数量......... priceId " + priceId);
						removePrice(player, priceId);
					}

				}
				player.addAllChangePropertyKey();
				PlayerBean bean = player.toPlayerBean();
				bean.updateChangeProperty(player.getChangeProperty());
				logger.info("玩家 ： " + player.getPlayerName() + " , 玩家id ： " + player.getPlayerId() + " 回存Key数量 : "
						+ player.getChangeProperty().size());
				savePlayers.add(new PlayerUpdateBean(player.toAccountBean(), bean, false));
				logger.info("玩家 ： " + player.getPlayerName() + " , 玩家id ： " + player.getPlayerId() + " 处理结束！");
			}
		}
		PlayerRestoreProcessor.getInstance().handleUpdateRoleBatch(savePlayers);
		logger.info("所有玩家获取道具异常，已经处理结束....................！");
		return null;
	}

	private void removePrice(Player player, int itemId) {
		List<Item> itemLs = new ArrayList<Item>();
		Map<Integer, Grid> map = player.getBackpackManager().getPieceMap();
		Iterator<Entry<Integer, Grid>> iterator = map.entrySet().iterator();
		logger.info("player [ name : " + player.getPlayerName() + " ; playerId : " + player.getPlayerId() + " ]  删除开始");
		while (iterator.hasNext()) {
			Entry<Integer, Grid> entry = iterator.next();
			Grid grid = entry.getValue();
			if (null == grid || null == grid.getItem())
				continue;
			boolean remove = itemId == grid.getItem().getId();
			if (remove) {
				logger.info("删除道具 [ " + grid.getItem().getId() + " : " + grid.getItem().getNum() + " ]");
				itemLs.add(grid.getItem());
			}

		}
		player.getBackpackManager().removeItems(itemLs, true, Reason.ITEM_USE, "删除错误道具");
		logger.info("player [ name : " + player.getPlayerName() + " ; playerId : " + player.getPlayerId() + " ] 删除完毕");
	}

	private void calRebirthHero(Player player, Hero hero, List<Item> items) {
		t_heroBean heroBean = BeanTemplet.getHeroBean(hero.getId());
		int gold = 0;
		// 还原等级
		if (hero.getLevel() > 1) {
			int exp = 0;
			for (int i = 1; i < hero.getLevel(); i++) {
				exp += HeroService.getInstance().getExp(i, heroBean);
			}
			items.addAll(BeanFactory.createProps(ItemType.HERO_EXP.value(), exp));
			logger.info("返还道具 [ " + ItemType.HERO_EXP.value() + " : " + exp + " ]");
		}
		// 还原星级
		if (hero.getStar() > 1) {
			int fragmentNum = 0;
			for (int i = 1; i < hero.getStar(); i++) {
				int starId = heroBean.getStar_template() * 100 + i + 1;
				t_hero_starBean starBean = BeanTemplet.getHeroStarBean(starId);
				fragmentNum += starBean.getNeed_num();
				gold += starBean.getNeed_gold();
			}
			if (fragmentNum > 0) {
				items.addAll(BeanFactory.createProps(heroBean.getFragment_id(), fragmentNum));
				logger.info("返还道具 [ " + heroBean.getFragment_id() + " : " + fragmentNum + " ]");
			}
		}
		// 还原阶级
		if (hero.getStep() > 0) {
			for (int i = 0; i < hero.getStep(); i++) {
				int step = heroBean.getStep_template() * 100 + i + 1;
				t_hero_stepBean stepBean = BeanTemplet.getHeroStepBean(step);
				items.addAll(BeanFactory.createProps(stepBean.getNeed_item1_id(), stepBean.getNeed_item1_num()));
				items.addAll(BeanFactory.createProps(stepBean.getNeed_item2_id(), stepBean.getNeed_item2_num()));
				items.addAll(BeanFactory.createProps(stepBean.getNeed_item3_id(), stepBean.getNeed_item3_num()));
				items.addAll(BeanFactory.createProps(stepBean.getNeed_item4_id(), stepBean.getNeed_item4_num()));
				gold += stepBean.getNeed_gold();
				logger.info("返还道具 [ " + stepBean.getNeed_item1_id() + " : " + stepBean.getNeed_item1_num() + " ]");
				logger.info("返还道具 [ " + stepBean.getNeed_item2_id() + " : " + stepBean.getNeed_item2_num() + " ]");
				logger.info("返还道具 [ " + stepBean.getNeed_item3_id() + " : " + stepBean.getNeed_item3_num() + " ]");
				logger.info("返还道具 [ " + stepBean.getNeed_item4_id() + " : " + stepBean.getNeed_item4_num() + " ]");
			}
		}
		// 还原专属
		if (hero.getExclusiveStar() > 0) {
			for (int i = 0; i < hero.getExclusiveStar(); i++) {
				int exclusiveId = heroBean.getExclusive() * 100 + i + 1;
				t_exclusiveBean exclusiveBean = BeanTemplet.getExclusiveBean(exclusiveId);
				String[] needItemArray = exclusiveBean.getNeed_item_id().split(",");
				items.addAll(BeanFactory.createProps(Integer.parseInt(needItemArray[0]),
						Integer.parseInt(needItemArray[1])));
				logger.info("返还道具 [ " + Integer.parseInt(needItemArray[0]) + " : " + Integer.parseInt(needItemArray[1])
						+ " ]");
				if (exclusiveBean.getNeed_num() > 0) {// 消耗专属宝物
					items.addAll(
							BeanFactory.createProps(exclusiveBean.getCid() * 100 + 1, exclusiveBean.getNeed_num()));
					logger.info("返还道具 [ " + (exclusiveBean.getCid() * 100 + 1) + " : " + exclusiveBean.getNeed_num()
							+ " ]");
				}
				gold += exclusiveBean.getNeed_gold();
			}
		}
		// 还原技能
		int skillPoint = 0;// 技能点
		for (HeroSkill heroSkill : hero.getSkillMap().values()) {
			if (heroSkill.getLevel() > 1) {
				t_skillBean skillBean = BeanTemplet.getSkillBean(heroSkill.getId());
				String[] consume = skillBean.getConsume().split(",");
				for (int i = 0; i < heroSkill.getLevel() - 1; i++) {
					gold += Integer.parseInt(consume[i]);
				}
				skillPoint += heroSkill.getLevel() - 1;
			}
		}
		if (skillPoint > 0) {
			items.addAll(BeanFactory.createProps(ItemType.SKILL_POINTS.value(), skillPoint));
			logger.info("返还道具 [ " + ItemType.SKILL_POINTS.value() + " : " + skillPoint + " ]");
		}
		// 还原四灵
		int characterExp = 0;
		for (HeroCharacter heroCharacter : hero.getCharacterMap().values()) {
			if (heroCharacter.isUnlock() && (heroCharacter.getLevel() > 1 || heroCharacter.getExp() > 10)) {
				t_hero_characterBean characterBean = BeanTemplet.getHeroCharacterBean(heroCharacter.getId());
				String[] exp = characterBean.getNeed_exp().split(",");
				for (int i = 1; i < heroCharacter.getLevel(); i++) {
					// 四灵初始等级为1级 配置只有1到20级的经验配置
					characterExp += Integer.parseInt(exp[i - 1]);
				}
			}
		}
		gold += characterExp * BeanTemplet.getGlobalBean(18).getInt_value();// 特性经验兑换金币消耗比例
																			// 一点经验
																			// =
																			// 5
		// 还原秘技
		if (!hero.getAllHeroEsoterica().isEmpty()) {
			for (HeroEsoterica bean : hero.getAllHeroEsoterica()) {
				items.addAll(bean.calRebirth(hero.getId()));
			}
		}
		t_itemBean itemBean = BeanTemplet.getItemBean(BeanTemplet.getGlobalBean(177).getInt_value());
		int num = (int) Math.ceil((float) characterExp / itemBean.getCharacter_exp());
		if (num != 0) {
			items.addAll(BeanFactory.createProps(itemBean.getId(), num));
			logger.info("返还道具 [ " + itemBean.getId() + " : " + num + " ]");
		}
		items.addAll(BeanFactory.createProps(ItemType.GOLD.value(), gold));
		logger.info("返还道具 [ " + ItemType.GOLD.value() + " : " + gold + " ]");

	}

}
