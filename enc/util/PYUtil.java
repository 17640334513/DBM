package util;

import java.util.HashMap;
import java.util.Map;

public class PYUtil {

	public static Map<Integer, String> numCNMap = new HashMap<>();
	static {
		numCNMap.put(0, "零");
		numCNMap.put(1, "一");
		numCNMap.put(2, "二");
		numCNMap.put(3, "三");
		numCNMap.put(4, "四");
		numCNMap.put(5, "五");
		numCNMap.put(6, "六");
		numCNMap.put(7, "七");
		numCNMap.put(8, "八");
		numCNMap.put(9, "九");
	}
	
	/** 
	 * 获取字符串第一个字首字母<br>
	 * 只能识别一级汉字（常用字）
	 *  */
	public static char getFirstPY(String str) throws Exception {
		int charGBK = getCode(str.charAt(0));		
		if (charGBK >= 45217 && charGBK <= 45252) return 'A';
		else if (charGBK >= 45253 && charGBK <= 45760) return 'B';
		else if (charGBK >= 45761 && charGBK <= 46317) return 'C';
		else if (charGBK >= 46318 && charGBK <= 46825) return 'D';
		else if (charGBK >= 46826 && charGBK <= 47009) return 'E';
		else if (charGBK >= 47010 && charGBK <= 47296) return 'F';
		else if (charGBK >= 47297 && charGBK <= 47613) return 'G';
		else if (charGBK >= 47614 && charGBK <= 48118) return 'H';
		else if (charGBK >= 48119 && charGBK <= 49061) return 'J';
		else if (charGBK >= 49062 && charGBK <= 49323) return 'K';
		else if (charGBK >= 49324 && charGBK <= 49895) return 'L';
		else if (charGBK >= 49896 && charGBK <= 50370) return 'M';
		else if (charGBK >= 50371 && charGBK <= 50613) return 'N';
		else if (charGBK >= 50614 && charGBK <= 50621) return 'O';
		else if (charGBK >= 50622 && charGBK <= 50905) return 'P';
		else if (charGBK >= 50906 && charGBK <= 51386) return 'Q';
		else if (charGBK >= 51387 && charGBK <= 51445) return 'R';
		else if (charGBK >= 51446 && charGBK <= 52217) return 'S';
		else if (charGBK >= 52218 && charGBK <= 52697) return 'T';
		else if (charGBK >= 52698 && charGBK <= 52979) return 'W';
		else if (charGBK >= 52980 && charGBK <= 53688) return 'X';
		else if (charGBK >= 53689 && charGBK <= 54480) return 'Y';
		else if (charGBK >= 54481 && charGBK <= 55289) return 'Z';
		else return Character.toUpperCase(str.charAt(0));
	}

	/** 获取汉字的编码 */
	public static int getCode(char c) throws Exception {
		byte[] bytes = (c + "").getBytes("GB2312");
		if (bytes.length < 2) return 0;
		return (bytes[0] << 8 & 0xff00) + (bytes[1] & 0xff);
	}

	/**目前只支持0~999的整数*/
	public static String getCNNum(int i) {
		if(i < 10) {
			return numCNMap.get(i);
		}else if(i < 100) {
			int tens = i / 10;
			String ten = tens == 1 ? "十" : numCNMap.get(tens) + "十";
			int ones = i % 10;
			String one = ones == 0 ? "" : numCNMap.get(ones);
			return ten + one;
		}else if(i < 1000) {
			int hundreds = i / 100;
			String hundred = numCNMap.get(hundreds) + "百";
			int underHundred = i % 100;
			if(underHundred == 0) return hundred;
			int tens = underHundred / 10;
			String ten = tens == 0 ? "零" : numCNMap.get(tens) + "十";
			int ones = underHundred % 10;
			String one = ones == 0 ? "" : numCNMap.get(ones);
			return hundred + ten + one;
		}
		return "目前只支持0~999的整数";
	}
	
	/**判断一个字符是否是汉字*/
    public static boolean isChinese(char c) {
        return String.valueOf(c).matches("[\u4e00-\u9fa5]");
    }
    
	public static void main(String[] args) {
		System.out.println(isChinese('〇'));
	}
}
