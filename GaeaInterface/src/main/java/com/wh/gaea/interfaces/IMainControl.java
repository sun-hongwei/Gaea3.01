package com.wh.gaea.interfaces;

import java.awt.Component;
import java.io.File;

import javax.swing.JInternalFrame;

import com.wh.gaea.draw.IUINode;
import com.wh.gaea.form.ChildForm;

import wh.interfaces.IDBConnection;

public interface IMainControl {

	public interface IControl {
		public void onStart(ChildForm subForm, Object param);

		public void onEnd(ChildForm subForm);
	}

	public enum FormType{
		ftWorkflow, ftUI, ftFlow, ftRun, ftCSS, ftAppWorkflow, ftScene, ftReport, ftSystemRegion, ftRequirement, ftRequirementVersion
	}

	void setTitle(String title);

	void openSceneDesign(IDrawNode node);

	void openRunWorkflow(File runFlowFile);

	/**
	 * 打开模块编辑器
	 * @param workflowRelationName 模块关系图名称，格式id.whn
	 * @param selectNodeId 选定的模块id，可以为null
	 */
	void openModelflowRelation(String workflowRelationName, String selectNodeId);
	
	void openFrameNodeEditor();
	
	boolean openModelflowRelation(IDrawNode node, String[] selectNodeIds);

	void openCodeflowRelation(String name, String uiid, String workflowid);

	void openUIBuilder(File file, String controlId) throws Exception;
	
	void openUIBuilder(String uiid, String controlId) throws Exception;

	void openWorkflowRelation(String title, String name);
	
	void openSubWorkflowRelation(IDrawNode node);
	
	void openRequirementBuilder(File file);

	//void openFlowRelation(ISubForm iSubForm, DrawNode node, boolean checkFront);
	
	void updateUIButtonTitle(ChildForm form, String title);

	ChildForm openFrame(Class<? extends ChildForm> formClass, boolean needNew, Object... args);

	void toFront(ChildForm form);

	void selectForm(ChildForm form);

	JInternalFrame[] getForms();
	
	ChildForm getFront();
	
	IDBConnection getDB();
	void setDB(IDBConnection db);
	void openReportEditor();
	
	void openReportEditor(Component editor, IUINode uiNode);
	
	void switchSubForm(ChildForm parent, Class<? extends ChildForm> subFormClass, IControl iControl, Object param)
			throws Exception;

	void initRequirementVersionMenu();

	Integer getSelectDPI();
}