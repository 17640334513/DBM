package dao;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import uf.UFMap;
import util.LogUtil;
import util.PropUtil;

public class Dao{
	
	/**查询超时时间*/
	public static int JDBC_QUERY_TIMEOUT=PropUtil.getInt("sys.JDBC_QUERY_TIMEOUT");
	/**批处理提交单位*/
	public static int JDBC_BATCH_UNIT=PropUtil.getInt("sys.JDBC_BATCH_UNIT");
	
	/** 数据源 */
	private static Map<String, DataSource> dataSourceMap = new HashMap<>();
	
	/** 初始化数据源 */
	public static void init() {
		try {
			String[] dataSources = PropUtil.get("DATA_SOURCES").split(",");
			Context context = null;
			try {
				context = new InitialContext();
				for(String dataSource : dataSources) {
					dataSourceMap.put(dataSource, (DataSource) context.lookup(dataSource));
					LogUtil.print("连接数据源["+dataSource+"]成功", "");
				}
			} finally {
				if(context != null) context.close();
			}
		}catch(Exception e) {
			LogUtil.print(e);
		}
    }
    
	/** 获取连接 */
	public static Connection getConn(String dataSource) throws Exception {
		return dataSourceMap.get(dataSource).getConnection();
	}
	
	/**查询tableName表的所有数据*/
	public static List<UFMap> queryAll(String tableName, String dataSource) throws Exception {
		String sql = "SELECT * FROM " + tableName;
		return query(sql, dataSource);
	}
	/** 根据sql语句查询 */
	public static List<UFMap> query(String sql, String dataSource) throws Exception {
		print("语句：" + sql);
		long startTime=System.currentTimeMillis();	
		try (Connection conn = getConn(dataSource); PreparedStatement ps=conn.prepareStatement(sql)) {
			ps.setQueryTimeout(JDBC_QUERY_TIMEOUT);
			try(ResultSet rs = ps.executeQuery()){
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnCount = rsmd.getColumnCount();
				List<UFMap> retList = new ArrayList<UFMap>();
				while(rs.next()){
					UFMap map = new UFMap();
					for(int i = 1; i <= columnCount; i ++) {
						Object obj = rs.getObject(i);
						if(obj instanceof Clob) {//把Clob字段转成String
							Clob clob = (Clob) obj;
							obj = clob.getSubString(1l, (int) clob.length());
						}
						map.put(rsmd.getColumnName(i), obj);
					}
					retList.add(map);
				}
				print("本次查到" + retList.size() + "条记录，消耗时间：" + (System.currentTimeMillis()-startTime) + " 毫秒");
				return retList;
			}						
		}
	}
	
	/**查询序列*/
	public static String querySequence(String sequenceName, String dataSource) throws Exception{
		String sql="SELECT "+sequenceName+".NEXTVAL FROM DUAL";
		print("语句:"+sql);
		long startTime=System.currentTimeMillis();
		try (Connection conn = getConn(dataSource); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setQueryTimeout(JDBC_QUERY_TIMEOUT);
			try (ResultSet rs = ps.executeQuery()) {
				String ret=null;
				if(rs.next()) ret=rs.getString(1);
				print("本次查到序列："+ret+"，消耗时间："+(System.currentTimeMillis()-startTime)+" 毫秒");
				return ret;
			}
		}
	}
	
	/**MYSQL查询database库中所有表名*/
	public static List<String> getTables(String database, String dataSource) throws Exception{
		String sql = "SHOW TABLES FROM " + database;
		print("语句:" + sql);
		long startTime = System.currentTimeMillis();
		try (Connection conn = getConn(dataSource); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setQueryTimeout(JDBC_QUERY_TIMEOUT);
			try (ResultSet rs = ps.executeQuery()) {
				List<String> retList=new ArrayList<>();
				while(rs.next()) {
					retList.add(rs.getString(1));
				}
				print("本次查到"+retList.size()+"条记录，消耗时间："+(System.currentTimeMillis()-startTime)+" 毫秒");
				return retList;
			}
		}
	}

	/**
	 * 运行增、删、改语句，返回成功数量
	 * @param sqlKey : sql文件名.sqlName
	 * */
	public static int executeSql(String sql, String dataSource) throws Exception{
		try (Connection conn = getConn(dataSource)) {
			print("语句："+sql);
			long startTime=System.currentTimeMillis();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				int ret=ps.executeUpdate();		
				print("本次修改成功"+ret+"条记录，消耗时间："+(System.currentTimeMillis()-startTime)+" 毫秒");
				return ret;
			}
		}
	}
	
	/**
	 * 插表，返回成功或失败
	 * @param tableName : 表名
	 * @param map : Map<列名,值>
	 * */
	public static boolean save(String tableName, UFMap map, String dataSource) throws Exception{
		try (Connection conn = getConn(dataSource)) {
			StringBuilder columns=new StringBuilder();
			StringBuilder values=new StringBuilder();
			map.forEach((columnName, value) -> {
				if(columnName.equals(columnName.toUpperCase())){
					columns.append("," + columnName);
					if(value == null) values.append(",null");
					else values.append(",'"+value+"'");			
				}						
			});
			String sql = "INSERT INTO " + tableName + "(" + columns.substring(1) + ") VALUES(" + values.substring(1) + ")";
			print("语句：" + sql);
			long startTime=System.currentTimeMillis();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				int ret=ps.executeUpdate();
				print("本次插表完成，消耗时间："+(System.currentTimeMillis()-startTime)+" 毫秒");
				return ret == 0 ? false : true;
			}
		}
	}
	
	/** 
	 * 删除表数据
	 * @param tableName : 表名
	 * @param map : 条件Map<列名,值> ，为null则清空表数据
	 *  */
	public static int delete(String tableName, UFMap map, String dataSource) throws Exception {
		try (Connection conn = getConn(dataSource)) {
			String sql = null;
			if(map == null) {
				sql = "TRUNCATE TABLE " + tableName;
			}else {
				StringBuilder sqlSb = new StringBuilder("DELETE FROM ").append(tableName).append(" WHERE ");
				int[] is = {0};
				map.forEach((columnName, value) -> {
					if(columnName.equals(columnName.toUpperCase())){
						if(is[0] == 0) {
							is[0] = 1;
						}else {
							sqlSb.append(" AND ");
						}			
						if(value==null){
							sqlSb.append(columnName + "IS NULL");
						}else{
							sqlSb.append(columnName + "='" + value + "'");
						}				
					}
				});
				sql = sqlSb.toString();
			}
			print("语句：" + sql);
			long startTime = System.currentTimeMillis();
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				int ret = ps.executeUpdate();
				print("本次共删除" + ret + "条记录，消耗时间：" + (System.currentTimeMillis() - startTime) + " 毫秒");
				return ret;
			}
		}
	}
	
	/** 查询表结构 */
	public static List<UFMap> getColumns(String tableName, String dataSource) throws Exception {
		String sql = "DESC " + tableName;
		return query(sql, dataSource);
	}
	
	/** 查询库列表 */
	public static List<UFMap> getDatabases(String dataSource) throws Exception {
		String sql = "SHOW DATABASES";
		return query(sql, dataSource);
	}
	
	/** 专用打印方法 */
	private static void print(Object obj){
		LogUtil.print(obj, Dao.class);
	}
	
}
