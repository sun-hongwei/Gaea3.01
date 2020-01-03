package wh.excel.simple.model;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import wh.excel.model.ExcelModel.ExecuteParam;
import wh.excel.model.ExcelToJsonModel;
import wh.excel.model.JsonToExcelModel;
import wh.excel.parse.DefaultTemplateParser;
import wh.excel.simple.interfaces.IExcelModel;
import wh.excel.template.Config;
import wh.excel.template.Template;

public class ExcelSwitchModel implements IExcelModel<Config> {

	@Override
	public ExportResultInfo exportTo(File templateFile, String templateSheetName, String dataSheetName,
			boolean includeHeader) throws Exception {
		return exportTo(templateFile, templateFile, templateSheetName, dataSheetName, includeHeader);
	}

	@Override
	public ExportResultInfo exportTo(File templateFile, File dataFile, String templateSheetName, String dataSheetName,
			boolean includeHeader) throws Exception {
		ExcelToJsonModel model = new ExcelToJsonModel();
		model.load(templateFile);
		model.setSheet(templateSheetName);
		Template<Config> template = DefaultTemplateParser.getTemplate(model);

		if (!templateFile.equals(dataFile)) {
			model.close();
			model = new ExcelToJsonModel();
			model.load(dataFile);
		}

		ExecuteParam executeParam = new ExecuteParam();
		executeParam.paramObj = new Object[] { includeHeader, template };
		executeParam.sheetName = dataSheetName;
		model.execute(executeParam);

		model.close();

		ExportResultInfo result = new ExportResultInfo();
		result.data = executeParam.result;
		result.template = template;
		return result;
	}

	@Override
	public Template<Config> getJsonToExcelTemplate(File mapTemplateFile, String templateSheetName) throws Exception {
		JsonToExcelModel model = new JsonToExcelModel();
		model.load(mapTemplateFile);
		model.setSheet(templateSheetName);
		Template<Config> template = DefaultTemplateParser.getTemplate(model);
		model.close();

		return template;
	}

	@Override
	public Template<Config> getExcelToJsonTemplate(File mapTemplateFile, String templateSheetName) throws Exception {
		ExcelToJsonModel model = new ExcelToJsonModel();
		model.load(mapTemplateFile);
		model.setSheet(templateSheetName);
		Template<Config> template = DefaultTemplateParser.getTemplate(model);
		model.close();

		return template;
	}

	@Override
	public void importFrom(File mapTemplateFile, File dataTemplateFile, File saveFile, String templateSheetName,
			String dataSheetName, JSONObject datasets) throws Exception {
		Template<Config> template = getJsonToExcelTemplate(mapTemplateFile, templateSheetName);

		importFrom(template, dataTemplateFile, dataSheetName, saveFile, datasets);
	}

	JsonToExcelModel model;
	Template<Config> template;
	String dataSheetName;

	protected void closeBatchImportModel() {
		if (model != null)
			model.close();

		model = null;
		template = null;
		dataSheetName = null;
	}

	@Override
	public void beginBatchImport(Template<Config> template, File dataTemplateFile, String dataSheetName) {
		closeBatchImportModel();

		model = new JsonToExcelModel();
		model.load(dataTemplateFile);
		model.setSheet(dataSheetName);

		this.template = template;
		this.dataSheetName = dataSheetName;
	}

	@Override
	public void batchImportFrom(JSONObject datasets) throws Exception {
		ExecuteParam executeParam = new ExecuteParam();
		executeParam.paramObj = new Object[] { datasets, template };
		executeParam.sheetName = dataSheetName;
		model.execute(executeParam);
	}

	@Override
	public void endBatchImport(File saveFile) throws IOException {
		try {
			if (saveFile.exists())
				if (!saveFile.delete())
					throw new IOException("删除文件[" + saveFile.getAbsolutePath() + "]失败！");
			model.saveAs(saveFile);
		} finally {
			closeBatchImportModel();
		}
	}

	@Override
	public void importFrom(Template<Config> template, File dataTemplateFile, String dataSheetName, File saveFile,
			JSONObject datasets) throws Exception {
		if (saveFile.exists())
			if (!saveFile.delete())
				throw new IOException("删除文件[" + saveFile.getAbsolutePath() + "]失败！");

		JsonToExcelModel model = new JsonToExcelModel();
		model.load(dataTemplateFile);
		model.setSheet(dataSheetName);
		ExecuteParam executeParam = new ExecuteParam();
		executeParam.paramObj = new Object[] { datasets, template };
		executeParam.sheetName = dataSheetName;
		model.execute(executeParam);

		model.saveAs(saveFile);
		model.close();
	}

	@Override
	public void changeImportData(File mapTemplateFile, File dataFile, String dataSheetName, JSONArray dataset)
			throws Exception {

		Template<Config> template = getExcelToJsonTemplate(mapTemplateFile, "config");
		
		ExcelToJsonModel model = new ExcelToJsonModel();
		model.load(dataFile);
		model.setSheet(dataSheetName);
		model.changeData(template, dataSheetName, dataset);
		model.close();
	}

}
