package com.wh.gaea.draws.drawinfo;

import com.wh.gaea.draw.IUINode;
import com.wh.gaea.interfaces.IDrawInfoDefines;

public class TextAreaInfo extends TextInfo{
	public String typeName(){
		return IDrawInfoDefines.TextArea_Name;
	}
	public TextAreaInfo(IUINode node) {
		super(node);
		height = "48px";
		width = "150px";
	}

	protected String getButtonImageFileName() {
		return "multi-line.png";
	}
		

}