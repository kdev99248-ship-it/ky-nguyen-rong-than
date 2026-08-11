package logic.activity;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import game.core.pub.script.IScript;
import game.core.pub.util.DateUtils;
import game.server.logic.activity.ActivityService;
import game.server.logic.activity.bean.ExtraDropConfig;
import game.server.logic.activity.bean.FlashSaleConfig;
import game.server.logic.activity.bean.LimittimeExchangeConfig;
import game.server.logic.activity.bean.SlotMachinesConfig;
import game.server.logic.activity.bean.SuperSupplyConfig;
import game.server.logic.operateActivity.OperateActivityService;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.player.RoleViewService;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.ScriptArgs.Key;
import game.server.logic.util.TimeUtils;

/**
 * 
 * @ClassName: ActivityCheckTimeScript 
 * @Description: 检查活动超时
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class ActivityCheckTimeScript implements IScript {
    private static Logger LOGGER = Logger.getLogger(ActivityCheckTimeScript.class);

    /** 开服日 */
    private long diffDays = 0;
    
    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Object call(String scriptName, Object arg) {
		LocalDateTime today = LocalDateTime.now();
		ScriptArgs script = (ScriptArgs) arg;
		long now = (long) script.get(Key.ARG1);
    	ActivityService service = ActivityService.getInstance();
		diffDays = DateUtils.getDiffDays(OperateActivityService.getOpenTime(), now);
    	checkExtraDrop(service, now, today);
    	checkLimittimeExchange(service, now, today);
    	checkFlashSale(service, now, today);
    	checkSlotMachines(service, today);
    	checkSuperSupply(service, now, today);
		return null;
	}

	/** 检查额外掉落活动 */
    private void checkExtraDrop(ActivityService service, long now, LocalDateTime today) {
    	Map<String, ExtraDropConfig> configMap = service.getAllExtraDropConfig();
    	for (ExtraDropConfig config : configMap.values()) {
    		if (config.getOpen() != 1) {
    			continue;
    		}
    		if (config.getTimeType() == 2) {
    			List<Long> activeTime = config.getActiveTime();
    			if (activeTime.size() % 2 != 0) {
    				LOGGER.error("额外掉落活动自然时间配置错误,id:" + config.getId());
    				continue;
    			}
    			for (int i = 0; i < activeTime.size(); i++) {
					if (now <= activeTime.get(i + 1) && now >= activeTime.get(i)) {
						config.setStartTime(activeTime.get(i));
						config.setEndTime(activeTime.get(i + 1));
			    		config.setActive(1);
			    		return;
					}
				}
    		} else if (config.getTimeType() == 1) {
    			for (Integer openDay : config.getOpenServerTime()) {
					if (diffDays - openDay == 0) {
						LocalDateTime start = LocalDateTime.of(
								today.getYear(), today.getMonth(), today.getDayOfMonth(),
								BeanTemplet.getGlobalBean(196).getInt_value(), 0);
						LocalDateTime end = start.plusDays(1);
						config.setStartTime(TimeUtils.toLong(start));
						config.setEndTime(TimeUtils.toLong(end));
			    		config.setActive(1);
			    		return;
					}
				}
    		}
			config.setStartTime(0);
			config.setEndTime(0);
    		config.setActive(2);
		}
    }
    /** 检查限时兑换活动 */
    private void checkLimittimeExchange(ActivityService service, long now, LocalDateTime today) {
    	Map<String, LimittimeExchangeConfig> configMap = service.getAllLimittimeExchange();
    	for (LimittimeExchangeConfig config : configMap.values()) {
    		if (config.getOpen() != 1) {
    			continue;
    		}
    		if (config.getTimeType() == 2) {
    			List<Long> activeTime = config.getActiveTime();
    			if (activeTime.size() % 2 != 0) {
    				LOGGER.error("限时兑换活动自然时间配置错误,id:" + config.getId());
    				continue;
    			}
    			for (int i = 0; i < activeTime.size(); i+=2) {
					if (now <= activeTime.get(i + 1) && now >= activeTime.get(i)) {
						config.setStartTime(activeTime.get(i));
						config.setEndTime(activeTime.get(i + 1));
			    		config.setActive(1);
			    		return;
					}
				}
    		} else if (config.getTimeType() == 1) {
    			for (Integer openDay : config.getOpenServerTime()) {
					if (diffDays - openDay == 0) {
						LocalDateTime start = LocalDateTime.of(
								today.getYear(), today.getMonth(), today.getDayOfMonth(),
								BeanTemplet.getGlobalBean(196).getInt_value(), 0);
						LocalDateTime end = start.plusDays(1);
						config.setStartTime(TimeUtils.toLong(start));
						config.setEndTime(TimeUtils.toLong(end));
			    		config.setActive(1);
			    		return;
					}
				}
    		}
			config.setStartTime(0);
			config.setEndTime(0);
    		config.setActive(2);
		}
    }
    /** 检查限时抢购活动 */
    private void checkFlashSale(ActivityService service, long now, LocalDateTime today) {
    	Map<String, FlashSaleConfig> configMap = service.getAllFlashSale();
    	LocalDateTime openTimeLDT = OperateActivityService.getOpenTimeLDT();
    	for (FlashSaleConfig config : configMap.values()) {
    		if (config.getOpen() != 1) {
    			continue;
    		}
    		if (config.getTimeType() == 2) {
    			List<Long> activeTime = config.getActiveTime();
    			if (activeTime.size() % 3 != 0) {
    				LOGGER.error("限时抢购活动自然时间配置错误,id:" + config.getId());
    				continue;
    			}
    			for (int i = 0; i < activeTime.size(); i+=3) {
					if (now <= activeTime.get(i + 2) && now >= activeTime.get(i)) {
						config.setStartTime(activeTime.get(i));
						config.setSaleTime(activeTime.get(i + 1));
						config.setEndTime(activeTime.get(i + 2));
						config.setActive(1);
						return;
					}
				}
    		} else if (config.getTimeType() == 1) {
    			List<Integer> openServerDay = config.getOpenServerDay();
    			List<Integer> openServerTime = config.getOpenServerTime();
    			for (int i = 0; i < openServerDay.size(); i+=3) {
    				int d1 = openServerDay.get(i);
    				int d2 = openServerDay.get(i + 1);
    				int d3 = openServerDay.get(i + 2);
    				LocalDateTime startDay = openTimeLDT.plusDays(d1);
    				LocalDateTime start = LocalDateTime.of(startDay.getYear(), startDay.getMonth(), startDay.getDayOfMonth(),
    						openServerTime.get(i) / (60 * 60), openServerTime.get(i) % (60 * 60));
    				LocalDateTime saleDay = openTimeLDT.plusDays(d2);
    				LocalDateTime sale = LocalDateTime.of(saleDay.getYear(), saleDay.getMonth(), saleDay.getDayOfMonth(),
    						openServerTime.get(i + 1) / (60 * 60), openServerTime.get(i + 1) % (60 * 60));
    				LocalDateTime endDay = openTimeLDT.plusDays(d3);
    				LocalDateTime end = LocalDateTime.of(endDay.getYear(), endDay.getMonth(), endDay.getDayOfMonth(),
    						openServerTime.get(i + 2) / (60 * 60), openServerTime.get(i + 2) % (60 * 60));
					if (now <= TimeUtils.toLong(start) && now >= TimeUtils.toLong(end)) {
						config.setStartTime(TimeUtils.toLong(start));
						config.setSaleTime(TimeUtils.toLong(sale));
						config.setEndTime(TimeUtils.toLong(end));
						config.setActive(1);
						return;
    				}
    				
    			}
    		}
			config.setStartTime(0);
			config.setSaleTime(0);
			config.setEndTime(0);
    		config.setActive(2);
		}
    }

    /** 检查探险训练家活动 */
    private void checkSlotMachines(ActivityService service, LocalDateTime today) {
    	Map<String, SlotMachinesConfig> allSlotMachinesConfig = service.getAllSlotMachinesConfig();
    	long now = System.currentTimeMillis();
    	boolean flag = false;
    	// 首先检查当前活动是否超时
		SlotMachinesConfig nowConfig = service.getSlotMachinesConfig();
		if (nowConfig != null && (nowConfig.getStartTime() > now || nowConfig.getEndTime() <= now)) {
			// 活动结束开始结算
			if (nowConfig.getEndTime() <= now) {
				service.slotMachinesEnd();
			}
			SlotMachinesConfig config = allSlotMachinesConfig.get(nowConfig.getId());
			if (config == null || config.getOpen() != 1 || nowConfig.getEndTime() <= now) {
				service.setSlotMachinesConfig(null);
				service.getSlotMachinesData().clear();
				service.getSlotMachinesHistory().clear();
				service.setJackpot(0);
				service.getSlotMachinesRankList().clear();
				service.getSlotMachinesRankMap().clear();
				flag = true;
			}
    	}
    	// 只有第一条有效的配置作为当前活动
    	for (SlotMachinesConfig config : allSlotMachinesConfig.values()) {
    		if (config.getOpen() != 1) {
    			continue;
    		}
    		if (config.getStartTime() <= now && config.getEndTime() > now) {
    			if (nowConfig == null) {
    				service.setSlotMachinesConfig(config);
    				flag = true;
    			}
    		}
		}
    	if (flag) {
    		List<Player> allPlayers = PlayerManager.getAllPlayers();
    		for (Player player : allPlayers) {
				if (!RoleViewService.getRoleById(player.getPlayerId()).isRobot() && PlayerManager.isPlayerOnline(player.getPlayerId())) {
					service.slotMachinesStatusNotify(player);
				}
			}
    	}
    }
    
    private void checkSuperSupply(ActivityService service, long now, LocalDateTime today) {
		Map<String, SuperSupplyConfig> allConfig = service.getAllSuperSupplyConfig();
		SuperSupplyConfig temp = null;
		for (SuperSupplyConfig config : allConfig.values()) {
			if (config.getOpen() == 1) {
				Date startTime = new Date(config.getStartTime());
				Date endTime = new Date(config.getEndTime());
				// 只有第一条有效的配置作为当前活动
				if (DateUtils.afterNow(startTime) && DateUtils.beforeNow(endTime)) {
					temp = config;
					break;
				}
			}
		}
		// 当前生效的配置发生改变,推送所有在线玩家
		boolean flag = false;
		if (service.getSuperSupplyConfig() != null) {
			if (temp != null) {
				if (!temp.equals(service.getSuperSupplyConfig())) {
					flag = true;
				}
			} else {
				flag = true;
			}
		} else {
			if (temp != null) {
				flag = true;
			}
		}
		service.setSuperSupplyConfig(temp);
		if (flag) {
			service.superSupplyNotifyAll();
		}
	}
}
