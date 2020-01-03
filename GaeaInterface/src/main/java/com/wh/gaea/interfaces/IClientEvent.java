package com.wh.gaea.interfaces;

import javax.swing.JComponent;

public interface IClientEvent {
	public interface ISetValue {
		public void onSetValue(int row, int col, Object value);
	}
	public void onEdit(int row, int col);
	public void onClick(JComponent sender, int row, int col, IClientEvent.ISetValue onSetValue);
	public boolean onUpateValue(JComponent sender, int row, int col, Object value);
}