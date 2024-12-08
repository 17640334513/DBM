package util;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropUtil {

	public static String FILE_DIR;
	
	public static final Map<String, String> propMap = new HashMap<String, String>();

	static {
		init();
	}
	
	public static String get(String propKey) {
		return propMap.get(propKey);
	}

	public static int getInt(String propKey) {
		return Integer.parseInt(get(propKey));
	}

	public static long getLong(String propKey) {
		return Long.parseLong(get(propKey));
	}

	public static boolean getBoolean(String propKey) {
		return Boolean.parseBoolean(get(propKey));
	}

	public static char getChar(String propKey) {
		return get(propKey).charAt(0);
	}

	public static BigDecimal getBigDecimal(String propKey) {
		return new BigDecimal(get(propKey));
	}
	
	public static void init() {
		try {
			Path propDir = getPath();
			for(Path path : Files.newDirectoryStream(propDir)) {
				String fileName = path.getFileName().toString();
				if (fileName.endsWith(".properties")) {
					Properties prop = new Properties();
					try (InputStream in = Files.newInputStream(path)) {
						prop.load(in);
					}
					fileName = fileName.substring(0, fileName.length() - 10);
					if("src.".equals(fileName)) {
						fileName = "";
					}
					String keyPre = fileName;
					prop.forEach((key, value) -> {
						//Properties会把key自动trim，而对于value只自动去掉左边的空格
						propMap.put(keyPre + key, value.toString().trim());
					});
					prop.clear();
				}
			}		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Path getPath() throws Exception {
		String currentPath = PropUtil.class.getResource("").getPath();
		if(currentPath.contains(":")) currentPath = currentPath.substring(1);
		Path tomcatDir = Paths.get(currentPath, "..", "..", "file", "properties");
		if (Files.exists(tomcatDir)) {
			tomcatDir = tomcatDir.toRealPath();
			FILE_DIR = tomcatDir.getParent() + File.separator;
			return tomcatDir;
		} else {
			Path mainDir = Paths.get("WebContent", "WEB-INF", "file", "properties");
			FILE_DIR = mainDir.getParent() + File.separator;
			return mainDir;
		}		
	}
	
}
