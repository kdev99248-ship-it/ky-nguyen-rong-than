package logic.bug;

import java.lang.reflect.Field;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.core.pub.script.IScript;
import game.server.logic.crossIndigo.CrossIndigoService;

public class AddCrossIndigoSupply implements IScript {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init() {

	}

	@Override
	public void destroy() {

	}

	@Override
	public Object call(String scriptName, Object arg) {
		long[] playerIds = new long[] {667330553886L, 661961844697L, 654445652030L};
		CrossIndigoService service = CrossIndigoService.getInstance();
		
		//反射获取object的data属性值(subEvent中的eventId)
		String eventId = null;
		Class clazz = service.getClass();//通过entitySave的Object对象，获取运行时类的对象
		Field field = null;
		try {
		    //获取object中的data属性
		    field = clazz.getDeclaredField("finalApply");
		    field.setAccessible(true);//设置data属性为可访问的
		    List<Long> finalApply;
		    try {
		        //通过Field.get(Object)获取object的data(SubEvent)中的eventId属性
		    	finalApply = (List<Long>) field.get(service);
		    	for (int i = 0; i < playerIds.length; i++) {
		    		if (!finalApply.contains(playerIds[i])) {
		    			finalApply.add(playerIds[i]);
		    			logger.error("添加" + playerIds[i] + "到决赛报名列表成功!");
		    		}
				}
		    	
		    } catch (IllegalAccessException e) {
		        e.printStackTrace();
		    }
		} catch (NoSuchFieldException e) {
		    e.printStackTrace();
		}
		
		
		return null;
	}

}
