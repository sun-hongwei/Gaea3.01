package com.wh.gaea.plugin.role;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.control.CheckBoxList;
import com.wh.gaea.control.modelsearch.ModelSearchView;
import com.wh.gaea.editor.JsonTreeDataEditor;
import com.wh.gaea.editor.ListItemData;
import com.wh.gaea.plugin.role.RoleInitDialog.RoleType;
import com.wh.parallel.computing.ParallelComputingExecutor;
import com.wh.parallel.computing.interfaces.ISimpleActionComputer;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.SwingTools;
import com.wh.swing.tools.checkboxnode.CheckBoxListRenderer;
import com.wh.swing.tools.checkboxnode.CheckBoxNode;
import com.wh.swing.tools.checkboxnode.CheckBoxNode.ISelection;
import com.wh.swing.tools.checkboxnode.CheckBoxNodeConfig;
import com.wh.swing.tools.checkboxnode.ICheck;
import com.wh.swing.tools.tree.TreeHelp;
import com.wh.swing.tools.tree.TreeHelp.INewNode;
import com.wh.swing.tools.tree.TreeHelp.ITraverseTree;
import com.wh.swing.tools.tree.TreeHelp.TreeItemInfo;
import com.wh.swing.tools.tree.drag.TreeDrag;
import com.wh.swing.tools.tree.drag.TreeDrag.IOnDrag;

import wh.interfaces.IConnectionFactory;
import wh.interfaces.IDBConnection;
import wh.interfaces.IDBConnection.DBConnectionInfo;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.LogicalOperation;
import wh.interfaces.ISqlBuilder.Operation;
import wh.interfaces.ISqlBuilder.SqlType;
import wh.role.interfaces.IInfos;
import wh.role.obj.CustomDataRoleInfo;
import wh.role.obj.DataRoleInfo;
import wh.role.obj.FunRoleInfo;
import wh.role.obj.GroupCustomDataRoleInfo;
import wh.role.obj.GroupCustomDataRoleInfo.RoleInfo;
import wh.role.obj.GroupInfo;
import wh.role.obj.RoleServiceObject.DataOperType;
import wh.role.obj.RoleServiceObject.FunRoleType;
import wh.role.obj.RoleServiceObject.ITraverse;
import wh.role.obj.UserInfo;

public class RoleConfigDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();
	private JLabel label_1;
	private JTree menutree;
	private JTree dataroletree;
	private JList<ICheck> uilist;
	private JTree buttontree;

	DBConnectionInfo dbConnectionInfo;
	ThreadLocal<IDBConnection> db = new ThreadLocal<IDBConnection>() {
		@Override
	    protected IDBConnection initialValue() {
	        try {
				return IConnectionFactory.getConnection(dbConnectionInfo);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
	    }
	};

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

	public void init() throws InvocationTargetException, InterruptedException, ExecutionException {

		ParallelComputingExecutor<String> executor = new ParallelComputingExecutor<String>(
				Arrays.asList(new String[] { "initCustomDataRoleList", "rtButton", "rtTree", "rtView", "rtMenu", "rtData"}), 2);
		executor.execute(new ISimpleActionComputer<String>() {
			
			@Override
			public void compute(String t1) throws Exception {
				switch (t1) {
				case "initCustomDataRoleList":
					initCustomDataRoleList();
					break;
				case "rtButton":
					init(RoleType.rtButton);
					break;
				case "rtTree":
					init(RoleType.rtTree);
					break;
				case "rtView":
					init(RoleType.rtView);
					break;
				case "rtMenu":
					init(RoleType.rtMenu);
					break;
				case "rtData":
					init(RoleType.rtData);
					break;
				}
			}
		});
	}

	protected void setList(JList<ICheck> list) {
		list.setModel(new DefaultListModel<ICheck>());
		list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setCellRenderer(new CheckBoxListRenderer(list, new CheckBoxListRenderer.ISelection() {
			@Override
			public void onSelected(ICheck obj) {
				isEdit = true;
			}
		}));
	}

	public void saveTree(IDataset dataset, JTree tree, String groupid, String typename) {
		TreeHelp.traverseTree(tree, new ITraverseTree<CheckBoxNode>() {

			@Override
			public boolean onNode(CheckBoxNode t) {
				boolean needSave = t.isSelected();
				if (typename.compareToIgnoreCase("button") == 0)
					needSave = t.isSelected();

				if (needSave) {
					TreeItemInfo tmp = (TreeItemInfo) t.getUserObject();
					IRow row = dataset.newRow();
					try {
						String roleid = tmp.data.getString("id");
						row.setValue("groupid", groupid);
						row.setValue("roleid", roleid);
						row.setValue("roletype", typename);
						dataset.addRow(row);
					} catch (Exception e) {
						e.printStackTrace();
						MsgHelper.showException(e);
						return false;
					}
				}
				return true;
			}
		});

	}

	public void saveList(IDataset dataset, JList<ICheck> list, String groupid, String typename) {
		DefaultListModel<ICheck> model = (DefaultListModel<ICheck>) list.getModel();

		for (int i = 0; i < model.size(); i++) {
			ListItemData item = (ListItemData) model.getElementAt(i);
			if (item.getChecked()) {
				String roleid = item.getID();
				IRow row = dataset.newRow();
				try {
					row.setValue("groupid", groupid);
					row.setValue("roleid", roleid);
					row.setValue("roletype", typename);
					dataset.addRow(row);
				} catch (Exception e) {
					e.printStackTrace();
					MsgHelper.showException(e);
					break;
				}
			}
		}
	}

	protected String getDataRoleType() {
		if (selfdata.isSelected())
			return "self";
		else if (groupdata.isSelected())
			return "group";
		else
			return "groups";
	}

	protected void setDataRoleType(String roletype) {
		if (roletype == null) {
			return;
		}
		switch (roletype) {
		case "self":
			selfdata.setSelected(true);
			break;
		case "group":
			groupdata.setSelected(true);
			break;
		default:
			groupsdata.setSelected(true);
			break;
		}
	}

	protected String getOperTypeString() {
		switch (dataroletype.getSelectedIndex()) {
		case 0:
			return "query";
		case 1:
			return "update";
		default:
			return "";
		}
	}

	protected DataOperType getOperType() {
		switch (dataroletype.getSelectedIndex()) {
		case 0:
			return DataOperType.dtQuery;
		case 1:
		default:
			return DataOperType.dtUpdate;
		}
	}

	protected void setOperType(String opertype) {
		switch (opertype) {
		case "query":
			dataroletype.setSelectedIndex(0);
			break;
		case "update":
			dataroletype.setSelectedIndex(1);
			break;
		default:
			break;
		}
	}

	public boolean saveDataRole(JTree tree, String groupid) throws Exception {
		IDBConnection db = this.db.get();
		ISqlBuilder delBuilder = IDBConnection.getSqlBuilder(db);
		delBuilder.addTable("workflow_role_data_group");
		delBuilder.setRawWhere(new StringBuilder("id in (select id from workflow_role_data where groupid = '" + groupid
				+ "' and opertype = '" + getOperTypeString() + "')"));
		delBuilder.setSqlType(SqlType.stDelete);
		db.execute(delBuilder);

		delBuilder = IDBConnection.getSqlBuilder(db);
		delBuilder.addTable("workflow_role_data");
		delBuilder.addWhere("groupid", Operation.otEqual, new Object[] { groupid });
		delBuilder.addLogicalOperation(LogicalOperation.otAnd);
		delBuilder.addWhere("opertype", Operation.otEqual, new Object[] { getOperTypeString() });
		delBuilder.setSqlType(SqlType.stDelete);
		db.execute(delBuilder);

		ISqlBuilder sqlBuilder;
		sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("id, groupid, opertype, roletype");
		sqlBuilder.addTable("workflow_role_data");
		sqlBuilder.addWhere("1", Operation.otEqual, new Object[] { 2 });
		IDataset dataset = db.query(sqlBuilder);

		ISqlBuilder detailBuilder;
		detailBuilder = IDBConnection.getSqlBuilder(db);
		detailBuilder.addField("groupid, id");
		detailBuilder.addTable("workflow_role_data_group");
		detailBuilder.addWhere("1", Operation.otEqual, new Object[] { 2 });
		IDataset detailds = db.query(detailBuilder);

		String id = UUID.randomUUID().toString();
		String opertype = getOperTypeString();
		String roletype = getDataRoleType();

		IRow row = dataset.newRow();
		row.setValue("id", id);
		row.setValue("groupid", groupid);
		row.setValue("opertype", opertype);
		row.setValue("roletype", roletype);
		dataset.addRow(row);

		if (roletype.compareToIgnoreCase("groups") == 0) {

			if (!TreeHelp.traverseTree(tree, new ITraverseTree<CheckBoxNode>() {
				@Override
				public boolean onNode(CheckBoxNode t) {
					if (t.isSelected()) {
						TreeItemInfo tmp = (TreeItemInfo) t.getUserObject();
						IRow row = detailds.newRow();
						try {
							String datagroupid = tmp.data.getString("id");
							row.setValue("groupid", datagroupid);
							row.setValue("id", id);
							detailds.addRow(row);
						} catch (Exception e) {
							e.printStackTrace();
							MsgHelper.showException(e);
							return false;
						}
					}
					return true;
				}
			}))
				return false;
		}

		dataset.post(db);
		detailds.post(db);

		return true;

	}

	public void saveRole(DefaultMutableTreeNode roleNode) throws Exception {
		if (roleNode == null)
			return;

		if (!isEdit)
			return;

		IDBConnection db = this.db.get();

		TreeItemInfo info = (TreeItemInfo) roleNode.getUserObject();
		String groupid = info.data.getString("id");

		ISqlBuilder userBuilder = IDBConnection.getSqlBuilder(db);
		userBuilder.addField("*");
		userBuilder.addTable("workflow_group");
		userBuilder.addWhere("groupid", Operation.otEqual, new Object[] { groupid });
		IDataset groupDataset = db.query(userBuilder);
		if (groupDataset.getRowCount() == 0)
			return;

		IRow row = groupDataset.getRow(0);
		row.setValue("groupname", groupname.getText());

		ISqlBuilder sqlBuilder;
		sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("groupid, roleid, roletype");
		sqlBuilder.addTable("workflow_group_role");
		sqlBuilder.addWhere("1", Operation.otEqual, new Object[] { 2 });
		IDataset dataset = db.query(sqlBuilder);

		saveTree(dataset, menutree, groupid, "menu");
		saveTree(dataset, buttontree, groupid, "button");
		saveTree(dataset, navTree, groupid, "tree");
		saveList(dataset, uilist, groupid, "view");

		db.beginTran();
		try {
			ISqlBuilder delBuilder = IDBConnection.getSqlBuilder(db);
			delBuilder.addTable(sqlBuilder.getTableName());
			delBuilder.addWhere("groupid", Operation.otEqual, new Object[] { groupid });
			delBuilder.setSqlType(SqlType.stDelete);
			db.execute(delBuilder);

			dataset.post(db);
			groupDataset.post(db);

			db.commitTran();
			info.data.put("text", groupname.getText());
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					roleTree.updateUI();
				}
			});

			Roler.instance().initGroup(groupid);
			isEdit = false;
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

		IDBConnection db = this.db.get();

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

	protected void setUserDataRoleGroups(String id) throws Exception {
		resetDataRoleTree();

		TreeHelp.traverseTree(dataroletree, new ITraverseTree<CheckBoxNode>() {

			@Override
			public boolean onNode(CheckBoxNode t) {
				TreeItemInfo info = (TreeItemInfo) t.getUserObject();
				try {
					t.setSingleSelected(Roler.instance().getDataRoles().check(id, info.data.getString("id")));
				} catch (JSONException e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}
		});
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

	protected void setGroupDataRole(String groupid) throws Exception {
		String typename = getOperTypeString();
		if (typename.isEmpty())
			typename = "query";

		setOperType(getOperTypeString());
	}

	protected void setUserDataRoleType(String groupid, String opertype) throws Exception {
		resetDataRole();

		Map<String, DataRoleInfo> infos = Roler.instance().getDataRoles().getRole(groupid);
		if (infos == null || infos.size() == 0)
			return;

		DataRoleInfo info = infos.get(opertype);
		if (info == null)
			return;

		setDataRoleType(info.roletype);
		if (info.roletype.compareToIgnoreCase("groups") == 0)
			setUserDataRoleGroups(info.id);
	}

	public void setGroupRole(String groupid, String name) throws Exception {

		resetRoles();

		this.groupid.setText(groupid);
		groupname.setText(name);

		Map<FunRoleType, Map<String, FunRoleInfo>> map = Roler.instance().getFunRoles().getSimpleRoles(groupid);
		for (FunRoleType rt : map.keySet()) {
			for (FunRoleInfo info : map.get(rt).values()) {
				String id = info.roleid;
				switch (rt) {
				case ftMenu:
					if (menuRoles.containsKey(id)) {
						CheckBoxNode node = menuRoles.get(id);
						node.setSingleSelected(true);
					}
					break;
				case ftTree:
					if (navRoles.containsKey(id)) {
						CheckBoxNode node = navRoles.get(id);
						node.setSingleSelected(true);
					}
					break;
				case ftUI:
					if (viewRoles.containsKey(id)) {
						ListItemData node = viewRoles.get(id);
						node.setChecked(true);
					}
					break;
				case ftButton:
					if (buttonRoles.containsKey(id)) {
						CheckBoxNode node = buttonRoles.get(id);
						node.setSingleSelected(true);
					}
					break;
				default:
					break;
				}
			}
		}

		GroupInfo groupInfo = Roler.instance().getGroups().getGroup(groupid);
		if (groupInfo == null)
			return;

		List<String> removedUsers = new ArrayList<>();
		List<UserInfo> users = new ArrayList<>();
		for (String userid : groupInfo.simpleUsers.keySet()) {
			UserInfo userInfo = Roler.instance().getUsers().getUser(userid);
			if (userInfo == null) {
				removedUsers.add(userid);
				continue;
			}
			users.add(userInfo);
		}

		userList.setModel(new DefaultListModel<>());
		DefaultListModel<UserInfo> model = (DefaultListModel<UserInfo>) userList.getModel();
		for (UserInfo userInfo : users) {
			model.addElement(userInfo);
		}

		setGroupDataRole(groupid);
		selectCustomDataRole(groupid);

		menutree.updateUI();
		navTree.updateUI();
		buttontree.updateUI();
		uilist.updateUI();
		dataroletree.updateUI();

		if (removedUsers.size() > 0) {

			MsgHelper.showMessage("当前组的用户" + Arrays.toString(removedUsers.toArray(new String[removedUsers.size()]))
					+ "已经被删除，请处理后再试！");
		}
		isEdit = false;
	}

	HashMap<String, CheckBoxNode> navRoles = new HashMap<>();
	HashMap<String, CheckBoxNode> menuRoles = new HashMap<>();
	HashMap<String, CheckBoxNode> buttonRoles = new HashMap<>();
	HashMap<String, ListItemData> viewRoles = new HashMap<>();
	private TreeDrag roleTree;
	private JTextField groupid;
	private JTextField groupname;

	public void resetDataRoleTree() {
		TreeHelp.traverseTree(dataroletree, new ITraverseTree<CheckBoxNode>() {

			@Override
			public boolean onNode(CheckBoxNode t) {
				t.setSelected(false);
				return true;
			}
		});

		dataroletree.updateUI();
	}

	public void resetTree(JTree tree) {
		TreeHelp.traverseTree(tree, new ITraverseTree<CheckBoxNode>() {

			@Override
			public boolean onNode(CheckBoxNode t) {
				t.setSelected(false);
				return true;
			}
		});

		tree.updateUI();
	}

	public void selectRoleNode(DefaultMutableTreeNode newNode, DefaultMutableTreeNode oldNode) {
		if (newNode == null) {
			resetRoles();
			return;
		}

		if (oldNode != null) {
			try {
				if (isEdit) {
					if (MsgHelper.showConfirmDialog("权限已经修改，是否保存？",
							JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
						saveRole(oldNode);
				}
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
			setGroupRole(id, info.toString());
			isEdit = false;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void resetDataRole() {
		buttonGroup.clearSelection();
		resetDataRoleTree();

	}

	public void resetRoles() {
		groupid.setText("");
		groupname.setText("");

		for (CheckBoxNode node : menuRoles.values()) {
			node.setSelected(false);
		}

		for (CheckBoxNode node : navRoles.values()) {
			node.setSelected(false);
		}

		for (CheckBoxNode node : buttonRoles.values()) {
			node.setSelected(false);
		}

		for (ListItemData node : viewRoles.values()) {
			node.setChecked(false);
		}

		resetTree(menutree);
		resetTree(navTree);
		resetTree(buttontree);
		uilist.updateUI();
		resetDataRole();

		customRoles.setModel(new DefaultListModel<>());
		customRoleItems.setModel(new DefaultListModel<>());

		isEdit = false;
	}

	HashMap<String, JSONObject> groups = new HashMap<>();
	private JRadioButton groupsdata;
	private JRadioButton groupdata;
	private JRadioButton selfdata;
	private JComboBox<String> dataroletype;

	@SuppressWarnings("unchecked")
	public static void initRoleList(JTree tree, Roler roler, HashMap<String, JSONObject> groups) {
		try {
			JSONArray datas = new JSONArray();
			((IInfos<GroupInfo>) Roler.instance().getGroups()).traverse(new ITraverse<GroupInfo>() {

				@Override
				public void callback(GroupInfo t) {
					JSONObject value = new JSONObject();
					value.put("id", t.groupid);
					value.put("pid", t.grouppid);
					value.put("text", t.groupname);
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

	protected void setMenuOrTree(JSONArray data, JTree tree, HashMap<String, CheckBoxNode> roles) {
		if (data == null) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
					}
				});
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return;
		}

		TreeHelp.jsonToTree(tree, data, "id", "text", "pid", new INewNode() {

			@Override
			public DefaultMutableTreeNode newNode() {
				return new CheckBoxNode();
			}
		});
		roles.clear();
		TreeHelp.traverseTree(tree, new ITraverseTree<CheckBoxNode>() {

			@Override
			public boolean onNode(CheckBoxNode t) {
				TreeItemInfo info = (TreeItemInfo) t.getUserObject();
				try {
					roles.put(info.data.getString("id"), t);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return true;
			}
		});

	}

	public void init(RoleType rt) {
		IDBConnection db = this.db.get();

		JSONArray data = RoleInitDialog.getRoleDBInfo(rt, db);
		if (data == null)
			data = new JSONArray();

		try {
			final JList<ICheck> listView;
			switch (rt) {
			case rtTree:
				setMenuOrTree(data, navTree, navRoles);
				break;
			case rtMenu:
				setMenuOrTree(data, menutree, menuRoles);
				break;
			case rtData:
				initRoleList(dataroletree, Roler.instance(), groups);
				break;
			case rtView: {
				HashMap<String, ListItemData> list = null;
				listView = uilist;
				viewRoles.clear();
				list = viewRoles;

				DefaultListModel<ICheck> model = new DefaultListModel<>();
				if (data != null) {

					for (int i = 0; i < data.length(); i++) {
						JSONObject values = data.getJSONObject(i);
						ListItemData info = new ListItemData(values);
						model.addElement(info);
						list.put(values.getString("id"), info);
					}
				}

				try {
					SwingUtilities.invokeAndWait(new Runnable() {

						@Override
						public void run() {
							listView.setModel(model);
							listView.updateUI();
						}
					});
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				break;
			}
			case rtButton: {
				HashMap<String, DefaultMutableTreeNode> nodes = new HashMap<>();
				DefaultTreeModel model = TreeHelp.newTreeModel(new INewNode() {

					@Override
					public DefaultMutableTreeNode newNode() {
						return new CheckBoxNode();
					}
				});
				buttonRoles.clear();
								
				for (int i = 0; i < data.length(); i++) {
					JSONObject values = data.getJSONObject(i);

					DefaultMutableTreeNode parent = null;
					String pidText = values.getString("pid");
					String pid = values.getString("id");
					String[] tmps = pid.split("\\.");

					pid = tmps[0];
					String text = values.getString("text");

					if (nodes.containsKey(pid)) {
						parent = nodes.get(pid);
					} else {
						parent = TreeHelp.addTreeNode(buttontree, model, "新建", "id", "text", "pid", false, new INewNode() {

							@Override
							public DefaultMutableTreeNode newNode() {
								return new CheckBoxNode();
							}
						});
						TreeItemInfo info = (TreeItemInfo) parent.getUserObject();
						info.data.put("id", pid);
						info.rename(pidText);
						nodes.put(pid, parent);
					}
					DefaultMutableTreeNode node = TreeHelp.addTreeNode(buttontree, "新建", model, parent, "id", "text",
							"pid", new INewNode() {

								@Override
								public DefaultMutableTreeNode newNode() {
									return new CheckBoxNode();
								}
							});
					TreeItemInfo info = (TreeItemInfo) node.getUserObject();
					info.rename(text);
					info.data.put("id", values.getString("id"));
					buttonRoles.put(values.getString("id"), (CheckBoxNode) node);

				}

				try {
					SwingUtilities.invokeAndWait(new Runnable() {

						@Override
						public void run() {
							buttontree.setModel(model);
							TreeHelp.expandOrCollapse(buttontree, (DefaultMutableTreeNode) model.getRoot(), true);
							buttontree.updateUI();
						}
					});
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				break;
			}
			default:
				break;

			}

		} catch (JSONException e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}

	ButtonGroup buttonGroup = new ButtonGroup();

	/**
	 * Create the dialog.
	 */

	JSONObject menuData = GlobalInstance.instance().getMainManuData();
	JSONObject navData = GlobalInstance.instance().getMainNavData();
	HashMap<String, String> workflowNodeNames = GlobalInstance.instance().getModelNameAndIds();
	private JTree navTree;
	private JList<UserInfo> userList;
	private JList<RoleInfo> customRoles;
	private CheckBoxList<String> customRoleItems;
	private JComboBox<CustomDataRoleInfo> customRoleSelector;

	protected void setManuAndTree(JTree tree, JSONObject data) {
		if (tree.getSelectionPath() == null)
			return;

		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
		if (node instanceof CheckBoxNode) {
			CheckBoxNode checkBoxNode = (CheckBoxNode) node;

			TreeItemInfo info = (TreeItemInfo) checkBoxNode.getUserObject();
			if (info == null)
				return;

			if (!info.data.has(JsonTreeDataEditor.id))
				return;

			try {
				String id = info.data.getString(JsonTreeDataEditor.id);

				if (!data.has(id))
					return;

				JSONObject cur = data.getJSONObject(id);
				if (cur.has(JsonTreeDataEditor.jumpid)) {
					String nodename = cur.getString(JsonTreeDataEditor.jumpid);
					if (!workflowNodeNames.containsKey(nodename))
						return;
					String nodeid = workflowNodeNames.get(nodename);
					TreeHelp.findAndScroll(buttontree, nodeid, JsonTreeDataEditor.id);
					if (viewRoles.containsKey(nodeid)) {
						uilist.setSelectedValue(viewRoles.get(nodeid), true);
					}
				}
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
		}

	}

	protected void configTree(JTree tree, JSONObject data) {
		tree.setScrollsOnExpand(false);
		tree.setExpandsSelectedPaths(false);
		tree.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				setManuAndTree(tree, data);
			}
		});
		tree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		tree.setShowsRootHandles(true);

		CheckBoxNodeConfig.config(tree, new ISelection() {

			@Override
			public void onSelected(CheckBoxNode selectNode) {
				if (roleTree.getSelectionPath() == null || roleTree.getSelectionPath().getLastPathComponent() == null)
					return;

				isEdit = true;
			}
		});

	}

	protected void addGroup() {
		String name = MsgHelper.showInputDialog("请输入组ID：");
		if (name == null || name.isEmpty())
			return;
		addGroup(name);
	}

	protected void addGroup(String name) {
		if (groups.containsKey(name)) {
			MsgHelper.showMessage("输入的组ID已经存在，请重新输入！");
			return;
		}

		DefaultMutableTreeNode node = TreeHelp.addTreeNode(roleTree, name, "id", "text", "pid", false, new INewNode() {

			@Override
			public DefaultMutableTreeNode newNode() {
				return new CheckBoxNode();
			}
		});

		TreeItemInfo info = (TreeItemInfo) node.getUserObject();

		IDBConnection db = this.db.get();

		ISqlBuilder sqlBuilder;
		try {
			sqlBuilder = IDBConnection.getSqlBuilder(db);
			sqlBuilder.addField("*");
			sqlBuilder.addTable("workflow_group");
			sqlBuilder.addWhere("1", Operation.otEqual, new Object[] { "2" });
			IDataset dataset = db.query(sqlBuilder);

			IRow row = dataset.newRow();
			row.setValue("groupid", name);
			row.setValue("groupname", name);
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
			} catch (Exception ex) {
				db.rollbackTran();
				ex.printStackTrace();
				MsgHelper.showException(ex);
			}
			groups.put(name, info.data);

			Roler.instance().initGroup(name);

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

		deleteGroup(node);
	}

	protected void deleteGroup(DefaultMutableTreeNode node) {
		if (node != null) {
			IDBConnection db = this.db.get();

			TreeItemInfo info = (TreeItemInfo) node.getUserObject();
			try {

				String groupId = info.data.getString("id");
				List<ISqlBuilder> delBuilders = new ArrayList<>();
				List<GroupInfo> groupInfos = Roler.instance().getGroups().getGroups(groupId);
				for (GroupInfo groupInfo : groupInfos) {

					groupId = groupInfo.groupid;
					ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
					sqlBuilder.addTable("workflow_group");
					sqlBuilder.addWhere("groupid", Operation.otEqual, new Object[] { groupId });
					sqlBuilder.setSqlType(SqlType.stDelete);
					delBuilders.add(sqlBuilder);
				}

				db.beginTran();
				try {
					db.execute(delBuilders);
					db.commitTran();
				} catch (Exception e) {
					db.rollbackTran();
					MsgHelper.showException(e);
					return;
				}

				for (GroupInfo groupInfo : groupInfos) {
					groupId = groupInfo.groupid;
					if (groups.containsKey(groupId))
						groups.remove(groupId);

					Roler.instance().removeGroup(groupId);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				MsgHelper.showException(e1);
			}
		}
	}

	protected void saveDataRole() {
		String groupid = getCurrentGroupID();
		if (groupid == null)
			return;

		IDBConnection db = this.db.get();

		db.beginTran();
		try {
			saveDataRole(dataroletree, groupid);
			db.commitTran();

			Roler.instance().getDataRoles().initGroup(groupid);

			DefaultMutableTreeNode node = roleTree.getSelectionPath() != null
					? (DefaultMutableTreeNode) roleTree.getSelectionPath().getLastPathComponent()
					: null;
			selectRoleNode(node, node);
		} catch (Exception e1) {
			try {
				db.rollbackTran();
			} catch (SQLException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			e1.printStackTrace();
			MsgHelper.showException(e1);
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

	CustomDataRoleManager customDataRoleManager;
	GroupCustomDataRoleManager groupCustomDataRoleManager;

	protected void initCustomDataRoleList() throws InvocationTargetException, InterruptedException {
		DefaultComboBoxModel<CustomDataRoleInfo> model = new DefaultComboBoxModel<CustomDataRoleInfo>();

		for (CustomDataRoleInfo info : customDataRoleManager.refresh()) {
			model.addElement(info);
		}

		SwingUtilities.invokeAndWait(new Runnable() {

			@Override
			public void run() {
				customRoleSelector.setModel(model);
				customRoleSelector.updateUI();
			}
		});
	}

	protected void selectCustomDataRole(String groupid) throws Exception {
		Map<DataOperType, GroupCustomDataRoleInfo> infoMap = Roler.instance().getGroupCustomDataRoles()
				.getRoleInfo(groupid);
		if (infoMap == null)
			return;

		if (!infoMap.containsKey(getOperType())) {
			return;
		}

		GroupCustomDataRoleInfo info = infoMap.get(getOperType());
		DefaultListModel<RoleInfo> model = new DefaultListModel<>();
		for (RoleInfo roleInfo : info.roles.values()) {
			model.addElement(roleInfo);
		}

		customRoles.setModel(model);
		customRoles.updateUI();

		if (model.size() > 0)
			customRoles.setSelectedIndex(0);
	}

	protected void selectCustomDataRoleItem(RoleInfo roleInfo) {
		CustomDataRoleInfo info = customDataRoleManager.get(roleInfo.name);

		DefaultListModel<String> model = new DefaultListModel<String>();

		IDBConnection db = this.db.get();

		if (info.items == null) {
			switch (info.useType) {
			case utList:
				info.items = new ArrayList<>(info.listInfo.items.keySet());
				break;
			case utSQL:
				IDataset dataset;
				try {
					dataset = db.query(info.sqlInfo.sql, null);
				} catch (Exception e) {
					e.printStackTrace();
					MsgHelper.showException(e);
					return;
				}
				if (dataset == null || dataset.getRows() == null)
					return;

				info.items = new ArrayList<>();
				for (IRow row : dataset.getRows()) {
					info.items.add(row.getValue(0).toString());
				}
				break;
			default:
				return;
			}
		}

		for (String item : info.items) {
			model.addElement(item);
		}

		customRoleItems.setModel(model);

		if (roleInfo.items.size() > 0) {
			int[] checks = new int[roleInfo.items.size()];
			int i = 0;
			for (String item : new ArrayList<>(roleInfo.items.values())) {
				int index = model.indexOf(item);
				if (index == -1)
					roleInfo.items.remove(item);
				else
					checks[i++] = index;
			}

			customRoleItems.setChecks(checks);
		} else
			customRoleItems.updateUI();
	}

	public RoleConfigDialog(IDBConnection db) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClose();
			}
		});

		this.dbConnectionInfo = db.getConnectionInfo();
		customDataRoleManager = CustomDataRoleManager.getManager(db);
		groupCustomDataRoleManager = GroupCustomDataRoleManager.getManager(db);

		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle("权限管理");
		setIconImage(Toolkit.getDefaultToolkit().getImage(RoleConfigDialog.class.getResource("/image/browser.png")));
		setBounds(100, 100, 1431, 822);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.4);
		addSplitResizeEvent(splitPane);
		contentPanel.add(splitPane, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		splitPane.setRightComponent(panel);

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
						saveRole((DefaultMutableTreeNode) roleTree.getSelectionPath().getLastPathComponent());
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		});
		toolBar.add(button_6);

		toolBar.addSeparator();
		JButton button_8 = new JButton("查看实际权限");
		button_8.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Roler.showRealRole(new String[] { getCurrentGroupID() }, Roler.instance());
			}
		});
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

		button_8.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_8);

		JButton button_7 = new JButton("刷新");
		button_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				initRoleList(dataroletree, Roler.instance(), groups);
			}
		});
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(button_7);

		JButton button_1 = new JButton("保存");
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveDataRole();
			}
		});
		panel.add(button_1);

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

		JSplitPane splitPane_2 = new JSplitPane();
		splitPane_2.setResizeWeight(0.5);
		addSplitResizeEvent(splitPane_2);
		panel.add(splitPane_2);

		JSplitPane splitPane_3 = new JSplitPane();
		splitPane_3.setResizeWeight(0.5);
		splitPane_3.setOrientation(JSplitPane.VERTICAL_SPLIT);
		addSplitResizeEvent(splitPane_3);
		splitPane_2.setLeftComponent(splitPane_3);

		JPanel panel_2 = new JPanel();
		splitPane_3.setLeftComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		panel_2.add(tabbedPane);
		tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("菜单权限", null, panel_1, null);
		panel_1.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_1 = new JScrollPane();
		panel_1.add(scrollPane_1);

		menutree = new ModelSearchView.TreeModelSearchView();
		menutree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("正在初始化。。。") {
				private static final long serialVersionUID = 1L;

				{
				}
			}
		));
		scrollPane_1.setViewportView(menutree);
		configTree(menutree, menuData);

		JPanel panel_3 = new JPanel();
		tabbedPane.addTab("导航树权限", null, panel_3, null);
		panel_3.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_5 = new JScrollPane();
		panel_3.add(scrollPane_5, BorderLayout.CENTER);

		navTree = new ModelSearchView.TreeModelSearchView();
		navTree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("正在初始化。。。") {
				private static final long serialVersionUID = 1L;

				{
				}
			}
		));
		scrollPane_5.setViewportView(navTree);
		configTree(navTree, navData);

		JPanel panel_5 = new JPanel();
		splitPane_3.setRightComponent(panel_5);
		panel_5.setLayout(new BorderLayout(0, 0));

		label_1 = new JLabel("按钮权限");
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		panel_5.add(label_1, BorderLayout.NORTH);
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JScrollPane scrollPane_2 = new JScrollPane();
		panel_5.add(scrollPane_2);

		buttontree = new ModelSearchView.TreeModelSearchView();
		buttontree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("正在初始化。。。") {
				private static final long serialVersionUID = 1L;

				{
				}
			}
		));
		buttontree.setScrollsOnExpand(false);
		buttontree.setExpandsSelectedPaths(false);
		buttontree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		buttontree.setShowsRootHandles(true);
		scrollPane_2.setViewportView(buttontree);
		CheckBoxNodeConfig.config(buttontree, new ISelection() {

			@Override
			public void onSelected(CheckBoxNode selectNode) {
				if (roleTree.getSelectionPath() == null || roleTree.getSelectionPath().getLastPathComponent() == null)
					return;

				isEdit = true;
			}
		});

		JSplitPane splitPane_4 = new JSplitPane();
		splitPane_4.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane_4.setResizeWeight(0.5);
		addSplitResizeEvent(splitPane_4);
		splitPane_2.setRightComponent(splitPane_4);

		JPanel panel_4 = new JPanel();
		splitPane_4.setLeftComponent(panel_4);
		panel_4.setLayout(new BorderLayout(0, 0));

		JLabel label_2 = new JLabel("界面权限");
		label_2.setHorizontalAlignment(SwingConstants.CENTER);
		panel_4.add(label_2, BorderLayout.NORTH);
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JScrollPane scrollPane_3 = new JScrollPane();
		panel_4.add(scrollPane_3);

		uilist = new ModelSearchView.ListModelSearchView<ICheck>();
		uilist.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_3.setViewportView(uilist);

		setList(uilist);

		JPanel panel_6 = new JPanel();
		splitPane_4.setRightComponent(panel_6);
		panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.Y_AXIS));

		JTabbedPane tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_6.add(tabbedPane_1);

		JPanel panel_10 = new JPanel();
		tabbedPane_1.addTab("基本数据权限设置", null, panel_10, null);
		panel_10.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_4 = new JScrollPane();
		panel_10.add(scrollPane_4);

		dataroletree = new ModelSearchView.TreeModelSearchView();
		dataroletree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("正在初始化。。。") {
				private static final long serialVersionUID = 1L;

				{
				}
			}
		));
		dataroletree.setScrollsOnExpand(false);
		dataroletree.setExpandsSelectedPaths(false);
		dataroletree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_4.setViewportView(dataroletree);

		CheckBoxNodeConfig.config(dataroletree, new ISelection() {

			@Override
			public void onSelected(CheckBoxNode selectNode) {
				if (dataroletree.getSelectionPath() == null
						|| dataroletree.getSelectionPath().getLastPathComponent() == null)
					return;

				isEdit = true;
			}
		});

		JPanel panel_9 = new JPanel();
		panel_10.add(panel_9, BorderLayout.NORTH);
		panel_9.setLayout(new BoxLayout(panel_9, BoxLayout.Y_AXIS));

		JPanel panel_7 = new JPanel();
		panel_9.add(panel_7);
		panel_7.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel label_3 = new JLabel("数据权限");
		panel_7.add(label_3);
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		dataroletype = new JComboBox<String>();
		panel_7.add(dataroletype);
		dataroletype.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		dataroletype.setModel(new DefaultComboBoxModel<String>(new String[] { "数据查看权限", "数据修改权限" }));

		dataroletype.setSelectedIndex(0);

		JPanel panel_8 = new JPanel();
		panel_9.add(panel_8);

		selfdata = new JRadioButton("仅自身数据");
		panel_8.add(selfdata);
		selfdata.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		selfdata.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEdit = true;
				dataroletree.setEnabled(false);
			}
		});
		selfdata.setHorizontalAlignment(SwingConstants.CENTER);

		buttonGroup.add(selfdata);

		groupdata = new JRadioButton("仅所在组数据");
		panel_8.add(groupdata);
		groupdata.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		groupdata.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEdit = true;
				dataroletree.setEnabled(false);
			}
		});
		groupdata.setHorizontalAlignment(SwingConstants.CENTER);
		buttonGroup.add(groupdata);

		groupsdata = new JRadioButton("选定组数据");
		panel_8.add(groupsdata);
		groupsdata.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		groupsdata.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isEdit = true;
				dataroletree.setEnabled(true);
			}
		});
		groupsdata.setHorizontalAlignment(SwingConstants.CENTER);
		buttonGroup.add(groupsdata);

		dataroletype.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String groupid = getCurrentGroupID();
					setUserDataRoleType(groupid, getOperTypeString());
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});

		JPanel panel_11 = new JPanel();
		tabbedPane_1.addTab("自定义数据权限设置", null, panel_11, null);
		panel_11.setLayout(new BorderLayout(0, 0));

		JPanel panel_12 = new JPanel();
		panel_11.add(panel_12, BorderLayout.NORTH);

		JLabel label = new JLabel("权限项目");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_12.add(label);

		customRoleSelector = new JComboBox<>();
		customRoleSelector.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_12.add(customRoleSelector);

		JButton button_2 = new JButton("添加");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String groupid = getCurrentGroupID();
				if (groupid == null || groupid.isEmpty())
					return;

				if (customRoleSelector.getSelectedItem() == null)
					return;

				GroupCustomDataRoleInfo info;
				try {
					info = groupCustomDataRoleManager.add(groupid, getOperType());
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
					return;
				}
				CustomDataRoleInfo dataRoleInfo = (CustomDataRoleInfo) customRoleSelector.getSelectedItem();
				DefaultListModel<RoleInfo> model = (DefaultListModel<RoleInfo>) customRoles.getModel();
				for (int i = 0; i < model.getSize(); i++) {
					RoleInfo roleInfo = model.getElementAt(i);
					if (roleInfo.name.equals(dataRoleInfo.name)) {
						MsgHelper.showMessage("项目已经存在！");
						return;
					}
				}

				RoleInfo roleInfo = groupCustomDataRoleManager.addRoleInfo(info, dataRoleInfo.name);

				model.addElement(roleInfo);

				customRoles.setSelectedIndex(model.size() - 1);

				customRoles.updateUI();
			}
		});
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_12.add(button_2);

		JButton button_3 = new JButton("删除");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GroupCustomDataRoleInfo info = groupCustomDataRoleManager.get(getCurrentGroupID(), getOperType());
				if (info == null)
					return;

				if (customRoles.getSelectedValue() == null)
					return;

				if (MsgHelper.showConfirmDialog("是否删除选定的项目？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					return;
				}

				try {
					groupCustomDataRoleManager.remove(info, customRoles.getSelectedValue());
					customRoleItems.setModel(new DefaultListModel<>());
					DefaultListModel<RoleInfo> model = (DefaultListModel<RoleInfo>) customRoles.getModel();
					model.removeElement(customRoles.getSelectedValue());
					customRoles.updateUI();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_12.add(button_3);

		JButton button_4 = new JButton("保存");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					GroupCustomDataRoleInfo info = groupCustomDataRoleManager.get(getCurrentGroupID(), getOperType());
					if (info == null)
						return;

					groupCustomDataRoleManager.save(info);
					MsgHelper.showMessage("保存成功！");
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});

		JButton button_5 = new JButton("全部删除");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GroupCustomDataRoleInfo info = groupCustomDataRoleManager.get(getCurrentGroupID(), getOperType());
				if (info == null)
					return;

				if (MsgHelper.showConfirmDialog("是否删除所有项目？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					return;
				}

				try {
					groupCustomDataRoleManager.remove(info);
					customRoleItems.setModel(new DefaultListModel<>());
					customRoles.setModel(new DefaultListModel<>());
					customRoles.updateUI();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_12.add(button_5);
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		panel_12.add(button_4);

		JSplitPane splitPane_5 = new JSplitPane();
		addSplitResizeEvent(splitPane_5);
		splitPane_5.setResizeWeight(0.5);
		panel_11.add(splitPane_5, BorderLayout.CENTER);

		JPanel panel_13 = new JPanel();
		splitPane_5.setLeftComponent(panel_13);
		panel_13.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_7 = new JScrollPane();
		panel_13.add(scrollPane_7, BorderLayout.CENTER);

		customRoles = new ModelSearchView.ListModelSearchView<>();
		customRoles.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		customRoles.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				if (customRoles.getSelectedValue() == null)
					return;

				int oldIndex = e.getFirstIndex();
				if (e.getFirstIndex() == customRoleItems.getSelectedIndex()) {
					oldIndex = e.getLastIndex();
				}

				if (oldIndex != -1) {

				}

				selectCustomDataRoleItem(customRoles.getSelectedValue());
			}
		});
		scrollPane_7.setViewportView(customRoles);

		JPanel panel_14 = new JPanel();
		splitPane_5.setRightComponent(panel_14);
		panel_14.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_8 = new JScrollPane();
		panel_14.add(scrollPane_8, BorderLayout.CENTER);

		customRoleItems = new ModelSearchView.CheckBoxListModelSearchView<>();
		customRoleItems.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		customRoleItems.addCheckedListener(new CheckBoxList.ICheckedListener() {

			@Override
			public void onCheck(boolean isCheck, Object value) {
				if (customRoles.getSelectedValue() == null)
					return;

				GroupCustomDataRoleInfo info = groupCustomDataRoleManager.get(getCurrentGroupID(), getOperType());
				if (info == null)
					return;
				if (customRoles.getSelectedValue() == null)
					return;

				RoleInfo roleInfo = info.roles.get(customRoles.getSelectedValue().name);
				String item = (String) value;
				if (isCheck)
					roleInfo.items.put(item, item);
				else
					roleInfo.items.remove(item);
			}
		});
		scrollPane_8.setViewportView(customRoleItems);

		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setResizeWeight(0.7);
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane_1.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				splitPane_1.setDividerLocation(splitPane_1.getResizeWeight());
				splitPane_1.setResizeWeight(splitPane_1.getResizeWeight());
			}
		});
		splitPane.setLeftComponent(splitPane_1);

		JScrollPane scrollPane = new JScrollPane();
		splitPane_1.setLeftComponent(scrollPane);

		roleTree = new ModelSearchView.TreeDragModelSearchView();
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

				selectRoleNode(newNode, oldNode);
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
		initRoleList(roleTree, Roler.instance(), groups);

		JScrollPane scrollPane_6 = new JScrollPane();
		splitPane_1.setRightComponent(scrollPane_6);

		userList = new ModelSearchView.ListModelSearchView<>();
		userList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (userList.getSelectedValue() == null)
					return;

				if (e.getClickCount() >= 2) {
					UserInfo userInfo = userList.getSelectedValue();
					GroupInfo groupInfo = Roler.instance().getGroups().getGroup(getCurrentGroupID());
					if (MsgHelper.showConfirmDialog(
							"是否将用户【" + userInfo.username + "】从组【" + groupInfo.groupname + "】中移除？",
							JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
						return;
					try {
						removeUser(userInfo.userid, groupInfo.groupid);
					} catch (Exception e2) {
						MsgHelper.showException(e2);
					}
				}
			}
		});
		scrollPane_6.setViewportView(userList);

		try {
			init();
		} catch (Exception e1) {
			e1.printStackTrace();
			MsgHelper.showException(e1);
		}

		tabbedPane_1.setSelectedIndex(0);

		SwingTools.showMaxDialog(this);
		inited = true;
	}

	public void removeUser(String userid, String groupid) throws Exception {
		IDBConnection db = this.db.get();

		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addTable("workflow_group_user");
		sqlBuilder.addWhere("userid", Operation.otEqual, new Object[] { userid });
		sqlBuilder.addLogicalOperation(LogicalOperation.otAnd);
		sqlBuilder.addWhere("groupid", Operation.otEqual, new Object[] { groupid });
		sqlBuilder.setSqlType(SqlType.stDelete);
		db.execute(sqlBuilder);

	}

	public static void showDialog(IDBConnection db) {
		if (db == null || !db.isOpen()) {
			MsgHelper.showMessage("请先连接数据库！");
			return;
		}
		RoleConfigDialog dialog = new RoleConfigDialog(db);
		dialog.setModal(true);
		dialog.setVisible(true);
	}
}
