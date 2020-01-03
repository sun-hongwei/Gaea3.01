package wh;

import java.sql.Types;
import java.util.Date;

import wh.interfaces.IConnectionFactory;
import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IColumn;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.LogicalOperation;
import wh.interfaces.ISqlBuilder.Operation;
import wh.interfaces.ISqlBuilder.SqlType;

public class SampleMain {

	public static void printDataset(IDBConnection db, IDataset dataset){
		for (IRow row : dataset.getRows()) {
			for (IColumn column : dataset.getColumns()) {
				System.out.print(column.getName() + "[" + column.getLabel() + "]:" + row.getValue(column.getName()));
				System.out.print(" ");
			}
			System.out.println("");
		}
		System.out.println("-------------------------------------------------------------------------------------------");
	}

	public static void main(String[] args) {
		try {
//			IDBConnection db = IConnectionFactory.getConnection(IDBConnection.getOracleConnectionString("192.168.1.150", "1521", "sample", "system", "123456"));
//			IDBConnection db = IConnectionFactory.getConnection(IDBConnection.getMSAccessDBFileConnectionString("X:\\demo\\access\\sample.accdb", null, null));
//			IDBConnection db = IConnectionFactory.getConnection(IDBConnection.getMYSQLConnectionString("192.168.1.150", "3306", 
//					"sample?useUnicode=true&characterEncoding=utf-8", "root", "root"));
			IDBConnection db = IConnectionFactory.getConnection(IDBConnection.getMSSQLServerConnectionString("127.0.0.1", "1433", "sample", "sa", "sa@123"));
//			IDBConnection db = IConnectionFactory.getConnection(IDBConnection.getSqlLiteConnectionString("X:\\demo\\sqlite\\test.db", null, null));
			
			IDataset dataset = db.createDataset();
			IColumn column = dataset.newColumn("id", "id", Types.INTEGER, 0, 0);
			dataset.addColumn(column);
			column = dataset.newColumn("name", "name", Types.VARCHAR, 255, 0);
			dataset.addColumn(column);
			column = dataset.newColumn("dd", "dd", Types.DATE, 0, 0);
			dataset.addColumn(column);
			column = dataset.newColumn("tt", "tt", Types.TIME, 0, 0);
			dataset.addColumn(column);
			column = dataset.newColumn("je", "je", Types.NUMERIC, 10, 2);
			dataset.addColumn(column);
			column = dataset.newColumn("memo", "memo", Types.BLOB, 0, 0);
			dataset.addColumn(column);
			
			dataset.addPrimKey("id");
			dataset.addPrimKey("name");
			
			dataset.createDBTable(db, "sample2");
			
			ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
			sqlBuilder.addField("id");
			sqlBuilder.addField("name");
			sqlBuilder.addField("descrp");
			sqlBuilder.addTable("sample");
			sqlBuilder.setSqlType(SqlType.stQuery);
			dataset = db.query(sqlBuilder);
			
			//edit
			for (int i = 0; i < dataset.getRowCount(); i++) {
				IRow row = dataset.getRow(i);
				row.setValue("descrp", new Date());
			}
			
			sqlBuilder.addWhere("id", Operation.otBetween, new Object[]{5, 7});
			sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
			sqlBuilder.addWhere("name", Operation.otEqual, new Object[]{"name6"});
			IDataset selectIRecordset = db.query(sqlBuilder);
			printDataset(db, selectIRecordset);

			//insert
			for (int i = 0; i < 100; i++) {
				IRow newRow = dataset.newRow();
				newRow.setValue("id", i);
				newRow.setValue("name", "name" + newRow.getValue("id"));
				newRow.setValue("descrp", "����һ�����ԣ�" + newRow.getValue("id") + "," + new Date().getTime());
				dataset.addRow(newRow);
			}
			//delete bbbbbb row
//			HashMap<String, Object> primKeyValues = new HashMap<>();
//			primKeyValues.put("id", 1);
//			int delRow = dataset.indexOfRow(primKeyValues);
//			dataset.removeRow(delRow);
			
			sqlBuilder.addPageOrderField("id");
			sqlBuilder.setPagePos(0);
			sqlBuilder.setPageSize(2);
			sqlBuilder.setRawWhere(new StringBuilder());
			sqlBuilder.addWhere("id", Operation.otBetween, new Object[]{5, 100});
			sqlBuilder.setSqlType(SqlType.stPageQuery);
			selectIRecordset = db.query(sqlBuilder);
			printDataset(db, selectIRecordset);


			db.beginTran();
			try{
				//delete
				ISqlBuilder delBuilder = IDBConnection.getSqlBuilder(db);
				delBuilder.addTable(sqlBuilder.getTableName());
				delBuilder.setSqlType(SqlType.stDelete);
				db.execute(delBuilder);

				dataset.post(db);
				db.commitTran();
			}catch(Exception e){
				db.rollbackTran();
			}
			
			sqlBuilder.setPagePos(1);
			sqlBuilder.setPageSize(2);
			selectIRecordset = db.query(sqlBuilder);
			printDataset(db, selectIRecordset);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
