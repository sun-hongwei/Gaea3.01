package com.wh.gaea.control;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JToolBar;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.draw.DrawInfo;
import com.wh.gaea.draw.IUINode;
import com.wh.gaea.draw.DrawInfo.Align;
import com.wh.gaea.draw.control.CommandInfoType;
import com.wh.gaea.draws.DrawCanvas;
import com.wh.gaea.draws.DrawNode;
import com.wh.gaea.draws.UICanvas;
import com.wh.gaea.draws.drawinfo.ListViewInfo;
import com.wh.gaea.draws.drawinfo.ReportInfo;
import com.wh.gaea.draws.drawinfo.ReportInfo.CellInfo;
import com.wh.gaea.interfaces.IDrawNode;

public class ChangeCanvasConfigure implements ActionListener {
	private UICanvas canvas;
	private IUpdate updater;
	private JComboBox<String> androidMode;
	private JComboBox<String> changeMode;

	public interface IUpdate {
		void changeCanvasToCustomMode();

		void notifyEdit(boolean isEdit);
	}

	protected String update(String value, double size, boolean isAbs) {
		double v = canvas.getStringSizeValue(value, size);
		if (isAbs)
			return String.valueOf(v);
		else
			return String.valueOf(canvas.roundTo(v / size * 100)) + "%";
	}

	public void changeCanvasCoordinates(boolean isAbs, List<IDrawNode> nodes) {
		for (IDrawNode drawNode : nodes) {
			IUINode node = (IUINode) drawNode;
			DrawInfo info = node.getDrawInfo();
			info.left = update(info.left, canvas.getPageWidth(), isAbs);
			if (info.xAlign == Align.alRight) {
				info.left = update(info.left, canvas.getPageWidth(), true);
			}
			info.top = update(info.top, canvas.getPageHeight(), isAbs);
			info.width = update(info.width, canvas.getPageWidth(), isAbs);
			info.height = update(info.height, canvas.getPageHeight(), isAbs);
		}
	}

	protected float getDpiScale() {
		if (canvas.isMTMode()) {
			return canvas.getAndroidDpiScale();
		}
		return DrawCanvas.getAndroidDpiScale(androidMode.getSelectedItem().toString());
	}

	protected void changeFont(DrawInfo info, int size) {
		info.font = new Font(info.font.getFontName(), info.font.getStyle(), size);
	}

	protected void changeFontSize(JSONObject header, float scale, boolean toWeb) {
		int fontsize = 12;
		if (header.has("fontsize")) {
			fontsize = header.getInt("fontsize");
		}
		header.put("fontsize", fontsize * (toWeb ? 1 / scale : scale));
	}

	public void changeCanvasFontSize(List<IDrawNode> nodes, boolean toWeb) {
		float scale = getDpiScale();
		changeCanvasFontSize(nodes, scale, toWeb);
	}
	
	public void changeCanvasFontSize(List<IDrawNode> nodes, float scale, boolean toWeb) {
		for (IDrawNode drawNode : nodes) {
			IUINode node = (IUINode) drawNode;
			DrawInfo info = node.getDrawInfo();
			int size = (int) canvas.getStringSizeValue(String.valueOf(info.font.getSize()), canvas.getPageWidth(),
					toWeb ? 1 / scale : scale);

			changeFont(info, size);

			if (info instanceof ReportInfo) {
				ReportInfo reportInfo = (ReportInfo) info;
				for (CellInfo cellInfo : reportInfo.getCells().values()) {
					changeFont(cellInfo.editor.getDrawInfo(), size);
				}
			} else if (info instanceof ListViewInfo) {
				ListViewInfo listViewInfo = (ListViewInfo) info;
				if (listViewInfo.header != null && !listViewInfo.header.isEmpty()) {
					JSONArray headers = new JSONArray(listViewInfo.header);
					for (Object object : headers) {
						JSONObject header = (JSONObject) object;
						changeFontSize(header, scale, toWeb);
					}

				}
				if (listViewInfo.data != null && !listViewInfo.data.isEmpty()) {
					JSONArray dataset = new JSONArray(listViewInfo.data);
					for (Object object : dataset) {
						JSONArray row = (JSONArray) object;
						for (Object object2 : row) {
							JSONObject data = (JSONObject) object2;
							changeFontSize(data, scale, toWeb);
						}
					}

				}
			}
		}
	}

	public void actionPerformed(ActionEvent e) {
		List<IDrawNode> nodes = canvas.getSelecteds();
		if (nodes.size() == 0)
			return;

		canvas.getACM().pushCommand(nodes, CommandInfoType.ctUpdateAttr);
		switch (changeMode.getSelectedIndex()) {
		case 0:// 转换为相对坐标
			changeCanvasCoordinates(false, nodes);
			break;
		case 1:// 转换为绝对坐标
			changeCanvasCoordinates(true, nodes);
			break;
		case 2:// 转换为android字体尺寸
			changeCanvasFontSize(nodes, false);
			break;
		case 3:// 转换为WEB字体尺寸
			changeCanvasFontSize(nodes, true);
			break;
		}
		canvas.clearSelect(false);
		canvas.setSelecteds(nodes.toArray(new DrawNode[nodes.size()]));

		updater.notifyEdit(true);
	}

	public ChangeCanvasConfigure(UICanvas canvas, JToolBar toolBar,
			JComboBox<String> androidMode, JComboBox<String> changeMode, IUpdate updater) {
		this.changeMode = changeMode;
		this.canvas = canvas;
		this.androidMode = androidMode;
		this.updater = updater;

		if (changeMode != null) {
			changeMode.setModel(new DefaultComboBoxModel<String>(
					new String[] { "相对坐标", "绝对坐标", "安卓字体", "WEB字体" }));
			changeMode.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					switch (changeMode.getSelectedIndex()) {
					case 2:
					case 3:
						toolBar.add(androidMode, toolBar.getComponentIndex(changeMode) + 1);
						break;
					default:
						toolBar.remove(androidMode);
						break;
					}
					
					toolBar.updateUI();
				}
			});
		}
		if (androidMode != null)
			androidMode.setModel(new DefaultComboBoxModel<String>(
					new String[] { "XXXHDPI", "XXHDPI", "XHDPI", "HDPI", "TVDPI", "MDPI", "LDPI" }));

	}
}
