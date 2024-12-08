package util;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import uf.BaseBean;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class JsonUtil {

	private static final String encStr = "'";

	/** Object转json */
	public static String toJson(Object obj) {
		String json = "";
		if (obj == null) {
			return "\"\"";
		} else if (obj instanceof Collection) {
			Collection list = (Collection) obj;
			for (Object o : list) {
				json += "," + toJson(o);
			}
			return "[" + (json.length() == 0 ? json : json.substring(1)) + "]";
		} else if (obj.getClass().isArray()) {
			Object[] objs = (Object[]) obj;
			for (Object o : objs) {
				json += "," + toJson(o);
			}
			return "[" + (json.length() == 0 ? json : json.substring(1)) + "]";
		} else if (obj instanceof Map) {
			Map map = (Map) obj;
			for (Object o : map.entrySet()) {
				Map.Entry entry = (Map.Entry) o;
				json += "," + toJson(entry.getKey()) + ":" + toJson(entry.getValue());
			}
			return "{" + (json.length() == 0 ? json : json.substring(1)) + "}";
		} else if (obj instanceof String) {// 把字符串中的"改成'，否则js读不了，parse()也读不了
			return "\"" + encode((String) obj) + "\"";
		} else if (obj instanceof LocalDateTime) {
			LocalDateTime date = (LocalDateTime) obj;
			return "\"" + DateUtil.DTF19.format(date) + "\"";
		} else {
			return "\"" + obj + "\"";
		}
	}
	
	/** 
	 * json转Object
	 * 只支持Map、List和String
	 *  */
	public static <T> T parse(String json) {
		if (json == null) return null;
		List<String> encList = new LinkedList<String>();// 用来存储被换掉的字符串，以便最后再还原
		json = encJson(json, encList);
		return (T) jsonParse(json, encList);
	}
	private static Object jsonParse(String json, List<String> encList) {
		json = json.trim();
		if (json.startsWith("[") && json.endsWith("]")) {
			json = json.substring(1, json.length() - 1);
			List list = new ArrayList();			
			int tier = 0, lastI = 0, len = json.length();
			for(int i = 0; i < len; i ++) {
				char c = json.charAt(i);
				if(c == ',' && tier == 0) {					
					list.add(jsonParse(json.substring(lastI, i), encList));
					lastI = i + 1;
				}else if(c == '{' || c == '[') tier ++;
				else if(c == '}' || c == ']') tier --;						
			}
			list.add(jsonParse(json.substring(lastI, len), encList));
			return list;
		} else if(json.startsWith("{") && json.endsWith("}")) {
			json = json.substring(1, json.length() - 1);
			Map map = new HashMap();
			int tier = 0, lastI = 0, len = json.length();
			Object key = null;
			for(int i = 0; i < len; i ++) {
				char c = json.charAt(i);
				if(c == ':' && tier == 0) {
					key = jsonParse(json.substring(lastI, i), encList);
					lastI = i + 1;
				}else if(c == ',' && tier == 0) {
					map.put(key, jsonParse(json.substring(lastI, i), encList));
					lastI = i + 1;
				}else if(c == '{' || c == '[') tier ++;
				else if(c == '}' || c == ']') tier --;								
			}
			map.put(key, jsonParse(json.substring(lastI, len), encList));
			return map;
		} else if (json.equals(encStr)) {// 把被换成'的字符串还原
			return decode(encList.remove(0));
		}
		return json;
	}
	
	/** 
	 * json转Object
	 * 支持bean以及所有常见的类型
	 * @param t : 要求的格式，若t中任意位置有集合，必须在集合中插入一个示例值
	 *  */
	public static <T> T parse(String json, T t) throws Exception {
		if (json == null || t == null) return null;
		List<String> encList = new LinkedList<String>();// 用来存储被换掉的字符串，以便最后再还原
		json = encJson(json, encList);
		return (T) jsonParse(json, t, encList);
	}
	private static Object jsonParse(String json, Object objT, List<String> encList) throws Exception {
		Class clazz = objT.getClass();
		json = json.trim();
		if (json.startsWith("[") && json.endsWith("]")) {
			json = json.substring(1, json.length() - 1);	
			if(clazz.isArray()) {
				Object[] obj = (Object[]) objT;
				Object subT = obj[0];								
				int tier = 0, lastI = 0, jsonLen = json.length(), objIndex = 0;
				for(int i = 0; i < jsonLen; i ++) {
					char c = json.charAt(i);
					if(c == ',' && tier == 0) {		
						obj[objIndex] = jsonParse(json.substring(lastI, i), subT, encList);
						lastI = i + 1;
					}else if(c == '{' || c == '[') tier ++;
					else if(c == '}' || c == ']') tier --;								
				}
				obj[objIndex] = jsonParse(json.substring(lastI, jsonLen), subT, encList);
				return obj;
			} else {
				List list = new ArrayList();
				Object subT = null;
				try {
					subT = ((List) objT).get(0);
				}catch(Exception e) {
					throw new Exception("List中必须有至少一个示例值");
				}
				int tier = 0, lastI = 0, jsonLen = json.length();
				for(int i = 0; i < jsonLen; i ++) {
					char c = json.charAt(i);
					if(c == ',' && tier == 0) {					
						list.add(jsonParse(json.substring(lastI, i), subT, encList));
						lastI = i + 1;
					}else if(c == '{' || c == '[') tier ++;
					else if(c == '}' || c == ']') tier --;							
				}
				list.add(jsonParse(json.substring(lastI, jsonLen), subT, encList));
				return list;
			}			
		} else if(json.startsWith("{") && json.endsWith("}")) {
			json = json.substring(1, json.length() - 1);
			if(objT instanceof BaseBean) {
				Object bean = clazz.newInstance();
				int tier = 0, lastI = 0, len = json.length();
				String name = null;
				for(int i = 0; i < len; i ++) {
					char c = json.charAt(i);
					if(c == ':' && tier == 0) {
						name = (String) jsonParse(json.substring(lastI, i), encList);
						lastI = i + 1;
					}else if(c == ',' && tier == 0) {
						Field field = clazz.getDeclaredField(name);
						field.setAccessible(true);
						Object subT = field.get(objT);
						field.set(bean, jsonParse(json.substring(lastI, i), subT, encList));
						lastI = i + 1;
					}else if(c == '{' || c == '[') tier ++;
					else if(c == '}' || c == ']') tier --;								
				}
				Field field = clazz.getDeclaredField(name);
				field.setAccessible(true);
				Object subT = field.get(objT);
				field.set(bean, jsonParse(json.substring(lastI, len), subT, encList));
				return bean;
			} else {
				Map map = new HashMap();
				Map formatMap = (Map) objT;
				Object keyT = null;
				Object valueT = null;
				try {
					keyT = formatMap.keySet().iterator().next();
					valueT = formatMap.values().iterator().next();
				}catch(Exception e) {
					throw new Exception("Map中必须有至少一个示例值");
				}
				int tier = 0, lastI = 0, len = json.length();
				Object key = null;
				for(int i = 0; i < len; i ++) {
					char c = json.charAt(i);
					if(c == ':' && tier == 0) {
						key = jsonParse(json.substring(lastI, i), keyT, encList);
						lastI = i + 1;
					}else if(c == ',' && tier == 0) {
						map.put(key, jsonParse(json.substring(lastI, i), valueT, encList));
						lastI = i + 1;
					}else if(c == '{' || c == '[') tier ++;
					else if(c == '}' || c == ']') tier --;								
				}
				map.put(key, jsonParse(json.substring(lastI, len), valueT, encList));
				return map;
			}
		} else if (json.equals(encStr)) {// 把被换成'的字符串还原
			if(clazz == String.class) {
				return decode(encList.remove(0));
			}
			return StringUtil.stringToT(encList.remove(0), clazz);
		}
		return json;
	}
	
	/** 把json中"包起来的字符串换成' */
	private static String encJson(String json, List<String> encList) {
		String[] splitjsons = json.split("\"");
		StringBuilder tmpJson = new StringBuilder(splitjsons[0]);
		for (int i = 1; i < splitjsons.length; i++) {
			if ((i + 1) % 2 == 0) {
				tmpJson.append(encStr);
				encList.add(splitjsons[i]);
			} else {
				tmpJson.append(splitjsons[i]);
			}
		}
		return tmpJson.toString();
	}
	
	private static String encode(String str) {
		return str.replace("\"", "&quot;");
	}
	private static String decode(String str) {
		return str.replace("&quot;", "\"");
	}
	
	/**获取map类型json中key的值（值必须是字符串）*/
	public static String getString(String json, String key) {
		int keyIndex = json.indexOf("\"" + key + "\"") + key.length() + 2;
		int leftIndex = json.indexOf("\"", keyIndex) + 1;
		int rightIndex = json.indexOf("\"", leftIndex);
		return json.substring(leftIndex, rightIndex);
	}
	
	/**获取map类型json中key的值（值必须是int）*/
	public static int getInt(String json, String key) {
		int keyIndex = json.indexOf("\"" + key + "\"") + key.length() + 2;
		int leftIndex = json.indexOf(":", keyIndex) + 1;
		int rightIndex = json.indexOf(",", leftIndex);
		int rightIndex0 = json.indexOf("}", leftIndex);
		if(rightIndex == -1 || rightIndex > rightIndex0) {
			rightIndex = rightIndex0;
		}
		return Integer.parseInt(json.substring(leftIndex, rightIndex).trim());
	}
	
	public static void main(String[] args) {
		String json = "{\"rows\":[{\"id\":\"113571\",\"cell\":{\"bond_id\":\"113571\",\"bond_nm\":\"博特转债\",\"price\":\"145.010\",\"stock_id\":\"603916\",\"stock_nm\":\"苏博特\",\"margin_flg\":\"\",\"btype\":\"C\",\"orig_iss_amt\":\"6.970\",\"curr_iss_amt\":\"2.709\",\"convert_dt\":\"2020-09-18\",\"convert_price\":\"18.88\",\"next_put_dt\":\"2024-03-11\",\"redeem_dt\":\"2020-11-17\",\"force_redeem\":\"最后交易日：2020年11月17日最后转股日：2020年11月17日赎回价：100.34元张\",\"redeem_flag\":\"Y\",\"redeem_price\":\"115.00\",\"redeem_price_ratio\":\"130%\",\"real_force_redeem_price\":\"100.340\",\"redeem_real_days\":29,\"redeem_total_days\":30,\"redeem_count_days\":15,\"redeem_tc\":\"如果公司A股股票连续三十个交易日中至少有十五个交易日的收盘价格不低于当期转股价格的130%(含 130%)\",\"sprice\":\"27.16\",\"redeem_icon\":\"R\",\"redeem_orders\":600.96666666667,\"redeem_count\":\"<span style=\"color:red;\">2930<span>\",\"after_next_put_dt\":0,\"force_redeem_price\":\"24.54\"}},{\"id\":\"123036\",\"cell\":{\"bond_id\":\"123036\",\"bond_nm\":\"先导转债\",\"price\":\"170.310\",\"stock_id\":\"300450\",\"stock_nm\":\"先导智能\",\"margin_flg\":\"R\",\"btype\":\"C\",\"orig_iss_amt\":\"10.000\",\"curr_iss_amt\":\"5.813\",\"convert_dt\":\"2020-06-17\",\"convert_price\":\"38.99\",\"next_put_dt\":\"2023-12-11\",\"redeem_dt\":\"2020-12-09\",\"force_redeem\":\"最后交易日：2020年12月9日最后转股日：2020年12月9日赎回价：100.30元张\",\"redeem_flag\":\"Y\",\"redeem_price\":\"110.00\",\"redeem_price_ratio\":\"130%\",\"real_force_redeem_price\":\"100.300\",\"redeem_real_days\":21,\"redeem_total_days\":30,\"redeem_count_days\":15,\"redeem_tc\":\"如果公司股票在任何连续30个交易日中至少15个交易日的收盘价格不低于当期转股价格的130%(含 130%)\",\"sprice\":\"66.80\",\"redeem_icon\":\"R\",\"redeem_orders\":600.7,\"redeem_count\":\"<span style=\"color:red;\">2130<span>\",\"after_next_put_dt\":0,\"force_redeem_price\":\"50.69\"}},{\"id\":\"123026\",\"cell\":{\"bond_id\":\"123026\",\"bond_nm\":\"中环转债\",\"price\":\"126.000\",\"stock_id\":\"300692\",\"stock_nm\":\"中环环保\",\"margin_flg\":\"\",\"btype\":\"C\",\"orig_iss_amt\":\"2.900\",\"curr_iss_amt\":\"0.807\",\"convert_dt\":\"2019-12-16\",\"convert_price\":\"12.25\",\"next_put_dt\":\"2022-06-10\",\"redeem_dt\":\"2020-12-14\",\"force_redeem\":\"最后交易日：2020年12月14日最后转股日：2020年12月14日赎回价：100.41元张\",\"redeem_flag\":\"Y\",\"redeem_price\":\"106.00\",\"redeem_price_ratio\":\"130%\",\"real_force_redeem_price\":\"100.410\",\"redeem_real_days\":20,\"redeem_total_days\":30,\"redeem_count_days\":15,\"redeem_tc\":\"在可转换公司债券转股期内，如果公司A股股票连续三十个交易日中至少有十五个交易日的收盘价格不低于当期转股价格的130%(含)\",\"sprice\":\"15.46\",\"redeem_icon\":\"R\",\"redeem_orders\":600.66666666667,\"redeem_count\":\"<span style=\"color:red;\">2030<span>\",\"after_next_put_dt\":0,\"force_redeem_price\":\"15.93\"}}]}";
		
		Map map = parse(json.replaceAll("\"color:red;\"", ""));
		List<Map> list = (List) map.get("rows");
		for(Map dataMap : list){
			System.out.println(dataMap);
		}
	}
	
}
