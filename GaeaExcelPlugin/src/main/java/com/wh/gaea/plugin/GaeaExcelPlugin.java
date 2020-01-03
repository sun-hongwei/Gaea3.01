package com.wh.gaea.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.interfaces.LinkInfo;
import com.wh.gaea.interfaces.selector.IExcelSelector;
import com.wh.gaea.plugin.excel.DBToExcelDialog;
import com.wh.gaea.plugin.excel.ExcelToDBDialog;
import com.wh.swing.tools.MsgHelper;

import wh.excel.simple.interfaces.IExcelModel.ExportResultInfo;
import wh.excel.simple.model.ExcelSwitchModel;

public class GaeaExcelPlugin extends BaseGaeaPlugin implements IGaeaPlugin, IExcelSelector{

	@Override
	public void setMenu(JMenu root) {
		getRootMenu(root);
		rootMenu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/menubar/数据交互.png")));

		JMenuItem menu = new JMenuItem("导出数据到Excel");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/tool/导出数据到Excel.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				DBToExcelDialog.show(GlobalInstance.instance().getMainControl());
			}
		});
		rootMenu.add(menu);

		menu = new JMenuItem("从Excel导入数据");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/tool/从Excel导入数据.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				ExcelToDBDialog.show(GlobalInstance.instance().getMainControl());
			}
		});
		rootMenu.add(menu);

	}

	@Override
	public void reset() {
	}

	@Override
	public int getLoadOrder() {
		return 0;
	}

	@Override
	public void showExcelToDBView() {
		ExcelToDBDialog.show(GlobalInstance.instance().getMainControl());
	}

	@Override
	public void importToDB(List<LinkInfo> infos) throws Exception {
		ExcelToDBDialog.execute(GlobalInstance.instance().getMainControl().getDB(), infos);
	}
	
	@Override
	public void importToDB(LinkInfo info) throws Exception {
		ExcelToDBDialog.execute(GlobalInstance.instance().getMainControl().getDB(), info);
	}
	
	@Override 
	public JSONObject importToDataset(File templateFile, File dataFile) throws Exception {
		ExcelSwitchModel swithMode = new ExcelSwitchModel();
		ExportResultInfo result = swithMode.exportTo(templateFile, dataFile, "config", "data", true);
		return (JSONObject) result.data;
	}
	
	@Override 
	public void exportToExcel(File templateFile, File saveFile, JSONObject dataset) throws Exception {
		ExcelSwitchModel switchMode = new ExcelSwitchModel();
		switchMode.importFrom(templateFile, templateFile, saveFile, "config", "data", dataset);
	}
	
	@Override
	public PlugInType getType() {
		return PlugInType.ptExcel;
	}

	@Override
	protected String getMenuRootName() {
		return "数据交互";
	}

	@Override
	public void changeImportData(File templatelFile, File dataFile, String dataSheetName, JSONArray dataset) throws Exception {
		ExcelSwitchModel switchMode = new ExcelSwitchModel();
		switchMode.changeImportData(templatelFile, dataFile, dataSheetName, dataset);
	}

}
