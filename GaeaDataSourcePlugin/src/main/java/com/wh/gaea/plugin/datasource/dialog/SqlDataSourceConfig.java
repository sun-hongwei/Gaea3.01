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
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.control.grid.DataGridHelp;
import com.wh.gaea.control.modelsearch.ModelSearchView;
import com.wh.gaea.datasource.DataSource;
import com.wh.gaea.datasource.DataSourceManager;
import com.wh.gaea.datasource.SQLDataSource;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.gaea.plugin.datasource.GridToExcel;
import com.wh.gaea.plugin.datasource.runner.SqlDataSourceRunner;
import com.wh.gaea.plugin.datasource.runner.SqlDataSourceRunner.IQueryFieldsCallback;
import com.wh.gaea.selector.TableSelector;
import com.wh.swing.tools.MsgHelper;

public class SqlDataSourceConfig extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTable dataProviderGrid;

	private JList<DataSource> dss;

	DataSourceManager dsm = DataSourceManager.getDSM();

	protected DataSource getCurrentDataSource() {
		return dss.getSelectedValue();
	}

	/***
	 * dataset格式： { COLUMN:[{name:"字段名称，可以不填写",size:10,
	 * type:"字段类型，string\int\float\double\date\bool",id:"字段id",
	 * memo:"字段说明，可以不填写"}], DATA:[{"字段id":"字段值"}] }
	 */
	protected void initDataProviderGrid(JSONObject dataset) {
		DataSource ds = getCurrentDataSource();
		if (ds == null) {
			MsgHelper.showMessage("请先选择/新建一个数据源");
			return;
		}

		if (dataset == null) {
			dataProviderGrid.setModel(new DefaultTableModel());
			return;
		}

		dataProviderGrid.setModel(ds);
	}

	public void requestDataset() {
		DataSource ds = getCurrentDataSource();
		if (ds == null) {
			MsgHelper.showMessage("请先选择/新建一个数据源");
			return;
		}

		saveCurrent();

		try {
			SqlDataSourceRunner.execute(ds, mainControl);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
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

		DataSource dataSource = new SQLDataSource();
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

		DataSource dataSource = new SQLDataSource();
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

		String sql = null;
		if (ds.params.has("sql"))
			sql = ds.params.getString("sql");
		sqlText.setText(sql);
		orderView.setText("");
		startView.setValue(0);
		endView.setValue(-1);
		memoView.setText(ds.memo);
		
		if (ds.params.has("order"))
			orderView.setText(ds.params.getString("order"));
		if (ds.params.has("start"))
			startView.setValue(ds.params.getInt("start"));
		if (ds.params.has("size"))
			endView.setValue(ds.params.getInt("size"));
		
		tablenameView.setText("");
		if (ds.params.has("table")){
			tablenameView.setText(ds.params.getString("table"));
		}
		
		DefaultListModel<String> model = (DefaultListModel<String>)primkeyView.getModel();
		model.clear();
		
		if (ds.params.has("primkey")){
			for (Object obj : ds.params.getJSONArray("primkey")) {
				model.addElement((String) obj);
			}
		}
		
		dataProviderGrid.setModel(ds);
		primkeyView.updateUI();
	}

	IMainControl mainControl;
	private JTextField orderView;
	private JTextArea sqlText;
	private JToolBar toolBar_1;
	private JSpinner startView;
	private JSpinner endView;
	private JScrollPane scrollPane_1;
	private JTextArea memoView;
	private JPanel panel_3;
	private JScrollPane scrollPane_3;
	private JTextField tablenameView;

	Primkey primkey = new Primkey();
	private JList<String> primkeyView;
	class Primkey{
		JSONArray fields;
		HashMap<String, JSONObject> fieldMap = new HashMap<>();
		public void init() {
			String tablename = tablenameView.getText();
			try {
				SqlDataSourceRunner.queryFields(tablename, new IQueryFieldsCallback() {
					
					@Override
					public void onCallback(JSONArray result) {
						fields = result;
						for (Object object : fields) {
							JSONObject fieldInfo = (JSONObject)object;
							fieldMap.put(fieldInfo.getString("field"), fieldInfo);
						}
					}
				});
			} catch (Exception e1) {
				e1.printStackTrace();
				MsgHelper.showException(e1);
				return;
			}
		}
		
		public void add() {
			if (fieldMap.size() == 0){
				MsgHelper.showMessage("请先获取数据表字段！");
				return;
			}
			
			DefaultListModel<String> model = (DefaultListModel<String>) primkeyView.getModel();
			JSONArray result = null;
			if (model != null){
				HashMap<String, JSONObject> fields = new HashMap<>(fieldMap);
				for (int i = 0; i < model.getSize(); i++) {
					String name = model.getElementAt(i);
					if (fields.containsKey(name)){
						fields.remove(name);
					}
				}

				result = TableSelector.showMulitSelector(null, new JSONArray(fields.values()));
			}else
				result = TableSelector.showMulitSelector(null, fields);
			
			if (result == null || result.length() == 0)
				return;
			
			for (Object object : result) {
				JSONObject row = (JSONObject)object;
				model.addElement(row.getString("field"));
			}
			
			primkeyView.updateUI();
		}

		public void remove() {
			DefaultListModel<String> model = (DefaultListModel<String>) primkeyView.getModel();
			if (model == null){
				return;
			}
			
			if (primkeyView.getSelectedIndex() == -1)
				return;
			
			if (MsgHelper.showConfirmDialog("是否删除选定的主键项目？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
				return;
			
			model.removeElementAt(primkeyView.getSelectedIndex());
			
			primkeyView.updateUI();
		}

	}
	protected void addPrimkey() {
		
	}
	/**
	 * Create the dialog.
	 */
	public SqlDataSourceConfig(IMainControl mainControl) {
		this.mainControl = mainControl;

		setTitle("数据源配置");
		setIconImage(Toolkit.getDefaultToolkit().getImage(SqlDataSourceConfig.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setBounds(100, 100, 1056, 824);
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
		okButton.setActionCommand("");
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
		button = new JButton("请求");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				requestDataset();
			}
		});
		JLabel label = new JLabel("分页字段 ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(label);
		orderView = new JTextField();
		orderView.setMaximumSize(new Dimension(300, 2147483647));
		orderView.setPreferredSize(new Dimension(200, 27));
		orderView.setMinimumSize(new Dimension(200, 27));
		orderView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(orderView);
		
		JLabel label_1 = new JLabel(" 起始索引 ");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(label_1);
		
		startView = new JSpinner();
		startView.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		startView.setPreferredSize(new Dimension(100, 28));
		startView.setMinimumSize(new Dimension(100, 28));
		startView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(startView);
		
		JLabel label_2 = new JLabel(" 每页数量 ");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(label_2);
		
		endView = new JSpinner();
		endView.setModel(new SpinnerNumberModel(new Integer(-1), null, null, new Integer(1)));
		endView.setPreferredSize(new Dimension(100, 28));
		endView.setMinimumSize(new Dimension(100, 28));
		endView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(endView);
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(button);
		toolBar_1.addSeparator();
		
		JButton btnexcel = new JButton("导出到Excel");
		btnexcel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GridToExcel.export((DefaultTableModel)dataProviderGrid.getModel());
			}
		});
		button = new JButton("保存");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveCurrent();
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(button);
		btnexcel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(btnexcel);
		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.4);
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		panel_3.add(splitPane_1, BorderLayout.CENTER);
		JPanel panel_1 = new JPanel();
		splitPane_1.setRightComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JSplitPane splitPane_2 = new JSplitPane();
		splitPane_2.setResizeWeight(0.6);
		splitPane_2.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				splitPane_2.setResizeWeight(splitPane_2.getResizeWeight());
				splitPane_2.setDividerLocation(splitPane_2.getResizeWeight());
			}
		});
		splitPane_2.setOrientation(JSplitPane.VERTICAL_SPLIT);
		panel_1.add(splitPane_2, BorderLayout.CENTER);
		scrollPane_1 = new JScrollPane();
		splitPane_2.setLeftComponent(scrollPane_1);
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
		scrollPane_1.setViewportView(dataProviderGrid);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		splitPane_2.setRightComponent(scrollPane_2);
		
		memoView = new JTextArea();
		scrollPane_2.setViewportView(memoView);
		
		JSplitPane splitPane_3 = new JSplitPane();
		splitPane_3.setResizeWeight(0.6);
		splitPane_3.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				splitPane_3.setResizeWeight(splitPane_3.getResizeWeight());
				splitPane_3.setDividerLocation(splitPane_3.getResizeWeight());
			}
		});
		splitPane_1.setLeftComponent(splitPane_3);
		scrollPane_3 = new JScrollPane();
		splitPane_3.setLeftComponent(scrollPane_3);
		sqlText = new JTextArea();
		sqlText.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_3.setViewportView(sqlText);
		
		JPanel panel_2 = new JPanel();
		splitPane_3.setRightComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_4 = new JPanel();
		panel_2.add(panel_4, BorderLayout.NORTH);
		panel_4.setLayout(new BorderLayout(0, 0));
		
		JLabel lblNewLabel = new JLabel(" 更新表名称 ");
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_4.add(lblNewLabel, BorderLayout.WEST);
		
		tablenameView = new JTextField();
		panel_4.add(tablenameView, BorderLayout.CENTER);
		tablenameView.setColumns(10);
		
		JToolBar toolBar_2 = new JToolBar();
		toolBar_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.setFloatable(false);
		panel_4.add(toolBar_2, BorderLayout.EAST);
		
		JButton button_1 = new JButton("添加");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				primkey.add();
			}
		});
		
		JButton button_3 = new JButton("字段");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				primkey.init();
			}
		});
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(button_3);
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(button_1);
		
		JButton button_2 = new JButton("删除");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				primkey.remove();
			}
		});
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(button_2);
		
		JScrollPane scrollPane_4 = new JScrollPane();
		panel_2.add(scrollPane_4, BorderLayout.CENTER);
		
		primkeyView = new JList<>(new DefaultListModel<>());
		scrollPane_4.setViewportView(primkeyView);
		
		init();

		setLocationRelativeTo(null);
	}

	protected void saveCurrent() {
		String sql = sqlText.getText();
		String order = orderView.getText();
		
		DataSource dataSource = dss.getSelectedValue();
		dataSource.url = "/jsonarray/service/query.do";
		dataSource.memo = memoView.getText();
		dataSource.params = new JSONObject();
		dataSource.params.put("sql", sql);
		if (order != null && !order.isEmpty()){
			int start = (int) startView.getValue();
			int size = (int) endView.getValue();
			dataSource.params.put("order", order);
			dataSource.params.put("start", start);
			dataSource.params.put("size", size);
		}
		
		dataSource.params.put("table", tablenameView.getText());
		JSONArray primKeys = new JSONArray();
		DefaultListModel<String> model = (DefaultListModel<String>)primkeyView.getModel();
		for (int i = 0; i < model.getSize(); i++) {
			primKeys.put(model.getElementAt(i));
		}
		dataSource.params.put("primkey", primKeys);

		dataSource.useLocal = false;
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
				if (dataSource.getType().equals(SQLDataSource.SQL_KEY))
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
		SqlDataSourceConfig config = new SqlDataSourceConfig(mainControl);
		config.setModal(true);
		config.setVisible(true);
		DataSource result = config.getResult();
		config.dispose();
		return result;
	}
}
