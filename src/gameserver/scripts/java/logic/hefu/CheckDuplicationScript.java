package logic.hefu;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Message.S2CPlayerMsg.PlayerType;
import game.core.pub.script.IScript;
import game.server.db.game.bean.ArenaRankingBean;
import game.server.db.game.bean.PlayerBean;
import game.server.db.game.dao.ArenaRankingDao;
import game.server.logic.arena.ArenaProcessorManager;
import game.server.logic.guild.GuildService;
import game.server.logic.guild.bean.Guild;
import game.server.logic.line.handler.PlayerUpdateBean;
import game.server.logic.line.handler.ReqUpdateRoleBatchHandler;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.player.RoleView;
import game.server.logic.player.RoleViewService;
import game.server.thread.PlayerRestoreProcessor;

/**
 * 合服 检查重名脚本
 */
public class CheckDuplicationScript implements IScript {

	private Logger logger = LoggerFactory.getLogger(CheckDuplicationScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {

		logger.info("-------[ 开始合服检查重名  ] -------");
		List<RoleView> roles = RoleViewService.getAllRoleView();
		Map<String, Long> nameIds = new HashMap<>(); // 名字与玩家id
		logger.info("-------[ 玩家个数 ：" + roles.size() + "  ] -------");
		List<PlayerUpdateBean> list = new ArrayList<>();
		for (RoleView roleView : roles) {
			if (null == roleView)
				continue;
			// logger.info("-------[ roleView.isRobot() ：" + roleView.isRobot()
			// + "] -------");
			// if (roleView.isRobot())
			// continue;
			String name = roleView.getName();
			long playerId = roleView.getPlayerId();
			if (playerId < Integer.MAX_VALUE)
				continue;
			Player player = PlayerManager.getOffLinePlayerByPlayerId(playerId);
			if (null == player || player.getPlayerType() != PlayerType.PLAYER_VALUE) {
				continue;
			}

			logger.info("-------[ id : " + playerId + " , 玩家 ：" + name + " 开始清理武道馆排名及道馆任务数据 ] -------");
			player.getArenaManager().setCurRank(0);
			player.getArenaManager().getChallengeList().clear();
			player.getTaskManager().getPublicTaskFinished().clear();// 任务清除
			
			// --------  冠军之路重置
			player.getChampionManager().setTodayFloor(0);
			player.getChampionManager().setTodayFightCount(0);
			player.getChampionManager().setTodayBuyCount(0);
			player.getChampionManager().setHasOneKeyFight(false);
			player.getChampionManager().setHasPassReward(false);
			player.getChampionManager().setHasFirstPassReward(false);
			player.getChampionManager().setLastFightTime(0);
			player.getChampionManager().setMaxFloor(0);
			// --------  冠军之路重置
			
			
			

			if (nameIds.containsKey(name)) {
				long checkPlayerId = nameIds.get(name);
				logger.info("-------[ 玩家名称 : " + name + " 重复  , 玩家1 id：" + checkPlayerId + " , 玩家2 id: " + playerId
						+ " ] -------");
				Player checkPlayer = PlayerManager.getOffLinePlayerByPlayerId(checkPlayerId);
				if (null != checkPlayer && null != player) {
					// 等级较低的玩家名称加上区服后缀
					// 若玩家等级相同，则比对当前经验值，经验值也相同的情况下，开区较晚的角色会被改名
					// 被改名的玩家自动发放一张改名卡
					boolean change = false;// false修改玩家1名称 true修改玩家2名称
					if (checkPlayer.getLevel() > player.getLevel()) {
						change = true;
						logger.info("-------[  玩家2 id: " + playerId + " , 等级较小 被改名 ！] -------");
					} else if (checkPlayer.getLevel() == player.getLevel()) {
						if (checkPlayer.getHeroManager().getLeaderExp() > player.getHeroManager().getLeaderExp()) {
							change = true;
							logger.info("-------[  玩家2 id: " + playerId + " , 经验较少 被改名 ！] -------");
						} else if (checkPlayer.getHeroManager().getLeaderExp() == player.getHeroManager()
								.getLeaderExp()) {
							if (checkPlayer.getCurrentServer() > player.getCreateServer()) {
								change = true;
								logger.info("-------[  玩家2 id: " + playerId + " , 服务器开服较晚 被改名 ！] -------");
							} else {
								change = false;
								logger.info("-------[  玩家1 id: " + checkPlayerId + " , 服务器开服较晚 被改名 ！] -------");
							}
						} else {
							change = false;
							logger.info("-------[  玩家1 id: " + checkPlayerId + " , 经验较少 被改名 ！] -------");
						}
					} else {
						change = false;
						logger.info("-------[  玩家1 id: " + checkPlayerId + " , 等级较小 被改名 ！] -------");
					}
					if (change) {
						logger.info("-------[  玩家2 id: " + playerId + " , 开始改名 ！] -------");
						String newName = name + "S" + (player.getCreateServer() % 1000);
						logger.info("-------[  玩家新名称 : " + newName + " ! ] -------");
						player.setPlayerName(newName);
						player.setModifyNameCount(0); // 重置改名次数
						nameIds.put(newName, playerId);
					} else {
						logger.info("-------[  玩家1 id: " + checkPlayerId + " , 开始改名  ！] -------");
						String newName = name + "S" + (checkPlayer.getCreateServer() % 1000);
						logger.info("-------[  玩家新名称 : " + newName + " ! ] -------");
						checkPlayer.setPlayerName(newName);
						checkPlayer.setModifyNameCount(0); // 重置改名次数
						nameIds.put(newName, playerId);
					}
				} else {
					logger.info("-------[ 改名出错  玩家1 null：" + (null != checkPlayer) + " , 玩家2 null: " + (null != player)
							+ " ] -------");
				}
			} else {
				nameIds.put(name, playerId);
			}
			player.addAllChangePropertyKey();
			PlayerBean bean = player.toPlayerBean();
			bean.updateChangeProperty(player.getChangeProperty());
			// player.offLineSave();// 回存
			list.add(new PlayerUpdateBean(player.toAccountBean(), player.toPlayerBean(), false));
		}
		PlayerRestoreProcessor.getInstance().submitRequest(new ReqUpdateRoleBatchHandler(-1, list));
		logger.info("-------[   玩家重名验证完毕！  ] -------");
		logger.info("-------[   联盟重名验证开始！  ] -------");
		Map<Long, Guild> guilds = GuildService.getInstance().getGuilds();
		nameIds.clear();
		StringBuilder sb;
		int i;
		for (Guild guild : guilds.values()) {
			if (null == guild)
				continue;
			String name = guild.getName();
			long guildId = guild.getId();
			if (nameIds.containsKey(name)) {
				logger.info("-------[ 联盟名称 : " + name + " 重复   ] -------");
				sb = new StringBuilder().append("-TEMP");
				i = 2;
				while (true) {
					if (nameIds.containsKey(sb.toString() + i)) {
						i++;
					} else {
						sb.append(i);
						guild.setName(sb.toString());
						break;
					}
					if (i > 100) {
						sb.append(i + 1000);
						guild.setName(sb.toString());
						break;
					}
				}
			}
			nameIds.put(guild.getName(), guildId);
		}
		GuildService.getInstance().saveAll();
		logger.info("-------[   联盟重名验证完毕！  ] -------");

		logger.info("-------[清理 武道馆数据 ]------");
		ArenaRankingDao.deleteAll();
		try {
			// 清空 武道馆cache缓存
			List<ArenaRankingBean> cache = new ArrayList<>();
			Field cahceField = ArenaProcessorManager.getInstance().getClass().getDeclaredField("cache");
			cahceField.setAccessible(true);
			cahceField.set(ArenaProcessorManager.getInstance(), cache);
			cahceField.setAccessible(false);// 还原权限的访问控制

			// 清空 武道馆idMap关系对应表缓存
			Map<Long, Integer> idMap = new ConcurrentHashMap<>();
			Field idMapField = ArenaProcessorManager.getInstance().getClass().getDeclaredField("idMap");
			idMapField.setAccessible(true);
			idMapField.set(ArenaProcessorManager.getInstance(), idMap);
			idMapField.setAccessible(false);// 还原权限的访问控制

			Method method = ArenaProcessorManager.getInstance().getClass().getDeclaredMethod("initializeRanking");
			method.setAccessible(true);// 取消权限的访问控制
			method.invoke(ArenaProcessorManager.getInstance());
			method.setAccessible(false);// 还原权限的访问控制
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchFieldException e) {
			e.printStackTrace();
		}
		logger.info("-------[ 武道馆初始化完毕  ]------");
		// 在线清理武道馆数据 清理完成后 刷新在线玩家的对手 合服需注释下面代码
		// for (RoleView roleView : roles) {
		// if (null == roleView)
		// continue;
		// long playerId = roleView.getPlayerId();
		// if (playerId < Integer.MAX_VALUE)
		// continue;
		// Player player = PlayerManager.getPlayerByPlayerId(playerId);
		// if (null == player || player.getPlayerType() !=
		// PlayerType.PLAYER_VALUE) {
		// continue;
		// }
		// player.getArenaManager().loginOver();
		// }
		logger.info("-------[ 武道馆排名异常修复完毕  ]------");
		return null;
	}
}
