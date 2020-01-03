package wh.role.obj;

import java.util.Date;

import org.json.JSONObject;

import wh.role.obj.RoleServiceObject.ISerializationJson;

public class UserInfo implements ISerializationJson {
	public String userid;
	public String username;
	public String password;
	public boolean superview;
	public boolean superbutton;
	public boolean supermenu;
	public boolean superdata;
	public Date registertime;
	public Date lasttime;

	public String toString() {
		return (username == null || username.isEmpty()) ? userid : username;
	}

	@Override
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("userid", userid);
		json.put("username", username);
		json.put("password", password);
		json.put("superview", superview);
		json.put("superbutton", superbutton);
		json.put("supermenu", supermenu);
		json.put("superdata", superdata);
		json.put("registertime", registertime.getTime());
		json.put("lasttime", lasttime.getTime());
		return json;
	}

	@Override
	public void fromJson(JSONObject json) {
		if (json.has("userid"))
			userid = json.getString("userid");
		else
			userid = null;

		if (json.has("username"))
			username = json.getString("username");
		else
			username = null;

		if (json.has("password"))
			password = json.getString("password");
		else
			password = null;

		if (json.has("superview"))
			superview = json.getBoolean("superview");
		else
			superview = false;

		if (json.has("superbutton"))
			superbutton = json.getBoolean("superbutton");
		else
			superbutton = false;

		if (json.has("supermenu"))
			supermenu = json.getBoolean("supermenu");
		else
			supermenu = false;

		if (json.has("superdata"))
			superdata = json.getBoolean("superdata");
		else
			superdata = false;

		if (json.has("registertime"))
			registertime = new Date(json.getLong("registertime"));
		else
			registertime = null;

		if (json.has("lasttime"))
			lasttime = new Date(json.getLong("lasttime"));
		else
			lasttime = null;

	}

	@Override
	public String getKey() {
		return userid;
	}
}