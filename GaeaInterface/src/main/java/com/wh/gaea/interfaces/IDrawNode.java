package com.wh.gaea.interfaces;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.tools.JsonHelp;

public interface IDrawNode {
	static <T> T newInstance(Class<T>c, IDrawCanvas canvas) throws Exception {
		return c.getConstructor(IDrawCanvas.class).newInstance(canvas);	
	}
	
	static void drawText(Graphics g, Font font, Rectangle rect, StringBuilder sBuilder) {
		Color textColor = new Color(250, 250, 250);
		drawText(g, font, textColor, rect, sBuilder);
	}

	static void drawText(Graphics g, Font font, Color color, Rectangle rectangle,
			StringBuilder sBuilder) {
		Rectangle rect = new Rectangle(rectangle);
		rect.x += 5;
		rect.y += 5;
		rect.width -= 10;
		rect.height -= 10;

		Color old = g.getColor();
		g.setColor(color);
		List<String> draws = new ArrayList<>();
		List<Rectangle2D> widths = new ArrayList<>();
		int start = 0;
		int end = start;

		int minDivX = 5;
		int maxWidth = (int) rect.getWidth() - minDivX * 2;
		FontMetrics fm = g.getFontMetrics(font);
		do {
			end += 2;
			String tmp = sBuilder.substring(start, end < sBuilder.length() ? end : sBuilder.length());
			Rectangle2D textRect = fm.getStringBounds(tmp, g);
			if (textRect.getWidth() > maxWidth || end >= sBuilder.length()) {
				draws.add(tmp);
				widths.add(textRect);
				start = end;
			}
		} while (end < sBuilder.length());

		int divY = 3;
		int y = rect.y
				+ (rect.height - (int) widths.get(0).getHeight() * draws.size() - divY * (draws.size() - 1)) / 2;
		y = y + (int) widths.get(0).getHeight();
		for (int i = 0; i < draws.size(); i++) {
			int x = rect.x + (rect.width - (int) widths.get(i).getWidth()) / 2;
			g.drawString(draws.get(i), x, y);
			y += widths.get(i).getHeight() + divY;
		}

		g.setColor(old);
	}

	static void drawMulitText(Graphics g, Font font, Color color, Rectangle rect, String value) {
		String text = value.replace("\r", "");
		String[] texts = text.split("\n");
		int hdiv = 3;
		Rectangle2D[] textRects = new Rectangle2D[texts.length];
		int heights = 0;
		int index = 0;
		int maxWidth = 0;
		FontMetrics fm = g.getFontMetrics(font);
		for (String string : texts) {
			Rectangle2D textRect = fm.getStringBounds(string, g);
			heights += textRect.getHeight() + hdiv;
			textRects[index++] = textRect;
			if (maxWidth < textRect.getWidth())
				maxWidth = (int) textRect.getWidth();
		}

		heights -= hdiv;

		int x = rect.x + (rect.width - maxWidth) / 2;
		int y = (int) (rect.y + (rect.height - heights) / 2 + textRects[0].getHeight());

		Color old = g.getColor();
		g.setColor(color);

		Font oldFont = g.getFont();
		g.setFont(font);

		for (int i = 0; i < textRects.length; i++) {
			g.drawString(texts[i], x, y);
			y += textRects[i].getHeight() + hdiv;
		}

		g.setFont(oldFont);
		g.setColor(old);
	}

	static Rectangle2D getTextRectangle(Graphics g, Font font, String text) {
		FontMetrics fm = g.getFontMetrics(font);
		Rectangle2D textRect = fm.getStringBounds(text, g);
		return textRect;
	}

	static int drawLineText(Graphics g, Font font, Color textColor, int x, int y, int width, int height,
			String text) {
		return drawLineText(g, font, textColor, x, y, width, height, text, true);
	}

	static int drawLineText(Graphics g, Font font, Color textColor, int x, int y, int width, int height,
			String text, boolean center) {
		return drawLineText(g, font, textColor, x, y, width, height, text, center, false);
	}

	static int getTextHeight(Graphics g, Font font, String text){
		FontMetrics fm = g.getFontMetrics(font);
		Rectangle2D textRect = null;
		textRect = fm.getStringBounds(text, g);
		return (int) textRect.getHeight();
	}
	
	static int drawLineText(Graphics g, Font font, Color textColor, int x, int y, int width, int height,
			String text, boolean center, boolean onlyCompute) {
		if (text == null || text.isEmpty() || width == 0)
			return 0;

		Color old = g.getColor();
		g.setColor(textColor);

		FontMetrics fm = g.getFontMetrics(font);
		Rectangle2D textRect = null;
		int count = text.length();
		textRect = fm.getStringBounds(text, g);

		if (onlyCompute)
			return (int) textRect.getHeight();

		String oldText = text;
		while (!text.isEmpty()) {
			if (text.length() < 4)
				break;

			textRect = fm.getStringBounds(text, g);
			if (textRect.getWidth() > width) {
				int multiple = (int) textRect.getWidth() / width;
				if (multiple >= 2) {
					int div = text.length() / 2;
					text = text.substring(0, div == 0 ? 1 : div);
				} else {
					text = text.substring(0, text.length() - 4);
				}
			} else if (text.length() == count || text.substring(text.length() - 3).compareTo("...") == 0)
				break;
			else {
				text = text + "...";
			}
		}

		if (oldText != null && !oldText.isEmpty() && (text == null || text.isEmpty())){
			text = "...";
		}
		
		textRect = fm.getStringBounds(text, g);
		
		int top = height;
		if (height == -1)
			top = y + (int) textRect.getHeight();
		else
			top = y + (height - (int) (fm.getAscent() + fm.getDescent())) / 2 + fm.getAscent();
		

		int drawX = x;
		if (center)
			drawX += (width - (int) textRect.getWidth()) / 2;
		
		g.setFont(font);

		g.drawString(text, drawX, top);

		g.setColor(old);

		return (int) textRect.getHeight();
	}

	static void drawVerticalText(Graphics g, Font font, Color textColor, int x, int y, int width, int height,
			String text, boolean center, boolean ascending) {
		if (text == null || text.isEmpty() || width <= 0 || height <= 0)
			return;

		Color old = g.getColor();
		g.setColor(textColor);

		FontMetrics fm = g.getFontMetrics(font);

		int index = ascending ? 0 : text.length() - 1;
		while (true) {
			String tmp;
			if (ascending) {
				if (index == text.length())
					break;
				tmp = String.valueOf(text.charAt(index++));
			} else {
				if (index == -1)
					break;
				tmp = String.valueOf(text.charAt(index--));
			}
			
			Rectangle2D textRect = fm.getStringBounds(tmp, g);

			if (textRect.getHeight() - fm.getAscent() > height - 1) {
				break;
			}

			y += textRect.getHeight();

			int drawX = x;
			if (center)
				drawX += (width - (int) textRect.getWidth()) / 2;
			
			g.setFont(font);

			g.drawString(tmp, drawX, y);
			
			
			height -= textRect.getHeight();
		}

		g.setColor(old);
	}

	static void drawLine(Graphics g, Point start, int width) {
		drawLine(g, start, new Point(start.x + width, start.y));
	}

	static void drawLine(Graphics g, Point start, Point end) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(2.0f));
		g2.drawLine(start.x, start.y, end.x, end.y);
	}

	static IDrawNode load(File nodeFile, ICreateNodeSerializable createUserDataSerializable)
			throws Exception {
		JSONObject data = (JSONObject) JsonHelp.parseCacheJson(nodeFile, null);
		if (data == null)
			return null;

		return fromJson(null, data, createUserDataSerializable);
	}

	static IDrawNode fromJson(IDrawCanvas canvas, JSONObject data,
			ICreateNodeSerializable createUserDataSerializable) throws Exception {
		IDrawNode node;
		if (data.has("class")) {
			String className = data.getString("class");
			if (className.compareTo("com.wh.system.form.draws.FlowNode$UIWorkflowNode") == 0) {
				className = "com.wh.system.form.draws.FlowNode$ActionNode";
			}
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();

			if (className.startsWith("com.wh.system.form.draws")){
				className = "com.wh.gaea.draws" + className.substring(className.lastIndexOf("."));
			}else if (className.startsWith("com.wh.system.draws")){
				className = "com.wh.gaea.draws" + className.substring(className.lastIndexOf("."));
			}else if (className.startsWith("com.wh.draws")){
				className = "com.wh.gaea.draws" + className.substring(className.lastIndexOf("."));
			}
			Class<?> classDeclare = classLoader.loadClass(className);
			node = (IDrawNode) IDrawNode.newInstance(classDeclare, canvas);
		} else
			node = (IDrawNode) createUserDataSerializable.newDrawNode(data);

		node.fromJson(data, createUserDataSerializable);
		return node;
	}

	boolean isParent(IDrawNode node);

	void pasted();

	boolean isDrawTreeRoot();

	IDrawCanvas getCanvas();

	List<String> getPrevs();

	List<String> getNexts();

	void setCanvas(IDrawCanvas canvas);

	/**
	 * 仅刷新显示区域，如果要永久修改节点的显示区域，应该调用setRect方法
	 */
	void invalidRect();

	/**
	 * 仅刷新显示区域，如果要永久修改节点的显示区域，应该调用setRect方法
	 */
	void invalidRect(Rectangle rect);

	/**
	 * 仅刷新显示区域，如果要永久修改节点的显示区域，应该调用setRect方法
	 */
	void invalidRect(Point pt);

	Rectangle getRect();

	void setRect(Rectangle rect);

	Point getLocation();

	int getWidth();

	int getHeight();

	void setLocation(Point pos);

	void setWidth(int width);

	void setHeight(int height);

	JSONObject toJson() throws JSONException;

	void fromJson(JSONObject data, ICreateNodeSerializable createUserDataSerializable) throws JSONException;

	Point getCenter();

	boolean needShow(Rectangle disRect);

	boolean isPoint(Point pt);

	void drawLins(Graphics g);

	void draw(Graphics g, boolean needCheckViewPort);

	String getId();
	
	String getName();
	
	String getTitle();

	String getMemo();

	void setTitle(String title);
	
	int getZOrder();

	void setId(String id);
	
	void setName(String name);
	
	void setFont(Font font);
	
	Font getFont();
	
	void setZOrder(int zOrder);
	void fixSize(boolean reset);

}