package wh.role.obj;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import wh.role.obj.RoleServiceObject.ISerializationJson;

public class GroupInfo implements ISerializationJson {
	public String groupid;
	public String groupname;
	public String grouptype;
	public String grouppid;
	public String groupmemo;
	public Map<String, String> simpleUsers = new HashMap<>();
	public Map<String, String> users = new HashMap<>();

	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("groupid", groupid);
		json.put("groupname", groupname);
		json.put("grouppid", grouppid);
		json.put("grouptype", grouptype);
		json.put("groupmemo", groupmemo);
		JSONArray userJson = new JSONArray();
		for (String userid : users.keySet()) {
			userJson.put(userid);
		}
		json.put("users", userJson);
		return json;
	}

	@Override
	public void fromJson(JSONObject json) {
		if (json.has("groupid"))
			groupid = json.getString("groupid");
		else
			groupid = null;

		if (json.has("groupname"))
			groupname = json.getString("groupname");
		else
			groupname = null;

		if (json.has("grouppid"))
			grouppid = json.getString("grouppid");
		else
			grouppid = null;

		if (json.has("grouptype"))
			grouptype = json.getString("grouptype");
		else
			grouptype = "role";

		if (json.has("groupmemo"))
			groupmemo = json.getString("groupmemo");
		else
			groupmemo = "";

		users.clear();
		if (json.has("users")) {
			JSONArray userjson = json.getJSONArray("users");
			for (int i = 0; i < userjson.length(); i++) {
				String userid = userjson.getString(i);
				users.put(userid, userid);
			}
		}
	}

	@Override
	public String getKey() {
		return groupid;
	}
}