package logic.herodispatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import game.core.pub.script.IScript;
import game.core.pub.util.file.ZipUtils;
import game.server.db.game.bean.ActivityDataBean;
import game.server.db.game.dao.ActivityDataDao2;
import game.server.logic.constant.ActivityType2;
import game.server.logic.herodispatch.HerodispatchService;
import game.server.logic.herodispatch.bean.HerodispatchBox;
import game.server.logic.util.CommonUtils;

/**
 * 回存精灵派遣数据
 * @author tzyx
 *
 */
public class SaveHerodispatchDataScript implements IScript{

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void init() {
		
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public Object call(String scriptName, Object arg) {
		saveHerodispatchData();
		return null;
	}

	public void saveHerodispatchData() {
		HerodispatchService service = HerodispatchService.getInstance();
		
		Map<Long, Integer> scoreMap = service.getScoreMap();
		JSONObject json = new JSONObject();
		JSONObject scoreJson = new JSONObject();
		for (Entry<Long, Integer> en : scoreMap.entrySet()) {
			scoreJson.put(en.getKey().toString(), en.getValue());
		}
		json.put("scoreJson", scoreJson);
		
		Map<Long, List<HerodispatchBox>> boxMap = service.getBoxMap();
		JSONObject boxJson = new JSONObject();
		for (Entry<Long, List<HerodispatchBox>> en : boxMap.entrySet()) {
			JSONArray arr = new JSONArray();
			for (HerodispatchBox box : en.getValue()) {
				arr.add(box.toJson());
			}
			boxJson.put(en.getKey().toString(), arr);
		}
		json.put("boxJson", boxJson);
		ActivityDataBean bean = new ActivityDataBean();
		byte[] data = ZipUtils.compress(json.toString().getBytes());
		bean.setDataString(CommonUtils.encodeBase64(data));
		bean.setType(ActivityType2.HERODISPATCH.value());
		bean.setId(String.valueOf(ActivityType2.HERODISPATCH.value()));
		List<ActivityDataBean> list = new ArrayList<>();
		list.add(bean);
		ActivityDataDao2.insertBatch(list);
	}
}
