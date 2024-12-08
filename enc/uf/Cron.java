package uf;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import util.CacheUtil;
import util.DateUtil;
import util.LogUtil;
import util.PropUtil;

/**定时任务总部*/
public class Cron {

	public static List<Object[]> jobList = Collections.synchronizedList(new ArrayList<>());
	private static final ScheduledExecutorService SCHEDULE = Executors.newSingleThreadScheduledExecutor();
	
	/**读取cron.properties加载所有定时任务*/
	public static void loadJobs() {
		PropUtil.propMap.forEach((key, value) -> {
			if(key.startsWith("cron.")) {
				key = key.substring(5);
				int lastPointIndex = key.lastIndexOf(".");
				String classPath = key.substring(0, lastPointIndex);
				String methodName = key.substring(lastPointIndex + 1);
				List<String[]> cronsList = new ArrayList<>();
				String[] cronss = value.split("\\|");
				for(String crons : cronss) {
					cronsList.add(crons.split(" "));
				}
				try {
					Method method = Class.forName(classPath).getDeclaredMethod(methodName);
					jobList.add(new Object[] {method, cronsList});
				} catch (Exception e) {
					LogUtil.print(e);
				}
			}
		});
	}
	
	/**增加一个minutes分钟后执行的job,执行一次后删除任务*/
	public void addJobOnce(Method method, int minutes) {
		LocalDateTime ltd = LocalDateTime.now().plusMinutes(minutes);
		List<String[]> cronsList = new ArrayList<>();
		String[] crons = new String[7];
		crons[0] = String.valueOf(ltd.getYear());
		crons[1] = "?";
		crons[2] = String.valueOf(ltd.getMonthValue());
		crons[3] = String.valueOf(ltd.getDayOfMonth());
		crons[4] = String.valueOf(ltd.getHour());
		crons[5] = String.valueOf(ltd.getMinute());
		crons[6] = "once";
		cronsList.add(crons);
		jobList.add(new Object[] {method, cronsList});
	}
	
	/**增加一个job*/
	public void addJob(Method method, String cron) {
		List<String[]> cronsList = new ArrayList<>();
		String[] cronss = cron.split("\\|");
		for(String crons : cronss) {
			cronsList.add(crons.split(" "));
		}
		jobList.add(new Object[] {method, cronsList});
	}
	
	@SuppressWarnings("unchecked")
	public static void init() {
		loadJobs();
		SCHEDULE.scheduleAtFixedRate(() -> {
			LocalDateTime ltd = LocalDateTime.now();
			//过期缓存清除
			UF.THREADS.execute(() -> {
				long now = ltd.toInstant(ZoneOffset.of("+8")).toEpochMilli();
				CacheUtil.lifeMap.forEach((key, expire) -> {
					if(expire <= now) {
						CacheUtil.remove(key);//ConcurrentMap是可以在forEach里删除元素的，但是Map不行
					}
				});
			});
			for(Object[] objs : jobList) {
				Method method = (Method) objs[0];
				for(String[] crons : (List<String[]>) objs[1]) {
					if(checkMinute(crons[0], ltd) && checkHour(crons[1], ltd)
							&& checkDay(crons[2], ltd) && checkMonth(crons[3], ltd)
							&& checkWeek(crons[4], ltd) && checkYear(crons[5], ltd)) {
						UF.THREADS.execute(() -> {
							try {
								String key = method.getDeclaringClass().getName() + "." + method.getName();
								LogUtil.print("定时任务开始", key) ;
								method.invoke(null);
								LogUtil.print("定时任务结束", key);
								if(crons.length == 7) jobList.remove(objs);
							} catch (Exception e) {
								LogUtil.print(e.getCause());
							}
						});
						break;
					}
				}
			}
		}, 60-Integer.parseInt(DateUtil.nowX("ss")), 60, TimeUnit.SECONDS);
		LogUtil.print("总定时任务启动成功", "");
	}
	
	public static void close() {
		SCHEDULE.shutdown();//执行完当前任务即关闭
	}
	
	private static boolean checkMinute(String cron, LocalDateTime ltd) {
		int minute = ltd.getMinute();
		String minuteStr = String.valueOf(minute);
		if("*".equals(cron) || minuteStr.equals(cron)) {
			return true;
		}
		for(String per : cron.split(",")) {
			if(minuteStr.equals(per)) {
				return true;
			}
			int slashIndex = per.indexOf("/");
			if(slashIndex != -1) {
				int start = Integer.parseInt(per.substring(0, slashIndex));
				int period = Integer.parseInt(per.substring(slashIndex + 1));
				while(start < minute) {
					start += period;
				}
				if(start == minute) {
					return true;
				}
				continue;
			}
			int rangeIndex = per.indexOf("-");
			if(rangeIndex != -1) {
				int start = Integer.parseInt(per.substring(0, rangeIndex));
				int end = Integer.parseInt(per.substring(rangeIndex + 1));
				if(start <= minute && minute <= end) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean checkHour(String cron, LocalDateTime ltd) {
		int hour = ltd.getHour();
		String hourStr = String.valueOf(hour);
		if("*".equals(cron) || hourStr.equals(cron)) {
			return true;
		}
		for(String per : cron.split(",")) {
			if(hourStr.equals(per)) {
				return true;
			}
			int slashIndex = per.indexOf("/");
			if(slashIndex != -1) {
				int start = Integer.parseInt(per.substring(0, slashIndex));
				int period = Integer.parseInt(per.substring(slashIndex + 1));
				while(start < hour) {
					start += period;
				}
				if(start == hour) {
					return true;
				}
				continue;
			}
			int rangeIndex = per.indexOf("-");
			if(rangeIndex != -1) {
				int start = Integer.parseInt(per.substring(0, rangeIndex));
				int end = Integer.parseInt(per.substring(rangeIndex + 1));
				if(start <= hour && hour <= end) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean checkDay(String cron, LocalDateTime ltd) {
		int day = ltd.getDayOfMonth();
		String dayStr = String.valueOf(day);
		if("*".equals(cron) || dayStr.equals(cron) || "?".equals(cron)) {
			return true;
		}
		int max = ltd.getMonth().maxLength();//本月总天数
		for(String per : cron.split(",")) {
			if(dayStr.equals(per)) {
				return true;
			}
			int slashIndex = per.indexOf("/");
			if(slashIndex != -1) {
				String startStr = per.substring(0, slashIndex);//可能带L
				int start;
				if(startStr.endsWith("L")) {
					int last = Integer.parseInt(startStr.substring(0, startStr.length() - 1));
					start = max + 1 - last;
				}else {
					start = Integer.parseInt(startStr);
				}
				int period = Integer.parseInt(per.substring(slashIndex + 1));
				while(start < day) {
					start += period;
				}
				if(start == day) {
					return true;
				}
				continue;
			}
			int rangeIndex = per.indexOf("-");
			if(rangeIndex != -1) {
				String startStr = per.substring(0, rangeIndex);//可能带L
				int start;
				if(startStr.endsWith("L")) {
					int last = Integer.parseInt(startStr.substring(0, startStr.length() - 1));
					start = max + 1 - last;
				}else {
					start = Integer.parseInt(startStr);
				}
				String endStr = per.substring(0, rangeIndex);//可能带L
				int end;
				if(endStr.endsWith("L")) {
					int last = Integer.parseInt(endStr.substring(0, endStr.length() - 1));
					end = max + 1 - last;
				}else {
					end = Integer.parseInt(endStr);
				}
				if(start <= day && day <= end) {
					return true;
				}
				continue;
			}
			if(per.endsWith("L")) {
				int last = Integer.parseInt(per.substring(0, per.length() - 1));
				if(max + 1 - last == day) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean checkMonth(String cron, LocalDateTime ltd) {
		int month = ltd.getMonthValue();
		String monthStr = String.valueOf(month);
		if("*".equals(cron) || monthStr.equals(cron)) {
			return true;
		}
		for(String per : cron.split(",")) {
			if(monthStr.equals(per)) {
				return true;
			}
			int slashIndex = per.indexOf("/");
			if(slashIndex != -1) {
				int start = Integer.parseInt(per.substring(0, slashIndex));
				int period = Integer.parseInt(per.substring(slashIndex + 1));
				while(start < month) {
					start += period;
				}
				if(start == month) {
					return true;
				}
				continue;
			}
			int rangeIndex = per.indexOf("-");
			if(rangeIndex != -1) {
				int start = Integer.parseInt(per.substring(0, rangeIndex));
				int end = Integer.parseInt(per.substring(rangeIndex + 1));
				if(start <= month && month <= end) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean checkWeek(String cron, LocalDateTime ltd) {
		int week = ltd.getDayOfWeek().getValue();
		String weekStr = String.valueOf(week);
		if("*".equals(cron) || weekStr.equals(cron) || "?".equals(cron)) {
			return true;
		}
		int max = ltd.getMonth().maxLength();//本月总天数
		int day = ltd.getDayOfMonth();
		for(String per : cron.split(",")) {
			if(weekStr.equals(per)) {
				return true;
			}
			int slashIndex = per.indexOf("/");
			if(slashIndex != -1) {
				String startStr = per.substring(0, slashIndex);//必须带L或#
				int start = day;
				if(startStr.length() == 2) {//带L
					while(start <= max - 7) {
						start += 7;
					}
					start += Integer.parseInt(startStr.substring(0, 1)) - week;
				}else {//带#
					int poundWeek = Integer.parseInt(startStr.substring(0, 1));
					int ordinal = Integer.parseInt(startStr.substring(2, 3));
					while(start >= 8) {
						start -= 7;
					}
					start -= week - poundWeek;//第一个周poundWeek
					start += 7 * (ordinal - 1);//第ordinal个周poundWeek
				}				
				int period = Integer.parseInt(per.substring(slashIndex + 1));
				while(start < day) {
					start += period;
				}
				if(start == day) {
					return true;
				}
				continue;
			}
			int rangeIndex = per.indexOf("-");
			if(rangeIndex != -1) {
				String startStr = per.substring(0, rangeIndex);//必须带L或#
				int start = day;
				if(startStr.length() == 2) {//带L
					while(start <= max - 7) {
						start += 7;
					}
					start += Integer.parseInt(startStr.substring(0, 1)) - week;
				}else {//带#
					int poundWeek = Integer.parseInt(startStr.substring(0, 1));
					int ordinal = Integer.parseInt(startStr.substring(2, 3));
					while(start >= 8) {
						start -= 7;
					}
					start -= week - poundWeek;//第一个周poundWeek
					start += 7 * (ordinal - 1);//第ordinal个周poundWeek
				}
				String endStr = per.substring(0, rangeIndex);//必须带L或#
				int end = day;
				if(endStr.length() == 2) {//带L
					while(end <= max - 7) {
						end += 7;
					}
					end += Integer.parseInt(endStr.substring(0, 1)) - week;
				}else {//带#
					int poundWeek = Integer.parseInt(endStr.substring(0, 1));
					int ordinal = Integer.parseInt(endStr.substring(2, 3));
					while(end >= 8) {
						end -= 7;
					}
					end -= week - poundWeek;//第一个周poundWeek
					end += 7 * (ordinal - 1);//第ordinal个周poundWeek
				}
				if(start <= day && day <= end) {
					return true;
				}
				continue;
			}
			if(per.length() == 2) {//带L
				int weekDay = day;
				while(weekDay <= max - 7) {
					weekDay += 7;
				}
				if(day == weekDay + Integer.parseInt(per.substring(0, 1)) - week) {
					return true;
				}
				continue;
			}
			if(per.length() == 3) {//带#
				int weekDay = day;
				int poundWeek = Integer.parseInt(per.substring(0, 1));
				int ordinal = Integer.parseInt(per.substring(2, 3));
				while(weekDay >= 8) {
					weekDay -= 7;
				}
				if(day == weekDay - week + poundWeek + 7 * (ordinal - 1)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean checkYear(String cron, LocalDateTime ltd) {
		int year = ltd.getYear();
		String yearStr = String.valueOf(year);
		if("*".equals(cron) || yearStr.equals(cron)) {
			return true;
		}
		for(String per : cron.split(",")) {
			if(yearStr.equals(per)) {
				return true;
			}
			int slashIndex = per.indexOf("/");
			if(slashIndex != -1) {
				int start = Integer.parseInt(per.substring(0, slashIndex));
				int period = Integer.parseInt(per.substring(slashIndex + 1));
				while(start < year) {
					start += period;
				}
				if(start == year) {
					return true;
				}
				continue;
			}
			int rangeIndex = per.indexOf("-");
			if(rangeIndex != -1) {
				int start = Integer.parseInt(per.substring(0, rangeIndex));
				int end = Integer.parseInt(per.substring(rangeIndex + 1));
				if(start <= year && year <= end) {
					return true;
				}
			}
		}
		return false;
	}
	
}
