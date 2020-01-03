package wh.excel.parse;

import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import wh.excel.model.ExcelModel;
import wh.excel.model.JsonToExcelModel;
import wh.excel.template.CommandRuntime.Command;
import wh.excel.template.CommandRuntime.CommandType;
import wh.excel.template.CommandRuntime.Var;
import wh.excel.template.Config;
import wh.excel.template.Config.EntryInfo;
import wh.excel.template.Config.ExprType;
import wh.excel.template.Config.LoopType;
import wh.excel.template.Config.YType;
import wh.excel.template.ConfigItemTemplate;
import wh.excel.template.Template;

public abstract class TemplateParser<T extends Config> {
	// (data_sheet)[j,0],(type)[j,1],(x,[y])[j,2],(expr)[j,3],(id)[j,4],([dt])[j,5],(loop)[j,6],([ref])[j,7],([row])[j,8],([datasetid])[j,9]

	public static int COMMAND_INDEX = 0;// A
	public static int EXPR_TYPE_INDEX = 1;// B
	public static int START_XY_INDEX = 2;// C
	public static int EXPR_INDEX = 3;// D
	public static int ID_INDEX = 4;// E
	public static int VALUE_TYPE_INDEX = 5;// F
	public static int LOOP_INDEX = 6;// G
	public static int REF_INDEX = 7;// H
	public static int ROW_INDEX = 8;// I
	public static int DATASET_ID_INDEX = 9;// J
	public static int INSTRUCTION_INDEX = 10;// K
	public static int TEMPLATE_INDEX = 11;// L
	public static int MASTER_LOOP_INDEX = 12;// M
	public static int UNIQUE_GROUP_INDEX = 13;// N
	public static int NOT_NULL_INDEX = 14;// O

	protected int parseStartY(ExcelModel<T> model, Template<T> template, XSSFRow row) {
		XSSFCell cell = row.getCell(1);
		switch (ExcelModel.getCellType((XSSFCell) cell)) {
		case FORMULA:
			return (int) model.getExprValue(cell, false);
		case NUMERIC:
			return (int) cell.getNumericCellValue();
		case STRING:
			return Integer.parseInt(cell.getStringCellValue());
		default:
			return 0;
		}
	}

	protected void parseMasterDataset(ExcelModel<T> model, Template<T> template, XSSFRow row) {
		XSSFCell cell = row.getCell(1);
		template.masterDatasetId = model.getCellStringValue(cell);
	}

	protected void parseVarDefine(ExcelModel<T> model, Template<T> template, XSSFRow row) {
		XSSFCell cell = row.getCell(2);
		String typeName = model.getCellStringValue(cell);
		cell = row.getCell(1);
		String name = model.getCellStringValue(cell);
		Var var = new Var();
		if (row.getLastCellNum() > 2) {
			cell = row.getCell(3);
			Object value = model.getCellValue(cell);
			var.value = value;
		}
		switch (typeName.toLowerCase().trim()) {
		case "int":
			var.type = Integer.class;
			break;
		case "bool":
			var.type = Boolean.class;
			break;
		case "float":
			var.type = Float.class;
			cell = row.getCell(4);
			if (cell != null)
				var.precision = Integer.parseInt(model.getCellStringValue(cell));
			if (var.precision <= 0)
				var.precision = 4;
			break;
		case "date":
			var.type = Date.class;
			cell = row.getCell(4);
			if (cell != null)
				var.format = model.getCellStringValue(cell);
			if (var.format == null || var.format.isEmpty())
				var.format = "yyyy-MM-dd HH:mm:ss";
			break;
		case "string":
			var.type = String.class;
			break;
		}
		var.name = name;

		template.commandRuntime.vars.put(var.name, var);
	}

	public Template<T> parse(ExcelModel<T> model) throws Exception {
		Template<T> template = new Template<>();

		XSSFSheet sheet = model.getSheet();
		if (sheet == null)
			throw new Exception("解析模板失败，未指定当前页！");

		template.clear();
		for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
			XSSFRow row = sheet.getRow(i);
			if (row == null)
				throw new Exception("解析当前页【" + sheet.getSheetName() + "】失败，未发现指定的数据行【" + row + "】！");
			String typeName = row.getCell(COMMAND_INDEX).getStringCellValue();
			switch (typeName) {
			case Template.DATA_MAP_NAME:
				Config config = parse(template, model, row);
				template.add(config);
				break;
			case Template.DATA_SHEET_START_Y:
				int y = parseStartY(model, template, row);
				template.startY = y - 1;
				break;
			case Template.DATA_SHEET_MASTER_DATASET:
				parseMasterDataset(model, template, row);
				break;
			case Template.DATA_SHEET_VAR_DEFINE:
				parseVarDefine(model, template, row);
				break;
			case Template.DATA_SHEET_TEMPLATE:
				parseTemplate(model, template, row);
				break;
			}
		}

		return template;
	}

	protected Entry<String, Object> parseRowNumber(String line) {
		Pattern vr = Pattern.compile("([a-zA-Z0-9]+)\\[([a-zA-Z0-9]+)\\]");
		Matcher vm = vr.matcher(line);
		Object row = null;
		if (vm.find()) {
			row = vm.group(2);
			try {
				int rowIndex = Integer.parseInt((String) row);
				row = rowIndex;
			} catch (Exception e) {
			}
			line = vm.group(1);
		}
		return new SimpleEntry<String, Object>(line, row);
	}

	protected void parseExpr(Object line, T config) {
		if (!(line instanceof String))
			return;

		String pattern = "([#\\$@])\\{([a-zA-Z0-9\\[\\]]+)\\}";

		// 创建 Pattern 对象
		Pattern r = Pattern.compile(pattern);

		// 现在创建 matcher 对象
		Matcher m = r.matcher((String) line);
		while (m.find()) {
			String prex = m.group(1);
			String key = m.group(2);

			Entry<String, Object> rowInfo = parseRowNumber(key);

			key = rowInfo.getKey();
			Object value = rowInfo.getValue();
			if (value != null) {
				key = rowInfo.getKey() + "[" + rowInfo.getValue() + "]";
			}

			EntryInfo info = new EntryInfo(key, rowInfo.getKey(), rowInfo.getValue());
			if (prex.compareTo("$") == 0)
				config.remoteExprs.put(prex + "{" + key + "}", info);
			else if (prex.compareTo("#") == 0)
				config.localExprs.put(prex + "{" + key + "}", info);
			else if (prex.compareTo("@") == 0)
				config.localAtExprs.put(prex + "{" + key + "}", info);
		}
	}

	protected void parseCommand(XSSFRow row, T config) {
		XSSFCell cell = row.getCell(INSTRUCTION_INDEX);
		parseCommand(cell, config, CommandType.ctInstru);
		cell = row.getCell(START_XY_INDEX);
		parseCommand(cell, config, CommandType.ctXY);
		cell = row.getCell(EXPR_INDEX);
		parseCommand(cell, config, CommandType.ctExpr);
	}

	protected void parseCommand(XSSFCell cell, T config, CommandType commandType) {
		if (cell == null)
			return;

		String line = cell.getStringCellValue();
		String pattern = "([\\$]\\{([a-zA-Z0-9]+)\\(([a-zA-Z0-9\\-]+)[\\,]?(.*)\\)\\})";

		// 创建 Pattern 对象
		Pattern r = Pattern.compile(pattern);

		// 现在创建 matcher 对象
		Matcher m = r.matcher((String) line);
		while (m.find()) {
			Command command = new Command();
			command.commandType = commandType;
			command.replaceStr = m.group(1);
			command.command = m.group(2);
			command.varName = m.group(3);
			if (m.groupCount() > 4) {
				command.value = m.group(4);
				parseExpr(command.value, config);
			}
			config.commands.add(command);
			config.replaceCommands.put(command.replaceStr, command);
		}
	}

	protected void parseFirst(XSSFRow row, T config) {
	}

	protected void parseLast(XSSFRow row, T config) {
	}

	protected void parseExprType(XSSFRow row, T config) {
		String typename = row.getCell(EXPR_TYPE_INDEX).getStringCellValue();
		switch (typename) {
		case "key":
			config.exprType = ExprType.ttKey;
			break;
		case "expr":
			config.exprType = ExprType.ttExpr;
			break;
		default:
			config.exprType = ExprType.ttConst;
			break;
		}
	}

	protected void parseRef(XSSFRow row, T config) {
		String tmp = null;
		XSSFCell cell = row.getCell(REF_INDEX);
		if (cell != null) {
			tmp = cell.getStringCellValue();
			if (!tmp.isEmpty())
				config.ref = tmp;
			else
				config.ref = null;
		}
	}

	protected void parseLoop(XSSFRow row, T config) {
		XSSFCell cell = row.getCell(LOOP_INDEX);
		if (cell == null)
			return;

		String tmp = cell.getStringCellValue();
		if (!tmp.isEmpty() && tmp.trim().compareToIgnoreCase("loop") == 0) {
			config.loopType = LoopType.ltLoop;
		} else
			config.loopType = LoopType.ltOne;

	}

	protected void parseTemplate(ExcelModel<T> model, Template<T> template, XSSFRow row) {
		XSSFCell cell = row.getCell(1);
		String id = cell.getStringCellValue();
		cell = row.getCell(2);
		String tmp = model.getCellStringValue(cell);
		int rowIndex = Integer.parseInt(tmp) - 1;

		if (template.configTemplates.containsKey(id))
			return;

		ConfigItemTemplate configItemTemplate = new ConfigItemTemplate();
		configItemTemplate.name = id;
		configItemTemplate.row = rowIndex;

		template.configTemplates.put(id, configItemTemplate);
	}

	protected void parseStartXY(XSSFRow row, T config) {
		XSSFCell cell = row.getCell(START_XY_INDEX);
		config.startX = -1;
		config.startY = -1;

		if (cell == null)
			return;

		String tmp = cell.getStringCellValue();
		String[] tmps = tmp.trim().split(",");
		config.startX = JsonToExcelModel.excelToNum(tmps[0]);
		if (config.ref == null || config.ref.isEmpty()) {
			if (tmps.length > 1) {
				try {
					config.startY = Integer.parseInt(tmps[1]);
					config.yType = YType.ytValue;
				} catch (Exception e) {
					config.yType = YType.ytCommand;
					config.ycommand = tmps[1];
				}

			} else {
				config.startY = -1;
				if (config.loopType == LoopType.ltLoop)
					config.yType = YType.ytLoop;
				else
					config.yType = YType.ytStartY;
			}
		} else {
			config.yType = YType.ytRef;
		}
	}

	protected void parseId(XSSFRow row, T config) {
		config.id = row.getCell(ID_INDEX).getStringCellValue();
		if (config.id == null || config.id.isEmpty())
			config.id = UUID.randomUUID().toString();
	}

	protected void parseNotNull(XSSFRow row, T config) {
		XSSFCell cell = row.getCell(NOT_NULL_INDEX);
		if (cell == null)
			return;

		String value = cell.getStringCellValue();
		if (value == null || value.isEmpty())
			config.notNull = false;
		else
			config.notNull = value.compareToIgnoreCase("not null") == 0;
	}

	protected void parseUniqueGroup(XSSFRow row, T config) {
		XSSFCell cell = row.getCell(UNIQUE_GROUP_INDEX);
		if (cell == null)
			return;

		String value = cell.getStringCellValue();
		if (value == null || value.isEmpty())
			return;
		else
			config.uniqueGroup = value;
	}

	protected void parseValueType(XSSFRow row, T config) {
		String tmp = row.getCell(VALUE_TYPE_INDEX).getStringCellValue();
		String[] tmps = tmp.split(":");
		String valueType = tmps[0];
		switch (valueType.toLowerCase().trim()) {
		case "int":
			config.valueType = Integer.class;
			break;
		case "float":
			config.valueType = Double.class;
			config.precision = tmps.length > 1 && tmps[1] != null && !tmps[1].isEmpty() ? Integer.parseInt(tmps[1]) : 2;
			break;
		case "date":
			config.valueType = Date.class;
			config.format = tmps.length > 1 && tmps[1] != null && !tmps[1].isEmpty() ? tmps[1] : "yyy-MM-dd HH:mm:ss";
			break;
		case "boolean":
			config.valueType = Boolean.class;
			break;
		default:
			config.valueType = String.class;
			break;
		}
	}

	protected void parseExpr(ExcelModel<T> model, XSSFRow row, T config) {
		XSSFCell cell = row.getCell(EXPR_INDEX);
		if (cell == null)
			return;

		config.expr = model.getCellValue(cell, Date.class.isAssignableFrom(config.valueType));
		switch (config.exprType) {
		case ttExpr:
			parseExpr(config.expr, config);
			break;
		case ttKey:
			Entry<String, Object> rowInfo = parseRowNumber((String) config.expr);
			config.remoteDatasetMap.put(rowInfo.getKey(), (Integer) rowInfo.getValue());
			break;
		default:
			break;
		}
	}

	protected void parseConfigTemplate(ExcelModel<T> model, XSSFRow row, T config) {
		XSSFCell cell = row.getCell(TEMPLATE_INDEX);
		if (cell == null)
			return;

		config.templateId = model.getCellStringValue(cell);
	}

	protected void parseMasterLoop(ExcelModel<T> model, XSSFRow row, T config) {
		XSSFCell cell = row.getCell(MASTER_LOOP_INDEX);
		if (cell == null)
			return;

		String tmp = model.getCellStringValue(cell);
		config.masterLoop = tmp.toLowerCase().trim() != "one";
	}

	protected void parseRow(XSSFRow row, T config) {
		XSSFCell cell = row.getCell(ROW_INDEX);
		if (cell != null) {
			String tmp = cell.getStringCellValue();
			if (!tmp.isEmpty())
				config.row = Integer.parseInt(tmp);
			else
				config.row = 0;
		}
	}

	protected void parseDatasetId(XSSFRow row, T config) {
		XSSFCell cell = row.getCell(DATASET_ID_INDEX);
		if (cell != null) {
			config.datasetId = cell.getStringCellValue();
		}
	}

	protected abstract T newInstance(Template<T> template);

	protected Config parse(Template<T> template, ExcelModel<T> model, XSSFRow row) {
		// (data_sheet)[j,0],(type)[j,1],(x,[y])[j,2],(expr)[j,3],(id)[j,4],([dt])[j,5],(loop)[j,6],([row])[j,7]
		T config = newInstance(template);

		parseExprType(row, config);
		parseRef(row, config);
		parseId(row, config);
		parseLoop(row, config);
		parseRow(row, config);
		parseValueType(row, config);
		parseDatasetId(row, config);
		parseConfigTemplate(model, row, config);
		parseMasterLoop(model, row, config);

		parseFirst(row, config);

		parseCommand(row, config);

		parseStartXY(row, config);
		parseExpr(model, row, config);

		parseUniqueGroup(row, config);
		parseNotNull(row, config);

		parseLast(row, config);

		return config;
	}
}
