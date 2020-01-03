package com.wh.gaea.interfaces;

import java.awt.Point;
import java.util.List;

import org.json.JSONObject;

import com.wh.gaea.draw.control.CommandInfoType;

public interface IActionCommandManager {

	void startPush(Point start, List<IDrawNode> nodes, CommandInfoType type);

	boolean end(Point endP);

	void popCommand();

	void pushCommand(IDrawNode[] nodes, CommandInfoType type);

	void pushCommand(IDrawNode node, CommandInfoType type);

	void pushCommand(JSONObject value, CommandInfoType type);

	void pushCommand(JSONObject value, String newid, CommandInfoType type);

	void pushCommand(JSONObject[] values, CommandInfoType type);

	void pushCommand(List<IDrawNode> nodes, CommandInfoType type);

	void pushCommand(JSONObject[] values, String newid, CommandInfoType type);

	void reset();

}