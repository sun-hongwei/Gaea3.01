package com.wh.gaea.form;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellEditor;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.control.ChangeCanvasConfigure;
import com.wh.gaea.control.EditorEnvironment;
import com.wh.gaea.control.IconComboBoxItem;
import com.wh.gaea.control.IconComboBoxRender;
import com.wh.gaea.control.ScrollToolBar;
import com.wh.gaea.control.grid.ButtonColumn.ButtonLabel;
import com.wh.gaea.control.grid.design.DefaultPropertyClient;
import com.wh.gaea.control.grid.design.DefaultPropertyClient.PropertyInfo;
import com.wh.gaea.control.grid.design.PropertyPanel;
import com.wh.gaea.datasource.DataSource;
import com.wh.gaea.datasource.DataSourceManager;
import com.wh.gaea.draw.DrawInfo;
import com.wh.gaea.draw.IUINode;
import com.wh.gaea.draws.DrawCanvas;
import com.wh.gaea.draws.UICanvas;
import com.wh.gaea.draws.UINode;
import com.wh.gaea.draws.drawinfo.ReportInfo;
import com.wh.gaea.draws.drawinfo.ReportInfo.CellInfo;
import com.wh.gaea.draws.drawinfo.ReportInfo.IClick;
import com.wh.gaea.editor.JsonEditorDialog;
import com.wh.gaea.interfaces.ChangeType;
import com.wh.gaea.interfaces.Config;
import com.wh.gaea.interfaces.IDrawCanvas;
import com.wh.gaea.interfaces.IDrawInfoDefines;
import com.wh.gaea.interfaces.IDrawNode;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.gaea.interfaces.IMainMenuOperation;
import com.wh.gaea.interfaces.INode;
import com.wh.gaea.interfaces.IOnPageSizeChanged;
import com.wh.gaea.interfaces.IScroll;
import com.wh.gaea.interfaces.ISubForm;
import com.wh.gaea.interfaces.PageSizeMode;
import com.wh.gaea.interfaces.ShowType;
import com.wh.gaea.selector.RadioSelector;
import com.wh.gaea.selector.RadioSelector.Result;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.SwingTools;
import com.wh.tools.JsonHelp;

public class ReportBuilder extends ChildForm implements IMainMenuOperation, ISubForm {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	HashMap<String, String> names = new HashMap<>();
	private JComboBox<IconComboBoxItem> controls;
	private JButton addButton;
	private JButton delButton;
	private JComboBox<String> pageMode;
	private JLabel label;
	private JLabel label_1;
	private JSplitPane splitPane;
	private JPanel panel;
	private JPanel panel_1;
	private PropertyPanel table;
	private JButton button;
	private JButton button_1;
	private JScrollBar hScrollBar;
	private JScrollBar vScrollBar;
	private JLabel label_2;
	private JSpinner customwidth;
	private JLabel label_3;
	private JSpinner customheight;
	private JButton button_4;
	private JButton button_5;
	private JButton editButton;
	private JPopupMenu popupMenu;
	private JMenuItem menuItem;
	private JMenuItem menuItem_1;
	private JMenuItem mntmxy;
	private JMenuItem menuNewTemplateItem;
	private JMenu menuDelTemplateItem;
	private JMenuItem mntmApplyTemplateItem;
	private JSeparator separator_2;
	private JMenu menu;
	private JButton button_6;
	private JButton button_2;
	private JComboBox<String> dataProviders;
	private JLabel label_4;
	private JButton button_3;
	private JButton button_7;
	private JCheckBox autoSizeButton;
	private JLabel sizeText;

	public UICanvas canvas = new UICanvas();

	boolean isEdit = false;

	UINode sourceNode;
	ReportInfo reportInfo;
	UINode editNode;
	boolean inited = false;
	UIBuilder uiBuilder;

	File file;

	public String getId() {
		return reportInfo.id;
	}

	boolean notFire = false;

	public void changePageToCustom() {
		notFire = true;
		pageMode.setSelectedItem(DrawCanvas.pageSizeToString(PageSizeMode.psCustom));
		notFire = false;
		changePage(PageSizeMode.psCustom);
	}

	public void changePage() {
		if (notFire)
			return;

		String text = (String) pageMode.getSelectedItem();
		if (text == null)
			return;

		PageSizeMode pageSize = DrawCanvas.StringToPageSize(text);
		changePage(pageSize);
	}

	public void changePage(PageSizeMode pageSize, int width, int height) {
		int dpi = (int) dpiControl.getValue();
		if (dpiControl.getEditor().isShowing()) {
			NumberEditor editor = (NumberEditor) dpiControl.getEditor();
			try {
				editor.commitEdit();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		canvas.getPageConfig().deviceDPI = dpi;
		canvas.getPageConfig().setPageSizeMode(pageSize, width, height);
		customwidth.setValue(canvas.getConfigPageSize().width);
		customheight.setValue(canvas.getConfigPageSize().height);
		for (IDrawNode tmp : canvas.nodes.values()) {
			UINode node = (UINode) tmp;
			node.invalidRect();
		}

		canvas.repaint();
		isEdit = true;
	}

	public void changePage(PageSizeMode pageSize) {
		int width = 0;
		int height = 0;
		if (pageSize == PageSizeMode.psCustom) {
			if (customwidth.getEditor().isShowing()) {
				NumberEditor editor = (NumberEditor) customwidth.getEditor();
				try {
					editor.commitEdit();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			if (customheight.getEditor().isShowing()) {
				NumberEditor editor = (NumberEditor) customheight.getEditor();
				try {
					editor.commitEdit();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			width = (int) customwidth.getValue();
			height = (int) customheight.getValue();
			if (width <= 0)
				width = 1024;

			if (height <= 0)
				height = 768;
		}

		changePage(pageSize, width, height);
	}

	protected void keyPressed(KeyEvent e) {
		if (e.getSource() == table.getTable() && e.isAltDown()) {
			TableCellEditor editor = table.getTable().getCellEditor();
			if (editor != null)
				editor.stopCellEditing();
			return;
		}

		if (!canvas.hasFocus())
			canvas.keyPressed(e);
	}

	protected void keyReleased(KeyEvent e) {
		if (!canvas.hasFocus())
			canvas.keyReleased(e);
	}

	public void saveToFile() {
		if (this.file != null) {
			saveToFile(this.file);
		} else {
			saveAsToFile();
		}
	}

	public void saveAsToFile() {
		File file = SwingTools.selectSaveFile(null,
				EditorEnvironment
						.getProjectPath(EditorEnvironment.getCurrentProjectName(), EditorEnvironment.Report_Dir_Path)
						.getAbsolutePath(),
				"saveRepot", "报表格式=rpt");
		saveToFile(file);
	}

	public void saveToFile(File file) {
		if (file == null)
			return;

		if (file.exists())
			if (!file.delete()) {
				MsgHelper.showMessage("不能删除文件：" + file.getAbsolutePath());
				return;
			}

		try {
			JSONObject data = reportInfo.toJson();
			JsonHelp.saveJson(file, data, null);
			this.file = file;

			isEdit = false;
		} catch (Exception e1) {
			e1.printStackTrace();
			MsgHelper.showException(e1);
		}
	}

	public void loadFromFile() {
		File file = SwingTools.selectOpenFile(null, EditorEnvironment.getProjectReportPath().getAbsolutePath(), "saveRepot",
				"报表格式=rpt");
		loadFromFile(file);
	}

	protected void loadFromFile(File file) {
		if (file == null)
			return;

		if (file.exists()) {
			try {
				JSONObject data = (JSONObject) JsonHelp.parseCacheJson(file, null);
				reportInfo.fromJson(data);
				Dimension size = new Dimension((int) reportInfo.getWidth(), (int) reportInfo.getHeight());
				changePage(PageSizeMode.psCustom, size.width + (int) IDrawCanvas.LINEWIDTH * 2,
						size.height + (int) IDrawCanvas.LINEWIDTH * 2);
				Point start = canvas.getClipRect().getLocation();
				Rectangle rect = new Rectangle(start.x, start.y, size.width, size.height);
				editNode.setRect(rect);
				canvas.repaint();

				for (String name : reportInfo.getDataSources()) {
					this.dataProviders.addItem(name);
				}

				if (sourceNode == null) {
					this.file = file;
					isEdit = false;
				}else {
					isEdit = true;
				}

				canvas.getACM().reset();
			} catch (Exception e1) {
				e1.printStackTrace();
				MsgHelper.showException(e1);
			}
		}
	}

	protected List<DataSource> getDataSources() {
		return DataSourceManager.getDSM().gets(reportInfo.getDataSources());
	}

	protected void doReportClick() {
		try {
			UINode node = reportInfo.getSelected();
			if (node == null)
				node = editNode;
			if (uiBuilder != null)
				DefaultPropertyClient.propertyBuilder(table, node, uiBuilder.workflowNode, uiBuilder.workflowNodes,
						uiBuilder.workflowRelationTitle, getDataSources());
			else
				DefaultPropertyClient.propertyBuilder(table, node, null, new HashMap<>(), "", getDataSources());
			CellInfo info = reportInfo.getSelectCellInfo();
			if (info != null) {
				sizeText.setText(String.valueOf(info.getWidth()) + " * " + String.valueOf(info.getHeight()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void initEditMenu() {
		popupMenu = new JPopupMenu();
		popupMenu.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				menuDelTemplateItem.removeAll();
				for (String name : reportInfo.getTemplateNames()) {
					JMenuItem item = new JMenuItem(name);
					menuDelTemplateItem.add(item);
					item.setActionCommand(name);
					item.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							String name = e.getActionCommand();
							if (MsgHelper.showConfirmDialog("是否删除模板【" + name + "】",
									JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
								reportInfo.removeTemplate(name);
							}

						}
					});
				}

				mntmApplyTemplateItem.removeAll();
				for (String name : reportInfo.getTemplateNames()) {
					JMenuItem item = new JMenuItem(name);
					mntmApplyTemplateItem.add(item);
					item.setActionCommand(name);
					item.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							String name = e.getActionCommand();
							if (MsgHelper.showConfirmDialog("是否应用模板【" + name + "】",
									JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
								reportInfo.applyTemplate(name);
							}

						}
					});
				}

			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// TODO Auto-generated method stub

			}
		});
		menuItem = new JMenuItem("等高");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String tmp = MsgHelper.showInputDialog("请输入高度：");
				if (tmp == null || tmp.isEmpty())
					return;

				try {
					int size = Integer.parseInt(tmp);
					reportInfo.equalHeight(size);
					canvas.repaint();
				} catch (Exception ee) {
					MsgHelper.showMessage(null, "输入格式错误", "提示", JOptionPane.ERROR_MESSAGE);
				}

			}
		});
		popupMenu.add(menuItem);

		menuItem_1 = new JMenuItem("等宽");
		menuItem_1.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String tmp = MsgHelper.showInputDialog("请输入宽度：");
				if (tmp == null || tmp.isEmpty())
					return;

				try {
					int size = Integer.parseInt(tmp);
					reportInfo.equalWidth(size);
					canvas.repaint();
				} catch (Exception ee) {
					MsgHelper.showMessage(null, "输入格式错误", "提示", JOptionPane.ERROR_MESSAGE);
				}

			}
		});
		popupMenu.add(menuItem_1);

		popupMenu.addSeparator();

		mntmxy = new JMenuItem("单元格数量（X*Y）");
		mntmxy.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String tmp = MsgHelper.showInputDialog("请输入单元格数量，格式（行数*列数）：",
						String.valueOf(reportInfo.rowCount()) + "*" + String.valueOf(reportInfo.colCount()));
				if (tmp == null || tmp.isEmpty())
					return;

				try {
					String[] tmps = tmp.split("\\*");

					int rows = Integer.parseInt(tmps[0].trim());
					int cols = Integer.parseInt(tmps[1].trim());
					canvas.beginPaint();
					reportInfo.setRowCount(rows);
					reportInfo.setColCount(cols);
					canvas.endPaint();
				} catch (Exception ee) {
					MsgHelper.showMessage(null, "输入格式错误", "提示", JOptionPane.ERROR_MESSAGE);
				}

			}
		});
		popupMenu.add(mntmxy);

		separator_2 = new JSeparator();
		popupMenu.add(separator_2);

		menu = new JMenu("模板");
		popupMenu.add(menu);

		menuNewTemplateItem = new JMenuItem("新建模板");
		menuNewTemplateItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String tmp = MsgHelper.showInputDialog("请输入新模板名称：", "newTemplate");
				if (tmp == null || tmp.isEmpty())
					return;

				reportInfo.addTemplate(tmp);
			}
		});
		menu.add(menuNewTemplateItem);

		menuDelTemplateItem = new JMenu("删除模板");
		menu.add(menuDelTemplateItem);

		mntmApplyTemplateItem = new JMenu("应用模板");
		menu.add(mntmApplyTemplateItem);

		menu.addSeparator();

		menu.add(new JMenu("保存模板")).addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				File file = SwingTools.selectSaveFile(null, null, "saveTemplate", "wtt");
				if (file == null)
					return;

				JSONObject data = new JSONObject();
				try {
					for (String name : reportInfo.getTemplateNames()) {
						data.put(name, reportInfo.getTemplate(name).toJson());
					}
					JsonHelp.saveJson(file, data, null);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		menu.add(new JMenu("装载模板")).addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				File file = SwingTools.selectOpenFile(null, null, "saveTemplate", "wtt");
				if (file == null)
					return;

				JSONObject data = new JSONObject();
				try {
					for (String name : reportInfo.getTemplateNames()) {
						data.put(name, reportInfo.getTemplate(name).toJson());
					}
					JsonHelp.saveJson(file, data, null);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});

		popupMenu.addSeparator();
		JMenuItem resizeMenuItem = new JMenuItem("自动尺寸");
		resizeMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				reportInfo.autoRect(true);
			}
		});

		popupMenu.add(resizeMenuItem);

	}

	public ReportBuilder(IMainControl mainControl) {
		super(mainControl);
		setTitle("报表设计");
		setBounds(100, 100, 1776, 791);

		initEditMenu();

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel toolPanel = new JPanel();
		JPanel scrollButtonPanel = new JPanel();
		JScrollPane toolbarScrollBar = new JScrollPane();
		toolbarScrollBar.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		toolbarScrollBar.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		JToolBar toolBar = new JToolBar();

		contentPane.add(toolPanel, BorderLayout.NORTH);
		toolPanel.setLayout(new BorderLayout(0, 0));
		toolPanel.add(toolbarScrollBar, BorderLayout.CENTER);
		toolbarScrollBar.setViewportView(toolBar);
		new ScrollToolBar(toolPanel, scrollButtonPanel, toolbarScrollBar, toolBar);

		label = new JLabel("页面：");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label);

		pageMode = new JComboBox<String>(new DefaultComboBoxModel<String>(IDrawCanvas.PAGENAMES));
		pageMode.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		Dimension d = pageMode.getPreferredSize();
		pageMode.setMaximumSize(new Dimension(100, d.height));
		pageMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changePage();
			}
		});
		toolBar.add(pageMode);

		toolBar.addSeparator(new Dimension(5, 0));

		toolBar.addSeparator(new Dimension(5, 0));

		label_2 = new JLabel("宽度 ");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_2);

		customwidth = new JSpinner();
		customwidth.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		customwidth.setMaximumSize(new Dimension(60, 32767));
		customwidth.setMinimumSize(new Dimension(80, 22));
		toolBar.add(customwidth);

		label_3 = new JLabel(" 高度 ");
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_3);

		customheight = new JSpinner();
		customheight.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		customheight.setMaximumSize(new Dimension(60, 32767));
		customheight.setMinimumSize(new Dimension(80, 0));
		toolBar.add(customheight);

		label_5 = new JLabel(" 密度 ");
		label_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_5);

		dpiControl = new JSpinner();
		dpiControl.setMinimumSize(new Dimension(80, 0));
		dpiControl.setMaximumSize(new Dimension(80, 32767));
		dpiControl.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(dpiControl);

		button_9 = new JButton(" 应用 ");
		button_9.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changePageToCustom();
			}
		});

		dpiTypes = new JComboBox<String>(new DefaultComboBoxModel<String>(new String[] { "安卓", "显示器", "自定义" }));
		dpiTypes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch (((JComboBox<?>) e.getSource()).getSelectedIndex()) {
				case 0:
					dpiControl.setValue(160);
					dpiControl.setEnabled(false);
					break;
				case 1:
					dpiControl.setValue(Toolkit.getDefaultToolkit().getScreenResolution());
					dpiControl.setEnabled(false);
					break;
				case 2:
					dpiControl.setEnabled(true);
					break;
				default:
					break;
				}
			}
		});
		dpiTypes.setSelectedIndex(1);
		dpiTypes.setMaximumSize(new Dimension(100, 23));
		dpiTypes.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(dpiTypes);
		button_9.setToolTipText("将自定义尺寸换算为android尺寸");
		button_9.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_9);

		toolBar.addSeparator();

		autoSizeButton = new JCheckBox("自动尺寸");
		autoSizeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (reportInfo != null)
					reportInfo.autoSize = autoSizeButton.isSelected();
			}
		});
		autoSizeButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		autoSizeButton.setSelected(true);
		toolBar.add(autoSizeButton);

		changeMode = new JComboBox<String>();
		changeMode.setMaximumSize(new Dimension(100, 23));
		changeMode.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(changeMode);

		androidMode = new JComboBox<String>();
		androidMode.setMaximumSize(new Dimension(100, 23));
		androidMode.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		button_8 = new JButton(" 转换 ");
		button_8.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_8);

		toolBar.addSeparator(new Dimension(5, 0));
		toolBar.addSeparator();

		label_1 = new JLabel(" 编辑器：");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_1);

		controls = new JComboBox<>();
		controls.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		controls.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!inited)
					return;
				if (reportInfo.getSelected() == null) {
					MsgHelper.showMessage(null, "请先选择一个单元格！", "更改单元格类型", JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				if (MsgHelper.showConfirmDialog("如果更换cell类型，当前cell的所有设置将丢失，是否继续？", "更改单元格类型",
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

				List<CellInfo> cellInfos = reportInfo.getSelectCellInfos();
				for (CellInfo cellInfo : cellInfos) {
					UINode.newInstance(((IconComboBoxItem) controls.getSelectedItem()).name, cellInfo.editor);
				}

				isEdit = true;
				canvas.repaint();
				try {
					if (uiBuilder != null)
						DefaultPropertyClient.propertyBuilder(table, reportInfo.getSelectCellInfo().editor,
								uiBuilder.workflowNode, uiBuilder.workflowNodes, uiBuilder.workflowRelationTitle,
								getDataSources());
					else
						DefaultPropertyClient.propertyBuilder(table, reportInfo.getSelectCellInfo().editor, null,
								new HashMap<>(), "", getDataSources());

				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		controls.setRenderer(new IconComboBoxRender<IconComboBoxItem>());
		DefaultComboBoxModel<IconComboBoxItem> model = new DefaultComboBoxModel<>();
		final String[] typenames = new String[] { IDrawInfoDefines.Label_Name, IDrawInfoDefines.TextBox_Name,
				IDrawInfoDefines.TextArea_Name, IDrawInfoDefines.ListBox_Name, IDrawInfoDefines.Image_Name,
				IDrawInfoDefines.ComboBox_Name, IDrawInfoDefines.ComboTreeBox_Name, IDrawInfoDefines.CheckBox_Name,
				IDrawInfoDefines.Chart_Name, IDrawInfoDefines.DateBox_Name, IDrawInfoDefines.TimeBox_Name,
				IDrawInfoDefines.Button_Name, IDrawInfoDefines.Tree_Name, IDrawInfoDefines.Grid_Name,
				IDrawInfoDefines.ScrollBar_Name, IDrawInfoDefines.UpLoad_Name, IDrawInfoDefines.ListView_Name,
				IDrawInfoDefines.ProgressBar_Name, IDrawInfoDefines.Div_Name, IDrawInfoDefines.RadioBox_Name, };
		for (String typename : typenames) {
			ImageIcon icon = new ImageIcon(UINode.getImage(typename));
			model.addElement(new IconComboBoxItem(typename, icon));
		}
		controls.setModel(model);

		d = controls.getPreferredSize();
		controls.setMaximumSize(new Dimension(100, d.height));
		controls.setSelectedIndex(0);
		toolBar.add(controls);

		addButton = new JButton("合并");
		addButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((ReportInfo) editNode.getDrawInfo()).merge();
			}
		});

		button = new JButton("添加");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AtomicInteger count = new AtomicInteger(1);
				Result result = RadioSelector.showDialog(new String[] { "添加行", "添加列" }, count);
				switch (result) {
				case rt1:
					canvas.beginPaint();
					for (int i = 0; i < count.get(); i++) {
						reportInfo.addRow();
					}
					canvas.endPaint();
					break;
				case rt2:
					canvas.beginPaint();
					for (int i = 0; i < count.get(); i++)
						reportInfo.addCol();
					canvas.endPaint();
					break;
				default:
					break;
				}
			}
		});

		editButton = new JButton("编辑");
		editButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		editButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (popupMenu.isVisible()) {
					popupMenu.setVisible(false);
					return;
				}
				Point pt = editButton.getLocation();
				popupMenu.show(editButton.getParent(), pt.x, pt.y + editButton.getHeight());

			}
		});

		toolBar.addSeparator();

		toolBar.add(editButton);

		toolBar.add(button);

		button_1 = new JButton("删除");
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Result result = RadioSelector.showDialog(new String[] { "删除行", "删除列" });
				switch (result) {
				case rt1:
					reportInfo.removeRow();
					break;
				case rt2:
					reportInfo.removeCol();
					break;
				default:
					break;
				}
			}
		});
		toolBar.add(button_1);

		button_6 = new JButton("初始");
		button_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (MsgHelper.showConfirmDialog("将删除所有内容，是否继续？",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					String tmp = MsgHelper.showInputDialog("请输入单元格数量，格式（行数*列数）：",
							String.valueOf(reportInfo.rowCount()) + "*" + String.valueOf(reportInfo.colCount()));
					if (tmp == null || tmp.isEmpty())
						return;

					try {
						String[] tmps = tmp.split("\\*");

						int rows = Integer.parseInt(tmps[0].trim());
						int cols = Integer.parseInt(tmps[1].trim());
						canvas.beginPaint();
						reportInfo.init(rows, cols);
						canvas.endPaint();
					} catch (Exception ee) {
						MsgHelper.showMessage(null, "输入格式错误", "提示", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		toolBar.add(button_6);
		toolBar.addSeparator();

		sizeText = new JLabel("   ");
		toolBar.add(sizeText);
		toolBar.add(addButton);
		delButton = new JButton("拆分");
		delButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		delButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reportInfo.split();
			}
		});
		toolBar.add(delButton);

		pageMode.setSelectedIndex(1);

		toolBar.addSeparator();

		toolBar.addSeparator(new Dimension(5, 0));
		toolBar.addSeparator(new Dimension(5, 0));

		label_4 = new JLabel("数据源：");
		label_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_4);

		dataProviders = new JComboBox<>();
		dataProviders.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		dataProviders.setMaximumSize(new Dimension(100, 23));
		dataProviders.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(dataProviders);

		button_2 = new JButton("添加");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DataSource result = GlobalInstance.instance().getDataSourceSelector().dataSourceSelector(ShowType.stRemote);
				if (result == null)
					return;

				for (int i = 0; i < dataProviders.getItemCount(); i++) {
					if (dataProviders.getItemAt(i).toString().compareTo(result.id) == 0)
						return;
				}

				dataProviders.addItem(result.id);

				reportInfo.addDataSource(result);

				isEdit = true;
			}
		});
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_2);

		button_3 = new JButton("删除");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (dataProviders.getSelectedIndex() == -1)
					return;

				if (MsgHelper.showConfirmDialog("您正在删除数据源，删除后，所有引用此数据源的控件都将无法获取数据，是否继续？",
						JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

				String name = (String) dataProviders.getSelectedItem();
				dataProviders.removeItemAt(dataProviders.getSelectedIndex());

				reportInfo.removeDataSource(name);

				isEdit = true;
			}
		});
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_3);

		toolBar.addSeparator();

		button_4 = new JButton("保存");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveToFile();
			}
		});
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_4);

		button_7 = new JButton("另存为");
		button_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsToFile();
			}
		});
		button_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_7);

		button_5 = new JButton("装载");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadFromFile();
			}
		});
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_5);

		splitPane = new JSplitPane();
		splitPane.setPreferredSize(new Dimension(0, 0));
		contentPane.add(splitPane, BorderLayout.CENTER);

		panel = new JPanel();
		splitPane.setLeftComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));
		canvas.setPreferredSize(new Dimension(0, 0));
		canvas.setMinimumSize(new Dimension(0, 0));

		panel.add(canvas, BorderLayout.CENTER);

		panel_1 = new JPanel();
		splitPane.setRightComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		table = new PropertyPanel();
		table.onClientEvent = new DefaultPropertyClient(new DefaultPropertyClient.IUpdateAdapter() {

			List<CellInfo> selectNodes;

			@Override
			public void setEditState(boolean isEdit) {
				ReportBuilder.this.isEdit = true;
			}

			@Override
			public Component getParent() {
				return ReportBuilder.this;
			}

			public Object showButtonEditJsonEditor(String name, ButtonLabel ob) throws JSONException {
				PropertyInfo info = (PropertyInfo) ob.sender;
				IUINode node = (IUINode) info.getSender();
				Object value = JsonEditorDialog.showJsonEditor(mainControl, ob.getValue(),
						node.getDrawInfo().typeName());
				ob.textField.setText(value == null ? "" : value.toString());
				ob.setValue(value);
				return value;
			}

			@Override
			public void onEdit(int row, int col) {
				selectNodes = reportInfo.getSelectCellInfos();
			}

			@Override
			public void onUpdateEnd(Object obj, String attrName, Object oldValue, Object attrValue) {
				if (attrName.compareTo("dataSource") == 0 && reportInfo.onClick != null)
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							reportInfo.onClick.onClick();
						}
					});

				if (obj instanceof DrawInfo && selectNodes != null) {
					for (CellInfo cellInfo : selectNodes) {
						try {
							UINode uiNode = cellInfo.editor;
							Object valObject = uiNode.getDrawInfo();
							Class<?> c = valObject.getClass();
							Field field = c.getField(attrName);
							if (field == null)
								continue;

							field.setAccessible(true);
							field.set(valObject, attrValue);
							uiNode.invalidRect();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			}

		}, canvas, mainControl, table);
		panel_1.add(table, BorderLayout.CENTER);

		canvas.getPageConfig().setConfig(new Config[] { Config.ccAllowDrag, Config.ccAllowEdit, Config.ccAllowSelect,
				Config.ccAllowResize, Config.ccAllowMulSelect });
		canvas.onPageSizeChanged = new IOnPageSizeChanged() {

			@Override
			public void onChanged(Point max) {
				hScrollBar.setMaximum(max.x);
				vScrollBar.setMaximum(max.y);
				hScrollBar.setValue(0);
				hScrollBar.setValue(0);
			}
		};

		canvas.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				Adjustable adj;
				if (!canvas.isCtrlPressed()) {
					adj = vScrollBar;
				} else
					adj = hScrollBar;

				if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
					int totalScrollAmount = e.getUnitsToScroll() * adj.getUnitIncrement();
					adj.setValue(adj.getValue() + totalScrollAmount);
				}
			}
		});

		canvas.nodeEvent = new INode() {

			@Override
			public void onChange(IDrawNode[] nodes, ChangeType ct) {
				switch (ct) {
				case ctRemove:
				case ctAdd:
				case ctPaste:
				case ctMove:
				case ctAddLink:
				case ctRemoveLink:
				case ctResize:
					isEdit = true;
					break;
				case ctDeselected:
					if (canvas.getSelected() == null)
						try {
							DefaultPropertyClient.propertyBuilder(table, editNode, null, new HashMap<>(), "",
									getDataSources());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					break;
				case ctSelected:
				case ctSelecteds:
				case ctMouseRelease:
					break;
				default:
					break;
				}
			}

			@Override
			public void onAdvanChange(IDrawNode node, ChangeType ct, Object data) {
				switch (ct) {
				case ctAdd:
				case ctReportAddColumn:
				case ctReportAddRow:
				case ctReportApplyTemplate:
				case ctReportChangeCell:
				case ctReportChangeColCount:
				case ctReportChangeColSize:
				case ctReportChangeRowCount:
				case ctReportChangeRowSize:
				case ctReportDeselected:
				case ctReportMerge:
				case ctReportRemoveCell:
				case ctReportRemoveColumn:
				case ctReportRemoveRow:
				case ctReportResetCellSize:
				case ctReportResetScale:
				case ctReportResetSize:
				case ctReportSelected:
				case ctReportSplit:
				case ctResize:
					isEdit = true;
					break;
				default:
					break;
				}
			}

			@Override
			public void DoubleClick(IDrawNode node) {
			}

			@Override
			public void Click(IDrawNode node) {
			}

		};

		canvas.onScroll = new IScroll() {

			@Override
			public void onScroll(int x, int y) {
				hScrollBar.setValue(Math.abs(x));
				vScrollBar.setValue(Math.abs(y));
			}
		};

		hScrollBar = new JScrollBar();
		hScrollBar.setUnitIncrement(10);
		hScrollBar.setMaximum(99999);
		hScrollBar.setOrientation(JScrollBar.HORIZONTAL);
		hScrollBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				canvas.setOffset(new Point(-e.getValue(), canvas.getOffset().y));
			}
		});
		panel.add(hScrollBar, BorderLayout.SOUTH);

		vScrollBar = new JScrollBar();
		vScrollBar.setUnitIncrement(10);
		vScrollBar.setMaximum(99999);
		vScrollBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				canvas.setOffset(new Point(canvas.getOffset().x, -e.getValue()));
			}
		});
		panel.add(vScrollBar, BorderLayout.EAST);

		splitPane.setResizeWeight(0.75);
		splitPane.setDividerLocation(0.75);
		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				splitPane.setDividerLocation(splitPane.getResizeWeight());
				splitPane.setResizeWeight(splitPane.getResizeWeight());
			}
		});

		init(PageSizeMode.psA4V, new Dimension());

		button_8.addActionListener(new ChangeCanvasConfigure(canvas, toolBar, androidMode, changeMode,
				new ChangeCanvasConfigure.IUpdate() {
					@Override
					public void notifyEdit(boolean isEdit) {
						ReportBuilder.this.isEdit = isEdit;
					}

					@Override
					public void changeCanvasToCustomMode() {
						changeCanvasToCustomMode();
					}
				}));

		inited = true;

	}

	protected void init(PageSizeMode pageSize, Dimension size) {
		if (canvas.getWidth() == 0 || canvas.getHeight() == 0) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					init(pageSize, size);
				}
			});
			return;
		}

		int dpi = canvas.getPageConfig().deviceDPI;
		if (dpi == Toolkit.getDefaultToolkit().getScreenResolution()) {
			dpiTypes.setSelectedIndex(1);
		}else if (dpi == IDrawCanvas.ANDROID_DPI) {
			dpiTypes.setSelectedIndex(0);
		}else {
			dpiTypes.setSelectedIndex(2);
		}
		
		dpiControl.setValue(dpi);
		
		canvas.beginPaint();
		
		pageMode.setSelectedItem(DrawCanvas.pageSizeToString(pageSize));
		if (pageSize == PageSizeMode.psCustom) {
			changePage(pageSize, size.width, size.height);
		}

		initReport();

		canvas.endPaint();
	}

	protected void initReport() {
		canvas.clear();
		editNode = canvas.add("报表编辑", IDrawInfoDefines.Report_Name);
		try {
			if (sourceNode != null) {
				editNode.getDrawInfo().fromJson(sourceNode.getDrawInfo().toJson());
				Rectangle r = new Rectangle(sourceNode.getRect());
				r.setLocation(canvas.getClipRect().getLocation());
				r.width = (int) (r.width + IDrawCanvas.LINEWIDTH * 2);
				r.height = (int) (r.height + IDrawCanvas.LINEWIDTH * 2);
				editNode.setRect(r);
			} else {
				Rectangle clip = canvas.getClipRect();
				Rectangle rectangle = new Rectangle(10, 10, editNode.getWidth(), editNode.getHeight());
				rectangle.x += clip.x;
				rectangle.y += clip.y;
				editNode.setRect(rectangle);
			}
			editNode.getDrawInfo().allowEdit = true;

			isEdit = false;
		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		reportInfo = (ReportInfo) editNode.getDrawInfo();

		reportInfo.onClick = new IClick() {

			@Override
			public void onClick() {
				doReportClick();
			}
		};

		dataProviders.setModel(new DefaultComboBoxModel<>());
		for (String id : reportInfo.getDataSources()) {
			dataProviders.addItem(id);
		}

		canvas.getACM().reset();
	}

	protected void close() {
		if (uiBuilder != null) {
			uiBuilder.canvas.repaint();
		}
	}

	protected void save() {
		if (isEdit) {
			if (sourceNode != null) {
				try {
					sourceNode.getCanvas().beginPaint();
					Rectangle rectangle = sourceNode.getRect();
					JSONObject jsondata = editNode.getDrawInfo().toJson();
					sourceNode.getDrawInfo().fromJson(jsondata);
					rectangle.width = editNode.getRect().width;
					rectangle.height = editNode.getRect().height;
					sourceNode.setRect(rectangle);
					sourceNode.getDrawInfo().allowEdit = false;
					ReportInfo info = (ReportInfo) sourceNode.getDrawInfo();
					info.clearSelected();
					sourceNode.invalidRect();
					sourceNode.getCanvas().cancelPaint();
					isEdit = false;
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				saveToFile();
			}
		}

	}

	@Override
	public void onStart(Object param) {
		Object[] params = (Object[]) param;
		uiBuilder = (UIBuilder) params[0];
		sourceNode = (UINode) params[1];
		if (uiBuilder == null || sourceNode == null)
			return;

		PageSizeMode pageSize = uiBuilder.canvas.getPageConfig().getCurPageSizeMode();
		Dimension dimension = uiBuilder.getCustomPageSize();
		init(pageSize, dimension);
	}

	ChildForm parentForm;
	private JComboBox<String> changeMode;
	private JComboBox<String> androidMode;
	private JButton button_8;
	private JLabel label_5;
	private JSpinner dpiControl;
	private JButton button_9;
	private JComboBox<String> dpiTypes;

	@Override
	public void setParentForm(ChildForm form) {
		parentForm = form;
	}

	@Override
	public ChildForm getParentForm() {
		return parentForm;
	}

	@Override
	public Object getResult() {
		return canvas;
	}

	@Override
	public void onSave() {
		save();
	}

	@Override
	public void onLoad() {
		if (isEdit) {
			if (MsgHelper.showConfirmDialog("当前报表已经改变，重新装载会丢失所有修改，是否继续？",
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
				return;

		}
		if (file != null && file.exists()) {
			loadFromFile(file);
		} else {
			initReport();
		}
	}

	@Override
	public void onClose() {
		close();
	}

	@Override
	public void onPublish(HashMap<String, IDrawNode> uikeysWorkflowNodes, Object param) throws Exception {
		throw new Exception("报表不能发布！");
	}

	@Override
	protected boolean allowQuit() {
		if (isEdit)
			switch (MsgHelper.showConfirmDialog("当前报表已经修改，是否保存？", JOptionPane.YES_NO_CANCEL_OPTION)) {
			case JOptionPane.YES_OPTION:
				save();
				break;
			case JOptionPane.CANCEL_OPTION:
				return false;
			default:
				break;
			}
		return true;
	}

}
