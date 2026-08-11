package logic.snatchTerritory;

import Message.C2SSnatchTerritoryMsg.ReqGymFightReportID;
import Message.S2CSnatchTerritoryMsg.GymReportsMsg;
import Message.S2CSnatchTerritoryMsg.RspGymFightReport;
import Message.S2CSnatchTerritoryMsg.RspGymFightReportID;
import game.core.pub.script.IScript;
import game.server.logic.log.LogService;
import game.server.logic.player.Player;
import game.server.logic.snatchTerritory.SnatchTerritoryService;
import game.server.logic.snatchTerritory.bean.GymReports;
import game.server.logic.snatchTerritory.bean.SnatchTerritory;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 请求获得战报记录
 */
public class RspGymReportScript implements IScript {

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
		int gymId = (int) script.get(ScriptArgs.Key.ARG1);
		SnatchTerritory st = SnatchTerritoryService.getInstance().getTerritoryMap().get(gymId);
		if (null != st) {
			RspGymFightReport.Builder builder = RspGymFightReport.newBuilder();
			GymReports reports = st.getReport();
			if (null != reports) {
				GymReportsMsg.Builder reportBuilder = reports.toMsgBuilder(player);
				builder.setGymReport(reportBuilder);
			}
			MessageUtils.send(player, player.getFactory()
					.fetchSMessage(RspGymFightReportID.RspGymFightReportMsgID_VALUE, builder.build().toByteArray()));

			// 玩家操作日志
			LogService.getInstance().logPlayerAction(player, ReqGymFightReportID.ReqGymFightReportMsgID_VALUE);

		}

		return null;
	}

}
