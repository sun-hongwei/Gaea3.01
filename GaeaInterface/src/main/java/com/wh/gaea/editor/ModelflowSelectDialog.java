package com.wh.gaea.editor;

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
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingConstants;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.interfaces.ChangeType;
import com.wh.gaea.interfaces.IDrawCanvas;
import com.wh.gaea.interfaces.IDrawNode;
import com.wh.gaea.interfaces.IDrawPageConfig;
import com.wh.gaea.interfaces.IEditorEnvironment;
import com.wh.gaea.interfaces.IInitPage;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.gaea.interfaces.INode;
import com.wh.gaea.interfaces.IOnPageSizeChanged;
import com.wh.gaea.interfaces.IScroll;
import com.wh.gaea.interfaces.PageSizeMode;
import com.wh.swing.tools.MsgHelper;

public class ModelflowSelectDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private boolean allowNull = false;
	private Class<? extends IDrawNode> selectClass = null;
	private static final String BeginNode_Title = "起始节点";
	private static final String EndNode_Title = "结束节点";

	class DrawNodeInfo {
		public String pageName;

	}

	IDrawCanvas canvas;
	private JScrollBar vScrollBar;
	private JScrollBar hScrollBar;

	protected void reset() {
		canvas.clearNodes();
		canvas.repaint();
	}

	protected boolean allowEditNode(IDrawNode node) {
		if (node.getTitle().compareTo(BeginNode_Title) == 0 || node.getTitle().compareTo(EndNode_Title) == 0)
			return false;
		else
			return true;
	}

	protected void ok() {
		if (canvas.getSelected() == null && !allowNull) {
			MsgHelper.showMessage(null, "请先选择一个节点！", "提示", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (GlobalInstance.instance().isModelStartNode(canvas.getSelected()) || 
				GlobalInstance.instance().isModelEndNode(canvas.getSelected())) {
			MsgHelper.showMessage(null, "不能选择开始和终止节点！", "提示", JOptionPane.WARNING_MESSAGE);
			return;
		}

		if (selectClass != null) {
			if (GlobalInstance.instance().isModelChildNodeClass(selectClass)) {
				if (!GlobalInstance.instance().isModelChildNode(canvas.getSelected())) {
					MsgHelper.showMessage(null, "请选择子节点类型的节点！", "提示", JOptionPane.WARNING_MESSAGE);
					return;
				}
			} else {
				if (GlobalInstance.instance().isModelChildNode(canvas.getSelected())) {
					MsgHelper.showMessage(null, "请选择非子节点类型的节点！", "提示", JOptionPane.WARNING_MESSAGE);
					return;
				}
			}
		}
		result = ModalResult.mrOk;
		setVisible(false);
		;
	}

	protected void onEditNode(IDrawNode node) {
		if (GlobalInstance.instance().isModelChildNode(node)) {
			File subWorkflowFile = GlobalInstance.instance().getChildModelRelationFile(node.getId(), false);
			reloadWorkflowRelationTree(subWorkflowFile);
		} else if (!(GlobalInstance.instance().isModelStartNode(node) || GlobalInstance.instance().isModelEndNode(node))) {
			ok();
		}
	}

	public IMainControl mainControl;

	protected void editDrawNode(IDrawNode node) {
		if (node == null)
			return;

		if (GlobalInstance.instance().isModelStartNode(node) || GlobalInstance.instance().isModelEndNode(node)) 
			return;

		if (!allowEditNode(node))
			MsgHelper.showMessage(null, "模块关系图不支持编辑节点", "提示", JOptionPane.WARNING_MESSAGE);
		else {
			onEditNode(node);
		}
	}

	enum ModalResult {
		mrOk, mrCancel
	}

	public ModelflowSelectDialog(IMainControl mainControl) {
		super();
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setTitle("模块节点选择");
		setIconImage(
				Toolkit.getDefaultToolkit().getImage(ModelflowSelectDialog.class.getResource("/image/browser.png")));
		this.mainControl = mainControl;
		setBounds(100, 100, 1198, 906);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setPreferredSize(new Dimension(200, 10));
		panel.setLayout(new BorderLayout(0, 0));

		JPanel workflowContaint = new JPanel();
		panel.add(workflowContaint, BorderLayout.CENTER);
		workflowContaint.setLayout(new BorderLayout(0, 0));

		canvas = GlobalInstance.instance().createWorkfowCanvas();
		canvas.setOnPageSizeChanged(new IOnPageSizeChanged() {

			@Override
			public void onChanged(Point max) {
				hScrollBar.setMaximum(max.x);
				vScrollBar.setMaximum(max.y);
				hScrollBar.setValue(0);
				hScrollBar.setValue(0);
			}
		});
		
		workflowContaint.add((Component) canvas, BorderLayout.CENTER);
		canvas.setNodeEvent(new INode() {

			@Override
			public void onChange(IDrawNode[] nodes, ChangeType ct) {
				// TODO Auto-generated method stub

			}

			@Override
			public void DoubleClick(IDrawNode node) {
				editDrawNode(node);
			}

			@Override
			public void Click(IDrawNode node) {
				// TODO Auto-generated method stub

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
		hScrollBar.setUnitIncrement(10);
		hScrollBar.setMaximum(99999);
		hScrollBar.setOrientation(JScrollBar.HORIZONTAL);
		hScrollBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				canvas.setOffset(new Point(-e.getValue(), canvas.getOffset().y));
			}
		});
		workflowContaint.add(hScrollBar, BorderLayout.SOUTH);

		vScrollBar = new JScrollBar();
		vScrollBar.setUnitIncrement(10);
		vScrollBar.setMaximum(99999);
		vScrollBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				canvas.setOffset(new Point(canvas.getOffset().x, -e.getValue()));
			}
		});
		workflowContaint.add(vScrollBar, BorderLayout.EAST);

		canvas.setOnScroll(new IScroll() {

			@Override
			public void onScroll(int x, int y) {
				hScrollBar.setValue(Math.abs(x));
				vScrollBar.setValue(Math.abs(y));
			}
		});

		canvas.getPageConfig().setPageSizeMode(PageSizeMode.psA4V, 0, 0);

		panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.EAST);

		JButton button = new JButton("取消");
		button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				result = ModalResult.mrCancel;
				setVisible(false);
			}
		});

		JButton button_1 = new JButton("确定");
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});

		JButton btnNull = new JButton("null返回");
		btnNull.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btnNull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!allowNull) {
					MsgHelper.showMessage("不允许选择null！");
					return;
				}

				result = ModalResult.mrOk;
				canvas.setSelected(null);
				setVisible(false);
			}
		});
		panel_2.add(btnNull);
		panel_2.add(button_1);
		panel_2.add(button);

		memo = new JLabel("New label");
		memo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		memo.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(memo, BorderLayout.CENTER);

		JPanel panel_3 = new JPanel();
		getContentPane().add(panel_3, BorderLayout.NORTH);
		panel_3.setLayout(new BorderLayout(0, 0));

		title = new JLabel("New label");
		title.setFont(new Font("微软雅黑", Font.PLAIN, 16));
		title.setHorizontalAlignment(SwingConstants.CENTER);
		panel_3.add(title);

		JButton button_2 = new JButton("返回");
		button_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					File file = GlobalInstance.instance().getParentModelRelationFile(curWorkflowRelationFile.getName());
					if (file != null)
						reloadWorkflowRelationTree(file);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				// reloadWorkflowRelationTree(EditorEnvironment.getMainWorkflowRelationFileName());
			}
		});
		panel_3.add(button_2, BorderLayout.EAST);

		setLocationRelativeTo(null);

	}

	File curWorkflowRelationFile;

	public void reloadWorkflowRelationTree(String filename) {
		File file = GlobalInstance.instance().getProjectFile(IEditorEnvironment.Workflow_Dir_Name, filename);
		reloadWorkflowRelationTree(file);
	}

	public void reloadWorkflowRelationTree(File file) {
		curWorkflowRelationFile = file;
		canvas.setFile(curWorkflowRelationFile);
		try {
			canvas.load(GlobalInstance.instance().createDefaultWorkflowDeserializable(), new IInitPage() {
				@Override
				public void onPage(IDrawPageConfig pageConfig) {
					canvas.getPageConfig().setPageSizeMode();
					title.setText(canvas.getPageConfig().getTitle());
					memo.setText(canvas.getPageConfig().getMemo());
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	ModalResult result = ModalResult.mrCancel;
	private JPanel panel_1;
	private JLabel title;
	private JLabel memo;

	public static class Result {
		public String id = null;
		public String title = null;
		public String name = null;
	}

	public static Result showDialog(IMainControl mainControl, String curID, String curName) {
		return showDialog(mainControl, curID, curName, false, null);
	}

	public static Result showDialog(IMainControl mainControl, String curID, String curName, boolean allowNull,
			Class<? extends IDrawNode> c) {
		ModelflowSelectDialog editor = new ModelflowSelectDialog(mainControl);
		File file = null;
		try {
			if (curID != null && !curID.isEmpty())
				file = GlobalInstance.instance().getModelRelationFileFromNodeID(curID);
			if (file == null && curName != null && !curName.isEmpty())
				file = GlobalInstance.instance().getModelRelationFileFromNodeName(curName);
		} catch (Exception e) {
			e.printStackTrace();
			file = null;
		}

		String name = GlobalInstance.instance().getMainModelRelationFileName();
		if (file != null)
			name = file.getName();
		editor.reloadWorkflowRelationTree(name);

		if (curID != null || curName != null) {
			for (IDrawNode node : editor.canvas.getNodes()) {
				if ((curID != null && !curID.isEmpty() && node.getId().compareTo(curID) == 0)
						|| (curName != null && !curName.isEmpty() && node.getName().compareTo(curName) == 0)) {
					editor.canvas.setSelected(node);
					break;
				}
			}
		}
		editor.allowNull = allowNull;
		editor.selectClass = c;
		editor.setModal(true);
		editor.setVisible(true);
		Result result = null;
		if (editor.result == ModalResult.mrOk) {
			result = new Result();
			IDrawNode node = editor.canvas.getSelected();
			if (node != null) {
				result.id = node.getId();
				result.title = node.getTitle();
				result.name = node.getName();
			}
		}
		editor.dispose();
		return result;
	}
}
