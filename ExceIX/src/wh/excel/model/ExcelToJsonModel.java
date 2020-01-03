package wh.excel.model;

import java.util.HashMap;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.json.JSONArray;
import org.json.JSONObject;

import wh.excel.template.Config;
import wh.excel.template.Config.ExprType;
import wh.excel.template.Template;

public class ExcelToJsonModel extends ExcelModel<Config> {

	@SuppressWarnings("unchecked")
	@Override
	public void execute(ExecuteParam executeParam) {
		boolean includeHeader = (boolean) executeParam.paramObj[0];
		Template<Config> template = (Template<Config>) executeParam.paramObj[1];
		setSheet(executeParam.sheetName);
		XSSFSheet sheet = getSheet();
		if (sheet == null)
			throw new RuntimeException("sheet[" + executeParam.sheetName + "] not found!");

		JSONArray header = new JSONArray();
		JSONArray dataset = new JSONArray();

		HashMap<String, String> fieldMap = new HashMap<>();
		for (Config config : template.configs) {
			String[] tmps = config.id.split(":");
			if (includeHeader) {
				JSONObject value = new JSONObject();
				value.put("id", tmps[0]);
				if (tmps.length > 1)
					value.put("name", tmps[1]);
				else
					value.put("name", tmps[0]);
				value.put("type", config.valueType.getName());

				header.put(value);
			}
			fieldMap.put(config.id, tmps[0]);
		}

		for (int i = template.startY; i < sheet.getLastRowNum() + 1; i++) {
			JSONObject value = new JSONObject();
			for (Config config : template.configs) {
				Object v = null;
				if (config.exprType == ExprType.ttConst)
					v = config.expr;
				else {
					XSSFCell cell = getRow(i).getCell(config.startX);
					if (cell == null)
						continue;
					v = getCellValue(cell, config.valueType);
				}
				value.put(fieldMap.get(config.id), v);
			}
			dataset.put(value);
		}

		if (includeHeader) {
			JSONObject result = new JSONObject();
			result.put("header", header);
			result.put("data", dataset);
			executeParam.result = result;
		} else
			executeParam.result = dataset;
	}

	public void changeData(Template<Config> template, String dataSheetName, JSONArray dataset)
			throws ClassNotFoundException {
		setSheet(dataSheetName);
		XSSFSheet sheet = getSheet();
		if (sheet == null)
			throw new RuntimeException("sheet[" + dataSheetName + "] not found!");

		if (!sheetExist("olddata"))
			getBook().cloneSheet(getBook().getSheetIndex(sheet), "olddata");

		HashMap<String, String> fieldMap = new HashMap<>();
		for (Config config : template.configs) {
			String[] tmps = config.id.split(":");
			String id = tmps[0];
//			String name = id;
//			if (tmps.length > 1)
//				name = tmps[1];
//			Class<?> type = Class.forName(config.valueType.getName());
			fieldMap.put(config.id, id);
		}

		try {
			int index = 0;
			for (int i = template.startY; i < sheet.getLastRowNum() + 1; i++) {
				if (index == dataset.length())
					return;

				JSONObject row = dataset.getJSONObject(index++);

				for (Config config : template.configs) {
					XSSFCell cell = getRow(i).getCell(config.startX);
					if (cell == null)
						cell = getRow(i).createCell(config.startX);

					if (!fieldMap.containsKey(config.id))
						continue;

					
					if (!fieldMap.containsKey(config.id))
						continue;
					
					String field = fieldMap.get(config.id);
					
					if (!row.has(field))
						continue;
					
					Object v = row.get(field);
					setCellType(config.valueType, cell);
					setCellValue(cell, v);
				}
			}
		} finally {
			save();
		}
	}

}
