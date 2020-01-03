package com.wh.gaea.plugin;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.interfaces.input.TableInput;
import com.wh.gaea.interfaces.selector.IRoleSelector;
import com.wh.gaea.plugin.role.CustomDataRoleManager;
import com.wh.gaea.plugin.role.CustomRoleDialog;
import com.wh.gaea.plugin.role.GroupCustomDataRoleManager;
import com.wh.gaea.plugin.role.RoleConfigDialog;
import com.wh.gaea.plugin.role.RoleInitDialog;
import com.wh.gaea.plugin.role.RoleInitDialog.RoleType;
import com.wh.gaea.plugin.role.RoleSelectDialog;
import com.wh.gaea.plugin.role.Roler;
import com.wh.gaea.plugin.role.UserManagerDialog;
import com.wh.gaea.role.RoleInfos;
import com.wh.swing.tools.MsgHelper;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.LogicalOperation;
import wh.interfaces.ISqlBuilder.Operation;
import wh.interfaces.ISqlBuilder.SqlType;
import wh.role.interfaces.IInfos;
import wh.role.obj.GroupInfo;
import wh.role.obj.RoleServiceObject.ITraverse;

public class GaeaRolePlugin extends BaseGaeaPlugin implements IGaeaPlugin, IRoleSelector {
	protected static final String NAME_KEY = "功能名称";
	protected AtomicBoolean isReset = new AtomicBoolean(false);
	
	protected JSONObject getDBUpdateStates() throws Exception {
		IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
		ISqlBuilder builder = IDBConnection.getSqlBuilder(db);
		builder.addTable("workflow_state");
		builder.addField("*");
		builder.setSqlType(SqlType.stQuery);
		IDataset dataset = db.query(builder);
		JSONArray rows = new JSONArray();
		for (IRow rowData : dataset.getRows()) {
			JSONObject row = new JSONObject();
			String project = (String) rowData.getValue("project");
			if (project.equals(GlobalInstance.instance().getCurrentProjectName())) {
				row.put("功能名称", rowData.getValue("item"));
				row.put("执行次数", rowData.getValue("count") == null ? 0 : rowData.getValue("count"));
				rows.put(row);
			}
		}

		JSONArray columns = new JSONArray();
		columns.put("功能名称");
		columns.put("执行次数");

		return TableInput.createJsonParam(columns, rows);
	}

	protected void setMenuDBUpdateState(String name, String project, int updateCount) {
		try {
			setDBUpdateState(name, project, updateCount);
			MsgHelper.showMessage("状态已经更新！");
		} catch (Exception e) {
			MsgHelper.showException(e);
		}
	}

	protected void setDBUpdateState(String name, String project, int updateCount) throws Exception {
		IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
		ISqlBuilder builder = IDBConnection.getSqlBuilder(db);
		try {
			builder.addTable("workflow_state");
			builder.addField("project");
			builder.addField("count");
			builder.addField("item");
			builder.addValue(project);
			builder.addValue(updateCount);
			builder.addValue(name);
			builder.setSqlType(SqlType.stInsert);
			if (db.execute(builder) != 0)
				throw new Exception();
		} catch (Exception e) {
			builder.addSet("project", project);
			builder.addSet("count", updateCount);
			builder.addWhere("project", Operation.otEqual, new Object[] { project });
			builder.addLogicalOperation(LogicalOperation.otAnd);
			builder.addWhere("item", Operation.otEqual, new Object[] { name });
			builder.setSqlType(SqlType.stUpdate);
			if (db.execute(builder) != 1)
				throw new Exception("数据更新失败");
		}
	}

	protected void removeDBUpdateState(String name, String project) throws Exception {
		IDBConnection db = GlobalInstance.instance().getMainControl().getDB();
		ISqlBuilder builder = IDBConnection.getSqlBuilder(db);
		builder.addTable("workflow_state");
		builder.addWhere("project", Operation.otEqual, new Object[] { project });
		builder.addLogicalOperation(LogicalOperation.otAnd);
		builder.addWhere("item", Operation.otEqual, new Object[] { name });
		builder.setSqlType(SqlType.stDelete);
		if (db.execute(builder) != 1)
			throw new Exception("数据更新失败");
	}

	@Override
	public void setMenu(JMenu root) {
		getRootMenu(root);
		rootMenu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/menubar/数据.png")));
		
		JSeparator separator = new JSeparator();
		rootMenu.add(separator);

		JMenu menuRoot = new JMenu("权限初始");
		menuRoot.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/config/权限初始.png")));
		rootMenu.add(menuRoot);

		JMenuItem menu = new JMenuItem("数据权限     ");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/config/数据权限.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				if (isReset.get()) {
					MsgHelper.showMessage(null, "正在重新初始化，请稍后再试！");
					return;					
				}
				RoleInitDialog.showDialog(RoleType.rtData, GlobalInstance.instance().getMainControl());
			}
		});
//		menuRoot.add(menu);

		menu = new JMenuItem("功能权限");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/config/功能权限.png")));
		menu.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menu.setVisible(false);
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				if (isReset.get()) {
					MsgHelper.showMessage(null, "正在重新初始化，请稍后再试！");
					return;					
				}
				RoleInitDialog.showDialog(RoleType.rtFunc, GlobalInstance.instance().getMainControl());
			}
		});
//		menuRoot.add(menu);

		menu = new JMenuItem("菜单权限");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/config/菜单权限.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				if (isReset.get()) {
					MsgHelper.showMessage(null, "正在重新初始化，请稍后再试！");
					return;					
				}
				RoleInitDialog.showDialog(RoleType.rtMenu, GlobalInstance.instance().getMainControl());
			}
		});
		menuRoot.add(menu);

		menu = new JMenuItem("视图权限");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/config/视图权限.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				if (isReset.get()) {
					MsgHelper.showMessage(null, "正在重新初始化，请稍后再试！");
					return;					
				}
				RoleInitDialog.showDialog(RoleType.rtView, GlobalInstance.instance().getMainControl());
			}
		});
		menuRoot.add(menu);

		menu = new JMenuItem("按钮权限");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/config/按钮权限.png")));
		menu.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				if (isReset.get()) {
					MsgHelper.showMessage(null, "正在重新初始化，请稍后再试！");
					return;					
				}
				RoleInitDialog.showDialog(RoleType.rtButton, GlobalInstance.instance().getMainControl());
			}
		});
		menuRoot.add(menu);

		menu = new JMenuItem("导航树权限");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/config/导航树权限.png")));
		menu.setFont(new Font("微软雅黑", Font.PLAIN, 13));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				if (isReset.get()) {
					MsgHelper.showMessage(null, "正在重新初始化，请稍后再试！");
					return;					
				}
				RoleInitDialog.showDialog(RoleType.rtTree, GlobalInstance.instance().getMainControl());
			}
		});
		menuRoot.add(menu);

		menuRoot = new JMenu("权限管理");
		menuRoot.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/menubar/权限管理.png")));
		rootMenu.add(menuRoot);

		menu = new JMenuItem("管理角色");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/config/管理角色.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				if (isReset.get()) {
					MsgHelper.showMessage(null, "正在重新初始化，请稍后再试！");
					return;					
				}
				RoleConfigDialog.showDialog(GlobalInstance.instance().getMainControl().getDB());
			}
		});
		menuRoot.add(menu);

		menu = new JMenuItem("管理用户");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/config/管理用户.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				if (isReset.get()) {
					MsgHelper.showMessage(null, "正在重新初始化，请稍后再试！");
					return;					
				}
				UserManagerDialog.showDialog(GlobalInstance.instance().getMainControl().getDB(), GlobalInstance.instance().getMainControl());
			}
		});
		menuRoot.add(menu);

		JSeparator separator_35 = new JSeparator();
		menuRoot.add(separator_35);

		menu = new JMenuItem("设置自定义数据权限");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/config/设置自定义数据权限.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				if (isReset.get()) {
					MsgHelper.showMessage(null, "正在重新初始化，请稍后再试！");
					return;					
				}
				CustomRoleDialog.showDialog(GlobalInstance.instance().getMainControl().getDB());
			}
		});
		menuRoot.add(menu);

		menuRoot = new JMenu("设置更新");
		menuRoot.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/menubar/设置更新.png")));
		menuRoot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

			}
		});
		rootMenu.add(menuRoot);

		menu = new JMenuItem("更新通知");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/config/更新通知.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				JSONObject result;
				try {
					result = TableInput.showSelector(null, getDBUpdateStates());
					if (result == null)
						return;
					setMenuDBUpdateState(result.getString(NAME_KEY), GlobalInstance.instance().getCurrentProjectName(), 1);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}

			}
		});
		menuRoot.add(menu);
		
		menu = new JMenuItem("新增更新项目");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/config/新增更新项目.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				try {
					JSONObject data = getDBUpdateStates();
					HashMap<String, String> exists = new HashMap<>();
					for (Object obj : data.getJSONArray(TableInput.JSON_DATA_KEY)) {
						JSONObject row = (JSONObject) obj;
						exists.put(row.getString(NAME_KEY).toLowerCase().trim(), "");
					}

					TableInput.showEditor(null, null, new TableInput.EditRowAdapter() {
						@Override
						public Object[] addRow(JTable table) {
							String name = MsgHelper.showInputDialog("请输入新项目功能名称");
							if (name == null || name.isEmpty())
								return null;
							else {
								try {
									setDBUpdateState(name, GlobalInstance.instance().getCurrentProjectName(), 0);
								} catch (Exception e) {
									e.printStackTrace();
									MsgHelper.showException(e);
									return null;
								}
								return new Object[] { name, 0 };
							}
						}

						@Override
						public boolean allowDelete(Vector<?> row) {
							try {
								removeDBUpdateState((String) row.get(0), GlobalInstance.instance().getCurrentProjectName());
								return true;
							} catch (Exception e) {
								e.printStackTrace();
								MsgHelper.showException(e);
								return false;
							}
						}

					}, data);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		menuRoot.add(menu);

		JSeparator separator_9 = new JSeparator();
		menuRoot.add(separator_9);

		menu = new JMenuItem("移除通知");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/config/移除通知.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				JSONObject result;
				try {
					result = TableInput.showSelector(null, getDBUpdateStates());
					if (result == null)
						return;
					setMenuDBUpdateState(result.getString(NAME_KEY), GlobalInstance.instance().getCurrentProjectName(), 0);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		menuRoot.add(menu);

	}

	ExecutorService thread = Executors.newFixedThreadPool(1);
	
	@Override
	public void reset() {
		if (isReset.get()) {
			MsgHelper.showMessage(null, "正在重新初始化，请稍后再试！");
			return;					
		}
		
		isReset.set(true);

		thread.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					Roler.reset(GlobalInstance.instance().getMainControl().getDB());
					CustomDataRoleManager.reset();
					GroupCustomDataRoleManager.reset();
				} finally {
					isReset.set(false);
				}
			}
		});
		
	}

	@Override
	public int getLoadOrder() {
		return 0;
	}

	@Override
	public RoleInfos selectRoles(RoleInfos initRoles) {
		if (isReset.get()) {
			MsgHelper.showMessage(null, "正在重新初始化，请稍后再试！");
			return new RoleInfos();					
		}
		return RoleSelectDialog.showDialog(GlobalInstance.instance().getMainControl().getDB(), initRoles);
	}

	@Override
	public GroupInfo getGroupInfo(String group) {
		if (isReset.get()) {
			MsgHelper.showMessage(null, "正在重新初始化，请稍后再试！");
			return null;					
		}
		
		return Roler.instance().getGroups().getGroup(group);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void traverseGroups(ITraverse<GroupInfo> onTraverse) {
		if (isReset.get()) {
			MsgHelper.showMessage(null, "正在重新初始化，请稍后再试！");
			return;					
		}
		
		((IInfos<GroupInfo>) Roler.instance().getGroups()).traverse(onTraverse);
	}

	@Override
	public void initGroup(String[] typenames) throws Exception {
		if (isReset.get()) {
			MsgHelper.showMessage(null, "正在重新初始化，请稍后再试！");
			return;					
		}
		
		Roler.instance().onlyInitGroup(typenames, true);
	}

	@Override
	public PlugInType getType() {
		return PlugInType.ptRole;
	}

	@Override
	protected String getMenuRootName() {
		return "数据配置";
	}
}
