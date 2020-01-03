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
import java.util.ArrayList;
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
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.json.JSONObject;

import com.wh.gaea.control.grid.DataGridHelp;
import com.wh.gaea.control.modelsearch.ModelSearchView;
import com.wh.gaea.datasource.DataSource;
import com.wh.gaea.datasource.DataSource.ColumnStyle;
import com.wh.gaea.datasource.DataSourceManager;
import com.wh.gaea.datasource.UrlDataSource;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.gaea.plugin.datasource.ExcelToJsonDialog;
import com.wh.gaea.plugin.datasource.GridToExcel;
import com.wh.gaea.plugin.datasource.runner.UrlDataSourceRunner;
import com.wh.gaea.selector.KeyValueSelector;
import com.wh.gaea.selector.KeyValueSelector.ICheckValue;
import com.wh.gaea.selector.KeyValueSelector.ModelResult;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.dialog.input.LoginInput;
import com.wh.swing.tools.dialog.input.LoginInput.Result;

public class UrlDataSourceConfig extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTable providerConfigGrid;
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
			UrlDataSourceRunner.execute(ds, mainControl, baseUrl.getText());
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

		DataSource dataSource = new UrlDataSource();
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

		DataSource dataSource = new UrlDataSource();
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

	protected ColumnStyle comboBoxToColumnStyle() {
		switch (csComboBox.getSelectedIndex()) {
		case 0:
			return ColumnStyle.csField;
		case 1:
			return ColumnStyle.csConst;
		case 2:
			return ColumnStyle.csExpr;

		default:
			return ColumnStyle.csNone;
		}
	}

	protected void setFieldStyle() {
		DataSource ds = dss.getSelectedValue();
		if (ds == null)
			return;

		if (dataProviderGrid.getSelectedColumn() == -1)
			return;

		ds.setFieldStyle(dataProviderGrid.getSelectedColumn(), comboBoxToColumnStyle());
	}

	protected void setDataSource(DataSource ds) {
		if (ds == null)
			return;
		if (providerConfigGrid.isEditing())
			providerConfigGrid.getCellEditor().stopCellEditing();

		memoView.setText(ds.memo);
		providerConfigGrid.setValueAt(ds.url == null ? "" : ds.url, 0, 1);
		providerConfigGrid.setValueAt(ds.params.toString(), 1, 1);
		providerConfigGrid.setValueAt(ds.useLocal, 2, 1);

		dataProviderGrid.setModel(ds);
	}

	IMainControl mainControl;
	private JComboBox<String> csComboBox;
	private JTextField baseUrl;
	private JToolBar toolBar1;
	private JToolBar toolBar2;
	private JPanel panel_2;
	private JTextArea memoView;
	private JScrollPane scrollPane_2;

	/**
	 * Create the dialog.
	 */
	public UrlDataSourceConfig(IMainControl mainControl) {
		this.mainControl = mainControl;

		setTitle("数据源配置");
		setIconImage(Toolkit.getDefaultToolkit().getImage(UrlDataSourceConfig.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setBounds(100, 100, 1027, 734);
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

				if (dss.getSelectedValue().columns == null || dss.getSelectedValue().dataset == null
						|| dss.getSelectedValue().columns.length() == 0
						|| dss.getSelectedValue().dataset.length() == 0) {
					MsgHelper.showMessage("请先请求一次数据源，或者手动填写数据集合！");
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
		JToolBar toolBar;
		toolBar1 = new JToolBar();
		toolBar1.setFloatable(false);
		toolBar1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel.add(toolBar1, BorderLayout.NORTH);
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
		toolBar1.add(button);
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
		toolBar1.add(button);
		 button = new JButton("复制");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copyDataSource();
			}
		});
		toolBar1.addSeparator();
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar1.add(button);
		JScrollPane scrollPane = new JScrollPane();
		panel.add(scrollPane, BorderLayout.CENTER);
		dss = new ModelSearchView.ListModelSearchView<DataSource>();
		dss.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		dss.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				setDataSource(dss.getSelectedValue());
			}
		});
		dss.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				setDataSource(dss.getSelectedValue());
			}
		});
		scrollPane.setViewportView(dss);
		 panel_2 = new JPanel();
		splitPane.setRightComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		 toolBar = new JToolBar();
		 toolBar.setFloatable(false);
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_2.add(toolBar, BorderLayout.NORTH);
		 button = new JButton(" 请求 ");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				requestDataset();
			}
		});
		JLabel label = new JLabel("请求地址  ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label);
		baseUrl = new JTextField();
		baseUrl.setText("http://localhost:8080/JSPAdapterServer");
		baseUrl.setMinimumSize(new Dimension(400, 27));
		baseUrl.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(baseUrl);
		
		JButton button_2 = new JButton(" 登录 ");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Result result = LoginInput.showDialog("admin");
				if (result == null)
					return;
				
				UrlDataSourceRunner.login(baseUrl.getText() + "/ex/user/login.do", result.user, result.pwd);
			}
		});
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_2);
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button);
		toolBar.addSeparator();
		 button = new JButton(" 保存 ");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveCurrent();
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button);
		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.3);
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		panel_2.add(splitPane_1, BorderLayout.CENTER);
		 scrollPane = new JScrollPane();
		splitPane_1.setLeftComponent(scrollPane);
		providerConfigGrid = new JTable();
		providerConfigGrid.setRowHeight(40);
		providerConfigGrid.setFillsViewportHeight(true);
		providerConfigGrid.setModel(new DefaultTableModel(
				new Object[][] { { "\u6570\u636E\u63D0\u4F9B\u8005URL", "/report/sample/a1" },
						{ "\u8BF7\u6C42\u53C2\u6570", null },
						{ "\u4F7F\u7528\u672C\u5730\u6570\u636E\u96C6\u5408", true}, },
				new String[] { "\u540D\u79F0", "\u503C" }));
		providerConfigGrid.getColumnModel().getColumn(0).setPreferredWidth(224);
		providerConfigGrid.getColumnModel().getColumn(1).setPreferredWidth(673);
		;
		providerConfigGrid.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane.setViewportView(providerConfigGrid);
		
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
		splitPane_1.setRightComponent(splitPane_2);
		JPanel panel_1 = new JPanel();
		splitPane_2.setLeftComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));
		toolBar2 = new JToolBar();
		toolBar2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.setFloatable(false);
		panel_1.add(toolBar2, BorderLayout.NORTH);
		button = new JButton("新增");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addRow();
			}
		});
		
		JLabel label_1 = new JLabel(" 行：");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(label_1);
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(button);
		button = new JButton("删除");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeRow();
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(button);
		JButton button_1 = new JButton("清空");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getCurrentDataSource() != null)
					getCurrentDataSource().removeRows();
			}
		});
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(button_1);
		toolBar2.addSeparator();
		button = new JButton("新增");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addField();
			}
		});
		
		JLabel label_2 = new JLabel(" 字段：");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(label_2);
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(button);
		button = new JButton("删除");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeField();
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(button);
		button = new JButton("编辑字段");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editField();
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(button);
		button = new JButton("清空");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getCurrentDataSource() != null)
					getCurrentDataSource().removeColumns();
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(button);
		csComboBox = new JComboBox<>();
		csComboBox.setVisible(false);
		csComboBox.setModel(new DefaultComboBoxModel<>(new String[] { "字段", "常量", "函数" }));
		csComboBox.setSelectedIndex(0);
		csComboBox.setMaximumSize(new Dimension(100, 23));
		csComboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(csComboBox);
		button = new JButton("修改");
		button.setVisible(false);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setFieldStyle();
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar2.add(button);
		
		toolBar2.addSeparator();
		
		JButton btnexcel = new JButton("导出Excel");
		toolBar2.add(btnexcel);
		btnexcel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GridToExcel.export((DefaultTableModel)dataProviderGrid.getModel());
			}
		});
		btnexcel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		JButton btnexcel_1 = new JButton("导入Excel");
		toolBar2.add(btnexcel_1);
		btnexcel_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dataProviderGrid.setModel(ExcelToJsonDialog.getModel(dss.getSelectedValue()));
			}
		});
		btnexcel_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
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
		
		JScrollPane scrollPane_1 = new JScrollPane();
		splitPane_2.setRightComponent(scrollPane_1);
		
		memoView = new JTextArea();
		scrollPane_1.setViewportView(memoView);

		init();

		setLocationRelativeTo(null);
	}

	protected void removeRow() {
		DataSource dSource = getCurrentDataSource();
		if (dSource == null) {
			MsgHelper.showMessage("请先选择一个数据源！");
			return;
		}

		if (dataProviderGrid.getSelectedRow() == -1)
			return;

		if (MsgHelper.showConfirmDialog("是否删除选定的行？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		dSource.removeRow(dataProviderGrid.getSelectedRow());

	}

	protected void showFieldEditor(Object[] defines) {
		DataSource dSource = getCurrentDataSource();
		if (dSource == null) {
			MsgHelper.showMessage("请先选择一个数据源！");
			return;
		}

		List<String> valueTypeList = new ArrayList<>();
		valueTypeList.add("string");
		valueTypeList.add("int");
		valueTypeList.add("float");
		valueTypeList.add("double");
		valueTypeList.add("date");
		valueTypeList.add("bool");

		ModelResult result = KeyValueSelector.show(null, mainControl, new ICheckValue() {

			@Override
			public boolean onCheck(Object[][] originalData, JTable table) {
				String checkValue = (String) table.getValueAt(0, 1);
				if (checkValue == null || checkValue.isEmpty()) {
					MsgHelper.showMessage("字段id不能为空！");
					return false;
				}
				checkValue = (String) table.getValueAt(1, 1);
				if (checkValue == null || checkValue.isEmpty()) {
					MsgHelper.showMessage("字段名称不能为空！");
					return false;
				}
				String typename = (String) table.getValueAt(2, 1);
				if (typename == null || typename.isEmpty()) {
					MsgHelper.showMessage("字段类型不能为空！");
					return false;
				}
				int size = (int) table.getValueAt(3, 1);
				if (size == 0 && typename.compareTo("string") == 0) {
					MsgHelper.showMessage("字段类型为string时，size必须大于0！");
					return false;
				}
				return true;
			}
		}, null, new Object[][] { { "字段id", defines == null ? "" : defines[0] },
				{ "字段名称", defines == null ? "" : defines[1] },
				{ "字段类型", defines == null ? "" : defines[2], valueTypeList },
				{ "长度", defines == null ? 0 : defines[3] }, }, new Object[] { "项目", "值" }, null, null, false);

		DefaultTableModel model = result.isok ? result.model : null;

		if (model == null)
			return;

		dSource.addField((String) model.getValueAt(0, 1), (String) model.getValueAt(1, 1),
				(String) model.getValueAt(2, 1), (int) model.getValueAt(3, 1), ColumnStyle.csField);

	}

	protected void addField() {
		showFieldEditor(null);
	}

	protected void editField() {
		DataSource dSource = getCurrentDataSource();
		if (dSource == null) {
			MsgHelper.showMessage("请先选择一个数据源！");
			return;
		}

		if (dataProviderGrid.getSelectedColumn() == -1)
			return;

		JSONObject column = dSource.getField(dataProviderGrid.getSelectedColumn());
		if (column == null)
			return;

		Object[] define = new Object[] { column.getString(DataSource.COLUMN_FIELD),
				column.getString(DataSource.COLUMN_NAME), column.getString(DataSource.COLUMN_TYPE),
				column.has(DataSource.COLUMN_SIZE) ? column.getInt(DataSource.COLUMN_SIZE) : 0 };

		showFieldEditor(define);
	}

	protected void removeField() {
		DataSource dSource = getCurrentDataSource();
		if (dSource == null) {
			MsgHelper.showMessage("请先选择一个数据源！");
			return;
		}

		if (dataProviderGrid.getSelectedColumn() == -1)
			return;

		dSource.removeField(dataProviderGrid.getSelectedColumn());

	}

	protected void addRow() {
		DataSource dSource = getCurrentDataSource();
		if (dSource == null) {
			MsgHelper.showMessage("请先选择一个数据源！");
			return;
		}

		dSource.addRow();
	}

	protected void saveCurrent() {
		if (providerConfigGrid.isEditing())
			providerConfigGrid.getCellEditor().stopCellEditing();

		DefaultTableModel model = (DefaultTableModel) providerConfigGrid.getModel();
		String url = (String) model.getValueAt(0, 1);
		Object pvalue = model.getValueAt(1, 1);
		String params = null;
		if (pvalue != null)
			params = pvalue.toString();
		Object value = model.getValueAt(2, 1);
		boolean useLocal = value instanceof Boolean ? (boolean) value : Boolean.parseBoolean((String)value );

		DataSource dataSource = dss.getSelectedValue();
		dataSource.url = url;
		dataSource.memo = memoView.getText();
		dataSource.params = params == null ? null : new JSONObject(params);
		dataSource.useLocal = useLocal;
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
				if (dataSource.getType().equals(UrlDataSource.URL_KEY))
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
		UrlDataSourceConfig config = new UrlDataSourceConfig(mainControl);
		config.setModal(true);
		config.setVisible(true);
		DataSource result = config.getResult();
		config.dispose();
		return result;
	}
}
