package com.wh.gaea.plugin;

import java.awt.Component;

import javax.swing.JMenu;

public abstract class BaseGaeaPlugin implements IGaeaPlugin {
	protected JMenu rootMenu;

	protected abstract String getMenuRootName();

	@Override
	public JMenu getRootMenu(JMenu root) {
		if (rootMenu != null)
			return rootMenu;

		for (int i = 0; i < root.getMenuComponentCount(); i++) {
			Component component = root.getMenuComponent(i);
			String title = null;
			if (component instanceof JMenu) {
				title = ((JMenu) component).getText();
			} else {
				continue;
			}
			if (title != null && title.equalsIgnoreCase(getMenuRootName())) {
				rootMenu = (JMenu) component;
				return rootMenu;
			}
		}

		rootMenu = new JMenu(getMenuRootName());
		root.add(rootMenu);
		return rootMenu;
	}

	@Override
	public int getLoadOrder() {
		return 1;
	}

}
