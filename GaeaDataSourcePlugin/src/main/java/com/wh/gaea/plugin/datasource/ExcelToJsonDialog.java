package com.wh.gaea.plugin.datasource;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.datasource.DataSource;
import com.wh.gaea.interfaces.IEditorEnvironment;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.SwingTools;

import wh.excel.simple.interfaces.IExcelModel.ExportResultInfo;
import wh.excel.simple.model.ExcelSwitchModel;

public class ExcelToJsonDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();

	public JSONObject execute(File file) throws Exception {
		ExcelSwitchModel excelSwitchModel = new ExcelSwitchModel();
		String configName = txtConfig.getText();
		if (configName == null || configName.isEmpty())
			configName = "config";

		String dataName = txtData.getText();
		if (dataName == null || dataName.isEmpty())
			dataName = "data";

		ExportResultInfo result = excelSwitchModel.exportTo(file, file, configName, dataName, true);
		return (JSONObject)result.data;
	}

	private JTextField txtData;
	private JTextField txtConfig;
	private JButton button_6;

	protected JSONObject data;

	/**
	 * Create the dialog.
	 */
	public ExcelToJsonDialog() {
		setTitle("从Excel导入");
		setIconImage(Toolkit.getDefaultToolkit().getImage(ExcelToJsonDialog.class.getResource("/image/browser.png")));
		setBounds(100, 100, 325, 216);

		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		JLabel label = new JLabel(" 数据页 ");
		label.setBounds(75, 36, 44, 17);
		contentPanel.add(label);
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		txtData = new JTextField();
		txtData.setBounds(119, 33, 100, 23);
		contentPanel.add(txtData);
		txtData.setMaximumSize(new Dimension(100, 2147483647));
		txtData.setText("data");
		txtData.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		txtData.setColumns(10);

		txtConfig = new JTextField();
		txtConfig.setBounds(119, 68, 100, 23);
		contentPanel.add(txtConfig);
		txtConfig.setMaximumSize(new Dimension(100, 2147483647));
		txtConfig.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		txtConfig.setText("config");
		txtConfig.setColumns(10);

		JLabel label_1 = new JLabel(" 配置页 ");
		label_1.setBounds(75, 71, 44, 17);
		contentPanel.add(label_1);
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		button_6 = new JButton(" 导入 ");
		button_6.setBounds(119, 130, 100, 23);
		contentPanel.add(button_6);
		button_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = SwingTools.selectOpenFile(null,
						GlobalInstance.instance().getProjectPath(IEditorEnvironment.Export_Dir_Name).getAbsolutePath(), null,
						"excel导入模板文件=xls;xlsx");
				
				if (file == null) {
					return;
				}

				try {
					data = execute(file);
					if (data != null) {
						setVisible(false);
						return;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		setLocationRelativeTo(null);
	}

	public static JSONObject showDialog() {
		ExcelToJsonDialog dialog = new ExcelToJsonDialog();
		dialog.setModal(true);
		dialog.setVisible(true);
		JSONObject result = dialog.data;
		dialog.dispose();
		return result;
	}
	
	public static DefaultTableModel getModel(DataSource ds){
		if (ds == null)
			return null;
		
		JSONObject result = showDialog();
		
		if (result == null || result.length() == 0)
			return new DefaultTableModel();
		
//		HashMap<Object, Object> cols = new HashMap<>();
//		for (Object object : data) {
//			JSONObject row = (JSONObject)object;
//			for (Object field : row.names()) {
//				if (!cols.containsKey(field)){
//					cols.put(field, field);
//				}
//			}
//			
//		}
//
//		Object[][] rows = new Object[data.length()][cols.size()];
//		for (int i = 0; i < rows.length; i++) {
//			int index = 0;
//			JSONObject row = data.getJSONObject(i);
//			for (Object field : cols.keySet()) {
//				rows[i][index++] = row.get((String)field);
//			}
//			index++;
//		}
		
		try {
			
			JSONObject columns = new JSONObject();
			for (Object obj : result.getJSONArray("header")) {
				JSONObject field = (JSONObject)obj;
				JSONObject column = new JSONObject();
				column.put(DataSource.COLUMN_FIELD, field.get("id"));
				column.put(DataSource.COLUMN_NAME, field.get("name"));
				columns.put(field.getString("id"), column);
			}
			result.put("column", columns);
			ds.loadDataset(result);
			return ds;
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
			return null;
		}
		
	}
}
