package uf;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("serial")
public class UFConcurrentMap extends ConcurrentHashMap<String, Object>{

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

	@Override
	/**重写是为了避免value为null时报错*/
	public Object put(String key, Object value) {
		if(value == null) return remove(key);
		return super.put(key, value);
	}
	
	/**key不存在才put，重写是为了避免value为null时报错*/
	@Override
	public Object putIfAbsent(String key, Object value) {
		if (containsKey(key)) return get(key);
		else return put(key, value);
	}
	
}
