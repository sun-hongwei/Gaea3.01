package com.wh.gaea.plugin.role;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.encrypt.Encryption;
import com.wh.gaea.control.modelsearch.ModelSearchView;
import com.wh.gaea.editor.JsonEditorDialog;
import com.wh.gaea.editor.ListItemData;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.parallel.computing.ParallelComputingExecutor;
import com.wh.parallel.computing.interfaces.ISimpleActionComputer;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.SwingTools;
import com.wh.swing.tools.checkboxnode.CheckBoxNode;
import com.wh.swing.tools.checkboxnode.CheckBoxNode.ISelection;
import com.wh.swing.tools.checkboxnode.CheckBoxNodeConfig;
import com.wh.swing.tools.dialog.WaitDialog;
import com.wh.swing.tools.dialog.WaitDialog.IProcess;
import com.wh.swing.tools.tree.TreeHelp;
import com.wh.swing.tools.tree.TreeHelp.INewNode;
import com.wh.swing.tools.tree.TreeHelp.ITraverseTree;
import com.wh.swing.tools.tree.TreeHelp.TreeItemInfo;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.Operation;
import wh.interfaces.ISqlBuilder.SqlType;
import wh.role.interfaces.IInfos;
import wh.role.obj.GroupInfo;
import wh.role.obj.RoleServiceObject.DataOperType;
import wh.role.obj.RoleServiceObject.ITraverse;
import wh.role.obj.UserInfo;

public class UserManagerDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTree roletree;

	IDBConnection db;

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

	public void init() {
		initUser();
		initRole();
	}

	public void saveTree(IDataset dataset, JTree tree, String userid) {
		TreeHelp.traverseTree(tree, new ITraverseTree<CheckBoxNode>() {

			@Override
			public boolean onNode(CheckBoxNode t) {
				if (t.isSelected()) {
					TreeItemInfo tmp = (TreeItemInfo) t.getUserObject();
					IRow row = dataset.newRow();
					try {
						String groupid = tmp.data.getString("id");
						row.setValue("userid", userid);
						row.setValue("groupid", groupid);
						row.setValue("updatetime", new Date());
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

	public void saveRole(ListItemData info) throws Exception {
		saveRole(info, false);
	}

	public void saveRole(ListItemData info, boolean noTran) throws Exception {
		saveRole(info, noTran, isEdit, false, false);
	}

	public void saveRole(ListItemData info, boolean noTran, boolean isEdit, boolean isOnlyRole, boolean isOnlyUserRole)
			throws Exception {
		if (info == null)
			return;

		if (!isEdit)
			return;

		ISqlBuilder sqlBuilder = null;
		String groupuserTableName = "workflow_group_user";
		String userid = info.data.getString("id");

		IDataset groupDataset = null;

		if (!isOnlyRole || (isOnlyRole && !isOnlyUserRole)) {
			sqlBuilder = IDBConnection.getSqlBuilder(db);
			sqlBuilder.addField("groupid, userid, updatetime");
			sqlBuilder.addTable(groupuserTableName);
			sqlBuilder.addWhere("1", Operation.otEqual, new Object[] { 2 });
			groupDataset = db.query(sqlBuilder);

			saveTree(groupDataset, roletree, userid);
		}

		ISqlBuilder userBuilder = IDBConnection.getSqlBuilder(db);
		userBuilder.addField("*");
		userBuilder.addTable("workflow_user");
		userBuilder.addWhere("userid", Operation.otEqual, new Object[] { userid });
		IDataset userDataset = db.query(userBuilder);

		IRow userInfo = null;
		if (userDataset.getRowCount() == 0) {
			userInfo = userDataset.newRow();
			userDataset.addRow(userInfo);
		} else
			userInfo = userDataset.getRow(0);

		if (!isOnlyRole) {
			userInfo.setValue("userid", this.userid.getText());
			userInfo.setValue("username", this.username.getText());
			userInfo.setValue("password", this.password.getText());
		}

		userInfo.setValue("superview", viewRole.isSelected());
		userInfo.setValue("superbutton", buttonRole.isSelected());
		userInfo.setValue("supermenu", menuRole.isSelected());
		userInfo.setValue("superdata", dataRole.isSelected());

		if (!noTran)
			db.beginTran();
		try {
			ISqlBuilder delBuilder;
			if (groupDataset != null) {
				delBuilder = IDBConnection.getSqlBuilder(db);
				delBuilder.addTable(groupuserTableName);
				delBuilder.addWhere("userid", Operation.otEqual, new Object[] { userid });
				delBuilder.setSqlType(SqlType.stDelete);
				db.execute(delBuilder);
				groupDataset.post(db);
			}

//			delBuilder = IDBConnection.getSqlBuilder(db);
//			delBuilder.addTable("workflow_user");
//			delBuilder.addWhere("userid", Operation.otEqual, new Object[] { userid });
//			delBuilder.setSqlType(SqlType.stDelete);
//			db.execute(delBuilder);

			userDataset.post(db);
			if (!noTran) {
				db.commitTran();
				Roler.instance().initUser(userid);
				isEdit = false;
			}
		} catch (Exception e) {
			if (!noTran) {
				db.rollbackTran();
				MsgHelper.showException(e);
			} else {
				throw e;
			}
		}
	}

	public void setUserRole(String userid) throws Exception {

		resetRoles();

		UserInfo userInfo = Roler.instance().getUsers().getUser(userid);
		if (userInfo == null)
			return;

		this.userid.setText(userInfo.userid);
		username.setText(userInfo.username);
		password.setText(userInfo.password);

		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		lastLoginTime.setText(dt.format(userInfo.lasttime));
		registerTime.setText(dt.format(userInfo.registertime));

		dataRole.setSelected(userInfo.superdata);
		viewRole.setSelected(userInfo.superview);
		buttonRole.setSelected(userInfo.superbutton);
		menuRole.setSelected(userInfo.supermenu);

		Map<String, GroupInfo> groups = Roler.instance().getGroups().getUserGroupTree(userid);
		TreeHelp.traverseTree(roletree, new ITraverseTree<CheckBoxNode>() {

			@Override
			public boolean onNode(CheckBoxNode t) {
				TreeItemInfo info = (TreeItemInfo) t.getUserObject();
				try {
					String groupid = info.data.getString("id");
					t.setSingleSelected(groups.containsKey(groupid));
				} catch (JSONException e) {
					e.printStackTrace();
				}

				return true;
			}
		});

		isEdit = false;
		roletree.updateUI();
	}

	HashMap<String, CheckBoxNode> roles = new HashMap<>();
	HashMap<String, ListItemData> users = new HashMap<>();
	private JList<ListItemData> userlist;
	private JTextField password;
	private JCheckBox viewRole;
	private JTextField registerTime;
	private JTextField lastLoginTime;
	private JCheckBox buttonRole;
	private JCheckBox menuRole;
	private JCheckBox dataRole;
	private JTextField userid;
	private JTextField username;

	public void resetRoles() {
		userid.setText("");
		username.setText("");
		password.setText("");
		lastLoginTime.setText("");
		registerTime.setText("");

		dataRole.setSelected(false);
		viewRole.setSelected(false);
		buttonRole.setSelected(false);
		menuRole.setSelected(false);

		for (CheckBoxNode node : roles.values()) {
			node.setSelected(false);
		}

		isEdit = false;
	}

	@SuppressWarnings("unchecked")
	public void initRole() {
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
				}

			});

			roletree.setModel(new DefaultTreeModel(null));
			TreeHelp.jsonToTree(roletree, datas, "id", "text", "pid", new INewNode() {

				@Override
				public DefaultMutableTreeNode newNode() {
					return new CheckBoxNode();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		isEdit = false;
	}

	public void removeUser() throws Exception {
		if (userlist.getSelectedValue() == null) {
			MsgHelper.showMessage("请先选择用户！");
			return;
		}

		String userid = userlist.getSelectedValue().data.getString("id");
		db.beginTran();
		try {
			ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
			sqlBuilder.addTable("workflow_user");
			sqlBuilder.addWhere("userid", Operation.otEqual, new Object[] { userid });
			sqlBuilder.setSqlType(SqlType.stDelete);
			db.execute(sqlBuilder);

			sqlBuilder = IDBConnection.getSqlBuilder(db);
			sqlBuilder.addTable("workflow_group_user");
			sqlBuilder.addWhere("userid", Operation.otEqual, new Object[] { userid });
			sqlBuilder.setSqlType(SqlType.stDelete);
			db.execute(sqlBuilder);

			db.commitTran();

			Roler.instance().init();

			users.remove(userid);
			DefaultListModel<ListItemData> model = (DefaultListModel<ListItemData>) userlist.getModel();
			model.removeElement(userlist.getSelectedValue());

			resetRoles();

			Roler.instance().removeUser(userid);
			userlist.updateUI();
			isEdit = false;
		} catch (Exception e) {
			db.rollbackTran();
			MsgHelper.showException(e);
		}

	}

	public void addUser() throws Exception {
		String name = MsgHelper.showInputDialog("请输入用户名称：");
		if (name == null || name.isEmpty())
			return;

		if (users.containsKey(name)) {
			MsgHelper.showMessage("输入的用户名称已经存在，请重新输入！");
			return;
		}

		String pwd = Encryption.MD5Util.MD5("123456");
		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("userid, username, password");
		sqlBuilder.addTable("workflow_user");
		sqlBuilder.addWhere("1", Operation.otEqual, new Object[] { "2" });
		IDataset dataset = db.query(sqlBuilder);
		IRow row = dataset.newRow();
		row.setValue("userid", name);
		row.setValue("username", name);
		row.setValue("password", pwd);
//		row.setValue("superview", 0);
//		row.setValue("superbutton", 0);
//		row.setValue("supermenu", 0);
//		row.setValue("superdata", 0);
//		row.setValue("registertime", new Date());
//		row.setValue("lasttime", new Date());
		dataset.addRow(row);
		db.beginTran();
		try {
			dataset.post(db);
			db.commitTran();
		} catch (Exception e) {
			db.rollbackTran();
			e.printStackTrace();
			MsgHelper.showException(e);
			return;
		}

		Roler.instance().initUser(name);

		JSONObject value = new JSONObject();
		value.put("id", name);
		value.put("text", name);
		value.put("password", pwd);
		value.put("superview", 0);
		value.put("superbutton", 0);
		value.put("supermenu", 0);
		value.put("superdata", 0);
		value.put("registertime", new Date());
		value.put("lasttime", new Date());

		ListModel<ListItemData> model = (ListModel<ListItemData>) userlist.getModel();
		ListItemData info = new ListItemData(value);
		((DefaultListModel<ListItemData>) model).addElement(info);
		users.put(name, info);
		userlist.setSelectedValue(info, true);
		userlist.updateUI();
	}

	public void editUser() throws JSONException {
		if (userlist.getSelectedValue() == null) {
			MsgHelper.showMessage("请先选择用户！");
			return;
		}

		String oldName = userlist.getSelectedValue().data.getString("id");
		String name = MsgHelper.showInputDialog("请输入新用户名称：", oldName);
		if (name == null || name.isEmpty() || name.compareToIgnoreCase(oldName) == 0)
			return;

		if (users.containsKey(name)) {
			MsgHelper.showMessage("输入的用户名称已经存在，请重新输入！");
			return;
		}

		userlist.getSelectedValue().updateID(name);
	}

	@SuppressWarnings("unchecked")
	public void initUser() {
		try {
			ListModel<ListItemData> model = new DefaultListModel<>();
			userlist.setModel(model);
			users.clear();
			((IInfos<UserInfo>) Roler.instance().getUsers()).traverse(new ITraverse<UserInfo>() {

				@Override
				public void callback(UserInfo t) {
					JSONObject value = new JSONObject();
					value.put("id", t.userid);
					value.put("text", t.username);
					value.put("password", t.password);
					value.put("superview", t.superview);
					value.put("superbutton", t.superbutton);
					value.put("supermenu", t.supermenu);
					value.put("superdata", t.superdata);
					value.put("registertime", t.registertime);
					value.put("lasttime", t.lasttime);
					ListItemData info = new ListItemData(value);
					((DefaultListModel<ListItemData>) model).addElement(info);
					users.put(value.getString("id"), info);
				}
			});

			userlist.updateUI();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */

	boolean needChangeEdit = true;
	private JEditorPane editorPane;

	public void updateEditState() {
		if (needChangeEdit)
			isEdit = true;
	}

	IMainControl mainControl;
	private JCheckBox onlyUserRoleView;

	public UserManagerDialog(IDBConnection db, IMainControl mainControl) {
		this.mainControl = mainControl;
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onClose();
			}
		});
		this.db = db;
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setTitle("用户管理");
		setIconImage(Toolkit.getDefaultToolkit().getImage(RoleConfigDialog.class.getResource("/image/browser.png")));
		setBounds(100, 100, 1171, 811);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		contentPanel.add(splitPane, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(0, 0));
		splitPane.setLeftComponent(scrollPane);

		userlist = new ModelSearchView.ListModelSearchView<>();
		userlist.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		userlist.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				int index = -1;
				if (e.getFirstIndex() != e.getLastIndex()) {
					if (e.getFirstIndex() == userlist.getSelectedIndex()) {
						index = e.getLastIndex();
					} else {
						index = e.getFirstIndex();
					}
				}
				try {
					if (index != -1) {
						ListItemData old = userlist.getModel().getElementAt(index);
						if (isEdit)
							if (MsgHelper.showConfirmDialog("权限已经修改，是否保存？",
									JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
								saveRole(old);
					}

					resetRoles();

					if (userlist.getSelectedValue() != null)
						setUserRole(userlist.getSelectedValue().getID());
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		scrollPane.setViewportView(userlist);

		JPanel panel = new JPanel();
		splitPane.setRightComponent(panel);
		panel.setLayout(null);

		JLabel lbls = new JLabel("组（s）");
		lbls.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		lbls.setBounds(15, 122, 81, 21);
		panel.add(lbls);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(15, 147, 767, 400);
		panel.add(scrollPane_1);

		roletree = new ModelSearchView.TreeModelSearchView();
		roletree.setScrollsOnExpand(false);
		roletree.setExpandsSelectedPaths(false);
		roletree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		roletree.setShowsRootHandles(true);
		roletree.setBounds(154, 175, 96, 64);
		scrollPane_1.setViewportView(roletree);

		JToolBar toolBar = new JToolBar();
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		contentPanel.add(toolBar, BorderLayout.NORTH);

		JButton btnNewButton = new JButton("添加");
		btnNewButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					addUser();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		toolBar.add(btnNewButton);

		JButton button = new JButton("删除");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					removeUser();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		toolBar.add(button);

		toolBar.addSeparator();

		JButton button_3 = new JButton("查看真实权限");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (userlist.getSelectedValue() == null)
					return;

				String userId = userlist.getSelectedValue().getID();

				Map<String, GroupInfo> groups = Roler.instance().getGroups().getUserGroups(userId);
				Roler.showRealRole(groups.keySet().toArray(new String[groups.size()]), Roler.instance(), false);
				Map<DataOperType, Map<String, UserInfo>> map = Roler.instance().getDataRoles().getUserRole(userId);
				for (DataOperType operType : map.keySet()) {
					String title = "";
					switch (operType) {
					case dtQuery:
						title = "查询权限";
						break;
					case dtUpdate:
						title = "更新权限";
						break;
					}

					JSONArray names = new JSONArray();
					for (UserInfo userInfo : map.get(operType).values()) {
						names.put(userInfo.username);
					}
					MsgHelper.showMessage(title + ":" + names.toString());
				}
			}
		});

		JButton button_6 = new JButton("保存");
		button_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isEdit) {
					if (userlist.getSelectedValue() != null) {
						if (userlist.getSelectedIndices().length > 1) {
							if (MsgHelper.showConfirmDialog("您将覆盖多个用户的权限设置，是否继续？",
									JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
								return;
						}
						db.beginTran();
						try {

							List<ListItemData> list = userlist.getSelectedValuesList();
							boolean onlyRole = list.size() > 1;
							for (ListItemData data : list) {
								saveRole(data, true, true, onlyRole, onlyUserRoleView.isSelected());
							}
							db.commitTran();

							isEdit = false;

							WaitDialog.Show("保存", "正在保存中，请等待。。。", new IProcess() {

								@Override
								public boolean doProc(WaitDialog waitDialog) throws Exception {
									ParallelComputingExecutor<ListItemData> executor = new ParallelComputingExecutor<>(userlist.getSelectedValuesList(), 5);
									executor.execute(new ISimpleActionComputer<ListItemData>() {
										
										@Override
										public void compute(ListItemData data) throws Exception {
											String userid = data.data.getString("id");
											Roler.instance().initUser(userid);
										}
									});
//									for (ListItemData data : userlist.getSelectedValuesList()) {
//										String userid = data.data.getString("id");
//										Roler.instance().initUser(userid);
//									}
									return true;
								}

								@Override
								public void closed(boolean isok, Throwable e) {
									if (isok)
										MsgHelper.showMessage("恭喜，保存用户权限成功！");
									else {
										MsgHelper.showException(e);
									}
								}
							}, null);

						} catch (Exception e1) {
							try {
								db.rollbackTran();
							} catch (SQLException e2) {
								e2.printStackTrace();
							}
							e1.printStackTrace();
							MsgHelper.showException(e1);
							return;
						}
					}
				}
			}
		});

		onlyUserRoleView = new JCheckBox("仅用户授权");
		onlyUserRoleView.setSelected(true);
		onlyUserRoleView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(onlyUserRoleView);
		button_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_6);
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_3);

		CheckBoxNodeConfig.config(roletree, new ISelection() {

			@Override
			public void onSelected(CheckBoxNode selectNode) {
				if (roletree.getSelectionPath() == null || roletree.getSelectionPath().getLastPathComponent() == null)
					return;

				isEdit = true;
			}
		});

		JButton button_4 = new JButton("展开");
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TreeHelp.expandOrCollapse(roletree,
						roletree.getSelectionPath() == null ? null : roletree.getSelectionPath().getParentPath(), true);
			}
		});
		button_4.setBounds(87, 562, 71, 23);
		panel.add(button_4);

		JButton button_5 = new JButton("折叠");
		button_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TreeHelp.expandOrCollapse(roletree,
						roletree.getSelectionPath() == null ? null : roletree.getSelectionPath().getParentPath(),
						false);
			}
		});
		button_5.setBounds(15, 562, 71, 23);
		panel.add(button_5);

		JLabel label_1 = new JLabel("基本信息");
		label_1.setAlignmentY(0.0f);
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_1.setBounds(15, 10, 54, 15);
		panel.add(label_1);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_1.setBounds(15, 35, 767, 77);
		panel.add(panel_1);
		panel_1.setLayout(null);

		JLabel lblid = new JLabel("用户ID");
		lblid.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		lblid.setBounds(5, 13, 54, 15);
		panel_1.add(lblid);

		password = new JTextField();
		password.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		password.setBounds(64, 46, 205, 21);
		panel_1.add(password);
		password.setColumns(10);

		viewRole = new JCheckBox("超级视图权限");
		viewRole.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		viewRole.setBounds(329, 45, 103, 23);
		panel_1.add(viewRole);

		buttonRole = new JCheckBox("超级按钮权限");
		buttonRole.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		buttonRole.setBounds(442, 45, 103, 23);
		panel_1.add(buttonRole);

		menuRole = new JCheckBox("超级菜单权限");
		menuRole.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		menuRole.setBounds(547, 45, 103, 23);
		panel_1.add(menuRole);

		dataRole = new JCheckBox("超级数据权限");
		dataRole.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		dataRole.setBounds(652, 45, 103, 23);
		panel_1.add(dataRole);

		registerTime = new JTextField();
		registerTime.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		registerTime.setEnabled(false);
		registerTime.setColumns(10);
		registerTime.setBounds(418, 10, 131, 21);
		panel_1.add(registerTime);

		JLabel label_3 = new JLabel("注册时间");
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_3.setBounds(359, 13, 54, 15);
		panel_1.add(label_3);

		lastLoginTime = new JTextField();
		lastLoginTime.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		lastLoginTime.setEnabled(false);
		lastLoginTime.setColumns(10);
		lastLoginTime.setBounds(639, 10, 122, 21);
		panel_1.add(lastLoginTime);

		JLabel label_4 = new JLabel("最后登录时间");
		label_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_4.setBounds(554, 13, 80, 15);
		panel_1.add(label_4);

		JLabel label = new JLabel("用户密码");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label.setBounds(5, 49, 54, 15);
		panel_1.add(label);

		userid = new JTextField();
		userid.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		userid.setEditable(false);
		userid.setColumns(10);
		userid.setBounds(64, 10, 113, 21);
		panel_1.add(userid);

		username = new JTextField();
		username.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		username.setColumns(10);
		username.setBounds(241, 10, 113, 21);
		panel_1.add(username);

		JLabel label_5 = new JLabel("用户名称");
		label_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_5.setBounds(182, 13, 54, 15);
		panel_1.add(label_5);

		JButton button_2 = new JButton("生成");
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_2.setMargin(new Insets(2, 2, 2, 2));
		button_2.setBounds(279, 45, 43, 23);
		panel_1.add(button_2);

		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(15, 600, 767, 110);
		panel.add(scrollPane_2);

		editorPane = new JEditorPane();
		editorPane.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scrollPane_2.setViewportView(editorPane);

		JButton button_1 = new JButton("编辑");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object obj = JsonEditorDialog.show(mainControl, editorPane.getText());
				if (obj != null) {
					editorPane.setText(obj.toString());
				}
			}
		});
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.setBounds(713, 562, 65, 23);
		panel.add(button_1);

		buttonRole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateEditState();
			}
		});
		viewRole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateEditState();
			}
		});
		menuRole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateEditState();
			}
		});
		dataRole.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateEditState();
			}
		});

		userid.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateEditState();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateEditState();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateEditState();
			}
		});
		username.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateEditState();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateEditState();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateEditState();
			}
		});
		password.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateEditState();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateEditState();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				updateEditState();
			}
		});
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				password.setText(Encryption.MD5Util.MD5(password.getText()));
			}
		});

		splitPane.setResizeWeight(0.3);
		splitPane.setDividerLocation(0.3);
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				splitPane.setDividerLocation(splitPane.getResizeWeight());
				splitPane.setResizeWeight(splitPane.getResizeWeight());
			}
		});

		init();

		SwingTools.showMaxDialog(this);
	}

	public static void showDialog(IDBConnection db, IMainControl mainControl) {
		if (db == null || !db.isOpen()) {
			MsgHelper.showMessage("请先连接数据库！");
			return;
		}

		UserManagerDialog dialog = new UserManagerDialog(db, mainControl);
		dialog.setModal(true);
		dialog.setVisible(true);
	}
}
