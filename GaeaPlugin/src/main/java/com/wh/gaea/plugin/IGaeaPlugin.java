package com.wh.gaea.plugin;

import javax.swing.JComponent;
import javax.swing.JMenu;

import com.wh.gaea.interfaces.EditorContext;
import com.wh.gaea.interfaces.IClientEvent.ISetValue;

public interface IGaeaPlugin {
	
	public enum PlugInType{
		ptDb, ptWorkflow, ptRole, ptExcel, ptDataSource, ptFunction
	}
	
	PlugInType getType();
	
	int getLoadOrder();
	
	void setMenu(JMenu root);
	
	void reset();

	JMenu getRootMenu(JMenu root);
	
	default JComponent getEditor(EditorContext context) {
		return null;
	}

	default boolean onEditorUpdateValue(EditorContext context, JComponent sender, String name, Object value) {
		return true;
	}

	default void onEditorClick(EditorContext context, JComponent sender, int row, int col, ISetValue onSetValue) {
		
	}

}
