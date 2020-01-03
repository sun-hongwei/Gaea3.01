package com.wh.gaea.draw;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.wh.gaea.draw.control.CommandInfoType;
import com.wh.gaea.interfaces.IDrawNode;

public interface IUINode extends IDrawNode{

	void popCommanded(IDrawNode oldNode, CommandInfoType cit);

	String toString();

	Rectangle getRealRect();

	DrawInfo getDrawInfo();

	void initRect();

	void drawNode(Graphics g);

	void setDrawInfo(DrawInfo info);

	Rectangle getRefRect();
	void setRefRect(Rectangle rect);

}