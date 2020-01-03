package com.wh.swing.tools.render;

import java.awt.Component;
import java.awt.Label;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class FieldInfoTableCellRender extends DefaultTableCellRenderer{
	
	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		Label label = (Label) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		return label;
	}
	
}
