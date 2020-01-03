package com.wh.swing.tools;

import org.json.JSONObject;

public class FieldInfo {
	public String field;
	public String name;
	public Class<?> valueType;
	
	@Override 
	public String toString() {
		return name;
	}
	
	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("id", field);
		json.put("type", valueType.getClass().getName());
		return json;
	}
}
