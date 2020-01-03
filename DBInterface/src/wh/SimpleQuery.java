package wh;

import java.util.ArrayList;
import java.util.List;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.LogicalOperation;
import wh.interfaces.ISqlBuilder.Operation;
import wh.interfaces.ISqlBuilder.SqlType;

public class SimpleQuery {

	public interface IWhere {
	}

	public static class Where implements IWhere {
		public String field;
		public Operation operation = Operation.otBetween;
		public Object[] value;

		public Where(String field, Operation operation, Object value) {
			this(field, operation, new Object[] { value });
		}

		public Where(String field, Operation operation, Object[] value) {
			this.field = field;
			this.operation = operation;
			this.value = value;
		}
	}

	public static class Logical implements IWhere {
		public LogicalOperation logicalOperation;

		public Logical(LogicalOperation logicalOperation) {
			this.logicalOperation = logicalOperation;
		}
	}

	public static IDataset query(IDBConnection db, String table) throws Exception {
		return query(db, table, (Where) null);
	}

	public static IDataset query(IDBConnection db, String table, Where where) throws Exception {
		return query(db, table, where == null ? null : new Where[] { where });
	}

	public static IDataset query(IDBConnection db, String table, Where where, String[] fields) throws Exception {
		return query(db, table, new Where[] { where }, fields);
	}

	public static IDataset query(IDBConnection db, String table, IWhere[] wheres) throws Exception {
		return query(db, table, wheres, new String[] { "*" });
	}

	public static IDataset query(IDBConnection db, String table, IWhere[] wheres, String[] fields) throws Exception {
		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		if (wheres != null) {
			boolean priorWhere = false;
			for (IWhere obj : wheres) {
				if (obj instanceof Where) {
					if (priorWhere) {
						sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);						
					}
					Where where = (Where) obj;
					sqlBuilder.addWhere(where.field, where.operation, where.value);
					priorWhere = true;
				} else if (obj instanceof Logical) {
					Logical locaLogicalOperation = (Logical) obj;
					sqlBuilder.addLogicalOperation(locaLogicalOperation.logicalOperation);
				}
			}
		}
		sqlBuilder.addTable(table);
		sqlBuilder.setRawFields(fields);

		return db.query(sqlBuilder);
	}

	public static void remove(IDBConnection db, String table, IWhere where) throws Exception {
		remove(db, table, new IWhere[] { where });
	}

	public static void remove(IDBConnection db, String table, IWhere[] wheres) throws Exception {
		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.setSqlType(SqlType.stDelete);

		if (wheres != null) {
			for (IWhere obj : wheres) {
				if (obj instanceof Where) {
					Where where = (Where) obj;
					sqlBuilder.addWhere(where.field, where.operation, where.value);
				} else if (obj instanceof Logical) {
					Logical locaLogicalOperation = (Logical) obj;
					sqlBuilder.addLogicalOperation(locaLogicalOperation.logicalOperation);
				}
			}
		}

		sqlBuilder.addTable(table);
		db.execute(sqlBuilder);
	}

	public static void insert(IDBConnection db, String table, String[] fields, Object[] values) throws Exception {
		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.setSqlType(SqlType.stInsert);

		for (int i = 0; i < values.length; i++) {
			sqlBuilder.addField(fields[i]);
			sqlBuilder.addValue(values[i]);
		}

		sqlBuilder.addTable(table);
		db.execute(sqlBuilder);
	}

	public static void update(IDBConnection db, String table, String[] fields, Object[] values) throws Exception {
		List<IWhere> wheres = new ArrayList<>();
		for (int i = 0; i < values.length; i++) {
			if (wheres.size() > 0) {
				wheres.add(new Logical(LogicalOperation.otAnd));
			}
			wheres.add(new Where(fields[i], Operation.otEqual, new Object[] {values[i]}));
		}
		
		IDataset dataset = query(db, table, wheres.toArray(new IWhere[wheres.size()]), new String[] {"count(*)"});
		if ((int)dataset.getRow(0).getValue(0) == 0) {
			insert(db, table, fields, values);
		}else {
			ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
			sqlBuilder.setSqlType(SqlType.stUpdate);
			sqlBuilder.addTable(table);

			for (int i = 0; i < values.length; i++) {
				sqlBuilder.addSet(fields[i], values[i]);
			}

			db.execute(sqlBuilder);
		}
	}

}
