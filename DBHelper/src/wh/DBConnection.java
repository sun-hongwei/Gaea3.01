package wh;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.ISqlBuilder;

class DBConnection implements IDBConnection {
	Connection connection;

	DBType type;
	DBConnectionInfo connectionInfo;

	public DBType getDBType() {
		return type;
	}

	public void close() throws SQLException {
		if (connection == null)
			return;

		if (!connection.isClosed()) {
			connection.close();
		}
		connection = null;
	}

	@Override
	public DBConnectionInfo getConnectionInfo() {
		return connectionInfo;
	}
	
	public void open(DBConnectionInfo connectionInfo) throws Exception {
		Class.forName(connectionInfo.driver).newInstance();
		close();
		if (connectionInfo.user == null) {
			connection = DriverManager.getConnection(connectionInfo.uri);
		} else
			connection = DriverManager.getConnection(connectionInfo.uri, connectionInfo.user, connectionInfo.pwd);
		type = connectionInfo.type;
		connection.setAutoCommit(true);
		this.connectionInfo = connectionInfo;
	}

	public void execute(List<ISqlBuilder> sqlBuilders) throws Exception {
		for (int i = 0; i < sqlBuilders.size(); i++) {
			execute(sqlBuilders.get(i));
		}
	}

	public boolean inTran() throws SQLException {
		return connection.getAutoCommit() == false;
	}

	public List<FieldMetaInfo> getFieldDefines(String tablename) {
		List<FieldMetaInfo> fielddefines = new ArrayList<>();
		try {
			ResultSet fields = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(),
					tablename, null);
			while (fields.next()) {
				FieldMetaInfo info = new FieldMetaInfo();
				info.name = fields.getString("COLUMN_NAME");
				info.type = fields.getInt("DATA_TYPE");
				info.typename = fields.getString("TYPE_NAME");
				info.size = fields.getInt("COLUMN_SIZE");
				fielddefines.add(info);
			}
			return fielddefines;
		} catch (SQLException e) {
			checkConnectionExecption(e);
			e.printStackTrace();
			return fielddefines;
		}
	}

	public List<String> getTables() {
		List<String> tables = new ArrayList<>();
		try {
			ResultSet tableSchems = connection.getMetaData().getTables(connection.getCatalog(), connection.getSchema(),
					null, new String[] { "TABLE" });
			while (tableSchems.next()) {
				String name = tableSchems.getString("TABLE_NAME");
				tables.add(name);
			}
			return tables;
		} catch (SQLException e) {
			checkConnectionExecption(e);
			e.printStackTrace();
			return tables;
		}
	}

	public void alertDBField(FieldMetaInfo field) {

	}

	public boolean directExecute(String dbcommand) throws Exception {
		if (inTran()) {
			return false;
		}

		Statement statement = null;
		try {
			if (connection == null)
				throw new NullPointerException("connection is null");

			statement = connection.createStatement();
			return statement.execute(dbcommand);

		} catch (Exception e) {
			checkConnectionExecption(e);
			e.printStackTrace();
			return false;
		} finally {
			if (statement != null)
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}
	}

	public void checkConnectionExecption(Exception e) {
		if (e instanceof SQLNonTransientConnectionException) {
			try {
				open(connectionInfo);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public IDataset query(ISqlBuilder sqlBuilder) throws Exception {
		String tablename = sqlBuilder.getTableName();
		List<String> primKeys = getPrimFields(tablename);
		String sql;
		try {
			sql = sqlBuilder.getSql();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		IDataset dataset = query(sql, primKeys.toArray(new String[primKeys.size()]));

		if (dataset != null)
			((AbstractRecordset) dataset).setSqlBuilder(sqlBuilder);

		return dataset;
	}

	public IDataset query(String sql, String[] primKeys) throws Exception {
		return query(sql, primKeys, connection.getAutoCommit());
	}

	public IDataset query(String sql, String[] primKeys, boolean autoCommit) throws Exception {
		Statement statement = null;
		try {
			if (connection == null)
				throw new NullPointerException("connection is null");

			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			if (rs == null)
				return null;

//			if (connection instanceof OracleConnection){
//				tablename = tablename.toUpperCase();
//			}
//
			Recordset recordset = new Recordset();
			recordset.init(primKeys, rs);
			((AbstractRecordset) recordset).setSQL(sql);
			rs.close();

			return recordset;

		} catch (Exception e) {
			checkConnectionExecption(e);
			e.printStackTrace();
			throw e;
		} finally {
			if (statement != null)
				try {
					statement.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}

	}

	public List<String> getPrimFields(String tablename) {
		try {
			if (connection == null)
				throw new NullPointerException("connection is null");

			List<String> primKeys = new ArrayList<String>();
			ResultSet table = connection.getMetaData().getPrimaryKeys(connection.getCatalog(), connection.getSchema(),
					tablename);
			while (table.next()) {
				primKeys.add(table.getString("COLUMN_NAME"));
			}
			table.close();
			return primKeys;
		} catch (Exception e) {
			checkConnectionExecption(e);
			e.printStackTrace();
			return null;
		}

	}

	public int execute(ISqlBuilder sqlBuilder) throws Exception {
		try {
			if (connection == null)
				throw new NullPointerException("connection is null");

			String sql = sqlBuilder.getSql();
			Statement statement = connection.createStatement();
			int rs = statement.executeUpdate(sql);
			return rs;
		} catch (Exception e) {
			checkConnectionExecption(e);
			e.printStackTrace();
			throw e;
		}

	}

	protected int execute(ISqlBuilder sqlBuilder, IDataset defines) throws Exception {
		try {
			if (connection == null)
				throw new NullPointerException("connection is null");

			String sql = ((SQLBuilder) sqlBuilder).getSql(defines);
			Statement statement = connection.createStatement();
			int rs = statement.executeUpdate(sql);
			return rs;
		} catch (Exception e) {
			checkConnectionExecption(e);
			e.printStackTrace();
			throw e;
		}

	}

	@Override
	public void beginTran() {
		try {
			DatabaseMetaData dmd = connection.getMetaData();
			if (dmd.supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE))
				connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			else if (dmd.supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ))
				connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			else if (dmd.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED))
				connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			else if (dmd.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED))
				connection.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			else {
				return;
			}

			connection.setAutoCommit(false);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void rollbackTran() throws SQLException {
		if (connection.getAutoCommit())
			return;
		
		connection.rollback();
		connection.setAutoCommit(true);
	}

	@Override
	public void commitTran() throws SQLException {
		if (connection.getAutoCommit())
			return;
		
		connection.commit();
		connection.setAutoCommit(true);
	}

	@Override
	public Statement getUpdateStatement() throws SQLException {
		return connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}

	@Override
	public PreparedStatement getPreparedStatement(String sql) throws SQLException {
		return connection.prepareStatement(sql);
	}

	@Override
	public String getCatalog() throws SQLException {
		return connection.getCatalog();
	}

	@Override
	public String getSchema() throws SQLException {
		return connection.getSchema();
	}

	@Override
	public IDataset createDataset() {
		Recordset recordset = new Recordset();
		return recordset;
	}

	@Override
	public boolean isOpen() {
		try {
			return connection != null && connection.isValid(1) && !connection.isClosed();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

}
