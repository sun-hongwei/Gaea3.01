package com.wh.gaea.interfaces;

import java.io.File;

import org.json.JSONObject;

public class LinkInfo {
	public File excelFile;
	public String tableName;
	public String configSheetName;
	public String dataSheetName;
	public File templateFile;

	public int count = -1;

	public LinkInfo(File excelFile, String tableName) {
		this.excelFile = excelFile;
		this.tableName = tableName;

	}

	public LinkInfo(JSONObject data) {
		if (data.has("excel"))
			excelFile = new File(data.getString("excel"));
		if (data.has("name"))
			tableName = data.getString("name");
		if (data.has("configSheetName"))
			configSheetName = data.getString("configSheetName");
		if (data.has("dataSheetName"))
			dataSheetName = data.getString("dataSheetName");
		if (data.has("templateFile"))
			templateFile = new File(data.getString("templateFile"));
	}

	@Override
	public String toString() {
		return excelFile.getName() + " => " + (tableName == null || tableName.isEmpty() ? "无" : tableName)
				+ (count == -1 ? "" : " => 执行：[" + count + "]行");
	}

	public JSONObject toJson() {
		JSONObject data = new JSONObject();
		data.put("excel", excelFile.getAbsolutePath());
		if (templateFile != null)
			data.put("templateFile", templateFile.getAbsolutePath());
		data.put("name", tableName);
		data.put("configSheetName", configSheetName);
		data.put("dataSheetName", dataSheetName);
		return data;
	}

}