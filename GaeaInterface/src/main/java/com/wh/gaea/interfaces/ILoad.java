package com.wh.gaea.interfaces;

import org.json.JSONException;
import org.json.JSONObject;

public interface ILoad {
	public void onBeforeLoad(JSONObject json, Object param) throws JSONException;
}