package com.wh.gaea.interfaces;

import java.awt.Point;
import java.awt.Rectangle;

import org.json.JSONException;
import org.json.JSONObject;

public interface IPolyLine {

	boolean ptInPolyline(Rectangle rect);

	boolean ptInPolyline(Point pt);

	boolean removePoint(Point pt);

	JSONObject toJson() throws JSONException;

}