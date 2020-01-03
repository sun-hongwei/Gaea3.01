package com.wh.gaea.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.control.ControlSearchHelp;
import com.wh.gaea.control.masterdata.MasterDataTypeFile;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.gaea.selector.KeyValueSelector;
import com.wh.gaea.selector.KeyValueSelector.ModelResult;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.checkboxnode.CheckBoxNode;
import com.wh.swing.tools.checkboxnode.ICheck;
import com.wh.swing.tools.tree.TreeHelp;
import com.wh.swing.tools.tree.TreeHelp.INewNode;
import com.wh.swing.tools.tree.TreeHelp.ITraverseTree;
import com.wh.swing.tools.tree.TreeHelp.TreeItemInfo;
import com.wh.swing.tools.tree.drag.TreeDrag;
import com.wh.swing.tools.tree.drag.TreeDrag.IOnDrag;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.LogicalOperation;
import wh.interfaces.ISqlBuilder.Operation;
import wh.interfaces.ISqlBuilder.SqlType;
import wh.role.obj.GroupInfo;
import wh.role.obj.RoleServiceObject.ITraverse;

public class MasterDataConfigDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();

	IDBConnection db;

	ControlSearchHelp menuKeyHelp;
	ControlSearchHelp navKeyHelp;
	ControlSearchHelp buttonKeyHelp;

	private boolean isEdit = false;

	protected void onClose() {
		if (isEdit) {
			if (MsgHelper.showConfirmDialog("所有未保存的修改都将丢失，是否继续？", "退出",
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return;
			}
		}
		dispose();
	}

	public void saveGroup(DefaultMutableTreeNode roleNode) throws Exception {
		if (roleNode == null)
			return;

		if (!isEdit)
			return;

		TreeItemInfo info = (TreeItemInfo) roleNode.getUserObject();
		String groupid = info.data.getString("id");

		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("*");
		sqlBuilder.addTable("workflow_group");
		sqlBuilder.addWhere("groupid", Operation.otEqual, new Object[] { groupid });
		IDataset groupDataset = db.query(sqlBuilder);
		if (groupDataset.getRowCount() == 0)
			return;

		IRow row = groupDataset.getRow(0);
		row.setValue("groupname", groupname.getText());
		row.setValue("grouptype", grouptype.getSelectedItem());
		row.setValue("groupmemo", groupmemo.getText());

		db.beginTran();
		try {
			groupDataset.post(db);

			db.commitTran();
			info.data.put("text", groupname.getText());
			info.data.put("type", grouptype.getSelectedItem());
			info.data.put("memo", groupmemo.getText());
			roleTree.updateUI();

			isEdit = false;
			
			refreshRole();
		} catch (Exception e) {
			db.rollbackTran();
			MsgHelper.showException(e);
		}
	}

	public void updateParentGroup(DefaultMutableTreeNode roleNode) throws Exception {
		if (roleNode == null)
			return;

		TreeItemInfo info = (TreeItemInfo) roleNode.getUserObject();
		String groupid = info.data.getString("id");

		ISqlBuilder userBuilder = IDBConnection.getSqlBuilder(db);
		if (info.data.has("pid"))
			userBuilder.addSet("grouppid", info.data.getString("pid"));
		else
			userBuilder.addSet("grouppid", "");
		userBuilder.addTable("workflow_group");
		userBuilder.addWhere("groupid", Operation.otEqual, new Object[] { groupid });
		userBuilder.setSqlType(SqlType.stUpdate);

		if (db.execute(userBuilder) <= 0)
			throw new Exception("更新组失败！");
	}

	boolean inited = false;

	protected String getCurrentGroupID() {
		if (roleTree == null)
			return null;

		if (roleTree.getSelectionPath() == null || roleTree.getSelectionPath().getLastPathComponent() == null) {
			if (inited)
				MsgHelper.showMessage(null, "请先选择一个组节点！");
			return null;
		}

		TreeItemInfo info = (TreeItemInfo) ((DefaultMutableTreeNode) roleTree.getSelectionPath().getLastPathComponent())
				.getUserObject();
		try {
			String groupid = info.data.getString("id");
			return groupid;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setGroup(String groupid) throws Exception {

		GroupInfo groupInfo = GlobalInstance.instance().getRoleSelector().getGroupInfo(groupid);
		this.groupid.setText(groupid);
		groupname.setText(groupInfo.groupname);
		this.grouptype.setSelectedItem(groupInfo.grouptype);
		groupmemo.setText(groupInfo.groupmemo);

		isEdit = false;
	}

	private TreeDrag roleTree;
	private JTextField groupid;
	private JTextField groupname;

	public void selectGroupNode(DefaultMutableTreeNode newNode, DefaultMutableTreeNode oldNode) {
		if (newNode == null) {
			resetGroup();
			return;
		}

		if (oldNode != null) {
			try {
				saveGroup(oldNode);
			} catch (Exception e1) {
				e1.printStackTrace();
				MsgHelper.showException(e1);
				return;
			}
		}
		DefaultMutableTreeNode node = newNode;
		TreeItemInfo info = (TreeItemInfo) node.getUserObject();
		if (info == null) {
			return;
		}
		String id;
		try {
			id = info.data.getString("id");
			setGroup(id);
			isEdit = false;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void resetGroup() {
		groupid.setText("");
		groupname.setText("");
		grouptype.setSelectedIndex(-1);
		groupmemo.setText("");

		isEdit = false;
	}

	HashMap<String, JSONObject> groups = new HashMap<>();

	public static void loadGroups(JTree tree, HashMap<String, JSONObject> groups) {
		try {
			JSONArray datas = new JSONArray();
			GlobalInstance.instance().getRoleSelector().traverseGroups(new ITraverse<GroupInfo>() {

				@Override
				public void callback(GroupInfo t) {
					JSONObject value = new JSONObject();
					value.put("id", t.groupid);
					value.put("pid", t.grouppid);
					value.put("text", t.groupname);
					value.put("type", t.grouptype);
					value.put("memo", t.groupmemo);
					datas.put(value);
					groups.put(value.getString("id"), value);
				}
			});

			tree.setModel(new DefaultTreeModel(null));
			TreeHelp.jsonToTree(tree, datas, "id", "text", "pid", new INewNode() {

				@Override
				public DefaultMutableTreeNode newNode() {
					return new CheckBoxNode();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JComboBox<String> grouptype;
	private JTextArea groupmemo;
	IMainControl mainControl;
	protected void addGroup() {
		if (grouptype.getSelectedItem() == null){
			MsgHelper.showMessage("清先选择一个数据类型！");
			return;
		}
		
		ModelResult result = KeyValueSelector.show(null, mainControl, null, null, new Object[][]{
			new Object[]{"编号", ""},
			new Object[]{"名称", ""},
		}, new Object[]{"项目", "值"}, null, new int[]{0}, false);
		
		DefaultTableModel model = null;
		if (result.isok)
			model = result.model;
		
		if (model == null)
			return;
		
		addGroup((String)model.getValueAt(0, 1), (String)model.getValueAt(1, 1));
	}

	protected void refreshRole() throws Exception {
		DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) grouptype.getModel();
		String[] types = new String[model.getSize()];
		for (int i = 0; i < types.length; i++) {
			types[i] = model.getElementAt(i);
		}
		GlobalInstance.instance().getRoleSelector().initGroup(types);
		roleTree.updateUI();
	}
	
	protected void addGroup(String id, String name) {
		if (groups.containsKey(id)) {
			MsgHelper.showMessage("输入的ID已经存在，请重新输入！");
			return;
		}

		if (name == null || name.isEmpty())
			name = id;
		DefaultMutableTreeNode node = TreeHelp.addTreeNode(roleTree, id, "id", "text", "pid", false, new INewNode() {

			@Override
			public DefaultMutableTreeNode newNode() {
				return new CheckBoxNode();
			}
		});

		TreeItemInfo info = (TreeItemInfo) node.getUserObject();

		ISqlBuilder sqlBuilder;
		try {
			sqlBuilder = IDBConnection.getSqlBuilder(db);
			sqlBuilder.addField("*");
			sqlBuilder.addTable("workflow_group");
			sqlBuilder.addWhere("1", Operation.otEqual, new Object[] { "2" });
			IDataset dataset = db.query(sqlBuilder);

			IRow row = dataset.newRow();
			row.setValue("groupid", id);
			row.setValue("groupname", name);
			row.setValue("grouptype", grouptype.getSelectedItem());
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
			String pid = "";
			
			if (parent.getUserObject() != null) {
				pid = ((TreeItemInfo) parent.getUserObject()).data.getString("id");
			}
			row.setValue("grouppid", pid);
			dataset.addRow(row);

			db.beginTran();
			try {
				dataset.post(db);
				db.commitTran();
				
				info.data.put("name", name);
				info.data.put("type", grouptype.getSelectedItem());
			} catch (Exception ex) {
				db.rollbackTran();
				ex.printStackTrace();
				MsgHelper.showException(ex);
			}
			groups.put(name, info.data);

			refreshRole();
		} catch (Exception e1) {
			e1.printStackTrace();
			MsgHelper.showException(e1);
		}
	}
	
	protected void deleteGroup() {
		DefaultMutableTreeNode node = TreeHelp.removeTreeNode(roleTree, new INewNode() {

			@Override
			public DefaultMutableTreeNode newNode() {
				return new CheckBoxNode();
			}
		});

		if (node == null)
			return;

		if (MsgHelper.showConfirmDialog("是否删除选定的项目？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		deleteGroup(node);
	}

	protected void deleteGroup(DefaultMutableTreeNode node) {
		if (node != null) {
			TreeItemInfo info = (TreeItemInfo) node.getUserObject();
			ISqlBuilder sqlBuilder;
			try {
				sqlBuilder = IDBConnection.getSqlBuilder(db);
				sqlBuilder.addTable("workflow_group");
				sqlBuilder.addWhere("groupid", Operation.otEqual, new Object[] { info.data.getString("id") });
				sqlBuilder.setSqlType(SqlType.stDelete);
				if (db.execute(sqlBuilder) <= 0)
					throw new Exception("删除数据库数据失败！");
				if (groups.containsKey(info.data.getString("id")))
					groups.remove(info.data.getString("id"));
				
				refreshRole();
			} catch (Exception e1) {
				e1.printStackTrace();
				MsgHelper.showException(e1);
			}
		}
	}

	public MasterDataConfigDialog(IMainControl mainControl, String[] types) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClose();
			}
		});
		this.mainControl = mainControl;
		this.db = mainControl.getDB();
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle("主数据管理");
		setIconImage(Toolkit.getDefaultToolkit().getImage(MasterDataConfigDialog.class.getResource("/image/browser.png")));
		setBounds(100, 100, 1103, 822);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane_1 = new JSplitPane();
		contentPanel.add(splitPane_1, BorderLayout.CENTER);
		splitPane_1.setResizeWeight(0.7);
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane_1.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				splitPane_1.setDividerLocation(splitPane_1.getResizeWeight());
				splitPane_1.setResizeWeight(splitPane_1.getResizeWeight());
			}
		});

		JScrollPane scrollPane = new JScrollPane();
		splitPane_1.setLeftComponent(scrollPane);

		roleTree = new TreeDrag();
		roleTree.setRootVisible(false);
		roleTree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		roleTree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode newNode = null;
				DefaultMutableTreeNode oldNode = null;
				if (e.getNewLeadSelectionPath() != null && e.getNewLeadSelectionPath().getLastPathComponent() != null)
					newNode = (DefaultMutableTreeNode) e.getNewLeadSelectionPath().getLastPathComponent();

				if (e.getOldLeadSelectionPath() != null && e.getOldLeadSelectionPath().getLastPathComponent() != null)
					oldNode = (DefaultMutableTreeNode) e.getOldLeadSelectionPath().getLastPathComponent();

				selectGroupNode(newNode, oldNode);
			}
		});

		roleTree.addOnDragListener(new IOnDrag() {

			@Override
			public void onDragEnd(DefaultMutableTreeNode newParent, DefaultMutableTreeNode oldParent, int newIndex,
					int oldIndex, DefaultMutableTreeNode node) {
				TreeItemInfo info = (TreeItemInfo) node.getUserObject();
				TreeItemInfo parentInfo = (TreeItemInfo) newParent.getUserObject();
				if (parentInfo == null) {
					info.data.remove("pid");
				} else
					info.data.put("pid", parentInfo.data.getString("id"));

				try {
					updateParentGroup(node);
				} catch (Exception e) {
					e.printStackTrace();
					MsgHelper.showException(e);
				}
			}
		});
		scrollPane.setViewportView(roleTree);

		JScrollPane scrollPane_1 = new JScrollPane();
		splitPane_1.setRightComponent(scrollPane_1);

		groupmemo = new JTextArea();
		scrollPane_1.setViewportView(groupmemo);

		JToolBar toolBar = new JToolBar();
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		contentPanel.add(toolBar, BorderLayout.NORTH);

		JButton btnNewButton = new JButton("添加");
		btnNewButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addGroup();
			}
		});
		toolBar.add(btnNewButton);

		JButton button = new JButton("删除");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteGroup();
			}
		});
		toolBar.add(button);

		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setMaximumSize(new Dimension(20, 32767));
		toolBar.add(separator);

		JLabel lblid = new JLabel("组ID  ");
		lblid.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(lblid);

		groupid = new JTextField();
		groupid.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		groupid.setEditable(false);
		groupid.setMaximumSize(new Dimension(200, 2147483647));
		toolBar.add(groupid);
		groupid.setColumns(10);

		JLabel lblid_1 = new JLabel("  名称  ");
		lblid_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(lblid_1);

		groupname = new JTextField();
		groupname.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEdit = true;
			}
		});
		groupname.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		groupname.setMaximumSize(new Dimension(200, 2147483647));
		groupname.setColumns(10);
		toolBar.add(groupname);

		JLabel label_4 = new JLabel(" ");
		toolBar.add(label_4);

		JButton button_6 = new JButton("保存");
		button_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (roleTree.getSelectionPath() != null)
					try {
						saveGroup((DefaultMutableTreeNode) roleTree.getSelectionPath().getLastPathComponent());
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		});

		JLabel label = new JLabel("数据类型 ");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label);

		grouptype = new JComboBox<String>();
		grouptype.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEdit = true;
			}
		});
		grouptype.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(grouptype);
		toolBar.add(button_6);

		toolBar.addSeparator();
		JButton button_11 = new JButton("全选");
		final AtomicReference<Component> lastFocusTree = new AtomicReference<>(null);
		button_11.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {

			}

			@Override
			public void focusGained(FocusEvent e) {
				if (e.getOppositeComponent() instanceof JTree || e.getOppositeComponent() instanceof JList) {
					lastFocusTree.set(e.getOppositeComponent());

				}
			}
		});
		button_11.addActionListener(new ActionListener() {
			HashMap<Component, Boolean> selectStates = new HashMap<>();

			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e) {
				if (lastFocusTree.get() == null)
					return;

				AtomicBoolean selected = new AtomicBoolean(false);
				if (selectStates.containsKey(lastFocusTree.get())) {
					selected.set(selectStates.get(lastFocusTree.get()));
				}

				selected.set(!selected.get());
				selectStates.put(lastFocusTree.get(), selected.get());
				if (lastFocusTree.get() instanceof JTree) {
					JTree tree = (JTree) lastFocusTree.get();
					DefaultMutableTreeNode selectNode = tree.getSelectionPath() == null ? null
							: (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
					TreeHelp.traverseTree(tree, new ITraverseTree<CheckBoxNode>() {

						@Override
						public boolean onNode(CheckBoxNode t) {
							if (selectNode != null) {
								if (t == selectNode || t.getParent() == selectNode) {
									t.setSingleSelected(selected.get());
								}
							} else
								t.setSingleSelected(selected.get());
							return true;
						}
					});

					tree.updateUI();
				} else if (lastFocusTree.get() instanceof JList) {
					JList<ICheck> list = (JList<ICheck>) lastFocusTree.get();
					ListModel<ICheck> model = list.getModel();
					for (int i = 0; i < model.getSize(); i++) {
						model.getElementAt(i).setChecked(selected.get());
					}
					list.updateUI();
				}
			}
		});
		button_11.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_11);

		toolBar.addSeparator();

		JButton button_12 = new JButton("折叠");
		final AtomicReference<JTree> expandTree = new AtomicReference<JTree>(null);

		button_12.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (e.getOppositeComponent() instanceof JTree)
					expandTree.set((JTree) e.getOppositeComponent());
			}
		});

		button_12.addActionListener(new ActionListener() {
			HashMap<JTree, Boolean> state = new HashMap<>();

			public void actionPerformed(ActionEvent e) {
				if (expandTree.get() == null)
					return;

				JTree tree = expandTree.get();
				boolean expand = true;
				if (state.containsKey(tree)) {
					expand = state.get(tree);
				}
				expand = !expand;
				state.put(tree, expand);

				TreeHelp.expandOrCollapse(tree, (DefaultMutableTreeNode) null, expand);
				// TreeHelp.expandOrCollapse(tree, tree.getSelectionPath() ==
				// null ? null : tree.getSelectionPath().getParentPath(),
				// expand);

			}
		});

		button_12.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_12);

		toolBar.addSeparator();

		groupname.getDocument().addDocumentListener(new DocumentListener() {

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

		setLocationRelativeTo(null);

		try {
			DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(types);
			grouptype.setModel(model);
			if (model.getSize() > 0)
				grouptype.setSelectedIndex(0);
			
			refreshRole();
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		loadGroups(roleTree, groups);
		inited = true;
	}

	public void removeUser(String userid, String groupid) throws Exception {
		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addTable("workflow_group_user");
		sqlBuilder.addWhere("userid", Operation.otEqual, new Object[] { userid });
		sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
		sqlBuilder.addWhere("groupid", Operation.otEqual, new Object[] { groupid });
		sqlBuilder.setSqlType(SqlType.stDelete);
		db.execute(sqlBuilder);

	}

	public static void showDialog(IMainControl mainControl) {
		IDBConnection db = mainControl.getDB();
		if (db == null || !db.isOpen()){
			MsgHelper.showMessage("请先连接数据库！");
			return;
		}

		JSONArray types;
		try {
			types = MasterDataTypeFile.getTypes();
			if (types.length() == 0){
				MsgHelper.showMessage("请先维护主数据的类别信息后再试！");
				return;
			}
			
			TreeMap<Object, Object> sorts = new TreeMap<>();
			for (Object name : types) {
				sorts.put(name, name);
			}
			MasterDataConfigDialog dialog = new MasterDataConfigDialog(mainControl, sorts.values().toArray(new String[sorts.size()]));
			dialog.setModal(true);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}
}
