package com.wh.gaea.control;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JDesktopPane;

public class DesktopPanelEx extends JDesktopPane {

	private static final long serialVersionUID = 1L;

	BufferedImage image;

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public void paint(Graphics g) {
		if (image != null && getAllFrames().length == 0) {
			Rectangle rect = g.getClipBounds();
			g.drawImage(image, rect.x, rect.y, rect.width, rect.height, null);
		} else {
			super.paint(g);
		}
	}
}
