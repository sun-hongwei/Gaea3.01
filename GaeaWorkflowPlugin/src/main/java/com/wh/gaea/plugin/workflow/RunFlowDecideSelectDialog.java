package com.wh.gaea.plugin.workflow;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.control.ControlSearchHelp;
import com.wh.gaea.interfaces.ChangeType;
import com.wh.gaea.interfaces.Config;
import com.wh.gaea.interfaces.IDrawCanvas;
import com.wh.gaea.interfaces.IDrawNode;
import com.wh.gaea.interfaces.IDrawPageConfig;
import com.wh.gaea.interfaces.IInitPage;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.gaea.interfaces.INode;
import com.wh.gaea.interfaces.IOnPageSizeChanged;
import com.wh.gaea.interfaces.IScroll;
import com.wh.gaea.interfaces.selector.IWorkflowSelector.RunFlowInfo;
import com.wh.gaea.plugin.role.RoleSelectDialog;
import com.wh.gaea.plugin.role.Roler;
import com.wh.gaea.role.RoleInfo;
import com.wh.gaea.role.RoleInfos;
import com.wh.swing.tools.MsgHelper;

import wh.role.interfaces.IInfos;
import wh.role.obj.GroupInfo;
import wh.role.obj.RoleServiceObject.ITraverse;

public class RunFlowDecideSelectDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	HashMap<String, String> names = new HashMap<>();

	IDrawCanvas canvas = GlobalInstance.instance().createFlowCanvas();
	private JComboBox<RunFlowInfo> runFlowSelector;
	private JScrollBar hScrollBar;
	private JScrollBar vScrollBar;

	IDrawNode workflowNode;
	HashMap<String, IDrawNode> workflowNodes;
	String workflowRelationTitle;

	IMainControl mainControl;

	JSONObject data = new JSONObject();

	public void loadDecideValue(RoleInfo info) {
		if (info == null)
			return;

		if (data.has(info.id)) {
			IDrawNode node = (IDrawNode) canvas.getNode(data.getString(info.id));
			canvas.setSelected(node);
		} else
			canvas.clearSelect();
		canvas.repaint();
	}

	public void saveDecideValue(RoleInfo info, IDrawNode node) {
		if (node == null || info == null) {
			MsgHelper.showException(new Exception("权限信息及归属的状态信息不能为空！"));
			return;
		}
		data.put(info.id, node.getId());
	}

	public JSONObject getResult() throws Exception {
		JSONObject result = new JSONObject();
		for (RoleInfo info : new RoleInfos((DefaultListModel<RoleInfo>) roleList.getModel())) {
			if (!data.has(info.id)) {
				roleList.setSelectedValue(info, true);
				throw new Exception("权限[" + info.name + "]没有设置状态");
			}

			IDrawNode node = (IDrawNode) canvas.getNode(data.getString(info.id));
			result.put(info.id, GlobalInstance.instance().getWorkflowState(node));
		}

		return result;
	}

	public void load(JSONObject decideValue) {
		if (decideValue == null) {
			roleList.setModel(new DefaultListModel<>());
			return;
		}

		data = new JSONObject();
		roleList.setModel(new DefaultListModel<>());

		JSONArray roleIds = decideValue.names();
		if (roleIds == null)
			return;

		initRoles(roleIds);
		HashMap<String, IDrawNode> states = new HashMap<>();
		for (IDrawNode node : canvas.getNodes()) {
			if (node instanceof IDrawNode) {
				IDrawNode stateNode = (IDrawNode) node;
				if (GlobalInstance.instance().getWorkflowState(node) != null)
					states.put(GlobalInstance.instance().getWorkflowState(node), stateNode);
			}
		}

		for (int i = 0; i < roleIds.length(); i++) {
			String roleid = roleIds.getString(i);
			String state = decideValue.getString(roleid);
			if (states.containsKey(state)) {
				data.put(roleid, states.get(state).getId());
			} else {
				DefaultListModel<RoleInfo> model = (DefaultListModel<RoleInfo>) roleList.getModel();
				for (int j = 0; j < model.getSize(); j++) {
					if (model.getElementAt(j).id.compareTo(roleid) == 0) {
						model.remove(j);
						break;
					}
				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	public void initRoles(JSONArray roleIds) {

		if (roleIds == null) {
			roleList.setModel(new DefaultListModel<>());
			return;
		}

		HashMap<String, RoleInfo> roleMap = new HashMap<>();
		for (int i = 0; i < roleIds.length(); i++) {
			roleMap.put(roleIds.getString(i), null);
		}

		RoleInfos roleInfos = new RoleInfos();
		try {

			((IInfos<GroupInfo>) Roler.instance().getGroups()).traverse(new ITraverse<GroupInfo>() {

				@Override
				public void callback(GroupInfo t) {
					if (roleMap.containsKey(t.groupid)) {
						roleInfos.add(new RoleInfo(t));
					}
				}
			});

			roleList.setModel(roleInfos.toModel());
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}

	public RunFlowDecideSelectDialog(IMainControl mainControl) throws Exception {
		super();
		setIconImage(Toolkit.getDefaultToolkit()
				.getImage(RunFlowDecideSelectDialog.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		this.mainControl = mainControl;
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 933, 665);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JToolBar toolBar = new JToolBar();
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		contentPane.add(toolBar, BorderLayout.NORTH);

		toolBar.addSeparator(new Dimension(5, 0));

		JLabel label = new JLabel(" 流程：");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(label);

		runFlowSelector = new JComboBox<>();
		runFlowSelector.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		runFlowSelector.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (runFlowSelector.getSelectedItem() != null) {
					RunFlowInfo info = (RunFlowInfo) runFlowSelector.getSelectedItem();
					loadRunFlow(info.flowData);
				}
			}
		});
		runFlowSelector.setMaximumSize(new Dimension(300, 21));
		toolBar.add(runFlowSelector);
		JButton button_2 = new JButton("添加权限");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RoleInfos result = RoleSelectDialog.showDialog(mainControl.getDB(),
						new RoleInfos((DefaultListModel<RoleInfo>) roleList.getModel()));
				if (result != null) {
					roleList.setModel(result.toModel());
				}
			}
		});
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_2);

		JButton button_3 = new JButton("删除权限");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RoleInfo roleInfo = roleList.getSelectedValue();
				if (roleInfo == null)
					return;

				if (MsgHelper.showConfirmDialog("是否删除选定的判定项目？", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;

				if (data.has(roleInfo.id))
					data.remove(roleInfo.id);
				((DefaultListModel<RoleInfo>) roleList.getModel()).removeElement(roleInfo);
			}
		});
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_3);

		toolBar.addSeparator();

		JButton button_4 = new JButton("保存");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!(canvas.getSelected() instanceof IDrawNode)) {
					MsgHelper.showMessage("请先选择一个状态节点！");
					return;
				}
				saveDecideValue(roleList.getSelectedValue(), (IDrawNode) canvas.getSelected());
			}
		});
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		toolBar.add(button_4);

		toolBar.addSeparator();

		JButton btnNull = new JButton("null返回");
		btnNull.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btnNull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runFlowSelector.setSelectedIndex(-1);
				isok = true;
				setVisible(false);
			}
		});
		toolBar.add(btnNull);

		toolBar.addSeparator();

		JButton button = new JButton("确定");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (canvas.getSelected() instanceof IDrawNode) {
						saveDecideValue(roleList.getSelectedValue(), (IDrawNode) canvas.getSelected());
					}

					getResult();
					isok = true;
					setVisible(false);
				} catch (Exception e1) {
					MsgHelper.showException(e1);
				}
			}
		});

		toolBar.add(button);

		JButton button_1 = new JButton("取消");
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isok = false;
				setVisible(false);
			}
		});
		toolBar.add(button_1);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		memo = new JLabel(" ");
		memo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		memo.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(memo);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.2);
		contentPane.add(splitPane, BorderLayout.CENTER);

		splitPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				splitPane.setResizeWeight(splitPane.getResizeWeight());
				splitPane.setDividerLocation(splitPane.getResizeWeight());
			}
		});

		JPanel panel_1 = new JPanel();
		splitPane.setRightComponent(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));
		panel_1.add((Component) canvas, BorderLayout.CENTER);

		canvas.setOnPageSizeChanged(new IOnPageSizeChanged() {

			@Override
			public void onChanged(Point max) {
				hScrollBar.setMaximum(max.x);
				vScrollBar.setMaximum(max.y);
				hScrollBar.setValue(0);
				hScrollBar.setValue(0);
			}
		});

		canvas.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				Adjustable adj;
				if (vScrollBar.getMaximum() > 0) {
					adj = vScrollBar;
				} else
					adj = hScrollBar;

				if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
					int totalScrollAmount = e.getUnitsToScroll() * adj.getUnitIncrement();
					adj.setValue(adj.getValue() + totalScrollAmount);
				}
			}
		});

		hScrollBar = new JScrollBar();
		panel_1.add(hScrollBar, BorderLayout.SOUTH);
		hScrollBar.setUnitIncrement(10);
		hScrollBar.setOrientation(JScrollBar.HORIZONTAL);
		hScrollBar.setMaximum(99999);

		vScrollBar = new JScrollBar();
		panel_1.add(vScrollBar, BorderLayout.EAST);
		vScrollBar.setUnitIncrement(10);
		vScrollBar.setMaximum(99999);
		vScrollBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				canvas.setOffset(new Point(canvas.getOffset().x, -e.getValue()));
			}
		});
		hScrollBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				canvas.setOffset(new Point(-e.getValue(), canvas.getOffset().y));
			}
		});
		canvas.setOnScroll(new IScroll() {

			@Override
			public void onScroll(int x, int y) {
				hScrollBar.setValue(Math.abs(x));
				vScrollBar.setValue(Math.abs(y));
			}
		});

		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);

		roleList = new JList<>();
		roleList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting())
					return;

				if (roleList.getSelectedValue() == null)
					return;

				loadDecideValue(roleList.getSelectedValue());
			}
		});
		scrollPane.setViewportView(roleList);

		setLocationRelativeTo(null);

		init();
	}

	ControlSearchHelp comboBoxHelp;

	public void init() throws Exception {
		Map<String, RunFlowInfo> infos = RunFlowSelectDialog.getRunFlows();
		if (infos.size() == 0)
			return;

		runFlowSelector.setModel(new DefaultComboBoxModel<>(infos.values().toArray(new RunFlowInfo[infos.size()])));

		if (comboBoxHelp != null)
			runFlowSelector.removeKeyListener(comboBoxHelp);

		if (runFlowSelector.getItemCount() > 0) {
			runFlowSelector.setSelectedIndex(0);
			comboBoxHelp = new ControlSearchHelp(runFlowSelector);
		}
	}

	public void loadRunFlow(JSONObject data) {
		try {

			canvas.clear();

			if (data == null) {
				return;
			}

			canvas.loadFromJson(data, GlobalInstance.instance().createNullDeserializable(), new IInitPage() {

				@Override
				public void onPage(IDrawPageConfig pageConfig) {
					canvas.getPageConfig().setPageSizeMode(pageConfig.getCurPageSizeMode(), pageConfig.getWidth(),
							pageConfig.getHeight());
				}
			}, true);
			canvas.getPageConfig().setConfig(new Config[] { Config.ccAllowSelect });
			canvas.setNodeEvent(new INode() {

				@Override
				public void onChange(IDrawNode[] nodes, ChangeType ct) {
					switch (ct) {
					case ctDeselected:
						break;
					case ctSelecteds:
					case ctSelected:
						IDrawNode node = (IDrawNode) canvas.getSelected();
						if (!(node instanceof IDrawNode)) {
							canvas.setSelected(null);
						}
						break;
					default:
						break;
					}
				}

				@Override
				public void DoubleClick(IDrawNode node) {

				}

				@Override
				public void Click(IDrawNode node) {

				}
			});

			memo.setText(canvas.getPageConfig().getMemo());

			canvas.repaint();
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showMessage(this, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	boolean isok = false;
	private JLabel memo;
	private JList<RoleInfo> roleList;

	public interface IAdd {
		public String onAdd();
	}

	public static String showDialog(IMainControl mainControl, String value) {
		RunFlowDecideSelectDialog dialog;
		try {

			JSONObject initValue = new JSONObject();
			if (value != null && !value.isEmpty())
				initValue = new JSONObject(value);

			dialog = new RunFlowDecideSelectDialog(mainControl);

			if (dialog.runFlowSelector.getItemCount() > 0)
				dialog.runFlowSelector.setSelectedIndex(0);

			dialog.load(initValue);

			dialog.setModal(true);
			dialog.setVisible(true);

			if (!dialog.isok || dialog.data.names() == null)
				return initValue == null ? null : initValue.toString();

			String result = dialog.getResult().toString();
			dialog.dispose();
			return result;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}