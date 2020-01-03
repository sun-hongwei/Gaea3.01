package com.wh.gaea.interfaces;

import org.json.JSONObject;

public interface ICreateNodeSerializable {
	public IDataSerializable getUserDataSerializable(IDrawNode node);

	public IDrawNode newDrawNode(JSONObject json);
}