package com.wh.gaea.interfaces;

public interface INode {
	public void Click(IDrawNode node);

	public void DoubleClick(IDrawNode node);

	public void onChange(IDrawNode[] nodes, ChangeType ct);

	default void onAdvanChange(IDrawNode node, ChangeType ct, Object data) {

	}
}