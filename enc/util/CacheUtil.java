package util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import uf.UFConcurrentMap;

public class CacheUtil {

	/**缓存本体*/
	private static UFConcurrentMap map = new UFConcurrentMap();
	/**缓存过期时间*/
	public static ConcurrentMap<String, Long> lifeMap = new ConcurrentHashMap<>();
	
	/**缓存键值对，永久有效*/
	public static Object set(String key, Object value) {
		LogUtil.print("存入缓存：" + key + ":" + value, 2);
		return map.put(key, value);
	}
	/**
	 * 缓存键值对
	 * @param minute: 多少分钟后该键值对失效(若为0，则表示下一个整分失效)
	 * */
	public static Object set(String key, Object value, int minute) {
		LogUtil.print("存入缓存：" + key + ":" + value + ",有效期：" + minute + "分钟", 2);
		lifeMap.put(key, System.currentTimeMillis() + minute * 60 * 1000);
		return map.put(key, value);
	}

	public static Object get(String key) {
		return map.get(key);
	}
	
	public static Object remove(String key) {
		LogUtil.print("清除缓存：" + key, 2);
		lifeMap.remove(key);//ConcurrentMap是可以在forEach里删除元素的，但是Map不行
		return map.remove(key);
	}
	
}
