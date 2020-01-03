package com.wh.gaea.draws;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.gaea.draws.DrawCanvas;
import com.wh.gaea.draws.DrawCanvas.IntersectPointHelp;
import com.wh.gaea.draws.DrawCanvas.IntersectPointHelp.IntersectPoint;
import com.wh.gaea.draws.DrawCanvas.IntersectPointHelp.Line;
import com.wh.gaea.interfaces.ChangeType;
import com.wh.gaea.interfaces.IDrawNode;
import com.wh.gaea.interfaces.IPolyLine;
import com.wh.gaea.interfaces.ResizeButtonType;

public class Polyline implements IPolyLine {

	List<Integer> xs = new ArrayList<>();
	List<Integer> ys = new ArrayList<>();

	boolean isAddPoint = false;
	Point addPoint = null;

	DrawNode start;
	DrawNode end;

	DrawCanvas canvas;

	String getHashKey() {
		return getHashKey(start, end);
	}

	@Override
	public boolean ptInPolyline(Rectangle rect) {
		updateBeginEndPoint();

		Rectangle2D advRect = (Rectangle2D) rect;
		for (int i = 0; i < xs.size() - 1; i++) {
			Line2D.Float line = new Line2D.Float((float) xs.get(i), (float) ys.get(i), (float) xs.get(i + 1),
					(float) ys.get(i + 1));

			if (advRect.intersectsLine(line))
				return true;

			// Line line = new Line(new IntersectPoint(xs.get(i),
			// ys.get(i)),
			// new IntersectPoint(xs.get(i + 1), ys.get(i + 1)));
			// IntersectPointHelp.IntersectPoint inPoint =
			// IntersectPointHelp.intersection(line, rect);
			// if (inPoint != null) {
			// return true;
			// }
		}

		return false;

	}

	@Override
	public boolean ptInPolyline(Point pt) {
		pt = canvas.getRealPoint(pt);
		for (int i = 0; i < xs.size() - 1; i++) {
			IntersectPointHelp.Line line = new Line(new IntersectPoint(xs.get(i), ys.get(i)),
					new IntersectPoint(xs.get(i + 1), ys.get(i + 1)));
			if (Line.intersection(line, pt)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean removePoint(Point pt) {
		pt = canvas.getRealPoint(pt);
		for (int i = 0; i < xs.size() - 1; i++) {
			Point rectPt = new Point(xs.get(i), ys.get(i));
			Rectangle rectangle = IntersectPointHelp.getTestRect(rectPt);
			if (rectangle.contains(pt)) {
				xs.remove(i);
				ys.remove(i);
				return true;
			}
		}

		return false;
	}

	public static String getHashKey(DrawNode start, DrawNode end) {
		return start.id + end.id;
	}

	boolean check(IDrawNode vStart, IDrawNode vEnd) {
		return vStart == start && vEnd == end;
	}

	protected Polyline(DrawCanvas canvas) {
		this.canvas = canvas;
	}

	public Polyline(DrawCanvas canvas, DrawNode start, DrawNode end) {
		this.canvas = canvas;
		this.start = start;
		this.end = end;

		Point startPt = start.getCenter();
		Point endPt = end.getCenter();
		xs.add(startPt.x);
		xs.add(endPt.x);
		ys.add(startPt.y);
		ys.add(endPt.y);

	}

	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject jsonObject = new JSONObject();

		jsonObject.put("start", start.id);
		jsonObject.put("end", end.id);

		JSONArray xJson = new JSONArray();
		for (int i = 0; i < xs.size(); i++) {
			xJson.put(xs.get(i));
		}
		jsonObject.put("xs", xJson);
		JSONArray yJson = new JSONArray();
		for (int i = 0; i < ys.size(); i++) {
			yJson.put(ys.get(i));
		}
		jsonObject.put("ys", yJson);
		return jsonObject;
	}

	public static Polyline fromJson(DrawCanvas canvas, JSONObject data) throws JSONException {
		Polyline line = new Polyline(canvas);
		JSONArray xJson = data.getJSONArray("xs");
		line.xs.clear();
		for (int i = 0; i < xJson.length(); i++) {
			line.xs.add(xJson.getInt(i));
		}
		line.ys.clear();
		JSONArray yJson = data.getJSONArray("ys");
		for (int i = 0; i < xJson.length(); i++) {
			line.ys.add(yJson.getInt(i));
		}

		String id = data.getString("start");
		if (!canvas.nodes.containsKey(id))
			return null;
		line.start = (DrawNode) canvas.nodes.get(id);

		id = data.getString("end");
		if (!canvas.nodes.containsKey(id))
			return null;
		line.end = (DrawNode) canvas.nodes.get(id);

		return line;
	}

	int resetIndex = -1;

	protected ResizeButtonType getResizeButtonType(Point pt) {
		resetIndex = -1;
		for (int i = 0; i < resetButtons.size(); i++) {
			Rectangle rectangle = resetButtons.get(i);
			if (rectangle.contains(pt)) {
				resetIndex = i + 1;
				return ResizeButtonType.rtCustom;
			}
		}

		return ResizeButtonType.rtNone;
	}

	public void mouseReleased(MouseEvent e) {
		if (isAddPoint) {
			for (int i = 0; i < ys.size() - 1; i++) {
				IntersectPointHelp.IntersectPoint p1 = new IntersectPoint((double) xs.get(i), (double) ys.get(i));
				IntersectPointHelp.IntersectPoint p2 = new IntersectPoint((double) xs.get(i + 1), (double) ys.get(i + 1));

				IntersectPointHelp.Line line = new Line(p1, p2);

				IntersectPointHelp.IntersectPoint intersectPoint = IntersectPointHelp.intersection(line,
						IntersectPointHelp.getTestRect(addPoint));
				if (intersectPoint != null) {
					xs.add(i + 1, (int) intersectPoint.x);
					ys.add(i + 1, (int) intersectPoint.y);
					canvas.fireChange(ChangeType.ctLineChanged);
					canvas.repaint();
					break;
				}
			}
		}

		isAddPoint = false;
		addPoint = null;
	}

	public void mousePressed(MouseEvent e) {
		if (e.isControlDown()) {
			if (!removePoint(e.getPoint())) {
				isAddPoint = true;
				addPoint = canvas.getRealPoint(e.getPoint());
			}
		} else {

		}
	}

	public void mouseMoved(MouseEvent e) {
		if (canvas.isMouseDown && canvas.curRt == ResizeButtonType.rtCustom && resetIndex != -1) {
			Point pt = canvas.getRealPoint(e.getPoint());
			xs.set(resetIndex, pt.x);
			ys.set(resetIndex, pt.y);
			canvas.fireChange(ChangeType.ctLineChanged);
			canvas.repaint();
		}
		// else{
		// if (canvas.isMouseDown && resetIndex == -1)
		// canvas.repaint();
		// }
	}

	protected void updateBeginEndPoint() {
		Point startPt = start.getCenter();
		Point endPt = end.getCenter();
		xs.set(0, startPt.x);
		ys.set(0, startPt.y);
		xs.set(xs.size() - 1, endPt.x);
		ys.set(xs.size() - 1, endPt.y);
	}

	protected void drawPolyline(Graphics g) {
		int[] xPoints = new int[xs.size()];
		int[] yPoints = new int[ys.size()];
		updateBeginEndPoint();
		for (int i = 0; i < yPoints.length; i++) {
			xPoints[i] = xs.get(i);
			yPoints[i] = ys.get(i);
		}

		Graphics2D graphics2d = (Graphics2D) g;
		graphics2d.setStroke(new BasicStroke(1.5f));
		g.drawPolyline(xPoints, yPoints, xPoints.length);
	}

	protected void drawNode(Graphics g) {
		drawPolyline(g);
	}

	List<Rectangle> resetButtons = new ArrayList<>();

	protected void darwResizeButtons(Graphics g) {
		resetButtons.clear();
		g.setColor(Color.BLUE);
		for (int i = 1; i < xs.size() - 1; i++) {
			int x = xs.get(i);
			int y = ys.get(i);
			int div = 5;
			Rectangle rectangle = new Rectangle(x - div, y - div, div * 2, div * 2);
			resetButtons.add(rectangle);
			g.fill3DRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, true);
		}
	}

}