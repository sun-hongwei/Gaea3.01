package wh;

import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IColumn;
import wh.interfaces.ISqlBuilder;

class SQLBuilder implements ISqlBuilder{
	SqlType sqlType = SqlType.stQuery;
	
	String tablename = null;
	
	StringBuilder where = new StringBuilder();
	StringBuilder having = new StringBuilder();
	StringBuilder group = new StringBuilder();
	StringBuilder order = new StringBuilder();
	StringBuilder set = new StringBuilder();
	StringBuilder values = new StringBuilder();
	
	List<String> pageOderKeys = new ArrayList<String>();
	int pagePos = -1;
	int pageSize = -1;
	
	List<String> tables = new ArrayList<>();
	List<String> fields = new ArrayList<>();
	
    String sql;
	IDBConnection db;
	protected void init(IDBConnection db){
		this.db = db;
	}

	protected String formatDate(Date dt, String format){
		SimpleDateFormat df = new SimpleDateFormat(format);
		return df.format(dt);
	}
	
	protected String getPageOrderString() throws Exception{
		if (pageOderKeys.size() == 0)
			throw new Exception("未提供有效的分页关键字！");
		
		String tmp = null;
		for (String order : pageOderKeys) {
			if (tmp == null)
				tmp = order;
			else
				tmp += "," + order;
		}
		
		return tmp;
	}
	
	protected String getTableString() throws Exception{
		if (tables.size() == 0)
			throw new Exception("未提供有效的表名！");
		
		String tmp = null;
		for (String table : tables) {
			if (tmp == null)
				tmp = table;
			else
				tmp += "," + table;
		}
		
		return tmp;
	}
	
	protected String getFieldsString() throws Exception{
		if (fields.size() == 0)
			return "*";
		
		String tmp = null;
		for (String field : fields) {
			if (tmp == null)
				tmp = field;
			else
				tmp += "," + field;
		}
		
		return tmp;
	}
	
	protected void addWhereString() {
		if (where.length() > 0)
			sql = sql + " where " + where.toString();
	}
	
	protected void addHavingString() {
		if (having.length() > 0)
			sql = sql + " having " + having.toString();
	}
	
	protected void addGrouptString() {
		if (group.length() > 0)
			sql =  sql + " group by " + group.toString();
	}
	
	protected void addSortString() {
		if (order.length() > 0)
			sql =  sql + " order by " + order.toString();
	}
	
	protected void addSetString() {
		if (set.length() > 0)
			sql =  sql + " set " + set.toString();
	}
	
	protected String getDateExpr(Date dt) throws NotSupportDBType{
		switch (db.getDBType()) {
		case dbAccessFile:
		case dbAccessSource:
			return "#" + formatDate(dt, "yyyy-MM-dd HH:mm:ss") + "#";
		case dbMSSQL:
		case dbSqLite:
		case dbSybase:
		case dbMySQL:
		case dbInformix:
		case dbPostSQL:
			return "'" + formatDate(dt, "yyyy-MM-dd HH:mm:ss") + "'";
		case dbOracle:
			return "to_date('" + formatDate(dt, "yyyy-MM-dd HH:mm:ss") + "','yyyy-mm-dd hh24:mi:ss')";
		case dbDB2:
			return "'" + formatDate(dt, "yyyy-MM-dd HH.mm.ss") + "'";
		default:
			throw new NotSupportDBType();
		}
	}
	
	protected String getSplitString() throws NotSupportDBType{
		switch (db.getDBType()) {
		case dbAccessFile:
		case dbAccessSource:
			return "\"";
		case dbMSSQL:
		case dbSqLite:
		case dbSybase:
		case dbOracle:
		case dbDB2:
		case dbMySQL:
		case dbInformix:
		case dbPostSQL:
			return "'";
		default:
			throw new NotSupportDBType();
		}
	}
	
	protected void buildQueryPageSql() throws Exception{
		switch (db.getDBType()) {
		case dbAccessFile:
		case dbAccessSource:
		case dbPostSQL:
			sql = "select " + getFieldsString() + " from " + getTableString();
			addWhereString();
			addGrouptString();
			addHavingString();
			addSortString();
			break;
		case dbSqLite:
			sql = "select " + getFieldsString() + " from " + getTableString();
			addWhereString();
			addGrouptString();
			addHavingString();
			addSortString();
			sql += " limit " + String.valueOf(pageSize) + " offset " + String.valueOf(pagePos * pageSize);
			break;
		case dbMySQL:
			sql = "select " + getFieldsString() + " from " + getTableString();
			addWhereString();
			addGrouptString();
			addHavingString();
			addSortString();
			sql += " limit " + String.valueOf(pagePos * pageSize) + "," + String.valueOf(pageSize);
			break;
		case dbDB2: 
		case dbMSSQL:
		case dbSybase:
			sql = "select ROW_NUMBER() over(order by " + getPageOrderString() + ") as 'rowNumber', " + getFieldsString() + " from " + getTableString();
			addWhereString();
			addGrouptString();
			addHavingString();
			addSortString();
			sql = "select * from (" + sql + ") as temp where rowNumber between " + String.valueOf(pagePos * pageSize + 1) + " and " + String.valueOf((pagePos + 1) * pageSize);
			break;
		case dbInformix:
			sql = "select SKIP " + String.valueOf(pagePos * pageSize) + " FIRST " + String.valueOf(pageSize) + " " + getFieldsString() + " from " + getTableString();
			addWhereString();
			addGrouptString();
			addHavingString();
			addSortString();
			break;
		case dbOracle:
			sql = "select ROWNUM AS rowNumber, " + getFieldsString() + " from " + getTableString();
			addWhereString();
			addGrouptString();
			addHavingString();
			addSortString();
			sql = "select * from (" + sql + ") temp where temp.rowNumber between " + String.valueOf(pagePos * pageSize + 1) + 
					" and " + String.valueOf((pagePos + 1) * pageSize);
			break;
		default:
			throw new NotSupportDBType();
		}
	} 
	
	protected void buildQueryCountSql() throws Exception{
		switch (db.getDBType()) {
		case dbAccessFile:
		case dbAccessSource:
		case dbPostSQL:
		case dbSqLite:
		case dbMySQL:
		case dbDB2:
		case dbMSSQL:
		case dbSybase:
		case dbInformix:
		case dbOracle:
			sql = "select count(*) from " + getTableString();
			addWhereString();
			addGrouptString();
			addHavingString();
			addSortString();
			break;
		default:
			throw new NotSupportDBType();
		}
	} 
	
	protected void buildQuerySql() throws Exception{
		switch (db.getDBType()) {
		case dbAccessFile:
		case dbAccessSource:
		case dbPostSQL:
		case dbSqLite:
		case dbMySQL:
		case dbDB2:
		case dbMSSQL:
		case dbSybase:
		case dbInformix:
		case dbOracle:
			sql = "select " + getFieldsString() + " from " + getTableString();
			addWhereString();
			addGrouptString();
			addHavingString();
			addSortString();
			break;
		default:
			throw new NotSupportDBType();
		}
	} 
	
	protected void buildUpdateSql() throws Exception{
		switch (db.getDBType()) {
		case dbAccessFile:
		case dbAccessSource:
		case dbPostSQL:
		case dbSqLite:
		case dbMySQL:
		case dbDB2:
		case dbMSSQL:
		case dbSybase:
		case dbInformix:
		case dbOracle:
			sql = "update " + tables.get(0);
			addSetString();
			addWhereString();
			break;
		default:
			throw new NotSupportDBType();
		}
	} 
	
	protected void buildInsertSql() throws Exception{
		switch (db.getDBType()) {
		case dbAccessFile:
		case dbAccessSource:
		case dbPostSQL:
		case dbSqLite:
		case dbMySQL:
		case dbDB2:
		case dbMSSQL:
		case dbSybase:
		case dbInformix:
		case dbOracle:
			String fields = getFieldsString();
			fields = fields.compareToIgnoreCase("*") == 0 ? "" : "(" + fields + ")";
			sql = "insert into " + tables.get(0) + fields + " values(" + values + ")";
			addSetString();
			addWhereString();
			break;
		default:
			throw new NotSupportDBType();
		}
	} 
	
	protected void buildDeleteSql() throws Exception{
		switch (db.getDBType()) {
		case dbAccessFile:
		case dbAccessSource:
		case dbPostSQL:
		case dbSqLite:
		case dbMySQL:
		case dbDB2:
		case dbMSSQL:
		case dbSybase:
		case dbInformix:
		case dbOracle:
			sql = "delete from " + tables.get(0);
			addWhereString();
			break;
		default:
			throw new NotSupportDBType();
		}
	} 

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getLikeSymbolString()
	 */
	@Override
	public String getLikeSymbolString() throws NotSupportDBType{
		switch (db.getDBType()) {
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
			return "%";
		default:
			throw new NotSupportDBType();
		}
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getUnEqualSymbolString()
	 */
	@Override
	public String getUnEqualSymbolString() throws NotSupportDBType{
		switch (db.getDBType()) {
		case dbAccessFile:
		case dbAccessSource:
		case dbMySQL:
		case dbMSSQL:
		case dbSqLite:
		case dbSybase:
		case dbOracle:
		case dbDB2:
		case dbInformix:
		case dbPostSQL:
			return "<>";
		default:
			throw new NotSupportDBType();
		}
	}
	
	public String formatDBString(String value){
		String formatValue = StringEscapeUtils.escapeJava(StringEscapeUtils.escapeJava(value));
		return formatValue;
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getFieldValue(java.lang.Object)
	 */
	@Override
	public String getFieldValue(Object value) throws NotSupportDBType{
		if (value == null)
			return "null";
		
		if (value instanceof String){
			switch (db.getDBType()) {
			case dbAccessFile:
			case dbAccessSource:
				return "'" + (String)value + "'";
			case dbMySQL:
				return "'" + formatDBString((String)value) + "'";
			default:
				return "'" + (String)value + "'";
			}
		}else if (value instanceof Date){
			return getDateExpr((Date)value);
		}else if (value instanceof Boolean){
			switch (db.getDBType()) {
			case dbMySQL:
			case dbSqLite:
			case dbMSSQL:
			case dbAccessFile:
			case dbAccessSource:
			case dbSybase:
			case dbOracle:
			case dbDB2:
			case dbInformix:
			case dbPostSQL:
				return ((Boolean)value)?"1":"0";
			default:
				throw new NotSupportDBType();
			}
		}
		
		return value == null ? "''" : value.toString();
	}
	
	@Override
	public void addLogicalOperation(LogicalOperation logicalOperation){
		switch (logicalOperation) {
		case otAnd:
			where.append(" and ");
			break;
		case otOr:
			where.append(" or ");
			break;
		case otNot:
			where.append(" not ");
			break;
		case otLeftPair:
			where.append(" ( ");
			break;
		case otRightPair:
			where.append(" ) ");
			break;
		default:
			break;
		}
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#addTable(java.lang.String)
	 */
	@Override
	public void addTable(String tablename){
		tables.add(tablename);
	}
	
	@Override
	public void addField(String fieldname){
		fields.add(fieldname);
	}
	
	@Override
	public void addWhere(String field, Operation operation, Object[] values) throws NotSupportDBType{
		switch (operation) {
		case otNotIn:
		case otIn:
			String vs = null;
			for (Object object : values) {
				if (vs == null)
					vs = getFieldValue(object);
				else{
					vs += "," + getFieldValue(object);
				}
			}
			
			String key = "in";
			if (operation == Operation.otNotIn)
				key = "not in";
			where.append(" " + field + " " + key + " (" + vs + ")");
			break;
		case otEqual:
			where.append(" " + field + " = " + getFieldValue(values[0]));
			break;
		case otGreater:
			where.append(" " + field + " > " + getFieldValue(values[0]));
			break;
		case otLess:
			where.append(" " + field + " < " + getFieldValue(values[0]));
			break;
		case otGreaterAndEqual:
			where.append(" " + field + " >= " + getFieldValue(values[0]));
			break;
		case otLessAndEqual:
			where.append(" " + field + " <= " + getFieldValue(values[0]));
			break;
		case otBetween:
			where.append(" " + field + " between " + getFieldValue(values[0]) + " and " + getFieldValue(values[1]));
			break;
		case otLike:
			where.append(" " + field + " like " + getSplitString() + getFieldValue(values[0]) + getSplitString());
			break;
		case otUnequal:
			where.append(" " + field + " " + getUnEqualSymbolString() + " " + getFieldValue(values[0]));
			break;
		default:
			throw new NotSupportDBType();
		}
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#addGroup(java.lang.String)
	 */
	@Override
	public void addGroup(String field) throws NotSupportDBType{
		if (group.length() == 0)
			group.append(field);
		else
			group.append("," + field);
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#addSort(java.lang.String)
	 */
	@Override
	public void addSort(String field) throws NotSupportDBType{
		if (order.length() == 0)
			order.append(field);
		else
			order.append("," + field);
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#addHaving(java.lang.String)
	 */
	@Override
	public void addHaving(String field) throws NotSupportDBType{
		if (having.length() == 0)
			having.append(field);
		else
			having.append("," + field);
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#addSet(java.lang.String, java.lang.Object)
	 */
	@Override
	public void addSet(String field, Object value) throws NotSupportDBType{
		if (set.length() == 0)
			set.append(" " + field + " = " + getFieldValue(value));
		else
			set.append(" , " + field + " = " + getFieldValue(value));
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#addValue(java.lang.Object)
	 */
	@Override
	public void addValue(Object value) throws NotSupportDBType{
		if (value == null){
			value = "null";
		}
		
		if (values.length() == 0)
			values.append(getFieldValue(value));
		else
			values.append("," + getFieldValue(value));
	}

	@Override
	public void setSqlType(SqlType sqlType) {
		this.sqlType = sqlType;
	}
	
	protected String getSql(IDataset dataset) throws Exception {
		String sql = getSql();
		if (sql == null){
			buildCreateSql(dataset);
		}
		return this.sql;
	}
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getSql()
	 */
	@Override
	public String getSql() throws Exception{
		switch (sqlType) {
		case stQuery:
			buildQuerySql();
			return sql;
		case stDelete:
			buildDeleteSql();
			return sql;
		case stInsert:
			buildInsertSql();
			return sql;
		case stPageQuery:
			buildQueryPageSql();
			return sql;
		case stUpdate:
			buildUpdateSql();
			return sql;
		case stDrop:
			buildDropSql();
			return sql;
		case stCreate:
			return null;
		default:
			throw new NotSupportDBType();
		}
	}

	private void buildDropSql() throws NotSupportDBType {
		switch (db.getDBType()) {
		case dbAccessFile:
		case dbAccessSource:
		case dbPostSQL:
		case dbSqLite:
		case dbMySQL:
		case dbDB2: 
		case dbMSSQL:
		case dbSybase:
		case dbInformix:
		case dbOracle:
			sql = "drop table " + getTableName();
			break;
		default:
			throw new NotSupportDBType();
		}
	}

	abstract class DBDefine{
		protected abstract String getFieldDefine(IColumn column) throws NotSupportDBType;
		
		protected void buildCreateSql(IDataset dataset) throws NotSupportDBType{
			String tablename = getTableName();
			sql = "create table " + tablename + "(";
			String fieldDefs = null;
			for (IColumn column : dataset.getColumns()) {
				String fieldDef = getFieldDefine(column);
				
				if (fieldDefs == null)
					fieldDefs = fieldDef;
				else{
					fieldDefs += "," + fieldDef;
				}
			}
			
			String primKeyDefs = null;
			for (String primKey : dataset.getPrimkeys()) {
				if (primKeyDefs == null)
					primKeyDefs = primKey;
				else {
					primKeyDefs += " , " + primKey;
				}
			}
			
			sql += fieldDefs + ",PRIMARY KEY (" + primKeyDefs + "))";
		}
		
		protected String nullDef(IColumn column){
			String nullDef = !column.allowNull() ? "" : " not null ";
			return nullDef;
		}
	}
	
	class AccesDefine extends DBDefine{
		protected String getFieldDefine(IColumn column) throws NotSupportDBType{
			String fieldName = "["+column.getName()+"]";
			switch (column.getType()) {
			case Types.ARRAY:
				return fieldName + " memo " + nullDef(column);
			case Types.TINYINT:
				return fieldName + " long " + nullDef(column);			
			case Types.SMALLINT:
				return fieldName + " long " + nullDef(column);
			case Types.INTEGER:
				return fieldName + " long " + nullDef(column);
			case Types.BIGINT:
				return fieldName + " long " + nullDef(column);
			case Types.BIT:
				return fieldName + " Bit " + nullDef(column);
			case Types.BOOLEAN:
				return fieldName + " Bit " + nullDef(column);
			case Types.FLOAT:
				return fieldName + " Float " + nullDef(column);
			case Types.REAL:
				return fieldName + " Real " + nullDef(column);
			case Types.DOUBLE:
				return fieldName + " Real " + nullDef(column);
			case Types.NUMERIC:
				return fieldName + " Money " + nullDef(column);
			case Types.DECIMAL:
				return fieldName + " Decimal " + nullDef(column);
			case Types.CHAR:
				return fieldName + " Text(" + column.getSize() + ") " + nullDef(column);
			case Types.VARCHAR:
				return fieldName + " Text(" + column.getSize() + ") " + nullDef(column);
			case Types.LONGVARCHAR:
				return fieldName + " memo " + nullDef(column);
			case Types.DATE:
				return fieldName + " DateTime " + nullDef(column);
			case Types.TIME:
				return fieldName + " DateTime " + nullDef(column);
			case Types.TIMESTAMP:
				return fieldName + " text(255) " + nullDef(column);
			case Types.BINARY:
				return fieldName + " memo " + nullDef(column);
			case Types.LONGVARBINARY:
				return fieldName + " memo " + nullDef(column);
			case Types.VARBINARY:
				return fieldName + " memo " + nullDef(column);
			case Types.BLOB:
				return fieldName + " memo " + nullDef(column);
			case Types.CLOB:
				return fieldName + " memo " + nullDef(column);
			case Types.ROWID:
				return fieldName + " Text(" + column.getSize() + ") " + nullDef(column);
			case Types.NCHAR:
				return fieldName + " Text(" + column.getSize() + ") " + nullDef(column);
			case Types.NVARCHAR:
				return fieldName + " Text(" + column.getSize() + ") " + nullDef(column);
			case Types.LONGNVARCHAR:
				return fieldName + " Text(" + column.getSize() + ") " + nullDef(column);
			case Types.NCLOB:
				return fieldName + " memo " + nullDef(column);
			default:
				throw new NotSupportDBType();
			}
		}
		
	}
	
	class SqliteDefine extends DBDefine{ 
		protected String getFieldDefine(IColumn column) throws NotSupportDBType{
			switch (column.getType()) {
			case Types.ARRAY:
				return column.getName() + " BLOB " + nullDef(column);
			case Types.TINYINT:
				return column.getName() + " INTEGER " + nullDef(column);
			case Types.SMALLINT:
				return column.getName() + " INTEGER " + nullDef(column);
			case Types.INTEGER:
				return column.getName() + " INTEGER " + nullDef(column);
			case Types.BIGINT:
				return column.getName() + " INTEGER " + nullDef(column);
			case Types.BIT:
				return column.getName() + " INTEGER " + nullDef(column);
			case Types.BOOLEAN:
				return column.getName() + " INTEGER " + nullDef(column);
			case Types.FLOAT:
				return column.getName() + " REAL " + nullDef(column);
			case Types.REAL:
				return column.getName() + " REAL " + nullDef(column);
			case Types.DOUBLE:
				return column.getName() + " REAL " + nullDef(column);
			case Types.NUMERIC:
				return column.getName() + " REAL " + nullDef(column);
			case Types.DECIMAL:
				return column.getName() + " REAL " + nullDef(column);
			case Types.CHAR:
				return column.getName() + " TEXT(" + column.getSize() + ") " + nullDef(column);
			case Types.VARCHAR:
				return column.getName() + " TEXT(" + column.getSize() + ") " + nullDef(column);
			case Types.LONGVARCHAR:
				return column.getName() + " TEXT(" + column.getSize() + ") " + nullDef(column);
			case Types.DATE:
				return column.getName() + " REAL " + nullDef(column);
			case Types.TIME:
				return column.getName() + " REAL " + nullDef(column);
			case Types.TIMESTAMP:
				return column.getName() + " TEXT(512) " + nullDef(column);
			case Types.BINARY:
				return column.getName() + " BLOB " + nullDef(column);
			case Types.LONGVARBINARY:
				return column.getName() + " BLOB " + nullDef(column);
			case Types.VARBINARY:
				return column.getName() + " BLOB " + nullDef(column);
			case Types.BLOB:
				return column.getName() + " BLOB " + nullDef(column);
			case Types.CLOB:
				return column.getName() + " TEXT " + nullDef(column);
			case Types.ROWID:
				return column.getName() + " TEXT " + nullDef(column);
			case Types.NCHAR:
				return column.getName() + " TEXT(" + column.getSize() + ") " + nullDef(column);
			case Types.NVARCHAR:
				return column.getName() + " TEXT(" + column.getSize() + ") " + nullDef(column);
			case Types.LONGNVARCHAR:
				return column.getName() + " TEXT(" + column.getSize() + ") " + nullDef(column);
			case Types.NCLOB:
				return column.getName() + " TEXT " + nullDef(column);
			default:
				throw new NotSupportDBType();
			}
		}
		
	}
	
	class MySQLDefine extends DBDefine{ 
		protected String getFieldDefine(IColumn column) throws NotSupportDBType{
			switch (column.getType()) {
			case Types.ARRAY:
				return column.getName() + " BLOB " + nullDef(column);
			case Types.TINYINT:
				return column.getName() + " TINYINT " + nullDef(column);
			case Types.SMALLINT:
				return column.getName() + " SMALLINT " + nullDef(column);
			case Types.INTEGER:
				return column.getName() + " INTEGER " + nullDef(column);
			case Types.BIGINT:
				return column.getName() + " BIGINT " + nullDef(column);
			case Types.BIT:
				return column.getName() + " TINYINT " + nullDef(column);
			case Types.BOOLEAN:
				return column.getName() + " TINYINT " + nullDef(column);
			case Types.FLOAT:
				return column.getName() + " FLOAT " + nullDef(column);
			case Types.REAL:
				return column.getName() + " REAL " + nullDef(column);
			case Types.DOUBLE:
				return column.getName() + " DOUBLE " + nullDef(column);
			case Types.NUMERIC:
				return column.getName() + " NUMERIC(" + column.getSize() + "," + column.getScale() + ") " + nullDef(column);
			case Types.DECIMAL:
				return column.getName() + " DECIMAL " + nullDef(column);
			case Types.CHAR:
				return column.getName() + " CHAR(" + column.getSize() + ") " + nullDef(column);
			case Types.VARCHAR:
				return column.getName() + " VARCHAR(" + column.getSize() + ") " + nullDef(column);
			case Types.LONGVARCHAR:
				return column.getName() + " TEXT " + nullDef(column);
			case Types.DATE:
				return column.getName() + " DATETIME " + nullDef(column);
			case Types.TIME:
				return column.getName() + " TIME " + nullDef(column);
			case Types.TIMESTAMP:
				return column.getName() + " TIMESTAMP " + nullDef(column);
			case Types.BINARY:
				return column.getName() + " CHAR(" + column.getSize() + ") " + nullDef(column);
			case Types.LONGVARBINARY:
				return column.getName() + " BLOB " + nullDef(column);
			case Types.VARBINARY:
				return column.getName() + " VARCHAR(" + column.getSize() + ") " + nullDef(column);
			case Types.BLOB:
				return column.getName() + " BLOB " + nullDef(column);
			case Types.CLOB:
				return column.getName() + " TEXT " + nullDef(column);
			case Types.ROWID:
				return column.getName() + " TEXT " + nullDef(column);
			case Types.NCHAR:
				return column.getName() + " TEXT(" + column.getSize() + ") " + nullDef(column);
			case Types.NVARCHAR:
				return column.getName() + " TEXT(" + column.getSize() + ") " + nullDef(column);
			case Types.LONGNVARCHAR:
				return column.getName() + " TEXT(" + column.getSize() + ") " + nullDef(column);
			case Types.NCLOB:
				return column.getName() + " TEXT " + nullDef(column);
			default:
				throw new NotSupportDBType();
			}
		}
		
	}
	
	class MSSQLDefine extends DBDefine{ 
		protected String getFieldDefine(IColumn column) throws NotSupportDBType{
			switch (column.getType()) {
			case Types.ARRAY:
				return column.getName() + " image " + nullDef(column);
			case Types.TINYINT:
				return column.getName() + " TINYINT " + nullDef(column);
			case Types.SMALLINT:
				return column.getName() + " SMALLINT " + nullDef(column);
			case Types.INTEGER:
				return column.getName() + " INTEGER " + nullDef(column);
			case Types.BIGINT:
				return column.getName() + " BIGINT " + nullDef(column);
			case Types.BIT:
				return column.getName() + " bit " + nullDef(column);
			case Types.BOOLEAN:
				return column.getName() + " bit " + nullDef(column);
			case Types.FLOAT:
				return column.getName() + " FLOAT " + nullDef(column);
			case Types.REAL:
				return column.getName() + " real " + nullDef(column);
			case Types.DOUBLE:
				return column.getName() + " DOUBLE " + nullDef(column);
			case Types.NUMERIC:
				return column.getName() + " NUMERIC(" + column.getSize() + "," + column.getScale() + ") " + nullDef(column);
			case Types.DECIMAL:
				return column.getName() + " decimal(" + column.getSize() + "," + column.getScale() + ") " + nullDef(column);
			case Types.CHAR:
				return column.getName() + " CHAR(" + column.getSize() + ") " + nullDef(column);
			case Types.VARCHAR:
				return column.getName() + " VARCHAR(" + column.getSize() + ") " + nullDef(column);
			case Types.LONGVARCHAR:
				return column.getName() + " TEXT " + nullDef(column);
			case Types.DATE:
				return column.getName() + " DATETIME " + nullDef(column);
			case Types.TIME:
				return column.getName() + " DATETIME " + nullDef(column);
			case Types.TIMESTAMP:
				return column.getName() + " timestamp " + nullDef(column);
			case Types.BINARY:
				return column.getName() + " binary(" + column.getSize() + ") " + nullDef(column);
			case Types.LONGVARBINARY:
				return column.getName() + " image " + nullDef(column);
			case Types.VARBINARY:
				return column.getName() + " varbinary(" + column.getSize() + ") " + nullDef(column);
			case Types.BLOB:
				return column.getName() + " image " + nullDef(column);
			case Types.CLOB:
				return column.getName() + " TEXT " + nullDef(column);
			case Types.ROWID:
				return column.getName() + " TEXT " + nullDef(column);
			case Types.NCHAR:
				return column.getName() + " NCHAR(" + column.getSize() + ") " + nullDef(column);
			case Types.NVARCHAR:
				return column.getName() + " NVARCHAR(" + column.getSize() + ") " + nullDef(column);
			case Types.LONGNVARCHAR:
				return column.getName() + " TEXT(" + column.getSize() + ") " + nullDef(column);
			case Types.NCLOB:
				return column.getName() + " TEXT " + nullDef(column);
			default:
				throw new NotSupportDBType();
			}
		}
		
		protected void buildCreateSql(IDataset dataset) throws NotSupportDBType{
			String tablename = getTableName();
			sql = "create table " + tablename + "(";
			String fieldDefs = null;
			for (IColumn column : dataset.getColumns()) {
				String fieldDef = getFieldDefine(column);
				
				if (fieldDefs == null)
					fieldDefs = fieldDef;
				else{
					fieldDefs += "," + fieldDef;
				}
			}
			
			String primKeyDefs = null;
			for (String primKey : dataset.getPrimkeys()) {
				if (primKeyDefs == null)
					primKeyDefs = primKey;
				else {
					primKeyDefs += " , " + primKey;
				}
			}
			
			sql += fieldDefs + ",constraint PK_" + tablename + " primary key (" + primKeyDefs + "))";
		}
	}
	
	class OracleDefine extends MSSQLDefine{ 
		protected String getFieldDefine(IColumn column) throws NotSupportDBType{
			switch (column.getType()) {
			case Types.ARRAY:
				return column.getName() + " image " + nullDef(column);
			case Types.TINYINT:
				return column.getName() + " INTEGER " + nullDef(column);
			case Types.SMALLINT:
				return column.getName() + " INTEGER " + nullDef(column);
			case Types.INTEGER:
				return column.getName() + " INTEGER " + nullDef(column);
			case Types.BIGINT:
				return column.getName() + " NUMERIC(20) " + nullDef(column);
			case Types.BIT:
				return column.getName() + " INTEGER " + nullDef(column);
			case Types.BOOLEAN:
				return column.getName() + " INTEGER " + nullDef(column);
			case Types.FLOAT:
				return column.getName() + " FLOAT " + nullDef(column);
			case Types.REAL:
				return column.getName() + " REAL " + nullDef(column);
			case Types.DOUBLE:
				return column.getName() + " REAL " + nullDef(column);
			case Types.NUMERIC:
				return column.getName() + " NUMERIC(" + column.getSize() + "," + column.getScale() + ") " + nullDef(column);
			case Types.DECIMAL:
				return column.getName() + " decimal(" + column.getSize() + "," + column.getScale() + ") " + nullDef(column);
			case Types.CHAR:
				return column.getName() + " CHAR(" + column.getSize() + ") " + nullDef(column);
			case Types.VARCHAR:
				return column.getName() + " VARCHAR2(" + column.getSize() + ") " + nullDef(column);
			case Types.LONGVARCHAR:
				return column.getName() + " LONG " + nullDef(column);
			case Types.DATE:
				return column.getName() + " DATE " + nullDef(column);
			case Types.TIME:
				return column.getName() + " timestamp " + nullDef(column);
			case Types.TIMESTAMP:
				return column.getName() + " timestamp " + nullDef(column);
			case Types.BINARY:
				return column.getName() + " RAW(" + column.getSize() + ") " + nullDef(column);
			case Types.LONGVARBINARY:
				return column.getName() + " LONG " + nullDef(column);
			case Types.VARBINARY:
				return column.getName() + " VARCHAR2(" + column.getSize() + ") " + nullDef(column);
			case Types.BLOB:
				return column.getName() + " BLOB " + nullDef(column);
			case Types.CLOB:
				return column.getName() + " CLOB " + nullDef(column);
			case Types.ROWID:
				return column.getName() + " ROWID " + nullDef(column);
			case Types.NCHAR:
				return column.getName() + " NCHAR(" + column.getSize() + ") " + nullDef(column);
			case Types.NVARCHAR:
				return column.getName() + " NVARCHAR2(" + column.getSize() + ") " + nullDef(column);
			case Types.LONGNVARCHAR:
				return column.getName() + " LONG " + nullDef(column);
			case Types.NCLOB:
				return column.getName() + " NCLOB " + nullDef(column);
			default:
				throw new NotSupportDBType();
			}
		}
		
	}
	
	private void buildCreateSql(IDataset dataset) throws NotSupportDBType {
		switch (db.getDBType()) {
		case dbAccessFile:
		case dbAccessSource:
			new AccesDefine().buildCreateSql(dataset);
			break;
		case dbSqLite:
			new SqliteDefine().buildCreateSql(dataset);
			break;
		case dbPostSQL:
		case dbMySQL:
			new MySQLDefine().buildCreateSql(dataset);
			break;
		case dbDB2: 
		case dbMSSQL:
		case dbSybase:
		case dbInformix:
			new MSSQLDefine().buildCreateSql(dataset);
			break;
		case dbOracle:
			new OracleDefine().buildCreateSql(dataset);
			break;
		default:
			throw new NotSupportDBType();
		}		
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getWhere()
	 */
	@Override
	public String getWhere() {
		return where.toString();
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getGroup()
	 */
	@Override
	public String[] getGroup() {
		return group.toString().split(",");
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getSort()
	 */
	@Override
	public String[] getSort() {
		return order.toString().split(",");
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getHaving()
	 */
	@Override
	public String getHaving() {
		return having.toString();
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getValues()
	 */
	@Override
	public String[] getValues() {
		return values.toString().split(",");
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getFields()
	 */
	@Override
	public String[] getFields() {
		return fields.toArray(new String[fields.size()]);
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getSets()
	 */
	@Override
	public String getSets() {
		return set.toString();
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getPageOrders()
	 */
	@Override
	public String[] getPageOrders() {
		return pageOderKeys.toArray(new String[pageOderKeys.size()]);
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getCatalog()
	 */
	@Override
	public String getCatalog() throws SQLException {
		return db.getCatalog();
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getSchema()
	 */
	@Override
	public String getSchema() throws SQLException {
		return db.getSchema();
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getTableName()
	 */
	@Override
	public String getTableName() {
		return tables.size() == 0 ? null : tables.get(0);
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getRawWhere()
	 */
	@Override
	public StringBuilder getRawWhere(){
		return new StringBuilder(where);
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getRawHaving()
	 */
	@Override
	public StringBuilder getRawHaving(){
		return new StringBuilder(having);
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getRawGroup()
	 */
	@Override
	public StringBuilder getRawGroup(){
		return new StringBuilder(group);
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getRawOrder()
	 */
	@Override
	public StringBuilder getRawOrder(){
		return new StringBuilder(order);
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getRawSet()
	 */
	@Override
	public StringBuilder getRawSet(){
		return new StringBuilder(set);
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getRawValues()
	 */
	@Override
	public StringBuilder getRawValues(){
		return new StringBuilder(values);
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getPagePos()
	 */
	@Override
	public int getPagePos(){
		return pagePos;
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#getPageSize()
	 */
	@Override
	public int getPageSize(){
		return pageSize;
	}

	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#setRawWhere(java.lang.StringBuilder)
	 */
	@Override
	public void setRawWhere(StringBuilder value){
		where = new StringBuilder(value);
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#setRawHaving(java.lang.StringBuilder)
	 */
	@Override
	public void setRawHaving(StringBuilder value){
		having = new StringBuilder(value);
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#setRawGroup(java.lang.StringBuilder)
	 */
	@Override
	public void setRawGroup(StringBuilder value){
		group = new StringBuilder(value);
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#setRawOrder(java.lang.StringBuilder)
	 */
	@Override
	public void setRawOrder(StringBuilder value){
		order = new StringBuilder(value);
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#setRawSet(java.lang.StringBuilder)
	 */
	@Override
	public void setRawSet(StringBuilder value){
		set = new StringBuilder(value);
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#setRawValues(java.lang.StringBuilder)
	 */
	@Override
	public void setRawValues(StringBuilder value){
		values = new StringBuilder(value);
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#setPagePos(int)
	 */
	@Override
	public void setPagePos(int value){
		pagePos = value;
	}
	
	/* (non-Javadoc)
	 * @see wh.ISqlBuilder#setPageSize(int)
	 */
	@Override
	public void setPageSize(int value){
		pageSize = value;
	}
	
	@Override
	public void setRawFields(String[] value){
		fields.clear();
		fields.addAll(Arrays.asList(value));
	}

	@Override
	public void addPageOrderField(String field) throws NotSupportDBType {
		pageOderKeys.add(field);
	}

	@Override
	public String getBlobInsertPlaceholder() throws NotSupportDBType {
		switch (db.getDBType()) {
		case dbOracle:
			return "empty_clob()";
		case dbAccessFile:
		case dbAccessSource:
		case dbMSSQL:
		case dbSqLite:
		case dbSybase:
		case dbDB2:
		case dbMySQL:
		case dbInformix:
		case dbPostSQL:
			return "NULL";
		default:
			throw new NotSupportDBType();
		}
	}

	@Override
	public String getPreparedUpdateSql(String[] fields) throws Exception {
		switch (db.getDBType()) {
		case dbAccessFile:
		case dbAccessSource:
		case dbPostSQL:
		case dbSqLite:
		case dbMySQL:
		case dbDB2:
		case dbMSSQL:
		case dbSybase:
		case dbInformix:
		case dbOracle:
			sql = "update " + tables.get(0) ;
			String setValue = null;
			for (int i = 0; i < fields.length; i++) {
				if (setValue == null)
					setValue = fields[i] + " = ?";
				else
					setValue += " , " + fields[i] + " = ?";
			}
			addWhereString();
			break;
		default:
			throw new NotSupportDBType();
		}
		return sql;
	}

	@Override
	public boolean hasWhere() {
		return where != null && where.length() > 0;
	}
		
}
