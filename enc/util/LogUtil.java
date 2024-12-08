package util;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentLinkedQueue;
import uf.UF;

public class LogUtil {

	/** 换行符 */
	public static final String SEPARATOR = System.getProperty("line.separator");
	/** info写手 */
	public static BufferedWriter writer;

	//private static List<Object[]> orderList = Collections.synchronizedList(new LinkedList<>());
	private static ConcurrentLinkedQueue<Object[]> queue = new ConcurrentLinkedQueue<>();

	private static String today;

	/** 异步打印日志任务 */
	private static Runnable logRunnable = new Runnable() {
		@Override
		public synchronized void run() {
			try {
				Object[] log = queue.poll();
				Object now8 = log[0];
				String info = log[1].toString();
				Object obj = log[2];
				if (!now8.equals(today)) createWriter();
				System.out.print(info);
				writer.write(info);
				if (obj instanceof Throwable) {
					Throwable e = (Throwable) obj;
					StackTraceElement[] stes = e.getStackTrace();
					for (StackTraceElement ste : stes) {
						String error = "	at " + ste + SEPARATOR;
						System.err.print(error);
						writer.write(error);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (writer != null) writer.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	/**
	 * 打印日志
	 * @param obj : 要打印的主体
	 */
	public static void print(Object obj) {
		print(obj, 2);
	}

	/**
	 * 打印日志
	 * @param obj : 要打印的主体
	 * @param index : 日志前缀地址往前退几步
	 */
	public static void print(Object obj, int index) {
		Throwable t = new Throwable();
		String prefix = ThreadUtil.getLogId() + " " + t.getStackTrace()[index] + ":";
		print(obj, prefix);
	}

	/**
	 * 打印日志
	 * @param obj : 要打印的主体
	 * @param c : 日志前缀地址在哪个类之前
	 */
	public static void print(Object obj, Class<?> c) {
		String str = c.getName() + ".";
		Throwable t = new Throwable();
		StackTraceElement[] ste = t.getStackTrace();
		int i = 2;
		for (; i < ste.length - 1; i++) {
			if (!ste[i].toString().startsWith(str)) break;
		}
		String prefix = ThreadUtil.getLogId() + " " + ste[i] + ":";
		print(obj, prefix);
	}

	/**
	 * 打印日志
	 * @param obj : 要打印的主体
	 * @param suffix : 自定义的前缀
	 */
	public static void print(Object obj, String prefix) {
		LocalDateTime now = LocalDateTime.now();
		String now23 = DateUtil.DTF23.format(now);
		String now8 = DateUtil.DTF8.format(now);
		queue.add(new Object[] { now8, now23 + " " + prefix + obj + SEPARATOR, obj });
		UF.THREADS.execute(logRunnable);
	}

	/** 只打印obj,不带任何前缀 */
	public static void out(Object obj) {
		queue.add(new Object[] { DateUtil.now8(), obj + SEPARATOR, obj });
		UF.THREADS.execute(logRunnable);
	}

	/** 为日志写手赋值 */
	public static void createWriter() {
		try {
			String logPre = PropUtil.get("sys.LOG_PRE");
			String logPath;
			if(UF.IS_WINDOWS) {
				logPath="D:/Ent/logs/" + logPre + "logs";
			}else {
				logPath = System.getProperty("catalina.base") + File.separator + "logs" + File.separator + logPre + "logs";
			}
			Path folder = Paths.get(logPath);
			if (Files.notExists(folder)) {
				Files.createDirectories(folder);
			}
			if (writer != null) writer.close();
			today = DateUtil.now8();
			Path todayPath = Paths.get(logPath, logPre + "-" + today + ".log");
			writer = Files.newBufferedWriter(todayPath, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
			//异步整理日志目录
			UF.THREADS.execute(() -> {				
				try {
					boolean isRun = false;
					for(Path path : Files.newDirectoryStream(folder)) {
						String fileName = path.getFileName().toString();
						if (fileName.endsWith(".log") && !fileName.contains(today)) {
							isRun = true;
							String bakFolderName = fileName.substring(fileName.length() - 12, fileName.length() - 6);
							Path bakFolder = Paths.get(logPath, bakFolderName);
							if (Files.notExists(bakFolder)) {
								Files.createDirectories(bakFolder);
							}
							Path newPath = Paths.get(logPath, bakFolderName, fileName);
							Files.move(path, newPath);
						}
					}
					for(Path bakFolder : Files.newDirectoryStream(folder)) {
						String bakFolderName = bakFolder.getFileName().toString();
						if (bakFolderName.length() == 6 && !today.contains(bakFolderName)) {
							isRun = true;
							IOUtil.toZip(bakFolder, Paths.get(logPath, bakFolderName + ".zip"));
							IOUtil.deleteDir(bakFolder);
						}				
					}
					if (isRun) print("整理日志目录完成！", "");
				}catch(Exception e) {
					LogUtil.print(e);
				}				
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
