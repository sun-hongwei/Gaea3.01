package com.wh.gaea.plugin.excel;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.datasource.DataSource;
import com.wh.gaea.datasource.DataSourceManager;
import com.wh.gaea.datasource.FileDataSource;
import com.wh.gaea.datasource.LocalDataSource;
import com.wh.gaea.datasource.SQLDataSource;
import com.wh.gaea.datasource.UrlDataSource;
import com.wh.gaea.interfaces.IEditorEnvironment;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.gaea.interfaces.ShowType;
import com.wh.gaea.plugin.datasource.dialog.LocalDataSourceConfig;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.SwingTools;
import com.wh.tools.JsonHelp;

import wh.excel.simple.model.ExcelSwitchModel;
import wh.excel.template.Config;
import wh.excel.template.Template;

public class DBToExcelDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();

	class LinkInfo {
		public File dataTemplateFile;
		public JSONObject datasets = new JSONObject();
		public String configSheetName;
		public String dataSheetName;
		public File mapTemplateFile;
		public File saveFile;

		public int count = -1;

		public LinkInfo(File excelFile) {
			this.saveFile = excelFile;
		}

		public LinkInfo(JSONObject data) {
			if (data.has("datasets"))
				datasets = data.getJSONObject("datasets");
			if (data.has("dataSheetName"))
				dataSheetName = data.getString("dataSheetName");
			if (data.has("configSheetName"))
				configSheetName = data.getString("configSheetName");

			if (data.has("saveFile"))
				saveFile = new File(data.getString("saveFile"));
			if (data.has("dataTemplateFile"))
				dataTemplateFile = new File(data.getString("dataTemplateFile"));
			if (data.has("mapTemplateFile"))
				mapTemplateFile = new File(data.getString("mapTemplateFile"));
		}

		@Override
		public String toString() {
			return saveFile.getName() + (count == -1 ? "" : " => 执行：[" + count + "]行");
		}

		public JSONObject toJson() {
			JSONObject data = new JSONObject();
			data.put("saveFile", saveFile.getAbsolutePath());
			if (dataTemplateFile != null)
				data.put("dataTemplateFile", dataTemplateFile.getAbsolutePath());
			if (mapTemplateFile != null)
				data.put("mapTemplateFile", mapTemplateFile.getAbsolutePath());
			data.put("datasets", datasets);
			data.put("configSheetName", configSheetName);
			data.put("dataSheetName", dataSheetName);
			return data;
		}

	}

	class DatasetInfo {
		public String name;
		public String datasetId;
		public String url;

		public DatasetInfo(String name, String datasetId, String url) {
			this.name = name;
			this.datasetId = datasetId;
			this.url = url;
		}

		public DatasetInfo(JSONObject data) {
			this.name = data.getString("name");
			this.datasetId = data.getString("datasetId");
			if (data.has("url"))
				this.url = data.getString("url");
		}

		public JSONObject toJson() {
			JSONObject result = new JSONObject();
			result.put("name", name);
			result.put("datasetId", datasetId);
			result.put("url", url);
			return result;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	File saveFile;
	IMainControl mainControl;
	private boolean isEdit = false;

	public void execute() throws Exception {
		DefaultListModel<LinkInfo> model = (DefaultListModel<LinkInfo>) list.getModel();
		if (model.getSize() == 0)
			return;

		for (int i = 0; i < model.getSize(); i++) {
			ExcelSwitchModel excelSwitchModel = new ExcelSwitchModel();
			LinkInfo info = model.getElementAt(i);

			// if (info.count > 0)
			// continue;

			File mapTemplateFile = null;
			if (info.mapTemplateFile != null) {
				if (!info.mapTemplateFile.exists()) {
					throw new IOException("模板文件[" + info.mapTemplateFile.getAbsolutePath() + "]不存在！");
				}

				mapTemplateFile = info.mapTemplateFile;
			} else
				mapTemplateFile = info.dataTemplateFile;

			Template<Config> template = excelSwitchModel.getJsonToExcelTemplate(mapTemplateFile, info.configSheetName);

			JSONArray dataset;
			if (template.masterDatasetId == null || template.masterDatasetId.isEmpty()) {
				int rowCount = 0;
				JSONObject datasets = new JSONObject();
				for (Object obj : info.datasets.names()) {
					String id = (String) obj;

					dataset = querySql(info.datasets.getJSONObject(id), null, -1, false);

					datasets.put(id, dataset);

					rowCount += dataset.length();
				}
				if (rowCount == 0)
					continue;

				excelSwitchModel.importFrom(template, info.dataTemplateFile, info.dataSheetName, info.saveFile,
						datasets);

				info.count = rowCount;
			} else {
				if (!info.datasets.has(template.masterDatasetId))
					throw new Exception("已定义的数据源中未发现主数据集【" + template.masterDatasetId + "】定义！");

				JSONArray masterDataset = querySql(info.datasets.getJSONObject(template.masterDatasetId), null, -1,
						false);
				excelSwitchModel.beginBatchImport(template, info.dataTemplateFile, info.dataSheetName);
				try {
					int rowCount = 0;
					for (int j = 0; j < masterDataset.length(); j++) {
						JSONObject datasets = new JSONObject();
						for (Object obj : info.datasets.names()) {
							String id = (String) obj;
							if (id.equals(template.masterDatasetId)) {
								datasets.put(id, masterDataset);
								continue;
							}

							dataset = querySql(info.datasets.getJSONObject(id), masterDataset, j, false);

							datasets.put(id, dataset);

							rowCount += dataset.length();
						}
						if (rowCount == 0)
							continue;

						excelSwitchModel.batchImportFrom(datasets);

						info.count = rowCount;
					}
				} finally {
					excelSwitchModel.endBatchImport(info.saveFile);
				}

			}

			list.updateUI();
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

		save();

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
				"excel导出模板=" + IEditorEnvironment.DB_Export_Excel_File_Extension);

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

	private JList<LinkInfo> list;
	private JTextField txtData;
	private JTextField txtConfig;
	private JComboBox<File> dataTemplateFiles;
	private JButton button_6;
	private JList<DatasetInfo> datasets;
	private JTextField datasetNameView;
	private JComboBox<File> mapTemplateFiles;
	private JTextField datasetIdView;
	private JTextField serverUrl;
	private JTable dataTable;
	private JToolBar sqlCommands;

	public void addConfig() {
		File file = SwingTools.selectSaveFile(null, null, null, "excel导出配置文件=xlsx;xls");
		if (file != null) {
			DefaultListModel<LinkInfo> model = (DefaultListModel<LinkInfo>) list.getModel();
			for (int i = 0; i < model.getSize(); i++) {
				LinkInfo info = model.getElementAt(i);
				if (info.saveFile.equals(file)) {
					MsgHelper.showMessage("导出文件[" + file.getAbsolutePath() + "]已经存在！");
					return;
				}
			}

			LinkInfo info = new LinkInfo(file);
			model.addElement(info);
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

	public boolean save() {
		if (list.getSelectedValue() == null) {
			MsgHelper.showMessage("请先选择一个配置项目!");
			return false;
		}

		if (dataTemplateFiles.getSelectedItem() == null) {
			MsgHelper.showMessage("请先选择一个数据模板文件!");
			return false;
		}

		if (mapTemplateFiles.getSelectedItem() == null) {
			MsgHelper.showMessage("请先选择一个映射模板文件!");
			return false;
		}

		if (datasets.getModel().getSize() == 0) {
			MsgHelper.showMessage("请至少添加一个数据源!");
			return false;
		}

		saveDatasetInfo(false);

		List<LinkInfo> infos = list.getSelectedValuesList();
		for (LinkInfo info : infos) {
			if (dataTemplateFiles.getSelectedItem().equals(info.saveFile)) {
				MsgHelper.showMessage("数据模板文件不能和导出文件相同！");
				list.setSelectedValue(info, true);
				return false;
			}
			if (mapTemplateFiles.getSelectedItem().equals(info.saveFile)) {
				MsgHelper.showMessage("映射模板文件不能和导出文件相同！");
				list.setSelectedValue(info, true);
				return false;
			}
			save(info);
		}

		isEdit = true;

		list.updateUI();

		return true;
	}

	public void save(LinkInfo info) {
		info.datasets = new JSONObject();
		DefaultListModel<DatasetInfo> model = (DefaultListModel<DatasetInfo>) datasets.getModel();
		for (int i = 0; i < model.getSize(); i++) {
			DatasetInfo datasetInfo = model.getElementAt(i);
			info.datasets.put(datasetInfo.name, datasetInfo.toJson());
		}

		info.configSheetName = txtConfig.getText();
		info.configSheetName = info.configSheetName == null || info.configSheetName.isEmpty() ? "config"
				: info.configSheetName;
		info.dataSheetName = txtData.getText();
		info.dataSheetName = info.dataSheetName == null || info.dataSheetName.isEmpty() ? "data" : info.dataSheetName;
		info.dataTemplateFile = (File) dataTemplateFiles.getSelectedItem();
		info.mapTemplateFile = (File) mapTemplateFiles.getSelectedItem();

	}

	protected void setTemplateComboBox(File templateFile, JComboBox<File> comboBox) {
		if (templateFile == null)
			comboBox.setSelectedItem(null);
		else {
			File selectFile = null;
			for (int i = 0; i < comboBox.getItemCount(); i++) {
				File file = comboBox.getItemAt(i);
				if (file == null)
					continue;

				if (file.equals(templateFile)) {
					selectFile = file;
					break;
				}
			}

			if (selectFile == null) {
				if (comboBox.getItemCount() == 0)
					comboBox.addItem(null);
				comboBox.addItem(templateFile);
			}
			comboBox.setSelectedItem(templateFile);
		}
	}

	public void load() {
		if (list.getSelectedValue() == null) {
			return;
		}

		LinkInfo info = list.getSelectedValue();

		datasetNameView.setText("");

		DefaultListModel<DatasetInfo> model = new DefaultListModel<>();
		if (info.datasets.names() != null)
			for (Object obj : info.datasets.names()) {
				String name = (String) obj;
				DatasetInfo datasetInfo = new DatasetInfo(info.datasets.getJSONObject(name));
				model.addElement(datasetInfo);
			}

		datasets.setModel(model);
		datasets.updateUI();
		sqlCommands.removeAll();
		sqlCommands.updateUI();

		dataTable.setModel(new DefaultTableModel());
		setTemplateComboBox(info.mapTemplateFile, mapTemplateFiles);
		setTemplateComboBox(info.dataTemplateFile, dataTemplateFiles);
		txtData.setText(info.dataSheetName);
		txtConfig.setText(info.configSheetName);
	}

	public File getSaveFile() {
		return SwingTools.selectSaveFile(null, null, null,
				"excel导出配置文件=" + IEditorEnvironment.DB_Export_Excel_File_Extension);
	}

	public void querySql() {

		try {
			querySql(datasets.getSelectedValue(), null, -1, true);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
			return;
		}

	}

	public JSONArray querySql(JSONObject data, JSONArray masterDataset, int curRow, boolean setModel) throws Exception {
		DatasetInfo datasetInfo = new DatasetInfo(data);
		return querySql(datasetInfo, masterDataset, curRow, setModel);
	}

	public JSONArray querySql(DatasetInfo datasetInfo, JSONArray masterDataset, int curRow, boolean setModel)
			throws Exception {

		if (datasetInfo == null)
			return new JSONArray();

		DataSource dataSource = DataSourceManager.getDSM().get(datasetInfo.datasetId);
		if (dataSource == null)
			return new JSONArray();

		if (dataSource.getType().equals(LocalDataSource.LOCAL_KEY)) {
			if (dataSource.url == null || dataSource.url.isEmpty() || dataSource.params == null
					|| !dataSource.params.has("sql"))
				return new JSONArray();

			if (masterDataset != null) {
				return LocalDataSourceConfig.executeSql(dataSource, masterDataset, curRow);
			} else {
				String sql = dataSource.params.getString("sql");
				return LocalDataSourceConfig.executeSql(mainControl, sql, dataSource, sqlCommands,
						setModel ? dataTable : null);
			}
		} else {
			if (dataSource == null || dataSource.url == null || dataSource.url.isEmpty())
				return new JSONArray();

			JSONObject params = new JSONObject();
			if (masterDataset != null)
				params = masterDataset.getJSONObject(curRow);

			return DataSource.setModel(dataSource, datasetInfo.url, setModel ? dataTable : null, params);
		}
	}

	public void saveDatasetInfo(boolean add) {
		if (!add && datasets.getSelectedIndex() == -1)
			return;

		String datasetId = datasetIdView.getText();
		if (datasetId == null || datasetId.isEmpty()) {
			MsgHelper.showMessage("请选择数据源名称！");
			return;
		}

		String name = datasetNameView.getText();
		if (name == null || name.isEmpty()) {
			MsgHelper.showMessage("请输入数据集名称！");
			return;
		}

		DefaultListModel<DatasetInfo> model = (DefaultListModel<DatasetInfo>) datasets.getModel();

		for (int i = 0; i < model.getSize(); i++) {
			DatasetInfo info = model.getElementAt(i);
			if (add && info.name.compareToIgnoreCase(name) == 0) {
				MsgHelper.showMessage("数据集名称[" + name + "]已经存在！");
				return;
			}
		}

		if (add)
			model.addElement(new DatasetInfo(name, datasetId, serverUrl.getText()));
		else if (datasets.getSelectedValue() != null) {
			DatasetInfo info = datasets.getSelectedValue();
			info.name = name;
			info.datasetId = datasetId;
		}

		datasets.updateUI();
	}

	/**
	 * Create the dialog.
	 */
	public DBToExcelDialog(IMainControl mainControl) {
		setTitle("导出到Excel");
		setIconImage(Toolkit.getDefaultToolkit().getImage(ExcelToDBDialog.class.getResource("/image/browser.png")));
		setBounds(100, 100, 1250, 742);

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

		JLabel label_5 = new JLabel(" 映射模板文件 ");
		label_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_5);

		mapTemplateFiles = new JComboBox<File>();
		mapTemplateFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String hint = null;
				if (mapTemplateFiles.getSelectedItem() != null)
					hint = ((File)mapTemplateFiles.getSelectedItem()).getAbsolutePath() ;
				mapTemplateFiles.setToolTipText(hint);
			}
		});
		mapTemplateFiles.setPreferredSize(new Dimension(150, 27));
		mapTemplateFiles.setMaximumSize(new Dimension(200, 32767));
		mapTemplateFiles.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(mapTemplateFiles);

		JButton button_10 = new JButton(" 选择 ");
		button_10.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String initDir = null;
				if (mapTemplateFiles.getSelectedItem() != null)
					initDir = ((File) mapTemplateFiles.getSelectedItem()).getParentFile().getAbsolutePath();
				File file = SwingTools.selectOpenFile(null, initDir, null, "excel导出映射模板文件=xlsx;xls");
				if (file != null) {
					if (mapTemplateFiles.getItemCount() == 0)
						mapTemplateFiles.addItem(null);
					mapTemplateFiles.addItem(file);
					mapTemplateFiles.setSelectedItem(file);
				}

			}
		});
		button_10.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_10);

		JLabel label_2 = new JLabel(" 数据模板文件 ");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_2);

		dataTemplateFiles = new JComboBox<>();
		dataTemplateFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String hint = null;
				if (dataTemplateFiles.getSelectedItem() != null)
					hint = ((File)dataTemplateFiles.getSelectedItem()).getAbsolutePath() ;
				dataTemplateFiles.setToolTipText(hint);
			}
		});
		dataTemplateFiles.setPreferredSize(new Dimension(150, 27));
		dataTemplateFiles.setMaximumSize(new Dimension(200, 32767));
		dataTemplateFiles.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(dataTemplateFiles);

		JButton button_5 = new JButton(" 选择 ");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String initDir = null;
				if (dataTemplateFiles.getSelectedItem() != null)
					initDir = ((File) dataTemplateFiles.getSelectedItem()).getParentFile().getAbsolutePath();
				File file = SwingTools.selectOpenFile(null, initDir, null, "excel导出数据模板文件=xlsx;xls");
				if (file != null) {
					if (dataTemplateFiles.getItemCount() == 0)
						dataTemplateFiles.addItem(null);
					dataTemplateFiles.addItem(file);
					dataTemplateFiles.setSelectedItem(file);
				}
			}
		});
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_5);

		toolBar.addSeparator();

		JButton button_3 = new JButton(" 保存 ");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (save())
					MsgHelper.showMessage("保存配置信息成功！");

			}
		});
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_3);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.5);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				splitPane.setResizeWeight(splitPane.getResizeWeight());
				splitPane.setDividerLocation(splitPane.getResizeWeight());
			}
		});
		contentPanel.add(splitPane, BorderLayout.CENTER);
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		list = new JList<>(new DefaultListModel<>());
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					if (list.getSelectedValue() != null) {
						try {
							Desktop.getDesktop().open(list.getSelectedValue().saveFile);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				load();
			}
		});
		list.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane.setViewportView(list);

		JPanel panel = new JPanel();
		splitPane.setRightComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane_1.setResizeWeight(0.6);
		splitPane_1.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				splitPane_1.setResizeWeight(splitPane_1.getResizeWeight());
				splitPane_1.setDividerLocation(splitPane_1.getResizeWeight());
			}
		});
		panel.add(splitPane_1, BorderLayout.CENTER);

		JPanel panel_2 = new JPanel();
		splitPane_1.setRightComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_1 = new JScrollPane();
		panel_2.add(scrollPane_1, BorderLayout.CENTER);

		datasets = new JList<>();
		datasets.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (datasets.getSelectedValue() != null) {
					DatasetInfo info = datasets.getSelectedValue();
					datasetNameView.setText(info.name);
					datasetIdView.setText(info.datasetId);

					serverUrl.setText(info.url);
					DataSource dataSource = DataSourceManager.getDSM().get(info.datasetId);
					if (dataSource != null)
						serverUrl.setEnabled(!dataSource.getType().equals(LocalDataSource.LOCAL_KEY));
					else {
						serverUrl.setEnabled(info.url != null && !info.url.isEmpty());
					}
					sqlCommands.removeAll();
					sqlCommands.updateUI();
					dataTable.setModel(new DefaultTableModel());
				}
			}
		});
		datasets.setModel(new DefaultListModel<>());
		scrollPane_1.setViewportView(datasets);

		JToolBar toolBar_2 = new JToolBar();
		panel_2.add(toolBar_2, BorderLayout.NORTH);
		toolBar_2.setFloatable(false);

		JButton button_7 = new JButton(" 添加 ");
		button_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveDatasetInfo(true);
			}
		});
		button_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(button_7);

		JButton button_8 = new JButton(" 删除 ");
		button_8.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (datasets.getSelectedValue() == null)
					return;

				if (MsgHelper.showConfirmDialog("是否删除选定的数据源？",
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

				((DefaultListModel<DatasetInfo>) datasets.getModel()).removeElementAt(datasets.getSelectedIndex());
			}
		});
		button_8.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(button_8);
		
		JButton button_9 = new JButton(" 保存 ");
		button_9.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveDatasetInfo(false);
			}
		});
		button_9.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(button_9);

		toolBar_2.addSeparator();

		JLabel label_4 = new JLabel(" 数据集名称 ");
		label_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(label_4);

		datasetNameView = new JTextField();
		datasetNameView.setMaximumSize(new Dimension(200, 2147483647));
		datasetNameView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(datasetNameView);
		datasetNameView.setColumns(10);

		JLabel label_3 = new JLabel(" 数据源名称 ");
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(label_3);

		datasetIdView = new JTextField();
		datasetIdView.setMaximumSize(new Dimension(200, 2147483647));
		datasetIdView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		datasetIdView.setColumns(10);
		toolBar_2.add(datasetIdView);

		JLabel label_7 = new JLabel(" 服务地址 ");
		label_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(label_7);

		serverUrl = new JTextField();
		serverUrl.setEnabled(false);
		serverUrl.setText("http://localhost:8080/JSPAdapterServer");
		serverUrl.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		serverUrl.setColumns(10);
		toolBar_2.add(serverUrl);

		JButton button_12 = new JButton(" 选择数据源 ");
		button_12.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DataSource dataSource = GlobalInstance.instance().getDataSourceSelector().dataSourceSelector(ShowType.stAll);
				if (dataSource == null)
					return;
				
				switch (dataSource.getType()) {
				case LocalDataSource.LOCAL_KEY:
					datasetIdView.setText(dataSource.id);
					serverUrl.setText("");
					serverUrl.setEnabled(false);
					break;
				case FileDataSource.FILE_KEY:
				case SQLDataSource.SQL_KEY:
				case UrlDataSource.URL_KEY:
					datasetIdView.setText(dataSource.id);
					serverUrl.setEnabled(true);
					break;
				}
			}
		});
		button_12.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(button_12);

		JPanel panel_1 = new JPanel();
		splitPane_1.setLeftComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		sqlCommands = new JToolBar();
		panel_1.add(sqlCommands, BorderLayout.NORTH);

		JScrollPane scrollPane_2 = new JScrollPane();
		panel_1.add(scrollPane_2, BorderLayout.CENTER);

		dataTable = new JTable();
		scrollPane_2.setViewportView(dataTable);

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

		JButton btnNewButton = new JButton("查询数据源");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				querySql();
			}
		});
		btnNewButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(btnNewButton);

		toolBar_1.addSeparator();

		button_6 = new JButton(" 导出 ");
		toolBar_1.add(button_6);
		button_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					execute();
					MsgHelper.showMessage("导出执行完毕！");

				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		setLocationRelativeTo(null);
	}

	public static void show(IMainControl mainControl) {
		DBToExcelDialog dialog = new DBToExcelDialog(mainControl);
		dialog.setModal(true);
		dialog.setVisible(true);
	}
}
