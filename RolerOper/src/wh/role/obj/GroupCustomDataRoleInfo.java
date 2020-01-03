package wh.role.obj;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import wh.role.obj.RoleServiceObject.DataOperType;
import wh.role.obj.RoleServiceObject.ISerializationJson;

public class GroupCustomDataRoleInfo implements ISerializationJson {
	public String id;
	public String groupid;
	public DataOperType operType = DataOperType.dtQuery;
	public Map<String, GroupCustomDataRoleInfo.RoleInfo> roles = new HashMap<>();

	public static class RoleInfo {
		public String name;
		public String id;
		public Map<String, String> items = new HashMap<>();

		public JSONObject toJson() {
			JSONObject data = new JSONObject();
			data.put("name", name);
			data.put("id", id);
			data.put("items", items.keySet());
			return data;
		}

		public void fromJson(JSONObject data) {
			name = data.getString("name");
			id = data.getString("id");
			items.clear();
			if (data.has("items")) {
				JSONArray itemData = data.getJSONArray("items");
				for (Object obj : itemData) {
					String item = (String) obj;
					items.put(item, item);
				}
			}
		}

		@Override
		public String toString() {
			return name;
		}
	}

	@Override
	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		data.put("id", id);
		data.put("groupid", groupid);
		data.put("operType", operType.name());
		JSONArray rolesData = new JSONArray();
		for (GroupCustomDataRoleInfo.RoleInfo info : roles.values()) {
			rolesData.put(info.toJson());
		}
		data.put("roles", rolesData);
		return data;
	}

	@Override
	public void fromJson(JSONObject json) {
		id = json.getString("id");
		groupid = json.getString("groupid");
		operType = DataOperType.valueOf(json.getString("operType"));
		roles.clear();
		if (json.has("roles")) {
			JSONArray itemsdata = json.getJSONArray("roles");
			for (Object object : itemsdata) {
				JSONObject roleData = (JSONObject) object;
				GroupCustomDataRoleInfo.RoleInfo info = new RoleInfo();
				info.fromJson(roleData);
				roles.put(info.name, info);
			}
		}
	}

	@Override
	public String getKey() {
		return groupid;
	}
}