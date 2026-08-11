package logic.util;

import org.apache.log4j.Logger;

import Message.Inner.GRUtil.PublishMarquee;
import game.core.pub.script.IScript;
import game.server.logic.chat.handler.InnerPublishMarqueeMsgHandler;
import game.server.logic.timeOpen.TimeOpenProcessor;
import game.server.logic.util.ScriptArgs;

/**
 * 
 * @author tzyx
 *
 */
public class GRPublishMarqueeScript implements IScript {

	private final Logger logger = Logger.getLogger(GRPublishMarqueeScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs script = (ScriptArgs) arg;
		PublishMarquee req = (PublishMarquee) script.get(ScriptArgs.Key.ARG1);
		String[] arr = null;
		if (!req.getParamsList().isEmpty()) {
			arr = new String[req.getParamsList().size()];
			for (int i = 0; i < req.getParamsList().size(); i++) {
				arr[i] = req.getParams(i);
			}
		}
		// 通过timeopen线程发送
		TimeOpenProcessor.getInstance().addCommand(new InnerPublishMarqueeMsgHandler(req.getModelId(), arr));
		return null;
	}

}
