package com.wh.gaea.plug;

import java.awt.Image;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.wh.gaea.control.EditorEnvironment;
import com.wh.gaea.draw.DrawInfo;
import com.wh.gaea.draw.IUINode;
import com.wh.gaea.draws.AppWorkflowCanvas;
import com.wh.gaea.draws.DrawCanvas;
import com.wh.gaea.draws.DrawNode;
import com.wh.gaea.draws.FlowCanvas;
import com.wh.gaea.draws.FlowNode.StateNode;
import com.wh.gaea.draws.UICanvas;
import com.wh.gaea.draws.UINode;
import com.wh.gaea.draws.WorkflowCanvas;
import com.wh.gaea.draws.WorkflowNode.BeginNode;
import com.wh.gaea.draws.WorkflowNode.ChildWorkflowNode;
import com.wh.gaea.draws.WorkflowNode.EndNode;
import com.wh.gaea.interfaces.ICreateNodeSerializable;
import com.wh.gaea.interfaces.IDrawCanvas;
import com.wh.gaea.interfaces.IDrawNode;
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
import com.wh.gaea.interfaces.IGaeaEditor;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.gaea.interfaces.ResultModelRelationInfo;
import com.wh.gaea.interfaces.selector.IDataSourceSelector;
import com.wh.gaea.interfaces.selector.IExcelSelector;
import com.wh.gaea.interfaces.selector.IRoleSelector;
import com.wh.gaea.interfaces.selector.IWorkflowSelector;

public class GaeaEditorManager implements IGaeaEditor {
	@Override
	public boolean isOpenProject() {
		return EditorEnvironment.isOpenProject();
	}

	@Override
	public File[] getUIFiles() {
		return EditorEnvironment.getUIFiles();
	}

	@Override
	public File[] getAppWorkflowFiles() {
		return EditorEnvironment.getAppWorkflowFiles();
	}

	@Override
	public File[] getRunFlowFiles() {
		return EditorEnvironment.getRunFlowFiles();
	}

	@Override
	public File[] getModelRelationFiles() {
		return EditorEnvironment.getModelRelationFiles();
	}

	@Override
	public File[] getModelNodeFiles() {
		return EditorEnvironment.getModelNodeFiles();
	}

	@Override
	public File[] getProjectFiles(String dirName, String ext) {
		return EditorEnvironment.getProjectFiles(dirName, ext);
	}

	@Override
	public NodeDescInfo getModelNodeDescInfo(String uiid) throws Exception {
		return EditorEnvironment.getModelNodeDescInfo(uiid);
	}

	@Override
	public NodeDescInfo getChildModelNodeDescInfo(String childModelRelationName) throws Exception {
		return EditorEnvironment.getChildModelNodeDescInfo(childModelRelationName);
	}

	@Override
	public NodeDescInfo getModelNodeDescInfo(String modelRelationName, String nodeid) throws Exception {
		return EditorEnvironment.getModelNodeDescInfo(modelRelationName, nodeid);
	}

	@Override
	public NodeDescInfo getNodeDescInfo(File file) throws Exception {
		return EditorEnvironment.getNodeDescInfo(file);
	}

	@Override
	public File getModelFile(String nodeid) {
		return EditorEnvironment.getModelFile(nodeid);
	}

	@Override
	public File getModelRelationFile(String modelRelationName) {
		return EditorEnvironment.getModelRelationFile(modelRelationName);
	}

	@Override
	public String getModelRelationNameFromNodeID(String nodeid) throws Exception {
		return EditorEnvironment.getModelRelationNameFromNodeID(nodeid);
	}

	@Override
	public String getModelRelationName(File modelRelationFile) {
		return EditorEnvironment.getModelRelationName(modelRelationFile);
	}

	@Override
	public String getModelRelationNameFromFileName(String name) {
		return EditorEnvironment.getModelRelationNameFromFileName(name);
	}

	@Override
	public boolean isMainModelRelation(File modelRelationFile) {
		return EditorEnvironment.isMainModelRelation(modelRelationFile);
	}

	@Override
	public File getFileAboutWorkflowNode(String dirName, String nodeid, String key, GetFileNameType ft,
			boolean needSave) {
		return EditorEnvironment.getFileAboutWorkflowNode(dirName, nodeid, key, ft, needSave);
	}

	@Override
	public File setFileAboutWorkflowNode(String dirName, String nodeid, String key, String id, GetFileNameType ft,
			boolean forceSave, boolean needSave) {
		return EditorEnvironment.setFileAboutWorkflowNode(dirName, nodeid, key, id, ft, forceSave, needSave);
	}

	@Override
	public File getFileAboutNode(String dirName, String configDirName, String nodeid, String key, GetFileNameType ft,
			boolean needSave) {
		return EditorEnvironment.getFileAboutNode(dirName, configDirName, nodeid, key, ft, needSave);
	}

	@Override
	public File setFileAboutNode(String dirName, String configDirName, String nodeid, String key, String id,
			GetFileNameType ft, boolean replaceExists, boolean needSave) {
		return EditorEnvironment.setFileAboutNode(dirName, configDirName, nodeid, key, id, ft, replaceExists, needSave);
	}

	@Override
	public File getRunFlowFile(String id) {
		return EditorEnvironment.getRunFlowFile(id);
	}

	@Override
	public File getMainModelRelationFile() {
		return EditorEnvironment.getMainModelRelationFile();
	}

	@Override
	public String getPublish_UI_FileName(String name) {
		return EditorEnvironment.getPublish_UI_FileName(name);
	}

	@Override
	public String getRelationFileName(String name) {
		return EditorEnvironment.getRelationFileName(name);
	}

	@Override
	public String getNodeFileName(String name) {
		return EditorEnvironment.getNodeFileName(name);
	}

	@Override
	public String getApp_FileName(String name) {
		return EditorEnvironment.getApp_FileName(name);
	}

	@Override
	public String getFlow_FileName(String name) {
		return EditorEnvironment.getFlow_FileName(name);
	}

	@Override
	public String getToolbarFileName(String name) {
		return EditorEnvironment.getToolbarFileName(name);
	}

	@Override
	public String getMenu_FileName(String name) {
		return EditorEnvironment.getMenu_FileName(name);
	}

	@Override
	public String getTree_FileName(String name) {
		return EditorEnvironment.getTree_FileName(name);
	}

	@Override
	public String getRunFlow_FileName(String name) {
		return EditorEnvironment.getRunFlow_FileName(name);
	}

	@Override
	public String getUI_FileName(String name) {
		return EditorEnvironment.getUI_FileName(name);
	}

	@Override
	public String getAppWorkflow_FileName(String name) {
		return EditorEnvironment.getAppWorkflow_FileName(name);
	}

	@Override
	public void copyNode(IDrawNode node, File descPath, boolean isLinked) throws Exception {
		EditorEnvironment.copyNode((DrawNode) node, descPath, isLinked);
	}

	@Override
	public File newUniqProjectFile(String dirName, String prefix, String ext) {
		return EditorEnvironment.newUniqProjectFile(dirName, prefix, ext);
	}

	@Override
	public File getEditorSourcePath(String pathname, String name) {
		return EditorEnvironment.getEditorSourcePath(pathname, name);
	}

	@Override
	public File getEditorPath(String pathname, String name) {
		return EditorEnvironment.getEditorPath(pathname, name);
	}

	@Override
	public String getCurrentProjectName() {
		return EditorEnvironment.getCurrentProjectName();
	}

	@Override
	public File getProjectPath(String dirName) {
		return EditorEnvironment.getProjectPath(dirName);
	}

	@Override
	public File getProjectPath(String projectName, String dirName) {
		return EditorEnvironment.getProjectPath(projectName, dirName);
	}

	@Override
	public File getProjectPathForName(String projectName) {
		return EditorEnvironment.getProjectPathForName(projectName);
	}

	@Override
	public File getBasePath(String dirName, String filename) {
		return EditorEnvironment.getBasePath(dirName, filename);
	}

	@Override
	public File getProjectBasePath() {
		return EditorEnvironment.getProjectBasePath();
	}

	@Override
	public List<String> getProjectNames() {
		return EditorEnvironment.getProjectNames();
	}

	@Override
	public List<String> getDispatchNames() {
		return EditorEnvironment.getDispatchNames();
	}

	@Override
	public boolean existsProjectName(String name) {
		return EditorEnvironment.existsProjectName(name);
	}

	@Override
	public boolean newProject(String name, boolean overwrite) throws Exception {
		return EditorEnvironment.newProject(name, overwrite);
	}

	@Override
	public boolean updateFrame() throws Exception {
		return EditorEnvironment.updateFrame();
	}

	@Override
	public boolean updateFrame(String projectName) throws Exception {
		return EditorEnvironment.updateFrame(projectName);
	}

	@Override
	public File getTemplateFile(String dirName, String name) {
		return EditorEnvironment.getTemplateFile(dirName, name);
	}

	@Override
	public File getResourceFile(String dirName, String name) {
		return EditorEnvironment.getResourceFile(dirName, name);
	}

	@Override
	public File getProjectFile(String pathname, String filename) {
		return EditorEnvironment.getProjectFile(pathname, filename);
	}

	@Override
	public File getProjectJSPath(String name) {
		return EditorEnvironment.getProjectJSPath(name);
	}

	@Override
	public File getProjectJSPath() {
		return EditorEnvironment.getProjectJSPath();
	}

	@Override
	public File getProjectImagePath() {
		return EditorEnvironment.getProjectImagePath();
	}

	@Override
	public File getReportFile(String name) {
		return EditorEnvironment.getReportFile(name);
	}

	@Override
	public File getProjectReportPath() {
		return EditorEnvironment.getProjectReportPath();
	}

	@Override
	public File getProjectFile(String projectName, String pathname, String filename) {
		return EditorEnvironment.getProjectFile(projectName, pathname, filename);
	}

	@Override
	public File getModelNodeFile(String nodeid) {
		return EditorEnvironment.getModelNodeFile(nodeid);
	}

	@Override
	public File getPublishWebFile(String pageName) throws Exception {
		return EditorEnvironment.getPublishWebFile(pageName);
	}

	@Override
	public File getPublishWebPath() throws Exception {
		return EditorEnvironment.getPublishWebPath();
	}

	@Override
	public File getPublishWebFile(String dirName, String pageName) throws Exception {
		return EditorEnvironment.getPublishWebFile(dirName, pageName);
	}

	@Override
	public String getWebUrl(String pageName) throws Exception {
		return EditorEnvironment.getWebUrl(pageName);
	}

	@Override
	public String getWebUrl(String projectName, String pageName) throws Exception {
		return EditorEnvironment.getWebUrl(projectName, pageName);
	}

	@Override
	public String formatProjectPath(File projectPath) throws Exception {
		return EditorEnvironment.formatProjectPath(projectPath);
	}

	@Override
	public String getWebRoot() throws Exception {
		return EditorEnvironment.getWebRoot();
	}

	@Override
	public void setWebRoot(File path) {
		EditorEnvironment.setWebRoot(path);
	}

	@Override
	public String getDataServiceRoot() throws Exception {
		return EditorEnvironment.getDataServiceRoot();
	}

	@Override
	public void setDataServiceRoot(String url) {
		EditorEnvironment.setDataServiceRoot(url);
	}

	@Override
	public boolean traverseModel(File relationFile, ITraverseDrawNode onTraverseDrawNode, Object param)
			throws Exception {
		return EditorEnvironment.traverseModel(relationFile, onTraverseDrawNode, param);
	}

	@Override
	public void traverseModel(ITraverseDrawNode onTraverseModel, Object param) throws Exception {
		EditorEnvironment.traverseModel(onTraverseModel, param);
	}

	@Override
	public boolean traverseNavRegion(RegionName regionName, ITraverseDrawNode onTraverseModel, Object param)
			throws Exception {
		return EditorEnvironment.traverseNavRegion(regionName, onTraverseModel, param);
	}

	@Override
	public void traverseModelAndNavs(ITraverseDrawNode onTraverseModel, Object param) throws Exception {
		EditorEnvironment.traverseModelAndNavs(onTraverseModel, param);
	}

	@Override
	public boolean existsModelNodeName(String name) throws Exception {
		return EditorEnvironment.existsModelNodeName(name);
	}

	@Override
	public File getFrameModelNodeFile(RegionName rName) {
		return EditorEnvironment.getFrameModelNodeFile(rName);
	}

	@Override
	public void saveFrameModelNode(IDrawNode node) throws Exception {
		EditorEnvironment.saveFrameModelNode((DrawNode) node);
	}

	@Override
	public IDrawNode[] getFrameModelNodes() throws Exception {
		return EditorEnvironment.getFrameModelNodes();
	}

	@Override
	public IDrawNode getFrameModelNode(RegionName rName) throws Exception {
		return EditorEnvironment.getFrameModelNode(rName);
	}

	@Override
	public File getParentModelRelationFile(String filname) throws Exception {
		return EditorEnvironment.getParentModelRelationFile(filname);
	}

	@Override
	public List<ResultModelRelationInfo> getParentModelRelationFiles(File modelRelationFile) throws Exception {
		return EditorEnvironment.getParentModelRelationFiles(modelRelationFile);
	}

	@Override
	public boolean checkExistsModelNodeName(ICheckCallBack onCheckCallBack) throws Exception {
		return EditorEnvironment.checkExistsModelNodeName(onCheckCallBack);
	}

	@Override
	public boolean traverseUI(File uiFile, ITraverseDrawNode onTraverseDrawNode, Object param) throws Exception {
		return EditorEnvironment.traverseUI(uiFile, onTraverseDrawNode, param);
	}

	@Override
	public boolean traverseUI(ITraverseDrawNode onTraverseUI, Object param) throws Exception {
		return EditorEnvironment.traverseUI(onTraverseUI, param);
	}

	@Override
	public boolean traverseUI(ITraverseDrawNode onTraverseUI, Object param, boolean onlyFile) throws Exception {
		return EditorEnvironment.traverseUI(onTraverseUI, param, onlyFile);
	}

	@Override
	public boolean traverseUI(ITraverseUIFile onTraverseUIFile, Object userObject) throws Exception {
		return EditorEnvironment.traverseUI(onTraverseUIFile, userObject);
	}

	@Override
	public boolean existsUINodeName(String name) throws Exception {
		return EditorEnvironment.existsModelNodeName(name);
	}

	@Override
	public ModelNodeInfo getModelInfoFromUI(File uiFile) throws Exception {
		return EditorEnvironment.getModelInfoFromUI(uiFile);
	}

	@Override
	public File getFrameNodeLockFile() {
		return EditorEnvironment.getFrameNodeLockFile();
	}

	@Override
	public IDrawNode getModelNodeFromUI(String uiid) throws Exception {
		return EditorEnvironment.getModelNodeFromUI(uiid);
	}

	@Override
	public IDrawNode getModelNodeFromUI(File uiFile) throws Exception {
		return EditorEnvironment.getModelNodeFromUI(uiFile);
	}

	@Override
	public IDrawNode getChildModelNodeFromFile(File childModelRelationFile) throws Exception {
		return EditorEnvironment.getChildModelNodeFromFile(childModelRelationFile);
	}

	@Override
	public File getModelRelationFileFromNodeID(String nodeid) throws Exception {
		return EditorEnvironment.getModelRelationFileFromNodeID(nodeid);
	}

	@Override
	public File getModelRelationFileFromNodeName(String nodeName) throws Exception {
		return EditorEnvironment.getModelRelationFileFromNodeName(nodeName);
	}

	@Override
	public boolean checkExistsUINodeName(ICheckCallBack onCheckCallBack, CheckType ct) throws Exception {
		return EditorEnvironment.checkExistsModelNodeName(onCheckCallBack);
	}

	@Override
	public boolean publish(IPublish publishEvent, boolean needResource, boolean needFrame, boolean needUserJS,
			boolean needReport, boolean needMainMenu, boolean needMainNav, boolean needDataSource,
			boolean needRemoteAuth) {
		return EditorEnvironment.publish(publishEvent, needResource, needFrame, needUserJS, needReport, needMainMenu,
				needMainNav, needDataSource, needRemoteAuth);
	}

	@Override
	public boolean publishModelNode(IPublish publishEvent, Collection<IDrawNode> nodes, boolean needResource,
			boolean needUserJS, boolean needReport, boolean needDataSource) {
		return EditorEnvironment.publishModelNode(publishEvent, nodes, needResource, needUserJS, needReport,
				needDataSource);
	}

	@Override
	public boolean publishModelNode(IPublish publishEvent, String[] modelNodeNames, boolean needResource,
			boolean needUserJS, boolean needReport, boolean needDataSource) {
		return EditorEnvironment.publishModelNode(publishEvent, modelNodeNames, needResource, needUserJS, needReport,
				needDataSource);
	}

	@Override
	public JSONObject getMainNavData() {
		return EditorEnvironment.getMainNavData();
	}

	@Override
	public File getMainMenuFile() {
		return EditorEnvironment.getMainMenuFile();
	}

	@Override
	public JSONObject getMainManuData() {
		return EditorEnvironment.getMainManuData();
	}

	@Override
	public String getUIID(String nodeid) {
		return EditorEnvironment.getUIID(nodeid);
	}

	@Override
	public String getModelNodeName(String nodeid) throws Exception {
		return EditorEnvironment.getModelNodeName(nodeid);
	}

	@Override
	public HashMap<String, String> getModelNameAndIds() {
		return EditorEnvironment.getModelNameAndIds();
	}

	@Override
	public File getAppFile(String nodeid, boolean allowNew) {
		return EditorEnvironment.getAppFile(nodeid, allowNew);
	}

	@Override
	public String getChildModelRelationName(String nodeid) {
		return EditorEnvironment.getChildModelRelationName(nodeid);
	}

	@Override
	public File getChildModelRelationFile(String childNodeId, boolean allowNew) {
		return EditorEnvironment.getChildModelRelationFile(childNodeId, allowNew);
	}

	@Override
	public HashMap<String, String> getFlowRelationNames() {
		return EditorEnvironment.getFlowRelationNames();
	}

	@Override
	public File getFlowRelationFile(String name) {
		return EditorEnvironment.getFlowRelationFile(name);
	}

	@Override
	public File getRunWorkflowFile(String runWordflowRelationName) {
		return EditorEnvironment.getRunWorkflowFile(runWordflowRelationName);
	}

	@Override
	public File getToolbarFile(String nodeid, boolean allowNew) {
		return EditorEnvironment.getToolbarFile(nodeid, allowNew);
	}

	@Override
	public File getUIFileForUIID(String uiid) {
		return EditorEnvironment.getUIFileForUIID(uiid);
	}

	@Override
	public File getUIFile(String nodeid, boolean allowNew) {
		return EditorEnvironment.getUIFile(nodeid, allowNew);
	}

	@Override
	public File getProjectMetaFile() {
		return EditorEnvironment.getProjectMetaFile();
	}

	@Override
	public File getMainNavTreeFile() {
		return EditorEnvironment.getMainNavTreeFile();
	}

	@Override
	public void openHelp(String name) {
		EditorEnvironment.openHelp(name);
	}

	@Override
	public void mergeProject(String projectName, boolean mergeMainMenu, boolean mergeMainNav,
			boolean mergeImage, boolean mergeHeader, boolean mergeFooter, boolean mergeLeftNav, boolean mergeRightNav,
			boolean mergeReport) {
		EditorEnvironment.mergeProject(projectName, mergeMainMenu, mergeMainNav, mergeImage, mergeHeader, mergeFooter,
				mergeLeftNav, mergeRightNav, mergeReport);
	}

	@Override
	public void importDispatchProject(String projectName, IDispatchCallback onCallback) {
		EditorEnvironment.importDispatchProject(projectName, onCallback);
	}

	@Override
	public boolean lockFile(File f) {
		return EditorEnvironment.lockFile(f);
	}

	@Override
	public void unlockFile(File f) {
		EditorEnvironment.unlockFile(f);
	}

	@Override
	public void reset(Class<?> c) {
		PluginManager.reset();
	}

	@Override
	public void reset() {
		PluginManager.reset();
	}

	@Override
	public IDrawCanvas createAppWorkflowCanvas() {
		return new AppWorkflowCanvas();
	}

	@Override
	public IDrawCanvas createUICanvas() {
		return new UICanvas();
	}

	static GaeaEditorManager gaeaEditorManager;

	public static GaeaEditorManager instance() {
		if (gaeaEditorManager == null)
			gaeaEditorManager = new GaeaEditorManager();
		return gaeaEditorManager;
	}

	@Override
	public IUINode createUINode(IDrawCanvas canvas) {
		return new UINode((DrawCanvas) canvas);
	}

	@Override
	public DrawInfo createDrawInfo(String typename, IUINode node) {
		return UINode.newInstance(typename, node);
	}

	@Override
	public Image getUIControlImage(String typename) {
		return UINode.getImage(typename);
	}

	@Override
	public IDrawNode findStartNode(IDrawCanvas canvas) {
		for (IDrawNode node : canvas.getNodes()) {
			if (node instanceof EndNode)
				continue;
			if (node instanceof BeginNode)
				return (BeginNode) node;
		}
		return null;
	}

	public IDrawNode findEndNode(IDrawCanvas canvas) {
		for (IDrawNode node : canvas.getNodes()) {
			if (node instanceof EndNode)
				return (EndNode) node;
		}
		return null;
	}

	@Override
	public boolean isModelStartNode(IDrawNode node) {
		return node instanceof BeginNode;
	}

	@Override
	public boolean isModelEndNode(IDrawNode node) {
		return node instanceof EndNode;
	}

	@Override
	public boolean isModelChildNode(IDrawNode node) {
		return node instanceof ChildWorkflowNode;
	}

	@Override
	public boolean isModelChildNodeClass(Class<?> c) {
		return ChildWorkflowNode.class.equals(c);
	}

	@Override
	public IDrawCanvas createWorkfowCanvas() {
		return new WorkflowCanvas();
	}

	@Override
	public String getMainModelRelationFileName() {
		return EditorEnvironment.getMainModelRelationFileName();
	}

	@Override
	public IDrawCanvas createFlowCanvas() {
		return new FlowCanvas();
	}

	@Override
	public ICreateNodeSerializable createDefaultWorkflowDeserializable() {
		return new EditorEnvironment.WorkflowDeserializable();
	}

	IDataSourceSelector datasourceSelector;

	@Override
	public IDataSourceSelector getDataSourceSelector() {
		return datasourceSelector;
	}

	@Override
	public void setDataSourceSelector(IDataSourceSelector selector) {
		datasourceSelector = selector;
	}

	IRoleSelector roleSelector;

	@Override
	public IRoleSelector getRoleSelector() {
		return roleSelector;
	}

	@Override
	public void setRoleSelector(IRoleSelector selector) {
		roleSelector = selector;
	}

	IWorkflowSelector workflowSelector;

	@Override
	public IWorkflowSelector getWorkflowSelector() {
		return workflowSelector;
	}

	@Override
	public void setWorkflowSelector(IWorkflowSelector selector) {
		workflowSelector = selector;
	}

	@Override
	public String getWorkflowState(IDrawNode node) {
		if (node instanceof StateNode)
			return ((StateNode) node).state;
		else {
			return null;
		}
	}

	@Override
	public ICreateNodeSerializable createNullDeserializable() {
		return new EditorEnvironment.NullDeserializable();
	}

	IExcelSelector execlSelector;

	@Override
	public IExcelSelector getExcelSelector() {
		return execlSelector;
	}

	@Override
	public void setExcelSelector(IExcelSelector selector) {
		execlSelector = selector;
	}

	@Override
	public boolean isUIDrawInfo(IDrawNode node, String typeName) {
		if (!(node instanceof UINode) || ((UINode) node).getDrawInfo() == null)
			return false;

		DrawInfo info = ((UINode) node).getDrawInfo();
		return info.typeName().equalsIgnoreCase(typeName);
	}

	IMainControl mainControl;

	@Override
	public IMainControl getMainControl() {
		return mainControl;
	}

	@Override
	public void setMainControl(IMainControl mainControl) {
		this.mainControl = mainControl;
	}

	@Override
	public <T> T getPlugin(Class<T> c) {
		return PluginManager.getPlugin(c);
	}

}
