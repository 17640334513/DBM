package util;

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DESUtil {

	private static final String key = "E=mc^2";// 默认密钥

	/** 加密字符串(默认密钥) */
	public static String encode(String strIn) throws Exception{
		return encode(strIn,key);
	}

	/** 解密字符串(默认密钥) */
	public static String decode(String strIn) throws Exception{
		return decode(strIn, key);
	}

	/** 加密字符串(自定义密钥) */
	public static String encode(String strIn, String ownKey) throws Exception{		
		return byteArr2HexStr(encode(strIn.getBytes(),ownKey));
	}	

	/** 解密字符串(自定义密钥) */
	public static String decode(String strIn, String ownKey) throws Exception{		
		return new String(decode(hexStr2ByteArr(strIn),ownKey));
	}
	
	public static byte[] encode(byte[] arrB, String ownKey) throws Exception{
		Cipher encryptCipher=initEncrypt(ownKey);
		return encryptCipher.doFinal(arrB);
	}
	
	public static byte[] decode(byte[] arrB, String ownKey) throws Exception{
		Cipher decryptCipher=initDecrypt(ownKey);
		return decryptCipher.doFinal(arrB);
	}
	
	/** 初始化加密工具 */
	private static Cipher initEncrypt(String ownKey) throws Exception{
		Key key = getKey(ownKey.getBytes());
		Cipher encryptCipher = Cipher.getInstance("DES");
		encryptCipher.init(Cipher.ENCRYPT_MODE, key);
		return encryptCipher;
	}
	
	/** 初始化解密工具 */
	private static Cipher initDecrypt(String ownKey) throws Exception{
		Key key = getKey(ownKey.getBytes());
		Cipher decryptCipher = Cipher.getInstance("DES");
		decryptCipher.init(Cipher.DECRYPT_MODE, key);
		return decryptCipher;
	}
	
	/**
	 * 将byte数组转换为表示16进制值的字符串， 如：byte[]{8,18}转换为：0813 <br>
	 * 和hexStr2ByteArr(String strIn) 互为可逆的转换过程
	 */
	private static String byteArr2HexStr(byte[] arrB) {
		int iLen = arrB.length;		
		StringBuilder sb = new StringBuilder(iLen * 2);// 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍
		for (int i = 0; i < iLen; i++) {
			int intTmp = arrB[i];			
			while (intTmp < 0) {// 把负数转换为正数
				intTmp = intTmp + 256;
			}			
			if (intTmp < 16) {// 小于0F的数需要在前面补0
				sb.append("0");
			}
			sb.append(Integer.toString(intTmp, 16));
		}
		return sb.toString();
	}

	/**
	 * 将表示16进制值的字符串转换为byte数组  <br>
	 * 和byteArr2HexStr(byte[] arrB)互为可逆的转换过程
	 */
	private static byte[] hexStr2ByteArr(String strIn) {
		byte[] arrB = strIn.getBytes();
		int iLen = arrB.length;		
		byte[] arrOut = new byte[iLen / 2];// 两个字符表示一个字节，所以字节数组长度是字符串长度除以2
		for (int i = 0; i < iLen; i = i + 2) {
			String strTmp = new String(arrB, i, 2);
			arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
		}
		return arrOut;
	}

	/** 从指定字符串生成密钥，密钥所需的字节数组长度为8位 不足8位时后面补0，超出8位只取前8位 */
	private static Key getKey(byte[] arrBTmp) {		
		byte[] arrB = new byte[8];		
		for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {// 将原始字节数组转换为8位
			arrB[i] = arrBTmp[i];
		}
		Key key = new SecretKeySpec(arrB, "DES");
		return key;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(Base64.getEncoder().encodeToString("".getBytes()));
	}
}