package com.wh.gaea.plugin.datasource;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.SwingTools;
import com.wh.tools.FileHelp;

import wh.excel.model.ExcelModel.ExecuteParam;
import wh.excel.model.SimpleJsonToExcelModel;

public class GridToExcel {
	
	public static void export(DefaultTableModel model){
		if (model == null){
			MsgHelper.showMessage("表格无数据！");
			return;
		}
		File file = SwingTools.selectSaveFile(null, GlobalInstance.instance().getProjectBasePath().getAbsolutePath(),
				"export.xlsx", "Excel文件=xls;xlsx");
		if (file == null)
			return;
		
		GridToExcel.export(file, model);
		MsgHelper.showMessage("成功导出!");
		try {
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	
	}
	
	public static void export(File file, DefaultTableModel model){
		JSONArray cols = new JSONArray();
		JSONArray rows = new JSONArray();
		
		for (int i = 0; i < model.getColumnCount(); i++) {
			JSONObject col = new JSONObject();
			col.put("field", model.getColumnName(i));
			col.put("name", model.getColumnName(i));
			cols.put(col);
		}
		
		for (int i = 0; i < model.getRowCount(); i++) {
			JSONObject row = new JSONObject();
			rows.put(row);
			
			for (int j = 0; j < model.getColumnCount(); j++) {
				row.put(model.getColumnName(j), model.getValueAt(i, j));
			}
			
		}
		SimpleJsonToExcelModel excelModel = new SimpleJsonToExcelModel();
		excelModel.load(null);
		ExecuteParam executeParam = new ExecuteParam();
		File path = file.getParentFile();
		JSONObject info = new JSONObject();
		info.put("name", FileHelp.removeExt(file.getName()));
		info.put("cols", cols);
		info.put("rows", rows);
		executeParam.paramObj = new Object[]{info, path};
		
		try {
			excelModel.execute(executeParam);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	
		excelModel.close();
		
	}
}
