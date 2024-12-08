package util;

import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import uf.UF;

public class EncodeUtil {
	
    /** 16进制的字符数组 */
    private static final String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};

    private static final String key = "42Xc^";
    private static final int[] is = {25,18,21,9,10,5,26,22,29,16,27,8,20,12,15,24,2,6,14,4};
    
    public static String MD5(String source) throws Exception {
    	return encode(source, "MD5");
    }
    public static String SHA256(String source) throws Exception {
    	return encode(source, "SHA-256");
    }
    public static String encode(String source, String algorithm) throws Exception{
        MessageDigest messageDigest = MessageDigest.getInstance(algorithm);// 获得摘要对象       
        messageDigest.update(source.getBytes(UF.UTF8));// 使用指定的字节数组更新摘要信息       
        return byteArrayToHexString(messageDigest.digest());// messageDigest.digest()获得16位长度
    }
    
    /** HMAC-SHA256加密 */
    public static String hmacSHA256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] array = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (byte item : array) {
            sb.append(Integer.toHexString((item & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }
    
    /** 独创终极加密(密文20位) */
    public static String encodeUF(String source) throws Exception{   	
    	return StringUtil.disorganize(MD5(key + source), is);
    }
    
    /** 转换字节数组为16进制字符串 */
    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte tem : bytes)  stringBuilder.append(byteToHexString(tem));
        return stringBuilder.toString();
    }
    /** 转换byte到16进制 */
    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)  n = 256 + n;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }
    
}
