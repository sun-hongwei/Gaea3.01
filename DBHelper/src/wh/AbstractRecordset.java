package wh;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import wh.interfaces.IDataset;
import wh.interfaces.ISqlBuilder;

abstract class AbstractRecordset implements IDataset {

	int position = -1;
	JSONArray rawData;
	List<String> primKeys = new ArrayList<>();
	HashMap<String, IColumn> keyColumns = new HashMap<>();
	
	List<IColumn> columns = new ArrayList<>();
	List<IRow> rows = new ArrayList<>();
	List<IRow> deleteRows = new ArrayList<>();
	
	HashMap<String, IRow> hashMapKeys = new HashMap<>();
	ISqlBuilder sqlBuilder;
	class Column implements IColumn{
		String tablename;
		String name;
		String typeName;
		String label;
		int type;
		int size;
		int scale;
		boolean _readonly = false;
		boolean _allowNull = true;
		Object tag;
		
		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getTypeName() {
			return typeName;
		}

		@Override
		public int getType() {
			return type;
		}

		@Override
		public int getSize() {
			return size;
		}

		@Override
		public String getLabel() {
			return label;
		}

		@Override
		public int getScale() {
			return scale;
		}

		@Override
		public String getTableName() {
			return tablename;
		}

		@Override
		public boolean readonly() {
			return _readonly;
		}

		@Override
		public boolean isBlob() {
			return AbstractRecordset.isBlob(type);
		}

		@Override
		public Object getTag() {
			return tag;
		}

		@Override
		public void setTag(Object tag) {
			this.tag = tag;
		}

		@Override
		public boolean allowNull() {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	
	protected static boolean isBlob(int type) {
		switch (type) {
		case Types.BLOB:
		case Types.CLOB:
		case Types.NCLOB:
			return true;
		default:
			return false;
		}
	}
	
	enum RowState {
		rsNone, rsInit, rsAdd, rsDelete, rsEdit
	}
	
	class Row implements IRow{

		RowState rowState = RowState.rsNone;
		JSONObject rowData;
		List<IColumn> columns;
		HashMap<String, IColumn> keyColumns;
		public Row(List<IColumn> columns, HashMap<String, IColumn> keyColumns){
			this.columns = columns;
			this.keyColumns = keyColumns;
		}
		
		@Override
		public JSONObject getValues() {
			return rowData;
		}

	    protected String indexOfColumnName(int index) {
			return columns.get(index).getName();
		}
	    
		@Override
		public Object getValue(int column) {
			return getValue(indexOfColumnName(column));
		}

		@Override
		public Object getValue(String columnName) {
			if (rowData == null)
				return null;

			if (!keyColumns.containsKey(columnName))
				return null;
			
			if (!rowData.has(columnName)){
				return null;
			}
			try {
				Object value = rowData.get(columnName);
				
				IColumn column = keyColumns.get(columnName);
				switch (column.getType()) {
				case Types.CHAR:
				case Types.NCHAR:
				case Types.VARCHAR:
				case Types.NVARCHAR:
				case Types.LONGNVARCHAR:
				case Types.LONGVARCHAR:
					value = StringEscapeUtils.unescapeJava(value == null ? null : value.toString());
					break;
				} 
				
				return value;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		protected void updateRowState(RowState state) {
			if (rowState == RowState.rsAdd || rowState == RowState.rsDelete)
				return;
			rowState = state;
		}

		protected void updateHashValues(IColumn cc){
			if (cc == null || primKeys.indexOf(cc.getName()) != -1){
				createHashKeys();
			}
		}
		
		@Override
		public void setValue(int column, Object value) throws Exception {
			if (rowData == null)
				throw new NullPointerException("rowData is null");
			Column cc = (Column)columns.get(column); 
			if (cc.isBlob() && !(value instanceof byte[]))
				throw new Exception("blob field that value must be byte[]");
			rowData.put(indexOfColumnName(column), value);
			updateRowState(RowState.rsEdit);
			updateHashValues(cc);
		}

		@Override
		public void setValue(String column, Object value) throws Exception {
			if (rowData == null)
				throw new NullPointerException("rowData is null");
			
			rowData.put(column, value);
			updateRowState(RowState.rsEdit);
			updateHashValues(keyColumns.get(column));
		}

		@Override
		public void setValues(JSONObject rowData) {
			this.rowData = rowData;
			updateRowState(RowState.rsInit);
			updateHashValues(null);
		}
		
		public RowState getState(){
			return rowState;
		}

		Object tag;
		@Override
		public Object getTag() {
			return tag;
		}

		@Override
		public void setTag(Object tag) {
			this.tag = tag;
		}
		
	}
	
	Object tag;
	@Override
	public Object getTag() {
		return tag;
	}

	@Override
	public void setTag(Object tag) {
		this.tag = tag;
	}
	
	public void init(String[] primkeys, Object dataset) throws Exception {
		
		ResultSet resultSet = (ResultSet)dataset;
		
		deleteRows.clear();
		columns.clear();
		rows.clear();
		this.primKeys.clear();
		
		HashMap<String, String> hashPrimKeys = new HashMap<>();
		if (primkeys != null && primkeys.length > 0){
			for (String key : primkeys) {
				hashPrimKeys.put(key.toLowerCase().trim(), key);
			}
		}
		
		rawData = new JSONArray();
		ResultSetMetaData metaData = resultSet.getMetaData();
		for (int i = 1; i <= metaData.getColumnCount(); i++) {
			Column column = new Column();
			column._readonly = metaData.isReadOnly(i);
			column.name = metaData.getColumnName(i).toLowerCase();
			column.label = metaData.getColumnLabel(i);
			column.type = metaData.getColumnType(i);
			column.typeName = metaData.getColumnTypeName(i);
			column.size = metaData.getPrecision(i);
			column.scale = metaData.getScale(i);
			column.tablename = metaData.getTableName(i);
			column._allowNull = metaData.isNullable(i) == ResultSetMetaData.columnNullable;
			keyColumns.put(column.getName(), column);
			columns.add(column);

			if (hashPrimKeys.containsKey(column.getName().toLowerCase().trim()))
				this.primKeys.add(column.getName());
		}
		

		boolean hasData = false;
		while (resultSet.next()){
			if (!hasData)
				hasData = true;
			JSONObject rowData = new JSONObject();
			for (int i = 1; i <= metaData.getColumnCount(); i++) {
				String key = metaData.getColumnName(i).toLowerCase();
				rowData.put(key, resultSet.getObject(i));
			}
			
			Row row = new Row(columns, keyColumns);
			row.setValues(rowData);
			rows.add(row);
			rawData.put(rowData);
		}
		
		if (hasData){
			position = 0;
			createHashKeys();
		}
	}
	
	@Override
	public List<IColumn> getColumns() {
		return new ArrayList<>(columns);
	}

	@Override
	public IColumn getColumn(int index) {
		return columns.get(index);
	}

	@Override
	public IColumn getColumn(String name) {
		return keyColumns.get(name);
	}

	@Override
	public IRow getRow(int index) {
		return rows.get(index);
	}

	@Override
	public List<IRow> getRows() {
		return new ArrayList<>(rows);
	}

	@Override
	public JSONArray getRawData() {
		return rawData;
	}

	@Override
	public JSONObject getRowData(int row) {
		try {
			return rawData.getJSONObject(row);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Object getValue(int row, int column) {
		return rows.get(row).getValue(column);
	}

	@Override
	public Object getValue(int row, String column) {
		return rows.get(row).getValue(column);
	}

	@Override
	public int getColumnCount() {
		return columns.size();
	}

	@Override
	public int getRowCount() {
		return rows.size();
	}

	@Override
	public IRow newRow() {
		Row row = new Row(columns, keyColumns);
		row.rowData = new JSONObject();
		return row;
	}

	@Override
	public void setRow(int index) {
		if (index >= 0 && index < rows.size())
			position = index;
	}

	@Override
	public int getRow() {
		return position;
	}

	@Override
	public ISqlBuilder getSqlBuilder() {
		return sqlBuilder;
	}

	public void setSqlBuilder(ISqlBuilder sqlBuilder){
		this.sqlBuilder = sqlBuilder;
	}
	
	String sql;
	public String getSQL() throws Exception{
		if (sqlBuilder != null)
			return sqlBuilder.getSql();
		else
			return sql;
	}
	
	public void setSQL(String sql){
		this.sql = sql;
	}
	
	@Override
	public void addRow(IRow row) {
		rows.add(row);
		((Row)row).updateRowState(RowState.rsAdd);
	}

	@Override
	public void removeRow() {
		removeRow(position);
	}

	@Override
	public void removeRow(int index) {
		if (index < 0 || index >= rows.size())
			throw new IndexOutOfBoundsException();
		IRow row = rows.remove(index);
		deleteRows.add(row);
	}

	@Override
	public String[] getPrimkeys() {
		return primKeys.toArray(new String[primKeys.size()]);
	}
	
	public static class MD5Util {
		public static String MD5(String s) {
			char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
					'9', 'A', 'B', 'C', 'D', 'E', 'F' };

			try {
				byte[] btInput = s.getBytes();

				MessageDigest mdInst = MessageDigest.getInstance("MD5");

				mdInst.update(btInput);

				byte[] md = mdInst.digest();

				int j = md.length;
				char str[] = new char[j * 2];
				int k = 0;
				for (int i = 0; i < j; i++) {
					byte byte0 = md[i];
					str[k++] = hexDigits[byte0 >>> 4 & 0xf];
					str[k++] = hexDigits[byte0 & 0xf];
				}
				return new String(str);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public static String MD5_128(String value) {
			return null;
		}
	}

	protected String getHashKey(HashMap<String, Object> primKeyValues){
		String value = null;
		for (String key : this.primKeys) {
			if (value == null)
				value = primKeyValues.get(key).toString();
			else
				value += primKeyValues.get(key).toString();
		}
		
		return MD5Util.MD5(value);
	}
	
	protected void createHashKeys() {
		hashMapKeys.clear();
		if (primKeys.size() == 0)
			return;
		
		HashMap<String, Object> primKeyValues = new HashMap<>();
		for (IRow irow : rows) {
			Row row = (Row)irow;
			primKeyValues.clear();
			for (String key : primKeys) {
				primKeyValues.put(key, row.getValue(key));
			}
			
			String key = getHashKey(primKeyValues);
			hashMapKeys.put(key, irow);
		}
	}
	
	@Override
	public int indexOfRow(HashMap<String, Object> primKeyValues) {
		String key = getHashKey(primKeyValues);
		if (key == null || !hashMapKeys.containsKey(key))
			return -1;
		
		IRow row = hashMapKeys.get(key);
		return rows.indexOf(row);
	}

	public IColumn newColumn(String name, String label, int type, int size, int scale) throws Exception{
		Column column = new Column();
		column.name = name.toLowerCase();
		column.label = label;
		column.type = type;
		column.size = size;
		column.scale = scale;
		column._readonly = false;
		for(Field field : Types.class.getDeclaredFields()){
			if ((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC){
				if (field.getInt(null) == type){
					column.typeName = field.getName();
					return column;
				}
			}
		}
		throw new ColumnTypeNotFoundException();
	}
	
	public void addColumn(IColumn column) throws RowDataExistedException, ColumnAlreadyExistedException{
		if (rows.size() > 0)
			throw new RowDataExistedException();
		
		if (keyColumns.containsKey(column.getName().toLowerCase())){
			throw new ColumnAlreadyExistedException();
		}
		
		columns.add(column);
		keyColumns.put(column.getName(), column);
	}
	
	public void removeColumn(String column_name) throws RowDataExistedException{
		if (keyColumns.containsKey(column_name)){
			IColumn column = keyColumns.remove(column_name);
			columns.remove(column);
			removePrimKey(column_name);
		}
	}

	public void addPrimKey(String column_name) throws RowDataExistedException{
		if (rows.size() > 0)
			throw new RowDataExistedException();
		
		int index = primKeys.indexOf(column_name);
		if (index == -1){
			primKeys.add(column_name);
		}
	}

	public void removePrimKey(String column_name) throws RowDataExistedException{
		if (rows.size() > 0)
			throw new RowDataExistedException();
		
		int index = primKeys.indexOf(column_name);
		if (index != -1){
			primKeys.remove(column_name);
		}
	}
}
