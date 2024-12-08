package util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class StringUtil {
	
	/** 得到x位随机字符串 */
	public static String getRandom(int x) {
		String nb = "0123456789qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < x; i++) {
			ret.append(nb.charAt(ThreadLocalRandom.current().nextInt(nb.length())));
		}
		return ret.toString();
	}

	/**获取x位随机数字验证码*/
	public static String getCode(int x) {
		StringBuilder ret = new StringBuilder();
		for (int i = 0; i < x; i++) {
			ret.append(ThreadLocalRandom.current().nextInt(9));
		}
		return ret.toString();
	}
	
	/** 将传入的字符串打乱顺序 */
	public static String disorganize(String str){
		StringBuilder ret=new StringBuilder();
		while(str.length()>0){
			int random=ThreadLocalRandom.current().nextInt(str.length());
			ret.append(str.charAt(random));
			str=str.substring(0, random)+str.substring(random+1);
		}
		return ret.toString();
	}
	
	/** 按特定顺序打乱字符串 */
	public static String disorganize(String str, int[] is) {
    	char[] cs = new char[is.length];
    	for(int i=0;i<is.length;i++) {
    		cs[i] = str.charAt(is[i]);
    	}
    	return new String(cs);
    }
	
	/** 获取str中第rank次出现c的位置 */
	public static int getCharIndex(String str, char c, int rank) {		
		for(int i = 0; i < str.length(); i++) {
			if(c == str.charAt(i)) {
				if(rank == 1) return i;
				rank--;
			}
		}
		return -1;
	}
	
	/** 获取str中第rank次出现subStr的位置 */
	public static int findSubStrIndex(String str,String subStr,int rank){
		int index = str.indexOf(subStr);
        while (index != -1) {       	
            if(rank==1) break;
            rank--;
            index = str.indexOf(subStr, index + 1);      
        }
        return index;
	}
	
	/** 获取str中有多少个subStr */
	public static int findSubStrCount(String str,String subStr){
		int count =0, start =0;  
        while((start=str.indexOf(subStr,start))>=0){  
            start += subStr.length();  
            count ++;  
        }  
        return count;  
	}
	
	/** 从str中删掉chars,如：removeChars("34455667","34567")返回的是"456" */
	public static String removeChars(String str,String chars){
		StringBuilder strb = new StringBuilder(str);
		for(int i=0;i<chars.length();i++){
			String c=String.valueOf(chars.charAt(i));
			strb.deleteCharAt(strb.indexOf(c));
		}
		return strb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T stringToT(String str, Class<?> c) {
		if (c == int.class || c == Integer.class) return (T) Integer.valueOf(str);
		else if (c == long.class || c == Long.class) return (T) Long.valueOf(str);
		else if (c == boolean.class || c == Boolean.class) return (T) Boolean.valueOf(str);
		else if (c == char.class || c == Character.class) return (T) new Character(str.charAt(0));
		else if (c == BigDecimal.class) return (T) new BigDecimal(str);
		else if (c == StringBuilder.class) return (T) new StringBuilder(str);
		else if (c == StringBuffer.class) return (T) new StringBuffer(str);
		else if (c == LocalDateTime.class) return (T) DateUtil.DTF19.parse(str);
		return (T) str;
	}
	
	/**比较字符串大小(逐位比较ASCII)*/
	public static int compare(String big, String small) {
		char[] bigs = big.toCharArray();
		char[] smalls = small.toCharArray();
		int bigsLen = bigs.length;
		int smallsLen = smalls.length;
		boolean b = bigsLen > smallsLen;
		int num = b ? smallsLen : bigsLen;
		for(int i = 0; i < num; i++) {
			if(bigs[i] > smalls[i]) return 1;
			if(bigs[i] < smalls[i]) return -1;
		}
		if(bigsLen == smallsLen) return 0;
		return b ? 1 : -1;
	}
	
	/**判断str中是否包含英文*/
	public static boolean hasEnglish(String str) {
		return Pattern.compile("[a-zA-z]").matcher(str).find();
	}
	/**判断str中是否包含中文*/
	public static boolean hasChinese(String str) {
		return Pattern.compile("[\\u4e00-\\u9fa5]").matcher(str).find();
	}
	
	/**截取str中第一个start到start后第一个end之间的字符串，若start为null，则表示从头开始；若end为null，则表示到结尾*/
	public static String substring(String str, String start, String end) {
		int startIndex;
		if(start == null) {
			startIndex = 0;
		}else {
			startIndex = str.indexOf(start) + start.length();
		}
		if(end == null) {
			return str.substring(startIndex);
		}else {
			int endIndex = str.indexOf(end, startIndex);
			return str.substring(startIndex, endIndex);
		}
	}
	
	public static void main(String[] args) {
		System.out.println(substring("start123t56t7", "start", "t"));
	}
	
}
