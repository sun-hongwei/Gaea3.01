package wh.excel.model;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.json.JSONArray;
import org.json.JSONObject;

import wh.excel.objs.ValueType;
import wh.excel.template.Config;

public class SimpleJsonToExcelModel extends ExcelModel<Config> {

	public static final Font DEFAULTFONT = new Font("微软雅黑", Font.PLAIN, 14);
	public static final Color TEXTCOLOR = Color.BLACK;

	protected void setCellStyle(XSSFCell cell) throws Exception {
		XSSFCellStyle cellStyle = cell.getCellStyle();
		XSSFFont font = curBook.createFont();
		font.setFontName(DEFAULTFONT.getFontName());
		switch (DEFAULTFONT.getStyle()) {
		case Font.BOLD:
			font.setBold(true);
			break;
		case Font.ITALIC:
			font.setItalic(true);
			break;
		}

		font.setFontHeightInPoints((short) DEFAULTFONT.getSize());
		// Color textColor =
		// ColorConvert.toColorFromString(header.getString("textColor"));
		XSSFColor color = new XSSFColor(TEXTCOLOR, new DefaultIndexedColorMap());
		font.setColor(color);
		cellStyle.setFont(font);
		setBorder(cell);
		cell.setCellStyle(cellStyle);
	}

	protected void setCellValue(XSSFCell cell, Object value, boolean convertString, JSONObject booleanConvert,
			String dateFormat, String timeFormat, String datetimeFormat) throws Exception {

		ValueType valueType = ValueType.vtNone;
		if (value instanceof Double) {
			DecimalFormat df = new DecimalFormat("#.0000");
			value = df.format(value);
			valueType = ValueType.vtDouble;
		} else if (value instanceof Float) {
			DecimalFormat df = new DecimalFormat("#.00");
			value = df.format(value);
			valueType = ValueType.vtFloat;
		} else if (value instanceof Boolean) {
			valueType = ValueType.vtBoolean;
		} else if (value instanceof Date) {
			try {
				SimpleDateFormat formater = new SimpleDateFormat(datetimeFormat);
				value = formater.format(value);
			} catch (Exception e) {
				value = value.toString();
			}
			valueType = ValueType.vtDateTime;
		} else {
			valueType = ValueType.vtString;
			if (convertString) {
				if (value instanceof String) {
					String str = (String) value;
					if (str.toLowerCase().startsWith("http://")) {
						valueType = ValueType.vtImage;
					} else if (str.toLowerCase().matches("false|true")) {
						try {
							value = Boolean.parseBoolean((String) value);
							valueType = ValueType.vtBoolean;
						} catch (Exception e) {
						}
					} else if (str.matches("^[0-9].*")) {
						try {
							SimpleDateFormat formater = new SimpleDateFormat(datetimeFormat);
							formater.parse((String)value);
							valueType = ValueType.vtDateTime;
						} catch (Exception e) {
							try {
								SimpleDateFormat formater = new SimpleDateFormat(dateFormat);
								formater.parse((String)value);
								valueType = ValueType.vtDate;
							} catch (Exception e2) {
								try {
									SimpleDateFormat formater = new SimpleDateFormat(timeFormat);
									formater.parse((String)value);
									valueType = ValueType.vtTime;
								} catch (Exception e3) {
								}
							}
						}
					} else {
					}
				}
			}
		}

		switch (valueType) {
		case vtImage:
			addImage((String) value, cell.getRowIndex(), cell.getRowIndex() + 1, cell.getColumnIndex(),
					cell.getColumnIndex() + 1);
			break;
		case vtBoolean:
			if (booleanConvert != null) {
				String key = value.toString();
				if (booleanConvert.has(key)) {
					value = booleanConvert.get(key);
				}
			}
			setCellValue(cell, value);
			break;

		case vtDate: {
			setCellValue(cell, value);
			break;
		}
		case vtDateTime: {
			setCellValue(cell, value);
			break;
		}
		case vtTime: {
			setCellValue(cell, value);
			break;
		}
		default:
			setCellValue(cell, value);
			break;
		}
	}

	protected void addCell(JSONObject col, XSSFRow row, int index, Object value, JSONObject booleanConvert,
			boolean convertString, String dateFormat, String timeFormat, String datetimeFormat) throws Exception {
		XSSFCell cell = row.createCell(index);
		setCellType(String.class, cell);
		XSSFCellStyle cellStyle = curBook.createCellStyle();
		cellStyle.setShrinkToFit(true);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cell.setCellStyle(cellStyle);
		setCellStyle(cell);
		setCellValue(cell, value, convertString, booleanConvert, dateFormat, timeFormat, datetimeFormat);
	}

	@Override
	public void execute(ExecuteParam executeParam) throws Exception {
		JSONObject data = (JSONObject) executeParam.paramObj[0];
		JSONArray cols = data.getJSONArray("cols");
		JSONArray rows = data.getJSONArray("rows");
		String name = data.getString("name");
		String sheetName = data.has("sheetName") ? data.getString("sheetName") : null;
		File path = (File) executeParam.paramObj[1];
		JSONObject booleanConvert = data.has("booleanConvert") ? data.getJSONObject("booleanConvert") : null;
		boolean convertString = data.has("convertString") ? data.getBoolean("convertString") : false;
		String dateFormat = data.has("dateFormat") ? data.getString("dateFormat") : "yyyy-MM-dd";
		String timeFormat = data.has("timeFormat") ? data.getString("timeFormat") : "HH:mm:ss";
		String datetimeFormat = data.has("datetimeFormat") ? data.getString("datetimeFormat") : "yyyy-MM-dd HH:mm:ss";

		if (sheetName == null || sheetName.isEmpty())
			sheetName = name;

		name = name + ".xlsx";

		setSheet(sheetName);

		XSSFRow row = newRow(0);
		row.setHeightInPoints(70 * 3 / 4);
		for (int j = 0; j < cols.length(); j++) {
			JSONObject col = cols.getJSONObject(j);
			addCell(col, row, j, col.getString("name"), null, false, null, null, null);
			curSheet.autoSizeColumn(j);
			curSheet.setColumnWidth(j, curSheet.getColumnWidth(j) * 35 / 10);
		}

		for (int i = 0; i < rows.length(); i++) {
			row = newRow(i + 1);

			JSONObject rowdata = rows.getJSONObject(i);

			row.setHeightInPoints(30 * 3 / 4);

			for (int j = 0; j < cols.length(); j++) {
				JSONObject col = cols.getJSONObject(j);
				if(rowdata.has(col.getString("field"))){
					
					addCell(col, row, j, rowdata.get(col.getString("field")), booleanConvert, convertString, dateFormat,
							timeFormat, datetimeFormat);
				}else{
					addCell(col, row, j, "", booleanConvert, convertString, dateFormat,
							timeFormat, datetimeFormat);
				}
			}
		}

		// curSheet.setFitToPage(true);
		
		if (!path.exists())
			path.mkdirs();
		File file = new File(path, name);
		if (file.exists())
			file.delete();

		executeParam.result = file;
		saveAs(file);
	}

}
