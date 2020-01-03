package com.wh.gaea.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.datasource.DataSource;
import com.wh.gaea.interfaces.ShowType;
import com.wh.gaea.interfaces.selector.IDataSourceSelector;
import com.wh.gaea.plugin.datasource.dialog.DataSourceSelector;
import com.wh.gaea.plugin.datasource.dialog.FileDataSourceConfig;
import com.wh.gaea.plugin.datasource.dialog.LocalDataSourceConfig;
import com.wh.gaea.plugin.datasource.dialog.SqlDataSourceConfig;
import com.wh.gaea.plugin.datasource.dialog.UrlDataSourceConfig;
import com.wh.gaea.plugin.datasource.runner.SqlDataSourceRunner;
import com.wh.swing.tools.MsgHelper;

public class GaeaDataSourcePlugin extends BaseGaeaPlugin implements IGaeaPlugin, IDataSourceSelector {

	@Override
	public void setMenu(JMenu root) {
		getRootMenu(root);
		rootMenu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/menubar/数据源.png")));
		
		JMenuItem menu = new JMenuItem("本地数据源配置");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/datasource/本地数据源配置.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				LocalDataSourceConfig.show(GlobalInstance.instance().getMainControl());
			}
		});
		rootMenu.add(menu);

		JSeparator separator_34 = new JSeparator();
		rootMenu.add(separator_34);

		menu = new JMenuItem("远程数据源设置              ");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/datasource/远程数据源配置.png")));
		menu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_MASK));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				UrlDataSourceConfig.show(GlobalInstance.instance().getMainControl());
			}
		});
		rootMenu.add(menu);

		menu = new JMenuItem("SQL数据源设置");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/datasource/SQL数据源配置.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				SqlDataSourceConfig.show(GlobalInstance.instance().getMainControl());
			}
		});
		rootMenu.add(menu);

		menu = new JMenuItem("文件数据源设置");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/datasource/文件数据源配置.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				FileDataSourceConfig.show(GlobalInstance.instance().getMainControl());
			}
		});
		rootMenu.add(menu);

	}

	@Override
	public void reset() {

	}

	@Override
	public int getLoadOrder() {
		return 1;
	}

	@Override
	public void showLocalView() {
		LocalDataSourceConfig.show(GlobalInstance.instance().getMainControl());
	}

	@Override
	public void showSQLView() {
		SqlDataSourceConfig.show(GlobalInstance.instance().getMainControl());
	}

	@Override
	public void showFileView() {
		FileDataSourceConfig.show(GlobalInstance.instance().getMainControl());
	}

	@Override
	public void showUrlView() {
		UrlDataSourceConfig.show(GlobalInstance.instance().getMainControl());
	}

	@Override
	public DataSource dataSourceSelector(ShowType st) {
		return DataSourceSelector.show(st, GlobalInstance.instance().getMainControl());
	}

	@Override
	public Map<String, String> parseSQL(String sql) {
		return SqlDataSourceRunner.Parser.parse(sql);
	}

	@Override
	public PlugInType getType() {
		return PlugInType.ptDataSource;
	}

	@Override
	protected String getMenuRootName() {
		return "数据源";
	}

}
