package com.wh.gaea.interfaces;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

public interface IDrawPageConfig {

	PageSizeMode getCurPageSizeMode();

	String getCurPageSizeModeName();

	void load(File file) throws Exception;

	void toJson(JSONObject json) throws JSONException;

	void fromJson(JSONObject json) throws JSONException;

	void setConfig(Config[] configs);

	Config[] getConfig();

	boolean checkConfig(Config config);

	void setPageSizeMode();

	void setPageSizeMode(String text, int width, int height);

	void setPageSizeMode(PageSizeMode setPageSize, int customWidth, int customHeight);

	String getTitle();
	String getMemo();
	String getName();
	
	int getWidth();
	int getHeight();
}