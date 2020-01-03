package wh;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import wh.interfaces.IDBConnection;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.LogicalOperation;
import wh.interfaces.ISqlBuilder.NotSupportDBType;
import wh.interfaces.ISqlBuilder.Operation;
import wh.interfaces.ISqlBuilder.SqlType;

class Recordset extends AbstractRecordset{

	protected String getPrimKeyValue(IRow row) {
		String value = null;
		for (String key : primKeys) {
			if (value == null){
				value = row.getValue(key).toString();
			}else {
				value += row.getValue(key);
			}
		}
		return value;
	}
	
	protected void setInsertValues(ISqlBuilder sqlBuilder, Row row) throws NotSupportDBType{
		for (IColumn iColumn : columns) {
			Object value = null;
			if (iColumn.isBlob()){
				value = sqlBuilder.getBlobInsertPlaceholder();
			}else{
				value = row.getValue(iColumn.getName());
			}
			if (value == null)
				continue;
			
			if (value instanceof String){
				String strValue = (String)value;
				if (strValue.isEmpty() || strValue.trim().toLowerCase().equals("null"))
					continue;
			}
			
			sqlBuilder.addField(iColumn.getName());
			sqlBuilder.addValue(value);
		}
	}
	
	protected void setUpdateValues(ISqlBuilder sqlBuilder, Row row) throws NotSupportDBType{
		for (IColumn iColumn : columns) {
			Object value = null;
			if (iColumn.isBlob()){
				value = sqlBuilder.getBlobInsertPlaceholder();
			}else{
				value = row.getValue(iColumn.getName());
			}
			if (value == null)
				value = null;
			else if (value instanceof String){
				String strValue = (String)value;
				strValue = strValue.trim().toLowerCase();
				if (strValue.isEmpty() || strValue.equals("'null'") || strValue.equals("\"null\""))
					value = null;
			}
			
			sqlBuilder.addSet(iColumn.getName(), value);
		}
	}
	
	protected void addPrimKeyWhere(ISqlBuilder sqlBuilder, Row row) throws NotSupportDBType{
		sqlBuilder.addLogicalOperation(LogicalOperation.otLeftPair);
		for (int i = 0; i < primKeys.size(); i++) {
			String key = primKeys.get(i);
			sqlBuilder.addWhere(key, Operation.otEqual, new Object[]{row.getValue(key)});
			if (i == 0 || i == primKeys.size() - 1){
				continue;
			}else{
				sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
			}
		}
		sqlBuilder.addLogicalOperation(LogicalOperation.otRightPair);
	}
	
	protected void addBlobRow(ISqlBuilder sqlBuilder, Row row, List<IColumn> blobColumns, List<IRow> blobs) throws Exception{
		if (blobColumns.size() > 0){
			Row blobRow = new Row(columns, keyColumns);
			for (IColumn column : blobColumns) {
				for (String key : primKeys) {
					blobRow.setValue(key, row.getValue(key));
				}
				byte[] data = (byte[])row.getValue(column.getName());
				blobRow.setValue(column.getName(), data);
			}
			blobs.add(blobRow);
		}
	}
	
	protected void updateRows(IDBConnection connection, List<ISqlBuilder> sqlBuilders, List<IColumn> blobColumns, List<IRow> blobs) throws Exception{
		for (IRow irow : rows) {
			Row row = (Row)irow;
			SQLBuilder sqlBuilder = new SQLBuilder();
			sqlBuilder.init(connection);
			sqlBuilder.addTable(this.sqlBuilder.getTableName());
			switch (row.getState()) {
			case rsAdd:
				setInsertValues(sqlBuilder, row);
				sqlBuilder.setSqlType(SqlType.stInsert);
				break;
			case rsEdit:
				addPrimKeyWhere(sqlBuilder, row);
				setUpdateValues(sqlBuilder, row);
				sqlBuilder.setSqlType(SqlType.stUpdate);
				break;
			default:
				continue;
			} 
			sqlBuilders.add(sqlBuilder);
			addBlobRow(sqlBuilder, row, blobColumns, blobs);
		}
		
		connection.execute(sqlBuilders);

	}
	
	protected void updateBlobs(IDBConnection connection, List<IColumn> blobColumns, List<IRow> blobs) throws Exception {
		List<String> updateBlobFields = new ArrayList<>();
		for (IColumn column : blobColumns) {
			updateBlobFields.add(column.getName());
		}
		
		String[] fields = updateBlobFields.toArray(new String[updateBlobFields.size()]);
		for (IRow iRow : blobs) {
			Row row = (Row)iRow;
			switch (connection.getDBType()) {
			case dbAccessFile:
			case dbAccessSource:
			case dbMSSQL:
			case dbSqLite:
			case dbSybase:
			case dbOracle:
			case dbDB2:
			case dbMySQL:
			case dbInformix:
			case dbPostSQL:
				SQLBuilder sqlBuilder = new SQLBuilder();
				sqlBuilder.init(connection);
				sqlBuilder.addTable(this.sqlBuilder.getTableName());
				addPrimKeyWhere(sqlBuilder, row);
				
				PreparedStatement pstmt = connection.getPreparedStatement(sqlBuilder.getPreparedUpdateSql(fields));
				for (int i = 0; i < blobColumns.size(); i++) {
					IColumn column = blobColumns.get(i);
					pstmt.setBytes(1, (byte[])row.getValue(column.getName()));
				}
				pstmt.executeQuery();
			default:
				throw new NotSupportDBType();
			}
		}
		
	}

	@Override
	public void post(IDBConnection connection) throws Exception {
		List<ISqlBuilder> sqlBuilders = new ArrayList<ISqlBuilder>();
		for (IRow irow : deleteRows) {
			Row row = (Row)irow;
			SQLBuilder sqlBuilder = new SQLBuilder();
			sqlBuilder.init(connection);
			sqlBuilder.addTable(this.sqlBuilder.getTableName());
			addPrimKeyWhere(sqlBuilder, row);
			sqlBuilder.setSqlType(SqlType.stDelete);
			sqlBuilders.add(sqlBuilder);
		}

		
		List<IColumn> blobColumns = new ArrayList<>();
		List<IRow> blobs = new ArrayList<>();
		for (IColumn column : columns) {
			if (column.isBlob())
				blobColumns.add(column);
		}
		
		updateRows(connection, sqlBuilders, blobColumns, blobs);
		updateBlobs(connection, blobColumns, blobs);
	}

	@Override
	public void createDBTable(IDBConnection connection, String tablename) throws Exception {
		SQLBuilder sqlBuilder = new SQLBuilder();
		sqlBuilder.init(connection);
		sqlBuilder.setSqlType(SqlType.stCreate);
		sqlBuilder.addTable(tablename);
		
		((DBConnection)connection).execute(sqlBuilder, this);
	}

}
