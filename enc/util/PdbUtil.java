package util;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import uf.UF;
import java.util.HashMap;
import java.util.Map;

public class PdbUtil {

	public static final Map<String, Properties> TABLE_MAP = new HashMap<>();
	
	static {
		init();
	}		
	
	public static void init() {
		try {
			Path dir = Paths.get(PropUtil.get("sys.PDB_PATH"));
			for (Path file : Files.newDirectoryStream(dir)) {
				String fileName = file.getFileName().toString();
				if (fileName.endsWith(".properties")) {
					Properties table = new Properties();
					try (InputStream in = Files.newInputStream(file)) {
						table.load(in);
					}
					String tableName = fileName.substring(0, fileName.length() - 11);
					TABLE_MAP.put(tableName, table);
				}
			}
			LogUtil.print("UDB加载成功，共有" + TABLE_MAP.size() + "张表");
		} catch (Exception e) {
			LogUtil.print(e);
		}
	}
	
	public static Properties getTable(String tableName) {
		return TABLE_MAP.get(tableName);
	}
	
	public static String get(String tableName, String key) {
		return TABLE_MAP.get(tableName).getProperty(key);
	}
	/**无则新增，有则修改*/
	public static void saveOrUpdate(String tableName, String key, String value) {
		if(!value.equals(TABLE_MAP.get(tableName).put(key, value))) {
			store(tableName);
		}		
	}
	/**无则新增，有则不动*/
	public static void save(String tableName, String key, String value) {
		Properties table = TABLE_MAP.get(tableName);
		if(!table.containsKey(key)) {
			table.setProperty(key, value);
			store(tableName);
		}
	}
	/**无则不动，有则修改*/
	public static void update(String tableName, String key, String value) {
		Properties table = TABLE_MAP.get(tableName);
		Object obj = table.get(key);
		if(obj != null && !obj.equals(value)) {
			table.setProperty(key, value);
			store(tableName);
		}		
	}
	public static void delete(String tableName, String key) {
		if(TABLE_MAP.get(tableName).remove(key) != null) {
			store(tableName);
		}
	}
	/**异步更新到文件*/
	private static void store(String tableName) {
		UF.THREADS.execute(() -> {
			try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(PropUtil.get("sys.PDB_PATH"), tableName), StandardCharsets.UTF_8, StandardOpenOption.APPEND)){
				TABLE_MAP.get(tableName).store(writer, null);
			} catch (Exception e) {
				LogUtil.print(e);
			}
		});
	}
	
}
