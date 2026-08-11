package logic.guildwar;

import org.apache.log4j.Logger;

import game.core.pub.script.IScript;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;

/**
 * 
 * @ClassName: UnRegisterGuildwarScript 
 * @Description: 注销推送消息
 * @author tzyx 
 * @date 2018年7月23日 下午3:45:32
 */
public class UnRegisterGuildwarScript implements IScript{
    private static Logger LOGGER = Logger.getLogger(UnRegisterGuildwarScript.class);

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
        ScriptArgs argsMap = (ScriptArgs) arg;
        Player player = (Player) argsMap.get(ScriptArgs.Key.PLAYER);
		LOGGER.info("注销推送消息");
		GuildwarService.getInstance().getNotifyPlayers().remove(Long.valueOf(player.getPlayerId()));
		return null;
	}

}
