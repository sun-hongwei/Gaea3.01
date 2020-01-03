package com.wh.gaea.plugin.role;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.role.RoleInfo;
import com.wh.gaea.role.RoleInfos;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.checkboxnode.CheckBoxNode;
import com.wh.swing.tools.checkboxnode.CheckBoxNode.ISelection;
import com.wh.swing.tools.checkboxnode.CheckBoxNodeConfig;
import com.wh.swing.tools.tree.TreeHelp;
import com.wh.swing.tools.tree.TreeHelp.INewNode;
import com.wh.swing.tools.tree.TreeHelp.ITraverseTree;
import com.wh.swing.tools.tree.TreeHelp.TreeItemInfo;

import wh.interfaces.IDBConnection;
import wh.role.interfaces.IInfos;
import wh.role.obj.GroupInfo;
import wh.role.obj.RoleServiceObject.ITraverse;

public class RoleSelectDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();

	IDBConnection db;

	boolean isok = false;
	
	protected List<TreeItemInfo> getSelectRoles() {
		
		List<TreeItemInfo> result = new ArrayList<>();
		TreeHelp.traverseTree(roleTree, new ITraverseTree<CheckBoxNode>() {

			@Override
			public boolean onNode(CheckBoxNode t) {
				if (!t.isSelected())
					return true;
				
				TreeItemInfo tmp = (TreeItemInfo) t.getUserObject();
				result.add(tmp);
				return true;
			}
		});
		
		return result;
	}

	protected void setSelectRoles(RoleInfos infos) {
		if (infos == null || infos.size() == 0)
			return;
		
		HashMap<String, RoleInfo> infoMap = new HashMap<>();
		for (RoleInfo roleInfo : infos) {
			infoMap.put(roleInfo.id, roleInfo);
		}
		
		TreeHelp.traverseTree(roleTree, new ITraverseTree<CheckBoxNode>() {

			@Override
			public boolean onNode(CheckBoxNode t) {
				TreeItemInfo tmp = (TreeItemInfo) t.getUserObject();
				t.setSelected(infoMap.containsKey(tmp.data.get("id")));
				return true;
			}
		});
	}

	@SuppressWarnings("unchecked")
	public void init() {
		try {
			JSONArray datas = new JSONArray();
			((IInfos<GroupInfo>)Roler.instance().getGroups()).traverse(new ITraverse<GroupInfo>() {

				@Override
				public void callback(GroupInfo t) {
					JSONObject value = new JSONObject();
					value.put("id", t.groupid);
					value.put("pid", t.grouppid);
					value.put("text", t.groupname);
					datas.put(value);
				}
			});

			roleTree.setModel(new DefaultTreeModel(null));
			TreeHelp.jsonToTree(roleTree, datas, "id", "text", "pid", new INewNode() {

				@Override
				public DefaultMutableTreeNode newNode() {
					return new CheckBoxNode();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JTree roleTree;
	
	public RoleSelectDialog(IDBConnection db) {
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		this.db = db;

		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setTitle("权限选择");
		setIconImage(Toolkit.getDefaultToolkit().getImage(RoleConfigDialog.class.getResource("/image/browser.png")));
		setBounds(100, 100, 768, 612);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		
		JToolBar toolBar = new JToolBar();
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		contentPanel.add(toolBar, BorderLayout.NORTH);
		JButton button_11 = new JButton("全选");
		button_11.addActionListener(new ActionListener() {
			boolean isSelected = false;
			public void actionPerformed(ActionEvent e) {	
				isSelected = !isSelected;
				TreeHelp.traverseTree(roleTree, new ITraverseTree<CheckBoxNode>() {

					@Override
					public boolean onNode(CheckBoxNode t) {
						t.setSingleSelected(isSelected);
						return true;
					}
				});
				
				roleTree.updateUI();
			}
		});
		button_11.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_11);
		
		toolBar.addSeparator();
		
		JButton button_12 = new JButton("折叠");		
		button_12.addActionListener(new ActionListener() {
			boolean isExpand = true;
			public void actionPerformed(ActionEvent e) {
				isExpand = !isExpand;
				
				TreeHelp.expandOrCollapse(roleTree, (DefaultMutableTreeNode)null, isExpand);

			}
		});
		
		button_12.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_12);
		
		toolBar.addSeparator();
		
		JButton button = new JButton("确定");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (getSelectRoles().size() > 0){
					isok = true;
					setVisible(false);
				} else
					MsgHelper.showMessage("请至少选择一个权限！");
			}
		});
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button);
		
		JButton button_1 = new JButton("取消");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_1);
		
		JScrollPane scrollPane = new JScrollPane();
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		
		roleTree = new JTree();
		roleTree.setShowsRootHandles(true);
		roleTree.setScrollsOnExpand(false);
		roleTree.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		roleTree.setExpandsSelectedPaths(false);
		CheckBoxNodeConfig.config(roleTree, new ISelection() {
			@Override
			public void onSelected(CheckBoxNode selectNode) {
				if (roleTree.getSelectionPath() == null || roleTree.getSelectionPath().getLastPathComponent() == null)
					return;
			}
		});

		scrollPane.setViewportView(roleTree);
		
		init();
		setLocationRelativeTo(null);
	}

	public static RoleInfos showDialog(IDBConnection db, RoleInfos selects) {
		RoleSelectDialog dialog = new RoleSelectDialog(db);
		dialog.setModal(true);
		dialog.setSelectRoles(selects);
		dialog.setVisible(true);
		
		if (dialog.isok){
			RoleInfos result = new RoleInfos();
			for (TreeItemInfo info : dialog.getSelectRoles()) {
				RoleInfo roleInfo = new RoleInfo();
				roleInfo.id = info.data.getString("id");
				roleInfo.name = info.data.getString("text");
				result.add(roleInfo);
			};
			return result;
		}else{
			return null;
		}
	}
}
