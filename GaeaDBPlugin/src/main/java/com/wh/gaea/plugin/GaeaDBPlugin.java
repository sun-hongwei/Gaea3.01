package com.wh.gaea.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.interfaces.IEditorEnvironment;
import com.wh.gaea.plugin.db.DBConnectionStringCreator;
import com.wh.swing.tools.MsgHelper;

import wh.interfaces.IDBConnection;

public class GaeaDBPlugin extends BaseGaeaPlugin implements IGaeaDBPlugin {

	@Override
	public void setMenu(JMenu root) {
		JMenu menuRoot = getRootMenu(root);

		rootMenu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/menubar/数据配置.png")));

		JMenuItem menu = new JMenuItem("连接数据库");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/config/连接数据库.png")));
		menuRoot.add(menu);
		menu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}
				if (!DBConnectionStringCreator.showDialog(GlobalInstance.instance().getProjectFile(
						IEditorEnvironment.Config_Dir_Path, IEditorEnvironment.Main_DB_Config_FileName)))
					return;

				IDBConnection db = DBConnectionStringCreator.getDBConnection(
						GlobalInstance.instance().getProjectFile(IEditorEnvironment.Config_Dir_Path,
								IEditorEnvironment.Main_DB_Config_FileName));

				if (db != null) {
					GlobalInstance.instance().getMainControl().setDB(db);
					GlobalInstance.instance().reset();
				}
			}
		});
		
	}

	@Override
	public void reset() {
	}

	@Override
	public int getLoadOrder() {
		return 0;
	}

	@Override
	public PlugInType getType() {
		return PlugInType.ptDb;
	}

	@Override
	public IDBConnection getDB() {
		return DBConnectionStringCreator.getDBConnection(
				GlobalInstance.instance().getProjectFile(IEditorEnvironment.Config_Dir_Path,
						IEditorEnvironment.Main_DB_Config_FileName));
	}

	@Override
	protected String getMenuRootName() {
		return "数据配置";
	}

}
