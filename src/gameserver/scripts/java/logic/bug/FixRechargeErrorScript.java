package logic.bug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;

import game.core.pub.script.IScript;
import game.core.pub.util.ExceptionEx;
import game.server.db.game.bean.RechargeBean;
import game.server.logic.recharge.handler.InnerInsertOneRechargeBeanHandler;
import game.server.logic.util.ScriptArgs;
import game.server.thread.RechargeProcessor;

/**
 * 修复充值异常脚本（主要是处理插入订单数据失败的情况）
 * 
 * @author liujiang
 * @date 2016-10-19
 */
public class FixRechargeErrorScript implements IScript {
	private static final Logger LOGGER = Logger.getLogger(FixRechargeErrorScript.class);

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		try {
			ScriptArgs args = (ScriptArgs) arg;
			String func = (String) args.get(ScriptArgs.Key.ARG1);
			if ("saveLogFile".equals(func)) {
				RechargeBean bean = (RechargeBean) args.get(ScriptArgs.Key.ARG2);
				saveLogFile(bean);
			} else if ("tick".equals(func)) {
				checkFix();
			} else if ("fixSuccess".equals(func)) {
				RechargeBean bean = (RechargeBean) args.get(ScriptArgs.Key.ARG2);
				fixSuccess(bean);
			}
			return null;
		} catch (Exception e) {
			LOGGER.error(ExceptionEx.e2s(e));
			return null;
		}
	}

	/** 保存充值记录未正常插入的订单文件 */
	public static void saveLogFile(RechargeBean bean) {
		try {
			String line = File.separator;
			String outputPath = System.getProperty("user.dir") + line + "logs" + line + "rechargeError";
			File outFolder = new File(outputPath);
			if (!outFolder.exists()) {
				outFolder.mkdirs();
			}
			File outFile = new File(outputPath + line + bean.getOrderId());
			System.out.println("outputPath=" + outFile.getPath());
			if (!outFile.exists()) {
				outFile.createNewFile();
			}
			FileWriter fileWritter = new FileWriter(outFile);
			JSONObject json = (JSONObject) bean.toJson();
			json.put("isFix", bean.isFix());
			json.put("fixNum", bean.getFixNum());
			fileWritter.write(json.toString());
			fileWritter.flush();
			fileWritter.close();
			LOGGER.info("---保存充值记录未正常插入的订单文件=" + json.toString());
		} catch (Exception e) {
			LOGGER.error(ExceptionEx.e2s(e));
		}
	}

	/** 检查并修复未正常插入的充值订单 */
	public static void checkFix() {
		try {
			String line = File.separator;
			String inputPath = System.getProperty("user.dir") + line + "logs" + line + "rechargeError";
			List<File> list = getFileList(inputPath);
			if (list == null || list.size() == 0) {
				return;
			}
			String temp = null;
			for (File file : list) {
				InputStreamReader read = new InputStreamReader(new FileInputStream(file), "UTF-8");
				BufferedReader reader = new BufferedReader(read);
				if ((temp = reader.readLine()) != null && !"".equals(temp)) {
					RechargeBean bean = new RechargeBean();
					JSONObject json = JSONObject.parseObject(temp);
					if (json == null) {
						continue;
					}
					bean.fromJson(json);
					bean.setFix(json.getBooleanValue("isFix"));
					bean.setFixNum(json.getIntValue("fixNum"));
					if (bean.getFixNum() >= 5) {
						read.close();
						// 5次还没修复好就放弃修复,后面直接查文件
						fixFail(bean);
						continue;
					}
					// 重新进行数据库回存
					RechargeProcessor.getInstance().addCommand(new InnerInsertOneRechargeBeanHandler(bean));
				}
				read.close();
			}
		} catch (Exception e) {
			LOGGER.error(ExceptionEx.e2s(e));
		}
	}

	/** 修复订单成功，记入另外的文件夹 */
	public static void fixSuccess(RechargeBean bean) {
		moveFile(bean, true);
	}

	/** 修复订单失败，记入另外的文件夹 */
	public static void fixFail(RechargeBean bean) {
		moveFile(bean, false);
	}

	/** 移动文件 */
	public static void moveFile(RechargeBean bean, boolean success) {
		try {
			String line = File.separator;
			String inputPath = System.getProperty("user.dir") + line + "logs" + line + "rechargeError";
			String outputPath = System.getProperty("user.dir") + line + "logs" + line + "rechargeFixFail";
			if (success) {
				outputPath = System.getProperty("user.dir") + line + "logs" + line + "rechargeFixSucc";
			}
			File outFile = new File(outputPath);
			if (!outFile.exists()) {
				outFile.mkdirs();
			}
			FileWriter fileWritter = new FileWriter(outFile + line + bean.getOrderId());
			JSONObject json = (JSONObject) bean.toJson();
			json.put("isFix", bean.isFix());
			json.put("fixNum", bean.getFixNum());
			fileWritter.write(json.toString());
			fileWritter.flush();
			fileWritter.close();

			File errorFile = new File(inputPath + line + bean.getOrderId());
			errorFile.delete();
			LOGGER.info("---修复订单是否成功=" + success + " " + json.toString());
		} catch (Exception e) {
			LOGGER.error(ExceptionEx.e2s(e));
		}
	}

	public static List<File> getFileList(String strPath) {
		File dir = new File(strPath);
		if (!dir.exists()) {
			return null;
		}
		List<File> filelist = new ArrayList<File>();
		File[] files = dir.listFiles(); // 该文件目录下文件全部放入数组
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				// String fileName = files[i].getName();
				// System.out.println("---fileName=" + fileName);
				filelist.add(files[i]);
			}
		}
		return filelist;
	}
}
