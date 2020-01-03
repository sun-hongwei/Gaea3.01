package com.wh.gaea.plugin.excel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.wh.gaea.GlobalInstance;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.SwingTools;

import wh.excel.model.ExcelToJsonModel;
import wh.excel.parse.DefaultTemplateParser;
import wh.excel.template.CommandRuntime.Command;
import wh.excel.template.CommandRuntime.CommandType;
import wh.excel.template.CommandRuntime.Var;
import wh.excel.template.Config;
import wh.excel.template.Config.ExprType;
import wh.excel.template.Config.LoopType;
import wh.excel.template.ConfigItemTemplate;
import wh.excel.template.Template;

public class ExcelExportHelper extends JDialog {
	private static final long serialVersionUID = 1L;
	private JTextField idview;
	private JToolBar toolBar_3;
	private JComboBox<String> exprtypeView;
	private JComboBox<String> dsView;
	private JCheckBox masterloopView;
	private JCheckBox loopView;
	private JComboBox<String> dtView;
	private JComboBox<Config> refView;
	private JSpinner dsrowView;
	private JComboBox<String> commandView;
	private JComboBox<ConfigItemTemplate> templateidView;
	private JComboBox<String> fieldView;
	private JCheckBox currowView;

	Template<Config> template;
	File templateFile;
	String sheetName;

	boolean isEdit = false;

	public void addTemplate(boolean add) throws Exception {
		File file;
		if (add)
			file = SwingTools.selectSaveFile(null,
					GlobalInstance.instance().getProjectBasePath().getAbsolutePath(), null,
					"excel导出映射模板文件=xlsx;xls");
		else
			file = SwingTools.selectOpenFile(null,
					GlobalInstance.instance().getProjectBasePath().getAbsolutePath(), null,
					"excel导出映射模板文件=xlsx;xls");
		if (file == null)
			return;

		template = new Template<Config>();
		templateFile = file;

		loadTemplate(file);
	}

	public void close() {
		if (isEdit) {
			if (MsgHelper.showConfirmDialog("当前模板已经修改，是否保存？", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
				saveTemplate();
		}
		template = null;
		templateFile = null;
	}

	private JTextField tempaleIdDefineView;
	private JSpinner templateRowView;
	private JList<ConfigItemTemplate> templateRowsView;
	private JList<Var> varsView;
	private JTextField varnameView;
	private JTextField varDefaultView;
	private JSpinner globalstartyView;
	private JList<Config> configView;
	private JComboBox<String> varTypeView;
	private JSpinner varPrecisionView;
	private JTextField varDateFormatView;
	private JTextArea exprView;
	private JList<Command> commandsView;
	private JTextField dateformatView;
	private JSpinner precisionView;
	private JSpinner startxView;
	private JSpinner startyView;
	private JTextField commandvalueView;
	private JComboBox<Var> commandnameView;
	private JComboBox<String> masterdsView;
	private JComboBox<String> dssView;

	public void loadTemplate(File file) throws Exception {
		if (!file.exists())
			return;

		close();

		ExcelToJsonModel model = new ExcelToJsonModel();
		model.load(file);
		sheetName = "config";
		if (!model.sheetExist(sheetName)) {
			sheetName = MsgHelper.showInputDialog("请输入模板文件的配置页名称");
			if (sheetName == null || sheetName.isEmpty() || sheetName.equalsIgnoreCase("config"))
				return;

		}

		if (!model.sheetExist(sheetName)) {
			MsgHelper.showWarn("您输入的配置页名【" + sheetName + "】并不存在");
			return;
		}
		model.setSheet(sheetName);
		template = DefaultTemplateParser.getTemplate(model);
		templateFile = file;

		model.close();

		isEdit = false;

		globalstartyView.setValue(template.startY);

		DefaultListModel<Var> varModel = new DefaultListModel<>();
		for (Var config : template.commandRuntime.vars.values()) {
			varModel.addElement(config);
		}
		varsView.setModel(varModel);

		DefaultListModel<ConfigItemTemplate> templateRowModel = new DefaultListModel<>();
		for (ConfigItemTemplate config : template.configTemplates.values()) {
			templateRowModel.addElement(config);
		}
		templateRowsView.setModel(templateRowModel);

		DefaultListModel<Config> configModel = new DefaultListModel<>();
		for (Config config : template.configs) {
			configModel.addElement(config);
		}
		configView.setModel(configModel);

		Map<String, String> dssMap = new HashMap<String, String>();
		DefaultComboBoxModel<String> dssModel = new DefaultComboBoxModel<String>();
		if (template.masterDatasetId != null && !template.masterDatasetId.isEmpty()) {
			dssModel.addElement(template.masterDatasetId);
			dssMap.put(template.masterDatasetId, null);
		}
		for (Config config : template.configs) {
			if (config.datasetId != null && !config.datasetId.isEmpty()) {
				if (dssMap.containsKey(config.datasetId))
					continue;
				dssMap.put(config.datasetId, null);
				dssModel.addElement(config.datasetId);
			}
		}
		dssView.setModel(dssModel);
		refreshDss();

		refreshVars();
		refreshConfigs();
		refreshConfigTemplates();
	}

	protected <T> void saveModel(ListModel<T> model, Map<String, T> map) throws Exception {
		map.clear();
		for (int i = 0; i < model.getSize(); i++) {
			T var = model.getElementAt(i);
			String name;
			Class<?> c = var.getClass();
			Field field = null;
			try {
				field = c.getField("name");
			} catch (Exception e) {
			}
			if (field == null)
				field = c.getField("id");
			name = (String) field.get(var);
			map.put(name, var);
		}
	}

	protected <T> void saveModel(ListModel<T> model, List<T> data) throws Exception {
		data.clear();
		for (int i = 0; i < model.getSize(); i++) {
			T var = model.getElementAt(i);
			data.add(var);
		}
	}

	public void saveTemplate() {
		saveTemplate(templateFile);
	}

	public void saveTemplate(File file) {
		if (templateFile == null)
			return;

		template.startY = (int) globalstartyView.getValue();
		template.masterDatasetId = (String) masterdsView.getSelectedItem();

		try {
			if (templateRowsView.getSelectedValue() != null)
				saveTemplateRow(templateRowsView.getSelectedValue());

			if (varsView.getSelectedValue() != null)
				saveVar(varsView.getSelectedValue());

			if (commandsView.getSelectedValue() != null)
				saveCommand(commandsView.getSelectedValue());

			if (configView.getSelectedValue() != null)
				saveConfig(configView.getSelectedValue());

			saveModel(varsView.getModel(), template.commandRuntime.vars);
			saveModel(templateRowsView.getModel(), template.configTemplates);

			saveModel(configView.getModel(), template.configs);

			DefaultTemplateParser.saveTemplate(template, sheetName, file);
			MsgHelper.showMessage("恭喜，保存成功！");
		} catch (Exception e) {
			MsgHelper.showException(e);
		}

	}

	public void refreshVars() {
		DefaultListModel<Var> configs = (DefaultListModel<Var>) varsView.getModel();
		DefaultComboBoxModel<Var> model = new DefaultComboBoxModel<>();
		for (int i = 0; i < configs.getSize(); i++) {
			Var var = configs.get(i);
			model.addElement(var);
		}

		commandnameView.setModel(model);

	}

	public void loadVar(Var info) {
		varDefaultView.setText(info.value == null ? null : info.value.toString());
		varnameView.setText(info.name);
		varPrecisionView.setValue(info.precision);
		varTypeView.setSelectedItem(DefaultTemplateParser.classToString(info.type));
		varDateFormatView.setText(info.format);
	}

	public void saveVar() {
		if (varsView.getSelectedValue() == null)
			return;

		saveVar(varsView.getSelectedValue());
		MsgHelper.showMessage("保存成功！");
	}

	public void saveVar(Var info) {
		info.value = varDefaultView.getText();
		info.name = varnameView.getText();
		info.precision = (int) varPrecisionView.getValue();
		info.type = DefaultTemplateParser.stringToClass(varTypeView.getSelectedItem().toString());
		info.format = varDateFormatView.getText();

		refreshVars();
	}

	protected void refreshConfigTemplates() {
		DefaultListModel<ConfigItemTemplate> configs = (DefaultListModel<ConfigItemTemplate>) templateRowsView
				.getModel();
		DefaultComboBoxModel<ConfigItemTemplate> model = new DefaultComboBoxModel<>();
		model.addElement(null);
		for (int i = 0; i < configs.getSize(); i++) {
			model.addElement(configs.getElementAt(i));
		}

		templateidView.setModel(model);
	}

	public void loadTemplateRow(ConfigItemTemplate info) {
		tempaleIdDefineView.setText(info.name);
		templateRowView.setValue(info.row);
	}

	public void saveTemplateRow() {
		if (templateRowsView.getSelectedValue() == null)
			return;
		saveTemplateRow(templateRowsView.getSelectedValue());
	}

	public void saveTemplateRow(ConfigItemTemplate info) {
		info.name = tempaleIdDefineView.getText();
		info.row = (int) templateRowView.getValue();
		templateRowsView.updateUI();
		refreshConfigTemplates();
	}

	public void refreshConfigs() {
		DefaultListModel<Config> configs = (DefaultListModel<Config>) configView.getModel();
		DefaultComboBoxModel<Config> model = new DefaultComboBoxModel<>();
		model.addElement(null);

		Map<String, Object> fieldMap = new HashMap<String, Object>();
		DefaultComboBoxModel<String> fieldModel = new DefaultComboBoxModel<>();
		for (int i = 0; i < configs.getSize(); i++) {
			Config config = configs.getElementAt(i);
			model.addElement(config);
			if (config.exprType == ExprType.ttKey) {
				String expr = (String) config.expr;
				if (expr != null && !expr.isEmpty() && !fieldMap.containsKey(config.expr)) {
					fieldMap.put(expr, null);
					fieldModel.addElement(expr);
				}
			}
		}
		Object old = fieldView.getSelectedItem();
		fieldView.setModel(fieldModel);
		fieldView.setSelectedItem(old);

		old = refView.getSelectedItem();
		refView.setModel(model);
		refView.setSelectedItem(old);
	}

	public void loadConfig(Config info) {
		String expr = info.expr == null ? "" : info.expr.toString();
		exprView.setText(expr);

		if (Date.class.isAssignableFrom(info.valueType))
			dateformatView.setText(info.format);
		else {
			dateformatView.setText(null);
		}

		if (Float.class.isAssignableFrom(info.valueType) || Double.class.isAssignableFrom(info.valueType)) {
			precisionView.setValue(info.precision);
		} else {
			precisionView.setValue(0);
		}

		exprtypeView.setSelectedItem(DefaultTemplateParser.exprTypeToString(info.exprType));
		dsView.setSelectedItem(info.datasetId);
		refView.setSelectedItem(template.get(info.ref));
		dtView.setSelectedItem(DefaultTemplateParser.classToString(info.valueType));
		startxView.setValue(info.startX);
		startyView.setValue(info.startY);
		loopView.setSelected(info.loopType == LoopType.ltLoop ? true : false);
		idview.setText(info.id);
		masterloopView.setSelected(info.masterLoop);
		dsrowView.setValue(info.row);
		templateidView.setSelectedItem(template.configTemplates.get(info.templateId));

		DefaultListModel<Command> commandModel = new DefaultListModel<>();
		for (Command command : info.commands) {
			if (command.commandType == CommandType.ctInstru)
				commandModel.addElement(command);
		}
		commandsView.setModel(commandModel);
	}

	public void loadCommand(Command info) {
		commandView.setSelectedItem(info.command);
		commandvalueView.setText(info.value == null ? null : info.value.toString());
		commandnameView.setSelectedItem(info.varName);
	}

	public void saveCommand() {
		if (commandsView.getSelectedValue() == null)
			return;

		saveCommand(commandsView.getSelectedValue());
		MsgHelper.showMessage("保存成功！");
	}

	public void saveCommand(Command info) {
		info.command = commandView.getSelectedItem().toString();
		info.value = commandvalueView.getText();
		info.varName = ((Var) commandnameView.getSelectedItem()).name;

		commandsView.updateUI();
	}

	public void saveConfig() {
		try {
			if (configView.getSelectedValue() == null)
				return;
			saveConfig(configView.getSelectedValue());
			MsgHelper.showMessage("保存成功！");
		} catch (Exception e) {
			MsgHelper.showException(e);
		}
	}

	public void saveConfig(Config info) throws Exception {
		info.expr = exprView.getText();

		info.valueType = DefaultTemplateParser.stringToClass(dtView.getSelectedItem().toString());
		if (Date.class.isAssignableFrom(info.valueType)) {
			String format = dateformatView.getText();
			if (format != null && !format.isEmpty()) {
				try {
					SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
					String dataString = simpleDateFormat.format(new Date());
					if (dataString == null || dataString.isEmpty()) {
						throw new Exception("日期格式设置不正确！");
					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			info.format = format;

		} else {
			info.format = null;
		}

		if (Float.class.isAssignableFrom(info.valueType) || Double.class.isAssignableFrom(info.valueType)) {
			info.precision = (int) precisionView.getValue();
		} else {
			info.precision = 0;
		}

		info.exprType = DefaultTemplateParser.stringToExprType(exprtypeView.getSelectedItem().toString());
		info.datasetId = dsView.getSelectedItem() == null ? null : dsView.getSelectedItem().toString();
		info.ref = refView.getSelectedItem() == null ? null : refView.getSelectedItem().toString();

		info.startX = (int) startxView.getValue();
		info.startY = (int) startyView.getValue();

		info.loopType = loopView.isSelected() ? LoopType.ltLoop : LoopType.ltOne;
		info.id = idview.getText();
		info.masterLoop = masterloopView.isSelected();
		info.row = (int) dsrowView.getValue();
		info.templateId = templateidView.getSelectedItem() == null ? null : templateidView.getSelectedItem().toString();

		DefaultListModel<Command> commandModel = (DefaultListModel<Command>) commandsView.getModel();
		info.commands.clear();
		for (int i = 0; i < commandModel.size(); i++) {
			info.commands.add(commandModel.elementAt(i));
		}

		refreshConfigs();

		configView.updateUI();
	}

	protected void addComponentEvent(JSplitPane splitPane) {
		splitPane.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				splitPane.setResizeWeight(splitPane.getResizeWeight());
				splitPane.setDividerLocation(splitPane.getResizeWeight());
			}
		});
	}

	protected <T> void addItem(JList<T> list, T item) {
		DefaultListModel<T> model = (DefaultListModel<T>) list.getModel();
		model.addElement(item);
		list.updateUI();
	}

	protected <T> void addItem(JComboBox<T> list, T item) {
		DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) list.getModel();
		model.addElement(item);
		list.updateUI();
	}

	protected <T> void deleteItem(JList<T> list) {
		if (list.getSelectedValue() == null)
			return;

		if (MsgHelper.showConfirmDialog("是否删除选定的项目？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
			return;
		}

		((DefaultListModel<T>) list.getModel()).removeElement(list.getSelectedValue());
		list.updateUI();
	}

	protected void refreshDss() {
		DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) dssView.getModel();

		DefaultComboBoxModel<String> dsViewModel = new DefaultComboBoxModel<>();
		DefaultComboBoxModel<String> masterdsViewModel = new DefaultComboBoxModel<>();
		for (int i = 0; i < model.getSize(); i++) {
			dsViewModel.addElement(model.getElementAt(i));
			masterdsViewModel.addElement(model.getElementAt(i));
		}
		dsView.setModel(dsViewModel);
		masterdsView.setModel(masterdsViewModel);
	}

	/**
	 * Create the dialog.
	 */
	public ExcelExportHelper() {
		setTitle("Excel导出模板配置助手");
		setModal(true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(ExcelExportHelper.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setBounds(100, 100, 1119, 726);
		getContentPane().setLayout(new BorderLayout());

		JPanel panel_3 = new JPanel();
		getContentPane().add(panel_3, BorderLayout.CENTER);
		panel_3.setLayout(new BorderLayout(0, 0));
		JSplitPane splitPane_1 = new JSplitPane();
		panel_3.add(splitPane_1);
		splitPane_1.setResizeWeight(0.4);
		addComponentEvent(splitPane_1);
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		JPanel panel = new JPanel();
		splitPane_1.setRightComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));

		JPanel panel_11 = new JPanel();
		panel_2.add(panel_11, BorderLayout.NORTH);
		panel_11.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar_9 = new JToolBar();
		toolBar_9.setBorder(null);
		toolBar_9.setFloatable(false);
		panel_11.add(toolBar_9, BorderLayout.WEST);

		JButton button_11 = new JButton("新建");
		button_11.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addItem(configView, new Config(template));
			}
		});
		button_11.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_9.add(button_11);

		JButton button_12 = new JButton("删除");
		button_12.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteItem(configView);
			}
		});
		button_12.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_9.add(button_12);

		JButton button_18 = new JButton("保存");
		button_18.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveConfig();
			}
		});
		button_18.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_9.add(button_18);

		JLabel label_16 = new JLabel("导出项目配置");
		label_16.setFont(new Font("微软雅黑", Font.PLAIN, 20));
		label_16.setHorizontalAlignment(SwingConstants.CENTER);
		panel_11.add(label_16, BorderLayout.CENTER);

		JPanel panel_12 = new JPanel();
		panel_2.add(panel_12, BorderLayout.CENTER);
		panel_12.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane_3 = new JSplitPane();
		splitPane_3.setResizeWeight(0.2);
		addComponentEvent(splitPane_3);

		panel_12.add(splitPane_3, BorderLayout.CENTER);

		JPanel panel_13 = new JPanel();
		splitPane_3.setRightComponent(panel_13);
		panel_13.setLayout(new BorderLayout(0, 0));

		JPanel panel_4 = new JPanel();
		panel_13.add(panel_4, BorderLayout.CENTER);
		panel_4.setLayout(new BorderLayout(0, 0));

		JPanel panel_14 = new JPanel();
		panel_4.add(panel_14, BorderLayout.SOUTH);
		panel_14.setLayout(new BorderLayout(0, 0));
		JToolBar toolBar = new JToolBar();
		panel_14.add(toolBar, BorderLayout.NORTH);
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.setFloatable(false);

		JLabel label = new JLabel(" 类型 ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label);

		exprtypeView = new JComboBox<String>();
		exprtypeView.setMinimumSize(new Dimension(150, 27));
		exprtypeView.setPreferredSize(new Dimension(150, 27));
		exprtypeView.setEditable(true);
		exprtypeView.setModel(new DefaultComboBoxModel<>(new String[] { "常量表达式", "数据字段名称", "变量表达式" }));
		exprtypeView.setSelectedIndex(1);
		exprtypeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(exprtypeView);

		JLabel label_8 = new JLabel(" 模板编号 ");
		toolBar.add(label_8);
		label_8.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		templateidView = new JComboBox<>();
		toolBar.add(templateidView);
		templateidView.setPreferredSize(new Dimension(200, 0));
		templateidView.setMinimumSize(new Dimension(200, 0));
		templateidView.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JLabel lblx = new JLabel(" 表格位置 (X) ");
		lblx.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(lblx);

		startxView = new JSpinner();
		startxView.setPreferredSize(new Dimension(80, 0));
		startxView.setMinimumSize(new Dimension(80, 0));
		startxView.setMaximumSize(new Dimension(100, 32767));
		startxView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		startxView.setEnabled(false);
		toolBar.add(startxView);

		JLabel lbly = new JLabel(" 表格位置 (Y) ");
		lbly.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(lbly);

		startyView = new JSpinner();
		startyView.setPreferredSize(new Dimension(80, 0));
		startyView.setMinimumSize(new Dimension(80, 0));
		startyView.setMaximumSize(new Dimension(100, 32767));
		startyView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		startyView.setEnabled(false);
		toolBar.add(startyView);

		JToolBar toolBar_1 = new JToolBar();
		panel_14.add(toolBar_1, BorderLayout.CENTER);
		toolBar_1.setFloatable(false);
		toolBar_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JLabel label_3 = new JLabel(" 引用 ");
		toolBar_1.add(label_3);
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		refView = new JComboBox<>();
		refView.setPreferredSize(new Dimension(200, 27));
		refView.setMinimumSize(new Dimension(200, 27));
		refView.setSelectedIndex(-1);
		refView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(refView);

		JLabel label_2 = new JLabel(" 数据类型 ");
		toolBar_1.add(label_2);
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		dtView = new JComboBox<String>();
		dtView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (precisionView == null)
					return;

				precisionView.setEnabled(false);
				dateformatView.setEnabled(false);

				switch (dtView.getSelectedItem().toString()) {
				case "float":
					precisionView.setEnabled(true);
					break;
				case "date":
					dateformatView.setEnabled(true);
					break;
				}
			}
		});
		dtView.setModel(new DefaultComboBoxModel<String>(new String[] { "string", "int", "float", "boolean", "date" }));
		dtView.setSelectedIndex(0);
		dtView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(dtView);

		JLabel label_17 = new JLabel(" 日期格式 ");
		label_17.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(label_17);

		dateformatView = new JTextField();
		dateformatView.setPreferredSize(new Dimension(200, 27));
		dateformatView.setMinimumSize(new Dimension(200, 27));
		dateformatView.setMaximumSize(new Dimension(250, 2147483647));
		dateformatView.setEnabled(false);
		dateformatView.setText("yyyy-MM-dd HH:mm:ss");
		dateformatView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(dateformatView);

		JLabel label_19 = new JLabel(" 精度 ");
		label_19.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(label_19);

		precisionView = new JSpinner();
		precisionView.setEnabled(false);
		precisionView.setPreferredSize(new Dimension(80, 0));
		precisionView.setMinimumSize(new Dimension(80, 0));
		precisionView.setMaximumSize(new Dimension(100, 32767));
		precisionView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(precisionView);

		JToolBar toolBar_10 = new JToolBar();
		toolBar_10.setFloatable(false);
		panel_14.add(toolBar_10, BorderLayout.SOUTH);

		JLabel lblId = new JLabel(" 编号 ");
		toolBar_10.add(lblId);
		lblId.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		idview = new JTextField();
		idview.setMinimumSize(new Dimension(200, 27));
		idview.setPreferredSize(new Dimension(200, 27));
		toolBar_10.add(idview);
		idview.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		loopView = new JCheckBox("循环数据集");
		toolBar_10.add(loopView);
		loopView.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		masterloopView = new JCheckBox("循环主数据集");
		toolBar_10.add(masterloopView);
		masterloopView.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JLabel lblid = new JLabel(" 数据源 ");
		toolBar_10.add(lblid);
		lblid.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		dsView = new JComboBox<String>();
		toolBar_10.add(dsView);
		dsView.setSelectedIndex(-1);
		dsView.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JLabel label_4 = new JLabel(" 数据行号 ");
		toolBar_10.add(label_4);
		label_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		dsrowView = new JSpinner();
		toolBar_10.add(dsrowView);
		dsrowView.setMaximumSize(new Dimension(100, 32767));
		dsrowView.setPreferredSize(new Dimension(80, 0));
		dsrowView.setMinimumSize(new Dimension(80, 0));
		dsrowView.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JPanel panel_15 = new JPanel();
		panel_4.add(panel_15, BorderLayout.CENTER);
		panel_15.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar_2 = new JToolBar();
		toolBar_2.setBorder(null);
		panel_15.add(toolBar_2, BorderLayout.NORTH);
		toolBar_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.setFloatable(false);

		JButton button_1 = new JButton("数据");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exprView.insert(
						"${" + (fieldView.getSelectedItem() == null ? "field" : (String) fieldView.getSelectedItem())
								+ "[" + (currowView.isSelected() ? "cur" : 0) + "]}",
						exprView.getCaretPosition());
			}
		});
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(button_1);

		JButton button = new JButton("表格");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exprView.insert(
						"@{" + (fieldView.getSelectedItem() == null ? "field" : (String) fieldView.getSelectedItem())
								+ "[" + (currowView.isSelected() ? "cur" : 0) + "]}",
						exprView.getCaretPosition());
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(button);

		JButton button_2 = new JButton("配置");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exprView.insert(
						"#{" + (fieldView.getSelectedItem() == null ? "field" : (String) fieldView.getSelectedItem())
								+ "[" + (currowView.isSelected() ? "cur" : 0) + "]}",
						exprView.getCaretPosition());
			}
		});
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(button_2);

		JButton button_21 = new JButton("字段");
		button_21.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exprView.insert(fieldView.getSelectedItem() == null ? "field" : (String) fieldView.getSelectedItem(),
						exprView.getCaretPosition());
			}
		});
		button_21.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(button_21);

		currowView = new JCheckBox("插入当前行");
		currowView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(currowView);

		toolBar_2.addSeparator();

		JLabel label_7 = new JLabel(" 字段 ");
		label_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(label_7);

		fieldView = new JComboBox<>();
		fieldView.setEditable(true);
		fieldView.setSelectedIndex(-1);
		fieldView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_2.add(fieldView);

		JPanel panel_16 = new JPanel();
		panel_15.add(panel_16, BorderLayout.CENTER);
		panel_16.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane_4 = new JSplitPane();
		splitPane_4.setResizeWeight(0.35);
		addComponentEvent(splitPane_4);
		panel_16.add(splitPane_4, BorderLayout.CENTER);

		JScrollPane scrollPane_1 = new JScrollPane();
		splitPane_4.setLeftComponent(scrollPane_1);

		exprView = new JTextArea();
		scrollPane_1.setViewportView(exprView);

		JPanel panel_17 = new JPanel();
		splitPane_4.setRightComponent(panel_17);
		panel_17.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar_11 = new JToolBar();
		toolBar_11.setFloatable(false);
		panel_17.add(toolBar_11, BorderLayout.SOUTH);

		JLabel label_5 = new JLabel(" 控制指令 ");
		toolBar_11.add(label_5);
		label_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		commandView = new JComboBox<>();
		commandView.setModel(new DefaultComboBoxModel<>(new String[] { "get", "set" }));
		commandView.setSelectedIndex(0);
		commandView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_11.add(commandView);

		JLabel label_1 = new JLabel(" 变量 ");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_11.add(label_1);

		commandnameView = new JComboBox<>();
		commandnameView.setMaximumSize(new Dimension(200, 32767));
		commandnameView.setSelectedIndex(-1);
		commandnameView.setPreferredSize(new Dimension(150, 27));
		commandnameView.setMinimumSize(new Dimension(150, 27));
		commandnameView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_11.add(commandnameView);

		JLabel label_21 = new JLabel(" 值 ");
		label_21.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_11.add(label_21);

		commandvalueView = new JTextField();
		commandvalueView.setPreferredSize(new Dimension(100, 27));
		commandvalueView.setMinimumSize(new Dimension(100, 27));
		commandvalueView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_11.add(commandvalueView);

		JButton button_13 = new JButton("新建");
		button_13.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addItem(commandsView, new Command());
			}
		});
		button_13.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_11.add(button_13);

		JButton button_14 = new JButton("删除");
		button_14.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteItem(commandsView);
			}
		});
		button_14.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_11.add(button_14);

		JButton button_20 = new JButton("保存");
		button_20.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveCommand();
			}
		});
		button_20.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_11.add(button_20);

		JScrollPane scrollPane_5 = new JScrollPane();
		panel_17.add(scrollPane_5, BorderLayout.CENTER);

		commandsView = new JList<>();
		commandsView.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				if (commandsView.getSelectedValue() == null)
					return;

				loadCommand(commandsView.getSelectedValue());
			}
		});
		scrollPane_5.setViewportView(commandsView);

		JScrollPane scrollPane_4 = new JScrollPane();
		splitPane_3.setLeftComponent(scrollPane_4);

		configView = new JList<>();
		configView.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				if (configView.getSelectedValue() == null)
					return;

				loadConfig(configView.getSelectedValue());
			}
		});
		scrollPane_4.setViewportView(configView);

		JSplitPane splitPane_2 = new JSplitPane();
		splitPane_2.setResizeWeight(0.3);
		addComponentEvent(splitPane_2);
		splitPane_1.setLeftComponent(splitPane_2);

		JPanel panel_5 = new JPanel();
		splitPane_2.setLeftComponent(panel_5);
		panel_5.setLayout(new BorderLayout(0, 0));

		JPanel panel_6 = new JPanel();
		panel_5.add(panel_6, BorderLayout.NORTH);
		panel_6.setLayout(new BorderLayout(0, 0));

		JLabel lblNewLabel_1 = new JLabel("模板行配置");
		lblNewLabel_1.setFont(new Font("微软雅黑", Font.PLAIN, 20));
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		panel_6.add(lblNewLabel_1);

		JToolBar toolBar_5 = new JToolBar();
		toolBar_5.setBorder(null);
		toolBar_5.setFloatable(false);
		panel_6.add(toolBar_5, BorderLayout.WEST);

		JButton button_5 = new JButton("新建");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addItem(templateRowsView, new ConfigItemTemplate());
				refreshConfigTemplates();
			}
		});
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_5.add(button_5);

		JButton button_8 = new JButton("删除");
		button_8.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteItem(templateRowsView);
				refreshConfigTemplates();
			}
		});
		button_8.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_5.add(button_8);

		JButton button_15 = new JButton("保存");
		button_15.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveTemplateRow();
			}
		});
		button_15.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_5.add(button_15);

		JPanel panel_7 = new JPanel();
		panel_5.add(panel_7, BorderLayout.SOUTH);
		panel_7.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar_6 = new JToolBar();
		toolBar_6.setFloatable(false);
		panel_7.add(toolBar_6);

		JLabel label_10 = new JLabel(" 数据行号 ");
		label_10.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_6.add(label_10);

		templateRowView = new JSpinner();
		templateRowView.setMinimumSize(new Dimension(50, 28));
		templateRowView.setPreferredSize(new Dimension(50, 28));
		templateRowView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_6.add(templateRowView);

		JLabel label_11 = new JLabel(" 模板编号 ");
		label_11.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_6.add(label_11);

		tempaleIdDefineView = new JTextField();
		tempaleIdDefineView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		tempaleIdDefineView.setColumns(10);
		toolBar_6.add(tempaleIdDefineView);

		JScrollPane scrollPane_2 = new JScrollPane();
		panel_5.add(scrollPane_2, BorderLayout.CENTER);

		templateRowsView = new JList<>();
		templateRowsView.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				if (templateRowsView.getSelectedValue() == null)
					return;

				loadTemplateRow(templateRowsView.getSelectedValue());
			}
		});
		scrollPane_2.setViewportView(templateRowsView);

		JPanel panel_8 = new JPanel();
		splitPane_2.setRightComponent(panel_8);
		panel_8.setLayout(new BorderLayout(0, 0));

		JPanel panel_9 = new JPanel();
		panel_8.add(panel_9, BorderLayout.NORTH);
		panel_9.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar_7 = new JToolBar();
		toolBar_7.setBorder(null);
		toolBar_7.setFloatable(false);
		panel_9.add(toolBar_7, BorderLayout.WEST);

		JButton button_9 = new JButton("新建");
		button_9.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addItem(varsView, new Var());
				refreshVars();
			}
		});
		button_9.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_7.add(button_9);

		JButton button_10 = new JButton("删除");
		button_10.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteItem(varsView);
				refreshVars();
			}
		});
		button_10.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_7.add(button_10);

		JButton button_19 = new JButton("保存");
		button_19.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveVar();
			}
		});
		button_19.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_7.add(button_19);

		JLabel label_12 = new JLabel("变量定义配置");
		label_12.setFont(new Font("微软雅黑", Font.PLAIN, 20));
		label_12.setHorizontalAlignment(SwingConstants.CENTER);
		panel_9.add(label_12, BorderLayout.CENTER);

		JScrollPane scrollPane_3 = new JScrollPane();
		panel_8.add(scrollPane_3, BorderLayout.CENTER);

		varsView = new JList<>();
		varsView.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				if (varsView.getSelectedValue() == null)
					return;

				loadVar(varsView.getSelectedValue());
			}
		});
		scrollPane_3.setViewportView(varsView);

		JPanel panel_10 = new JPanel();
		panel_8.add(panel_10, BorderLayout.SOUTH);
		panel_10.setLayout(new BorderLayout(0, 0));

		JToolBar toolBar_8 = new JToolBar();
		toolBar_8.setFloatable(false);
		panel_10.add(toolBar_8, BorderLayout.CENTER);

		JLabel label_13 = new JLabel(" 缺省值 ");
		toolBar_8.add(label_13);
		label_13.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		varDefaultView = new JTextField();
		varDefaultView.setMaximumSize(new Dimension(100, 2147483647));
		varDefaultView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		varDefaultView.setColumns(10);
		toolBar_8.add(varDefaultView);

		JLabel label_18 = new JLabel(" 变量名称 ");
		label_18.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_8.add(label_18);

		varnameView = new JTextField();
		varnameView.setMaximumSize(new Dimension(100, 2147483647));
		toolBar_8.add(varnameView);
		varnameView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		varnameView.setColumns(10);

		JLabel label_14 = new JLabel(" 格式 ");
		toolBar_8.add(label_14);
		label_14.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		varDateFormatView = new JTextField();
		varDateFormatView.setEnabled(false);
		varDateFormatView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		varDateFormatView.setColumns(10);
		toolBar_8.add(varDateFormatView);

		JLabel label_6 = new JLabel(" 精度 ");
		label_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_8.add(label_6);

		varPrecisionView = new JSpinner();
		varPrecisionView.setEnabled(false);
		varPrecisionView.setPreferredSize(new Dimension(80, 0));
		varPrecisionView.setMinimumSize(new Dimension(80, 0));
		varPrecisionView.setMaximumSize(new Dimension(60, 32767));
		varPrecisionView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_8.add(varPrecisionView);

		JLabel label_15 = new JLabel(" 数据类型 ");
		label_15.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_8.add(label_15);

		varTypeView = new JComboBox<String>();
		varTypeView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				varPrecisionView.setEnabled(false);
				varDateFormatView.setEnabled(false);

				switch (varTypeView.getSelectedItem().toString()) {
				case "float":
					varPrecisionView.setEnabled(true);
					break;
				case "date":
					varDateFormatView.setEnabled(true);
					break;
				}
			}
		});
		varTypeView.setModel(new DefaultComboBoxModel<>(new String[] { "string", "int", "float", "boolean", "date" }));
		varTypeView.setSelectedIndex(0);
		varTypeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_8.add(varTypeView);

		JToolBar toolBar_4 = new JToolBar();
		toolBar_4.setFloatable(false);
		panel_3.add(toolBar_4, BorderLayout.NORTH);

		JLabel label_9 = new JLabel(" 默认起始行号 ");
		label_9.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_4.add(label_9);

		globalstartyView = new JSpinner();
		globalstartyView.setMaximumSize(new Dimension(100, 32767));
		globalstartyView.setMinimumSize(new Dimension(100, 28));
		globalstartyView.setPreferredSize(new Dimension(100, 28));
		globalstartyView.setModel(new SpinnerNumberModel(new Integer(2), null, null, new Integer(1)));
		globalstartyView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_4.add(globalstartyView);

		JLabel label_20 = new JLabel(" 主数据源 ");
		label_20.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_4.add(label_20);

		masterdsView = new JComboBox<>();
		masterdsView.setSelectedIndex(-1);
		masterdsView.setPreferredSize(new Dimension(200, 27));
		masterdsView.setMinimumSize(new Dimension(200, 27));
		masterdsView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_4.add(masterdsView);
		toolBar_3 = new JToolBar();
		toolBar_3.setBorder(null);
		toolBar_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_3.setFloatable(false);
		getContentPane().add(toolBar_3, BorderLayout.NORTH);

		JButton button_3 = new JButton("新建");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					addTemplate(true);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_3.add(button_3);

		toolBar_3.addSeparator();

		JButton button_6 = new JButton("装载");
		button_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					addTemplate(false);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_3.add(button_6);

		JButton button_7 = new JButton("保存");
		toolBar_3.add(button_7);
		button_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveTemplate();
			}
		});
		button_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JButton button_4 = new JButton("另存为");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File file = SwingTools.selectSaveFile(null, GlobalInstance.instance().getProjectBasePath().getAbsolutePath(),
						null, "excel导出映射模板文件=xlsx;xls");

				if (file == null)
					return;

				if (file.exists())
					if (!file.delete()) {
						MsgHelper.showException("错误", "不能删除文件【" + file.getAbsolutePath() + "】");
						return;
					}
				saveTemplate(file);

			}
		});
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_3.add(button_4);

		toolBar_3.addSeparator();

		JLabel lbls = new JLabel(" 数据源(s) ");
		toolBar_3.add(lbls);
		lbls.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		dssView = new JComboBox<>();
		toolBar_3.add(dssView);
		dssView.setSelectedIndex(-1);
		dssView.setPreferredSize(new Dimension(200, 27));
		dssView.setMinimumSize(new Dimension(200, 27));
		dssView.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JButton button_16 = new JButton("新建");
		toolBar_3.add(button_16);
		button_16.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = MsgHelper.showInputDialog("请输入新数据源名称");
				if (name == null || name.isEmpty())
					return;

				DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) dssView.getModel();
				for (int i = 0; i < model.getSize(); i++) {
					String dsName = model.getElementAt(i);
					if (dsName.equalsIgnoreCase(name)) {
						MsgHelper.showWarn("输入的数据源名字已经存在！");
						return;
					}
				}

				if (model.getSize() == 0)
					model.addElement("");

				model.addElement(name);

				refreshDss();
			}
		});
		button_16.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JButton button_17 = new JButton("删除");
		toolBar_3.add(button_17);
		button_17.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dssView.getSelectedItem() == null)
					return;

				if (MsgHelper.showConfirmDialog("是否删除选定的数据源？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

				DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) dssView.getModel();
				model.removeElement(dssView.getSelectedItem());

				refreshDss();
			}
		});
		button_17.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		setLocationRelativeTo(null);
	}

	public static void showModal() {
		ExcelExportHelper helper = new ExcelExportHelper();
		helper.setModal(true);
		helper.setVisible(true);
	}
}
