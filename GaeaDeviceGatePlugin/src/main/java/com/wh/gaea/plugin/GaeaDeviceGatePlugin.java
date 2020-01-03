package com.wh.gaea.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.plugin.emwx.dialog.DeviceConfigDialog;
import com.wh.swing.tools.MsgHelper;

public class GaeaDeviceGatePlugin extends BaseGaeaPlugin implements IGaeaPlugin {

	@Override
	public void setMenu(JMenu root) {
		getRootMenu(root);
		rootMenu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/menubar/设备接入.png")));
		
		JMenuItem menu = new JMenuItem("设备配置文件生成助手");
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/plugin/设备配置文件生成助手.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				DeviceConfigDialog.show(GlobalInstance.instance().getMainControl());
			}
		});
		rootMenu.add(menu);

	}

	@Override
	public void reset() {

	}

	@Override
	public int getLoadOrder() {
		return 2;
	}

	@Override
	public PlugInType getType() {
		return PlugInType.ptDb;
	}

	@Override
	protected String getMenuRootName() {
		return "设备接入";
	}

}
