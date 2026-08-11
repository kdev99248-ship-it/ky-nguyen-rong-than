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

import com.alibaba.fastjson.JSONObject;

import game.core.pub.script.IScript;
import game.server.config.ServerConfig;
import game.server.logic.player.Player;
import game.server.logic.util.ScriptArgs;

/**
 * 更新充值返利信息
 */
public class UpdateRechargeRebateScript implements IScript {

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
		long power = (long) args.get(ScriptArgs.Key.ARG1);
		double amount = (double) args.get(ScriptArgs.Key.ARG2);
		long diamonds = (long) args.get(ScriptArgs.Key.ARG3);
		boolean got = (boolean) args.get(ScriptArgs.Key.ARG4);
		int serverId = ServerConfig.getInstance().getServerId();
		String rechargeIp = ServerConfig.getInstance().getRechargeIp();
		if (null == rechargeIp || rechargeIp.trim().equals("")) {
			logger.error(" 更新返利数据失败  rechargeIp is null : amount = " + amount + " , power = " + power);
			return null;
		}
		String url = rechargeIp + "/get/update-recharge.do";
		CloseableHttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(15000).build();
		JSONObject content = new JSONObject();
		content.put("userId", player.getAccountId());
		content.put("account", player.getAccountId());
		if (got)
			content.put("gotServerId", serverId);
		content.put("server", ServerConfig.getInstance().getServerId());
		content.put("power", power);
		content.put("amount", amount);
		content.put("diamonds", diamonds);
		ContentType contentType = ContentType.create("application/json", Consts.UTF_8);
		StringEntity entity = new StringEntity(content.toJSONString(), contentType);
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(entity);
		httpPost.setConfig(requestConfig);
		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpPost);
			int httpCode = response.getStatusLine().getStatusCode();
			if (httpCode != HttpStatus.SC_OK) {
				logger.error(" UpdateRechargeRebateScript StatusCode is not 200");
				return null;
			}
			HttpEntity responseEntity = response.getEntity();
			String text = EntityUtils.toString(responseEntity, "UTF-8");
			if (!text.contains("success")) {
				logger.info(" 更新返利数据失败 error result : " + text);
				return null;
			}
			logger.info(" ------[ " + player.getPlayerId() + " 更新返利数据成功！]------ ");
			return null;
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

		return null;
	}

}
