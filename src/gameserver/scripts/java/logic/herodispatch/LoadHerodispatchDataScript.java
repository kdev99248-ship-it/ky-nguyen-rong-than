package logic.herodispatch;

import java.util.ArrayList;
import java.util.List;
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
public class LoadHerodispatchDataScript implements IScript{

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public void init() {
		
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public Object call(String scriptName, Object arg) {
		loadHerodispatchData();
		return null;
	}

	public void loadHerodispatchData() {

		ActivityDataBean data = ActivityDataDao2.selectById(String.valueOf(ActivityType2.HERODISPATCH.value()));
		if (null == data) {
			return;
		}
		HerodispatchService service = HerodispatchService.getInstance();

		byte[] d = CommonUtils.decodeBase64(data.getDataString().getBytes());
		JSONObject json = JSONObject.parseObject(new String(ZipUtils.decompress(d)));
		
		service.getScoreMap().clear();
		JSONObject scoreJson = (JSONObject) json.get("scoreJson");
		for (Entry<String, Object> en : scoreJson.entrySet()) {
			service.getScoreMap().put(Long.valueOf(en.getKey()), (Integer) en.getValue());
		}
		
		service.getBoxMap().clear();
		JSONObject boxJson = (JSONObject) json.get("boxJson");
		for (Entry<String, Object> en : boxJson.entrySet()) {
			JSONArray arr = (JSONArray) en.getValue();
			List<HerodispatchBox> l = new ArrayList<>();
			for (int i = 0; i < arr.size(); i++) {
				HerodispatchBox box = new HerodispatchBox();
				JSONObject j = (JSONObject) arr.get(i);
				box.fromJson(j);
				l.add(box);
			}
			service.getBoxMap().put(Long.valueOf(en.getKey()), l);
		}
	}
}
