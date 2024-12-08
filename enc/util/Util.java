package util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class Util {	
	
	private static Pattern telPattern = Pattern.compile("^(1[3-9])\\d{9}$");
	
	/**判断是否符合手机号格式*/
	public static boolean isTel(String tel) {
		return telPattern.matcher(tel).matches();
	}
	
	public static List newArrayList(Object... args) {
		List list = new ArrayList();
		for (Object arg : args) {
			list.add(arg);
		}
		return list;
	}

	public static boolean isEmpty(Object obj) {
		return obj == null || "".equals(obj);
	}

	/** 计算密码强度评分 */
	public static int pwdScore(String pwd) {
		int score = 0;
		int hasUpper = 0, hasLower = 0, hasNumber = 0;
		StringBuilder sb = new StringBuilder();
		char[] cs = pwd.toCharArray();
		for (int i = 0; i < cs.length; i++) {
			score += 1;
			char c = cs[i];
			boolean newChar = sb.indexOf(c + "") == -1;
			if (c >= 'A' && c <= 'Z') {
				if(hasUpper == 0) hasUpper = 1;				
			} else if (c >= 'a' && c <= 'z') {
				if(hasLower == 0) hasLower = 1;
			} else if (c >= '0' && c <= '9') {
				if(hasNumber == 0) hasNumber = 1;
			} else if (newChar) {
				score += 5;
			} else {
				score += 1;
			}
			if (i > 0 && c != cs[i - 1] + 1 && c != cs[i - 1] - 1 && newChar) {
				score += 6;
			}
			sb.append(c);
		}
		return score + (int) Math.pow(3, hasUpper + hasLower + hasNumber);
	}
	
	public static boolean delay(long millis) {
		try {
			Thread.sleep(millis);
			return true;
		} catch (InterruptedException e) {
			LogUtil.print(e);
		}
		return false;
	}
	
	/**
	 * 计算从last到next的增长率
	 * @return 百分比数去掉百分号%
	 * */
	public static BigDecimal riseRate(BigDecimal last, BigDecimal next) {
		BigDecimal rise = next.subtract(last).setScale(4, BigDecimal.ROUND_HALF_UP);
		return rise.multiply(new BigDecimal(100)).divide(last, 2, BigDecimal.ROUND_HALF_UP);
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(new Date(9999999999999l));
	}
	
}
