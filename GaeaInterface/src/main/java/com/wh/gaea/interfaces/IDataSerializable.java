package com.wh.gaea.interfaces;

public interface IDataSerializable {
	public String save(Object userData);

	public Object load(String value);

	public IDrawNode newDrawNode(Object userdata);

	public void initDrawNode(IDrawNode node);
}