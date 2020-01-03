package com.wh.gaea.plugin.role;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.StringReader;
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
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;

import com.wh.gaea.control.modelsearch.ModelSearchView;
import com.wh.swing.tools.MsgHelper;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.role.obj.CustomDataRoleInfo;
import wh.role.obj.CustomDataRoleInfo.UseType;

public class CustomRoleDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();

	IDBConnection db;

	private boolean isEdit = false;

	CustomDataRoleManager cdrm;

	protected void onClose() {
		if (isEdit) {
			if (MsgHelper.showConfirmDialog("所有未保存的修改都将丢失，是否继续？", "退出",
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return;
			}
		}
		dispose();
	}

	public void save() {
		CustomDataRoleInfo info = roleList.getSelectedValue();
		if (info == null)
			return;

		try {
			save(info);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}

	public void save(CustomDataRoleInfo info) throws Exception {
		if (info == null)
			return;

		if (!isEdit)
			return;

		info.field = fieldView.getText();
		info.tablename = (String) tablenameView.getSelectedItem();
		info.field = fieldView.getText();
		info.useType = typeView.getSelectedItem().equals("SQL方式") ? UseType.utSQL : UseType.utList;
		info.sqlInfo.sql = sqlView.getText();
		info.listInfo.items.clear();

		try (BufferedReader br = new BufferedReader(new StringReader(rolelistView.getText()));) {
			String line = null;
			do {
				line = br.readLine();
				if (line == null || line.isEmpty())
					break;
				info.listInfo.items.put(line, line);
			} while (true);

		} catch (Exception e) {
			MsgHelper.showException(e);
		}

		try {
			cdrm.save(info);
			isEdit = false;
		} catch (Exception e) {
			MsgHelper.showException(e);
		}
	}

	public void initRoleList() {
		DefaultListModel<CustomDataRoleInfo> model = new DefaultListModel<CustomDataRoleInfo>();

		for (CustomDataRoleInfo info : cdrm.getRoles()) {
			model.addElement(info);
		}

		roleList.setModel(model);

		roleList.updateUI();

		isEdit = false;
	}

	boolean inited = false;

	private JTextField nameView;

	public void selectRole() {
		if (roleList.getSelectedValue() == null)
			return;

		selectRole(roleList.getSelectedValue());
	}

	public void selectRole(CustomDataRoleInfo info) {
		if (info == null) {
			return;
		}

		nameView.setText(info.name);
		tablenameView.setSelectedItem(info.tablename);
		fieldView.setText(info.field);
		typeView.setSelectedItem(info.useType == UseType.utSQL ? "SQL方式" : "列表方式");
		sqlView.setText(info.sqlInfo.sql);
		rolelistView.setText("");
		for (String item : info.listInfo.items.keySet()) {
			rolelistView.append(item + "\r\n");
		}

		isEdit = false;
	}

	/**
	 * Create the dialog.
	 */

	private JList<CustomDataRoleInfo> roleList;
	private JComboBox<String> typeView;
	private JTable roleTable;
	private JTextField fieldView;
	private JTextArea sqlView;
	private JComboBox<String> tablenameView;
	private JTextArea rolelistView;

	protected void addItem() {
		if (isEdit) {
			if (MsgHelper.showConfirmDialog("当前项目已经修改，是否保存？", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				save();
			}
		}

		String name = MsgHelper.showInputDialog("请输入项目名称：");
		if (name == null || name.isEmpty())
			return;

		DefaultListModel<CustomDataRoleInfo> model = (DefaultListModel<CustomDataRoleInfo>) roleList.getModel();
		for (int i = 0; i < model.getSize(); i++) {
			if (model.getElementAt(i).name.equalsIgnoreCase(name)) {
				MsgHelper.showMessage("名称为【" + name + "】的项目已经存在！");
				return;
			}
		}
		model.addElement(new CustomDataRoleInfo(name));

		roleList.setSelectedIndex(model.getSize() - 1);
		isEdit = false;
	}

	protected void deleteItem() {
		if (roleList.getSelectedValue() == null)
			return;

		if (MsgHelper.showConfirmDialog("是否删除选定的项目？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		deleteItem(roleList.getSelectedValue());
	}

	protected void deleteItem(CustomDataRoleInfo info) {
		if (info != null) {
			try {
				cdrm.remove(info);
				DefaultListModel<CustomDataRoleInfo> model = (DefaultListModel<CustomDataRoleInfo>) roleList.getModel();
				model.removeElement(info);
				roleList.updateUI();
				isEdit = false;
			} catch (Exception e1) {
				e1.printStackTrace();
				MsgHelper.showException(e1);
			}
		}
	}

	protected void addSplitResizeEvent(JSplitPane splitPane) {
		splitPane.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				splitPane.setResizeWeight(splitPane.getResizeWeight());
				splitPane.setDividerLocation(splitPane.getResizeWeight());
			}
		});

	}

	public void initTableNames() {
		List<String> ts = db.getTables();
		tablenameView.setModel(new DefaultComboBoxModel<>(ts.toArray(new String[ts.size()])));
		if (tablenameView.getItemCount() > 0) {
			tablenameView.setSelectedIndex(0);
		}
		tablenameView.updateUI();
	}

	public void addDocumentListener(Document document) {
		document.addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				isEdit = true;
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				isEdit = true;
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				isEdit = true;
			}
		});
	}

	public CustomRoleDialog(IDBConnection db) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClose();
			}
		});
		this.db = db;

		cdrm = CustomDataRoleManager.getManager(db);

		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle("自定义数据权限配置");
		setIconImage(Toolkit.getDefaultToolkit().getImage(RoleConfigDialog.class.getResource("/image/browser.png")));
		setBounds(100, 100, 1046, 718);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.3);
		addSplitResizeEvent(splitPane);
		contentPanel.add(splitPane, BorderLayout.CENTER);

		JToolBar toolBar = new JToolBar();
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		contentPanel.add(toolBar, BorderLayout.NORTH);

		JButton btnNewButton = new JButton("添加");
		btnNewButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addItem();
			}
		});
		toolBar.add(btnNewButton);

		JButton button = new JButton("删除");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteItem();
			}
		});
		toolBar.add(button);

		toolBar.addSeparator();

		JLabel lblid = new JLabel(" 项目名称 ");
		lblid.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(lblid);

		nameView = new JTextField();
		nameView.setEditable(false);
		nameView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEdit = true;
			}
		});
		nameView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		nameView.setMaximumSize(new Dimension(200, 2147483647));
		toolBar.add(nameView);
		nameView.setColumns(10);

		JLabel label_2 = new JLabel(" 应用表 ");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_2);

		tablenameView = new JComboBox<String>();
		tablenameView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEdit = true;
			}
		});
		tablenameView.setMaximumSize(new Dimension(150, 32767));
		tablenameView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(tablenameView);

		JLabel label_1 = new JLabel(" 字段名称 ");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label_1);

		fieldView = new JTextField();
		addDocumentListener(fieldView.getDocument());
		fieldView.setMaximumSize(new Dimension(200, 2147483647));
		fieldView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		fieldView.setColumns(10);
		toolBar.add(fieldView);

		JLabel label = new JLabel(" 权限来源 ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label);

		typeView = new JComboBox<>();
		typeView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEdit = true;
			}
		});
		typeView.setModel(new DefaultComboBoxModel<>(new String[] { "SQL方式", "列表方式" }));
		typeView.setSelectedIndex(1);
		typeView.setMaximumSize(new Dimension(100, 32767));
		typeView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(typeView);

		JLabel label_4 = new JLabel(" ");
		toolBar.add(label_4);

		JButton button_6 = new JButton("保存");
		button_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				save();
			}
		});
		toolBar.add(button_6);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		splitPane.setRightComponent(tabbedPane);
		tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("SQL方式", null, panel_1, null);
		panel_1.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.3);
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		addSplitResizeEvent(splitPane_1);
		panel_1.add(splitPane_1, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		splitPane_1.setLeftComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_1 = new JScrollPane();
		panel.add(scrollPane_1, BorderLayout.CENTER);

		sqlView = new JTextArea();
		addDocumentListener(sqlView.getDocument());
		scrollPane_1.setViewportView(sqlView);

		JToolBar toolBar_1 = new JToolBar();
		toolBar_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.setFloatable(false);
		panel.add(toolBar_1, BorderLayout.NORTH);

		JButton button_1 = new JButton(" 设置 ");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IDataset dataset;
				try {
					dataset = db.query(sqlView.getText(), null);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
					return;
				}
				if (dataset == null)
					return;

				if (dataset.getColumnCount() > 1) {
					MsgHelper.showMessage("权限查询语句应仅返回一个字段列表！");
					return;
				}

				DefaultTableModel model = new DefaultTableModel(new Object[] { dataset.getColumn(0).getName() }, 0);
				for (IRow row : dataset.getRows()) {
					model.addRow(new Object[] { row.getValue(0) });
				}

				roleTable.setModel(model);
				roleTable.updateUI();
			}
		});
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(button_1);

		JButton button_2 = new JButton(" 执行 ");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IDataset dataset;
				try {
					dataset = db.query(sqlView.getText(), null);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
					return;
				}
				if (dataset == null)
					return;

				Object[] names = new Object[dataset.getColumnCount()];
				for (int i = 0; i < dataset.getColumnCount(); i++) {
					names[i] = dataset.getColumn(i).getLabel();
				}

				DefaultTableModel model = new DefaultTableModel(names, 0);
				for (IRow row : dataset.getRows()) {
					Object[] rowData = new Object[dataset.getColumnCount()];
					for (int i = 0; i < dataset.getColumnCount(); i++) {
						rowData[i] = row.getValue(i);
					}
					model.addRow(rowData);
				}

				roleTable.setModel(model);
				roleTable.updateUI();
			}
		});
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar_1.add(button_2);

		JPanel panel_2 = new JPanel();
		splitPane_1.setRightComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_2 = new JScrollPane();
		panel_2.add(scrollPane_2, BorderLayout.CENTER);

		roleTable = new ModelSearchView.TableModelSearchView();
		roleTable.setCellSelectionEnabled(true);
		roleTable.setFillsViewportHeight(true);
		roleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		roleTable.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_2.setViewportView(roleTable);

		JPanel panel_3 = new JPanel();
		tabbedPane.addTab("列表方式", null, panel_3, null);
		panel_3.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_5 = new JScrollPane();
		panel_3.add(scrollPane_5, BorderLayout.CENTER);

		rolelistView = new JTextArea();
		addDocumentListener(rolelistView.getDocument());
		scrollPane_5.setViewportView(rolelistView);

		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);

		roleList = new ModelSearchView.ListModelSearchView<>();
		roleList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					return;
				}

				if (isEdit) {
					if (MsgHelper.showConfirmDialog("当前项目已经修改，是否保存？",
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
						DefaultListModel<CustomDataRoleInfo> model = (DefaultListModel<CustomDataRoleInfo>) roleList
								.getModel();
						try {
							if (roleList.getSelectedIndex() == e.getFirstIndex())
								save(model.getElementAt(e.getLastIndex()));
							else
								save(model.getElementAt(e.getFirstIndex()));
						} catch (Exception e1) {
							e1.printStackTrace();
							MsgHelper.showException(e1);
						}
					}
				}
				selectRole();
			}
		});

		scrollPane.setViewportView(roleList);

		initTableNames();
		initRoleList();

		setLocationRelativeTo(null);

		inited = true;
	}

	public static void showDialog(IDBConnection db) {
		if (db == null || !db.isOpen()) {
			MsgHelper.showMessage("请先连接数据库！");
			return;
		}

		CustomRoleDialog dialog = new CustomRoleDialog(db);
		dialog.setModal(true);
		dialog.setVisible(true);
	}
}
