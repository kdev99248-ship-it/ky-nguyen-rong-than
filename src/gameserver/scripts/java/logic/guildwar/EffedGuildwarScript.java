package logic.guildwar;


import org.apache.log4j.Logger;

import game.core.pub.script.IScript;
import game.server.logic.guildwar.GuildwarService;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;

/**
 * 
 * @ClassName: EffedGuildwarScript 
 * @Description: 请求特效播放完毕
 * @author tzyx 
 * @date 2018年7月19日 下午1:10:04
 */
public class EffedGuildwarScript implements IScript {
     private static Logger LOGGER = Logger.getLogger(EffedGuildwarScript.class);

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public Object call(String scriptName, Object arg) {
        ScriptArgs argsMap = (ScriptArgs) arg;
        Player player = (Player) argsMap.get(ScriptArgs.Key.PLAYER);
        GuildwarService service = GuildwarService.getInstance();
		if (!service.guildwarOpen(player))
        	return null;
		LOGGER.info("请求特效播放完毕");
        long playerId = player.getPlayerId();
        GuildwarService.getInstance().getEffedIds().put(playerId, playerId);
		return null;
	}

}
