package com.wh.gaea.interfaces.selector;

import java.io.File;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.interfaces.LinkInfo;

public interface IExcelSelector {
	/**
	 * 显示execl导出到数据库的ui
	 */
	void showExcelToDBView();

	/***
	 * 导入execl数据到数据库
	 * 
	 * @Param infos excel数据源定义列表
	 */
	void importToDB(List<LinkInfo> infos) throws Exception;

	/***
	 * 将excel文件转换为json
	 * 
	 * @param file 要导入的数据源excel文件，此文件必须同时包含配置信息及数据页，配置信息页必须名为“config”，数据页必须名为“data”
	 * @return 包含数据的jsonobject，其中“header”项为列定义JSONArray,每列格式为{id:"字段id",name:"字段名称",type:"字段类型名称，java类定义名称"}，“data”项为数据jsonarray，每行格式为{field:value}
	 * @throws Exception 
	 */
	JSONObject importToDataset(File templateFile, File dataFile) throws Exception;

	/***
	 * 将dataset包含的数据导入excel文件中
	 * @param templateFile excel导入的模板映射文件，必须包括数据映射信息及格式定义信息，数据映射页必须名为“config”，格式定义页必须名为“data”
	 * @param saveFile 输出的excel数据文件，存在则会覆盖
	 * @param dataset 要导入的数据信息，格式{"数据id":[{field:value}]}
	 * @throws Exception 
	 */
	void exportToExcel(File templateFile, File saveFile, JSONObject dataset) throws Exception;
	
	/***
	 * 修改导入数据页数据，修改前后的数据不会发生行数及列数的变化
	 * @param templatelFile 导入的数据映射模板文件
	 * @param dataFile 要修改的数据文件
	 * @param dataSheetName 包含的数据页名称
	 * @param dataset 要应用的数据集合
	 * @throws Exception 
	 */
	void changeImportData(File templatelFile, File dataFile, String dataSheetName, JSONArray dataset) throws Exception;

	void importToDB(LinkInfo info) throws Exception;
}
