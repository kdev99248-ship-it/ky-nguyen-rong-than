package logic.basicActivity;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import Message.S2CPlayerMsg.PromptType;
import data.bean.t_day_rewardBean;
import game.core.pub.script.IScript;
import game.server.logic.backpack.BackpackService;
import game.server.logic.basicActivity.BasicActivityService;
import game.server.logic.basicActivity.bean.CarnivalActivity;
import game.server.logic.constant.Reason;
import game.server.logic.item.bean.Item;
import game.server.logic.player.Player;
import game.server.logic.util.BeanFactory;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

public class CarnivalExtRewardScript implements IScript {

	private Logger log = org.slf4j.LoggerFactory.getLogger(CarnivalExtRewardScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs args = (ScriptArgs) arg;
		Player player = (Player) args.get(ScriptArgs.Key.PLAYER);
		int index = (int) args.get(ScriptArgs.Key.ARG1);
		CarnivalActivity carnival = player.getBasicActivityManager().getCarnivalActivity();
		if (1 == carnival.getFinished(index)) {
			if (1 == carnival.getRewarded(index)) {
				MessageUtils.sendPrompt(player, PromptType.WARNING, 3010);
				log.info(" player :[ name " + player.getPlayerName() + " , id " + player.getPlayerId()
						+ " ] 每日狂欢 index条件未满足 index: " + index + " 玩家当前index已领取 " + carnival.getRewarded(index));
				return null;
			}
			// 可以领取
			List<t_day_rewardBean> list = BeanTemplet.getCarnivalExtRewardList();
			t_day_rewardBean bean = list.get(index - 1);
			if (null != bean) {
				String reward = bean.getReward();
				String[] awards = reward.split(";");
				List<Item> items = new ArrayList<>();
				for (int i = 0; i < awards.length; i++) {
					if (null == awards[i])
						continue;
					String[] award = awards[i].split(",");
					items.addAll(BeanFactory.createProps(Integer.parseInt(award[0]), Integer.parseInt(award[1])));
				}
				// 添加道具
				if (!items.isEmpty() && items.size() > 0) {
					player.getBackpackManager().addItems(items, Reason.CARNIVAL_EXT_REWARD, "每日狂欢额外完成奖励");
					carnival.updateRewarded(index);// 添加领取记录
					BasicActivityService.getInstance().notifyExtCarnivalInfoUpdate(player);
					BackpackService.getInstance().getRewardNotify(player, items);
				}

			} else {
				// 配置问题 或者玩家转入index异常
				MessageUtils.sendPrompt(player, PromptType.WARNING, 3011);
				log.info(" player :[ name " + player.getPlayerName() + " , id " + player.getPlayerId()
						+ " ] 每日狂欢 index 异常 index: " + index);
			}

		} else {
			MessageUtils.sendPrompt(player, PromptType.WARNING, 3001);
			log.info(" player :[ name " + player.getPlayerName() + " , id " + player.getPlayerId()
					+ " ] 每日狂欢 index条件未满足 index: " + index + " 玩家当前index的状态： " + carnival.getFinished(index));
		}
		return null;
	}

}
