package com.wh.gaea.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.control.KeyValue;
import com.wh.gaea.interfaces.selector.IWorkflowSelector;
import com.wh.gaea.plugin.workflow.RunFlowDecideSelectDialog;
import com.wh.gaea.plugin.workflow.RunFlowSelectDialog;
import com.wh.swing.tools.MsgHelper;

public class GaeaWorkflowPlugin extends BaseGaeaPlugin implements IGaeaPlugin, IWorkflowSelector {

	@Override
	public void setMenu(JMenu root) {
		getRootMenu(root);
		rootMenu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/menubar/工作流.png")));

		JMenuItem menu = new JMenuItem("运行流程设计");
		menu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK));
		menu.setIcon(new ImageIcon(BaseGaeaPlugin.class.getResource("/image/menu/config/运行流程设计.png")));
		menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!GlobalInstance.instance().isOpenProject()) {
					MsgHelper.showMessage(null, "请先打开一个项目！");
					return;
				}

				try {
					RunFlowResult runFlowResult = RunFlowSelectDialog.show(GlobalInstance.instance().getMainControl());
					KeyValue<String, String> result = runFlowResult.isok ? runFlowResult.runFlowInfo : null;
					if (result == null)
						return;

					GlobalInstance.instance().getMainControl().openRunWorkflow(GlobalInstance.instance().getRunFlowFile(result.value));
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
				}
			}
		});

		rootMenu.add(menu);
	}

	@Override
	public void reset() {
	}

	@Override
	public int getLoadOrder() {
		return 0;
	}

	@Override
	public String selectDecideValue(String decide) {
		return RunFlowDecideSelectDialog.showDialog(GlobalInstance.instance().getMainControl(), decide);
	}

	@Override
	public RunFlowResult selectRunFlowInfo() throws Exception {
		return RunFlowSelectDialog.show(GlobalInstance.instance().getMainControl());
	}

	@Override
	public PlugInType getType() {
		return PlugInType.ptWorkflow;
	}

	@Override
	protected String getMenuRootName() {
		return "工作流";
	}

}
