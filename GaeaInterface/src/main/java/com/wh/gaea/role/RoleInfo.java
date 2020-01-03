package com.wh.gaea.role;

import org.json.JSONObject;

import wh.role.obj.GroupInfo;

public class RoleInfo{
	public String id;
	public String name;
	
	public JSONObject toJson(){
		JSONObject result = new JSONObject();
		result.put("id", id);
		result.put("name", name);
		return result;
	}
	
	public RoleInfo(){
		
	}
	
	public RoleInfo(GroupInfo groupInfo){
		id = groupInfo.groupid;
		name = groupInfo.groupname;
	}
	
	public RoleInfo(JSONObject json){
		if (json.has("id"))
			id = json.getString("id");
		if (json.has("name"))
			name = json.getString("name");
	}
	
	public String toString(){
		return name == null || name.isEmpty() ? id : name;
	}
}