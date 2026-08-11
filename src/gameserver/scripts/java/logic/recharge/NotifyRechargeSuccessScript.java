package logic.recharge;

import java.io.IOException;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import game.core.pub.script.IScript;
import game.server.config.ServerConfig;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;

public class NotifyRechargeSuccessScript implements IScript {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		ScriptArgs args = (ScriptArgs) arg;
		Player player = (Player) args.get(ScriptArgs.Key.PLAYER);
		int channel = (int) args.get(ScriptArgs.Key.ARG1);
		String msg = null;
		if (null != args.get(ScriptArgs.Key.ARG2)) {
			msg = (String) args.get(ScriptArgs.Key.ARG2);
		}
		String parms = "";
		String url = ServerConfig.getInstance().getRechargeIp();// 充值服地址
		switch (channel) {
		case 1001:
		case 1002:
			// 1001 1002 应用宝渠道 统一处理
			url = url + "/recharge/yyb/callback.do";
			JSONObject json = JSON.parseObject(msg);
			json.put("debug", ServerConfig.getInstance().isTest());
			json.put("serverId", ServerConfig.getInstance().getServerId());
			json.put("roleId", player.getPlayerId());
			json.put("accountId", player.getAccountUid());
			json.put("channel", player.getChannel());
			parms = json.toJSONString();
			break;

		default:
			break;
		}
		send(player, url, parms);

		return null;
	}

	/** 应用宝支付流程
	 * 
	 * @param path 请求地址
	 *  
	 *  */
	public void send(Player player, String path, String parms) {
		logger.error("logic.recharge.NotifyRechargeSuccessScript send[ path: " +  path + " , parms: " + parms +"]");
		CloseableHttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(15000).build();
		ContentType contentType = ContentType.create("application/json", Consts.UTF_8);
		StringEntity entity = new StringEntity(parms, contentType);
		HttpPost httpPost = new HttpPost(path);
		httpPost.setEntity(entity);
		httpPost.setConfig(requestConfig);
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			int httpCode = response.getStatusLine().getStatusCode();
			if (httpCode != HttpStatus.SC_OK) {
				logger.error("---  logic.recharge.NotifyRechargeSuccessScript StatusCode is not 200 --- ");
				return;
			}
			HttpEntity responseEntity = response.getEntity();
			String text = EntityUtils.toString(responseEntity, "UTF-8");
			logger.info(" ------------ 应用宝支付流程 完成  " + text);
	
			return;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
