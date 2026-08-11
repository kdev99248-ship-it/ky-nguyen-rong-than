package logic.guildwar;

import org.apache.log4j.Logger;

import game.core.pub.script.IScript;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;

/**
 * 
 * @ClassName: RegisterGuildwarScript 
 * @Description: 注册推送消息
 * @author tzyx 
 * @date 2018年7月23日 下午3:45:32
 */
public class RegisterGuildwarScript implements IScript{
    private static Logger LOGGER = Logger.getLogger(RegisterGuildwarScript.class);

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
        GuildwarService service = GuildwarService.getInstance();
		LOGGER.info("注册推送消息");
		if (!service.getNotifyPlayers().contains(Long.valueOf(player.getPlayerId()))) {
			service.getNotifyPlayers().add(Long.valueOf(player.getPlayerId()));
		}
		return null;
	}

}
