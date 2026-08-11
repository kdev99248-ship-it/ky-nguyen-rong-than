package logic.recharge;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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

import data.bean.t_betaConfigBean;
import game.core.pub.script.IScript;
import game.server.config.ServerConfig;
import game.server.logic.constant.ItemType;
import game.server.logic.constant.Reason;
import game.server.logic.mail.MailService;
import game.server.logic.player.Player;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;

public class RechargeRebateScript implements IScript {

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
		getRechargeReturnAward(player);
		return null;
	}

	/** 获取返利信息 */
	public void getRechargeReturnAward(Player player) {
		int serverId = ServerConfig.getInstance().getServerId();
		String url = ServerConfig.getInstance().getRechargeIp() + "/get/retrun-award.do";
		CloseableHttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(15000).build();
		JSONObject content = new JSONObject();
		content.put("userId", player.getAccountUid());
		content.put("account", player.getAccountId());
		content.put("serverId", serverId);
		t_betaConfigBean bean = BeanTemplet.getBetaConfig(3);// 取战力前几
		content.put("limit", Integer.valueOf(bean.getInt_param()));
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
				logger.error("getRechargeReturnAward StatusCode is not 200");
				return;
			}
			HttpEntity responseEntity = response.getEntity();
			String text = EntityUtils.toString(responseEntity, "UTF-8");
			JSONObject result = JSONObject.parseObject(text);
			if (!result.containsKey("got")) {
				logger.info(" error result : " + result);
				return;
			}
			if (!result.getBooleanValue("got")) {
				logger.info("领取返利奖励： " + result);
				long resultNum = 0;
				double amount = result.getDoubleValue("amount");
				bean = BeanTemplet.getBetaConfig(1);// 返还钻石
				if ((resultNum = result.getLongValue("diamonds")) > 0) {
					sendDiamondAward(bean, player, amount, resultNum);
				}
				bean = BeanTemplet.getBetaConfig(2);// 内测玩家奖励
				if ((resultNum = result.getLongValue("power")) > 0) {
					sendBetaAward(bean, player);
				}
				bean = BeanTemplet.getBetaConfig(3);// 战力前几返利奖励
				if ((resultNum = result.getIntValue("powerRank")) > 0) {
					sendPowerRankAward(bean, player, result.getLongValue("power"), (int) resultNum);
				}
				bean = BeanTemplet.getBetaConfig(4);// vip返还是否开放
				if ((resultNum = result.getLongValue("diamonds")) > 0 && bean.getInt_param().trim().equals("1")) {
					sendVIPAward(bean, player, amount, resultNum);
				}

			}
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

	/** vip返利 */
	private void sendVIPAward(t_betaConfigBean bean, Player player, double amount, long diamonds) {
		Map<Integer, Integer> adjunctMap = new HashMap<Integer, Integer>();
		adjunctMap.put(ItemType.VIP_EXP.value(), (int) diamonds);
		List<String> contentParams = new ArrayList<>();
		contentParams.add("" + amount);
		contentParams.add("" + diamonds);
		MailService.getInstance().sendSysMail2Player(player.getPlayerId(), 103, contentParams, Reason.BETA_AWARD,
				String.valueOf(" "), null, System.currentTimeMillis(), adjunctMap);
		logger.info(" 返利vip等级  ： playerId = " + player.getPlayerId() + " , vipLevel = " + player.getVipLevel()
				+ " , amount = " + amount + " , diamonds = " + diamonds);
	}

	/** 内测玩家返利 */
	private void sendBetaAward(t_betaConfigBean bean, Player player) {
		Map<Integer, Integer> adjunctMap = new HashMap<Integer, Integer>();
		String[] str = StringUtils.split(bean.getText_param(), ";");
		if (null == str) {
			logger.info(" 内测玩家回馈奖励  ，配置错误 ：" + bean.getText_param());
			return;
		}
		logger.info(" 内测玩家回馈奖励  ，玩家id ： " + player.getPlayerId() + " 奖励发放， 掉落表道具 ：" + bean.getText_param());
		for (int j = 0; j < str.length; j++) {
			String[] strs = str[j].split(",");
			adjunctMap.put(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]));
		}
		List<String> contentParams = new ArrayList<>();
		contentParams.add(" ");
		MailService.getInstance().sendSysMail2Player(player.getPlayerId(), 101, contentParams, Reason.BETA_AWARD,
				String.valueOf(" "), null, System.currentTimeMillis(), adjunctMap);
	}

	/** 钻石返利 */
	private void sendDiamondAward(t_betaConfigBean bean, Player player, double amount, long diamonds) {
		Map<Integer, Integer> adjunctMap = new HashMap<Integer, Integer>();
		String[] str = StringUtils.split(bean.getText_param(), ";");
		if (null == str) {
			logger.info(" 内测玩家钻石返利回馈奖励  ，配置错误 ：" + bean.getText_param());
			return;
		}
		logger.info(" 内测玩家钻石返利回馈奖励  ，玩家id ： " + player.getPlayerId() + " 奖励发放， 掉落表道具 ：" + bean.getText_param()
				+ " [ 内测充值数据： " + " , amount = " + amount + " , diamonds = " + diamonds + " ]");
		int diamond = 0;
		for (int j = 0; j < str.length; j++) {
			String[] strs = str[j].split(",");
			if (amount > Double.parseDouble(strs[0]) && Double.parseDouble(strs[1]) >= amount) {
				diamond = (int) (diamonds * (Integer.parseInt(strs[2]) / 10000d));
				break;
			}
		}
		adjunctMap.put(ItemType.DIAMOND.value(), diamond);
		List<String> contentParams = new ArrayList<>();
		contentParams.add("" + amount);
		contentParams.add("" + diamond);
		MailService.getInstance().sendSysMail2Player(player.getPlayerId(), 100, contentParams, Reason.BETA_AWARD,
				String.valueOf(" "), null, System.currentTimeMillis(), adjunctMap);
	}

	/** 战力排名返利 */
	private void sendPowerRankAward(t_betaConfigBean bean, Player player, long power, int powerRank) {
		Map<Integer, Integer> adjunctMap = new HashMap<Integer, Integer>();
		String[] str = StringUtils.split(bean.getText_param(), "|");
		if (null == str) {
			logger.info(" 内测玩家战力排名回馈奖励  ，配置错误 ：" + bean.getText_param());
			return;
		}
		logger.info(" 内测玩家战力排名回馈奖励  ，玩家id ： " + player.getPlayerId() + " 奖励发放， 掉落表道具 ：" + bean.getText_param());
		int temp = 0;
		String awardStr = "";
		String first = "";
		for (int j = 0; j < str.length; j++) {
			String[] strs = str[j].split(",");
			if (Integer.parseInt(strs[0]) >= powerRank && temp <= Integer.parseInt(strs[0])) {
				awardStr = str[j];
				first = strs[0];
				break;
			}
			temp = Integer.parseInt(strs[0]);
		}
		awardStr = awardStr.replaceFirst(first + ",", "");
		str = awardStr.split(";");
		for (int j = 0; j < str.length; j++) {
			String[] strs = str[j].split(",");
			adjunctMap.put(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]));
		}
		List<String> contentParams = new ArrayList<>();
		contentParams.add(powerRank + "");
		MailService.getInstance().sendSysMail2Player(player.getPlayerId(), 102, contentParams, Reason.BETA_AWARD,
				String.valueOf(" "), null, System.currentTimeMillis(), adjunctMap);
	}

}
