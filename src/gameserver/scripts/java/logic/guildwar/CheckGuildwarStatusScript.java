package logic.guildwar;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.S2CGuildwarMsg.GuildwarStatus;
import Message.S2CGuildwarMsg.GuildwarType;
import game.core.pub.script.IScript;
import game.core.pub.script.ScriptManager;
import game.server.logic.chat.handler.InnerPublishMarqueeMsgHandler;
import game.server.logic.constant.ModelMsg;
import game.server.logic.guild.GuildService;
import game.server.logic.guild.bean.Guild;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.guildwar.bean.GuildwarGroup;
import game.server.logic.guildwar.bean.GuildwarGuild;
import game.server.logic.guildwar.bean.GuildwarPlayer;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.thread.LogicProcessor;

/**
 * 
 * @ClassName: CheckGuildwarStatusScript 
 * @Description: 检查时间改变联盟战活动状态
 * @author tzyx 
 * @date 2018年8月8日 上午1:03:32
 */
public class CheckGuildwarStatusScript implements IScript{
    private static Logger LOGGER = Logger.getLogger(CheckGuildwarStatusScript.class);

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
        checkGuildwarStatus();
		return null;
	}

	/** 检查时间改变联盟战活动状态 */
	private void checkGuildwarStatus() {
		GuildwarService service = GuildwarService.getInstance();
		// 本周比赛结束,不检查
		if (service.isThisWeekRaceOver() == true) {
			return;
		}
		if (service.isRaceCancel()) {
			return;
		}
		int now = LocalTime.now().minusHours(BeanTemplet.getGlobalBean(196).getInt_value()).toSecondOfDay();
		// 同一秒,终止
		if (now == service.getLastCheckTime()) {
			return;
		}
		if (service.getRaceDay() == 4) {
			return;
		}
		// 从休赛转为报名
		// 上次检查时间小于报名时间并且当前时间大于等于报名时间
		if (service.getLastCheckTime() < service.getApplyTime1()
				&& now >= service.getApplyTime1()
				) {
			// 修改阶段
			service.setGuildwarStatus(GuildwarStatus.APPLY_GUILDWAR_VALUE);
			// 修改上次检查时间,推送信息给注册玩家
			service.notifyRaceCountdown();
			service.setLastCheckTime(now);
			LOGGER.info("联盟战转为报名期");
		}
		// 从报名转为等待
		// 上次检查时间小于报名结束时间并且当前时间大于等于报名结束时间
		if (service.getGuildwarStatus() == GuildwarStatus.APPLY_GUILDWAR_VALUE
				&& service.getLastCheckTime() < service.getApplyTime2()
				&& now >= service.getApplyTime2()
				) {
			if (checkRaceCancel()) {
				return;
			}
			service.setGuildwarStatus(GuildwarStatus.WAIT_GUILDWAR_VALUE);
			List<Long> guildList = service.getGuildList();
			GuildwarGuild guild;
			for (int i = 0; i < guildList.size(); i++) {
				guild = service.getGuild(guildList.get(i));
				guild.setSurvivors(new ArrayList<>(guild.getParter()));
			}
			// 修改上次检查时间,推送信息给注册玩家
			service.notifyRaceCountdown();
			service.setLastCheckTime(now);
			LOGGER.info("联盟战转为等待期");
		}
		// 上次检查时间小于开战时间前10分钟并且当前时间大于等于开战时间前10分钟
		if (service.getGuildwarStatus() == GuildwarStatus.WAIT_GUILDWAR_VALUE
				&& service.getLastCheckTime() < service.getStartRace() - service.getNextRaceCountdown() * 60
				&& now >= service.getStartRace() - service.getNextRaceCountdown() * 60
				) {
			// 修改上次检查时间,推送信息给注册玩家
			service.notifyRaceCountdown();
			service.setLastCheckTime(now);
			if (!service.isFightStartNotify()) {
				// 开赛前走马灯提示 
				service.setFightStartNotify(true);
				LogicProcessor.getInstance()
				.addCommand(new InnerPublishMarqueeMsgHandler(ModelMsg.GUILDWAR_START_COUNT_DOWN.value()));
			}
			LOGGER.info("联盟战提示开战");
		}
		// 从等待转为比赛
		// 上次检查时间小于开战时间并且当前时间大于等于开战时间
		if (service.getGuildwarStatus() == GuildwarStatus.WAIT_GUILDWAR_VALUE
				&& service.getLastCheckTime() < service.getStartRace()
				&& now >= service.getStartRace()
				) {
			service.setGuildwarStatus(GuildwarStatus.RACE_GUILDWAR_VALUE);
			// 开始战斗
			LOGGER.info("联盟战开战");
			// 修改上次检查时间,推送信息给注册玩家
			service.notifyRaceCountdown();
			service.setLastCheckTime(now);
			ScriptManager.getInstance().call("logic.guildwar.RaceGuildwarScript", new ScriptArgs());
		}
		// 比赛结束状态改为休赛,当时间超过开战结束时间且状态为比赛也是改为休赛
		if (service.getLastCheckTime() < service.getEndRace() && now >= service.getEndRace()) {
			LOGGER.info("联盟战结束");
			service.setGuildwarStatus(GuildwarStatus.SUSPEND_GUILDWAR_VALUE);
			// 修改上次检查时间,推送信息给注册玩家
			service.notifyRaceCountdown();
			service.setLastCheckTime(now);
			if (service.getRaceDay() == GuildwarType.SCORE_VALUE) {
				// 积分赛,发放参赛选手的积分奖励
				ScriptManager.getInstance().call("logic.guildwar.SendScorePrizeScript", new ScriptArgs());
			} else if (service.getRaceDay() == GuildwarType.FINALS_VALUE) {
				// 发放联盟战结束后的公会排名奖励奖励 
		        ScriptArgs argsMap = new ScriptArgs();
		        // 根据真实排名排序
		        service.sortByGuildRank();
		        List<Long> l = new ArrayList<>();
		        Long guildId;
		        for (int j = 0; j < service.getGuildList().size(); j++) {
		        	guildId = service.getGuildList().get(j);
					if (service.getGuild(guildId).getScore() != 0) {
						l.add(guildId);
					}
				}
		        // 还原积分排名
		        service.sortGuildScore();
		        argsMap.put(ScriptArgs.Key.ARG1, l);
		        argsMap.put(ScriptArgs.Key.ARG2, 37);
		        argsMap.put(ScriptArgs.Key.ARG3, 3);
				ScriptManager.getInstance().call("logic.guildwar.SendGuildRankingPrizeScript", argsMap);
				// 决赛结束,发放助威结果邮件
				ScriptManager.getInstance().call("logic.guildwar.SendBetPrizeScript", new ScriptArgs());
				// 结束本周比赛
				service.setThisWeekRaceOver(true);
			}
		}
	}
    /** 检查比赛是否需要取消 */
    private boolean checkRaceCancel() {
    	GuildwarService service = GuildwarService.getInstance();
    	int applyGuildNum = getApplyGuildNum();
		if (service.getRaceDay() == GuildwarType.SCORE_VALUE) {
			if (applyGuildNum < 2) {
				service.setRaceCancel(true);
				// 如果是积分赛最后一天打完. 取积分排名前9的队伍
				if (service.getRaceDayIndex() == 4) {
					LOGGER.info("联盟战积分赛");
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
						service.setRefuseReason("联盟战联盟数量不足,本周联盟战结束");
						LOGGER.info("联盟战联盟数量不足,本周联盟战结束");
						LogicProcessor.getInstance()
						.addCommand(new InnerPublishMarqueeMsgHandler(ModelMsg.GUILDWAR_CANCEL.value()));
					} else {
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
						LogicProcessor.getInstance()
						.addCommand(new InnerPublishMarqueeMsgHandler(ModelMsg.GUILDWAR_TODAY_CANCEL.value()));
					}
				} else {
					LogicProcessor.getInstance()
					.addCommand(new InnerPublishMarqueeMsgHandler(ModelMsg.GUILDWAR_TODAY_CANCEL.value()));
				}
				service.setRefuseReason("联盟战参赛联盟数量不足,今日联盟战取消");
				service.notifyRaceCountdown();
				return true;
			}
		} else if (service.getRaceDay() == GuildwarType.KNOCK_OUT_VALUE) {
			if (applyGuildNum < 6) {
				service.setRaceCancel(true);
				// 结束本周比赛
				service.setThisWeekRaceOver(true);
				// 休赛
				service.setGuildwarStatus(4);
				service.notifyRaceCountdown();
				service.setRefuseReason("联盟战参赛联盟数量不足,本周联盟战结束");
				LOGGER.info("联盟战参赛联盟数量不足,本周联盟战结束");
				LogicProcessor.getInstance()
				.addCommand(new InnerPublishMarqueeMsgHandler(ModelMsg.GUILDWAR_CANCEL.value()));
				return true;
			}
		} else if (service.getRaceDay() == GuildwarType.FINALS_VALUE) {
			if (applyGuildNum < 2) {
				service.setRaceCancel(true);
				// 结束本周比赛
				service.setThisWeekRaceOver(true);
				// 休赛
				service.setGuildwarStatus(4);
				service.notifyRaceCountdown();
				service.setRefuseReason("联盟战参赛联盟数量不足,本周联盟战结束");
				LOGGER.info("联盟战参赛联盟数量不足,本周联盟战结束");
				LogicProcessor.getInstance()
				.addCommand(new InnerPublishMarqueeMsgHandler(ModelMsg.GUILDWAR_TODAY_CANCEL.value()));
				return true;
			}
		}
    	return false;
    }
    
    /** 获取报名公会的数量 */
    private int getApplyGuildNum() {
    	GuildwarService service = GuildwarService.getInstance();
    	Map<Long, GuildwarPlayer> appliedPlayers = service.getAppliedPlayers();
    	List<Long> guildIds = new ArrayList<>();
    	for (GuildwarPlayer gp : appliedPlayers.values()) {
			if (!guildIds.contains(gp.getGuildId())) {
				guildIds.add(gp.getGuildId());
			}
		}
    	return guildIds.size();
    }
}
