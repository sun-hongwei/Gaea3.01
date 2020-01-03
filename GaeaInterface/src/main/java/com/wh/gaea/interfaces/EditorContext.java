package com.wh.gaea.interfaces;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import com.wh.gaea.datasource.DataSource;

public class EditorContext {
	public Object node;
	public String name;
	public Object value;
	public String title;
	public JComponent editor;
	public IDrawNode workflowNode;
	public HashMap<String, IDrawNode> workflowNodes;
	public String workflowRelationTitle;
	public Map<String, DataSource> dataSources = new HashMap<>();
}
