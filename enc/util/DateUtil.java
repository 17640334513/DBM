package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {

	public static final DateTimeFormatter DTF6 = DateTimeFormatter.ofPattern("yyMMdd");
	public static final DateTimeFormatter DTF8 = DateTimeFormatter.ofPattern("yyyyMMdd");
	public static final DateTimeFormatter DTF10 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final DateTimeFormatter DTF12 = DateTimeFormatter.ofPattern("yyMMddHHmmss");
	public static final DateTimeFormatter DTF14 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	public static final DateTimeFormatter DTF15 = DateTimeFormatter.ofPattern("yyMMddHHmmssSSS");
	public static final DateTimeFormatter DTF17 = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");	
	public static final DateTimeFormatter DTF19 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static final DateTimeFormatter DTF23 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");
	public static final DateTimeFormatter DTF8T = DateTimeFormatter.ofPattern("HH:mm:ss");

	/** 得到自定义格式的时间字符串 */
	public static String nowX(String format) {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
	}

	public static String now8() {
		return LocalDateTime.now().format(DTF8);
	}

	public static String now10() {
		return LocalDateTime.now().format(DTF10);
	}
	
	public static String now12() {
		return LocalDateTime.now().format(DTF12);
	}
	
	public static String now14() {
		return LocalDateTime.now().format(DTF14);
	}

	public static String now15() {
		return LocalDateTime.now().format(DTF15);
	}
	
	public static String now17() {
		return LocalDateTime.now().format(DTF17);
	}

	public static String now19() {
		return LocalDateTime.now().format(DTF19);
	}

	public static String now23() {
		return LocalDateTime.now().format(DTF23);
	}
	
	public static String nowTime() {
		return LocalDateTime.now().format(DTF8T);
	}
	
	public static LocalDateTime dateToLocalDateTime(Date date) {
		return date.toInstant().atOffset(ZoneOffset.of("+8")).toLocalDateTime();
	}        
	public static Date localDateTimeToDate(LocalDateTime localDateTime) {
		return Date.from(localDateTime.toInstant(ZoneOffset.of("+8")));
	}
	public static LocalDate dateToLocalDate(Date date) {
		return date.toInstant().atOffset(ZoneOffset.of("+8")).toLocalDate();
	}
	
}
