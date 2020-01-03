package com.wh.gaea.plugin.datasource.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.control.grid.DataGridHelp;
import com.wh.gaea.control.modelsearch.ModelSearchView;
import com.wh.gaea.datasource.DataSource;
import com.wh.gaea.datasource.DataSourceManager;
import com.wh.gaea.datasource.LocalDataSource;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.gaea.plugin.datasource.GridToExcel;
import com.wh.gaea.plugin.datasource.runner.LocalDataSourceRunner;
import com.wh.gaea.plugin.datasource.runner.LocalDataSourceRunner.Dataset;
import com.wh.gaea.plugin.datasource.runner.LocalDataSourceRunner.SqlRunConfig;
import com.wh.gaea.plugin.db.DBConnectionStringCreator;
import com.wh.gaea.selector.KeyValueSelector;
import com.wh.gaea.selector.KeyValueSelector.ModelResult;
import com.wh.swing.tools.MsgHelper;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IColumn;
import wh.interfaces.IDataset.IRow;

public class LocalDataSourceConfig extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTable dataProviderGrid;

	private JList<DataSource> dss;

	DataSourceManager dsm = DataSourceManager.getDSM();

	protected DataSource getCurrentDataSource() {
		return dss.getSelectedValue();
	}

	protected void addDataSource() throws Exception {
		String id = MsgHelper.showInputDialog("请输入新数据源的名称");
		if (id == null || id.isEmpty())
			return;

		DefaultListModel<DataSource> model = (DefaultListModel<DataSource>) dss.getModel();
		for (int i = 0; i < model.getSize(); i++) {
			if (model.getElementAt(i).id.compareTo(id) == 0) {
				MsgHelper.showMessage("数据源[" + id + "]已经存在！");
				return;
			}
		}

		DataSource dataSource = new LocalDataSource();
		dataSource.id = id;
		dataSource.setFile(dsm.getSaveFile(dataSource));
		dataSource.save();
		model.addElement(dataSource);
		dsm.add(dataSource);
	}

	protected void copyDataSource() {
		DataSource source = getCurrentDataSource();
		if (source == null) {
			MsgHelper.showMessage("请先选择一个数据源！");
			return;
		}

		String id = MsgHelper.showInputDialog("请输入新数据源的名称");
		if (id == null || id.isEmpty())
			return;

		DefaultListModel<DataSource> model = (DefaultListModel<DataSource>) dss.getModel();
		for (int i = 0; i < model.getSize(); i++) {
			if (model.getElementAt(i).id.compareTo(id) == 0) {
				MsgHelper.showMessage("数据源[" + id + "]已经存在！");
				return;
			}
		}

		DataSource dataSource = new LocalDataSource();
		dataSource.id = id;
		dataSource.setFile(dsm.getSaveFile(dataSource));
		source.copyTo(dataSource);
		model.addElement(dataSource);
		dsm.add(dataSource);
	}

	protected void removeDataSource() throws IOException {
		if (dss.getSelectedValue() == null)
			return;

		if (MsgHelper.showConfirmDialog("是否删除选定的数据源？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		dsm.remove(dss.getSelectedValue().id);
		DefaultListModel<DataSource> model = (DefaultListModel<DataSource>) dss.getModel();
		model.removeElementAt(dss.getSelectedIndex());
	}

	protected void setDataSource(DataSource ds) {
		if (ds == null)
			return;

		dbConnectionString.setText(ds.url == null ? "" : ds.url);
		sqlView.setText(ds.params == null ? "" : ds.params.getString("sql"));
		memoView.setText(ds.memo);
		dataProviderGrid.setModel(new DefaultTableModel());

		sqlCommands.removeAll();
	}

	protected static Dataset dbToDataset(IDataset dataset) {
		Dataset ds = new Dataset();
		for (IColumn column : dataset.getColumns()) {
			ds.columns.put(column.getName());
		}
		for (IRow row : dataset.getRows()) {
			JSONObject rowData = new JSONObject();
			for (IColumn column : dataset.getColumns()) {
				rowData.put(column.getName(), row.getValue(column.getName()));
			}
			ds.rows.put(rowData);
		}
		return ds;
	}

	protected static void setDatasetModel(JTable table, Dataset dataset) {

		if (dataset == null) {
			table.setModel(new DefaultTableModel());
			return;
		}

		Object[][] rows = new Object[dataset.rows.length()][dataset.columns.length()];
		Object[] columns = new Object[dataset.columns.length()];
		int index = 0;
		for (Object column : dataset.columns) {
			columns[index++] = (String) column;
		}

		index = 0;
		for (Object rowObject : dataset.rows) {
			int colIndex = 0;
			JSONObject row = (JSONObject) rowObject;
			for (Object column : dataset.columns) {
				String field = (String) column;
				if (row.has(field))
					rows[index][colIndex++] = row.get(field);
			}
			index++;
		}
		DefaultTableModel model = new DefaultTableModel(rows, columns);
		table.setModel(model);
	}

	static class ButtonInfo extends JButton {
		private static final long serialVersionUID = 1L;
		public String name;
		public String sql;
		public SqlRunConfig config;
		public Dataset masterDataset;
		public DataSource dataSource;
		public int row;

		@Override
		public String toString() {
			return name;
		}

		public ButtonInfo(String text) {
			super(text);
			name = text;
		}
	}

	public void executeSql() throws Exception {
		executeSql(mainControl, sqlView.getText(), getCurrentDataSource(), sqlCommands, dataProviderGrid);

	}

	public static JSONArray executeSql(IMainControl mainControl, String sql, DataSource dataSource,
			JToolBar sqlCommands, JTable table) throws Exception {
		sqlCommands.removeAll();

		IDBConnection db = DBConnectionStringCreator.getDBConnection(new JSONObject(dataSource.url));
		if (db == null) {
			MsgHelper.showMessage("请先连接数据库！");
			return new JSONArray();
		}

		if (sql == null || sql.isEmpty())
			return new JSONArray();

		Dataset dataset;
		try {
			SqlRunConfig config = LocalDataSourceRunner.parseSql(sql);
			if (config != null) {
				Object[][] rows = new Object[2][2];
				rows[0][0] = "查询语句";
				rows[1][0] = "标题字段名称";
				JSONObject userData = dataSource.userData;
				if (userData.has("sql"))
					rows[0][1] = userData.getString("sql");
				if (userData.has("field"))
					rows[1][1] = userData.getString("field");

				ModelResult result = KeyValueSelector.show(null, mainControl, null, null, rows,
						new Object[] { "项目", "值" }, null, new int[] { 0 }, false);

				DefaultTableModel model = null;
				if (result.isok)
					model = result.model;
				if (model == null)
					return new JSONArray();
				String masterSql = (String) model.getValueAt(0, 1);
				String primKey = (String) model.getValueAt(1, 1);
				IDataset masterDataset = db.query(masterSql, null);

				userData.put("sql", masterSql);
				userData.put("field", primKey);

				Dataset data = dbToDataset(masterDataset);
				for (int i = 0; i < masterDataset.getRowCount(); i++) {
					IRow row = masterDataset.getRow(i);
					String name = (String) row.getValue(primKey);
					ButtonInfo buttonInfo = new ButtonInfo(name);
					buttonInfo.sql = sql;
					buttonInfo.config = config;
					buttonInfo.masterDataset = data;
					buttonInfo.row = i;
					buttonInfo.dataSource = dataSource;
					buttonInfo.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							ButtonInfo buttonInfo = (ButtonInfo) e.getSource();
							try {
								IDBConnection dbConnection = DBConnectionStringCreator
										.getDBConnection(new JSONObject(dataSource.url));
								IDataset result = LocalDataSourceRunner.query(buttonInfo.config,
										buttonInfo.masterDataset.rows, buttonInfo.row, dbConnection);

								setDatasetModel(table, dbToDataset(result));
							} catch (Exception e1) {
								e1.printStackTrace();
								table.setModel(new DefaultTableModel());
							}
						}
					});
					sqlCommands.add(buttonInfo);
					sqlCommands.updateUI();
				}
			} else {
				dataset = dbToDataset(db.query(sql, null));
				if (table != null)
					setDatasetModel(table, dataset);
				return dataset.rows;
			}

		} catch (Exception e) {
			table.setModel(new DefaultTableModel());
			e.printStackTrace();
			MsgHelper.showException(e);
		}

		return new JSONArray();
	}

	public static JSONArray executeSql(DataSource dSource, JSONArray dataset, int curRow) throws Exception {
		return executeSql(dSource.params.getString("sql"), new JSONObject(dSource.url), dataset, curRow);
	}

	public static JSONArray executeSql(String sql, JSONObject dbConnectionParams, JSONArray rows, int curRow)
			throws Exception {
		if (sql == null || sql.isEmpty())
			return new JSONArray();

		IDataset dataset;
		IDBConnection db = DBConnectionStringCreator.getDBConnection(dbConnectionParams);
		SqlRunConfig config = LocalDataSourceRunner.parseSql(sql);

		if (config != null) {
			dataset = LocalDataSourceRunner.query(config, rows, curRow, db);
		} else {
			dataset = db.query(sql, null);
		}

		JSONArray result = new JSONArray();
		for (IRow row : dataset.getRows()) {
			JSONObject rowData = new JSONObject();
			for (IColumn column : dataset.getColumns()) {
				rowData.put(column.getName(), row.getValue(column.getName()));
			}
			result.put(rowData);
		}

		return result;
	}

	IMainControl mainControl;
	private JTextField dbConnectionString;
	private JTextArea sqlView;
	private JToolBar toolBar_1;
	private JScrollPane scrollPane_2;
	private JToolBar sqlCommands;
	private JPanel panel_3;
	private JTextArea memoView;

	/**
	 * Create the dialog.
	 */
	public LocalDataSourceConfig(IMainControl mainControl) {
		this.mainControl = mainControl;

		setTitle("数据源配置");
		setIconImage(
				Toolkit.getDefaultToolkit().getImage(LocalDataSourceConfig.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setBounds(100, 100, 1202, 881);
		getContentPane().setLayout(new BorderLayout());
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("确定");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dss.getSelectedValue() == null) {
					MsgHelper.showMessage("请先选择一个数据源！");
					return;
				}

				setVisible(false);
			}
		});
		okButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		buttonPane.add(okButton);
		JButton cancelButton = new JButton("取消");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.3);
		getContentPane().add(splitPane, BorderLayout.CENTER);
		JPanel panel = new JPanel();
		splitPane.setLeftComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));
		JToolBar toolBar = new JToolBar();
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(toolBar, BorderLayout.NORTH);
		JButton button = new JButton("新增");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					addDataSource();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button);
		button = new JButton("删除");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					removeDataSource();
				} catch (IOException e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button);
		button = new JButton("复制");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copyDataSource();
			}
		});
		toolBar.addSeparator();
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button);
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);
		dss = new ModelSearchView.ListModelSearchView<DataSource>();
		dss.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setDataSource(dss.getSelectedValue());
			}
		});
		dss.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		dss.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				setDataSource(dss.getSelectedValue());
			}
		});
		scrollPane.setViewportView(dss);
		panel_3 = new JPanel();
		splitPane.setRightComponent(panel_3);
		panel_3.setLayout(new BorderLayout(0, 0));
		toolBar_1 = new JToolBar();
		toolBar_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_3.add(toolBar_1, BorderLayout.NORTH);
		button = new JButton("连接");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String connectString = dbConnectionString.getText();
				JSONObject connectInfo = (connectString == null || connectString.isEmpty()) ? new JSONObject()
						: new JSONObject(connectString);
				JSONObject result = DBConnectionStringCreator.showDialog(connectInfo);
				if (result == null)
					return;

				dbConnectionString.setText(result.toString());
				dbConnectionString.updateUI();
			}
		});
		JLabel label = new JLabel("数据库连接串 ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(label);
		dbConnectionString = new JTextField();
		dbConnectionString.setMaximumSize(new Dimension(500, 2147483647));
		dbConnectionString.setPreferredSize(new Dimension(400, 27));
		dbConnectionString.setMinimumSize(new Dimension(400, 27));
		dbConnectionString.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(dbConnectionString);
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(button);

		JButton btnexcel = new JButton("导出Excel");
		btnexcel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GridToExcel.export((DefaultTableModel) dataProviderGrid.getModel());
			}
		});

		JButton button_2 = new JButton("执行");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					executeSql();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(button_2);

		toolBar_1.addSeparator();
		btnexcel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(btnexcel);
		button = new JButton("保存");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveCurrent();
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(button);
		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.6);
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		panel_3.add(splitPane_1, BorderLayout.CENTER);
		splitPane_1.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				splitPane_1.setResizeWeight(splitPane_1.getResizeWeight());
				splitPane_1.setDividerLocation(splitPane_1.getResizeWeight());
			}
		});

		JPanel panel_2 = new JPanel();
		splitPane_1.setLeftComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_1 = new JScrollPane();
		panel_2.add(scrollPane_1, BorderLayout.CENTER);

		sqlView = new JTextArea();
		sqlView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_1.setViewportView(sqlView);

		JSplitPane splitPane_2 = new JSplitPane();
		splitPane_2.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				splitPane_2.setResizeWeight(splitPane_2.getResizeWeight());
				splitPane_2.setDividerLocation(splitPane_2.getResizeWeight());
			}
		});
		splitPane_1.setRightComponent(splitPane_2);
		splitPane_2.setResizeWeight(0.6);
		splitPane_2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		JPanel panel_1 = new JPanel();
		splitPane_2.setLeftComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		sqlCommands = new JToolBar();
		sqlCommands.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_1.add(sqlCommands, BorderLayout.NORTH);
		scrollPane_2 = new JScrollPane();
		panel_1.add(scrollPane_2, BorderLayout.CENTER);

		dataProviderGrid = new ModelSearchView.TableModelSearchView();
		dataProviderGrid.setFillsViewportHeight(true);
		DataGridHelp.FitTableColumns(dataProviderGrid);

		dataProviderGrid.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (dataProviderGrid.getSelectedRow() == -1 || dataProviderGrid.getSelectedColumn() == -1)
					return;

				if (dataProviderGrid.isEditing())
					return;

				dataProviderGrid.editCellAt(dataProviderGrid.getSelectedRow(), dataProviderGrid.getSelectedColumn());
				dataProviderGrid.getEditorComponent().requestFocus();
			}
		});
		dataProviderGrid.setColumnSelectionAllowed(true);
		dataProviderGrid.setCellSelectionEnabled(true);
		dataProviderGrid.setRowHeight(30);
		dataProviderGrid.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_2.setViewportView(dataProviderGrid);

		JScrollPane scrollPane_3 = new JScrollPane();
		splitPane_2.setRightComponent(scrollPane_3);

		memoView = new JTextArea();
		scrollPane_3.setViewportView(memoView);

		init();

		setLocationRelativeTo(null);
	}

	protected void saveCurrent() {
		String url = dbConnectionString.getText();
		String params = sqlView.getText();

		DataSource dataSource = dss.getSelectedValue();
		dataSource.url = url;
		dataSource.memo = memoView.getText();
		dataSource.params = new JSONObject();
		dataSource.params.put("sql", params);
		dataSource.useLocal = true;
		try {
			dataSource.save();
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}

	protected void init() {
		try {
			dsm.loadAll();
			DefaultListModel<DataSource> model = new DefaultListModel<>();
			for (DataSource dataSource : dsm.getDataSources()) {
				if (dataSource.getType().equals(LocalDataSource.LOCAL_KEY))
					model.addElement(dataSource);
			}

			dss.setModel(model);

			if (model.getSize() > 0)
				dss.setSelectedIndex(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected DataSource getResult() {
		return dss.getSelectedValue();
	}

	public static DataSource show(IMainControl mainControl) {
		LocalDataSourceConfig config = new LocalDataSourceConfig(mainControl);
		config.setModal(true);
		config.setVisible(true);
		DataSource result = config.getResult();
		config.dispose();
		return result;
	}
}
