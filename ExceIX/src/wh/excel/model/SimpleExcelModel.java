package wh.excel.model;

import java.io.File;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.json.JSONArray;
import org.json.JSONObject;

import wh.excel.template.Config;

public class SimpleExcelModel extends ExcelModel<Config>{

	@SuppressWarnings("unchecked")
	@Override
	public void execute(ExecuteParam executeParam) throws Exception {
		File file = (File) executeParam.paramObj[0];
		JSONArray dataset = (JSONArray) executeParam.paramObj[1];
		Map<String, Integer> indexs = (Map<String, Integer>)executeParam.paramObj[2];
		
		int max = 0;
		for (Integer index : indexs.values()) {
			if (max < index)
				max = index;
		}
		
		max++;
		
		load(file);
		setSheet(executeParam.sheetName);
		XSSFSheet sheet = getSheet();
		if (sheet == null)
			throw new RuntimeException("sheet[" + executeParam.sheetName + "] not found!");
		
		int rowindex = 0;
		for (Object object : dataset) {
			JSONObject row = (JSONObject)object;
			XSSFRow cellRow = getRow(rowindex++);
			if (cellRow == null)
				cellRow = newRow(rowindex - 1);

			for (int i = 0; i < max; i++) {
				cellRow.createCell(i);
			}
			
			for (Object object2 : row.names()) {
				String key = (String)object2;
				int col = indexs.get(key);
				
				XSSFCell cell = cellRow.getCell(col);
				setCellType(String.class, cell);
				setCellValue(cell, row.get(key));
			}
		}
		
		saveAs(file);
	}

}
