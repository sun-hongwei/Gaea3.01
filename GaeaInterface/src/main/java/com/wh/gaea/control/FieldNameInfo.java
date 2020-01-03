package com.wh.gaea.control;

import org.json.JSONObject;

public class FieldNameInfo {
	public String field;
	public String name;

	public FieldNameInfo(String field, String name) {
		this.field = field;
		this.name = name;
	}

	public String toString() {
		return name;
	}

	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		data.put("field", field);
		data.put("name", name);
		return data;
	}

	public FieldNameInfo(JSONObject data) {
		if (data.has("field"))
			field = data.getString("field");
		if (data.has("name"))
			name = data.getString("name");
	}
}