package logic.snatchTerritory;

import java.util.ArrayList;
import java.util.List;

import Message.S2CSnatchTerritoryMsg.RspReportID;
import Message.S2CSnatchTerritoryMsg.RspReportMsg;
import game.core.pub.script.IScript;
import game.server.logic.player.Player;
import game.server.logic.snatchTerritory.bean.GymReports;
import game.server.logic.snatchTerritory.bean.MineReports;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 请求获得战报记录
 */
public class RspGymFightReportScript implements IScript {

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		Player player = (Player) script.get(ScriptArgs.Key.PLAYER);
		List<MineReports> mineRports = new ArrayList<>(player.getSnatchTerritoryManager().getMineReports());
		List<GymReports> guildReports = new ArrayList<>(player.getSnatchTerritoryManager().getGuildReports());
		RspReportMsg.Builder builder = RspReportMsg.newBuilder();
		for (MineReports mineRport : mineRports) {
			if (null == mineRport)
				continue;
			builder.addMine(mineRport.toMsgBuilder(player));
		}
		for (GymReports gymReport : guildReports) {
			if (null == gymReport)
				continue;
			builder.addGuild(gymReport.toMsgBuilder(player));
		}
		MessageUtils.send(player,
				player.getFactory().fetchSMessage(RspReportID.RspReportMsgID_VALUE, builder.build().toByteArray()));
		return null;
	}

}
