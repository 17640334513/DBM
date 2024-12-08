package action.out;

import action.BaseAction;
import dao.Dao;
import uf.UFMap;

public class AdminAction extends BaseAction {
	
	/** 执行sql语句 */
	static UFMap executeSql(UFMap map) throws Exception {
		String dataSource = map.getT("dataSource");
		String sql = map.getString("sql").trim();
		String SQL = sql.toUpperCase();
		if(SQL.startsWith("SELECT") || SQL.startsWith("SHOW") || SQL.startsWith("DESC")) {
			return right(Dao.query(sql, dataSource));
		}else {
			return right(Dao.executeSql(sql, dataSource));
		}
	}
	
}
