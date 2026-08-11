package logic.bug;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.bean.t_itemBean;
import data.bean.t_magic_weaponBean;
import game.core.pub.script.IScript;
import game.server.db.game.bean.PlayerBean;
import game.server.logic.backpack.bean.Grid;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.line.handler.PlayerUpdateBean;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.util.BeanTemplet;
import game.server.thread.PlayerRestoreProcessor;
import game.server.thread.dboperator.handler.ReqUpdatePlayerHandler;

public class RemovePlayerItemScript implements IScript {
	private static Logger logger = LoggerFactory.getLogger(RemovePlayerItemScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		long[] players = new long[] { 537407782961l };

		for (int i = 0; i < players.length; i++) {
			Player player = PlayerManager.getOffLinePlayerByPlayerId(players[i]);
			if (null != player) {
				List<Item> itemLs = new ArrayList<Item>();
				String items = "14242,14243,11205,11206,11207,11208,11209,11210,11211,11212,11213,11214,11215,11216,11217,11218,11219,11220,11221,11222,11223,11224,11225,11226,11227,11228,11229,11230,11231,11232,11233,11234,11235,11236,11237,11238,11239,11240,11241,11242,11243,11244,11245,11246,11205,11206,11207,11208,11209,11210,11211,11212,11213,11214,11215,11216,11217,11218,11219,11220,11221,11222,11223,11224,11225,11226,11227,11228,11229,11230,11231,11232,11233,11234,11235,11236,11237,11238,11239,11240,11241,11242,11243,11244,11245,11246,11163,11164,11165,11166,11167,11168,11169,11170,11171,11172,11173,11174,11175,11176,11177,11178,11179,11180,11181,11182,11183,11184,11185,11186,11187,11188,11189,11190,11191,11192,11193,11194,11195,11196,11197,11198,11199,11200,11201,11202,11203,11204";
				Map<Integer, Grid> map = player.getBackpackManager().getItemMap();
				Iterator<Entry<Integer, Grid>> iterator = map.entrySet().iterator();
				logger.info("player [ name : " + player.getPlayerName() + " ; playerId : " + players[i] + " ]  删除开始");
				while (iterator.hasNext()) {
					Entry<Integer, Grid> entry = iterator.next();
					Grid grid = entry.getValue();
					if (null == grid || null == grid.getItem())
						continue;
					boolean remove = items.contains(grid.getItem().getId() + "");
					if (remove) {
						logger.info("删除道具 [ " + grid.getItem().getId() + " : " + grid.getItem().getNum() + " ]");
						// iterator.remove();
						itemLs.add(grid.getItem());
					} else {
						t_itemBean itemBean = BeanTemplet.getItemBean(grid.getItem().getId());
						if (null == itemBean)
							continue;
						t_magic_weaponBean weaponBean = BeanTemplet.getMagicWeaponBean(itemBean.getInt_param());
						if (null == weaponBean)
							continue;
						if (weaponBean.getQuality() >= 20) {
							logger.info("删除道具 [ " + grid.getItem().getId() + " : " + grid.getItem().getNum() + " ]");
							// iterator.remove();
							itemLs.add(grid.getItem());
						}
					}

				}
				map = player.getBackpackManager().getMagicWeaponMap();
				iterator = map.entrySet().iterator();
				logger.info("player [ name : " + player.getPlayerName() + " ; playerId : " + players[i] + " ]  删除开始");
				while (iterator.hasNext()) {
					Entry<Integer, Grid> entry = iterator.next();
					Grid grid = entry.getValue();
					if (null == grid || null == grid.getItem())
						continue;
					boolean remove = items.contains(grid.getItem().getId() + "");
					if (remove) {
						logger.info("删除道具 [ " + grid.getItem().getId() + " : " + grid.getItem().getNum() + " ]");
						// iterator.remove();
						itemLs.add(grid.getItem());
					} else {
						t_itemBean itemBean = BeanTemplet.getItemBean(grid.getItem().getId());
						if (null == itemBean)
							continue;
						t_magic_weaponBean weaponBean = BeanTemplet.getMagicWeaponBean(itemBean.getInt_param());
						if (null == weaponBean)
							continue;
						if (weaponBean.getQuality() >= 20) {
							logger.info("删除道具 [ " + grid.getItem().getId() + " : " + grid.getItem().getNum() + " ]");
							itemLs.add(grid.getItem());
							// iterator.remove();
						}
					}

				}
				map = player.getBackpackManager().getPieceMap();
				iterator = map.entrySet().iterator();
				logger.info("player [ name : " + player.getPlayerName() + " ; playerId : " + players[i] + " ]  删除开始");
				while (iterator.hasNext()) {
					Entry<Integer, Grid> entry = iterator.next();
					Grid grid = entry.getValue();
					if (null == grid || null == grid.getItem())
						continue;
					boolean remove = items.contains(grid.getItem().getId() + "");
					if (remove) {
						logger.info("删除道具 [ " + grid.getItem().getId() + " : " + grid.getItem().getNum() + " ]");
						itemLs.add(grid.getItem());
						// iterator.remove();
					} else {
						t_itemBean itemBean = BeanTemplet.getItemBean(grid.getItem().getId());
						if (null == itemBean)
							continue;
						t_magic_weaponBean weaponBean = BeanTemplet.getMagicWeaponBean(itemBean.getInt_param());
						if (null == weaponBean)
							continue;
						if (weaponBean.getQuality() >= 20) {
							logger.info("删除道具 [ " + grid.getItem().getId() + " : " + grid.getItem().getNum() + " ]");
							itemLs.add(grid.getItem());
							// iterator.remove();
						}
					}

				}
				player.getBackpackManager().removeItems(itemLs, true, Reason.ITEM_USE, "删除错误道具");
				logger.info("player [ name : " + player.getPlayerName() + " ; playerId : " + players[i] + " ] 删除完毕");
				player.addAllChangePropertyKey();
				PlayerBean bean = player.toPlayerBean();
				bean.updateChangeProperty(player.getChangeProperty());
				PlayerRestoreProcessor.getInstance().submitRequest(
						new ReqUpdatePlayerHandler(-1, new PlayerUpdateBean(player.toAccountBean(), bean, false)));
			}
		}

		return null;
	}

}
