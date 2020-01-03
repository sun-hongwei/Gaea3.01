package wh.interfaces;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public interface IDBConnection {
	public enum DBType{
		dbMySQL, dbMSSQL, dbOracle, dbAccessFile, dbAccessSource, dbSybase, dbPostSQL, dbInformix, dbDB2, dbSqLite
	}
	
	public static class DBConnectionInfo{
		public String serverName;
		public String driver;
		public String uri_Prefix;
		public String host;
		public String port;
		public String name;
		public String user;
		public String pwd;
		public String uri;
		public DBType type;
		
	}
	
	public static class FieldMetaInfo{
		public String name;
		public int type;
		public String typename;
		public int size;
	}

	public static ISqlBuilder getSqlBuilder(IDBConnection db) throws Exception{
		Class<?> sqlBuilderClass = Class.forName("wh.SQLBuilder");
		Constructor<?> constructor = sqlBuilderClass.getDeclaredConstructor();
		constructor.setAccessible(true);
		ISqlBuilder sqlBuilder = (ISqlBuilder)constructor.newInstance();
		Method init = sqlBuilderClass.getDeclaredMethod("init", IDBConnection.class);
		init.setAccessible(true);
		init.invoke(sqlBuilder, db);
		return sqlBuilder;
	}
	
	public DBType getDBType();
	public void close() throws SQLException;
	public void open(DBConnectionInfo connectionInfo) throws Exception;
	public void beginTran();
	public void rollbackTran() throws SQLException;
	public void commitTran() throws SQLException;
	public IDataset query(ISqlBuilder sqlBuilder) throws Exception;
	public IDataset query(String sql, String[] primKeys) throws Exception;
	public List<String> getPrimFields(String tablename);
	public boolean directExecute(String dbcommand) throws Exception;
	public int execute(ISqlBuilder sqlBuilder) throws Exception;
	public void execute(List<ISqlBuilder> sqlBuilders) throws Exception;	
	public Statement getUpdateStatement() throws SQLException;
	public PreparedStatement getPreparedStatement(String sql) throws SQLException;
	public String getCatalog() throws SQLException;
	public String getSchema() throws SQLException;
	public IDataset createDataset();
	public List<FieldMetaInfo> getFieldDefines(String tablename);
	public List<String> getTables();
	public boolean isOpen();
	
	public static DBConnectionInfo getMYSQLConnectionString(String host, String port, String db, String user, String pwd){
		DBConnectionInfo info = new DBConnectionInfo();
		info.driver = "com.mysql.cj.jdbc.Driver";
		info.uri_Prefix = "jdbc:mysql://";
		info.host = host;
		info.port = port;
		info.name = db;
		info.user = user;
		info.pwd = pwd;
		info.type = DBType.dbMySQL;
		info.uri = info.uri_Prefix + info.host + ((info.port == null || info.port.isEmpty()) ? "" : ":" + info.port) + "/" + info.name + "?" +
				"user=" + info.user + "&password=" + info.pwd + "&autoReconnectForPools=true&useUnicode=true&characterEncoding=UTF8&useSSL=false&serverTimezone=Asia/Shanghai";
		return info;
	}

	public static DBConnectionInfo getMSSQLServerConnectionString(String host, String port, String db, String user, String pwd){
		DBConnectionInfo info = new DBConnectionInfo();
		info.driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
		info.uri_Prefix = "jdbc:sqlserver://";
		info.host = host;
		info.port = port;
		info.name = db;
		info.user = user;
		info.pwd = pwd;
		info.type = DBType.dbMSSQL;
		info.uri = info.uri_Prefix + info.host + ((info.port == null || info.port.isEmpty())?"" : ":" + info.port) + ";DatabaseName=" + info.name;
		return info;
	}

	public static DBConnectionInfo getSybaseConnectionString(String host, String port, String db, String user, String pwd){
		DBConnectionInfo info = new DBConnectionInfo();
		info.driver = "com.sybase.jdbc3.jdbc.SybDriver";
		info.uri_Prefix = "jdbc:sybase:Tds:";
		info.host = host;
		info.port = port;
		info.name = db;
		info.user = user;
		info.pwd = pwd;
		info.type = DBType.dbMSSQL;
		info.uri = info.uri_Prefix + info.host + ((info.port == null || info.port.isEmpty())?"" : ":" + info.port) + "/" + info.name;
		return info;
	}

	public static DBConnectionInfo getOracleConnectionString(String host, String port, String db, String user, String pwd){
		DBConnectionInfo info = new DBConnectionInfo();
		info.driver = "oracle.jdbc.driver.OracleDriver";
		info.uri_Prefix = "jdbc:oracle:thin:@";
		info.host = host;
		info.port = port;
		info.name = db;
		info.user = user;
		info.pwd = pwd;
		info.type = DBType.dbOracle;
		info.uri = info.uri_Prefix + info.host + ((info.port == null || info.port.isEmpty())?"" : ":" + info.port) + ":" + info.name;
		return info;
	}

	public static DBConnectionInfo getDB2ConnectionString(String host, String port, String db, String user, String pwd){
		DBConnectionInfo info = new DBConnectionInfo();
		info.driver = "com.ibm.db2.jdbc.app.DB2Driver";
		info.uri_Prefix = "jdbc:db2://";
		info.host = host;
		info.port = port;
		info.name = db;
		info.user = user;
		info.pwd = pwd;
		info.type = DBType.dbDB2;
		info.uri = info.uri_Prefix + info.host + ((info.port == null || info.port.isEmpty())?"" : ":" + info.port) + "/" + info.name;
		return info;
	}

	public static DBConnectionInfo getInformixConnectionString(String host, String port, String serverName, String db, String user, String pwd){
		DBConnectionInfo info = new DBConnectionInfo();
		info.driver = "com.informix.jdbc.IfxDriver";
		info.uri_Prefix = "jdbc:informix-sqli://";
		info.host = host;
		info.port = port;
		info.name = db;
		info.user = user;
		info.pwd = pwd;
		info.serverName = serverName;
		info.type = DBType.dbInformix;
		info.uri = info.uri_Prefix + info.host + ((info.port == null || info.port.isEmpty())?"" : ":" + info.port) + "/" + info.name + ":INFORMIXSERVER=" + info.serverName;
		return info;
	}

	public static DBConnectionInfo getPostgreSQLConnectionString(String host, String db, String user, String pwd){
		DBConnectionInfo info = new DBConnectionInfo();
		info.driver = "org.postgresql.Driver";
		info.uri_Prefix = "jdbc:postgresql://";
		info.host = host;
		info.name = db;
		info.user = user;
		info.pwd = pwd;
		info.type = DBType.dbPostSQL;
		info.uri = info.uri_Prefix + info.host + "/" + info.name;
		return info;
	}

	public static DBConnectionInfo getMSAccessDBFileConnectionString(String dbfile, String user, String pwd){
		DBConnectionInfo info = new DBConnectionInfo();
		info.driver = "com.hxtt.sql.access.AccessDriver";
		info.uri_Prefix = "jdbc:Access:///";
		info.name = dbfile;
		info.user = user;
		info.pwd = pwd;
		info.type = DBType.dbAccessFile;
		info.uri = info.uri_Prefix + info.name;
		return info;
	}

	public static DBConnectionInfo getMSAccessDBODBCConnectionString(String odbcName, String user, String pwd){
		DBConnectionInfo info = new DBConnectionInfo();
		info.driver = "com.hxtt.sql.access.AccessDriver";
		info.uri_Prefix = "jdbc:Access:///";
		info.name = odbcName;
		info.user = user;
		info.pwd = pwd;
		info.type = DBType.dbAccessSource;
		info.uri = info.uri_Prefix + info.name;
		return info;
	}

	public static DBConnectionInfo getSqlLiteConnectionString(String dbfileName, String user, String pwd){
		DBConnectionInfo info = new DBConnectionInfo();
		info.driver = "org.sqlite.JDBC";
		info.uri_Prefix = "jdbc:sqlite://";
		info.name = dbfileName;
		info.user = user;
		info.pwd = pwd;
		info.type = DBType.dbSqLite;
		info.uri = info.uri_Prefix + info.name;
		return info;
	}

	DBConnectionInfo getConnectionInfo();

}
