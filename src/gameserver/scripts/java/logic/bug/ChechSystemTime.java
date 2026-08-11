package logic.bug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.core.pub.script.IScript;
import game.server.logic.crossIndigo.CrossIndigoService;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;

public class ChechSystemTime implements IScript {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		long[] playerIds = new long[] {537407784570L};
//		List<Long> ids = new ArrayList<>();
		for (long id : playerIds) {
			Player player = PlayerManager.getOffLinePlayerByPlayerId(id);
			if (null != player) {
				player.getCrossIndigoManager().cleanLastfinalRace();
				player.getCrossIndigoManager().cleanLastRace();

				logger.info("================player id : " + id + " , name " + player.getPlayerName());
//				ids.add(player.getPlayerId());
//				logger.info("================player CurNightmareSection : "
//						+ player.getSectionManager().getCurNightmareSection());
//				logger.info("================player NightmareChapterMap : "
//						+ player.getSectionManager().getNightmareChapterMap().toString());
//				player.getSectionManager().setCurNightmareSection(0);
//				player.getSectionManager().getNightmareChapterMap().clear();
//				logger.info("================player CurNightmareSection : "
//						+ player.getSectionManager().getCurNightmareSection());
//				logger.info("================player NightmareChapterMap : "
//						+ player.getSectionManager().getNightmareChapterMap().toString());
//				SectionService.getInstance().checkOpenChapter(player);// 检测新章节
//				SectionService.getInstance().checkOpenSection(player);// 检测新关卡
//				player.addChangePropertyKey(PlayerPropertyType.DATA);
//				player.offLineSave();
//				logger.info("================player CurNightmareSection : "
//						+ player.getSectionManager().getCurNightmareSection());
//				logger.info("================player NightmareChapterMap : "
//						+ player.getSectionManager().getNightmareChapterMap().toString());

				player.offLineSave();
			}
		}
//		logger.info("=======================================清除噩:" + ids.size());
		CrossIndigoService.getInstance().getParticipantMap().clear();
		CrossIndigoService.getInstance().getLastfinalRaceList().clear();
		logger.info("=======================================清除噩夢關卡數據");
		return null;
	}

}
