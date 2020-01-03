package com.wh.gaea.dialog;

import com.wh.gaea.draws.DrawNode;
import com.wh.gaea.interfaces.ISubForm;

public interface IEditNode{
	public void onEditUI(ISubForm iSubForm, DrawNode node);
	public void onEditSubWorkflow(ISubForm iSubForm, DrawNode node);
}


