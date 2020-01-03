package com.wh.gaea.draws.drawinfo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.gaea.draw.DrawInfo;
import com.wh.gaea.draw.IUINode;
import com.wh.gaea.interfaces.IDrawInfoDefines;

public class ProgressBarInfo extends DrawInfo{
	public String typeName(){
		return IDrawInfoDefines.ProgressBar_Name;
	}
	public ProgressBarInfo(IUINode node) {
		super(node);
		allowEdit = false;
	}
	
	public void drawNode(Graphics g, Rectangle rect){
		Color old = g.getColor();
		g.setColor(Color.BLUE);
		float div = (float)start / size;
		float width = div * rect.width;
		if (width > 0)
			g.fillRect(rect.x, rect.y, (int)width, rect.height);
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
		g.setColor(old);
	}

	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("start", start);
		
		json.put("size", size);
		json.put("reportHeight", reportHeight);
		json.put("reportWidth", reportWidth);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);
		if (json.has("start"))
			start = json.getInt("start");
		else
			start = 0;
		
		if (json.has("size"))
			size = json.getInt("size");
		else
			size = 100;
		
		if (json.has("reportHeight"))
			reportHeight = json.getInt("reportHeight");
		else
			reportHeight = 80;
		
		if (json.has("reportWidth"))
			reportWidth = json.getInt("reportWidth");
		else
			reportWidth = 300;
		
	}

	public int start = 0;
	public int size = 100;
	public int reportHeight = 80;
	public int reportWidth = 300;
}