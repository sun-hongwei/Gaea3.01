package wh.excel.simple.interfaces;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import wh.excel.template.Config;
import wh.excel.template.Template;

public interface IExcelModel<T extends Config> {

	public static class ExportResultInfo{
		public Object data;
		public Template<Config> template;
	}
	
	/***
	 * 将dataset的数据导入到excel中，并将数据页保存成单独的文件
	 * @param mapTemplateFile 数据集与excel的映射关系模板文件
	 * @param dataTemplateFile 数据格式模板文件
	 * @param saveFile 输出文件，如果存在会覆盖
	 * @param templateSheetName 模板定义的sheet名称
	 * @param dataSheetName 待导入数据的sheet名称
	 * @param dataset 要导入的数据json，形如[{field:"value"}]
	 * @throws Exception 
	 */
	void importFrom(File mapTemplateFile, File dataTemplateFile, File saveFile, String templateSheetName,
			String dataSheetName, JSONObject datasets) throws Exception;

	/***
	 * 将dataset的数据导入到excel中，并将数据页保存成单独的文件
	 * @param template 数据映射模板对象
	 * @param saveFile 输出文件，如果存在会覆盖
	 * @param dataSheetName 待导入数据的sheet名称
	 * @param dataset 要导入的数据json，形如[{field:"value"}]
	 * @throws Exception 
	 */
	void importFrom(Template<Config> template, File dataTemplateFile, String dataSheetName, 
			File saveFile, JSONObject datasets) throws Exception;

	/**
	 * 将excel数据导出到json，数据与模板在同一个文件中
	 * @param templateFile excel的导出模板文件
	 * @param templateSheetName 模板定义的sheet名称
	 * @param dataSheetName 待导出数据的sheet名称
	 * @param includeHeader 导出json是否包含表头，true包含，其他不包含
	 * true数据格式：{header:[{fieldId:fieldname}], data:[{field:value}]}
	 * false数据格式：[{field:value}]
	 * @return includeHeader=true,返回JSONObject对象, 否则返回JSONArray对象
	 * @throws Exception 
	 * 
	 */
	ExportResultInfo exportTo(File templateFile, String templateSheetName, String dataSheetName, boolean includeHeader) throws Exception;

	/**
	 * 将excel数据导出到json，数据与模板在同一个文件中
	 * @param templateFile excel的导出模板文件
	 * @param dataFile excel的数据文件
	 * @param templateSheetName 模板定义的sheet名称，文件由templateFile指定
	 * @param dataSheetName 待导出数据的sheet名称，文件由dataFile指定
	 * @param includeHeader 参考exportTo
	 * @return includeHeader=true,返回JSONObject对象, 否则返回JSONArray对象
	 * @throws Exception 
	 */
	ExportResultInfo exportTo(File templateFile, File dataFile, String templateSheetName, String dataSheetName, boolean includeHeader) throws Exception;

	/**
	 * 获取excel的数据映射模板
	 * @param templateFile excel的映射模板文件
	 * @param templateSheetName 模板定义的sheet名称，文件由templateFile指定
	 * @return 返回模板对象
	 */
	Template<Config> getJsonToExcelTemplate(File mapTemplateFile, String templateSheetName) throws Exception;

	/***
	 * 将dataset的数据导入到excel中
	 * @param dataset 要导入的数据json，形如[{field:"value"}]
	 * @throws Exception 
	 */
	void batchImportFrom(JSONObject datasets) throws Exception;

	/***
	 * 完成批量导入，将数据页保存成单独的文件后释放所有资源
	 * @param saveFile 输出文件，如果存在会覆盖
	 * @throws Exception 
	 */
	void endBatchImport(File saveFile) throws IOException;

	/***
	 * 开始一个批量导入操作，完毕后必须调用endBatchImport以释放资源
	 * @param template 数据映射模板对象
	 * @param dataTemplateFile 数据格式模板文件
	 * @param dataSheetName 待导入数据的sheet名称
	 * @throws Exception 
	 */
	void beginBatchImport(Template<Config> template, File dataTemplateFile, String dataSheetName);

	/***
	 * 修改导入数据页数据，修改前后的数据不会发生行数及列数的变化
	 * @param templatelFile 导入的数据映射模板文件
	 * @param dataFile 要修改的数据文件
	 * @param dataSheetName 包含的数据页名称
	 * @param dataset 要应用的数据集合
	 */
	void changeImportData(File templatelFile, File dataFile, String dataSheetName, JSONArray dataset)
			throws Exception;

	Template<Config> getExcelToJsonTemplate(File mapTemplateFile, String templateSheetName) throws Exception;

}