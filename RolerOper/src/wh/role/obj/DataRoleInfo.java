package wh.role.obj;

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;

import wh.role.obj.RoleServiceObject.ISerializationJson;

public class DataRoleInfo implements ISerializationJson {
	public String id;
	public String groupid;
	public String opertype;
	public String roletype;
	public Collection<String> groups = new ArrayList<>();

	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("id", id);
		json.put("groupid", groupid);
		json.put("opertype", opertype);
		json.put("roletype", roletype);
		JSONArray groupjson = new JSONArray();
		for (String groupid : groups) {
			groupjson.put(groupid);
		}
		json.put("groups", groupjson);
		return json;
	}

	@Override
	public void fromJson(JSONObject json) {
		if (json.has("id"))
			id = json.getString("id");
		else
			id = null;

		if (json.has("groupid"))
			groupid = json.getString("groupid");
		else
			groupid = null;

		if (json.has("opertype"))
			opertype = json.getString("opertype");
		else
			opertype = null;

		if (json.has("roletype"))
			roletype = json.getString("roletype");
		else
			roletype = null;

		groups.clear();
		if (json.has("groups")) {
			JSONArray groupJson = json.getJSONArray("groups");
			for (int i = 0; i < groupJson.length(); i++) {
				String groupid = groupJson.getString(i);
				groups.add(groupid);
			}
		}

	}

	@Override
	public String getKey() {
		return id;
	}

}