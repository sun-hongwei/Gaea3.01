package com.wh.gaea.plugin.role;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.draw.IUINode;
import com.wh.gaea.editor.ControlSelectDialog;
import com.wh.gaea.editor.JsonTreeDataEditor;
import com.wh.gaea.editor.JsonTreeDataEditor.IChange;
import com.wh.gaea.interfaces.IDrawInfoDefines;
import com.wh.gaea.interfaces.IDrawNode;
import com.wh.gaea.interfaces.IEditorEnvironment;
import com.wh.gaea.interfaces.IEditorInterface.ITraverseDrawNode;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.SwingTools;
import com.wh.tools.JsonHelp;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.Operation;
import wh.interfaces.ISqlBuilder.SqlType;

public class RoleInitDialog extends JDialog {

	boolean isEdit = false;
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();

	JsonTreeDataEditor jsonTreeDataEditor = new JsonTreeDataEditor(new IChange() {

		@Override
		public void onChange(Object data) {
			isEdit = true;
		}
	});

	void docancel() {
		if (isEdit) {
			if (MsgHelper.showConfirmDialog("关闭将丢失所有未保存的工作，是否继续？", "退出",
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
				return;
		}
		setVisible(false);
	}

	public RoleInitDialog(IMainControl mainControl) {
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setIconImage(Toolkit.getDefaultToolkit().getImage(RoleInitDialog.class.getResource("/image/browser.png")));
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				docancel();
			}
		});
		setBounds(100, 100, 1117, 790);
		setContentPane(contentPanel);
		contentPanel.setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.add(jsonTreeDataEditor, BorderLayout.CENTER);
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		contentPanel.add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("确定");
		okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JSONArray data = jsonTreeDataEditor.getTableData();
					save(data, rt, db);
					setVisible(false);
				} catch (Exception ex) {
					ex.printStackTrace();
					MsgHelper.showMessage(null, "保存数据到数据库失败：" + SwingTools.getExceptionMsg(ex) + "，请检查输入及数据库连接！", "保存",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		uiButton = new JButton("查看界面");
		uiButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		uiButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jsonTreeDataEditor.table.getSelectedRow() == -1) {
					MsgHelper.showMessage(null, "请先在表格中选择一个条目！", "查看界面", JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				String tmp = (String) jsonTreeDataEditor.table.getValueAt(jsonTreeDataEditor.table.getSelectedRow(), 0);
				if (tmp == null || tmp.isEmpty()) {
					MsgHelper.showMessage(null, "id未设置或者无效！！", "查看界面", JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				if (rt == RoleType.rtButton) {
					String[] ids = tmp.split("\\.");
					if (ids.length < 2) {
						MsgHelper.showMessage(null, "id格式错误！", "查看界面", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					String nodeid = ids[0];
					String controlid = ids[1];
					String uiid = GlobalInstance.instance().getUIID(nodeid);
					ControlSelectDialog.showDialog(uiid, controlid, false);
				} else if (rt == RoleType.rtView) {
					String uiid = GlobalInstance.instance().getUIID(tmp);
					ControlSelectDialog.showDialog(uiid, false);
				}
			}
		});

		JButton button = new JButton("重新生成数据");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JSONArray data = getRoleFileInfo(rt);
					jsonTreeDataEditor.setValue(data);
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}

			}
		});

		JButton button_1 = new JButton("导入");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					importFromFile();
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		buttonPane.add(button_1);

		JButton button_2 = new JButton("导出");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportToFile(rt, db);
			}
		});
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		buttonPane.add(button_2);
		buttonPane.add(button);
		buttonPane.add(uiButton);

		JButton repairButton = new JButton("修正");
		repairButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (jsonTreeDataEditor.table.getSelectedRow() == -1) {
					MsgHelper.showMessage(null, "请先在表格中选择一个条目！", "查看界面", JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				String nodeid = (String) jsonTreeDataEditor.table.getValueAt(jsonTreeDataEditor.table.getSelectedRow(),
						0);
				if (nodeid == null || nodeid.isEmpty()) {
					MsgHelper.showMessage(null, "id未设置或者无效！！", "查看界面", JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				if (rt == RoleType.rtButton) {
					String[] ids = nodeid.split("\\.");
					if (ids.length < 2) {
						MsgHelper.showMessage(null, "id格式错误！", "查看界面", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					nodeid = ids[0];
					String controlid = ids[1];
					String uiid = GlobalInstance.instance().getUIID(nodeid);
					try {
						mainControl.openUIBuilder(uiid, controlid);
					} catch (Exception e1) {
						e1.printStackTrace();
						MsgHelper.showException(e1);
					}
				} else if (rt == RoleType.rtView) {
					try {
						File file = GlobalInstance.instance().getModelRelationFileFromNodeID(nodeid);
						mainControl.openModelflowRelation(GlobalInstance.instance().getModelRelationName(file), nodeid);
					} catch (Exception e1) {
						e1.printStackTrace();
						MsgHelper.showException(e1);
					}
				}

			}
		});
		repairButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		buttonPane.add(repairButton);
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		JButton cancelButton = new JButton("取消");
		cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				docancel();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);

		jsonTreeDataEditor.setMustRoot(false);
		setLocationRelativeTo(null);
	}

	public enum RoleType {
		rtData, rtView, rtFunc, rtButton, rtMenu, rtTree
	}

	public static JSONArray getRoleFileInfo(RoleType rt) throws Exception {
		JSONArray json = new JSONArray();
		switch (rt) {
		case rtButton:
		case rtView: {
			GlobalInstance.instance().traverseModelAndNavs(new ITraverseDrawNode() {

				@Override
				public boolean onNode(File file, String title, IDrawNode workflowNode, Object param) {
					try {
						if (GlobalInstance.instance().isModelStartNode(workflowNode)
								|| GlobalInstance.instance().isModelEndNode(workflowNode))
							return true;

						if (rt == RoleType.rtButton) {
							File uiFile = GlobalInstance.instance().getUIFile(workflowNode.getId(), false);
							GlobalInstance.instance().traverseUI(uiFile, new ITraverseDrawNode() {

								@Override
								public boolean onNode(File uiFile, String title, IDrawNode controlNode, Object param) {
									IUINode uiNode = (IUINode) controlNode;
									boolean b = GlobalInstance.instance().isUIDrawInfo(uiNode, IDrawInfoDefines.Button_Name);
									if (!b) {
										if (GlobalInstance.instance().isUIDrawInfo(uiNode, IDrawInfoDefines.Label_Name)
												|| GlobalInstance.instance().isUIDrawInfo(uiNode, IDrawInfoDefines.Image_Name)) {
											b = uiNode.getDrawInfo().isHref;
										}
									}

									if (b) {
										try {
											JSONObject buttonInfo = new JSONObject();

											String text = uiNode.getDrawInfo().value != null
													? uiNode.getDrawInfo().value.toString()
													: null;
											if (text.isEmpty()) {
												text = uiNode.getDrawInfo().title;
											}
											if (text.isEmpty())
												text = uiNode.getDrawInfo().name;

											buttonInfo.put("text", text);
											buttonInfo.put("id",
													workflowNode.getId() + "." + uiNode.getDrawInfo().name);
											buttonInfo.put("pid", workflowNode.getTitle());
											json.put(buttonInfo);
										} catch (JSONException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									return true;
								}
							}, null);
						} else {
							JSONObject value = new JSONObject();
							value.put("text", workflowNode.getTitle());
							value.put("id", workflowNode.getId());
							json.put(value);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return true;
				}
			}, null);
			break;
		}
		case rtData:
			break;
		case rtMenu: {
			File file = GlobalInstance.instance().getProjectFile(IEditorEnvironment.Menu_Dir_Path,
					GlobalInstance.instance().getMenu_FileName(IEditorEnvironment.Main_Menu_FileName));
			if (!file.exists())
				return null;

			JSONArray jsonObject = (JSONArray) JsonHelp.parseCacheJson(file, null);
			return jsonObject;
		}
		case rtTree: {
			File file = GlobalInstance.instance().getMainNavTreeFile();
			if (!file.exists())
				return null;

			JSONArray jsonObject = (JSONArray) JsonHelp.parseCacheJson(file, null);
			return jsonObject;
		}
		default:
			return null;
		}

		return json;
	}

	protected static String getOperation(RoleType rt) {
		String typename = null;
		switch (rt) {
		case rtButton:
			typename = "button";
			break;
		case rtData:
			typename = "data";
			break;
		case rtFunc:
			typename = "func";
			break;
		case rtMenu:
			typename = "menu";
			break;
		case rtTree:
			typename = "tree";
			break;
		case rtView:
			typename = "view";
			break;
		}
		return typename;
	}

	protected static IDataset getRoleDataset(RoleType rt, IDBConnection db) {
		try {
			String typename = getOperation(rt);
			ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
			sqlBuilder.addField("*");
			sqlBuilder.addTable("workflow_role");
			sqlBuilder.addWhere("roletype", Operation.otEqual, new Object[] { typename });
			sqlBuilder.setSqlType(SqlType.stQuery);
			IDataset result = db.query(sqlBuilder);
			return result;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static JSONArray getRoleDBInfo(RoleType rt, IDBConnection db) {

		IDataset dataset = getRoleDataset(rt, db);
		if (dataset == null || dataset.getRowCount() == 0) {
			if (dataset == null)
				MsgHelper.showMessage(null, "打开数据集合失败，请检查数据库连接是否正常！", "装载", JOptionPane.ERROR_MESSAGE);
			return null;
		}
		JSONArray json = new JSONArray();
		for (IRow row : dataset.getRows()) {
			try {
				JSONObject value = new JSONObject();
				value.put("id", row.getValue("roleid").toString());
				if (row.getValue("rolepid") != null)
					value.put("pid", row.getValue("rolepid").toString());
				value.put("text", row.getValue("roletext").toString());
				if (row.getValue("rolememo") != null)
					value.put("memo", row.getValue("rolememo").toString());
				if (row.getValue("jumpid") != null)
					value.put("jumpid", row.getValue("jumpid").toString());
				json.put(value);
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}

		return json;
	}

	public void save(JSONArray data, RoleType rt, IDBConnection db) throws Exception {
		for (int i = 0; i < data.length(); i++) {
			JSONObject row = data.getJSONObject(i);
			if (!row.has("id") || JsonHelp.isEmpty(row, "id")) {
				jsonTreeDataEditor.select(i);
				throw new Exception("当前 数据行未设置【id】字段值");
			}
			if (!row.has("text") || JsonHelp.isEmpty(row, "text")) {
				jsonTreeDataEditor.select(i);
				throw new Exception("当前 数据行未设置【text】字段值");
			}
		}

		String typename = getOperation(rt);
		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("*");
		sqlBuilder.addTable("workflow_role");
		sqlBuilder.addWhere("1", Operation.otEqual, new Object[] { 2 });
		sqlBuilder.setSqlType(SqlType.stQuery);
		IDataset dataset = db.query(sqlBuilder);

		if (dataset == null) {
			MsgHelper.showMessage(null, "打开数据集合失败，请检查数据库连接是否正常！", "保存", JOptionPane.ERROR_MESSAGE);
			return;
		}

		HashMap<String, IRow> rowMap = new HashMap<>();
		for (int i = 0; i < data.length(); i++) {
			JSONObject value = data.getJSONObject(i);
			if (rt == RoleType.rtButton && (!value.has("pid") || value.getString("pid").isEmpty()))
				continue;
			String id = value.getString("id");
			if (rowMap.containsKey(id)) {
				jsonTreeDataEditor.select(id);
				throw new Exception("id[" + id + "] 已经存在，请使用[修正]功能修改后再试！");
			}
			IRow row = dataset.newRow();
			row.setValue("roleid", id);
			row.setValue("roletext", value.getString("text"));
			if (value.has("pid"))
				row.setValue("rolepid", value.getString("pid"));
			if (value.has("memo"))
				row.setValue("rolememo", value.getString("memo"));
			row.setValue("roletype", typename);
			rowMap.put(id, row);
			dataset.addRow(row);
		}

		db.beginTran();
		try {
			ISqlBuilder delBuilder = IDBConnection.getSqlBuilder(db);
			delBuilder.addTable("workflow_role");
			delBuilder.addWhere("roletype", Operation.otEqual, new Object[] { typename });
			delBuilder.setSqlType(SqlType.stDelete);
			db.execute(delBuilder);
			dataset.post(db);
			db.commitTran();
		} catch (Exception e) {
			db.rollbackTran();
			throw e;
		}
	}

	public void exportToFile(RoleType rt, IDBConnection db) {
		try {
			exportToFile(jsonTreeDataEditor.getResult(), rt, db);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}

	public void exportToFile(JSONArray data, RoleType rt, IDBConnection db) throws Exception {
		if (data.length() == 0) {
			MsgHelper.showMessage("数据集为空！");
			return;
		}

		File file = SwingTools.selectSaveFile(null, GlobalInstance.instance().getProjectBasePath().getAbsolutePath(),
				"export_role_" + getOperation(rt) + ".rit", "权限导出文件(*.rit)=rit");

		if (file == null)
			return;

		JSONObject saveData = new JSONObject();
		saveData.put("typename", rt.name());
		saveData.put("data", data);

		JsonHelp.saveJson(file, saveData, null);

		MsgHelper.showMessage("恭喜，导出成功！");
	}

	public void importFromFile() throws Exception {
		File file = SwingTools.selectOpenFile(null, GlobalInstance.instance().getProjectBasePath().getAbsolutePath(),
				"export_role_" + getOperation(rt) + ".rit", "权限导出文件(*.rit)=rit");

		if (file == null)
			return;

		JSONObject data = (JSONObject) JsonHelp.parseCacheJson(file, null);

		if (!data.has("typename")) {
			throw new Exception("导出文件格式不正确！");
		}

		if (!data.has("data")) {
			throw new Exception("导出文件格式不正确！");
		}

		JSONArray all = jsonTreeDataEditor.getResult();
		HashMap<String, JSONObject> allMap = new HashMap<>();
		for (Object object : all) {
			JSONObject row = (JSONObject) object;
			allMap.put(JsonHelp.getString(row, "id"), row);
		}
		for (Object object : data.getJSONArray("data")) {
			JSONObject row = (JSONObject) object;
			String id = JsonHelp.getString(row, "id");
			if (allMap.containsKey(id))
				continue;

			allMap.put(id, row);
		}
		jsonTreeDataEditor.setValue(new JSONArray(allMap.values()));
		MsgHelper.showMessage("恭喜， 导入成功！");
	}

	RoleType rt;
	IDBConnection db;
	private JButton uiButton;

	protected void setRoleType(RoleType rt, IDBConnection db) {
		this.rt = rt;
		this.db = db;
		uiButton.setVisible(rt == RoleType.rtView || rt == RoleType.rtButton);
		try {

			JSONArray result = getRoleDBInfo(rt, db);
			if (result == null)
				result = getRoleFileInfo(rt);
			if (result != null) {
				jsonTreeDataEditor.setValue(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public static void showDialog(RoleType rt, IMainControl mainControl) {
		IDBConnection db = mainControl.getDB();
		if (db == null || !db.isOpen()) {
			MsgHelper.showMessage("请先连接数据库！");
			return;
		}

		RoleInitDialog dialog = new RoleInitDialog(mainControl);
		dialog.setRoleType(rt, mainControl.getDB());
		dialog.setModal(true);
		dialog.setVisible(true);
		dialog.dispose();
	}
}
