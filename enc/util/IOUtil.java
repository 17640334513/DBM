package util;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public class IOUtil {

	/**
	 * 压缩文件或文件夹（当文件名为中文时，压缩后会出现文件名乱码，但文件里的中文不会乱码）
	 * @param oriFile : 被压缩的文件或文件夹
	 * @param newZip : 压缩后的zip文件全路径
	 */
	public static void toZip(Path oriFile, Path newZip) throws Exception {
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(newZip))) {
			zip(zos, oriFile, null);
			LogUtil.print("\"" + oriFile + "\" 已压缩至 \"" + newZip + "\"", 2);
		}
	}
	@SuppressWarnings("resource")
	private static void zip(ZipOutputStream zos, Path oriFile, String base) throws Exception {
		if (Files.isDirectory(oriFile)) {
			if (base == null) base = "";
			else zos.putNextEntry(new ZipEntry(base = base + File.separator));
			for (Path file : Files.newDirectoryStream(oriFile)) {
				zip(zos, file, base + file.getFileName());
			}
		} else {
			zos.putNextEntry(new ZipEntry(base));
			try (InputStream in = Files.newInputStream(oriFile)) {
				int b;
				while ((b = in.read()) != -1) zos.write(b);
			}
		}
	}

	/** 根据xml报文或soap报文获取Document对象(拼装报文时直接上字符串拼装！) */
	public static Document getDocument(String xmlOrSoap) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		try (InputStream is = new ByteArrayInputStream(xmlOrSoap.getBytes())) {
			return db.parse(is);
		}
	}
	
	/** 判断文件是否已经写入完成 */
	public static boolean isOk(Path file) throws Exception {
		long oldLength = 0l;		
		for(;;Util.delay(500)) {
			long length = Files.size(file);
			if(length > oldLength) {
				oldLength = length;
			}else {
				return true;
			}
		}
	}
	
	/**删除整个文件夹*/
	public static void deleteDir(Path dir) throws Exception {
		try(DirectoryStream<Path> ds = Files.newDirectoryStream(dir)){
			Iterator<Path> pathIt = ds.iterator();
			while(pathIt.hasNext()) {
				Path path = pathIt.next();
				if(Files.isDirectory(path)) {
					deleteDir(path);
				}else {
					Files.delete(path);
				}
			}
		}
		Files.delete(dir);
	}
	
	/**快速获取文件总行数*/
	public static int getTotalLines(File file) throws Exception {
		try(FileReader in = new FileReader(file);LineNumberReader reader = new LineNumberReader(in)){
			reader.skip(Long.MAX_VALUE);
	        return reader.getLineNumber() + 1;
		}
    }
	
	/**写csv文件*/
	public static void writeCsv(Path file, List<String[]> dataList) throws Exception {
		try(BufferedWriter writer = Files.newBufferedWriter(file, Charset.forName("GBK"))){
			for(String[] datas : dataList) {
				writer.write("\"" + String.join("\",\"", datas) + "\"");
				writer.newLine();
			}
		}
	}
	
}
