package wh.role.obj;

import org.json.JSONObject;

import wh.role.obj.RoleServiceObject.ISerializationJson;

public class FunRoleInfo implements ISerializationJson {
	public String roleid;
	public String roletext;
	public String rolepid;
	public String rolememo;
	public String roletype;

	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("roleid", roleid);
		json.put("roletext", roletext);
		json.put("rolepid", rolepid);
		json.put("rolememo", rolememo);
		json.put("roletype", roletype);
		return json;
	}

	@Override
	public void fromJson(JSONObject json) {
		if (json.has("roleid"))
			roleid = json.getString("roleid");
		else
			roleid = null;

		if (json.has("roletext"))
			roletext = json.getString("roletext");
		else
			roletext = null;

		if (json.has("rolepid"))
			rolepid = json.getString("rolepid");
		else
			rolepid = null;

		if (json.has("rolememo"))
			rolememo = json.getString("rolememo");
		else
			rolememo = null;

		if (json.has("roletype"))
			roletype = json.getString("roletype");
		else
			roletype = null;

	}

	@Override
	public String getKey() {
		return roleid;
	}
}