package logic.info;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import Message.S2CPlayerMsg.PlayerType;
import game.core.pub.script.IScript;
import game.server.db.game.bean.PlayerBean;
import game.server.db.game.dao.PlayerDao;
import game.server.logic.hero.bean.Hero;
import game.server.logic.player.Player;

public class HeroStatisticScript implements IScript {

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
		Map<Integer, Integer> map = new HashMap<>();
		Map<Integer, Integer> mapStar = new HashMap<>();
		int count = PlayerDao.countByplayerType(PlayerType.PLAYER_VALUE);
		List<PlayerBean> playerBeans = PlayerDao.selectAllByPlayerType(PlayerType.PLAYER_VALUE, 0, count);
		System.out.println("count --->" + count);
		int progress = 0;
		// JSONArray bArray = new JSONArray();
		for (PlayerBean playerBean : playerBeans) {
			// JSONObject jsonObject = new JSONObject();
			Player player = new Player();
			player.loadInitialize(null, playerBean);
			// jsonObject.put("playerId", player.getPlayerId());
			// JSONArray jsonArray = new JSONArray();
			for (Hero hero : player.getHeroManager().getAllHero()) {
				JSONObject e = new JSONObject();
				e.put("heroId", hero.getId());
				e.put("lvl", hero.getLevel());
				e.put("star", hero.getStar());
				e.put("step", hero.getStep());
				// jsonArray.add(e);
				map.merge(hero.getId(), 1, (m, n) -> m + n);
				mapStar.merge(hero.getId() * 10 + hero.getStar(), 1, (m, n) -> m + n);
			}
			System.out.println("progress --->" + progress++);
			// jsonObject.put("heroList", jsonArray);
			// bArray.add(jsonObject);
		}

		// byte[] b = bArray.toJSONString().getBytes();

		// Đường dẫn tương đối theo CWD (src/gameserver) để chạy được trên Linux.
		// Bản gốc ghi thẳng ra F:\ — trên Linux sẽ tạo file tên "F:\hero.txt"
		// ngay trong thư mục server chứ không báo lỗi, rất khó lần ra.
		try {
			File file = new File("logs/hero.txt");
			if (!file.exists()) {
				file.createNewFile();
			}

			final PrintWriter writer = new PrintWriter("logs/hero.txt");
			map.keySet().stream().sorted().forEach(m -> {
				writer.println(m + " : " + map.get(m));
			});
			writer.flush();
			writer.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			File file = new File("logs/heroStar.txt");
			if (!file.exists()) {
				file.createNewFile();
			}
			final PrintWriter writer1 = new PrintWriter("logs/heroStar.txt");
			mapStar.keySet().stream().sorted().forEach(m -> {
				writer1.println(m / 10 + "_" + m % 10 + " : " + mapStar.get(m));
			});
			writer1.flush();
			writer1.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// FileOutputStream outputStream = null;
		// try {
		// outputStream = new FileOutputStream("logs/hero.txt");
		//
		// outputStream.write(b);
		//
		// outputStream.close();
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }finally {
		// if(outputStream != null) {
		// try {
		// outputStream.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// }

		return null;
	}

}
