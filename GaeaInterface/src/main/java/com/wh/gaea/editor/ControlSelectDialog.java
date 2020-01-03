package com.wh.gaea.editor;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Component;
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
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.draw.DrawInfo;
import com.wh.gaea.draw.IUINode;
import com.wh.gaea.interfaces.ChangeType;
import com.wh.gaea.interfaces.Config;
import com.wh.gaea.interfaces.IContainer;
import com.wh.gaea.interfaces.ICreateNodeSerializable;
import com.wh.gaea.interfaces.IDataSerializable;
import com.wh.gaea.interfaces.IDrawCanvas;
import com.wh.gaea.interfaces.IDrawNode;
import com.wh.gaea.interfaces.IDrawPageConfig;
import com.wh.gaea.interfaces.IEditorEnvironment;
import com.wh.gaea.interfaces.IEditorInterface.NodeDescInfo;
import com.wh.gaea.interfaces.IInitPage;
import com.wh.gaea.interfaces.INode;
import com.wh.gaea.interfaces.IOnPageSizeChanged;
import com.wh.gaea.interfaces.IScroll;
import com.wh.swing.tools.MsgHelper;

public class ControlSelectDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;

	HashMap<String, String> names = new HashMap<>();

	IDrawCanvas canvas = GlobalInstance.instance().createUICanvas();
	private JScrollBar hScrollBar;
	private JScrollBar vScrollBar;
	private JPanel panel_1;

	IDrawNode workflowNode;
	HashMap<String, IDrawNode> workflowNodes;
	String workflowRelationTitle;

	HashMap<String, Class<DrawInfo>> selectClassMap;

	protected void ok() {
		if (canvas.getSelected() == null) {
			MsgHelper.showMessage("请先选择一个控件后再试！");
			return;
		}

		if (selectClassMap != null && selectClassMap.size() > 0) {
			if (!selectClassMap.containsKey(((IUINode) canvas.getSelected()).getDrawInfo().getClass().getName())) {
				String[] names = new String[selectClassMap.size()];
				int index = 0;
				for (Class<DrawInfo> c : selectClassMap.values()) {
					try {
						Constructor<DrawInfo> constructor = c.getDeclaredConstructor(IUINode.class);
						DrawInfo drawInfo = constructor.newInstance((IUINode) null);
						names[index++] = drawInfo.typeName();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				MsgHelper.showMessage("请先选择一个" + Arrays.toString(names) + "类型控件后再试！");
				return;
			}
		}
		isok = true;
		setVisible(false);
	}

	public ControlSelectDialog() throws Exception {
		super();
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setIconImage(Toolkit.getDefaultToolkit().getImage(ControlSelectDialog.class.getResource("/image/browser.png")));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 1204, 768);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		panel_1 = new JPanel();
		contentPane.add(panel_1, BorderLayout.CENTER);
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

		canvas.setNodeEvent(new INode() {

			@Override
			public void onChange(IDrawNode[] nodes, ChangeType ct) {
				// TODO Auto-generated method stub

			}

			@Override
			public void DoubleClick(IDrawNode node) {
				if (!multiSelected)
					ok();
			}

			@Override
			public void Click(IDrawNode node) {
			}
		});

		panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		toolBar = new JToolBar();
		panel.add(toolBar, BorderLayout.EAST);
		toolBar.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		okButton = new JButton("确定");
		okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ok();
			}
		});

		btnNull = new JButton("null返回");
		btnNull.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btnNull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isok = true;
				canvas.setSelected(null);
				setVisible(false);
			}
		});
		toolBar.add(btnNull);
		toolBar.add(okButton);

		cancelButton = new JButton("取消");
		cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				isok = false;
				setVisible(false);
			}
		});
		toolBar.add(cancelButton);

		memo = new JLabel("New label");
		memo.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		memo.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(memo);

		setLocationRelativeTo(null);
	}

	class FileInfo {
		public String id;
		public String title;
		public String memo;
		public File file;

		public FileInfo(File file) throws Exception {
			if (file != null && file.exists()) {
				this.file = file;
				NodeDescInfo info = GlobalInstance.instance().getNodeDescInfo(file);
				id = info.id;
				title = info.title;
				memo = info.memo;
			}
		}

		public String toString() {
			if (title == null || title.isEmpty())
				return id;
			else
				return title;
		}
	}

	public void load(String name) {
		try {
			if (name == null || name.isEmpty())
				return;

			File f = GlobalInstance.instance().getProjectFile(IEditorEnvironment.UI_Dir_Name,
					GlobalInstance.instance().getUI_FileName(name));
			if (f == null)
				return;

			canvas.clear();

			if (!f.exists()) {
				// if (needHint)
				// MsgHelper.showMessage(this, "文件不存在！", "提示",
				// JOptionPane.WARNING_MESSAGE);
				return;
			}

			canvas.setFile(f);
			canvas.load(new ICreateNodeSerializable() {

				@Override
				public IDrawNode newDrawNode(JSONObject json) {
					return GlobalInstance.instance().createUINode(canvas);
				}

				@Override
				public IDataSerializable getUserDataSerializable(IDrawNode node) {
					return null;
				}
			}, new IInitPage() {

				@Override
				public void onPage(IDrawPageConfig pageConfig) {
					canvas.getPageConfig().setPageSizeMode(pageConfig.getCurPageSizeMode(), pageConfig.getWidth(),
							pageConfig.getHeight());
				}
			});

			canvas.getPageConfig()
					.setConfig(multiSelected ? new Config[] { Config.ccAllowSelect, Config.ccAllowMulSelect }
							: new Config[] { Config.ccAllowSelect });

			NodeDescInfo info = GlobalInstance.instance().getModelNodeDescInfo(name);

			if (info == null)
				memo.setText(canvas.getPageConfig().getMemo());
			else
				memo.setText(canvas.getPageConfig().getMemo() + " => 关联的工作流[" + info.workflowRelationName + "]中的节点："
						+ info.title + "[" + info.id + "]");

			for (IDrawNode node : canvas.getNodes()) {
				node.invalidRect();
			}

			canvas.repaint();

		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showMessage(this, e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void select(String[] ids) {
		if (ids == null)
			return;

		List<IDrawNode> nodeList = new ArrayList<>();
		for (String id : ids) {
			IDrawNode node = (IDrawNode) canvas.getNode(id);
			if (node != null)
				nodeList.add(node);
		}

		canvas.setSelecteds(nodeList.toArray(new IDrawNode[nodeList.size()]));
	}

	boolean isok = false;
	private JPanel panel;
	private JLabel memo;
	private JButton cancelButton;
	private JButton okButton;
	private JButton btnNull;
	boolean multiSelected = false;
	private JToolBar toolBar;

	public static Result showDialog(String uiid) {
		return showDialog(uiid, true);
	}

	public static Result showDialog(String uiid, boolean needOkButton) {
		return showDialog(uiid, null, needOkButton);
	}

	public static Result showDialog(String uiid, String selectControlId, boolean needOkButton) {
		return showDialog(uiid, selectControlId, null, needOkButton);
	}

	@SuppressWarnings("unchecked")
	public static Result showDialog(String uiid, String selectControlId, Class<?> selectClass, boolean needOkButton) {
		Result result = showDialog(uiid,
				selectControlId == null || selectControlId.isEmpty() ? null : new String[] { selectControlId },
				new Class[] { selectClass }, needOkButton, false);
		return result;
	}

	public static class Result {
		public boolean isok = false;
		public IDrawNode[] data;
	}

	public static Result showDialog(String uiid, String[] selectControlIds, Class<DrawInfo>[] selectClass,
			boolean needOkButton, boolean multiSelected) {
		ControlSelectDialog dialog;
		Result result = new Result();
		try {
			dialog = new ControlSelectDialog();
			if (!needOkButton)
				dialog.toolBar.remove(dialog.okButton);
			dialog.multiSelected = multiSelected;
			dialog.load(uiid);

			if (selectClass != null && selectClass.length > 0) {
				HashMap<String, Class<DrawInfo>> selectClassMap = new HashMap<>();
				for (Class<DrawInfo> class1 : selectClass) {
					if (class1 == null)
						continue;

					selectClassMap.put(class1.getName(), class1);
				}

				if (selectClassMap.size() > 0) {
					dialog.selectClassMap = selectClassMap;
					for (IDrawNode node : new ArrayList<>(dialog.canvas.getNodes())) {
						if (node == null)
							continue;

						IUINode uiNode = (IUINode)node;
						if (!selectClassMap.containsKey(uiNode.getDrawInfo().getClass().getName())
								&& !(uiNode.getDrawInfo() instanceof IContainer)) {
							dialog.canvas.remove(node.getId());
						}
					}
				}

				dialog.canvas.repaint();
			}

			if (selectControlIds != null && selectControlIds.length > 0)
				dialog.select(selectControlIds);

			dialog.setModal(true);
			dialog.setVisible(true);

			result.isok = dialog.isok;
			List<IDrawNode> nodeList = dialog.canvas.getSelecteds();
			result.data = nodeList.size() == 0 ? null : nodeList.toArray(new IDrawNode[nodeList.size()]);

			dialog.dispose();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.isok = false;
			return result;
		}
	}

}
