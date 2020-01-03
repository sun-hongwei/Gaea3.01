package wh.excel.parse;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import wh.excel.model.ExcelModel;
import wh.excel.model.SimpleExcelModel;
import wh.excel.model.ExcelModel.ExecuteParam;
import wh.excel.template.Config;
import wh.excel.template.Template;
import wh.excel.template.CommandRuntime.Command;
import wh.excel.template.CommandRuntime.CommandType;
import wh.excel.template.CommandRuntime.Var;
import wh.excel.template.Config.ExprType;
import wh.excel.template.Config.LoopType;
import wh.excel.template.ConfigItemTemplate;

public class DefaultTemplateParser<T> extends TemplateParser<Config> {

	public static final String EXPR_VAR = "expr";
	public static final String EXPR_KEY = "key";
	public static final String EXPR_CONST = "const";

	@Override
	protected Config newInstance(Template<Config> template) {
		return new Config(template);
	}

	public static String exprTypeToString(ExprType exprType) {
		switch (exprType) {
		case ttConst:
			return EXPR_CONST;
		case ttExpr:
			return EXPR_VAR;
		case ttKey:
			return EXPR_KEY;
		}

		return null;
	}

	public static ExprType stringToExprType(String exprTypeString) {
		switch (exprTypeString) {
		case EXPR_VAR:
			return ExprType.ttExpr;
		case EXPR_CONST:
			return ExprType.ttConst;
		case EXPR_KEY:
			return ExprType.ttKey;
		}
		return ExprType.ttExpr;
	}

	public static String classToString(Class<?> c) {
		if (Boolean.class.isAssignableFrom(c)) {
			return "boolean";
		} else if (Integer.class.isAssignableFrom(c)) {
			return "int";
		} else if (Float.class.isAssignableFrom(c) || Double.class.isAssignableFrom(c)) {
			return "float";
		} else if (Date.class.isAssignableFrom(c)) {
			return "date";
		} else {
			return "string";
		}
	}

	public static Class<?> stringToClass(String className) {
		switch (className.toLowerCase()) {
		case "int":
			return Integer.class;
		case "boolean":
			return Boolean.class;
		case "float":
			return Float.class;
		case "date":
			return Date.class;
		case "string":
		default:
			return String.class;
		}
	}

	public static Template<Config> getTemplate(ExcelModel<Config> model) throws Exception {
		DefaultTemplateParser<Config> parser = new DefaultTemplateParser<>();
		return parser.parse(model);
	}

	public static class DatasetInfo {
		public JSONArray dataset = new JSONArray();
		public Map<String, Integer> sortMap = new HashMap<String, Integer>();
	}

	public static void saveTemplate(Template<Config> template, String sheetName, File file) throws Exception {
		DatasetInfo dataset = templateToDataset(template);
		ExcelModel<Config> model = new SimpleExcelModel();
		ExecuteParam executeParam = new ExecuteParam();
		executeParam.sheetName = sheetName;
		executeParam.paramObj = new Object[] { file, dataset.dataset, dataset.sortMap };
		model.execute(executeParam);
	}

	public static DatasetInfo templateToDataset(Template<Config> template) {
		DatasetInfo info = new DatasetInfo();

		info.sortMap.put("COMMAND_INDEX", COMMAND_INDEX);
		info.sortMap.put("EXPR_TYPE_INDEX", EXPR_TYPE_INDEX);
		info.sortMap.put("START_XY_INDEX", START_XY_INDEX);
		info.sortMap.put("EXPR_INDEX", EXPR_INDEX);
		info.sortMap.put("ID_INDEX", ID_INDEX);
		info.sortMap.put("VALUE_TYPE_INDEX", VALUE_TYPE_INDEX);
		info.sortMap.put("LOOP_INDEX", LOOP_INDEX);
		info.sortMap.put("REF_INDEX", REF_INDEX);
		info.sortMap.put("ROW_INDEX", ROW_INDEX);
		info.sortMap.put("DATASET_ID_INDEX", DATASET_ID_INDEX);
		info.sortMap.put("INSTRUCTION_INDEX", INSTRUCTION_INDEX);
		info.sortMap.put("TEMPLATE_INDEX", TEMPLATE_INDEX);
		info.sortMap.put("MASTER_LOOP_INDEX", MASTER_LOOP_INDEX);
		info.sortMap.put("UNIQUE_GROUP_INDEX", UNIQUE_GROUP_INDEX);
		info.sortMap.put("NOT_NULL_INDEX", NOT_NULL_INDEX);

		JSONObject row = new JSONObject();
		row.put("COMMAND_INDEX", Template.DATA_SHEET_START_Y);
		row.put("EXPR_TYPE_INDEX", template.startY + 1);
		info.dataset.put(row);

		if (template.masterDatasetId != null && !template.masterDatasetId.isEmpty()) {
			row = new JSONObject();
			row.put("COMMAND_INDEX", Template.DATA_SHEET_MASTER_DATASET);
			row.put("EXPR_TYPE_INDEX", template.masterDatasetId);
			info.dataset.put(row);
		}

		for (Var var : template.commandRuntime.vars.values()) {
			row = new JSONObject();
			String typename = classToString(var.type);
			row.put("COMMAND_INDEX", Template.DATA_SHEET_VAR_DEFINE);
			row.put("EXPR_TYPE_INDEX", var.name);
			row.put("START_XY_INDEX", typename);
			row.put("EXPR_INDEX", var.value);
			if (typename.equalsIgnoreCase("float"))
				row.put("ID_INDEX", var.precision);
			else if (typename.equalsIgnoreCase("date"))
				row.put("ID_INDEX", var.format);
			info.dataset.put(row);
		}

		for (ConfigItemTemplate configItemTemplate : template.configTemplates.values()) {
			row = new JSONObject();
			row.put("COMMAND_INDEX", Template.DATA_SHEET_TEMPLATE);
			row.put("EXPR_TYPE_INDEX", configItemTemplate.name);
			row.put("START_XY_INDEX", configItemTemplate.row + 1);
			info.dataset.put(row);
		}

		for (Config config : template.configs) {
			row = new JSONObject();
			row.put("COMMAND_INDEX", Template.DATA_MAP_NAME);
			row.put("EXPR_TYPE_INDEX", exprTypeToString(config.exprType));

			String x = ExcelModel.NumToExcel(config.startX);
			String y = "";
			if (config.startY >= 0)
				y = ExcelModel.NumToExcel(config.startY);
			if (y == null || y.isEmpty())
				if (config.ycommand != null && !config.ycommand.isEmpty())
					y = config.ycommand;

			row.put("START_XY_INDEX", (y == null || y.isEmpty()) ? x : (x + "," + y));
			row.put("EXPR_INDEX", config.expr);
			row.put("ID_INDEX", config.id);
			row.put("VALUE_TYPE_INDEX", classToString(config.valueType));
			row.put("LOOP_INDEX", config.loopType == LoopType.ltLoop ? "loop" : "");
			row.put("REF_INDEX", config.ref);
			row.put("ROW_INDEX", config.row == 0 ? "" : config.row);
			row.put("DATASET_ID_INDEX", config.datasetId);
			row.put("TEMPLATE_INDEX", config.templateId);
			row.put("MASTER_LOOP_INDEX", config.masterLoop ? "" : "false");
			row.put("UNIQUE_GROUP_INDEX", config.uniqueGroup);
			row.put("NOT_NULL_INDEX", config.notNull ? "true" : "");
			String commands = null;
			for (Command command : config.commands) {
				if (command.commandType != CommandType.ctInstru)
					continue;
				
				String commandString = "${" + command.command + "[" + command.varName
						+ (command.value == null ? "" : ("," + command.value.toString())) + "]}";
				if (commands == null)
					commands = commandString;
				else {
					commands += commandString;
				}
			}
			row.put("INSTRUCTION_INDEX", commands);
			info.dataset.put(row);
		}

		return info;
	}
}
