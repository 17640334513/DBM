package uf;

import java.math.BigDecimal;
import java.util.HashMap;

@SuppressWarnings("serial")
public class UFMap extends HashMap<String, Object>{
	
	public UFMap add(String key, Object value) {
		put(key, value);
		return this;
	}

	public boolean contains(String key, Object value) {
		if(containsKey(key)) {
			return value.equals(get(key));
		}
		return false;
	}
	
	/** 调用此方法需要确保可以强制转换 */
	@SuppressWarnings("unchecked")
	public <T> T getT(String key) {
		return (T) get(key);
	}
	
	public String getString(String key){
		return get(key).toString();
	}
	
	public int getInt(String key){
		return Integer.parseInt(getString(key));
	}
	
	public long getLong(String key){
		return Long.parseLong(getString(key));
	}
	
	public boolean getBoolean(String key){
		return Boolean.parseBoolean(getString(key));
	}
	
	public char getChar(String key){
		return getString(key).charAt(0);
	}
	
	public BigDecimal getBigDecimal(String key){
		return new BigDecimal(getString(key));
	}

}
