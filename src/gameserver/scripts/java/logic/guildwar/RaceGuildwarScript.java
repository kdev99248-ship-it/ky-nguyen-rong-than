package logic.guildwar;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.log4j.Logger;

import Message.S2CGuildwarMsg.GuildwarType;
import game.core.pub.script.IScript;
import game.core.pub.script.ScriptManager;
import game.core.pub.util.ExceptionEx;
import game.core.pub.util.SimpleRandom;
import game.server.logic.chat.handler.InnerPublishMarqueeMsgHandler;
import game.server.logic.constant.ModelMsg;
import game.server.logic.fight.FightService;
import game.server.logic.fight.fightRoom.GuildWarRoom;
import game.server.logic.guild.GuildService;
import game.server.logic.guild.bean.Guild;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.guildwar.bean.GuildwarGroup;
import game.server.logic.guildwar.bean.GuildwarGuild;
import game.server.logic.guildwar.bean.GuildwarPlayer;
import game.server.logic.guildwar.bean.GuildwarRace;
import game.server.logic.guildwar.bean.GuildwarRaceDay;
import game.server.logic.guildwar.bean.GuildwarRaceRecord;
import game.server.logic.hero.bean.Hero;
import game.server.logic.util.ScriptArgs;
import game.server.thread.LogicProcessor;

/**
 * 
 * @ClassName: RaceGuildwarScript 
 * @Description: 联盟战比赛
 * @author tzyx 
 * @date 2018年8月7日 下午10:43:48
 */
public class RaceGuildwarScript implements IScript {
	
    private static Logger LOGGER = Logger.getLogger(RaceGuildwarScript.class);
    
    private int mession;
    
    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Object call(String scriptName, Object arg) {
    	Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				race();
			}
		});
    	thread.setName("guildwarThread");
    	thread.start();
		return null;
	}

	/** 比赛 */
	private void race() {
		GuildwarService service = GuildwarService.getInstance();
		LOGGER.info("联盟战战斗开始");
		if (service.getRaceDay() == GuildwarType.SCORE_VALUE) {
			// 积分赛直接使用全部参赛选手
			List<Long> fighter = new ArrayList<>(service.getAppliedPlayerList());
			List<List<Long>> group = new ArrayList<>();
			group.add(fighter);
			// 开始比赛
			LOGGER.info("联盟战积分赛战斗开始");
			race(group);
			LOGGER.info("联盟战积分赛战斗结束");
			// 如果是积分赛最后一天打完. 取积分排名前9的队伍
			if (service.getRaceDayIndex() == 4) {
				List<GuildwarGuild> l = new ArrayList<>();
				GuildwarGuild guildwarGuild;
				for (int i = 0; i < service.getGuildList().size(); i++) {
					guildwarGuild = service.getGuildMap().get(service.getGuildList().get(i));
					if (l.size() <= 9 && guildwarGuild.getScoreRank() > 0) {
						l.add(guildwarGuild);
					}
				}
				// 不满足最低六只队伍
				if (l.size() < 6) {
					// 关闭本次联盟战,不进行淘汰赛和决赛
					// 发放奖励邮件,关闭联盟战
					List<Long> ids = new ArrayList<>();
					for (int i = 0; i < l.size(); i++) {
						GuildwarGuild guild = l.get(i);
						guild.setFinalRank(9);
						ids.add(guild.getGuildId());
					}
					// 积分赛,发放参赛选手的积分奖励
					ScriptManager.getInstance().call("logic.guildwar.SendScorePrizeScript", new ScriptArgs());
					// 发放联盟战结束后的公会排名奖励奖励 
			        ScriptArgs argsMap = new ScriptArgs();
			        argsMap.put(ScriptArgs.Key.ARG1, ids);
			        argsMap.put(ScriptArgs.Key.ARG2, 41);
			        argsMap.put(ScriptArgs.Key.ARG3, 0);
					ScriptManager.getInstance().call("logic.guildwar.SendGuildRankingPrizeScript", argsMap);
					// 结束本周比赛
					service.setThisWeekRaceOver(true);
					// 休赛
					service.setGuildwarStatus(4);
					service.notifyRaceCountdown();
					service.setRefuseReason("联盟战联盟数量不足,本周联盟战结束");
					LOGGER.info("联盟战联盟数量不足,本周联盟战结束");
					return;
				}
				LOGGER.info("联盟战联盟数量满足,设置淘汰赛分组");
				// 大于6  依次分配到分组
				service.getRaceGroup().clear();
				GuildwarGroup guildwarGroup = null;
				for (int i = 0; i < l.size(); i++) {
					if (i >= 9) {
						break;
					}
					switch (i % 3) {
					case 0:
						if (service.getRaceGroup().size() - 1 >= 0) {
							guildwarGroup = service.getRaceGroup().get(0); // 第一组
						}
						break;
					case 1:
						if (service.getRaceGroup().size() - 1 >= 1) {
							guildwarGroup = service.getRaceGroup().get(1); // 第二组
						}
						break;
					case 2:
						if (service.getRaceGroup().size() - 1 >= 2) {
							guildwarGroup = service.getRaceGroup().get(2); // 第三组
						}
						break;
					default:
						break;
					}
					// 还没有对应的分组,创建分组
					if (null == guildwarGroup) {
						guildwarGroup = new GuildwarGroup();
						service.getRaceGroup().add(guildwarGroup);
					}
					guildwarGroup.getGuilds().add(l.get(i).getGuildId());
					// 设置淘汰赛名单
					service.getKnockoutGuild().add(l.get(i).getGuildId());
					// 置空,避免下一次循环继续使用
					guildwarGroup = null;
				}
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < service.getKnockoutGuild().size(); i++) {
					Guild guild = GuildService.getInstance().getGuilds().get(service.getKnockoutGuild().get(i));
					sb.append(guild.getName()).append(",");
				}
				String param = "";
				if (sb.length() > 1) {
					param = sb.substring(0, sb.length() - 1);
				}
				LogicProcessor.getInstance()
				.addCommand(new InnerPublishMarqueeMsgHandler(ModelMsg.GUILDWAR_SCORE_FINISH.value(), param));
			} else {
				LogicProcessor.getInstance()
				.addCommand(new InnerPublishMarqueeMsgHandler(ModelMsg.GUILDWAR_SCORE_OVER.value()));
			}
		} else if (service.getRaceDay() == GuildwarType.KNOCK_OUT_VALUE) {
			// 淘汰赛按分组区分参赛选手
			List<Long> fighter;
			GuildwarGroup guildwarGroup;
			List<List<Long>> group = new ArrayList<>();
			// 设置本次淘汰赛的总数
			int nu = 0;
			for (GuildwarGuild gu : service.getGuildMap().values()) {
				if (gu.getParter().size() > 0) {
					nu++;
				}
			}
			service.setLastRanking(nu);
			int addNum = 0;
			Long guildId;
			for (int i = 0; i < service.getRaceGroup().size(); i++) {
				fighter = new ArrayList<>();
				guildwarGroup = service.getRaceGroup().get(i);
				for (int j = 0; j < guildwarGroup.getGuilds().size(); j++) {
					guildId = guildwarGroup.getGuilds().get(j);
					for (GuildwarPlayer p : service.getAppliedPlayers().values()) {
						if (guildwarGroup.getGuilds().get(j) == p.getGuildId()) {
							fighter.add(p.getPlayerId());
							addNum++;
						}
					}
					// 该联盟无人报名,直接设置成最后的名次
					if (addNum == 0) {
						GuildwarGuild guildwarGuild = service.getGuildMap().get(guildId);
						if (null == guildwarGuild) {
							LOGGER.error("联盟战淘汰赛异常,找不到联盟信息");
						}
						service.getGuildMap().get(guildId).setScoreRank(service.getLastRanking());
						service.setLastRanking(service.getLastRanking() - 1);
					}
					addNum = 0;
				}
				group.add(fighter);
			}
			// 开始比赛
			LOGGER.info("联盟战淘汰赛战斗开始");
			race(group);
			LOGGER.info("联盟战淘汰赛战斗结束");
			// 根据比赛结果设置每组的胜者
			for (int i = 0; i < service.getRaceGroup().size(); i++) {
				guildId = service.getAppliedPlayers().get(group.get(i).get(0)).getGuildId();
				service.getRaceGroup().get(i).setKnockoutWinner(guildId);
				// 设置可以参加决赛的联盟
				service.getKnockoutGuild().add(guildId);
//				service.getGuildMap().get(guildId).setFinalRank(i + 1);
				service.getFinalGuild().add(guildId);
			}
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < service.getFinalGuild().size(); i++) {
				Guild guild = GuildService.getInstance().getGuilds().get(service.getFinalGuild().get(i));
				sb.append(guild.getName()).append(",");
			}
			String param = "";
			if (sb.length() > 1) {
				param = sb.substring(0, sb.length() - 1);
			}
			LogicProcessor.getInstance()
			.addCommand(new InnerPublishMarqueeMsgHandler(ModelMsg.GUILDWAR_KNOCKOUT_OVER.value(), param));
		} else if (service.getRaceDay() == GuildwarType.FINALS_VALUE) {
			// 设置本次决赛赛的总数
			int nu = 0;
			List<Long> gid = new ArrayList<>();
			for (GuildwarGuild gu : service.getGuildMap().values()) {
				if (gu.getParter().size() > 0) {
					gid.add(gu.getGuildId());
					nu++;
				}
			}
			// 没有报名的公会直接设置为第三名
			if (nu != 3) {
				for (int i = 0; i < service.getFinalGuild().size(); i++) {
					if (!gid.contains(service.getFinalGuild().get(i))) {
						GuildwarGuild guild = service.getGuild(service.getFinalGuild().get(i));
						guild.setFinalRank(3);
					}
					
				}
			}
			service.setLastRanking(nu);
			// 决赛,直接使用报名选手
			List<List<Long>> grouping = new ArrayList<>();
			List<Long> fighter = new ArrayList<>(service.getAppliedPlayerList());
			grouping.add(fighter);
			// 开始比赛
			LOGGER.info("联盟战决赛战斗开始");
			race(grouping);
			LOGGER.info("联盟战决赛战斗结束");
			// 设置冠军
			long guildId = service.getAppliedPlayers().get(grouping.get(0).get(0)).getGuildId();
			setFinnalWinner(guildId);
			LogicProcessor.getInstance()
			.addCommand(new InnerPublishMarqueeMsgHandler(ModelMsg.GUILDWAR_OVER.value(),GuildService.getInstance().getGuilds().get(guildId).getName()));
		}
		LOGGER.info("联盟战战斗结束");
		// 休赛
		service.setGuildwarStatus(4);
		service.notifyRaceCountdown();
	}
	
	/** 设置联盟战冠军 */
	private void setFinnalWinner(long guildId) {
		GuildwarService service = GuildwarService.getInstance();
		for (int i = 0; i < service.getRaceGroup().size(); i++) {
			service.getRaceGroup().get(i).setFinalWinner(guildId);
		}
		// 设置本次冠军
		service.setWinnerGuildId(guildId);
		// 增加获胜历史记录
		Integer num = service.getHistoryWinRanking().get(guildId);
		if (num == null) {
			num = 1;
			service.getHistoryWinRankingList().add(guildId);
		} else {
			num += 1;
		}
		service.setLastWinnerGuildId(guildId);
		service.getHistoryWinRanking().put(guildId, num);
		// 历史获胜排序
		service.sortHistoryGuild();
	}
	/** 比赛 */
	private void race(List<List<Long>> fighterPre) {
		GuildwarService service = GuildwarService.getInstance();
		int fightFinish;
		while (true) {
			mession = 1;
			LOGGER.info("比赛第" + service.getRound() + "轮");
			fightFinish = 0;
			for (int k = 0; k < fighterPre.size(); k++) {
				if (groupRace(fighterPre.get(k))) {
					fightFinish++;
				}
			}
			service.setRound(service.getRound() + 1);; // 增加一个轮次
			// 如果是积分赛,每轮打完根据积分重新排序联盟
			if (service.getRaceDay() == GuildwarType.SCORE_VALUE) {
				service.sortGuildScore();
			}
			// 还有分组没有决出最后胜利公会,进行下一轮
			if (fightFinish != fighterPre.size()) {
				// 下一轮开始的时间戳
				service.setNextRoundTime(System.currentTimeMillis() + (service.getRoundCountdown() * 1000));
				// 推送消息给注册玩家
				service.notifyRaceCountdown();
				try {
					LOGGER.info("开始延时");
					Thread.sleep(service.getRoundCountdown() * 1000); // 延时开始下一轮
					LOGGER.info("延时结束");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				// 比赛结束
				service.setNextRoundTime(0);
				// 修改比赛状态
				service.setTodayFinish(true);
				// 休赛
				service.setGuildwarStatus(4);
				// 推送消息给注册玩家
				service.notifyRaceCountdown();
				break;
			}
		}
	}
	/** 分组战斗 */
	private boolean groupRace(List<Long> players) {
		GuildwarService service = GuildwarService.getInstance();
		List<Long> roundWinner = new ArrayList<>();
		List<Long> fighter1 = new ArrayList<>();
		List<Long> fighter2 = new ArrayList<>();
		List<Long> playersCopy = new ArrayList<>(players);
		// 参赛者进行排序
		raceSort(players, fighter1, fighter2, roundWinner);
		List<Long> through = new ArrayList<>(roundWinner);
		// 进行正常比赛
		for (int i = 0; i < fighter1.size(); i++) {
			singleFight(service.getAppliedPlayers().get(fighter1.get(i)), service.getAppliedPlayers().get(fighter2.get(i)), roundWinner, playersCopy);
		}
		// 轮空选手生成轮空记录
		createThroughRecord(through);
		// 判断胜利者是否都是同一个公会
		long guildId = 0;
		boolean f = false;
		for (int i = 0; i < roundWinner.size(); i++) {
			if (guildId == 0) {
				guildId = service.getAppliedPlayers().get(roundWinner.get(i)).getGuildId();
			} else {
				if (guildId != service.getAppliedPlayers().get(roundWinner.get(i)).getGuildId()) {
					f = true;
					break;
				}
			}
		}
		// 从参赛者中移除失败的选手
		Iterator<Long> iterator = players.iterator();
		while (iterator.hasNext()) {
			Long id = iterator.next();
			if (!roundWinner.contains(id)) {
				iterator.remove();
			}
		}
		// 本轮获胜者同一个联盟,不打了
		if (!f) {
			LOGGER.info("联盟战小组获胜者同一个联盟,没有下一轮战斗");
			return true;
		}
		LOGGER.info("联盟战小组获胜者不是同一个联盟,继续下一轮战斗");
		return false;
	}
	
	/**
	 * 轮空选手生成战况
	 * @param through
	 */
	private void createThroughRecord(List<Long> through) {
		GuildwarService service = GuildwarService.getInstance();
		GuildService guildService = GuildService.getInstance();
		GuildwarPlayer red;
		GuildwarRaceRecord record;
		for (int i = 0; i < through.size(); i++) {
			red = service.getAppliedPlayer(through.get(i));
			record = new GuildwarRaceRecord();
			record.setId(UUID.randomUUID().toString());
			record.setRaceDay(service.getRaceDay());
			record.setRaceDayIndex(service.getRaceDayIndex());
			record.setRound(service.getRound());
			record.setSession(mession++);
			record.setReplayerId(0);
			
			record.setRedGuildId(red.getGuildId());
			record.setRedId(red.getPlayerId());
			record.setRedName(red.getPlayerName());
			record.setRedLvl(red.getLvl());
			Hero maxPowHero = red.getMaxPowHero();
			record.setRedHeroId(maxPowHero.getId());
			record.setRedSkinId(maxPowHero.getSkinId());
			record.setRedGuildName(guildService.getPlayerGuildName(red.getPlayerId()));
			record.setRedHp(red.getAllHp());
			record.setRedMaxHp(red.getAllMaxHp());
			
			record.setBlueGuildId(0);
			record.setBlueId(0);
			record.setBlueName("");
			record.setBlueLvl(0);
			record.setBlueHeroId(0);
			record.setBlueSkinId(0);
			record.setBlueGuildName("");
			record.setBlueHp(0);
			record.setBlueMaxHp(0);
			
			// 直接按红方胜利计算
			// 红方胜场不变
			record.setWinner(red.getPlayerId());
			record.setWinningStreak(red.getWinningStreak());
			record.setWinGuildName(record.getRedGuildName());
			record.setFailGuildName(record.getBlueGuildName());
			// 设置凉凉的联盟id
			record.setFinishGuildId(0);
			service.getAllRecord().put(record.getId(), record);
			saveRecord(red, null, record);
			LOGGER.info("联盟战玩家:" + red.getPlayerName() + "轮空,可进入下一轮战斗");
			if (service.getRaceDay() == GuildwarType.SCORE_VALUE) {
				// 轮空的选手获得败者积分
				GuildwarGuild redGuild = service.getGuildMap().get(red.getGuildId());
				Map<Long, Integer> allRedScore = redGuild.getAllScore();
				redGuild.setScore(redGuild.getScore() + service.getLoseScore());
				red.setScore(red.getScore() + service.getLoseScore());
				if (allRedScore.containsKey(red.getPlayerId())) {
					allRedScore.put(red.getPlayerId(), allRedScore.get(red.getPlayerId()) + service.getLoseScore());
				} else {
					allRedScore.put(red.getPlayerId(), red.getScore());
				}
			}
		}
	}
	
	/**
	 * 参赛选手排序,方便进行战斗
	 * @param players 参赛选手集合
	 * @param fighter 本轮战斗选手集合
	 * @param roundWinner 轮空到下轮次的选手集合
	 */
	private void raceSort(List<Long> players, List<Long> fighter1, List<Long> fighter2, List<Long> roundWinner) {
		GuildwarService service = GuildwarService.getInstance();
		// 判断剩余是否都是同一个公会
		long gid = 0;
		boolean flag = false;
		for (int i = 0; i < players.size(); i++) {
			if (gid == 0) {
				gid = service.getAppliedPlayers().get(players.get(i)).getGuildId();
			} else {
				if (gid != service.getAppliedPlayers().get(players.get(i)).getGuildId()) {
					flag = true;
					break;
				}
			}
		}
		if (!flag) {
			for (int i = 0; i < players.size(); i++) {
				roundWinner.add(players.get(i));
			}
		} else {
			List<Long> ids1 = new ArrayList<>(players);
			List<Long> ids2 = new ArrayList<>(players);
			GuildwarPlayer guildwarPlayer1 = null;
			GuildwarPlayer guildwarPlayer2 = null;
			while (ids2.size() > 1) {
				Collections.shuffle(ids1);
				for (int i = 0; i < ids1.size(); i++) {
					// 如果剩余的选手中没有,跳过这次循环
					if (!ids2.contains(Long.valueOf(ids1.get(i)))) {
						continue;
					}
					if (null == guildwarPlayer1) {
						guildwarPlayer1 = service.getAppliedPlayers().get(ids1.get(i));
					} else {
						guildwarPlayer2 = service.getAppliedPlayers().get(ids1.get(i));
						// 两个相同,跳过这次循环
						if (guildwarPlayer1.getGuildId() == guildwarPlayer2.getGuildId()) {
							guildwarPlayer2 = null;
							continue;
						} else {
							fighter1.add(guildwarPlayer1.getPlayerId());
							fighter2.add(guildwarPlayer2.getPlayerId());
							ids2.remove(Long.valueOf(guildwarPlayer1.getPlayerId()));
							ids2.remove(Long.valueOf(guildwarPlayer2.getPlayerId()));
							guildwarPlayer1 = null;
							guildwarPlayer2 = null;
						}
					}
				}
				// 重新复制
				ids1 = new ArrayList<>(ids2);
				// 判断剩余是否都是同一个公会
				long guildId = 0;
				boolean f = false;
				for (int i = 0; i < ids1.size(); i++) {
					if (guildId == 0) {
						guildId = service.getAppliedPlayers().get(ids1.get(i)).getGuildId();
					} else {
						if (guildId != service.getAppliedPlayers().get(ids1.get(i)).getGuildId()) {
							f = true;
							break;
						}
					}
				}
				if (!f) {
					// 剩余都是同一个公会,轮空,当作胜利者放到下一轮比赛中去
					if (!ids1.isEmpty()) {
						roundWinner.addAll(ids1);
					}
					ids2.clear();
				}
			}
		}
	} 
	/**
	 * 单场战斗
	 * @param red 进攻方
	 * @param blue 防守方
	 * @param roundWinner 获胜者
	 * @param playersCopy 所有参赛者,用于判断联盟是否全部失败
	 */
	private void singleFight(GuildwarPlayer red, GuildwarPlayer blue, List<Long> roundWinner, List<Long> playersCopy) {
		GuildwarService service = GuildwarService.getInstance();
		GuildwarRace guildwarRace;
		guildwarRace = new GuildwarRace();
		guildwarRace.setRed(red);
		guildwarRace.setBlue(blue);
		guildwarRace.setId(UUID.randomUUID().toString());
		GuildwarRaceRecord record;
		try {
			// 容错处理,避免被击败的玩家继续战斗
			if (red.getAllHp() == 0 || blue.getAllHp() == 0) {
				throw new Exception("玩家英雄空血");
			}
			LOGGER.info("联盟战单场战斗开始:" + red.getPlayerName() + "_" + red.getPlayerId() + ":" + blue.getPlayerName() + "_" + blue.getPlayerId());
			GuildWarRoom guildWarRoom = FightService.getInstance().createGuildWarFight(guildwarRace, service.getGuildMap().get(red.getGuildId()).getGainBuff(), service.getGuildMap().get(blue.getGuildId()).getGainBuff());
			record = createFightRecord(guildwarRace, guildWarRoom.getFightRepot().getUid(), guildWarRoom.getResult(), playersCopy, false);
			LOGGER.info("联盟战单场战斗结束,联盟:" + record.getWinGuildName() + ",玩家:" + record.getWinner() + "获胜");
		} catch (Exception e) {
			e.printStackTrace();
			LOGGER.error(ExceptionEx.e2s(e));
			/**
			 * 战斗出现异常，随机生成结果，避免影响联盟战整体流程，这时会没有比赛回放
			 */
			LOGGER.info("联盟战单场战斗出现异常");
			if (red.getAllHp() == 0 || blue.getAllHp() == 0) {
				record = createFightRecord(guildwarRace, 0, red.getAllHp() == 0 ? 0 : 1, playersCopy, true);
			} else {
				SimpleRandom random = new SimpleRandom();
				record = createFightRecord(guildwarRace, 0, random.next(0, 1), playersCopy, true);
			}
		}
		// 生成战斗记录
		// 保存战斗记录
		service.getAllRecord().put(record.getId(), record);
		// 参赛选手,公会保存战斗记录
		saveRecord(red, blue, record);
		// 积分赛,增加联盟积分和个人积分
		if (service.getRaceDay() == GuildwarType.SCORE_VALUE) {
			addGuildAndPlayerScore(red, blue, record, service.getGuildMap().get(red.getGuildId()), service.getGuildMap().get(blue.getGuildId()));
		}
		// 保存战斗胜利人的id
		roundWinner.add(record.getWinner());
	}
	/** 保存比赛记录 */
	private void saveRecord(GuildwarPlayer red, GuildwarPlayer blue, GuildwarRaceRecord record) {
		LOGGER.info("联盟战保存单场战斗记录");
		GuildwarService service = GuildwarService.getInstance();
		int guildwarRaceDay = service.getRaceDay() == 1 ? service.getRaceDay() + service.getRaceDayIndex() - 1 : service.getRaceDay() + 3;
		String dayTypeKey = service.getDayTypeKey();
		// ------红方
		if (red.getAllRace().containsKey(dayTypeKey)) {
			red.getAllRace().get(dayTypeKey).add(record.getId());
		} else {
			List<String> arr = new ArrayList<>();
			arr.add(record.getId());
			red.getAllRace().put(dayTypeKey, arr);
		}
		GuildwarGuild redGuild = service.getGuildMap().get(red.getGuildId());
		List<List<String>> redDayRecord = redGuild.getAllRace().get(GuildwarRaceDay.getRaceDay(guildwarRaceDay).getValue());
		// 取出或初始化指定日期的战斗记录
		if (null == redDayRecord) {
			redDayRecord = new ArrayList<>();
			redGuild.getAllRace().put(GuildwarRaceDay.getRaceDay(guildwarRaceDay).getValue(), redDayRecord);
		}
		// 取出或初始化指定轮次的战斗记录
		List<String> redRoundRecord;
		if (redDayRecord.size() < record.getRound()) {
			redRoundRecord = new ArrayList<>();
			redDayRecord.add(redRoundRecord);
		} else {
			redRoundRecord = redDayRecord.get(record.getRound() - 1);
		}
		// 添加战斗记录到指定轮次
		redRoundRecord.add(record.getId());

		// ------蓝方
		if (null != blue) {
			if (blue.getAllRace().containsKey(dayTypeKey)) {
				blue.getAllRace().get(dayTypeKey).add(record.getId());
			} else {
				List<String> arr = new ArrayList<>();
				arr.add(record.getId());
				blue.getAllRace().put(dayTypeKey, arr);
			}
			GuildwarGuild blueGuild = service.getGuildMap().get(blue.getGuildId());
			List<List<String>> blueDayRecord = blueGuild.getAllRace().get(GuildwarRaceDay.getRaceDay(guildwarRaceDay).getValue());
			// 取出或初始化指定日期的战斗记录
			if (null == blueDayRecord) {
				blueDayRecord = new ArrayList<>();
				blueGuild.getAllRace().put(GuildwarRaceDay.getRaceDay(guildwarRaceDay).getValue(), blueDayRecord);
			}
			// 取出或初始化指定轮次的战斗记录
			List<String> blueRoundRecord;
			if (blueDayRecord.size() < record.getRound()) {
				blueRoundRecord = new ArrayList<>();
				blueDayRecord.add(blueRoundRecord);
			} else {
				blueRoundRecord = blueDayRecord.get(record.getRound() - 1);
			}
			// 添加战斗记录到指定轮次
			blueRoundRecord.add(record.getId());
		}
	}
	/** 增加联盟积分和个人积分 */
	private void addGuildAndPlayerScore(GuildwarPlayer red, GuildwarPlayer blue, GuildwarRaceRecord record,
			GuildwarGuild redGuild, GuildwarGuild blueGuild) {
		GuildwarService service = GuildwarService.getInstance();
		Map<Long, Integer> allBlueScore = blueGuild.getAllScore();
		Map<Long, Integer> allRedScore = redGuild.getAllScore();
		if (blueGuild.getGuildId() == record.getWinnerGuildId()) {
			blueGuild.setScore(blueGuild.getScore() + service.getWinScore());
			blue.setScore(blue.getScore() + service.getWinScore());
			if (allBlueScore.containsKey(blue.getPlayerId())) {
				allBlueScore.put(blue.getPlayerId(), allBlueScore.get(blue.getPlayerId()) + service.getWinScore());
			} else {
				allBlueScore.put(blue.getPlayerId(), blue.getScore());
			}
			redGuild.setScore(redGuild.getScore() + service.getLoseScore());
			red.setScore(red.getScore() + service.getLoseScore());
			if (allRedScore.containsKey(red.getPlayerId())) {
				allRedScore.put(red.getPlayerId(), allRedScore.get(red.getPlayerId()) + service.getLoseScore());
			} else {
				allRedScore.put(red.getPlayerId(), red.getScore());
			}
		} else {
			blueGuild.setScore(blueGuild.getScore() + service.getLoseScore());
			blue.setScore(blue.getScore() + service.getLoseScore());
			if (allBlueScore.containsKey(blue.getPlayerId())) {
				allBlueScore.put(blue.getPlayerId(), allBlueScore.get(blue.getPlayerId()) + service.getLoseScore());
			} else {
				allBlueScore.put(blue.getPlayerId(), blue.getScore());
			}
			redGuild.setScore(redGuild.getScore() + service.getWinScore());
			red.setScore(red.getScore() + service.getWinScore());
			if (allRedScore.containsKey(red.getPlayerId())) {
				allRedScore.put(red.getPlayerId(), allRedScore.get(red.getPlayerId()) + service.getWinScore());
			} else {
				allRedScore.put(red.getPlayerId(), red.getScore());
			}
		}
	}
	/** 根据战斗结果生成比赛记录 */
	private GuildwarRaceRecord createFightRecord(GuildwarRace guildwarRace, long replayId, int result, List<Long> playersCopy, boolean error) {
		GuildwarService service = GuildwarService.getInstance();
		GuildService guildService = GuildService.getInstance();
		GuildwarRaceRecord record = new GuildwarRaceRecord();
		long failGuild = 0;
		
		record.setId(guildwarRace.getId());
		record.setRaceDay(service.getRaceDay());
		record.setRaceDayIndex(service.getRaceDayIndex());
		record.setRound(service.getRound());
		record.setSession(mession++);
		record.setReplayerId(replayId);
		
		GuildwarPlayer red = guildwarRace.getRed();
		GuildwarGuild redGuildwarGuild = service.getGuildMap().get(red.getGuildId());
		record.setRedGuildId(red.getGuildId());
		record.setRedId(red.getPlayerId());
		record.setRedName(red.getPlayerName());
		record.setRedLvl(red.getLvl());
		Hero redMaxPowHero = red.getMaxPowHero();
		record.setRedHeroId(redMaxPowHero.getId());
		record.setRedSkinId(redMaxPowHero.getSkinId());
		record.setRedGuildName(guildService.getPlayerGuildName(red.getPlayerId()));
		record.setRedHp(red.getAllHp());
		record.setRedMaxHp(red.getAllMaxHp());
		
		GuildwarPlayer blue = guildwarRace.getBlue();
		GuildwarGuild blueGuildwarGuild = service.getGuildMap().get(blue.getGuildId());
		record.setBlueGuildId(blue.getGuildId());
		record.setBlueId(blue.getPlayerId());
		record.setBlueName(blue.getPlayerName());
		record.setBlueLvl(blue.getLvl());
		Hero blueMaxPowHero = blue.getMaxPowHero();
		record.setBlueHeroId(blueMaxPowHero.getId());
		record.setBlueSkinId(blueMaxPowHero.getSkinId());
		record.setBlueGuildName(guildService.getPlayerGuildName(blue.getPlayerId()));
		record.setBlueHp(blue.getAllHp());
		record.setBlueMaxHp(blue.getAllMaxHp());
		
		// 红方战斗结果 0-失败 1-胜利 2-平
		if (result == 0) {
			blue.setWinningStreak(blue.getWinningStreak() + 1);
			// 设置公会最大连胜玩家
			if (blueGuildwarGuild.getBigWinner() == 0) {
				blueGuildwarGuild.setBigWinner(blue.getPlayerId());
			} else {
				GuildwarPlayer big = service.getAppliedPlayer(blueGuildwarGuild.getBigWinner());
				if (blue.getWinningStreak() > big.getWinningStreak()) {
					blueGuildwarGuild.setBigWinner(blue.getPlayerId());
				}
			}
			record.setWinner(blue.getPlayerId());
			record.setWinningStreak(blue.getWinningStreak());
			record.setWinGuildName(record.getBlueGuildName());
			record.setFailGuildName(record.getRedGuildName());
			redGuildwarGuild.getSurvivors().remove(Long.valueOf(red.getPlayerId()));
			failGuild = red.getGuildId();
			if (error) {
				// 在战斗异常情况下,失败的玩家英雄血量全清空
				for (Entry<Integer, Integer> en : red.getHpMap().entrySet()) {
					en.setValue(0);
				}
			}
		} else {
			red.setWinningStreak(red.getWinningStreak() + 1);
			// 设置公会最大连胜玩家
			if (redGuildwarGuild.getBigWinner() == 0) {
				redGuildwarGuild.setBigWinner(red.getPlayerId());
			} else {
				GuildwarPlayer big = service.getAppliedPlayer(redGuildwarGuild.getBigWinner());
				if (red.getWinningStreak() > big.getWinningStreak()) {
					redGuildwarGuild.setBigWinner(red.getPlayerId());
				}
			}
			record.setWinner(red.getPlayerId());
			record.setWinningStreak(red.getWinningStreak());
			record.setWinGuildName(record.getRedGuildName());
			record.setFailGuildName(record.getBlueGuildName());
			blueGuildwarGuild.getSurvivors().remove(Long.valueOf(blue.getPlayerId()));
			failGuild = blue.getGuildId();
			if (error) {
				// 在战斗异常情况下,失败的玩家英雄血量全清空
				for (Entry<Integer, Integer> en : blue.getHpMap().entrySet()) {
					en.setValue(0);
				}
			}
		}
		// 设置凉凉的联盟id
		GuildwarPlayer guildwarPlayer;
		Long playerId;
		for (int i = 0; i < playersCopy.size(); i++) {
			playerId = playersCopy.get(i);
			if (playerId == red.getPlayerId() || playerId == blue.getPlayerId()) {
				continue;
			}
			guildwarPlayer = service.getAppliedPlayers().get(playerId);
			if (guildwarPlayer.getGuildId() == failGuild) {
				failGuild = 0;
				break;
			}
		}
		record.setFinishGuildId(failGuild);
		// 决赛淘汰赛的时候公会被淘汰设置名次
		if (failGuild != 0 && (service.getRaceDay() == GuildwarType.KNOCK_OUT_VALUE
				|| service.getRaceDay() == GuildwarType.FINALS_VALUE)) {
			service.getGuildMap().get(failGuild).setFinalRank(service.getLastRanking());
			service.setLastRanking(service.getLastRanking() - 1);
		}
		// 最后的胜利者
		if (service.getLastRanking() == 1) {
			service.getGuildMap().get(record.getWinnerGuildId()).setFinalRank(1);
		}
		// 移除失败者id避免影响后续的检查
		if (result == 0) {
			playersCopy.remove(Long.valueOf(red.getPlayerId()));
		} else {
			playersCopy.remove(Long.valueOf(blue.getPlayerId()));
		}
		return record;
	}

}
