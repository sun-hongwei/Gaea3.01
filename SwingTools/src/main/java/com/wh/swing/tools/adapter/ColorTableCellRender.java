package com.wh.swing.tools.adapter;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ColorTableCellRender extends DefaultTableCellRenderer{

	private static final long serialVersionUID = 1L;

	Map<Integer, Color> colColors = new HashMap<>();
	Map<Integer, Color> rowColors = new HashMap<>();
	
	public void resetColColor() {
		colColors.clear();
	}
	
	public void resetRowColor() {
		rowColors.clear();
	}
	
	public void setColColor(int col, Color color) {
		colColors.put(col, color);
	}
	
	public void setRowColor(int row, Color color) {
		rowColors.put(row, color);
	}
	
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
    	JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    	
    	Color color = Color.WHITE;
    	if (rowColors.containsKey(row)) {
    		color = rowColors.get(row);
    	}
    	
    	if (colColors.containsKey(column)) {
    		color = colColors.get(column);
    	}
    	
    	if (isSelected || table.getSelectedRow() == row) 
    		color = Color.BLUE;

    	if (color != null)
    		label.setBackground(color);
    	
    	return label;
    }
}
