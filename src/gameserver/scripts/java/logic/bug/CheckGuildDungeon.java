package logic.bug;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.core.pub.script.IScript;
import game.server.logic.guild.GuildService;
import game.server.logic.guild.bean.Guild;

public class CheckGuildDungeon implements IScript {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		Map<Long, Guild> guilds = GuildService.getInstance().getGuilds();
		Iterator<Entry<Long, Guild>> iterator = guilds.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Long, Guild> entry = iterator.next();
			Guild g = entry.getValue();

			logger.info("[ name: " + g.getName() + " , dungeonId: " + g.getDungeon().getDungeonId() + " ]");
		}

		return null;
	}

}
