package logic.bug;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Message.S2CPlayerMsg.PlayerType;
import Message.S2CRankMsg.RankType;
import game.core.pub.script.IScript;
import game.server.logic.hero.bean.Hero;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.player.RoleViewService;
import game.server.logic.rank.RankProcessor;
import game.server.logic.rank.bean.BaseRankInfo;

public class ResetRankScript implements IScript {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {

		List<Long> list = RoleViewService.getAllPlayerId();

		List<BaseRankInfo> sourceList = new ArrayList<>();// 精灵战力数据
//		List<BaseRankInfo> trainList = new ArrayList<>();// 训练家数据
		for (Long id : list) {
			Player player = PlayerManager.getOffLinePlayerByPlayerId(id);
			if (null != player && player.getPlayerType() == PlayerType.PLAYER_VALUE) {
				for (Hero hero : player.getHeroManager().getAllHero()) {
					BaseRankInfo rankInfo = new BaseRankInfo(player.getPlayerId(), hero.getPower(), hero.getPowerTime(),
							hero.getId(), hero.getStar());
					sourceList.add(rankInfo);
				}
//				RoleView roleView = player.toRoleView();
//				trainList.add(
//						new BaseRankInfo(roleView.getPlayerId(), roleView.getTrainLvl(), roleView.getTrainValue()));
			}
		}
		RankProcessor.getInstance().punRankMap(RankType.HEROPOWER_RANK, sourceList);
		//RankProcessor.getInstance().punRankMap(RankType.TRAIN_RANK, trainList);
		
		logger.info(" 精灵战力排行榜重置完成");
		
		return null;
	}

}
