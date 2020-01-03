package com.wh.swing.tools.dialog;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.accessibility.Accessible;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

public class JImage extends JComponent implements IControl, SwingConstants, Accessible {

	public enum DrawWay {
		dwStretch, dwCenter, dwFillInCenter, dwXY
	}

	BufferedImage image;
	float scale = 1F;
	DrawWay drawWay = DrawWay.dwCenter;
	int angle = 0;
	Rectangle margin;
	
	public Rectangle getMargin() {
		return margin;
	}

	public void setMargin(Rectangle margin) {
		this.margin = margin;
		repaint();
	}

	public int getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		this.angle = angle;
		repaint();
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
		repaint();
	}

	public DrawWay getDrawWay() {
		return drawWay;
	}

	public void setDrawWay(DrawWay drawWay) {
		this.drawWay = drawWay;
		repaint();
	}


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DefaultControl defaultControl = new DefaultControl();

	@Override
	public Object getTag(String key) {
		return defaultControl.getTag(key);
	}

	@Override
	public void setTag(String key, Object value) {
		defaultControl.setTag(key, value);
	}

	public void setIcon(ImageIcon image) {		
		this.image = new BufferedImage(image.getIconWidth(), image.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		this.image.getGraphics().drawImage(image.getImage(), 0, 0, null);
		repaint();
	}

	public void setImage(BufferedImage image) {
		this.image = image;
		repaint();
	}

	protected Rectangle getFillInRectangle(Rectangle rect) {
		Rectangle destRect = new Rectangle(rect);
		Rectangle controlRect = getBounds();
		float divWH = (float)rect.width / rect.height;
		float divHW = (float)rect.height / rect.width;
		while (destRect.width > controlRect.width && destRect.height > controlRect.height) {
			if (destRect.width > controlRect.height) {
				if (destRect.width <= controlRect.width)
					destRect.width -= 5;
				else
					destRect.width = controlRect.width;
				destRect.height = (int) (destRect.width * divHW);
			}else {
				if (destRect.height <= controlRect.height)
					destRect.height -= 5;
				else
					destRect.height = controlRect.height;				
				destRect.width = (int) (destRect.height * divWH);
			}
		}
		
		destRect.x = (controlRect.width - destRect.width) / 2;
		destRect.y = (controlRect.height - destRect.height) / 2;
		
		return destRect;
	}
	
	public void paint(Graphics g) {
		if (image == null)
			return;
		
		Rectangle rect = getBounds();
		Graphics2D graphics2d = (Graphics2D) g;
		
		if (margin != null && margin.width > 0 && margin.height > 0) {
			graphics2d.translate(margin.x, margin.y);
			graphics2d.setClip(new Rectangle(0, 0, margin.width, margin.height));
		}
		
		if (angle != 0)
			graphics2d.rotate(Math.toRadians(angle), getWidth() / 2, getHeight() / 2);
		
		graphics2d.scale(scale, scale);
		
		switch (drawWay) {
		case dwFillInCenter:
			rect = getFillInRectangle(new Rectangle(0, 0, image.getWidth(), image.getHeight()));
			graphics2d.drawImage(image, rect.x, rect.y, rect.width, rect.height, null);
			break;
		case dwStretch:
			graphics2d.drawImage(image, rect.x, rect.y, rect.width, rect.height, null);
			break;
		case dwXY:
			graphics2d.drawImage(image, rect.x, rect.y, null);
			break;
		case dwCenter:
			rect.x = (int) ((getWidth() - image.getWidth()) / 2 / scale);
			rect.y = (int) ((getHeight() - image.getHeight()) / 2 / scale);
			graphics2d.drawImage(image, rect.x, rect.y, null);
			break;
		}		
	}
}
