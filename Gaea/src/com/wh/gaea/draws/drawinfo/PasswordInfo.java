package com.wh.gaea.draws.drawinfo;

import com.wh.gaea.draw.IUINode;
import com.wh.gaea.interfaces.IDrawInfoDefines;

public class PasswordInfo extends TextInfo{
	public String typeName(){
		return IDrawInfoDefines.Password_Name;
	}
	public PasswordInfo(IUINode node) {
		super(node);
	}

	protected String getButtonImageFileName() {
		return "password.png";
	}
				
}