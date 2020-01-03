package com.wh.gaea.interfaces;

import java.util.HashMap;

public interface IMainMenuOperation {
	public void onSave();

	public void onLoad();

	public void onClose();
	
	void onPublish(HashMap<String, IDrawNode> uikeysWorkflowNodes, Object param) throws Exception;
}
