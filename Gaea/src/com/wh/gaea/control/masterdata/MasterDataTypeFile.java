package com.wh.gaea.control.masterdata;

import java.io.File;

import org.json.JSONArray;

import com.wh.gaea.control.EditorEnvironment;
import com.wh.tools.JsonHelp;

public class MasterDataTypeFile {
	static File file = EditorEnvironment.getProjectFile(EditorEnvironment.MasterData_Dir_Name, "types." + EditorEnvironment.MasterData_Type_File_Extension);
	
	public static JSONArray getTypes() throws Exception{
		if (file.exists())
			return (JSONArray) JsonHelp.parseCacheJson(file, null);
		else
			return new JSONArray();
	}
	
	public static void setTypes(JSONArray types) throws Exception{
		JsonHelp.saveJson(file, types, null);
	}
}
