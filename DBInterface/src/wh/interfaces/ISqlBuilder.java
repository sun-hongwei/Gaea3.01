package wh.interfaces;

import java.sql.SQLException;

public interface ISqlBuilder {
	public static class NotSupportDBType extends Exception{
		private static final long serialVersionUID = 1L;}

	public enum Operation{
		otEqual, otUnequal, otGreater, otLess, otGreaterAndEqual, otLessAndEqual, otBetween, otLike, otIn, otNotIn
	}

	public enum LogicalOperation{
		otOr, otAnd, otNot, otLeftPair, otRightPair
	}

	public enum SqlType{
		stQuery, stDelete, stInsert, stPageQuery, stUpdate, stCreate, stDrop
	}
	
	public abstract String getLikeSymbolString() throws NotSupportDBType;

	public abstract String getUnEqualSymbolString() throws NotSupportDBType;

	public abstract String getFieldValue(Object value) throws NotSupportDBType;

	public abstract void addLogicalOperation(LogicalOperation logicalOperation);

	public abstract void addTable(String tablename);

	public abstract void addWhere(String field, Operation operation,
			Object[] values) throws NotSupportDBType;

	public abstract void addPageOrderField(String field) throws NotSupportDBType;

	public abstract void addGroup(String field) throws NotSupportDBType;

	public abstract void addSort(String field) throws NotSupportDBType;

	public abstract void addHaving(String field) throws NotSupportDBType;

	public abstract void addSet(String field, Object value)
			throws NotSupportDBType;

	public abstract void addValue(Object value) throws NotSupportDBType;

	public abstract void setSqlType(SqlType sqlType);

	public abstract String getSql() throws Exception;
	public abstract String getPreparedUpdateSql(String[] fields) throws Exception;

	public abstract boolean hasWhere();
	
	public abstract String getWhere();

	public abstract String[] getGroup();

	public abstract String[] getSort();

	public abstract String getHaving();

	public abstract String[] getValues();

	public abstract String[] getFields();

	public abstract String getSets();

	public abstract String[] getPageOrders();

	public abstract String getCatalog() throws SQLException;

	public abstract String getSchema() throws SQLException;

	public abstract String getTableName();

	public abstract StringBuilder getRawWhere();

	public abstract StringBuilder getRawHaving();

	public abstract StringBuilder getRawGroup();

	public abstract StringBuilder getRawOrder();

	public abstract StringBuilder getRawSet();

	public abstract StringBuilder getRawValues();

	public abstract int getPagePos();

	public abstract int getPageSize();

	public abstract void setRawWhere(StringBuilder value);

	public abstract void setRawHaving(StringBuilder value);

	public abstract void setRawGroup(StringBuilder value);

	public abstract void setRawOrder(StringBuilder value);

	public abstract void setRawSet(StringBuilder value);

	public abstract void setRawValues(StringBuilder value);

	public abstract void setPagePos(int value);

	public abstract void setPageSize(int value);
	
	public abstract void setRawFields(String[] value);
	
	public abstract void addField(String fieldname);

	public String getBlobInsertPlaceholder() throws NotSupportDBType;
	
}