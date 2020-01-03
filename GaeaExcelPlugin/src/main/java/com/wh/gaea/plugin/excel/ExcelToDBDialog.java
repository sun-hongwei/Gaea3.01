package com.wh.gaea.plugin.excel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.interfaces.IEditorEnvironment;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.gaea.interfaces.LinkInfo;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.SwingTools;
import com.wh.tools.JsonHelp;

import wh.excel.simple.interfaces.IExcelModel.ExportResultInfo;
import wh.excel.simple.model.ExcelSwitchModel;
import wh.excel.template.Config;
import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.Operation;
import wh.interfaces.ISqlBuilder.SqlType;

public class ExcelToDBDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();

	private boolean isEdit = false;

	protected static void checkUniqueRow(HashMap<String, String> fieldsMap, HashMap<String, List<Config>> uniqueMap,
			HashMap<String, JSONObject> uniqueRowMap, JSONObject row, String field) throws Exception {
		if (fieldsMap.containsKey(field)) {
			String uniqueGroup = fieldsMap.get(field);
			List<Config> configs = uniqueMap.get(uniqueGroup);
			String key = null;
			JSONArray fields = new JSONArray();
			JSONArray values = new JSONArray();
			for (Config config : configs) {
				String uniqueField = config.expr.toString().trim().toUpperCase();
				Object value = row.get(uniqueField);
				if (value == null) {
					value = "";
				}
				if (key == null) {
					key = value.toString();
				} else {
					key += value.toString();
				}
				fields.put(config.expr.toString());
				values.put(value);
			}

			if (uniqueRowMap.containsKey(key)) {
				throw new Exception("当前导入字段" + fields.toString() + "设置唯一，但当前值" + values.toString() + "已经存在！");
			} else {
				uniqueRowMap.put(key, row);
			}
		}
	}

	protected static void checkNotNull(JSONObject row, String field, HashMap<String, Config> notNullMap)
			throws Exception {
		if (notNullMap.containsKey(field)) {
			Object value = row.get(field);
			if (value == null || value.toString().isEmpty()) {
				throw new Exception("当前导入字段【" + field + "】设置必须不为null，但当前值" + row.toString() + "中此值为null！");
			}
		}
	}

	public void execute() throws Exception {
		DefaultListModel<LinkInfo> model = (DefaultListModel<LinkInfo>) list.getModel();
		if (model.getSize() == 0)
			return;

		List<LinkInfo> linkInfos = new ArrayList<>();
		for (int i = 0; i < model.getSize(); i++) {
			LinkInfo info = model.getElementAt(i);
			linkInfos.add(info);
		}
		try {
			execute(mainControl.getDB(), linkInfos);
			list.updateUI();
		} catch (Exception e) {
			list.updateUI();
			throw e;
		}
	}
	
	public static void execute(IDBConnection db, List<LinkInfo> linkInfos) throws Exception {
		List<LinkInfo> losts = new ArrayList<>();
		if (linkInfos.size() == 0)
			return;

		db.beginTran();
		try {
			for (LinkInfo info : linkInfos) {
				execute(db, info);
				losts.add(info);
			}
			db.commitTran();
		} catch (Exception e) {
			for (LinkInfo linkInfo : losts) {
				linkInfo.count = -1;
			}
			db.rollbackTran();
			e.printStackTrace();
			throw e;
		}
	}

	public static void execute(IDBConnection db, LinkInfo info) throws Exception {
		ExcelSwitchModel excelSwitchModel = new ExcelSwitchModel();

		ExportResultInfo resultInfo = null;
		if (info.templateFile != null) {
			if (!info.templateFile.exists()) {
				throw new IOException("模板文件[" + info.templateFile.getAbsolutePath() + "]不存在！");
			}

			resultInfo = excelSwitchModel.exportTo(info.templateFile, info.excelFile, info.configSheetName,
					info.dataSheetName, false);
		} else {
			resultInfo = excelSwitchModel.exportTo(info.excelFile, info.configSheetName, info.dataSheetName, false);
		}

		JSONArray rows = (JSONArray) resultInfo.data;
		if (rows != null) {
			HashMap<String, String> fieldMap = new HashMap<>();// 字段列表
			HashMap<String, String> fieldsMap = new HashMap<>();// 字段与唯一组映射
			HashMap<String, List<Config>> uniqueMap = new HashMap<>();// 唯一组与配置列表映射
			HashMap<String, Config> notNullMap = new HashMap<>();// 不能为空的字段列表
			for (Config config : resultInfo.template.configs) {
				boolean needUniqueGroup = config.uniqueGroup != null && !config.uniqueGroup.isEmpty();
				String uniqueGroup = null;
				if (needUniqueGroup)
					uniqueGroup = config.uniqueGroup.trim().toUpperCase();
				String field = config.expr.toString().trim().toUpperCase();
				if (uniqueGroup != null && !uniqueGroup.isEmpty()) {
					List<Config> map;
					if (uniqueMap.containsKey(uniqueGroup)) {
						map = uniqueMap.get(uniqueGroup);
					} else {
						map = new ArrayList<>();
						uniqueMap.put(uniqueGroup, map);
					}
					fieldsMap.put(field, uniqueGroup);
					map.add(config);
				}

				if (config.notNull) {
					notNullMap.put(field, config);
				}
			}

			HashMap<String, JSONObject> uniqueRowMap = new HashMap<String, JSONObject>();
			for (Object object : rows) {
				JSONObject row = (JSONObject) object;
				for (Object key : row.names()) {
					String field = (String) key;
					field = field.trim().toUpperCase();
					fieldMap.put(field, field);

					checkUniqueRow(fieldsMap, uniqueMap, uniqueRowMap, row, field);

					checkNotNull(row, field, notNullMap);
				}
			}

			ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
			for (String field : fieldMap.values()) {
				sqlBuilder.addField(field);
			}
			sqlBuilder.addTable(info.tableName);
			sqlBuilder.addWhere("1", Operation.otEqual, new Object[] { 2 });
			sqlBuilder.setSqlType(SqlType.stQuery);
			IDataset dataset = db.query(sqlBuilder);
			for (Object object : rows) {

				JSONObject row = (JSONObject) object;
				IRow rowData = dataset.newRow();
				for (Object key : row.names()) {
					String field = (String) key;
					field = field.trim();
					fieldMap.put(field.toUpperCase(), field);
					Object value = row.get((String) key);
					if (value == null)
						continue;

					if (value instanceof String) {
						String strValue = (String) value;
						if (strValue.trim().isEmpty())
							continue;

						if (strValue.toLowerCase().trim().equals("null"))
							continue;
					}
					rowData.setValue(field.toLowerCase(), value);
				}

				dataset.addRow(rowData);
			}

			dataset.post(db);
			info.count = rows.length();
		}

	}

	public void saveConfig() {
		DefaultListModel<LinkInfo> model = (DefaultListModel<LinkInfo>) list.getModel();

		if (model.getSize() == 0) {
			MsgHelper.showMessage("请至少添加一条配置信息后再试！");
			return;
		}

		if (this.saveFile == null) {
			this.saveFile = getSaveFile();
		}

		if (saveFile == null)
			return;

		JSONArray rows = new JSONArray();
		for (int i = 0; i < model.getSize(); i++) {
			rows.put(model.getElementAt(i).toJson());
		}

		try {
			JsonHelp.saveJson(saveFile, rows, null);
			MsgHelper.showMessage("保存配置信息成功！");
			isEdit = false;
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}

	public void loadConfig() {
		File file = SwingTools.selectOpenFile(null, null, null,
				"excel导入模板=" + IEditorEnvironment.Excel_Import_DB_File_Extension);

		if (file == null)
			return;

		try {
			JSONArray rows = (JSONArray) JsonHelp.parseCacheJson(file, null);
			DefaultListModel<LinkInfo> model = new DefaultListModel<>();
			for (int i = 0; i < rows.length(); i++) {
				LinkInfo info = new LinkInfo(rows.getJSONObject(i));
				model.addElement(info);
			}

			list.setModel(model);

			this.saveFile = file;
			isEdit = false;

		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}

	File saveFile;
	IMainControl mainControl;
	private JList<LinkInfo> list;
	private JComboBox<String> tableNameView;
	private JTextField txtData;
	private JTextField txtConfig;
	private JComboBox<File> templateFiles;
	private JButton button_6;

	public void initTableNames() {
		List<String> ts = mainControl.getDB().getTables();
		tableNameView.setModel(new DefaultComboBoxModel<>(ts.toArray(new String[ts.size()])));
		if (tableNameView.getItemCount() > 0) {
			tableNameView.setSelectedIndex(0);
		}
		tableNameView.updateUI();
	}

	public void addConfig() {
		File[] files = SwingTools.selectOpenFiles(null, null, null, "excel导入配置文件=xls;xlsx");
		if (files != null) {
			DefaultListModel<LinkInfo> model = (DefaultListModel<LinkInfo>) list.getModel();
			for (File file : files) {
				LinkInfo info = new LinkInfo(file, null);
				model.addElement(info);
			}
			isEdit = true;
			list.updateUI();
		}
	}

	public void removeConfig() {
		if (list.getSelectedValue() == null) {
			return;
		}

		if (MsgHelper.showConfirmDialog("是否删除选定的配置项目？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		((DefaultListModel<LinkInfo>) list.getModel()).removeElementAt(list.getSelectedIndex());

		isEdit = true;

	}

	public void save() {
		if (list.getSelectedValue() == null) {
			MsgHelper.showMessage("请先选择一个配置项目!");
			return;
		}

		if (tableNameView.getSelectedItem() == null) {
			MsgHelper.showMessage("请先选择一个表!");
			return;
		}

		List<LinkInfo> infos = list.getSelectedValuesList();
		for (LinkInfo info : infos) {
			save(info);
		}

		isEdit = true;

		list.updateUI();
	}

	public void save(LinkInfo info) {
		info.tableName = tableNameView.getSelectedItem().toString();
		info.configSheetName = txtConfig.getText();
		info.configSheetName = info.configSheetName == null || info.configSheetName.isEmpty() ? "config"
				: info.configSheetName;
		info.dataSheetName = txtData.getText();
		info.dataSheetName = info.dataSheetName == null || info.dataSheetName.isEmpty() ? "data" : info.dataSheetName;
		info.templateFile = (File) templateFiles.getSelectedItem();
	}

	public void load() {
		if (list.getSelectedValue() == null) {
			return;
		}

		LinkInfo info = list.getSelectedValue();

		tableNameView.setSelectedItem(info.tableName);
		if (info.templateFile == null)
			templateFiles.setSelectedItem(null);
		else {
			File selectFile = null;
			for (int i = 0; i < templateFiles.getItemCount(); i++) {
				File file = templateFiles.getItemAt(i);
				if (file == null)
					continue;

				if (file.equals(info.templateFile)) {
					selectFile = file;
					break;
				}
			}

			if (selectFile == null) {
				if (templateFiles.getItemCount() == 0)
					templateFiles.addItem(null);
				templateFiles.addItem(info.templateFile);
			}
			templateFiles.setSelectedItem(info.templateFile);
		}
		txtData.setText(info.dataSheetName);
		txtConfig.setText(info.configSheetName);
	}

	public File getSaveFile() {
		return SwingTools.selectSaveFile(null, null, null,
				"excel导入配置文件=" + IEditorEnvironment.Excel_Import_DB_File_Extension);
	}

	/**
	 * Create the dialog.
	 */
	public ExcelToDBDialog(IMainControl mainControl) {
		setTitle("从Excel导入");
		setIconImage(Toolkit.getDefaultToolkit().getImage(ExcelToDBDialog.class.getResource("/image/browser.png")));
		setBounds(100, 100, 1306, 742);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				if (isEdit) {
					if (MsgHelper.showConfirmDialog("当前配置已经改变，是否保存？",
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						saveConfig();
				}
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub

			}
		});
		this.mainControl = mainControl;

		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		JToolBar toolBar = new JToolBar();
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.setFloatable(false);
		contentPanel.add(toolBar, BorderLayout.NORTH);
		JButton button = new JButton(" 添加 ");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addConfig();
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button);

		JButton btnShanchu = new JButton(" 删除 ");
		btnShanchu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeConfig();
			}
		});
		btnShanchu.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(btnShanchu);

		toolBar.addSeparator();

		JLabel label = new JLabel(" 数据页 ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label);

		txtData = new JTextField();
		txtData.setMaximumSize(new Dimension(100, 2147483647));
		txtData.setText("data");
		txtData.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(txtData);
		txtData.setColumns(10);

		JLabel label_1 = new JLabel(" 配置页 ");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_1);

		txtConfig = new JTextField();
		txtConfig.setMaximumSize(new Dimension(100, 2147483647));
		txtConfig.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		txtConfig.setText("config");
		toolBar.add(txtConfig);
		txtConfig.setColumns(10);

		JLabel label_2 = new JLabel(" 模板文件 ");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_2);

		templateFiles = new JComboBox<>();
		templateFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String hint = null;
				if (templateFiles.getSelectedItem() != null)
					hint = ((File) templateFiles.getSelectedItem()).getAbsolutePath();
				templateFiles.setToolTipText(hint);
			}
		});
		templateFiles.setMaximumSize(new Dimension(200, 32767));
		templateFiles.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(templateFiles);

		JButton button_5 = new JButton(" 选择 ");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = SwingTools.selectOpenFile(null, null, null, "excel导入模板文件=xls;xlsx");
				if (file != null) {
					if (templateFiles.getItemCount() == 0)
						templateFiles.addItem(null);
					templateFiles.addItem(file);
					templateFiles.setSelectedItem(file);
				}
			}
		});
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_5);

		toolBar.addSeparator();

		JLabel lblNewLabel = new JLabel(" 导入表名称 ");
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(lblNewLabel);
		tableNameView = new JComboBox<>();
		tableNameView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String hint = (String) tableNameView.getSelectedItem();
				tableNameView.setToolTipText(hint);
			}
		});
		tableNameView.setMaximumSize(new Dimension(200, 32767));
		toolBar.add(tableNameView);

		JButton button_3 = new JButton(" 保存 ");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_3);
		JScrollPane scrollPane = new JScrollPane();
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		list = new JList<>(new DefaultListModel<>());
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				load();
			}
		});
		list.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane.setViewportView(list);

		JToolBar toolBar_1 = new JToolBar();
		toolBar_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.setFloatable(false);
		contentPanel.add(toolBar_1, BorderLayout.SOUTH);

		JButton button_1 = new JButton(" 保存配置 ");
		toolBar_1.add(button_1);
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveConfig();
			}
		});
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JButton button_4 = new JButton(" 配置另存为 ");
		toolBar_1.add(button_4);
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = getSaveFile();
				if (file == null)
					return;

				saveFile = file;
				saveConfig();
			}
		});
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		toolBar_1.addSeparator();
		JButton button_2 = new JButton(" 装载配置 ");
		toolBar_1.add(button_2);
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadConfig();
			}
		});
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		toolBar_1.addSeparator();

		button_6 = new JButton(" 导入 ");
		toolBar_1.add(button_6);
		button_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					execute();
					MsgHelper.showMessage("导入执行完毕！");

				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		initTableNames();

		setLocationRelativeTo(null);
	}

	public static void show(IMainControl mainControl) {
		IDBConnection db = mainControl.getDB();
		if (db == null || !db.isOpen()) {
			MsgHelper.showMessage("请先连接数据库！");
			return;
		}

		ExcelToDBDialog dialog = new ExcelToDBDialog(mainControl);
		dialog.setModal(true);
		dialog.setVisible(true);
	}
}
