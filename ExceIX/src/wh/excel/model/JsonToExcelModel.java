package wh.excel.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.json.JSONArray;
import org.json.JSONObject;

import wh.excel.template.CommandRuntime.Command;
import wh.excel.template.CommandRuntime.Var;
import wh.excel.template.Config;
import wh.excel.template.Config.EntryInfo;
import wh.excel.template.Config.ExprType;
import wh.excel.template.Config.LoopType;
import wh.excel.template.ConfigItemTemplate;
import wh.excel.template.Template;

public class JsonToExcelModel extends ExcelModel<Config> {

	protected Object getFieldValue(JSONObject row, String field) {
		Object value = null;
		if (row.has(field))
			value = row.get(field);
		else
			return null;
		return value;
	}

	protected String replaceRemoteExpr(String expr, Config config, JSONArray dataset, int curRowIndex) {
		Map<String, EntryInfo> exprs = config.remoteExprs;
		for (String key : exprs.keySet()) {
			EntryInfo keEntry = exprs.get(key);
			Object curRow = keEntry.row;
			String reString = null;
			if (curRow == null) {
				for (int i = 0; i < dataset.length(); i++) {
					JSONObject row = dataset.getJSONObject(i);
					String field = keEntry.dataKey;

					String exprString = getExprString(config, getValue(config, getFieldValue(row, field)));
					if (reString == null)
						reString = exprString;
					else
						reString += "," + exprString;
				}
			} else if (curRow instanceof Integer) {
				JSONObject row = dataset.getJSONObject((int) curRow);

				reString = getExprString(config, getValue(config, getFieldValue(row, keEntry.dataKey)));
			} else {
				if (((String) curRow).compareTo("cur") == 0) {
					JSONObject row = dataset.getJSONObject(curRowIndex);
					reString = getExprString(config, getValue(config, getFieldValue(row, keEntry.dataKey)));
				}
			}
			expr = expr.replace(key, reString);
		}

		return expr;
	}

	interface ILocalExpr {
		String getReplaceStr(Config config, XSSFRow curCellRow, Config refConfig);
	}

	protected String replaceLocalExpr(Template<Config> template, Map<String, EntryInfo> exprs, String expr,
			Config config, int curCellRowIndex, ILocalExpr localExpr) {
		for (String key : exprs.keySet()) {
			EntryInfo kEntry = exprs.get(key);
			Object curRow = kEntry.row;
			String reString = null;
			Config refConfig = template.get(kEntry.dataKey);
			if (curRow == null) {
				for (int i = refConfig.startRuntimeCellRow; i < refConfig.endRuntimeCellRow; i++) {
					XSSFRow curCellRow = getRow(i);
					String exprString = localExpr.getReplaceStr(config, curCellRow, refConfig);
					if (reString == null)
						reString = exprString;
					else
						reString += "," + exprString;
				}
			} else if (curRow instanceof Integer) {
				XSSFRow curCellRow = getRow(refConfig.startRuntimeCellRow + (int) curRow);
				reString = localExpr.getReplaceStr(config, curCellRow, refConfig);
			} else {
				if (((String) curRow).compareTo("cur") == 0) {
					XSSFRow curCellRow = getRow(curCellRowIndex);
					reString = localExpr.getReplaceStr(config, curCellRow, refConfig);
				}
			}
			expr = expr.replace(key, reString);
		}

		return expr;
	}

	protected String replaceLocalExprs(Template<Config> template, String expr, Config config, int curCellRow) {
		expr = replaceLocalExpr(template, config.localExprs, expr, config, curCellRow, new ILocalExpr() {

			@Override
			public String getReplaceStr(Config config, XSSFRow curCellRow, Config refConfig) {
				Object value = getCellValue(curCellRow.getCell(refConfig.startX),
						Date.class.isAssignableFrom(refConfig.valueType));
				return getExprString(config, value);
			}
		});

		expr = replaceLocalExpr(template, config.localAtExprs, expr, config, curCellRow, new ILocalExpr() {

			@Override
			public String getReplaceStr(Config config, XSSFRow curCellRow, Config refConfig) {
				return NumToExcel(refConfig.startX) + (curCellRow.getRowNum() + 1);
			}
		});

		return expr;
	}

	protected String replaceExprs(Template<Config> template, Config config, JSONArray dataset, int curCellRow,
			int curDatasetRow) {
		String expr = (String) config.expr;
		expr = replaceLocalExprs(template, expr, config, curCellRow);
		expr = replaceRemoteExpr(expr, config, dataset, curDatasetRow);
		return expr;
	}

	protected Object computeValue(Template<Config> template, Config config, JSONArray dataset, int curCellRow,
			int curDatasetRow) {
		switch (config.exprType) {
		case ttConst:
			return getValue(config, config.expr);
		case ttExpr:
			return replaceExprs(template, config, dataset, curCellRow, curDatasetRow);
		case ttKey:
			String key = config.remoteDatasetMap.keySet().iterator().next();
			JSONObject row = dataset.getJSONObject(curDatasetRow);
			return getValue(config, getFieldValue(row, key));
		}
		throw new RuntimeException("expr type is not supported!");
	}

	protected void setCellValue(Template<Config> template, Config config, JSONArray dataset, int curCellRow,
			int curDatasetRow) {
		Object value = computeValue(template, config, dataset, curCellRow, curDatasetRow);
		XSSFCell cell = getCell(config.startX, curCellRow);

		int dataFormatIndex = cell.getCellStyle().getDataFormat();
		setCellType(config.valueType, cell);
		if (config.exprType == ExprType.ttExpr) {
			if (!curBook.getForceFormulaRecalculation())
				curBook.setForceFormulaRecalculation(true);
			//公式必须计时计算，因为如果不计算，公式引用的单元格可能发生变化
			try {
				cell.setCellType(CellType.FORMULA);
				cell.setCellFormula((String) value);
				value = getCellValue(cell);
				setCellType(config.valueType, cell);				
			} catch (Exception e) {
				cell.setCellType(CellType.STRING);
			}
		} 
		setCellValue(cell, value);

		if (dataFormatIndex == -1){
			if (Double.class.isAssignableFrom(config.valueType)) {
				value = roundTo((double) getCellValue(cell, false), config);
				setCellType(Double.class, cell);
				setCellValue(cell, value);
			}
		}else{
			cell.getCellStyle().setDataFormat(dataFormatIndex);
		}
	}

	protected JSONArray getDataset(Config config, JSONObject datasets) throws Exception {
		JSONArray dataset = null;
		if (config.datasetId == null || config.datasetId.isEmpty()) {
			if (config.loopType != LoopType.ltLoop)
				return new JSONArray();

			if (datasets.length() != 1)
				throw new Exception("不能确定数据集！");
			dataset = datasets.getJSONArray(datasets.names().getString(0));
		} else {
			if (!datasets.has(config.datasetId))
				throw new Exception("未提供数据集[" + config.datasetId + "]!");
			dataset = datasets.getJSONArray(config.datasetId);
		}

		return dataset;
	}

	protected boolean incCommand(Command command, Template<Config> template) {
		Var var = template.commandRuntime.vars.get(command.varName);
		if (!Integer.class.isAssignableFrom(var.type))
			return false;

		if (!command.command.equals("inc"))
			return false;

		if (var.value == null)
			var.value = 0;
		var.value = (int) var.value + 1;

		return true;
	}

	protected boolean decCommand(Command command, Template<Config> template) {
		Var var = template.commandRuntime.vars.get(command.varName);
		if (!Integer.class.isAssignableFrom(var.type))
			return false;

		if (!command.command.equals("dec"))
			return false;

		if (var.value == null)
			var.value = 0;
		var.value = (int) var.value - 1;
		return true;
	}

	protected boolean setCommand(Command command, Object value, Template<Config> template) {
		Var var = template.commandRuntime.vars.get(command.varName);
		if (!command.command.equals("set"))
			return false;

		var.value = value;
		return true;
	}

	protected Object getCommand(Command command, Config config, Template<Config> template) {
		Var var = template.commandRuntime.vars.get(command.varName);
		if (!command.command.equals("get"))
			return null;
		return getValue(var.value, var.type, var.format, var.precision);
	}

	protected boolean rowCommand(Command command, Template<Config> template, int row) {
		Var var = template.commandRuntime.vars.get(command.varName);
		if (!command.command.equals("row"))
			return false;

		var.value = row;
		return true;
	}

	protected boolean colCommand(Command command, Template<Config> template, int col) {
		Var var = template.commandRuntime.vars.get(command.varName);
		if (!command.command.equals("col"))
			return false;

		var.value = col;
		return true;
	}

	protected boolean cellCommand(Command command, Template<Config> template, XSSFCell cell) {
		Var var = template.commandRuntime.vars.get(command.varName);
		if (!command.command.equals("col"))
			return false;

		var.value = getCellValue(cell, Date.class.isAssignableFrom(var.type));

		return true;
	}

	protected Object execCommand(String value, Template<Config> template, Config config) {
		if (config.replaceCommands.containsKey(value)) {
			Command command = config.replaceCommands.get(value);
			return getCommand(command, config, template);
		}

		return null;

	}

	protected void execCommand(Template<Config> template, Config config, XSSFCell cell) {
		for (Command command : config.commands) {
			if (incCommand(command, template)) {
				continue;
			}
			if (decCommand(command, template)) {
				continue;
			}
			if (setCommand(command, getCellValue(cell, Date.class.isAssignableFrom(config.valueType)), template)) {
				continue;
			}
			if (rowCommand(command, template, cell.getRowIndex())) {
				continue;
			}
			if (colCommand(command, template, cell.getColumnIndex())) {
				continue;
			}
			if (cellCommand(command, template, cell)) {
				continue;
			}
		}
	}

	// 如果配置项目设置了模板，恢复模板行的样式
	protected void restoreTemplateRow(Template<Config> template, Config config) {
		if (template.configTemplates.containsKey(config.templateId)) {
			ConfigItemTemplate configItemTemplate = template.configTemplates.get(config.templateId);
			RowInfo rowInfo = configItemTemplate.saveRowInfo;
			XSSFRow row = getRow(config.startRuntimeCellRow);
			restoreRow(rowInfo, row);
		}
	}

	int repeatCount = 0;

	protected void setStartYType(Template<Config> template, Config config) {
		switch (config.yType) {
		case ytCommand:
			config.startRuntimeCellRow = (int) execCommand(config.ycommand, template, config);
			break;
		case ytLoop:
		case ytStartY:
			config.startRuntimeCellRow = template.startY;
			break;
		case ytValue:
			config.startRuntimeCellRow = config.startY;
			break;
		case ytRef:
			Config ref = template.get(config.ref);
			config.startRuntimeCellRow = ref.endRuntimeCellRow;
			break;
		}

		config.endRuntimeCellRow = config.startRuntimeCellRow + 1;

	}

	protected boolean isRepeatConfig(Config config) {
		if (isEmpty(config.templateId) && isEmpty(config.ref))
			return false;
		return true;

	}

	/***
	 * 将jsonarray格式的数据导入到sheetName指定的sheet中
	 * 
	 * @param template
	 *            导入数据的模板定义
	 * @param dataset
	 *            要导入的数据，格式:[{key1:value1,key2:value2}]，每个对象为一行记录
	 * @param sheetName
	 *            要把数据导入的目标sheet名称， 如果不存在则创建
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public void execute(ExecuteParam executeParam) throws Exception {
		JSONObject datasets = (JSONObject) executeParam.paramObj[0];
		Template<Config> template = (Template<Config>) executeParam.paramObj[1];
		String sheetName = executeParam.sheetName;

		setSheet(sheetName);
		if (curSheet == null)
			insertSheet(sheetName);

		boolean needRemoveLastRow = false;
		// 初始化执行环境，在批量处理中仅初始化一次
		if (repeatCount == 0) {
			HashMap<Integer, CellRangeAddress> mergeMap = getOneRowRegions();

			// 初始模板，并保存
			TreeMap<Integer, Integer> sortRemoves = new TreeMap<>();
			for (ConfigItemTemplate configItemTemplate : template.configTemplates.values()) {
				XSSFRow row = getRow(configItemTemplate.row);
				configItemTemplate.saveRowInfo = saveRow(row, mergeMap);
				sortRemoves.put(configItemTemplate.row, configItemTemplate.row);
			}

			// 删除数据页中的模板行，降序删除
			for (Integer index : sortRemoves.descendingKeySet()) {
				if (!removeRow(index))
					needRemoveLastRow = true;
			}

			// 创建没有设置ref、template的配置项目所对应的单元格并保存行格式
			HashMap<Integer, RowInfo> saveRows = new HashMap<>();
			for (Config config : template.configs) {
				if (isRepeatConfig(config))
					continue;

				setStartYType(template, config);

				XSSFRow row = getRow(config.startRuntimeCellRow);
				if (!saveRows.containsKey(config.startRuntimeCellRow))
					saveRows.put(config.startRuntimeCellRow, saveRow(row, mergeMap));

				row.createCell(config.startX);
			}

			// 为上述单元格赋值，执行command
			for (Config config : template.configs) {
				if (isRepeatConfig(config))
					continue;

				int curExcelRowIndex = config.startRuntimeCellRow;
				JSONArray dataset = getDataset(config, datasets);

				setCellValue(template, config, dataset, curExcelRowIndex, 0);
				execCommand(template, config, getRow(curExcelRowIndex).getCell(config.startX));
			}

			// 恢复之前保存的行
			for (Integer rowIndex : saveRows.keySet()) {
				XSSFRow row = getRow(rowIndex);
				restoreRow(saveRows.get(rowIndex), row);
			}
		}

		HashMap<String, Boolean> datasetRowInserted = new HashMap<>();
		HashMap<Integer, XSSFRow> config_template_RowCreated = new HashMap<>();
		for (Config config : template.configs) {
			if (!isRepeatConfig(config))
				continue;

			setStartYType(template, config);

			if (config.loopType == LoopType.ltLoop) {
				JSONArray dataset = getDataset(config, datasets);
				config.endRuntimeCellRow = config.startRuntimeCellRow + dataset.length();
				if (!datasetRowInserted.containsKey(config.datasetId)) {
					datasetRowInserted.put(config.datasetId, true);
					for (int i = 0; i < dataset.length(); i++) {
						insertRow(config.startRuntimeCellRow, false);
						restoreTemplateRow(template, config);
					}
				}

			} else {
				if (!isEmpty(config.templateId)) {
					if (!config_template_RowCreated.containsKey(config.startRuntimeCellRow)) {
						insertRow(config.startRuntimeCellRow, false);
						restoreTemplateRow(template, config);
						config_template_RowCreated.put(config.startRuntimeCellRow, null);
					}
				}
			}
		}

		for (Config config : template.configs) {
			if (!isRepeatConfig(config))
				continue;

			int curExcelRowIndex = config.startRuntimeCellRow;
			JSONArray dataset = getDataset(config, datasets);

			switch (config.loopType) {
			case ltLoop:
				for (int i = 0; i < dataset.length(); i++) {
					setCellValue(template, config, dataset, curExcelRowIndex++, i);
					execCommand(template, config, getRow(curExcelRowIndex - 1).getCell(config.startX));
				}
				break;
			default:
				setCellValue(template, config, dataset, curExcelRowIndex,
						template.masterDatasetId != null && config.datasetId.equals(template.masterDatasetId)
								? repeatCount : 0);
				execCommand(template, config, getRow(curExcelRowIndex).getCell(config.startX));
				break;
			}
		}

		repeatCount++;
		
		if (needRemoveLastRow)
			curSheet.removeRow(getRow(curSheet.getLastRowNum()));
	}

}
