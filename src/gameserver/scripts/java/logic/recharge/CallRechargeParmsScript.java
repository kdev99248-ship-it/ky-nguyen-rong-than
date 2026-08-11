package logic.recharge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.MessageLite.Builder;

import Message.S2CRechargeMsg.RechargeGiftOrderNotify;
import Message.S2CRechargeMsg.RechargeGiftOrderNotifyID;
import Message.S2CRechargeMsg.RechargeOrderRsp;
import Message.S2CRechargeMsg.RechargeOrderRspID;
import data.bean.t_rechargeBean;
import game.core.pub.script.IScript;
import game.server.config.ServerConfig;
import game.server.info.ChannelInfoProvider;
import game.server.logic.player.Player;
import game.server.logic.util.BeanTemplet;
import game.server.logic.util.ScriptArgs;
import game.server.logic.util.TimeUtils;
import game.server.util.MessageUtils;

/**
 * 支付下单
 * 
 * @author duwei
 *
 */
public class CallRechargeParmsScript implements IScript {

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
		int productId = (int) args.get(ScriptArgs.Key.ARG1);
		String orderId = (String) args.get(ScriptArgs.Key.ARG2);
		com.google.protobuf.MessageLite.Builder msg = (Builder) args.get(ScriptArgs.Key.ARG3);
		String parms = (String) args.get(ScriptArgs.Key.ARG4);
		t_rechargeBean bean = (t_rechargeBean) args.get(ScriptArgs.Key.ARG5);
		JSONObject json = new JSONObject();
		if (null != parms && !parms.trim().equals("")) {
			json = JSONObject.parseObject(parms);
		}
		t_rechargeBean rechargeBean = BeanTemplet.getRechargeBean(productId);
		if (rechargeBean == null) {
			logger.error("player " + player.getPlayerId() + " , 充值商品:" + productId + "不存在!");
			return false;
		}
		String amount = rechargeBean.getPrice();
		json.put("amount", amount);

		// 是否需要服务器加密
		String returnMsg = getCode(player, productId, msg, orderId, json.toJSONString(), bean);
		logger.info(String.format("玩家[roleId:%s]充值订单订单加密：%s", player.getPlayerId(), returnMsg));
		if (productId < 200 && productId != BeanTemplet.getGlobalBean(418).getInt_value() && productId != 100
				&& productId != 101) {// 钻石类商品
			RechargeOrderRsp.Builder builder = (Message.S2CRechargeMsg.RechargeOrderRsp.Builder) msg;
			if (null != returnMsg && !returnMsg.trim().equals(""))
				builder.setExtmsg(returnMsg);
			MessageUtils.send(player, player.getFactory().fetchSMessage(RechargeOrderRspID.RechargeOrderRspMsgID_VALUE,
					builder.build().toByteArray()));
		} else {
			// 购买道具
			RechargeGiftOrderNotify.Builder builder = (Message.S2CRechargeMsg.RechargeGiftOrderNotify.Builder) msg;
			if (null != returnMsg && !returnMsg.trim().equals(""))
				builder.setExtmsg(returnMsg);
			MessageUtils.send(player, player.getFactory().fetchSMessage(
					RechargeGiftOrderNotifyID.RechargeGiftOrderNotifyMsgID_VALUE, builder.build().toByteArray()));

		}
		logger.info(String.format("玩家[roleId:%s]充值订单生成成功，已发送客户端", player.getPlayerId()));
		return null;
	}

	/** uc海牛下单地址 */
	private final String UC_SEACOW_URL = "http://pokegoapi.u776.com:86/interface/uc/getOrder.php";
	/** 小米海牛下单地址 */
	private final String MI_SEACOW_URL = "http://pokegoapi.u776.com/interface/xiaomi/getOrder.php";
	/** 华为海牛下单地址 */
	private final String HUAWEI_SEACOW_URL = "http://pokegoapi.u776.com/interface/huawei/getOrder.php";
	/** vivo海牛下单地址 */
	private final String VIVO_SEACOW_URL = "http://pokegoapi.u776.com/interface/vivo/getOrder.php";
	/** oppo海牛下单地址 */
	private final String OPPO_SEACOW_URL = "http://pokegoapi.u776.com/interface/oppo/getOrder.php";
	/** 360海牛下单地址 */
	private final String S360_SEACOW_URL = "http://pokegoapi.u776.com/interface/360/getOrder.php";
	/** quickSDK海牛下单地址 */
	private final String QUICK_SEACOW_URL = "http://pokegoapi.u776.com/interface/shiniu17/getOrder.php";
	/** quickSDK2海牛下单地址 */
	private final String QUICK2_SEACOW_URL = "http://pokegoapi.u776.com/interface/hainiu17/getOrder.php";
	/** 小七海牛下单地址 */
	private final String XIAO7_SEACOW_URL = "http://pokegoapi.u776.com/interface/xiao7/getOrder.php";
	/** 金立海牛下单地址 */
	private final String JINLI_SEACOW_URL = "http://pokegoapi.u776.com/interface/jinli/getOrderV2.php";
	/** 海牛官方 */
	private final String SEACOW_SEACOW_URL = "http://pokegoapi.u776.com/interface/seacow/getOrder.php";
	/** 小七ios海牛下单地址 */
	private final String XIAO7_IOS_SEACOW_URL = "http://pokegoapi.u776.com/interface/xiao7_ios/getOrder.php";

	public String getCode(Player player, int productId, com.google.protobuf.MessageLite.Builder builder, String orderId,
			String parms, t_rechargeBean bean) {
		try {
			// 扩展参数 innerOrderId@channelId@playerId@serverId
			Map<String, String> map = new HashMap<>();
			JSONObject json = new JSONObject();
			JSONObject js = JSONObject.parseObject(parms);
			JSONObject returnJs;
			String remarkKey; // 渠道保留信息
			String key; // 渠道保留appkey
			String callbackInfo = orderId + "@" + player.getChannel() + "@" + player.getPlayerId() + "@"
					+ ServerConfig.getInstance().getServerId();
			String title = bean.getDesc();
			String desc = bean.getDesc();
			switch (player.getChannel()) {
			case "1001":
			case "1002":
				// seacowGetOrder(player, url, amount, accountId, callbackInfo,
				// orderId);
				// 应用宝 购买道具
				logger.info(
						String.format("玩家[roleId:%s]充值订单生成成功，渠道：%s，海牛应用宝！", player.getPlayerId(), player.getChannel()));
				return null;
			case "1003":
				// uc渠道
				returnJs = seacowGetOrder(player, UC_SEACOW_URL, js.getString("amount"),
						String.valueOf(player.getAccountUid()), callbackInfo, orderId);
				if (null == returnJs)
					return null;
				logger.info(String.format("玩家[roleId:%s]充值订单生成成功，渠道：%s，海牛方返回数据:%s", player.getPlayerId(),
						player.getChannel(), returnJs));
				map.put("accountId", player.getAccountId().replaceAll("UC", ""));
				map.put("amount", js.getString("amount"));
				map.put("callbackInfo", returnJs.getString("callbackInfo"));// 重新赋值扩展参数
				map.put("cpOrderId", returnJs.getString("cpOrderId"));

				json.put("amount", map.get("amount"));
				json.put("accountId", map.get("accountId"));
				json.put("cpOrderId", returnJs.getString("cpOrderId"));
				json.put("callbackInfo", returnJs.getString("callbackInfo"));
				json.put("signType", "MD5");
				remarkKey = ChannelInfoProvider.getDefault().getChannelRemarkInfo(player.getChannel());
				if (null != remarkKey && !remarkKey.trim().equals("")) {
					JSONObject remark = JSONObject.parseObject(remarkKey);
					key = remark.getString("appkey");
				} else {
					logger.info("channel : " + player.getChannel() + " remarkKey is null ! ");
					return null;
				}
				json.put("sign", signUC(map, key));
				break;

			case "1004":
				// 华为 购买进行签名
				returnJs = seacowGetOrder(player, HUAWEI_SEACOW_URL, js.getString("amount"),
						String.valueOf(player.getAccountUid()), callbackInfo, orderId);
				if (null == returnJs)
					return null;

				logger.info(String.format("玩家[roleId:%s]充值订单生成成功，渠道：%s，海牛方返回数据:%s", player.getPlayerId(),
						player.getChannel(), returnJs));

				remarkKey = ChannelInfoProvider.getDefault().getChannelRemarkInfo(player.getChannel());
				String privateKey;
				String publicKey;
				String merchantId;
				String appId;
				if (null != remarkKey && !remarkKey.trim().equals("")) {
					JSONObject remark = JSONObject.parseObject(remarkKey);
					privateKey = remark.getString("payprivatekey");
					publicKey = remark.getString("paypublickey");
					merchantId = remark.getString("cpid");
					appId = remark.getString("appid");
				} else {
					logger.info("channel : " + player.getChannel() + " remarkKey is null ! ");
					return null;
				}
				// map.put("productNo", js.getString("productNo")); 此参数在新版支付中使用
				map.put("applicationID", appId);
				map.put("merchantId", merchantId);
				map.put("sdkChannel", "3");
				map.put("requestId", returnJs.getString("cpOrderId"));
				/** 下面参数新版支付不用 **/
				map.put("productName", bean.getDesc());
				map.put("productDesc", bean.getDesc());
				map.put("amount", String.format("%.2f", js.getDoubleValue("amount")));
				String signParms = sortHuaweiSignParms(map);
				String signHuawei = signHuawei(signParms, privateKey, false, "RAS256");
				boolean check = doCheckHuawei(signParms, signHuawei, publicKey, "RAS256");
				logger.info("华为 下单  : + " + (check ? "签名自检成功！" : "签名自检失败 ！"));
				if (!check) {
					logger.info("华为 下单 : 签名自检失败 ！ 此单下单不成功 ");
					return null;
				}
				// sign = URLEncoder.encode(sign, "UTF-8");
				// json.put("productNo", map.get("productNo"));此参数在新版支付中使用
				json.put("applicationID", map.get("applicationID"));
				json.put("merchantId", map.get("merchantId"));
				json.put("sdkChannel", map.get("sdkChannel"));
				json.put("requestId", returnJs.getString("cpOrderId"));
				json.put("extReserved", returnJs.getString("callbackInfo"));
				// 下面参数新版支付不用
				json.put("productName", map.get("productName"));
				json.put("productDesc", map.get("productDesc"));
				json.put("amount", map.get("amount"));
				// 上面参数新版支付不用
				json.put("sign", signHuawei);
				break;
			case "1005":
				// 小米
				returnJs = seacowGetOrder(player, MI_SEACOW_URL, js.getString("amount"),
						String.valueOf(player.getAccountUid()), callbackInfo, orderId);
				if (null == returnJs)
					return null;

				logger.info(String.format("玩家[roleId:%s]充值订单生成成功，渠道：%s，海牛方返回数据:%s", player.getPlayerId(),
						player.getChannel(), returnJs));
				json.put("amount", js.getString("amount"));
				json.put("cpOrderId", returnJs.getString("cpOrderId"));
				json.put("cpUserInfo", returnJs.getString("callbackInfo"));
				break;
			case "1006":
				// vivo平台
				returnJs = seacowGetOrder(player, VIVO_SEACOW_URL, js.getString("amount"),
						String.valueOf(player.getAccountUid()), callbackInfo, orderId);
				if (null == returnJs)
					return null;
				logger.info(String.format("玩家[roleId:%s]充值订单生成成功，渠道：%s，海牛方返回数据:%s", player.getPlayerId(),
						player.getChannel(), returnJs));

				json = vivoGetOrderKey(player, js.getString("amount"), String.valueOf(player.getPlayerId()),
						returnJs.getString("callbackInfo"), returnJs.getString("cpOrderId"), desc, title,
						returnJs.getString("notifyUrl"));
				if (null == json) {
					logger.error("---   VIVO 返回参数错误! ");
					return null;
				}
				json.put("cpOrderId", returnJs.getString("cpOrderId"));
				json.put("cpUserInfo", returnJs.getString("callbackInfo"));
				json.put("notifyUrl", returnJs.getString("notifyUrl"));
				break;
			case "1007":
				// oppo平台
				returnJs = seacowGetOrder(player, OPPO_SEACOW_URL, js.getString("amount"),
						String.valueOf(player.getAccountUid()), callbackInfo, orderId);
				if (null == returnJs)
					return null;

				logger.info(String.format("玩家[roleId:%s]充值订单生成成功，渠道：%s，海牛方返回数据:%s", player.getPlayerId(),
						player.getChannel(), returnJs));
				json.put("amount", js.getString("amount"));
				json.put("order", returnJs.getString("cpOrderId"));
				json.put("attach", returnJs.getString("callbackInfo"));
				json.put("notifyUrl", returnJs.getString("notifyUrl"));
				break;
			case "1008":
				// 360
				returnJs = seacowGetOrder(player, S360_SEACOW_URL, js.getString("amount"),
						String.valueOf(player.getAccountUid()), callbackInfo, orderId);
				if (null == returnJs)
					return null;
				logger.info(String.format("玩家[roleId:%s]充值订单生成成功，渠道：%s，海牛方返回数据:%s", player.getPlayerId(),
						player.getChannel(), returnJs));
				json = get360OrderKey(player, js.getString("amount"), String.valueOf(player.getPlayerId()),
						returnJs.getString("callbackInfo"), returnJs.getString("cpOrderId"), desc, title,
						productId + "");
				logger.info(
						String.format("玩家[roleId:%s]充值订单生成成功，360返回数据:%s", player.getPlayerId(), json.toJSONString()));
				json.put("amount", (int) (Double.valueOf(js.getString("amount")) * 100));
				json.put("notifyUrl", returnJs.getString("notifyUrl"));
				json.put("order", returnJs.getString("cpOrderId"));
				json.put("userId", player.getAccountId().replaceFirst("360", ""));
				break;
			case "1009":
				// quick
				returnJs = seacowGetOrder(player, QUICK_SEACOW_URL, js.getString("amount"),
						String.valueOf(player.getAccountUid()), callbackInfo, orderId);
				if (null == returnJs)
					return null;
				logger.info(String.format("玩家[roleId:%s]充值订单生成成功，渠道：%s，海牛方返回数据:%s", player.getPlayerId(),
						player.getChannel(), returnJs));

				json.put("amount", js.getString("amount"));
				json.put("cpOrderId", returnJs.getString("cpOrderId"));
				json.put("cpUserInfo", returnJs.getString("callbackInfo"));
				if (returnJs.containsKey("notifyUrl"))
					json.put("notifyUrl", returnJs.getString("notifyUrl"));
				break;
			case "1010":
				// quick
				returnJs = seacowGetOrder(player, QUICK2_SEACOW_URL, js.getString("amount"),
						String.valueOf(player.getAccountUid()), callbackInfo, orderId);
				if (null == returnJs)
					return null;
				logger.info(String.format("玩家[roleId:%s]充值订单生成成功，渠道：%s，海牛方返回数据:%s", player.getPlayerId(),
						player.getChannel(), returnJs));

				json.put("amount", js.getString("amount"));
				json.put("cpOrderId", returnJs.getString("cpOrderId"));
				json.put("cpUserInfo", returnJs.getString("callbackInfo"));
				if (returnJs.containsKey("notifyUrl"))
					json.put("notifyUrl", returnJs.getString("notifyUrl"));
				break;
			case "1011":
				// 小七
				returnJs = seacowGetOrder(player, XIAO7_SEACOW_URL, js.getString("amount"),
						String.valueOf(player.getAccountUid()), callbackInfo, orderId);
				if (null == returnJs)
					return null;
				logger.info(String.format("玩家[roleId:%s]充值订单生成成功，渠道：%s，海牛方返回数据:%s", player.getPlayerId(),
						player.getChannel(), returnJs));
				remarkKey = ChannelInfoProvider.getDefault().getChannelRemarkInfo(player.getChannel());
				if (null != remarkKey && !remarkKey.trim().equals("")) {
					JSONObject remark = JSONObject.parseObject(remarkKey);
					key = remark.getString("rsakey");
				} else {
					logger.info("channel : " + player.getChannel() + " remarkKey is null ! ");
					return null;
				}
				String area = ServerConfig.getInstance().getServerName();
				String subject = String.format("%.2f", js.getDoubleValue("amount")) + "_" + bean.getDesc();
				String str = "game_area=" + area + "&game_orderid=" + returnJs.getString("cpOrderId") + "&game_price="
						+ String.format("%.2f", js.getDoubleValue("amount")) + "&subject=" + subject + key;
				String signXiao7 = md5(str);
				json.put("amount", String.format("%.2f", js.getDoubleValue("amount")));// String.format("%.2f",
																						// js.getDoubleValue("amount"))
				json.put("cpOrderId", returnJs.getString("cpOrderId"));
				json.put("cpUserInfo", returnJs.getString("callbackInfo"));
				json.put("game_area", area);
				json.put("subject", subject);
				json.put("game_sign", signXiao7);
				if (returnJs.containsKey("notifyUrl"))
					json.put("notifyUrl", returnJs.getString("notifyUrl"));
				break;
			case "1012":
				// 金立
				json = getJinliOrderKey(player, js.getString("amount"), String.valueOf(player.getPlayerId()),
						callbackInfo, orderId, desc, title, productId + "");

				if (null == json || json.isEmpty()) {
					logger.info(String.format("玩家[roleId:%s]充值订单生成失败，海牛金立返回数据:%s", player.getPlayerId(),
							((null == json || json.isEmpty()) ? "没有" : json.toJSONString())));
					return null;
				} else {
					logger.info(String.format("玩家[roleId:%s]充值订单生成成功，海牛金立返回数据:%s", player.getPlayerId(),
							json.toJSONString()));
				}

				break;
			case "1013":
			case "1016":
				// 海牛官方
				returnJs = seacowGetOrder(player, SEACOW_SEACOW_URL, js.getString("amount"),
						String.valueOf(player.getAccountUid()), callbackInfo, orderId);
				if (null == returnJs)
					return null;

				logger.info(String.format("玩家[roleId:%s]充值订单生成成功，渠道：%s，海牛方返回数据:%s", player.getPlayerId(),
						player.getChannel(), returnJs));
				json.put("amount", js.getString("amount"));
				json.put("cpOrderId", returnJs.getString("cpOrderId"));
				json.put("cpUserInfo", returnJs.getString("callbackInfo"));
				break;

			case "1015":
				// 小七ios
				returnJs = seacowGetOrder(player, XIAO7_IOS_SEACOW_URL, js.getString("amount"),
						String.valueOf(player.getAccountUid()), callbackInfo, orderId);
				if (null == returnJs)
					return null;
				logger.info(String.format("玩家[roleId:%s]充值订单生成成功，渠道：%s，海牛方返回数据:%s", player.getPlayerId(),
						player.getChannel(), returnJs));
				remarkKey = ChannelInfoProvider.getDefault().getChannelRemarkInfo(player.getChannel());
				if (null != remarkKey && !remarkKey.trim().equals("")) {
					JSONObject remark = JSONObject.parseObject(remarkKey);
					key = remark.getString("rsakey");
				} else {
					logger.info("channel : " + player.getChannel() + " remarkKey is null ! ");
					return null;
				}
				String x7ios_area = ServerConfig.getInstance().getServerName();
				String x7ios_subject = String.format("%.2f", js.getDoubleValue("amount")) + "_" + bean.getDesc();
				String x7ios_str = "game_area=" + x7ios_area + "&game_orderid=" + returnJs.getString("cpOrderId")
						+ "&game_price=" + String.format("%.2f", js.getDoubleValue("amount")) + "&subject="
						+ x7ios_subject + key;
				String signXiao7_ios = md5(x7ios_str);
				json.put("amount", String.format("%.2f", js.getDoubleValue("amount")));// String.format("%.2f",
																						// js.getDoubleValue("amount"))
				json.put("cpOrderId", returnJs.getString("cpOrderId"));
				json.put("cpUserInfo", returnJs.getString("callbackInfo"));
				json.put("game_area", x7ios_area);
				json.put("subject", x7ios_subject);
				json.put("game_sign", signXiao7_ios);
				if (returnJs.containsKey("notifyUrl"))
					json.put("notifyUrl", returnJs.getString("notifyUrl"));
				break;
			case "1101":
			case "1102":
				// 小七官方
				logger.info(String.format("玩家[roleId:%s]充值订单生成成功，渠道：%s !", player.getPlayerId(), player.getChannel()));
				remarkKey = ChannelInfoProvider.getDefault().getChannelRemarkInfo(player.getChannel());

				String token_x7 = player.getAccountInfo().getValidateCode();
				String x7_url = null, appkey = null;
				if (null != remarkKey && !remarkKey.trim().equals("")) {
					JSONObject remark = JSONObject.parseObject(remarkKey);
					key = remark.getString("rsakey");
					appkey = remark.getString("appkey");
					x7_url = remark.getString("notifyUrl");
				} else {
					logger.info("channel : " + player.getChannel() + " remarkKey is null ! ");
					return null;
				}

				boolean newVersion = js.getBooleanValue("new_version");
				String game_guid = null;
				JSONObject resultX7Data = new JSONObject();

				String x7_area = ServerConfig.getInstance().getServerName();
				String x7_subject = String.format("%.2f", js.getDoubleValue("amount")) + "_" + bean.getDesc();
				String x7_str = "";

				if (newVersion) {
					String x7_token_url = "https://api.x7sy.com/user/check_v4_login";
					String checkMd5 = md5(appkey + token_x7);
					String result = xiao7Get(x7_token_url, "tokenkey=" + token_x7 + "&sign=" + checkMd5);
					if (null == result) {
						logger.info("channel : " + player.getChannel() + " 请求guid result is null ! ");
						return null;
					}
					logger.info("result : " + result);
					JSONObject resultX7Json = JSONObject.parseObject(result);
					if (0 != resultX7Json.getIntValue("errorno")) {
						logger.info(" 请求guid结果不成功，错误反馈： " + resultX7Json.getString("errormsg"));
						return null;
					}
					resultX7Data = resultX7Json.getJSONObject("data");
					game_guid = resultX7Data.getString("guid");

					x7_str = "game_area=" + x7_area + "&game_guid=" + game_guid + "&game_orderid=" + orderId
							+ "&game_price=" + String.format("%.2f", js.getDoubleValue("amount")) + "&subject="
							+ x7_subject + key;

				} else {
					x7_str = "game_area=" + x7_area + "&game_orderid=" + orderId + "&game_price="
							+ String.format("%.2f", js.getDoubleValue("amount")) + "&subject=" + x7_subject + key;
				}

				logger.info(" signStr : " + x7_str);
				String xiao7_sign = md5(x7_str);
				logger.info(" sign : " + xiao7_sign);
				json.put("amount", String.format("%.2f", js.getDoubleValue("amount")));// String.format("%.2f",js.getDoubleValue("amount"))
				json.put("cpOrderId", orderId);
				json.put("cpUserInfo", callbackInfo);
				if(newVersion){
					json.put("is_real_user", resultX7Data.getString("is_real_user"));
					json.put("is_eighteen", resultX7Data.getString("is_eighteen"));
					json.put("game_guid", game_guid);
				}
				json.put("game_area", x7_area);
				json.put("subject", x7_subject);
				json.put("game_sign", xiao7_sign);
				if (null != x7_url && !x7_url.trim().equals(""))
					json.put("notifyUrl", x7_url);
				
				logger.info(" json : " + json.toJSONString());
				break;
			case "1201":
			case "1202":
				// 触娱
				remarkKey = ChannelInfoProvider.getDefault().getChannelRemarkInfo(player.getChannel());
				String cy_url = null;
				if (null != remarkKey && !remarkKey.trim().equals("")) {
					JSONObject remark = JSONObject.parseObject(remarkKey);
					cy_url = remark.getString("notifyUrl");
				} else {
					logger.info("channel : " + player.getChannel() + " remarkKey is null ! ");
					return null;
				}
				logger.info(String.format("玩家[roleId:%s]充值订单生成成功，渠道：%s", player.getPlayerId(), player.getChannel()));

				json.put("amount", js.getString("amount"));
				json.put("cpOrderId", orderId);
				json.put("cpUserInfo", callbackInfo);
				if (null != cy_url && !cy_url.trim().equals(""))
					json.put("notifyUrl", cy_url);
				break;
			case "1203":
				// 触娱 uc
				logger.info(String.format("玩家[roleId:%s]充值订单生成成功，渠道：%s .", player.getPlayerId(), player.getChannel()));
				map.put("accountId", player.getAccountId().replaceAll("UC", ""));
				map.put("amount", js.getString("amount"));
				map.put("callbackInfo", callbackInfo);// 重新赋值扩展参数
				map.put("cpOrderId", orderId);

				json.put("amount", map.get("amount"));
				json.put("accountId", map.get("accountId"));
				json.put("cpOrderId", orderId);
				json.put("callbackInfo", callbackInfo);
				json.put("signType", "MD5");
				remarkKey = ChannelInfoProvider.getDefault().getChannelRemarkInfo(player.getChannel());
				if (null != remarkKey && !remarkKey.trim().equals("")) {
					JSONObject remark = JSONObject.parseObject(remarkKey);
					key = remark.getString("appkey");
				} else {
					logger.info("channel : " + player.getChannel() + " remarkKey is null ! ");
					return null;
				}
				json.put("sign", signUC(map, key));
				break;
			default:
				return null;
			}
			return json.toJSONString();
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("海牛下单流程 getCode :  参数获取错误 ! ");
			return null;
		}
	}

	/** vivo下单地址 */
	private final String VIVO_TRADE_URL = "https://pay.vivo.com.cn/vcoin/trade";
	/** 360下单地址 */
	private final String S360_TRADE_URL = "https://mgame.360.cn/srvorder/get_token.json";

	/**
	 * 海牛下单
	 * 
	 * *数据库海牛配置999
	 */
	public JSONObject seacowGetOrder(Player player, String url, String amount, String accountId, String callbackInfo,
			String orderId) {
		String remarkKey = ChannelInfoProvider.getDefault().getChannelRemarkInfo(String.valueOf(999));
		String key;
		String appId;
		if (null != remarkKey && !remarkKey.trim().equals("")) {
			JSONObject remark = JSONObject.parseObject(remarkKey);
			appId = remark.getString("appid");
			key = remark.getString("appkey");
		} else {
			logger.info("channel : " + player.getChannel() + " remarkKey is null ! ");
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("accountId=" + accountId);
		sb.append("amount=" + amount);
		sb.append("appId=" + appId);
		sb.append("callbackInfo=" + callbackInfo);
		sb.append("orderId=" + orderId);
		sb.append(key);
		String sign = md5(sb.toString());

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("appId", appId));
		formparams.add(new BasicNameValuePair("amount", amount));
		formparams.add(new BasicNameValuePair("accountId", accountId));
		formparams.add(new BasicNameValuePair("callbackInfo", callbackInfo));
		formparams.add(new BasicNameValuePair("orderId", orderId));
		formparams.add(new BasicNameValuePair("sign", sign));
		// json.put("appid", appId);
		// json.put("amount", amount);
		// json.put("accountId", accountId);
		// json.put("callbackInfo", callbackInfo);
		// json.put("orderId", orderId);
		// json.put("sign", sign);
		logger.error("logic.recharge.CallRechargeParmsScript send[ path: " + url + " , parms: " + formparams.toString()
				+ "]");
		CloseableHttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(15000).build();
		// ContentType contentType = ContentType.create("application/json",
		// Consts.UTF_8);
		// StringEntity entity = new StringEntity(json.toJSONString(),
		// contentType);
		CloseableHttpResponse response = null;
		try {
			UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
			HttpPost httpPost = new HttpPost(url);
			httpPost.setEntity(uefEntity);
			httpPost.setConfig(requestConfig);
			response = httpClient.execute(httpPost);
			int httpCode = response.getStatusLine().getStatusCode();
			if (httpCode != HttpStatus.SC_OK) {
				logger.error("---  logic.recharge.CallRechargeParmsScript StatusCode is not 200 --- ");
				return null;
			}
			HttpEntity responseEntity = response.getEntity();
			String text = EntityUtils.toString(responseEntity, "UTF-8");
			logger.info(" ------------ result : " + text);
			JSONObject json = JSONObject.parseObject(text);
			String status = json.getString("status");
			String msg = json.getString("msg");
			if (!status.trim().equals("0000")) {
				logger.error("---  logic.recharge.CallRechargeParmsScript 海牛返回参数错误码: " + status + " , msg : " + msg);
				return null;
			}

			return json.getJSONObject("data");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
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

	/**
	 * vivo获取支付信息
	 */
	public JSONObject vivoGetOrderKey(Player player, String amount, String accountId, String callbackInfo,
			String orderId, String desc, String title, String notifyUrl) {
		String remarkKey = ChannelInfoProvider.getDefault().getChannelRemarkInfo(player.getChannel());
		String key;
		String appId;
		String cpId;
		if (null != remarkKey && !remarkKey.trim().equals("")) {
			JSONObject remark = JSONObject.parseObject(remarkKey);
			appId = remark.getString("appid");
			key = remark.getString("appkey");
			cpId = remark.getString("cpid");
		} else {
			logger.info("channel : " + player.getChannel() + " remarkKey is null ! ");
			return null;
		}
		int intAmount = (int) (Double.valueOf(amount) * 100);
		String dateStr = TimeUtils.dateToString(new Date(), "yyyyMMddHHmmss");
		StringBuffer sb = new StringBuffer();
		sb.append("appId=" + appId);
		sb.append("&cpId=" + cpId);
		sb.append("&cpOrderNumber=" + orderId);
		sb.append("&extInfo=" + callbackInfo);
		sb.append("&notifyUrl=" + notifyUrl);
		sb.append("&orderAmount=" + intAmount);// 单位：分必须为整数
		sb.append("&orderDesc=" + desc);
		sb.append("&orderTime=" + dateStr);
		sb.append("&orderTitle=" + title);
		sb.append("&version=1.0.0");
		sb.append("&" + md5(key).toLowerCase());
		String sign = md5(sb.toString()).toLowerCase();

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("appId", appId));
		formparams.add(new BasicNameValuePair("cpId", cpId));
		formparams.add(new BasicNameValuePair("extInfo", callbackInfo));
		formparams.add(new BasicNameValuePair("cpOrderNumber", orderId));
		formparams.add(new BasicNameValuePair("notifyUrl", notifyUrl));
		formparams.add(new BasicNameValuePair("orderAmount", intAmount + ""));
		formparams.add(new BasicNameValuePair("orderDesc", desc));
		formparams.add(new BasicNameValuePair("orderTime", dateStr));
		formparams.add(new BasicNameValuePair("orderTitle", title));
		formparams.add(new BasicNameValuePair("version", "1.0.0"));
		formparams.add(new BasicNameValuePair("signMethod", "MD5"));
		formparams.add(new BasicNameValuePair("signature", sign));
		JSONObject json = new JSONObject();
		logger.error("logic.recharge.CallRechargeParmsScript send[ path: " + VIVO_TRADE_URL + " , parms: "
				+ formparams.toString() + "]");
		CloseableHttpClient httpClient = HttpClients.createDefault();
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(15000).build();
		CloseableHttpResponse response = null;
		try {
			UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
			HttpPost httpPost = new HttpPost(VIVO_TRADE_URL);
			httpPost.setEntity(uefEntity);
			httpPost.setConfig(requestConfig);
			response = httpClient.execute(httpPost);
			int httpCode = response.getStatusLine().getStatusCode();
			if (httpCode != HttpStatus.SC_OK) {
				logger.error("---  logic.recharge.CallRechargeParmsScript StatusCode is not 200 --- ");
				return null;
			}
			HttpEntity responseEntity = response.getEntity();
			String text = EntityUtils.toString(responseEntity, "UTF-8");
			logger.info(" 下单 VIVO 返回------------ result : " + text);
			json = JSONObject.parseObject(text);
			String status = json.getString("respCode");
			String msg = json.getString("respMsg");
			if (!status.trim().equals("200")) {
				logger.error("---  logic.recharge.CallRechargeParmsScript VIVO 返回参数错误码: " + status + " , msg : " + msg);
				return null;
			}

			return json;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
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

	/**
	 * 360获取支付信息
	 */
	public JSONObject get360OrderKey(Player player, String amount, String accountId, String callbackInfo,
			String orderId, String desc, String title, String productId) {
		String remarkKey = ChannelInfoProvider.getDefault().getChannelRemarkInfo(player.getChannel());
		String key;
		String appSecret;
		if (null != remarkKey && !remarkKey.trim().equals("")) {
			JSONObject remark = JSONObject.parseObject(remarkKey);
			key = remark.getString("appkey");
			appSecret = remark.getString("appSecret");
		} else {
			logger.info("channel : " + player.getChannel() + " remarkKey is null ! ");
			return null;
		}
		// amount#app_ext1#app_key#app_order_id#app_uid#app_uname#product_id#product_name#sign_type#user_id#appsecret
		int intAmount = (int) (Double.valueOf(amount) * 100);
		StringBuffer sb = new StringBuffer();
		sb.append("" + intAmount);// 单位：分必须为整数 amount
		sb.append("#" + callbackInfo); // app_ext1
		sb.append("#" + key); // app_key
		sb.append("#" + orderId); // app_order_id
		sb.append("#" + player.getPlayerId());// app_uid
		sb.append("#" + player.getPlayerName());// app_uname
		sb.append("#" + productId);// product_id
		sb.append("#" + desc);// product_name
		sb.append("#md5");// sign_type
		sb.append("#" + player.getAccountId().replaceFirst("360", ""));// user_id
		sb.append("#" + appSecret);
		String sign = md5(sb.toString()).toLowerCase();
		CloseableHttpResponse response = null;
		try {
			sb.setLength(0);
			sb.append("amount=" + URLEncoder.encode(intAmount + "", "UTF-8"));// 单位：分必须为整数
			sb.append("&app_ext1=" + URLEncoder.encode(callbackInfo, "UTF-8"));
			sb.append("&app_key=" + URLEncoder.encode(key, "UTF-8"));
			sb.append("&app_order_id=" + URLEncoder.encode(orderId, "UTF-8"));
			sb.append("&app_uid=" + URLEncoder.encode(player.getPlayerId() + "", "UTF-8"));
			sb.append("&app_uname=" + URLEncoder.encode(player.getPlayerName(), "UTF-8"));
			sb.append("&product_id=" + URLEncoder.encode(productId, "UTF-8"));
			sb.append("&product_name=" + URLEncoder.encode(desc, "UTF-8"));
			sb.append("&sign_type=md5");
			sb.append("&user_id=" + URLEncoder.encode(player.getAccountId().replaceFirst("360", ""), "UTF-8"));
			sb.append("&sign=" + sign);
			JSONObject json = new JSONObject();
			logger.error("logic.recharge.CallRechargeParmsScript send[ path: " + S360_TRADE_URL + " , parms: "
					+ sb.toString() + "]");
			CloseableHttpClient httpClient = HttpClients.createDefault();
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(15000)
					.build();
			HttpGet httpGet = new HttpGet(S360_TRADE_URL + "?" + sb.toString());
			httpGet.setConfig(requestConfig);
			response = httpClient.execute(httpGet);
			int httpCode = response.getStatusLine().getStatusCode();
			if (httpCode != HttpStatus.SC_OK) {
				logger.error("---  logic.recharge.CallRechargeParmsScript 360 StatusCode is not 200 --- ");
				return null;
			}
			HttpEntity responseEntity = response.getEntity();
			String text = EntityUtils.toString(responseEntity, "UTF-8");
			logger.info(" 下单 360 返回------------ result : " + text);
			json = JSONObject.parseObject(text);
			if (!json.containsKey("token_id") && !json.containsKey("order_token")) {
				logger.error("---  logic.recharge.CallRechargeParmsScript 360 返回参数错误: " + text);
				return null;
			}
			return json;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
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

	/**
	 * 海牛 -金立获取支付信息
	 */
	public JSONObject getJinliOrderKey(Player player, String amount, String accountId, String callbackInfo,
			String orderId, String desc, String title, String productId) {
		String remarkKey = ChannelInfoProvider.getDefault().getChannelRemarkInfo(String.valueOf(999));
		String key;
		String appId;
		if (null != remarkKey && !remarkKey.trim().equals("")) {
			JSONObject remark = JSONObject.parseObject(remarkKey);
			key = remark.getString("appkey");
			appId = remark.getString("appid");
		} else {
			logger.info("channel : " + player.getChannel() + " remarkKey is null ! ");
			return null;
		}

		String amountStr = String.format("%.2f", Double.valueOf(amount));
		String timestamp = TimeUtils.dateToString(new Date(), "yyyyMMddHHmmss");
		StringBuffer sb = new StringBuffer();
		sb.append("app_id=" + appId);
		sb.append("deal_price=" + amountStr);
		sb.append("deliver_type=" + 1);
		sb.append("ext_info=" + callbackInfo);
		sb.append("out_order_no=" + orderId);
		sb.append("subject=" + desc);
		sb.append("submit_time=" + timestamp);
		sb.append("total_fee=" + amountStr);
		sb.append("user_id=" + player.getAccountId().replaceFirst("JL", ""));
		sb.append(key);
		String sign = md5(sb.toString());

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("app_id", appId));
		// formparams.add(new BasicNameValuePair("amount", amountStr));
		formparams.add(new BasicNameValuePair("user_id", player.getAccountId().replaceFirst("JL", "")));
		formparams.add(new BasicNameValuePair("out_order_no", orderId));
		formparams.add(new BasicNameValuePair("subject", desc));
		formparams.add(new BasicNameValuePair("submit_time", timestamp));
		formparams.add(new BasicNameValuePair("total_fee", amountStr));
		formparams.add(new BasicNameValuePair("deal_price", amountStr));
		formparams.add(new BasicNameValuePair("deliver_type", 1 + ""));
		formparams.add(new BasicNameValuePair("ext_info", callbackInfo));
		formparams.add(new BasicNameValuePair("sign", sign));
		CloseableHttpResponse response = null;
		try {
			logger.error(
					"海牛金立 send[ path: " + JINLI_SEACOW_URL + " , parms: " + sb.toString() + " , sign = " + sign + " ]");
			CloseableHttpClient httpClient = HttpClients.createDefault();
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(15000)
					.build();
			UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
			HttpPost httpPost = new HttpPost(JINLI_SEACOW_URL);
			httpPost.setEntity(uefEntity);
			httpPost.setConfig(requestConfig);
			response = httpClient.execute(httpPost);
			int httpCode = response.getStatusLine().getStatusCode();
			if (httpCode != HttpStatus.SC_OK) {
				logger.error("--- 海牛http响应状态码  StatusCode is not 200 --- ");
				return null;
			}
			HttpEntity responseEntity = response.getEntity();
			String text = EntityUtils.toString(responseEntity, "UTF-8");
			logger.info(" --- 海牛金立 ---- result : " + text);
			JSONObject json = JSONObject.parseObject(text);
			String status = json.getString("status");
			String msg = json.getString("msg");
			if (!status.trim().equals("0000")) {
				logger.error("---   海牛金立 返回参数错误码: " + status + " , msg : " + msg);
				return null;
			}
			return json.getJSONObject("data");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
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

	/**
	 * 小7 GET请求
	 */
	public static String xiao7Get(String payUrl, String payParam) {

		String result = "";
		try {
			/************* 请 * 求 * 方 * 法 *************/
			BufferedReader in = null;
			String urlNameString = payUrl + "?" + payParam;
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			connection.connect();
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}

			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			return result;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 应用宝 购买道具 购买道具仅限单机游戏
	 */
	@Deprecated
	public static String yybGetBuyItemUrl(Player player, int productId, com.google.protobuf.MessageLite.Builder builder,
			String orderId, String parms) {

		// appid： 应用的唯一ID。可以通过appid查找APP基本信息。
		// ts： UNIX时间戳（从格林威治时间1970年01月01日00时00分00秒起至现在的总秒数）。
		// payitem：
		// 请使用x*p*num的格式，x表示物品ID，p表示单价（以Q点为单位，1Q币=10Q点，单价的制定需遵循腾讯定价规范），num表示默认的购买数量。（格式：物品ID1*单价1*
		// 建议数量1，批量购买物品时使用;分隔，如：id1*price1*num1;id2*price2*num2)长度必须<=512
		// goodsmeta： 物品信息，格式必须是“name*des”，批量购买套餐时也只能有1个道具名称和1个描述，即给出该套餐的名称和描述。
		// name表示物品的名称，des表示物品的描述信息。用户购买物品的确认支付页面，将显示该物品信息。长度必须<=256字符，必须使用utf8编码。目前goodsmeta超过76个字符后不能添加回车字符。
		// goodsurl： 物品的图片url(长度<512字符)
		// sig： 请求串的签名，参考Sig签名计算。
		// pf： 平台来源，参考公共参数说明。
		// pfkey： 跟平台来源和openkey根据规则生成的一个密钥串。如果是腾讯自研应用固定传递pfkey=”pfkey”
		// zoneid： 账户分区ID。应用如果没有分区：传zoneid=1
		// amt： (可选)道具总价格。（amt必须等于所有物品：单价*建议数量的总和 单位为1Q点）
		// max_num： (可选)
		// 用户可购买的道具数量的最大值。仅当appmode的值为2时，可以输入该参数。输入的值需大于参数“payitem”中的num，如果小于num，则自动调整为num的值。
		// appmode： (可选)1表示用户不可以修改物品数量，2 表示用户可以选择购买物品的数量。默认2（批量购买的时候，必须等于1）
		// app_metadata：（可选）发货时透传给应用。长度必须<=128字符
		// userip： （可选）用户的外网IP
		// format： (可选）json、jsonp_$func。默认json。如果jsonp，前缀为：$func
		// 例如：format=jsonp_sample_pay，返回格式前缀为：sample_pay()

		JSONObject js = JSONObject.parseObject(parms);
		String payToken = js.getString("payToken");
		String openid = js.getString("openid");
		String pf = js.getString("pf");
		String pfkey = js.getString("pfkey");
		String openkey = js.getString("openkey");
		String cookType = js.getString("type");

		String zoneid = ServerConfig.getInstance().getServerId() + "";
		String payUrl = "https://ysdk.qq.com/mpay/buy_goods_m";
		if (ServerConfig.getInstance().isTest()) {
			payUrl = "https://ysdktest.qq.com/mpay/buy_goods_m";
			zoneid = "1";
		}

		String appid;
		String appkey;
		boolean isDebug = ServerConfig.getInstance().isTest();
		String remarkKey = ChannelInfoProvider.getDefault().getChannelRemarkInfo(1000 + "");// 米大师参数为1000
		if (null != remarkKey && !remarkKey.trim().equals("")) {
			JSONObject remark = JSONObject.parseObject(remarkKey);
			appid = remark.getString("appid");
			appkey = remark.getString(isDebug ? "debug" : "appkey");
		} else {
			return null;
		}

		/************* 参 * 数 * 封 * 装 *************/
		StringBuffer param = new StringBuffer();
		param.append("appid=" + appid);
		param.append("&format=json");
		param.append("&openid=" + openid);
		param.append("&openkey=" + openkey);
		param.append("&pf=" + pf);
		param.append("&pfkey=" + pfkey);
		param.append("&payToken=" + payToken);
		param.append("&ts=" + (System.currentTimeMillis() / 1000));
		param.append("&zoneid=" + zoneid);
		String payParam = param.toString();

		String result = null;
		try {
			String t1 = URLEncoder.encode("/v3/r/mpay/buy_goods_m", "UTF-8");
			String t2 = URLEncoder.encode(payParam, "UTF-8");
			String t3 = "GET&" + t1 + "&" + t2;
			String t5 = encode(HmacSHA1Encrypt(t3, appkey + "&"));
			String t6 = URLEncoder.encode(t5, "UTF-8");
			payParam += "&sig=" + t6;
			String cookie = "";
			if (cookType.equals("qq")) {
				cookie = "session_id=openid;" + "session_type=kp_actoken;" + "org_loc=/v3/r/mpay/buy_goods_m";
			} else if (cookType.equals("wx")) {
				cookie = "session_id=hy_gameid;" + "session_type=wc_actoken;" + "org_loc=/v3/r/mpay/buy_goods_m";
			}

			/************* 请 * 求 * 方 * 法 *************/
			BufferedReader in = null;
			// param = URLEncoder.encode(param,"UTF-8");
			String urlNameString = payUrl + "?" + payParam;
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			if (cookie != null)
				connection.setRequestProperty("Cookie", cookie);
			connection.connect();
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}

			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			return result;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/*** 应用宝 yyb SHA1加密方式 */
	public static byte[] HmacSHA1Encrypt(String encryptText, String encryptKey) throws Exception {
		final String MAC_NAME = "HmacSHA1";
		final String ENCODING = "UTF-8";
		byte[] data = encryptKey.getBytes(ENCODING);
		// 根据给定的字节数组构造一个密钥,第二参数指定一个密钥算法的名称
		SecretKey secretKey = new SecretKeySpec(data, MAC_NAME);
		// 生成一个指定 Mac 算法 的 Mac 对象
		Mac mac = Mac.getInstance(MAC_NAME);
		// 用给定密钥初始化 Mac 对象
		mac.init(secretKey);

		byte[] text = encryptText.getBytes(ENCODING);
		// 完成 Mac 操作
		return mac.doFinal(text);
	}

	public static String signUC(Map<String, String> reqMap, String signKey) {
		// 将所有key按照字典顺序排序
		TreeMap<String, String> signMap = new TreeMap<String, String>(reqMap);
		StringBuilder stringBuilder = new StringBuilder(1024);
		for (Map.Entry<String, String> entry : signMap.entrySet()) {
			// sgin和signType不参与签名
			if ("sign".equals(entry.getKey()) || "signType".equals(entry.getKey())) {
				continue;
			}
			// 值为null的参数不参与签名
			if (entry.getValue() != null) {
				stringBuilder.append(entry.getKey()).append("=").append(entry.getValue());
			}
		}
		// 拼接签名秘钥
		stringBuilder.append(signKey);
		// 剔除参数中含有的'&'符号
		String signSrc = stringBuilder.toString().replaceAll("&", "");
		return md5(signSrc).toLowerCase();
	}

	/*** 华为参数排序拼接 */
	private String sortHuaweiSignParms(Map<String, String> parms) {
		StringBuffer content = new StringBuffer();

		// 按照key做排序
		List<String> keys = new ArrayList<String>(parms.keySet());
		Collections.sort(keys);

		for (int i = 0; i < keys.size(); i++) {
			String key = keys.get(i);
			if ("sign".equals(key) || "signType".equals(key) || "cpSign".equals(key)) {
				continue;
			}
			String value = (String) parms.get(key);
			if (value != null && !value.trim().equals("")) {
				content.append((i == 0 ? "" : "&") + key + "=" + value);
			} else {
				continue;
			}

		}

		return content.toString();
	}

	/** 华为签名 */
	private String signHuawei(String content, String privateKey, boolean checkSignType, String signType) {
		String type = "SHA1WithRSA";
		if (signType.equals("RAS256")) {
			type = "SHA256WithRSA";
		} else {
			type = "SHA1WithRSA";
		}
		try {
			byte[] e = org.apache.commons.codec.binary.Base64.decodeBase64(privateKey);
			PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(e);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
			Signature signature = Signature.getInstance(type);
			signature.initSign(privateK);
			signature.update(content.getBytes(Charset.forName("UTF-8")));
			return org.apache.commons.codec.binary.Base64.encodeBase64String(signature.sign());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private boolean doCheckHuawei(String content, String sign, String publicKey, String signType) {
		try {
			String type = "";

			if (signType.equals("RAS256")) {
				type = "SHA256WithRSA";
			} else {
				type = "SHA1WithRSA";
			}
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			// byte[] encodedKey = decode(publicKey.toCharArray());
			byte[] encodedKey = org.apache.commons.codec.binary.Base64.decodeBase64(publicKey);
			PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

			java.security.Signature signature = java.security.Signature.getInstance(type);

			signature.initVerify(pubKey);
			signature.update(content.getBytes("utf-8"));

			boolean bverify = signature.verify(org.apache.commons.codec.binary.Base64.decodeBase64(sign));
			return bverify;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * MD5 加密
	 */
	public static String md5(String sourceString) {
		if ("".equals(sourceString) || null == sourceString || "".equals(sourceString.trim())) {
			return null;
		}
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			byte[] btInput = sourceString.getBytes();
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			mdInst.update(btInput);
			byte[] md = mdInst.digest();
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}

	/************** base64 ****************/
	private static char[] base64EncodeChars = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
			'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
			'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1',
			'2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };

	public static String encode(byte[] data) {
		StringBuffer sb = new StringBuffer();
		int len = data.length;
		int i = 0;
		int b1, b2, b3;

		while (i < len) {
			b1 = data[i++] & 0xff;
			if (i == len) {
				sb.append(base64EncodeChars[b1 >>> 2]);
				sb.append(base64EncodeChars[(b1 & 0x3) << 4]);
				sb.append("==");
				break;
			}
			b2 = data[i++] & 0xff;
			if (i == len) {
				sb.append(base64EncodeChars[b1 >>> 2]);
				sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
				sb.append(base64EncodeChars[(b2 & 0x0f) << 2]);
				sb.append("=");
				break;
			}
			b3 = data[i++] & 0xff;
			sb.append(base64EncodeChars[b1 >>> 2]);
			sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
			sb.append(base64EncodeChars[((b2 & 0x0f) << 2) | ((b3 & 0xc0) >>> 6)]);
			sb.append(base64EncodeChars[b3 & 0x3f]);
		}
		return sb.toString();
	}

	public byte[] decode(char[] data) {
		byte[] codes = new byte[256];
		for (int i = 0; i < 256; i++)
			codes[i] = -1;
		for (int i = 'A'; i <= 'Z'; i++)
			codes[i] = (byte) (i - 'A');
		for (int i = 'a'; i <= 'z'; i++)
			codes[i] = (byte) (26 + i - 'a');
		for (int i = '0'; i <= '9'; i++)
			codes[i] = (byte) (52 + i - '0');
		codes['-'] = 62;
		codes['_'] = 63;
		int len = ((data.length + 3) / 4) * 3;
		if (data.length > 0 && data[data.length - 1] == '=')
			--len;
		if (data.length > 1 && data[data.length - 2] == '=')
			--len;
		byte[] out = new byte[len];
		int shift = 0;
		int accum = 0;
		int index = 0;
		for (int ix = 0; ix < data.length; ix++) {
			int value = codes[data[ix] & 0xFF];
			if (value >= 0) {
				accum <<= 6;
				shift += 6;
				accum |= value;
				if (shift >= 8) {
					shift -= 8;
					out[index++] = (byte) ((accum >> shift) & 0xff);
				}
			}
		}
		if (index != out.length) {
			logger.debug("decode data length is error ! ");
			return null;
		}
		return out;
	}

}
