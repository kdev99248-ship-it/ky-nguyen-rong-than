package logic.clean;

import java.util.ArrayList;
import java.util.List;

import game.core.pub.script.IScript;
import game.server.logic.line.handler.PlayerUpdateBean;
import game.server.logic.line.handler.ReqUpdateRoleBatchHandler;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.player.RoleViewService;
import game.server.thread.PlayerRestoreProcessor;

public class CleanVipGiftScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		// try {
		// DataBaseConfig user = new
		// DataBaseConfig("jdbc:mysql://10.157.2.8:3306/user_center", "root",
		// "STGwmV0zZ3DgU0ga");
		// Driver d = new Driver();
		// d.database(user.getProperties());
		// java.sql.Connection c =
		// d.connect("jdbc:mysql://10.157.2.8:3306/user_center",
		// user.getProperties());
		// if (null != c) {
		// java.sql.Statement stat = c.createStatement();
		// ResultSet result = stat.executeQuery("SELECT web_host FROM
		// user_center.t_s_server_config");
		// while (result.next()) {
		// String url = result.getString("web_host");
		// ScriptArgs args = new ScriptArgs();
		// args.put(ScriptArgs.Key.ARG1, url);
		// ScriptJavaLoader.getInstance().call("logic.clean", args);
		// }
		// }
		// } catch (SQLException e) {
		// e.printStackTrace();
		// } catch (ScriptNotFoundException e) {
		// e.printStackTrace();
		// }
		List<Long> list = RoleViewService.getAllPlayerId();
		List<PlayerUpdateBean> beanlist = new ArrayList<>();
		for (int i = 0; i < list.size(); i++) {
			Long id = list.get(i);
			Player player = PlayerManager.getOffLinePlayerByPlayerId(id);
			if (null != player && player.getVipManager().getVipGifts().size() > 0) {
				player.getVipManager().getVipGifts().clear();
				if (!player.isOnline()) {
					PlayerUpdateBean update = new PlayerUpdateBean(player.toAccountBean(), player.toPlayerBean(),
							player.isOnline());
					beanlist.add(update);
				} else {
					System.err.println(
							"[     玩家： " + player.getPlayerName() + " id " + player.getPlayerId() + "  在线清理完成！  ]");
				}
			}

		}
		PlayerRestoreProcessor.getInstance().submitRequest(new ReqUpdateRoleBatchHandler(-1, beanlist));
		System.err.println("[ 回存更新离线玩家数量： " + beanlist.size() + "  ]");
		return null;
	}

}
