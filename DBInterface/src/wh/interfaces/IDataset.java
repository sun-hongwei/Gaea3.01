package wh.interfaces;

import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public interface IDataset {
	public static class RowDataExistedException extends Exception{
		private static final long serialVersionUID = 1L;}

	public static class ColumnAlreadyExistedException extends Exception{
		private static final long serialVersionUID = 1L;}

	public static class ColumnTypeNotFoundException extends Exception{
		private static final long serialVersionUID = 1L;}

	public interface IColumn{
		public String getLabel();
		public String getName();
		public String getTableName();
		public String getTypeName();
		public int getType();
		public int getSize();
		public int getScale();
		public boolean readonly();
		public boolean isBlob();
		public boolean allowNull();
		public Object getTag();
		public void setTag(Object tag);
	}
	
	public interface IRow{
		public JSONObject getValues();
		public Object getValue(int column);
		public Object getValue(String column);
		public void setValue(int column, Object value) throws Exception;
		public void setValue(String column, Object value) throws Exception;
		public void setValues(JSONObject rowData);
		public Object getTag();
		public void setTag(Object tag);
	}
	
	public String getSQL() throws Exception;
	
	public Object getTag();
	public void setTag(Object tag);
	public String[] getPrimkeys();
	public ISqlBuilder getSqlBuilder();
	public List<IColumn> getColumns();
	public IColumn getColumn(int index);
	public IColumn getColumn(String name);
	public IRow getRow(int index);
	public List<IRow> getRows();
	public JSONArray getRawData();
	public JSONObject getRowData(int row);
	public Object getValue(int row, int column);
	public Object getValue(int row, String column);
	public int getColumnCount();
	public int getRowCount();
	public void post(IDBConnection connection) throws Exception;
	public IRow newRow();
	public void addRow(IRow row);
	public void removeRow();
	public int indexOfRow(HashMap<String, Object> primKeyValues);
	public void removeRow(int index);
	public void setRow(int index);
	public int getRow();
	
	public IColumn newColumn(String name, String label, int type, int size, int scale) throws Exception;
	public void addColumn(IColumn column) throws RowDataExistedException, ColumnAlreadyExistedException;
	public void removeColumn(String column_name) throws RowDataExistedException;
	public void addPrimKey(String column_name) throws RowDataExistedException;
	public void removePrimKey(String column_name) throws RowDataExistedException;
	public void createDBTable(IDBConnection connection, String tablename) throws Exception;
}
