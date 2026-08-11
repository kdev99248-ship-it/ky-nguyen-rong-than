package logic.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import Message.S2CBackpackMsg.PropInfo;
import Message.Inner.GRUtil.SendSysMail2Player;
import game.core.pub.script.IScript;
import game.server.logic.constant.Reason;
import game.server.logic.mail.MailService;
import game.server.logic.util.ScriptArgs;

/**
 * 
 * @author tzyx
 *
 */
public class GRSendSysMail2PlayerScript implements IScript {

	private final Logger logger = Logger.getLogger(GRSendSysMail2PlayerScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		SendSysMail2Player req = (SendSysMail2Player) script.get(ScriptArgs.Key.ARG1);
		Map<Integer, Integer> adjunctMap = null;
		if (!req.getAdjunctMapList().isEmpty()) {
			adjunctMap = new HashMap<>();
			PropInfo propInfo;
			for (int i = 0; i < req.getAdjunctMapList().size(); i++) {
				propInfo = req.getAdjunctMapList().get(i);
				adjunctMap.put(propInfo.getId(), propInfo.getNum());
			}
		}
		MailService.getInstance().sendSysMail2Player(req.getPlayerId(), req.getType(), req.getContentParamsList(), Reason.valueOf(req.getReason())
				, req.getReasonExtra(), req.getParam(), req.getSendTime(), adjunctMap);
		return null;
	}

}
