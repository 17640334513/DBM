package uf;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import util.LogUtil;
import util.PropUtil;

public class UF {

	public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
	
	public static final String UTF8 = "UTF-8";
	
	public static final String CONTENT_TYPE = "text/html; charset=utf-8";
	
	/** 线程池 */
	public static final ThreadPoolExecutor THREADS = new ThreadPoolExecutor(PropUtil.getInt("sys.MIN_THREAD"), PropUtil.getInt("sys.MAX_THREAD"), PropUtil.getInt("sys.THREAD_TIMEOUT"), TimeUnit.MINUTES, new LinkedBlockingDeque<Runnable>());
	static {
		LogUtil.print("资源配置文件路径:" + PropUtil.FILE_DIR, "");
		LogUtil.print("加载properties文件成功,共" + PropUtil.propMap.size() + "个参数", "");
		LogUtil.print("线程池创建成功，当前空闲线程数：" + THREADS.getCorePoolSize(), "");
	}
	
}
