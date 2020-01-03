package wh.role.obj;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class CustomDataRoleInfo {
	
	public enum UseType {
		utSQL, utList
	}

	public class SqlInfo {
		public String sql;

		public JSONObject toJson() {
			JSONObject result = new JSONObject();
			result.put("sql", sql);
			return result;
		}

		public void fromJson(JSONObject data) {
			if (data.has("sql"))
				sql = data.getString("sql");
			else
				sql = null;
		}
	}

	public class ListInfo {
		public Map<String, String> items = new LinkedHashMap<>();
		
		public JSONObject toJson() {
			JSONObject result = new JSONObject();
			result.put("items", new JSONArray(items.keySet()));
			return result;
		}

		public void fromJson(JSONObject data) {
			items.clear();
			if (data.has("items"))
				for (Object obj : data.getJSONArray("items")) {
					String item = (String)obj;
					items.put(item, item);
				}
		}
	}

	public String name;
	public String tablename;
	public String field;
	public UseType useType = UseType.utSQL;

	public List<String> items;
	
	public SqlInfo sqlInfo = new SqlInfo();
	public ListInfo listInfo = new ListInfo();

	public CustomDataRoleInfo(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return name;
	}

	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		data.put("name", name);
		data.put("tablename", tablename);
		data.put("field", field);
		data.put("useType", useType.name());
		data.put("sqlInfo", sqlInfo.toJson());
		data.put("listInfo", listInfo.toJson());
		return data;
	}
	
	public void fromJson(JSONObject data){
		if (data.has("name"))
			name = data.getString("name");
		else
			name = null;
		
		if (data.has("tablename"))
			tablename = data.getString("tablename");
		else
			tablename = null;
		
		if (data.has("field"))
			field = data.getString("field");
		else
			field = null;
		
		if (data.has("useType"))
			useType = UseType.valueOf(data.getString("useType"));
		else
			useType = UseType.utSQL;
		
		sqlInfo = new SqlInfo();
		if (data.has("sqlInfo"))
			sqlInfo.fromJson(data.getJSONObject("sqlInfo"));
		
		listInfo = new ListInfo();
		if (data.has("listInfo"))
			listInfo.fromJson(data.getJSONObject("listInfo"));
		
	}
}
