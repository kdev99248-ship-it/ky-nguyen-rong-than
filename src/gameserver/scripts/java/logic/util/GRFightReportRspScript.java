package logic.util;

import org.apache.log4j.Logger;

import Message.S2CFightMsg.FightDataRsp;
import Message.S2CFightMsg.FightDataRspID;
import Message.Inner.GRUtil.GRFightReportRsp;
import game.core.pub.script.IScript;
import game.server.logic.player.Player;
import game.server.logic.player.PlayerManager;
import game.server.logic.util.ScriptArgs;
import game.server.util.MessageUtils;

/**
 * 战斗回放
 * @author tzyx
 *
 */
public class GRFightReportRspScript implements IScript {

	private final Logger logger = Logger.getLogger(GRFightReportRspScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		GRFightReportRsp req = (GRFightReportRsp) script.get(ScriptArgs.Key.ARG1);
		Player player = PlayerManager.getOffLinePlayerByPlayerId(req.getPlayerId());
		FightDataRsp report = req.getReport();
		// 推送
		MessageUtils.send(player, player.getFactory().fetchSMessage(FightDataRspID.FightDataRspMsgID_VALUE,
				report.toByteArray()));
		return null;
	}

}
