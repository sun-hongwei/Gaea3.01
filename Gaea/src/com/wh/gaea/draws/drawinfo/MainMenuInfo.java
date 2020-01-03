package com.wh.gaea.draws.drawinfo;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.gaea.draw.DrawInfo;
import com.wh.gaea.draw.IUINode;
import com.wh.gaea.editor.MenuEditorDialog;
import com.wh.gaea.interfaces.IDrawInfoDefines;
import com.wh.gaea.interfaces.IDrawNode;
import com.wh.tools.JsonHelp;

public class MainMenuInfo extends DrawInfo{
	public String typeName(){
		return IDrawInfoDefines.MainMenu_Name;
	}

	public MainMenuInfo(IUINode node) {
		super(node);
		width = "100%";
		height = "50px";
		value = "";
	}

	HashMap<String, Rectangle> rowCells = new HashMap<>();
	
	public void drawNode(Graphics g, Rectangle rect){
		try {
			File file = MenuEditorDialog.getMainMenuFile();
			if (!file.exists())
				return;
			
			JSONArray json = (JSONArray) JsonHelp.parseCacheJson(file, null);
			List<String> menus = MenuEditorDialog.getMenuRoot(json, "pid", "text");
			if (menus.size() == 0)
				return;
			
			int left = rect.x;
			int top = rect.y;
			int height = rect.height;
			int width = rect.width / (menus.size() + (rect.width % menus.size() == 0 ? 0 : 1));
			for (int i = 0; i < menus.size(); i++) {
				String text = menus.get(i);
				g.drawRect(left, top, width, height);
				IDrawNode.drawLineText(g, getFont(), textColor, left, top, width, height, text);						
				left += width;
			}					
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public JSONObject toJson(boolean needAll) throws JSONException{
		JSONObject json = super.toJson(needAll);
		json.put("sharedata", "");
		json.put("border", border);
		return json;
	}
	
	public void fromJson(JSONObject json) throws JSONException{
		super.fromJson(json);
		if (json.has("border"))
			border = json.getBoolean("border");
		else
			border = false;
	}

    public boolean border = false;

    public String sharedata;
}