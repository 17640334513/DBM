package util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/** 线程管理工具 */
public class ThreadUtil {
	
	public static ThreadLocal<Map<String,Object>> threadLocal = new ThreadLocal<Map<String,Object>>();
		
	/** request对象的key */
	public static final String Request = "Request";
	/** response对象的key */
	public static final String Response = "Response";
	/** 是否打印日志的key */
	public static final String Isprint = "Isprint";
	/** 日志ID的key */
	private static final String Logid = "Logid";
	
	private static AtomicLong seq6 = new AtomicLong(100000L);
	/** 获取防止重复的6位序列号 */
	public static long getSeq6() {
		long ret = seq6.getAndIncrement();
		if (ret == 999999L) seq6.set(100000L);
		return ret;
	}
	
	/** 设置面向当前线程的键值对 */
	public static void set(String key, Object value){
		Map<String,Object> threadMap = threadLocal.get();
		if(threadMap==null){
			threadMap = new HashMap<String, Object>();
			threadMap.put(key, value);
			threadLocal.set(threadMap);
		}
		threadMap.put(key, value);
	}
	
	/** 获取当前线程设置过的参数 */
	public static Object get(String key){
		Map<String,Object> threadMap = threadLocal.get();
		if(threadMap==null) return null;
		return threadMap.get(key);
	}
	
	/** 获取当前线程设置过的参数 */
	@SuppressWarnings("unchecked")
	public static <T> T getT(String key){
		Map<String,Object> threadMap = threadLocal.get();
		if(threadMap==null) return null;
		return (T) threadMap.get(key);
	}
	
	/** 获取当前线程的日志ID */
	public static String getLogId(){
		Map<String,Object> threadMap = threadLocal.get();
		if(threadMap==null){
			String logId = "[" + getSeq6() + "]";
			threadMap = new HashMap<String, Object>();
			threadMap.put(Logid, logId);
			threadLocal.set(threadMap);
			return logId;
		}
		String logId = (String) threadMap.get(Logid);
		if(logId==null){
			logId = "["+getSeq6()+"]";
			threadMap.put(Logid, logId);
		}
		return logId;
	}
	
	/** 删除当前线程设置过的参数 */
	public static void remove(String key){
		Map<String,Object> threadMap=threadLocal.get();
		if(threadMap != null) threadMap.remove(key);
	}
	
	/** 清理当前线程设置的所有参数(建议每个线程结束时都调用一下这个方法) */
	public static void clear(){
		threadLocal.remove();
	}

}
