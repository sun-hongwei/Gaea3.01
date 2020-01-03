package wh.excel.model;

import java.awt.Color;
import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.json.JSONArray;
import org.json.JSONObject;

import wh.excel.objs.EditorType;
import wh.excel.template.Config;
import wh.tools.ColorConvert;

public class ReportToExcelModel extends ExcelModel<Config> {

	protected void setCellStyle(XSSFCell cell, JSONObject header) throws Exception {
		XSSFCellStyle cellStyle = cell.getCellStyle();
		XSSFFont font = curBook.createFont();
		font.setFontName(header.getString("fontName"));
		switch (header.getInt("fontStyle")) {
		case 3:
		case 1:
			font.setBold(true);
			break;
		case 2:
			font.setItalic(true);
			break;
		}
		
		font.setFontHeightInPoints((short)header.getInt("fontSize"));
		Color textColor = ColorConvert.toColorFromString(header.getString("textColor"));
		XSSFColor color = new XSSFColor(
				textColor, 
				new DefaultIndexedColorMap());
		font.setColor(color);
		cellStyle.setFont(font);
		setBorder(cell);
		cell.setCellStyle(cellStyle);
	}
	
	@Override
	public void execute(ExecuteParam executeParam)
			throws Exception {
		JSONObject report_data = (JSONObject) executeParam.paramObj[0];
		JSONArray cells = report_data.getJSONArray("cells");
		JSONArray rows = report_data.getJSONArray("rows");
		JSONArray cols = report_data.getJSONArray("cols");
		String report_name = report_data.getString("name");
//		String report_id = report_data.getString("id");
		String filename = report_data.getString("filename");
		String sheetName = report_data.getString("sheetName");
		File path = (File)executeParam.paramObj[1];
		
		if (sheetName == null || sheetName.isEmpty())
			sheetName = report_name;

		if (filename == null || filename.isEmpty())
			filename = report_name + ".xlsx";
		else
			filename += ".xlsx";

		setSheet(sheetName);		

		int width = 0;
		for (int j = 0; j < cols.length(); j++) {
			JSONObject colData = cols.getJSONObject(j);
			width += colData.getInt("width");
		}

//		float scale = 1F;
//		if (width > 255){
//			scale = 255F / width;
//		}
//		
		for (int i = 0; i < rows.length(); i++) {
			XSSFRow row = newRow(i);

			JSONObject rowData = rows.getJSONObject(i);
			int height = rowData.getInt("height");
			row.setHeightInPoints(height * 3 / 4);
			
			for (int j = 0; j < cols.length(); j++) {
				XSSFCell cell = row.createCell(j);
				setCellType(String.class, cell);
				XSSFCellStyle cellStyle = curBook.createCellStyle();
				cellStyle.setAlignment(HorizontalAlignment.CENTER);
				cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
				cell.setCellStyle(cellStyle);
			}
		}

		for (int j = 0; j < cols.length(); j++) {
			JSONObject colData = cols.getJSONObject(j);
			width = colData.getInt("width");
//			int charWidth = getCell(j, 0).getCellStyle().getFont().getFontHeightInPoints();
//			curSheet.setColumnWidth(j, (int)((width / charWidth + (width % charWidth > 0 ? 1 : 0)) * 256));
			curSheet.setColumnWidth(j, (int)(width * 37.04));

//			curSheet.setColumnWidth(j, (int)((width * scale + 0.72) * 256));
		}

		for (Object obj : cells) {
			JSONObject cellInfo = (JSONObject) obj;
			int startRow = cellInfo.getInt("startRow");
			int endRow = cellInfo.getInt("endRow");
			int startCol = cellInfo.getInt("startCol");
			int endCol = cellInfo.getInt("endCol");
			
			int typecode = cellInfo.getJSONObject("editor"). getInt("type");
			JSONObject header = cellInfo.getJSONObject("editor").getJSONObject("data");
			
			Object value = "";
			if (cellInfo.has("value"))
				value = cellInfo.get("value");
			
			if (value instanceof Double) {
				DecimalFormat df = new DecimalFormat("#.0000");
				value = df.format(value);
			} else if (value instanceof Float) {
				DecimalFormat df = new DecimalFormat("#.00");
				value = df.format(value);
			} else if (value instanceof Boolean) {
				DecimalFormat df = new DecimalFormat("#.00");
				value = df.format(value);
			}

			if (endRow - startRow > 1 || endCol - startCol > 1) {
				CellRangeAddress newCellRangeAddress = new CellRangeAddress(startRow, endRow - 1, startCol, endCol - 1);
				curSheet.addMergedRegion(newCellRangeAddress);
			}

			XSSFCell cell = getCell(startCol, startRow);

			setCellStyle(cell, header);
			switch (typecode) {
			case EditorType.Chart_Type:
				addImageDataUrl((String) value, startRow, endRow, startCol, endCol);
				break;
			case EditorType.IMAGE_TYPE:
				addImage((String) value, startRow, endRow, startCol, endCol);
				break;
			case EditorType.CHECKBOX_TYPE:
				if (value instanceof String){
					if (value.toString().trim().toLowerCase().equals("true"))
						value = true;
					else
						value = false;
				}else if (value instanceof Number){
					value = Integer.parseInt((String) value) != 0;
				}else {
					value = false;
				}
				setCellValue(cell, value);
				break;

			case EditorType.DATE_TYPE:
				if (value instanceof Date) {
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					value = format.format((Date) value);
				}
				setCellValue(cell, value);
				break;
			case EditorType.DATE_TIME:
				if (value instanceof Date) {
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					value = format.format((Date) value);
				}
				setCellValue(cell, value);
				break;
			case EditorType.TIME_TYPE:
				if (value instanceof Date) {
					SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
					value = format.format((Date) value);
				}
				setCellValue(cell, value);
				break;
			default:
				setCellValue(cell, value);
				break;
			} 
			
			
		}
		
		for (CellRangeAddress cellRangeAddress : curSheet.getMergedRegions()) {
			setBorder(cellRangeAddress);
		}
		
//		curSheet.setFitToPage(true);

		if (!path.exists())
			path.mkdirs();
		File file = new File(path, filename);
		if (file.exists())
			file.delete();
		
		executeParam.result = file;
		saveAs(file);
	}

}
