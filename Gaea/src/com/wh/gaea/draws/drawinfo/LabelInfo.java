package com.wh.gaea.draws.drawinfo;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.gaea.draw.IUINode;
import com.wh.gaea.interfaces.IDrawInfoDefines;
import com.wh.gaea.interfaces.IDrawNode;

public class LabelInfo extends ClickableInfo {
	public enum TextAlign {
		taTop, taLeft, taBottom, taRight
	}

	public enum TextWay{
		twLeftRight, twRightLeft
	}
	
	public String typeName() {
		return IDrawInfoDefines.Label_Name;
	}

	public LabelInfo(IUINode node) {
		super(node);
		needBackground = false;
		needFrame = false;
		allowEdit = false;
	}

	public void drawNode(Graphics g, Rectangle rect) {
		String text = value == null ? "" : value.toString();
		if (text == null || text.isEmpty())
			return;

		if (autosize) {
			Rectangle2D textRect = IDrawNode.getTextRectangle(g, getFont(), text);
			int newWidth = (int) textRect.getWidth();
			int newHeight = (int) textRect.getHeight();
			if (newWidth != rect.width || newHeight != rect.height) {
				rect.width = newWidth;
				rect.height = newHeight;
				node.fixSize(false);
			}
		}

		if (!autosize && text != null && !text.isEmpty()) {
			switch (textAlign) {
			case taTop:
			case taBottom:
				if (textAlign == TextAlign.taBottom) {
					String tmp = text;
					text = "";
					for (int i = tmp.length() - 1; i >= 0; i--) {
						text += tmp.charAt(i);
					}
				}
				IDrawNode.drawLineText(g, getFont(), textColor, rect.x, rect.y, rect.width, rect.height, text, false, false);
				break;
			case taRight:
				IDrawNode.drawVerticalText(g, getFont(), textColor, rect.x, rect.y, rect.width, rect.height, text, false, false);
				break;
			case taLeft:
				IDrawNode.drawVerticalText(g, getFont(), textColor, rect.x, rect.y, rect.width, rect.height, text, false, true);
				break;
			}
		}else {
			IDrawNode.drawLineText(g, getFont(), textColor, rect.x, rect.y, rect.width, rect.height, text, false, false);
		}
	}

	public void fromJson(JSONObject json) throws JSONException {
		super.fromJson(json);
		try {
			if (json.has("textAlign"))
				textAlign = TextAlign.valueOf(json.getString("textAlign"));
			else
				textAlign = TextAlign.taTop;
			
		} catch (Exception e) {
		}

		try {
			if (json.has("textWay"))
				textWay = TextWay.valueOf(json.getString("textWay"));
			else
				textWay = TextWay.twLeftRight;
			
		} catch (Exception e) {
		}

		if (json.has("align"))
			align = json.getString("align");
		else
			align = "center";

		if (json.has("href"))
			href = json.getString("href");
		else
			href = null;

		if (json.has("download"))
			download = json.getString("download");
		else
			download = null;

		if (json.has("target"))
			target = json.getString("target");
		else
			target = null;

		if (json.has("fontVariant"))
			fontVariant = json.getString("fontVariant");
		else
			fontVariant = "normal";

		if (json.has("gradient"))
			gradient = json.getString("gradient");
		else
			gradient = null;

		if (json.has("autosize"))
			autosize = json.getBoolean("autosize");
		else
			autosize = true;

		if (json.has("showLinkLine"))
			showLinkLine = json.getBoolean("showLinkLine");
		else
			showLinkLine = false;
	}

	public JSONObject toJson(boolean needAll) throws JSONException {
		JSONObject json = super.toJson(needAll);
		json.put("textAlign", textAlign.name());
		json.put("textWay", textWay.name());
		json.put("align", needAll && align == null ? "center" : align);
		json.put("href", needAll && href == null ? "" : href);
		json.put("download", needAll && download == null ? "" : download);
		json.put("target", needAll && target == null ? "_self" : target);
		json.put("fontVariant", needAll && fontVariant == null ? "normal" : fontVariant);
		json.put("gradient", needAll && gradient == null ? "" : gradient);
		json.put("autosize", autosize);
		json.put("showLinkLine", showLinkLine);
		return json;
	}

	public String href;
	public String download;
	public String target = "_self";
	public String align = "center";
	public String fontVariant = "normal";
	public boolean autosize = true;
	public String gradient;
	public boolean showLinkLine = false;
	public TextAlign textAlign = TextAlign.taTop;
	public TextWay textWay = TextWay.twLeftRight;

}