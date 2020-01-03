package com.wh.gaea.interfaces;

import java.awt.Image;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.wh.gaea.draw.DrawInfo;
import com.wh.gaea.draw.IUINode;
import com.wh.gaea.interfaces.IEditorInterface.CheckType;
import com.wh.gaea.interfaces.IEditorInterface.GetFileNameType;
import com.wh.gaea.interfaces.IEditorInterface.ICheckCallBack;
import com.wh.gaea.interfaces.IEditorInterface.IDispatchCallback;
import com.wh.gaea.interfaces.IEditorInterface.IPublish;
import com.wh.gaea.interfaces.IEditorInterface.ITraverseDrawNode;
import com.wh.gaea.interfaces.IEditorInterface.ITraverseUIFile;
import com.wh.gaea.interfaces.IEditorInterface.ModelNodeInfo;
import com.wh.gaea.interfaces.IEditorInterface.NodeDescInfo;
import com.wh.gaea.interfaces.IEditorInterface.RegionName;
import com.wh.gaea.interfaces.selector.IDataSourceSelector;
import com.wh.gaea.interfaces.selector.IExcelSelector;
import com.wh.gaea.interfaces.selector.IRoleSelector;
import com.wh.gaea.interfaces.selector.IWorkflowSelector;

public interface IGaeaEditor {
	boolean isOpenProject() ;
	File[] getUIFiles();
	File[] getAppWorkflowFiles();
	File[] getRunFlowFiles();
	File[] getModelRelationFiles();
	File[] getModelNodeFiles();
	File[] getProjectFiles(String dirName, String ext);
	NodeDescInfo getModelNodeDescInfo(String uiid) throws Exception;
	NodeDescInfo getChildModelNodeDescInfo(String childModelRelationName) throws Exception;
	NodeDescInfo getModelNodeDescInfo(String modelRelationName, String nodeid) throws Exception;
	NodeDescInfo getNodeDescInfo(File file) throws Exception;
	File getModelFile(String nodeid);
	File getModelRelationFile(String modelRelationName);
	String getModelRelationNameFromNodeID(String nodeid) throws Exception;
	String getModelRelationName(File modelRelationFile);
	String getModelRelationNameFromFileName(String name);
	boolean isMainModelRelation(File modelRelationFile);
	File getFileAboutWorkflowNode(String dirName, String nodeid, String key, GetFileNameType ft,
			boolean needSave) ;
	File setFileAboutWorkflowNode(String dirName, String nodeid, String key, String id,
			GetFileNameType ft, boolean forceSave, boolean needSave);
	File getFileAboutNode(String dirName, String configDirName, String nodeid, String key,
			GetFileNameType ft, boolean needSave) ;
	File setFileAboutNode(String dirName, String configDirName, String nodeid, String key, String id,
			GetFileNameType ft, boolean replaceExists, boolean needSave);
	File getRunFlowFile(String id);
	File getMainModelRelationFile();
	String getPublish_UI_FileName(String name);
	String getRelationFileName(String name);
	String getNodeFileName(String name) ;
	String getApp_FileName(String name);

	String getFlow_FileName(String name) ;
	String getToolbarFileName(String name);

	String getMenu_FileName(String name) ;

	String getTree_FileName(String name);

	String getRunFlow_FileName(String name) ;
	String getUI_FileName(String name);
	String getAppWorkflow_FileName(String name);

	void copyNode(IDrawNode node, File descPath, boolean isLinked) throws Exception;
	File newUniqProjectFile(String dirName, String prefix, String ext);
	File getEditorSourcePath(String pathname, String name) ;
	File getEditorPath(String pathname, String name);
	String getCurrentProjectName();
	File getProjectPath(String dirName) ;
	File getProjectPath(String projectName, String dirName);
	File getProjectPathForName(String projectName) ;
	File getBasePath(String dirName, String filename) ;
	File getProjectBasePath() ;
	List<String> getProjectNames() ;
	List<String> getDispatchNames() ;
	boolean existsProjectName(String name) ;
	boolean newProject(String name, boolean overwrite) throws Exception;
	boolean updateFrame() throws Exception ;
	boolean updateFrame(String projectName) throws Exception;
	File getTemplateFile(String dirName, String name);
	File getResourceFile(String dirName, String name);
	File getProjectFile(String pathname, String filename);
	File getProjectJSPath(String name);
	File getProjectJSPath() ;
	File getProjectImagePath() ;

	File getReportFile(String name);
	File getProjectReportPath();
	File getProjectFile(String projectName, String pathname, String filename);
	File getModelNodeFile(String nodeid);
	File getPublishWebFile(String pageName) throws Exception ;
	File getPublishWebPath() throws Exception;
	File getPublishWebFile(String dirName, String pageName) throws Exception;
	String getWebUrl(String pageName) throws Exception ;
	String getWebUrl(String projectName, String pageName) throws Exception ;
	String formatProjectPath(File projectPath) throws Exception ;
	String getWebRoot() throws Exception;
	void setWebRoot(File path);
	String getDataServiceRoot() throws Exception;
	void setDataServiceRoot(String url);
	boolean traverseModel(File relationFile, ITraverseDrawNode onTraverseDrawNode, Object param)
			throws Exception ;
	void traverseModel(ITraverseDrawNode onTraverseModel, Object param) throws Exception;

	boolean traverseNavRegion(RegionName regionName, ITraverseDrawNode onTraverseModel, Object param)
			throws Exception ;
	void traverseModelAndNavs(ITraverseDrawNode onTraverseModel, Object param) throws Exception ;
	boolean existsModelNodeName(String name) throws Exception ;
	File getFrameModelNodeFile(RegionName rName) ;
	void saveFrameModelNode(IDrawNode node) throws Exception ;
	IDrawNode[] getFrameModelNodes() throws Exception ;
	IDrawNode getFrameModelNode(RegionName rName) throws Exception;

	File getParentModelRelationFile(String filname) throws Exception;

	List<ResultModelRelationInfo> getParentModelRelationFiles(File modelRelationFile) throws Exception;
	boolean checkExistsModelNodeName(ICheckCallBack onCheckCallBack) throws Exception ;
	boolean traverseUI(File uiFile, ITraverseDrawNode onTraverseDrawNode, Object param) throws Exception ;
	boolean traverseUI(ITraverseDrawNode onTraverseUI, Object param) throws Exception;
	boolean traverseUI(ITraverseDrawNode onTraverseUI, Object param, boolean onlyFile) throws Exception;
	boolean traverseUI(ITraverseUIFile onTraverseUIFile, Object userObject) throws Exception ;
	boolean existsUINodeName(String name) throws Exception ;

	ModelNodeInfo getModelInfoFromUI(File uiFile) throws Exception ;
	
	File getFrameNodeLockFile() ;
	IDrawNode getModelNodeFromUI(String uiid) throws Exception ;
	IDrawNode getModelNodeFromUI(File uiFile) throws Exception ;
	IDrawNode getChildModelNodeFromFile(File childModelRelationFile) throws Exception ;
	File getModelRelationFileFromNodeID(String nodeid) throws Exception ;
	File getModelRelationFileFromNodeName(String nodeName) throws Exception ;

	boolean checkExistsUINodeName(ICheckCallBack onCheckCallBack, CheckType ct) throws Exception;
	boolean publish(IPublish publishEvent, boolean needResource, boolean needFrame, boolean needUserJS,
			boolean needReport, boolean needMainMenu, boolean needMainNav, boolean needDataSource,
			boolean needRemoteAuth) ;

	boolean publishModelNode(IPublish publishEvent, Collection<IDrawNode> nodes, boolean needResource,
			boolean needUserJS, boolean needReport, boolean needDataSource);
	boolean publishModelNode(IPublish publishEvent, String[] modelNodeNames, boolean needResource,
			boolean needUserJS, boolean needReport, boolean needDataSource) ;
	JSONObject getMainNavData();
	File getMainMenuFile() ;
	JSONObject getMainManuData();
	String getUIID(String nodeid) ;
	String getModelNodeName(String nodeid) throws Exception;

	HashMap<String, String> getModelNameAndIds();
	File getAppFile(String nodeid, boolean allowNew);
	String getChildModelRelationName(String nodeid);
	File getChildModelRelationFile(String childNodeId, boolean allowNew) ;
	HashMap<String, String> getFlowRelationNames() ;
	File getFlowRelationFile(String name) ;
	File getRunWorkflowFile(String runWordflowRelationName);
	File getToolbarFile(String nodeid, boolean allowNew) ;
	File getUIFileForUIID(String uiid) ;
	File getUIFile(String nodeid, boolean allowNew) ;
	File getProjectMetaFile() ;
	File getMainNavTreeFile();
	
	void reset(Class<?> c);
	void reset();
	
	void openHelp(String name) ;
	void importDispatchProject(String projectName, IDispatchCallback onCallback) ;
	
	boolean lockFile(File f);
	void unlockFile(File f);
	
	IDrawCanvas createAppWorkflowCanvas();
	IDrawCanvas createUICanvas();
	IDrawCanvas createWorkfowCanvas();
	IDrawCanvas createFlowCanvas();
	
	IUINode createUINode(IDrawCanvas canvas);
	
	DrawInfo createDrawInfo(String typename, IUINode node);

	Image getUIControlImage(String typename);
	
	IMainControl getMainControl();
	void setMainControl(IMainControl mainControl);
	
	IDrawNode findStartNode(IDrawCanvas canvas);
	IDrawNode findEndNode(IDrawCanvas canvas);
	
	boolean isModelStartNode(IDrawNode node);
	boolean isModelEndNode(IDrawNode node);
	
	boolean isModelChildNode(IDrawNode node);
	boolean isModelChildNodeClass(Class<?> c);
	
	boolean isUIDrawInfo(IDrawNode node, String typeName);
	
	ICreateNodeSerializable createDefaultWorkflowDeserializable();
	ICreateNodeSerializable createNullDeserializable();
	
	String getMainModelRelationFileName();
	
	IDataSourceSelector getDataSourceSelector();
	void setDataSourceSelector(IDataSourceSelector selector);
	
	IRoleSelector getRoleSelector();
	void setRoleSelector(IRoleSelector selector);
	
	IWorkflowSelector getWorkflowSelector();
	void setWorkflowSelector(IWorkflowSelector selector);
	
	IExcelSelector getExcelSelector();
	void setExcelSelector(IExcelSelector selector);
	
	String getWorkflowState(IDrawNode node);
	
	<T> T getPlugin(Class<T> c);
	void mergeProject(String projectName, boolean mergeMainMenu, boolean mergeMainNav, boolean mergeImage,
			boolean mergeHeader, boolean mergeFooter, boolean mergeLeftNav, boolean mergeRightNav, boolean mergeReport);
}