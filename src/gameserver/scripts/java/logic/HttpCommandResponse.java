package logic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;

import data.GameDataManager;
import game.core.pub.script.IScript;
import game.server.NettyGameServer;
import game.server.config.ServerConfig;
import game.server.http.mina.GameHttpServerImpl;
import game.server.logic.constant.Reason;
import game.server.logic.fight.fightRoom.SectionFightRoom;
import game.server.logic.item.bean.Item;
import game.server.logic.item.handler.AddItemsHandler;
import game.server.logic.item.handler.RemoveItemsHandler;
import game.server.logic.line.GameLineManager;
import game.server.logic.mail.MailService;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.player.RoleView;
import game.server.logic.player.RoleViewService;
import game.server.logic.util.BeanFactory;
import game.server.util.RandomName;

/**
 * Description of HttpCommandResponse
 *
 * @author Pengmian
 * @date 2015-5-21
 */
public class HttpCommandResponse implements IScript {

	private final Logger log = Logger.getLogger(HttpCommandResponse.class);

	public int getId() {
		return 1000;
	}

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@SuppressWarnings("unchecked")
	@Override
	public Object call(String scriptName, Object arg) {
		HttpExchange he = (HttpExchange) arg;
		String remoteAddress = he.getRemoteAddress().toString();
		HashMap<String, String> parameters = (HashMap<String, String>) he.getAttribute("parameters");
		String command = parameters.get("command");
		if (command == null || command.equalsIgnoreCase("")) {
			log.error("无效的Http命令");
			return GameHttpServerImpl.HttpRet.INVALID.desc();
		} else {
			log.info("收到Http请求: [" + command + "] ----------------------------------------");
			Iterator<Map.Entry<String, String>> it = parameters.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> entry = it.next();
				log.info("Key: [" + entry.getKey() + "] Value: [" + entry.getValue() + "]");
			}
			log.info("-----------------------------------------------------------------------");
		}

		if (!ServerConfig.getInstance().isTest()) {
			log.info("----INVALID_CLIENT---- isTestServer=" + ServerConfig.getInstance().isTest());
			return GameHttpServerImpl.HttpRet.ILLEGAL.desc();
		}

        String ret = GameHttpServerImpl.HttpRet.INVALID.desc();
        switch (command.toLowerCase()) {
            case "stop":
        		int time = 5;
        		new Thread(new Runnable() {
        			@Override
        			public void run() {
        				try {
        					Thread.sleep(time * 1000);
        				} catch (InterruptedException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
        				System.exit(0);
        			}
        		}).start();
        		ret = time + "秒后开始关闭服务器";
        		log.info(ret);
                System.exit(0);
                break;
            case "reloadgamedata":
                ret = reloadGameData(remoteAddress, parameters);
                break;
            case "additem":
                ret = addItem(parameters);
                break;
            case "removeitem":
                ret = removeItem(parameters);
                break;
            case "sectionfight":
                ret = sectionFight(parameters);
                break;
            case "dayreset":
                ret = dayReset(parameters);
                break;
            case "resetchampionfloor":
                ret = resetChampionFloor(parameters);
                break;
            case "sendmail":
                ret = sendMail(parameters);
                break;
            default:
                log.error("unknown command: [" + command + "]");
                break;
        }
        log.info("ret: [" + ret + "]");
        return ret;
    }

	/**
	 * 关卡战斗
	 * 
	 * @param parameters
	 * @return
	 */
	public String sectionFight(HashMap<String, String> parameters) {
		try {
			String account = String.valueOf(parameters.get("account"));
			int sectionId = Integer.parseInt(parameters.get("sectionId"));
			int serverId = Integer.parseInt(parameters.get("serverId"));
			Player player = PlayerManager.getPlayerByAccountId(account, serverId);
			if (player == null) {
				log.error("player is not find in PlayerManager");
				return GameHttpServerImpl.HttpRet.FAILED.desc();
			}
			SectionFightRoom room = new SectionFightRoom();
			room.init(player, 1, sectionId);
			room.autoFight();
			return GameHttpServerImpl.HttpRet.OK.desc();
		} catch (Exception ex) {
			log.error("sectionFight error: ", ex);
			return GameHttpServerImpl.HttpRet.FAILED.desc();
		}
	}

	/**
	 * 重置
	 * 
	 * @param parameters
	 * @return
	 */
	public String dayReset(HashMap<String, String> parameters) {
		try {
			String account = String.valueOf(parameters.get("account"));
			int id = Integer.parseInt(parameters.get("ID"));
			int serverId = Integer.parseInt(parameters.get("serverId"));
			Player player = PlayerManager.getPlayerByAccountId(account, serverId);
			if (player == null) {
				log.error("player is not find in PlayerManager");
				return GameHttpServerImpl.HttpRet.FAILED.desc();
			}
			switch (id) {
			case 1:
				player.getArenaManager().dayReset(true);
				break;
			case 2:
				player.getSectionManager().dayReset(true);
				break;
			case 3:
				player.getDrawCardManager().dayReset(true);
				break;
			case 4:
				player.getDailyTrialManager().dayReset(true);
				break;
			default:
				break;
			}

			return GameHttpServerImpl.HttpRet.OK.desc();
		} catch (Exception ex) {
			log.error("sectionFight error: ", ex);
			return GameHttpServerImpl.HttpRet.FAILED.desc();
		}
	}

	/**
	 * 重置冠军之路层数
	 * 
	 * @param parameters
	 * @return
	 */
	public String resetChampionFloor(HashMap<String, String> parameters) {
		try {
			String account = String.valueOf(parameters.get("account"));
			int floor = Integer.parseInt(parameters.get("floor"));
			int serverId = Integer.parseInt(parameters.get("serverId"));
			Player player = PlayerManager.getPlayerByAccountId(account, serverId);
			if (player == null) {
				log.error("player is not find in PlayerManager");
				return GameHttpServerImpl.HttpRet.FAILED.desc();
			}
			player.getChampionManager().setTodayFloor(floor);
			player.getChampionManager().setMaxFloor(floor);
			player.getChampionManager().setHasPassReward(false);
			player.getChampionManager().setHasFirstPassReward(false);
			player.getChampionManager().setHasOneKeyFight(false);
			player.getChampionManager().setTodayBuyCount(0);
			player.getChampionManager().setTodayFightCount(0);
			player.getChampionManager().sendChampionData();
			return GameHttpServerImpl.HttpRet.OK.desc();
		} catch (Exception ex) {
			log.error("sectionFight error: ", ex);
			return GameHttpServerImpl.HttpRet.FAILED.desc();
		}
	}

	/**
	 * 发送邮件
	 * 
	 * @param parameters
	 * @return
	 */
	public String sendMail(HashMap<String, String> parameters) {
		try {
			long playerId = Long.valueOf(parameters.get("playerId"));
			int mailId = Integer.parseInt(parameters.get("mailId"));
			RoleView roleView = RoleViewService.getRoleById(playerId);
			if (roleView == null) {
				log.error("player is not find in this server");
				return GameHttpServerImpl.HttpRet.FAILED.desc();
			}
			MailService.getInstance().sendSysMail2Player(playerId, mailId, null, Reason.GM_MAIL, "gm", null,
					System.currentTimeMillis(), null);
			return GameHttpServerImpl.HttpRet.OK.desc();
		} catch (Exception ex) {
			log.error("sectionFight error: ", ex);
			return GameHttpServerImpl.HttpRet.FAILED.desc();
		}
	}

	/**
	 * 添加道具
	 * 
	 * @param parameters
	 * @return
	 */
	public String addItem(HashMap<String, String> parameters) {
		try {
			String account = String.valueOf(parameters.get("account"));
			int itemId = Integer.parseInt(parameters.get("itemId"));
			int itemNum = Integer.parseInt(parameters.get("itemNum"));
			int serverId = Integer.parseInt(parameters.get("serverId"));
			Player player = PlayerManager.getPlayerByAccountId(account, serverId);
			if (player == null) {
				log.error("player is not find in PlayerManager");
				return GameHttpServerImpl.HttpRet.FAILED.desc();
			}
			List<Item> items = BeanFactory.createProps(itemId, itemNum);
			if (items != null && items.size() > 0) {
				// 抛给对应玩家线程处理
				GameLineManager.getInstance().addCommand(player.getLineId(),
						new AddItemsHandler(player, items, true, true, Reason.GM, "gm"));
			} else {
				return GameHttpServerImpl.HttpRet.FAILED.desc();
			}
			return GameHttpServerImpl.HttpRet.OK.desc();
		} catch (Exception ex) {
			log.error("addItem error: ", ex);
			return GameHttpServerImpl.HttpRet.FAILED.desc();
		}
	}

	/**
	 * 删除道具
	 * 
	 * @param parameters
	 * @return
	 */
	public String removeItem(HashMap<String, String> parameters) {
		try {
			String account = String.valueOf(parameters.get("account"));
			int itemId = Integer.parseInt(parameters.get("itemId"));
			int itemNum = Integer.parseInt(parameters.get("itemNum"));
			int serverId = Integer.parseInt(parameters.get("serverId"));
			Player player = PlayerManager.getPlayerByAccountId(account, serverId);
			if (player == null) {
				log.error("player is not find in PlayerManager");
				return GameHttpServerImpl.HttpRet.FAILED.desc();
			}
			List<Item> items = BeanFactory.createProps(itemId, itemNum);
			if (items != null && items.size() > 0) {
				// 抛给对应玩家线程处理
				GameLineManager.getInstance().addCommand(player.getLineId(),
						new RemoveItemsHandler(player, items, true, Reason.GM, "gm"));
			} else {
				return GameHttpServerImpl.HttpRet.FAILED.desc();
			}
			return GameHttpServerImpl.HttpRet.OK.desc();
		} catch (Exception ex) {
			log.error("addItem error: ", ex);
			return GameHttpServerImpl.HttpRet.FAILED.desc();
		}
	}

	/**
	 * 重新加载配置表, 例如: 重新加载所有配置表 http://ip:port/?command=reloadgamedata
	 *
	 * @param remoteAddress
	 * @param parameters
	 * @return
	 */
	public String reloadGameData(String remoteAddress, HashMap<String, String> parameters) {
		log.info("in reloadGameData, request");
		try {
			String param = parameters.get("param");
			if (param != null) {
				String[] token = param.split(",");
				for (String tableName : token) {
					reloadSingleGameDataTable(tableName + "Container");
				}
			} else {
				Field[] declaredFields = GameDataManager.getInstance().getClass().getDeclaredFields();
				for (Field field : declaredFields) {
					reloadSingleGameDataTable(field.getName());
				}
				RandomName.reset();
			}
			NettyGameServer.getInstance().reloadGameDataTime = System.currentTimeMillis();
			return GameHttpServerImpl.HttpRet.OK.desc();
		} catch (Exception ex) {
			log.error("reloadGameData error: ", ex);
			return GameHttpServerImpl.HttpRet.FAILED.desc();
		}
	}

	/**
	 * 加载单个配置表数据
	 *
	 * @param fieldName
	 *            GameDataManager中xxContainer
	 */
	private String reloadSingleGameDataTable(String fieldName) {
		String ret = GameHttpServerImpl.HttpRet.FAILED.desc();
		try {
			if (!fieldName.contains("Container"))
				return ret;

			Field declaredField = GameDataManager.getInstance().getClass().getDeclaredField(fieldName);
			int hashCode = declaredField.get(GameDataManager.getInstance()).hashCode();
			Class<?> cls = declaredField.getType();
			Object newInstance = cls.newInstance();
			Method method = cls.getMethod("load");
			method.invoke(newInstance);
			declaredField.set(GameDataManager.getInstance(), newInstance);
			int hashCode2 = declaredField.get(GameDataManager.getInstance()).hashCode();
			if (hashCode != hashCode2)
				log.info("reload " + fieldName + " end");
			else
				log.info("reload " + fieldName + " is faild" + hashCode + " " + hashCode2);
			ret = GameHttpServerImpl.HttpRet.OK.desc();
			return ret;
		} catch (Exception ex) {
			log.error("reloadGameData Exception", ex);
		}
		return ret;
	}

}
