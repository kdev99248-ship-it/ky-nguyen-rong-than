package logic.reload;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.core.pub.script.IScript;
import game.server.logic.activity.ActivityService;
import game.server.logic.constant.ActivityType2;

/**
 *<pre> 重载个别活动脚本
 * @author duwei
 *
 * 2019年6月20日
 */


public class ReloadActivityScript implements IScript{
	
	Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void init() {
		
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public Object call(String scriptName, Object arg) {
		//重载训练家
		ActivityService.getInstance().reloadActivity(ActivityType2.SLOT_MACHINES.value(), 0);
		return null;
	}

}
