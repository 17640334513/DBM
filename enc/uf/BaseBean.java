package uf;

import java.lang.reflect.Field;
import util.JsonUtil;
import util.LogUtil;

public class BaseBean {

	@Override
	public String toString() {
		StringBuilder json = new StringBuilder();
		try {
			Field[] fields = this.getClass().getDeclaredFields();
			Field.setAccessible(fields, true);
			for (Field field : fields) {
				json.append(",\"" + field.getName() + "\":" + JsonUtil.toJson(field.get(this)));
			}
		} catch (Exception e) {
			LogUtil.print(e, 2);
		}
		return "{" + (json.length() == 0 ? json : json.substring(1)) + "}";
	}
	
}
