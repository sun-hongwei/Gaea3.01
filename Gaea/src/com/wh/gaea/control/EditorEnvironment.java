package com.wh.gaea.control;

import java.awt.Desktop;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import com.wh.gaea.control.EditorEnvironment.SharedLock.ICallback;
import com.wh.gaea.draws.DrawCanvas;
import com.wh.gaea.draws.DrawCanvas.PageConfig;
import com.wh.gaea.draws.DrawNode;
import com.wh.gaea.draws.FlowCanvas;
import com.wh.gaea.draws.UICanvas;
import com.wh.gaea.draws.UINode;
import com.wh.gaea.draws.WorkflowCanvas;
import com.wh.gaea.draws.WorkflowNode;
import com.wh.gaea.draws.WorkflowNode.BeginNode;
import com.wh.gaea.draws.WorkflowNode.ChildWorkflowNode;
import com.wh.gaea.draws.WorkflowNode.EndNode;
import com.wh.gaea.editor.JsonTreeDataEditor;
import com.wh.gaea.form.Defines;
import com.wh.gaea.form.SceneBuilder;
import com.wh.gaea.interfaces.ICreateNodeSerializable;
import com.wh.gaea.interfaces.IDataSerializable;
import com.wh.gaea.interfaces.IDrawCanvas;
import com.wh.gaea.interfaces.IDrawNode;
import com.wh.gaea.interfaces.IEditorEnvironment;
import com.wh.gaea.interfaces.IEditorInterface.GetFileNameType;
import com.wh.gaea.interfaces.IEditorInterface.ICheckCallBack;
import com.wh.gaea.interfaces.IEditorInterface.IDispatchCallback;
import com.wh.gaea.interfaces.IEditorInterface.IPublish;
import com.wh.gaea.interfaces.IEditorInterface.ITraverseDrawNode;
import com.wh.gaea.interfaces.IEditorInterface.ITraverseUIFile;
import com.wh.gaea.interfaces.IEditorInterface.ModelNodeInfo;
import com.wh.gaea.interfaces.IEditorInterface.NodeDescInfo;
import com.wh.gaea.interfaces.IEditorInterface.RegionName;
import com.wh.gaea.interfaces.ILoad;
import com.wh.gaea.interfaces.ResultModelRelationInfo;
import com.wh.gaea.tools.FileCache;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.dialog.WaitDialog;
import com.wh.swing.tools.dialog.WaitDialog.IProcess;
import com.wh.tools.FileHelp;
import com.wh.tools.HostInfoHelp;
import com.wh.tools.JsonHelp;
import com.wh.tools.PinyinHelp;
import com.wh.tools.XmlDom;

public class EditorEnvironment implements IEditorEnvironment {
	static final SharedLock fileLockObject = new SharedLock();

	public static boolean lockFile(File f) {
		return fileLockObject.lockFile(f);
	}

	public static void unlockFile(File f) {
		fileLockObject.unlockFile(f);
	}

	public static void shareAccessFile(File f, ICallback onCall) {
		fileLockObject.shareAccessFile(f, onCall);
	}

	public static String getMainModelRelationFileName() {
		return getRelationFileName(Main_Workflow_Relation_FileName);
	}

	public static class SharedLock {
		public static String getFileFullName(File f) {
			String string = f.getAbsolutePath();
			string = string.replace(File.pathSeparator, "");
			string = string.replace(File.separator, "");
			string = string.replace(":", "");
			return string;
		}

		protected HashMap<File, File> locks = new HashMap<>();

		public interface ICallback {
			void onProc();
		}

		public boolean shareAccessFile(File f, ICallback onCallback) {
			if (f.isDirectory())
				return true;

			try (FileOutputStream outputStream = new FileOutputStream(f);
					FileLock fileLock = outputStream.getChannel().tryLock();) {

				if (!fileLock.isValid())
					return false;

				if (onCallback != null) {
					onCallback.onProc();
				}

				fileLock.release();

				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		public boolean lockFile(File f) {
			if (f.isDirectory())
				return true;

			if (!FileCache.isNetFile(f))
				return true;
			synchronized (locks) {
				if (locks.containsKey(f))
					return true;

				File lockFile = EditorEnvironment.getProjectFile(Lock_Dir_Path, getFileFullName(f));
				try {
					boolean needRead = lockFile.exists();
					if (needRead) {
						JSONObject value = (JSONObject) JsonHelp.parseJson(lockFile, null, false);
						if (value.has("host")) {
							if (value.getString("host").compareToIgnoreCase(HostInfoHelp.getComputerName()) == 0) {
								locks.put(f, lockFile);
								return true;
							}
						}
						return false;
					} else {
						JSONObject value = new JSONObject();
						value.put("host", HostInfoHelp.getComputerName());
						JsonHelp.saveJson(lockFile, value, null, false);
					}
					locks.put(f, lockFile);
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}
		}

		public void unlockFile(File f) {
			synchronized (locks) {
				if (!locks.containsKey(f))
					return;

				File raf = locks.remove(f);
				if (raf.exists())
					raf.delete();
			}
		}
	}

	@SuppressWarnings({ "rawtypes" })
	public static void expandAll(JTree tree, TreePath parent, boolean expand) {
		if (parent == null) {
			parent = new TreePath((DefaultMutableTreeNode) tree.getModel().getRoot());
		}
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path, expand);
			}
		}
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}

	public static void fixNode(HashMap<String, DrawNode> nodes, List<String> idList, String fixID) {
		fixNode(nodes, idList, fixID, true);
	}

	public static void fixNode(HashMap<String, DrawNode> nodes, List<String> idList, String fixID, boolean needRemove) {
		boolean needFix = false;
		for (String id : new ArrayList<>(idList)) {
			if (!nodes.containsKey(id)) {
				needFix = true;
				if (needRemove)
					idList.remove(id);
			}
		}

		if (needFix && fixID != null && !fixID.isEmpty()) {
			idList.add(fixID);
		}
	}

	public static class WorkflowDeserializable implements ICreateNodeSerializable {

		@Override
		public IDataSerializable getUserDataSerializable(IDrawNode node) {
			return new WorkflowSerializable();
		}

		@Override
		public DrawNode newDrawNode(JSONObject json) {
			return new WorkflowNode(null);
		}
	}

	public static class NullDeserializable implements ICreateNodeSerializable {

		@Override
		public IDataSerializable getUserDataSerializable(IDrawNode node) {
			return new WorkflowSerializable();
		}

		@Override
		public DrawNode newDrawNode(JSONObject json) {
			return null;
		}
	}

	public static class WorkflowSerializable implements IDataSerializable {
		@Override
		public String save(Object userData) {
			return (String) userData;
		}

		@Override
		public Object load(String value) {
			if (value == null || value.isEmpty())
				return null;

			return value;
		}

		@Override
		public DrawNode newDrawNode(Object userdata) {
			return new WorkflowNode(null);
		}

		@Override
		public void initDrawNode(IDrawNode node) {
			DrawCanvas canvas = (DrawCanvas) node.getCanvas();
			Point pt = canvas.getRealPoint(new Point(canvas.getWidth() / 2, canvas.getHeight() / 2));
			node.invalidRect(pt);
		}
	}

	public static boolean isOpenProject() {
		return currentProjectName != null && !currentProjectName.isEmpty();
	}

	public static File[] getUIFiles() {
		return getProjectFiles(UI_Dir_Name, UI_File_Extension);
	}

	public static File[] getAppWorkflowFiles() {
		return getProjectFiles(AppWorkflow_Dir_Name, AppWorkflow_File_Extension);
	}

	public static File[] getRunFlowFiles() {
		return getProjectFiles(RunFlow_Dir_Name, RunFlow_File_Extension);
	}

	public static File[] getModelRelationFiles() {
		return getProjectFiles(Workflow_Dir_Name, Relation_File_Extension);
	}

	public static File[] getModelNodeFiles() {
		return getProjectFiles(Workflow_Dir_Name, Node_File_Extension);
	}

	public static File[] getProjectFiles(String dirName, String ext) {
		if (!isOpenProject())
			return null;

		File path = getProjectPath(getCurrentProjectName(), dirName);
		if (path == null)
			return null;

		File[] files = path.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && pathname.getName().indexOf(ext) != -1;
			}
		});

		return files;
	}

	public static NodeDescInfo getModelNodeDescInfo(String uiid) throws Exception {
		AtomicReference<NodeDescInfo> result = new AtomicReference<NodeDescInfo>(null);
		traverseModel(new ITraverseDrawNode() {

			@Override
			public boolean onNode(File file, String workflowRelationName, IDrawNode node, Object param) {
				try {
					String node_uiid = getUIID(node.getId());
					if (node_uiid != null && uiid != null && node_uiid.compareTo(uiid) == 0) {
						NodeDescInfo info = getModelNodeDescInfo(workflowRelationName, node.getId());
						result.set(info);
						return false;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}
		}, null);
		return result.get();
	}

	public static NodeDescInfo getChildModelNodeDescInfo(String childModelRelationName) throws Exception {
		File parentFile = getParentModelRelationFile(childModelRelationName);
		if (parentFile == null)
			return null;

		IDrawNode node = getChildModelNodeFromFile(getModelNodeFile(FileHelp.removeExt(childModelRelationName)));
		String nodeid = FileHelp.removeExt(childModelRelationName);
		if (node != null)
			nodeid = node.getId();

		return getModelNodeDescInfo(parentFile.getName(), nodeid);
	}

	public static NodeDescInfo getModelNodeDescInfo(String modelRelationName, String nodeid) throws Exception {
		if (modelRelationName.indexOf('.') == -1)
			modelRelationName = getRelationFileName(modelRelationName);
		List<IDrawNode> nodes = DrawCanvas.loadNodes(null, getProjectFile(Workflow_Dir_Name, modelRelationName),
				new WorkflowDeserializable(), null, null, null);

		if (nodes == null)
			return null;

		for (IDrawNode inode : nodes) {
			DrawNode node = (DrawNode) inode;
			if (node.id.compareTo(nodeid) == 0) {
				NodeDescInfo info = new NodeDescInfo();
				info.workflowRelationName = FileHelp.removeExt(modelRelationName);
				info.id = node.id;
				info.title = node.title;
				info.memo = node.memo;
				return info;
			}
		}

		return null;
	}

	public static NodeDescInfo getNodeDescInfo(File file) throws Exception {

		if (file == null || !file.exists())
			return null;

		IDrawCanvas canvas = new DrawCanvas();
		JSONObject json = (JSONObject) JsonHelp.parseCacheJson(file, null);
		canvas.getPageConfig().fromJson(json);

		NodeDescInfo info = new NodeDescInfo();
		info.id = file.getName();
		int index = info.id.indexOf(".");
		info.id = info.id.substring(0, index);
		info.title = canvas.getPageConfig().getTitle();
		info.memo = canvas.getPageConfig().getMemo();

		return info;
	}

	public static File getModelFile(String nodeid) {
		return getProjectFile(Workflow_Dir_Name, getNodeFileName(nodeid));
	}

	public static File getModelRelationFile(String modelRelationName) {
		return getProjectFile(Workflow_Dir_Name, getRelationFileName(modelRelationName));
	}

	public static HashMap<String, IDrawNode> createDrawNodeHashMap(Collection<IDrawNode> nodes) {
		HashMap<String, IDrawNode> hashMap = new HashMap<>();
		for (IDrawNode drawNode : nodes) {
			hashMap.put(drawNode.getId(), (DrawNode) drawNode);
		}

		return hashMap;
	}

	public static String getModelRelationNameFromNodeID(String nodeid) throws Exception {
		return getModelRelationName(getModelRelationFileFromNodeID(nodeid));
	}

	public static String getModelRelationName(File modelRelationFile) {
		if (modelRelationFile == null)
			return null;

		String name = modelRelationFile.getName();
		return getModelRelationNameFromFileName(name);
	}

	public static String getModelRelationNameFromFileName(String name) {
		return FileHelp.removeExt(name);
	}

	public static boolean isMainModelRelation(File modelRelationFile) {
		String name = getModelRelationName(modelRelationFile);
		return name.compareToIgnoreCase(Main_Workflow_Relation_FileName) == 0;
	}

	public static List<DrawNode> getModelRelationInputs(IDrawNode modelNode, HashMap<String, IDrawNode> modelNodes,
			String modelRelationTitle) throws Exception {
		List<DrawNode> inputs = new ArrayList<>();
		for (String id : modelNode.getNexts()) {
			if (!modelNodes.containsKey(id)) {
				throw new Exception(
						"未发现流程图" + (modelRelationTitle == null ? "" : modelRelationTitle) + "中id：" + id + "的流程节点！");
			}
			DrawNode node = (DrawNode) modelNodes.get(id);
			if (node instanceof BeginNode || node instanceof EndNode)
				continue;
			else if (node instanceof ChildWorkflowNode) {
				getChildModelRelationInputs((WorkflowNode) node, inputs);
			} else
				inputs.add(node);
		}

		return inputs;
	}

	public static void getChildModelRelationInputs(DrawNode modelNode, List<DrawNode> result) throws Exception {

		File workflowRelationFile = getChildModelRelationFile(modelNode.id, false);
		List<IDrawNode> nodes = DrawCanvas.loadNodes(null, workflowRelationFile,
				new EditorEnvironment.WorkflowDeserializable(), null, null, null);

		if (nodes == null || nodes.size() == 0)
			return;

		HashMap<String, IDrawNode> hashNodes = new HashMap<>();
		for (IDrawNode drawNode : nodes) {
			hashNodes.put(drawNode.getId(), drawNode);
		}

		WorkflowNode beginNode = null;
		for (IDrawNode tmp : nodes) {
			WorkflowNode node = (WorkflowNode) tmp;
			if (node instanceof EndNode)
				continue;
			if (node instanceof BeginNode) {
				beginNode = node;
				break;
			}

		}
		if (beginNode == null) {
			return;
		}

		for (String id : beginNode.getNexts()) {
			if (!hashNodes.containsKey(id)) {
				throw new NotFound();
			}

			WorkflowNode node = (WorkflowNode) hashNodes.get(id);
			if (node instanceof BeginNode || node instanceof EndNode)
				continue;
			else if (node instanceof ChildWorkflowNode) {
				getChildModelRelationInputs(node, result);
			} else
				result.add(node);
		}
	}

	public static void updateUI(String nodeid, String uiid) throws Exception {
		setFileAboutWorkflowNode(EditorEnvironment.UI_Dir_Name, nodeid, EditorEnvironment.Config_UI_Key_Name, uiid,
				GetFileNameType.ftUI, true, true);
	}

	public static void updateApp(String nodeid, String appid) throws Exception {
		setFileAboutWorkflowNode(EditorEnvironment.App_Dir_Name, nodeid, EditorEnvironment.Config_App_Key_Name, appid,
				GetFileNameType.ftApp, true, true);
	}

	public static File getFileAboutWorkflowNode(String dirName, String nodeid, String key, GetFileNameType ft,
			boolean needSave) {
		return setFileAboutWorkflowNode(dirName, nodeid, key, null, ft, false, needSave);
	}

	public static File setFileAboutWorkflowNode(String dirName, String nodeid, String key, String id,
			GetFileNameType ft, boolean forceSave, boolean needSave) {
		return setFileAboutNode(dirName, Workflow_Dir_Name, nodeid, key, id, ft, forceSave, needSave);
	}

	public static File getFileAboutNode(String dirName, String configDirName, String nodeid, String key,
			GetFileNameType ft, boolean needSave) {
		return setFileAboutNode(dirName, configDirName, nodeid, key, null, ft, false, needSave);
	}

	public static void createModelNodeConfigFile(String nodeid) {
		File nodeconfig = EditorEnvironment.getProjectFile(Workflow_Dir_Name,
				EditorEnvironment.getNodeFileName(nodeid));
		try {
			JsonHelp.saveJson(nodeconfig, "{}", null);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}

	public static File setFileAboutNode(String dirName, String configDirName, String nodeid, String key, String id,
			GetFileNameType ft, boolean replaceExists, boolean needSave) {
		return setFileAboutNode(currentProjectName, dirName, configDirName, nodeid, key, id, ft, replaceExists,
				needSave);
	}

	public static File setFileAboutNode(String projectName, String dirName, String configDirName, String nodeid,
			String key, String id, GetFileNameType ft, boolean replaceExists, boolean needSave) {

		File nodeconfig = EditorEnvironment.getProjectFile(projectName, configDirName,
				EditorEnvironment.getNodeFileName(nodeid));

		try {
			JSONObject json;
			if (!nodeconfig.exists()) {
				if (needSave)
					json = new JSONObject();
				else
					return null;
			} else
				json = (JSONObject) JsonHelp.parseCacheJson(nodeconfig, null);
			boolean exist = json.has(key);
			if (exist && !replaceExists) {
				String tmp = json.getString(key);
				if (tmp != null && !tmp.isEmpty()) {
					if (id != null && tmp.equals(id))
						needSave = false;
					else
						id = tmp;
				}

			}

			if (needSave) {
				if (replaceExists && (id == null || id.isEmpty()))
					id = UUID.randomUUID().toString();

				if (id == null || id.isEmpty()) {
					boolean needCreate = !json.has(key);
					if (!needCreate) {
						String v = json.getString(key);
						needCreate = v == null || v.isEmpty();
					}

					if (needCreate) {
						id = UUID.randomUUID().toString();
					}

				}
				json.put(key, id);
			}

			if (id == null || id.isEmpty()) {
				return null;
			}

			File file = null;
			switch (ft) {
			case ftUI:
				file = EditorEnvironment.getProjectFile(projectName, dirName, EditorEnvironment.getUI_FileName(id));
				break;
			case ftWorkflowNode:
				file = EditorEnvironment.getProjectFile(projectName, dirName, EditorEnvironment.getNodeFileName(id));
				break;
			case ftApp:
				file = EditorEnvironment.getProjectFile(projectName, dirName, EditorEnvironment.getApp_FileName(id));
				break;
			case ftToolbar:
				file = EditorEnvironment.getProjectFile(projectName, dirName, EditorEnvironment.getToolbarFileName(id));
				break;
			default:
				break;

			}

			if (needSave)
				JsonHelp.saveJson(nodeconfig, json, null);

			return file;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static File getRunFlowFile(String id) {
		return getProjectFile(RunFlow_Dir_Name, getRunFlow_FileName(id));
	}

	public static File getMainModelRelationFile() {
		return EditorEnvironment.getProjectFile(EditorEnvironment.Workflow_Dir_Name,
				EditorEnvironment.getRelationFileName(EditorEnvironment.Main_Workflow_Relation_FileName));

	}

	public static String getPublish_UI_FileName(String name) {
		return name + "." + UI_Publish_File_Extension;
	}

	public static String getRelationFileName(String name) {
		return name + "." + Relation_File_Extension;
	}

	public static String getNodeFileName(String name) {
		return name + "." + Node_File_Extension;
	}

	public static String getApp_FileName(String name) {
		return name + "." + App_File_Extension;
	}

	public static String getFlow_FileName(String name) {
		return name + "." + Flow_File_Extension;
	}

	public static String getToolbarFileName(String name) {
		return name + "." + Toolbar_File_Extension;
	}

	public static String getMenu_FileName(String name) {
		return name + "." + Menu_File_Extension;
	}

	public static String getTree_FileName(String name) {
		return name + "." + Tree_File_Extension;
	}

	public static String getRunFlow_FileName(String name) {
		return name + "." + RunFlow_File_Extension;
	}

	public static String getUI_FileName(String name) {
		return name + "." + UI_File_Extension;
	}

	public static String getAppWorkflow_FileName(String name) {
		return name + "." + AppWorkflow_File_Extension;
	}

	protected static File copyFile(File source, File path, String dirName, boolean isLinked) throws IOException {
		if (dirName != null && !dirName.isEmpty()) {
			path = new File(path, dirName);
		}
		File destPath = createDir(path);
		if (path == null)
			throw new IOException("mkdir[" + destPath.getAbsolutePath() + "] is fail!");

		File desc = new File(destPath, source.getName());
		if (desc.exists())
			if (!desc.delete())
				throw new IOException("delete file[" + desc.getAbsolutePath() + "] fail!");

		FileHelp.copyFileTo(source, desc, isLinked);
		return desc;
	}

	public static void copyModelRelation(File modelRelationFile, File destPath, String name, List<IDrawNode> nodes,
			boolean isLinked) throws Exception {
		File file = copyFile(modelRelationFile, destPath, Workflow_Dir_Name, isLinked);

		if (name != null && !name.isEmpty()) {
			File newFile = new File(file.getParentFile(), name);
			if (!file.renameTo(newFile))
				throw new IOException("rename [" + file.getAbsolutePath() + "] to [" + newFile.getAbsolutePath() + "]");
			file = newFile;
		}

		if (nodes != null) {
			WorkflowCanvas canvas = new WorkflowCanvas();
			canvas.setFile(file);
			canvas.load(new WorkflowDeserializable(), null);
			HashMap<String, String> nodeMap = new HashMap<>();
			for (IDrawNode drawNode : nodes) {
				nodeMap.put(drawNode.getId(), drawNode.getName());
			}

			for (IDrawNode drawNode : canvas.getNodes()) {
				if (!nodeMap.containsKey(drawNode.getId())) {
					canvas.remove(new DrawNode[] { (DrawNode) drawNode }, false, false, false, false);
				}
			}
			canvas.save();
		}
	}

	public static void copyNode(DrawNode node, File descPath, boolean isLinked) throws Exception {
		if (node instanceof ChildWorkflowNode) {
			File tmp = getChildModelRelationFile(node.id, false);
			if (tmp != null && tmp.exists())
				copyFile(tmp, descPath, Workflow_Dir_Name, isLinked);
		}

		File tmp = getAppFile(node.id, false);
		if (tmp != null && tmp.exists())
			copyFile(tmp, descPath, App_Dir_Name, isLinked);

		List<File> files = SceneBuilder.getFlowFiles(node.id);
		for (File file : files) {
			if (file != null && file.exists())
				copyFile(file, descPath, Flow_Dir_Name, isLinked);
		}

		tmp = getUIFile(node.id, false);
		if (tmp != null && tmp.exists())
			copyFile(tmp, descPath, UI_Dir_Name, isLinked);

		tmp = getModelFile(node.id);
		if (tmp != null && tmp.exists())
			copyFile(tmp, descPath, Workflow_Dir_Name, isLinked);

		tmp = getToolbarFile(node.id, false);
		if (tmp != null && tmp.exists())
			copyFile(tmp, descPath, Toolbar_Dir_Name, isLinked);

	}

	public static File newUniqProjectFile(String dirName, String prefix, String ext) {
		File path = getProjectFile(dirName, null);
		File file = null;
		int index = 0;
		while ((file = new File(path, prefix + String.valueOf(index++) + "." + ext)) != null && !file.exists()) {

		}

		return file;
	}

	public static File getEditorSourcePath(String pathname, String name) {
		File path = new File(FileHelp.getRootPath(), "/" + Defines.Java_Dir_Resource.getName());
		if (pathname != null && !pathname.isEmpty())
			path = new File(path, pathname);
		if (name != null && !name.isEmpty())
			path = new File(path, name);
		return path;
	}

	public static File getEditorPath(String pathname, String name) {
		File path = FileHelp.getRootPath();
		if (pathname != null && !pathname.isEmpty())
			path = new File(path, pathname);
		if (name != null && !name.isEmpty())
			path = new File(path, name);
		return path;
	}

	static File createDir(File f) {
		if (f == null)
			return null;

		if (!f.exists())
			if (f.mkdirs())
				return f;
			else {
				return null;
			}
		else
			return f;
	}

	static String currentProjectName;
	static File currentProjectPath;

	public static void setCurrentProjectName(String projectName) {
		currentProjectName = projectName;
	}

	public static String getCurrentProjectName() {
		return currentProjectName;
	}

	public static File getProjectPath(String dirName) {
		return getProjectPath(getCurrentProjectName(), dirName);
	}

	public static File getProjectPath(String projectName, String dirName) {
		File f = new File(getProjectPathForName(projectName), dirName);
		return createDir(f);
	}

	public static File getProjectPathForName(String projectName) {
		File f = new File(getProjectBasePath(), projectName);
		return createDir(f);
	}

	public static File getBasePath(String dirName, String filename) {
		File path = new File(FileHelp.getRootPath(), dirName + File.separator + filename);
		return path;
	}

	public static void setProjectBasePath(File dir) {
		currentProjectPath = dir;
		createDir(currentProjectPath);
	}

	public static void setProjectBasePath() {
		currentProjectPath = new File(FileHelp.getRootPath(), Defines.Java_Dir_Project.getName());
		createDir(currentProjectPath);
	}

	public static File getProjectBasePath() {
		File path = currentProjectPath;
		return createDir(path);
	}

	public static List<String> getProjectNames() {
		List<String> projects = new ArrayList<>();
		for (File file : getProjectBasePath().listFiles()) {
			if (file.isDirectory() && new File(file, Project_ConfigFileName).exists())
				projects.add(file.getName().replace("." + Project_File_Extension, ""));
		}

		return projects;
	}

	public static List<String> getDispatchNames() {
		List<String> projects = new ArrayList<>();

		File dispatchDir = new File(FileHelp.getRootPath(), Workflow_Dispatch_Dir_Name);
		for (File file : dispatchDir.listFiles()) {
			if (file.isDirectory())
				projects.add(file.getName());
		}

		return projects;
	}

	public static boolean existsProjectName(String name) {
		if (name == null || name.isEmpty())
			return false;

		File f = getProjectBasePath();
		if (f == null)
			return false;
		f = new File(f, name);
		return f.exists();
	}

	public static boolean newProject(String name, boolean overwrite) throws Exception {
		if (existsProjectName(name) && !overwrite)
			return false;

		File projectPath = getProjectPathForName(name);
		if (projectPath.exists()) {
			if (!FileHelp.delDir(projectPath)) {
				MsgHelper.showMessage(null, "删除原有文件失败，请检查文件系统是否异常！", "建立项目", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

		if (projectPath.mkdirs()) {
			String oldName = currentProjectName;
			try {
				if (!FileHelp.copyFilesTo(EditorEnvironment.getEditorSourcePath("default", null),
						getProjectPath(name, Frame_Dir_Path))) {
					MsgHelper.showMessage(null, "拷贝框架文件失败，请检查文件系统是否异常！", "建立项目", JOptionPane.ERROR_MESSAGE);
					return false;
				}
				setCurrentProjectName(name);
				File file = getProjectFile(null, Project_ConfigFileName);
				XmlDom xmlDom = new XmlDom(file.getAbsolutePath());
				xmlDom.NewDOM();
				xmlDom.Save();
				return true;
			} catch (IOException e) {
				setCurrentProjectName(oldName);
				e.printStackTrace();
				FileHelp.delDir(projectPath);
				MsgHelper.showMessage(null,
						"建立项目失败：" + e.getMessage() == null ? e.getClass().toString() : e.getMessage(), "建立项目",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} else {
			MsgHelper.showMessage(null, "建立项目文件目录失败，请检查文件系统是否异常！", "建立项目", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	public static boolean updateFrame() throws Exception {
		return updateFrame(currentProjectName);
	}

	public static boolean updateFrame(String projectName) throws Exception {
		if (!existsProjectName(projectName)) {
			MsgHelper.showMessage(null, "请先打开一个项目！", "更新Frame", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		File framePath = getProjectPath(projectName, Frame_Dir_Path);
		copyDir(EditorEnvironment.getEditorSourcePath("default", null), framePath, true);
		return true;
	}

	public static File getTemplateFile(String dirName, String name) {
		File path = getResourceFile(Template_Path, dirName);
		return new File(path, name);
	}

	public static File getResourceFile(String dirName, String name) {
		return new File(new File(Defines.Java_Dir_Resource, dirName), name);
	}

	public static File getProjectFile(String pathname, String filename) {
		return getProjectFile(currentProjectName, pathname, filename);
	}

	public static File getProjectJSPath(String name) {
		return new File(getProjectJSPath(), name + ".js");
	}

	public static File getProjectJSPath() {
		File path = getProjectFile(currentProjectName, User_JavaScript_Dir_Path, Client_Dir_Path);
		path = new File(path, User_JavaScript_Dir_Path);
		if (!path.exists())
			path.mkdirs();
		return path;
	}

	public static File getProjectImagePath() {
		File path = getProjectPath(Image_Resource_Path);
		if (!path.exists())
			path.mkdirs();
		return path;
	}

	public static File getReportFile(String name) {
		return new File(getProjectReportPath(), name + "." + Report_File_Extension);
	}

	public static File getProjectReportPath() {
		File path = getProjectPath(Report_Dir_Path);
		if (!path.exists())
			path.mkdirs();
		return path;
	}

	public static File getProjectFile(String projectName, String pathname, String filename) {
		File path = getProjectPathForName(projectName);
		if (pathname != null && !pathname.isEmpty()) {
			path = new File(path, pathname);
		}

		if (!path.exists())
			if (!path.mkdirs())
				return null;

		if (filename == null || filename.isEmpty())
			return path;
		else
			return new File(path, filename);
	}

	public static File getModelNodeFile(String nodeid) {
		return getProjectFile(Workflow_Dir_Name, getNodeFileName(nodeid));
	}

	public static File getPublishWebFile(String pageName) throws Exception {
		return getPublishWebFile(null, pageName);
	}

	public static File getPublishWebPath() throws Exception {
		return getPublishWebFile(null, null);
	}

	public static File getPublishWebFile(String dirName, String pageName) throws Exception {
		String webRoot = getWebRoot();
		if (webRoot == null || webRoot.isEmpty())
			return null;

		File path = new File(webRoot, currentProjectName);
		if (dirName != null && !dirName.isEmpty())
			path = new File(path, dirName);

		if (createDir(path) == null)
			return null;

		if (pageName != null && !pageName.isEmpty())
			path = new File(path, pageName);
		return path;
	}

	public static String getWebUrl(String pageName) throws Exception {
		return getWebUrl(currentProjectName, pageName);
	}

	public static String getWebUrl(String projectName, String pageName) throws Exception {
		File baseFile = getPublishWebFile(projectName, pageName);
		if (baseFile == null)
			return null;

		String hostUrl = formatProjectPath(baseFile);
		String baseUrl = getWebRoot().trim();
		baseUrl = baseUrl.substring(baseUrl.length() - 1, 1).compareTo("/") != 0 ? baseUrl + "/" : baseUrl;
		return baseUrl + hostUrl;
	}

	public static String formatProjectPath(File projectPath) throws Exception {
		String path = projectPath.getAbsolutePath() + "/";
		path = path.replace("\\", "/").replace("//", "/");
		return path;
	}

	static final String ROOT_KEY = "/root/editor";
	static final String WEBPATH_KEY = "webpath";
	static final String WEBROOT_KEY = "webroot";
	static final String DATA_SERVICE_URL_key = "dataurl";

	static XmlDom getConfig() throws Exception {
		File configFile = getProjectFile(null, Project_ConfigFileName);
		XmlDom config = new XmlDom(configFile.getAbsolutePath());
		if (configFile.exists())
			config.Load();
		else
			config.NewDOM();

		return config;
	}

	public static String getWebRoot() throws Exception {
		XmlDom config = getConfig();
		String pathname = config.GetValue(ROOT_KEY, WEBROOT_KEY);
		if (pathname != null && !pathname.isEmpty())
			return pathname;
		return null;
	}

	public static void setWebRoot(File path) {
		try {
			XmlDom config = getConfig();
			config.SetValue(ROOT_KEY, WEBROOT_KEY, path.getAbsolutePath());
			config.Save();
		} catch (Exception e1) {
			MsgHelper.showMessage(null, e1.getMessage() == null ? e1.getClass().getName() : e1.getMessage(), "设置失败",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static String getDataServiceRoot() throws Exception {
		XmlDom config = getConfig();
		String url = config.GetValue(ROOT_KEY, DATA_SERVICE_URL_key);
		if (url != null && !url.isEmpty())
			return url;
		return null;
	}

	public static void setDataServiceRoot(String url) {
		try {
			url = url.trim();
			if (url.endsWith("/"))
				url = url.substring(0, url.length() - 1);
			XmlDom config = getConfig();
			config.SetValue(ROOT_KEY, DATA_SERVICE_URL_key, url);
			config.Save();
		} catch (Exception e1) {
			MsgHelper.showMessage(null, e1.getMessage() == null ? e1.getClass().getName() : e1.getMessage(), "设置失败",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	public static void loadProjectConfig() {
		try {
			XmlDom config = getConfig();
			config.Load();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	public static boolean traverseModel(File relationFile, ITraverseDrawNode onTraverseDrawNode, Object param)
			throws Exception {
		AtomicReference<String> workflowRelationNameRef = new AtomicReference<String>("");
		List<IDrawNode> nodes = DrawCanvas.loadNodes(null, relationFile, new EditorEnvironment.WorkflowDeserializable(),
				new ILoad() {

					@Override
					public void onBeforeLoad(JSONObject data, Object param) throws JSONException {
						String workflowRelationName = DrawCanvas.getTitle(data);
						if (workflowRelationName == null || workflowRelationName.isEmpty())
							workflowRelationName = getModelRelationName(relationFile);

						workflowRelationNameRef.set(workflowRelationName);
					}
				}, null, null);
		if (nodes == null)
			return true;

		for (IDrawNode tmp : nodes) {
			WorkflowNode node = (WorkflowNode) tmp;
			if (!onTraverseDrawNode.onNode(relationFile, workflowRelationNameRef.get(), node, param))
				return false;
			if (node instanceof ChildWorkflowNode) {
				File subFile = EditorEnvironment.getChildModelRelationFile(node.id, false);
				if (!traverseModel(subFile, onTraverseDrawNode, param))
					return false;
			}
		}

		return true;

	}

	public static void traverseModel(ITraverseDrawNode onTraverseModel, Object param) throws Exception {
		File mainWorkflowRelationFile = EditorEnvironment.getProjectFile(EditorEnvironment.Workflow_Dir_Name,
				EditorEnvironment.getRelationFileName(EditorEnvironment.Main_Workflow_Relation_FileName));
		traverseModel(mainWorkflowRelationFile, onTraverseModel, param);
	}

	public static boolean traverseNavRegion(RegionName regionName, ITraverseDrawNode onTraverseModel, Object param)
			throws Exception {
		WorkflowNode node = getFrameModelNode(regionName);
		File workflowFile = getFrameModelNodeFile(regionName);
		if (workflowFile == null) {
			return true;
		}

		return onTraverseModel.onNode(workflowFile, regionName.name(), node, param);

	}

	public static void traverseModelAndNavs(ITraverseDrawNode onTraverseModel, Object param) throws Exception {
		File mainWorkflowRelationFile = EditorEnvironment.getProjectFile(EditorEnvironment.Workflow_Dir_Name,
				EditorEnvironment.getRelationFileName(EditorEnvironment.Main_Workflow_Relation_FileName));
		traverseModel(mainWorkflowRelationFile, onTraverseModel, param);

		if (!traverseNavRegion(RegionName.rnTop, onTraverseModel, param))
			return;

		if (!traverseNavRegion(RegionName.rnBottom, onTraverseModel, param))
			return;

		if (!traverseNavRegion(RegionName.rnLeft, onTraverseModel, param))
			return;

		if (!traverseNavRegion(RegionName.rnRight, onTraverseModel, param))
			return;

	}

	public static boolean existsModelNodeName(String name) throws Exception {
		AtomicBoolean result = new AtomicBoolean(false);
		traverseModel(new ITraverseDrawNode() {

			@Override
			public boolean onNode(File file, String workflowRelationName, IDrawNode node, Object param) {
				boolean b = node.getName().compareToIgnoreCase(param.toString()) == 0;
				if (b)
					result.set(true);
				return !b;
			}
		}, name);
		return result.get();
	}

	public static File getFrameModelNodeFile(RegionName rName) {
		return getFrameModelNodeFile(currentProjectName, rName);
	}

	public static File getFrameModelNodeFile(String projectName, RegionName rName) {
		String filename = "";
		switch (rName) {
		case rnBottom:
			filename = getRelationFileName("bottom_region");
			break;
		case rnLeft:
			filename = getRelationFileName("left_region");
			break;
		case rnRight:
			filename = getRelationFileName("right_region");
			break;
		case rnTop:
			filename = getRelationFileName("top_region");
			break;
		default:
			return null;
		}

		File file = getProjectFile(projectName, EditorEnvironment.Workflow_Dir_Name, filename);
		return file;
	}

	public static void saveFrameModelNode(DrawNode node) throws Exception {
		RegionName rName;
		switch (node.id) {
		case "bottom_region":
			rName = RegionName.rnBottom;
			break;
		case "left_region":
			rName = RegionName.rnLeft;
			break;
		case "right_region":
			rName = RegionName.rnRight;
			break;
		case "top_region":
			rName = RegionName.rnTop;
			break;
		default:
			throw new Exception("无效的系统区域名称[" + node.id + "]！");
		}

		File file = getFrameModelNodeFile(rName);
		if (file.exists())
			if (!file.delete()) {
				throw new IOException("删除区域描述文件[" + file.getAbsolutePath() + "]失败！");
			}

		JSONObject data = node.toJson();

		JsonHelp.saveJson(file, data, null);
	}

	public static WorkflowNode[] getFrameModelNodes() throws Exception {
		return new WorkflowNode[] { getFrameModelNode(RegionName.rnLeft), getFrameModelNode(RegionName.rnRight),
				getFrameModelNode(RegionName.rnTop), getFrameModelNode(RegionName.rnBottom), };
	}

	public static WorkflowNode getFrameModelNode(RegionName rName) throws Exception {
		File file = getFrameModelNodeFile(rName);
		if (file == null)
			return null;

		WorkflowNode node = null;
		if (file.exists())
			node = (WorkflowNode) IDrawNode.load(file, new EditorEnvironment.WorkflowDeserializable());
		else {
			node = new WorkflowNode(null);
			node.id = FileHelp.removeExt(file.getName());
			node.name = node.id;
			switch (rName) {
			case rnBottom:
				node.title = "页脚区域";
				node.setRect(new Rectangle(660, 100, 100, 100));
				break;
			case rnLeft:
				node.title = "左侧区域";
				node.setRect(new Rectangle(300, 100, 100, 100));
				break;
			case rnRight:
				node.title = "右侧区域";
				node.setRect(new Rectangle(420, 100, 100, 100));
				break;
			case rnTop:
				node.title = "页头区域";
				node.setRect(new Rectangle(540, 100, 100, 100));
				break;
			default:
				break;

			}
		}
		return node;
	}

	public static File getParentModelRelationFile(String filname) throws Exception {
		AtomicReference<File> result = new AtomicReference<File>(null);
		traverseModel(new ITraverseDrawNode() {

			@Override
			public boolean onNode(File file, String workflowRelationName, IDrawNode node, Object param) {
				if (node instanceof ChildWorkflowNode) {
					File subFile = EditorEnvironment.getChildModelRelationFile(node.getId(), false);
					if (subFile.getName().compareTo(param.toString()) == 0) {
						result.set(file);
						return false;
					}
				}
				return true;
			}
		}, filname);
		return result.get();
	}

	public static List<ResultModelRelationInfo> getParentModelRelationFiles(File modelRelationFile) throws Exception {
		final List<ResultModelRelationInfo> results = new ArrayList<>();
		results.add(new ResultModelRelationInfo(modelRelationFile, null));
		traverseModel(new ITraverseDrawNode() {

			@Override
			public boolean onNode(File file, String workflowRelationName, IDrawNode node, Object param) {
				if (!(node instanceof ChildWorkflowNode))
					return true;

				String[] check = (String[]) param;
				File subFile = EditorEnvironment.getChildModelRelationFile(node.getId(), false);
				if (subFile.getName().compareTo(check[0]) == 0) {
					results.add(new ResultModelRelationInfo(file, null));
					check[0] = file.getName();
				}
				return true;
			}
		}, new String[] { modelRelationFile.getName() });
		return results;
	}

	public static boolean checkExistsModelNodeName(ICheckCallBack onCheckCallBack) throws Exception {
		final HashMap<String, File> files = new HashMap<>();
		final HashMap<String, IDrawNode> nodes = new HashMap<>();
		AtomicBoolean result = new AtomicBoolean(true);
		traverseModel(new ITraverseDrawNode() {

			@Override
			public boolean onNode(File file, String workflowRelationName, IDrawNode node, Object param) {
				String name = node.getId().toLowerCase();
				if (nodes.containsKey(name)) {
					result.set(false);
					MsgHelper.showMessage("工作流【" + workflowRelationName + "】中的【" + node.getId() + "(" + node.getTitle()
							+ ")】的节点名称重复，请修改后重试！");
					if (onCheckCallBack != null)
						onCheckCallBack.onRepeat(workflowRelationName, file, node, files.get(name), nodes.get(name),
								param);
					return false;
				} else {
					nodes.put(name, node);
					files.put(name, file);
				}
				return true;
			}
		}, nodes);
		return result.get();
	}

	public static boolean traverseUI(File uiFile, ITraverseDrawNode onTraverseDrawNode, Object param) throws Exception {
		AtomicReference<String> uiNameRef = new AtomicReference<String>("");
		List<IDrawNode> nodes = DrawCanvas.loadNodes(null, uiFile, new ICreateNodeSerializable() {

			@Override
			public DrawNode newDrawNode(JSONObject json) {
				return new UINode(null);
			}

			@Override
			public IDataSerializable getUserDataSerializable(IDrawNode node) {
				return null;
			}
		}, new ILoad() {

			@Override
			public void onBeforeLoad(JSONObject data, Object param) throws JSONException {
				String uiName = DrawCanvas.getTitle(data);
				if (uiName == null || uiName.isEmpty())
					uiName = getModelRelationName(uiFile);

				uiNameRef.set(uiName);
			}
		}, null, null);

		if (nodes == null)
			return true;

		for (IDrawNode tmp : nodes) {
			UINode node = (UINode) tmp;
			if (!onTraverseDrawNode.onNode(uiFile, uiNameRef.get(), node, param))
				return false;
		}

		return true;

	}

	public static boolean traverseUI(ITraverseDrawNode onTraverseUI, Object param) throws Exception {
		return traverseUI(onTraverseUI, param, false);
	}

	public static boolean traverseUI(ITraverseDrawNode onTraverseUI, Object param, boolean onlyFile) throws Exception {
		File dir = getProjectPath(currentProjectName, EditorEnvironment.UI_Dir_Name);

		HashMap<File, WorkflowNode> workflowNodes = new HashMap<>();
		traverseModel(new ITraverseDrawNode() {

			@Override
			public boolean onNode(File file, String title, IDrawNode node, Object param) {
				File uiFile = getUIFile(node.getId(), false);
				if (uiFile != null) {
					workflowNodes.put(uiFile, (WorkflowNode) node);
				}
				return true;
			}
		}, param);

		for (File f : dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.substring(name.length() - UI_File_Extension.length())
						.compareToIgnoreCase(UI_File_Extension) == 0;
			}
		})) {
			if (onlyFile) {
				if (!onTraverseUI.onNode(f, null, null, param))
					return false;
				continue;
			}

			if (!workflowNodes.containsKey(f))
				continue;

			if (!traverseUI(f, onTraverseUI, param))
				return false;
		}

		return true;
	}

	public static boolean traverseUI(ITraverseUIFile onTraverseUIFile, Object userObject) throws Exception {
		File dir = getProjectPath(currentProjectName, EditorEnvironment.UI_Dir_Name);

		HashMap<File, WorkflowNode> workflowNodes = new HashMap<>();
		traverseModel(new ITraverseDrawNode() {

			@Override
			public boolean onNode(File file, String title, IDrawNode node, Object param) {
				File uiFile = getUIFile(node.getId(), false);
				if (uiFile != null) {
					workflowNodes.put(uiFile, (WorkflowNode) node);
				}
				return true;
			}
		}, null);

		for (File f : dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.substring(name.length() - UI_File_Extension.length())
						.compareToIgnoreCase(UI_File_Extension) == 0;
			}
		})) {
			UICanvas canvas = new UICanvas();
			canvas.setFile(f);
			canvas.load(new ICreateNodeSerializable() {

				@Override
				public DrawNode newDrawNode(JSONObject json) {
					return new UINode(null);
				}

				@Override
				public IDataSerializable getUserDataSerializable(IDrawNode node) {
					return null;
				}
			}, null);

			if (onTraverseUIFile.callback(f, canvas, userObject))
				canvas.save();

		}

		return true;
	}

	public static boolean existsUINodeName(String name) throws Exception {
		AtomicBoolean result = new AtomicBoolean(false);
		traverseUI(new ITraverseDrawNode() {

			@Override
			public boolean onNode(File file, String title, IDrawNode node, Object param) {
				boolean b = node.getName().compareToIgnoreCase(param.toString()) == 0;
				if (b)
					result.set(true);
				return !b;
			}
		}, name);
		return result.get();
	}

	public static ModelNodeInfo getModelInfoFromUI(File uiFile) throws Exception {
		final ModelNodeInfo info = new ModelNodeInfo();
		traverseModel(new ITraverseDrawNode() {

			@Override
			public boolean onNode(File file, String workflowRelationName, IDrawNode node, Object param) {
				File curUIFile = getUIFile(node.getId(), false);
				if (curUIFile == null)
					return true;
				if (curUIFile.getName().compareToIgnoreCase(uiFile.getName()) == 0) {
					try {
						for (IDrawNode tmpNode : DrawCanvas.loadNodes(null, file,
								new EditorEnvironment.WorkflowDeserializable(), new ILoad() {

									@Override
									public void onBeforeLoad(JSONObject data, Object param) throws JSONException {
										String workflowRelationName = DrawCanvas.getTitle(data);
										if (workflowRelationName == null || workflowRelationName.isEmpty())
											workflowRelationName = getModelRelationName(file);

										info.title = workflowRelationName;
									}
								}, null, null)) {
							info.nodes.put(tmpNode.getId(), tmpNode);
						}
						info.node = node;
						return false;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				return true;
			}
		}, null);
		return info;
	}

	public static File getFrameNodeLockFile() {
		return getProjectFile(Workflow_Dir_Name, FrameModelName + ".lock");
	}

	public static WorkflowNode getModelNodeFromUI(String uiid) throws Exception {
		File file = getUIFileForUIID(uiid);
		return getModelNodeFromUI(file);
	}

	public static WorkflowNode getModelNodeFromUI(File uiFile) throws Exception {
		final WorkflowNode[] info = new WorkflowNode[] { null };
		traverseModel(new ITraverseDrawNode() {

			@Override
			public boolean onNode(File file, String workflowRelationName, IDrawNode node, Object param) {
				File curUIFile = getUIFile(node.getId(), false);
				if (curUIFile == null)
					return true;
				if (curUIFile.getName().compareToIgnoreCase(uiFile.getName()) == 0) {
					info[0] = (WorkflowNode) node;
					return false;
				}

				return true;
			}
		}, null);
		return info[0];
	}

	public static IDrawNode getChildModelNodeFromFile(File childModelRelationFile) throws Exception {
		final ModelNodeInfo info = new ModelNodeInfo();
		traverseModel(new ITraverseDrawNode() {

			@Override
			public boolean onNode(File file, String workflowRelationName, IDrawNode node, Object param) {
				if (!(node instanceof ChildWorkflowNode))
					return true;

				File curFile = getChildModelRelationFile(node.getId(), false);
				if (curFile.getName().compareToIgnoreCase(childModelRelationFile.getName()) == 0) {
					info.node = node;
					return false;
				}
				return true;
			}
		}, null);
		return info.node;
	}

	public static File getModelRelationFileFromNodeID(String nodeid) throws Exception {
		final AtomicReference<File> info = new AtomicReference<File>(null);
		traverseModel(new ITraverseDrawNode() {

			@Override
			public boolean onNode(File file, String workflowRelationName, IDrawNode node, Object param) {
				if (node.getId().compareToIgnoreCase(nodeid) == 0) {
					info.set(file);
					return false;
				}
				return true;
			}
		}, null);
		return info.get();
	}

	public static File getModelRelationFileFromNodeName(String nodeName) throws Exception {
		final AtomicReference<File> info = new AtomicReference<File>(null);
		traverseModel(new ITraverseDrawNode() {

			@Override
			public boolean onNode(File file, String workflowRelationName, IDrawNode node, Object param) {
				if (node.getName().compareToIgnoreCase(nodeName) == 0) {
					info.set(file);
					return false;
				}
				return true;
			}
		}, null);
		return info.get();
	}

	public enum CheckType {
		ctName, ctID
	}

	public static boolean checkExistsUINodeName(ICheckCallBack onCheckCallBack, CheckType ct) throws Exception {
		final HashMap<String, File> files = new HashMap<>();
		final HashMap<String, DrawNode> nodes = new HashMap<>();
		AtomicBoolean result = new AtomicBoolean(true);
		traverseUI(new ITraverseDrawNode() {

			@Override
			public boolean onNode(File file, String title, IDrawNode node, Object param) {
				UINode uiNode = (UINode) node;

				String name = ct == CheckType.ctName ? uiNode.getDrawInfo().name.toLowerCase()
						: uiNode.getDrawInfo().id.toLowerCase();
				if (nodes.containsKey(name)) {
					result.set(false);
					MsgHelper.showMessage("界面【" + title + "】中的【" + uiNode.getDrawInfo().title + "/"
							+ uiNode.getDrawInfo().value + "】的控件名称重复，请修改后重试！");
					if (onCheckCallBack != null)
						if (!onCheckCallBack.onRepeat(title, file, node, files.get(name), nodes.get(name), param))
							return true;
					return false;
				} else {
					nodes.put(name, uiNode);
					files.put(name, file);
				}
				return true;
			}
		}, nodes);
		return result.get();
	}

	protected static void copyDir(File source, File dest, boolean clear) throws IOException {
		copyDir(source, dest, clear, null);
	}

	protected static void copyDir(String dirName, String projectName) throws IOException {
		File source = EditorEnvironment.getProjectPath(projectName, dirName);
		File dest = EditorEnvironment.getProjectPath(currentProjectName, dirName);
		copyDir(source, dest, false);
	}

	protected static void copyDir(String sourceDirName, File dest, boolean clear) throws IOException {
		copyDir(sourceDirName, dest, clear, null);
	}

	protected static void copyDir(String sourceDirName, File dest, boolean clear, String[] exts) throws IOException {
		File source = EditorEnvironment.getProjectPath(currentProjectName, sourceDirName);
		copyDir(source, dest, clear, exts);
	}

	protected static File getPath(File path, String dirName) {
		String[] dirs = dirName.split(File.separator.compareTo("\\") == 0 ? "\\\\" : File.separator);

		for (String dir : dirs) {
			path = new File(path, dir);
		}

		return path;
	}

	protected static void mkProjectDir(String dirName) throws IOException {
		File dest = getPath(getProjectBasePath(), dirName);
		if (!dest.exists()) {
			if (!dest.mkdirs()) {
				throw new IOException("建立" + dest.getAbsolutePath() + "目录失败！");
			}
		}
	}

	protected static void mkPublishDir(File publishDir, String dirName) throws IOException {
		File dest = getPath(publishDir, dirName);
		if (!dest.exists()) {
			if (!dest.mkdirs()) {
				throw new IOException("建立" + dest.getAbsolutePath() + "目录失败！");
			}
		}
	}

	protected static void copyDir(File source, File dest, boolean clear, String[] exts) throws IOException {
		if (source == null || dest == null)
			return;

		if (dest.exists() && clear) {
			if (!FileHelp.delDir(dest)) {
				throw new IOException("删除" + dest.getAbsolutePath() + "目录失败！");
			}
		}
		if (!FileHelp.copyFilesTo(source, dest, exts)) {
			throw new IOException(
					"拷贝目录:" + source.getAbsolutePath() + "到:" + dest.getAbsolutePath() + "失败，请检查文件系统是否异常！");
		}

	}

	protected static void copyFile(File source, File destPath) throws IOException {
		if (source.exists()) {
			if (!destPath.exists())
				if (!destPath.mkdirs())
					throw new IOException("not make dir[" + destPath.getAbsolutePath() + "]");
			FileHelp.copyFileTo(source, new File(destPath, source.getName()));
		}

	}

	public static boolean publish(IPublish publishEvent, boolean needResource, boolean needFrame, boolean needUserJS,
			boolean needReport, boolean needMainMenu, boolean needMainNav, boolean needDataSource,
			boolean needRemoteAuth) {
		if (currentProjectName == null || currentProjectName.isEmpty()) {
			MsgHelper.showMessage(null, "未设置项目！", "发布", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try {
			File webPath = getPublishWebPath();
			if (webPath == null) {
				MsgHelper.showMessage(null, "请先设置web根目录！", "发布", JOptionPane.WARNING_MESSAGE);
				return false;
			}

			if (!webPath.exists())
				if (!webPath.mkdirs()) {
					MsgHelper.showMessage(null, "建立" + webPath.getAbsolutePath() + "目录失败！", "发布失败",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}

			mkPublishDir(webPath, Frame_Dir_Path);
			mkPublishDir(webPath, Image_Resource_Path);
			mkPublishDir(webPath, DataSource_Dir_Name);
			mkPublishDir(webPath, Publish_Config_Dir_Name);
			mkPublishDir(webPath, "client" + File.separator + "userjs");
			if (needFrame) {
				copyDir(Frame_Dir_Path, webPath, true);
			}

			if (needReport) {
				copyDir(Report_Dir_Path, new File(webPath, Report_Dir_Path), true);
			}

			if (needResource) {
				copyDir(Image_Resource_Path, new File(webPath, Image_Resource_Path), true);
			}

			if (needDataSource) {
				copyDir(DataSource_Dir_Name, new File(webPath, DataSource_Dir_Name), true);
			}

			if (needRemoteAuth) {
				copyDir(Remote_Dir_Name, new File(webPath, Remote_Dir_Name), true);
			}

			copyFile(getProjectMetaFile(), new File(webPath, Publish_Config_Dir_Name));

			if (needMainMenu) {
				File source = getMainMenuFile();
				copyFile(source, new File(webPath, Menu_Dir_Path));
			}

			if (needMainNav) {
				File source = getMainNavTreeFile();
				copyFile(source, new File(webPath, Tree_Dir_Path));
			}

			if (publishEvent != null)
				publishEvent.publishContents();

			if (needUserJS) {
				File userjsPath = getProjectPath(currentProjectName, User_JavaScript_Dir_Path);
				List<File> sourceFiles = Arrays.asList(userjsPath.listFiles());
				List<File> destFiles = new ArrayList<>();
				File dir = new File(webPath, "client");
				if (!FileHelp.getFiles(dir, destFiles)) {
					MsgHelper.showMessage(null, "读取" + dir.getAbsolutePath() + "目录失败！", "发布失败",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}

				dir = new File(webPath, "miniui");
				if (!FileHelp.getFiles(dir, destFiles)) {
					MsgHelper.showMessage(null, "读取" + dir.getAbsolutePath() + "目录失败！", "发布失败",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}

				dir = new File(webPath, "css");
				if (!FileHelp.getFiles(dir, destFiles)) {
					MsgHelper.showMessage(null, "读取" + dir.getAbsolutePath() + "目录失败！", "发布失败",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}

				dir = new File(webPath, "Services");
				if (!FileHelp.getFiles(dir, destFiles)) {
					MsgHelper.showMessage(null, "读取" + dir.getAbsolutePath() + "目录失败！", "发布失败",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}

				dir = new File(webPath, "config");
				if (!FileHelp.getFiles(dir, destFiles)) {
					MsgHelper.showMessage(null, "读取" + dir.getAbsolutePath() + "目录失败！", "发布失败",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}

				dir = new File(webPath, "systemscript");
				if (!FileHelp.getFiles(dir, destFiles)) {
					MsgHelper.showMessage(null, "读取" + dir.getAbsolutePath() + "目录失败！", "发布失败",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}

				HashMap<String, File> names = new HashMap<>();
				for (File file : destFiles) {
					names.put(file.getName(), file);
				}

				for (File file : sourceFiles) {
					if (file.isDirectory()) {
						FileHelp.copyFilesTo(file, new File(webPath, file.getName()));
						continue;
					}

					String key = file.getName();
					if (names.containsKey(key)) {
						FileHelp.copyFileTo(file, names.get(key));
					} else {
						FileHelp.copyFileTo(file, new File(webPath, key));
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			String mString = e.getMessage() == null ? e.getClass().getName() : e.getMessage();
			MsgHelper.showMessage(null, "发布失败：" + mString, "发布失败", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	public static boolean publishModelNode(IPublish publishEvent, Collection<IDrawNode> nodes, boolean needResource,
			boolean needUserJS, boolean needReport, boolean needDataSource) {
		String[] ids = new String[nodes.size()];
		int index = 0;
		for (IDrawNode node : nodes) {
			ids[index++] = node.getName();
		}
		return publishModelNode(publishEvent, ids, needResource, needUserJS, needReport, needDataSource);
	}

	public static boolean publishModelNode(IPublish publishEvent, String[] modelNodeNames, boolean needResource,
			boolean needUserJS, boolean needReport, boolean needDataSource) {
		if (currentProjectName == null || currentProjectName.isEmpty()) {
			MsgHelper.showMessage(null, "未设置项目！", "发布", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		try {
			File webPath = getPublishWebPath();
			if (webPath == null) {
				MsgHelper.showMessage(null, "请先设置web根目录！", "发布", JOptionPane.WARNING_MESSAGE);
				return false;
			}

			if (!webPath.exists())
				if (!webPath.mkdirs()) {
					MsgHelper.showMessage(null, "建立" + webPath.getAbsolutePath() + "目录失败！", "发布失败",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}

			mkPublishDir(webPath, Image_Resource_Path);
			mkPublishDir(webPath, DataSource_Dir_Name);
			mkPublishDir(webPath, "client" + File.separator + "userjs");

			if (needReport) {
				copyDir(Report_Dir_Path, new File(webPath, Report_Dir_Path), true);
			}

			if (needResource) {
				copyDir(Image_Resource_Path, new File(webPath, Image_Resource_Path), true);
			}

			if (needDataSource) {
				copyDir(DataSource_Dir_Name, new File(webPath, DataSource_Dir_Name), true);
			}

			if (publishEvent != null)
				publishEvent.publishContents();

			if (needUserJS) {
				File userjsPath = getProjectPath(currentProjectName, User_JavaScript_Dir_Path + File.separator
						+ "client" + File.separator + User_JavaScript_Dir_Path);
				for (String modelNodeName : modelNodeNames) {
					File sourceFile = new File(userjsPath, modelNodeName + ".js");
					if (sourceFile.exists())
						FileHelp.copyFileTo(sourceFile, new File(webPath, "client" + File.separator
								+ User_JavaScript_Dir_Path + File.separator + sourceFile.getName()));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			String mString = e.getMessage() == null ? e.getClass().getName() : e.getMessage();
			MsgHelper.showMessage(null, "发布失败：" + mString, "发布失败", JOptionPane.ERROR_MESSAGE);
			return false;
		}

		return true;
	}

	public static JSONObject getMainNavData() {
		File navFile = EditorEnvironment.getMainNavTreeFile();
		if (navFile == null)
			return new JSONObject();

		try {
			if (!navFile.exists())
				return new JSONObject();

			JSONArray navs = (JSONArray) JsonHelp.parseCacheJson(navFile, null);
			JSONObject menuData = new JSONObject();
			for (int i = 0; i < navs.length(); i++) {
				JSONObject navItem = navs.getJSONObject(i);
				menuData.put(navItem.getString(JsonTreeDataEditor.id), navItem);
			}
			return menuData;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return new JSONObject();
	}

	public static File getMainMenuFile() {
		File menuFile = EditorEnvironment.getProjectFile(EditorEnvironment.Menu_Dir_Path,
				EditorEnvironment.getMenu_FileName(EditorEnvironment.Main_Menu_FileName));
		return menuFile;
	}

	public static JSONObject getMainManuData() {
		File menuFile = getMainMenuFile();

		if (menuFile == null)
			return new JSONObject();

		try {
			if (!menuFile.exists())
				return new JSONObject();

			JSONArray menus = (JSONArray) JsonHelp.parseCacheJson(menuFile, null);
			JSONObject menuData = new JSONObject();
			for (int i = 0; i < menus.length(); i++) {
				JSONObject menu = menus.getJSONObject(i);
				menuData.put(menu.getString(JsonTreeDataEditor.id), menu);
			}
			return menuData;
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return new JSONObject();
	}

	public static String getUIID(String nodeid) {
		try {
			File uiFile = EditorEnvironment.getFileAboutWorkflowNode(EditorEnvironment.UI_Dir_Name, nodeid,
					EditorEnvironment.Config_UI_Key_Name, GetFileNameType.ftUI, false);
			if (uiFile == null)
				return null;
			return FileHelp.removeExt(uiFile.getName());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String getModelNodeName(String nodeid) throws Exception {
		File file = getModelRelationFileFromNodeID(nodeid);
		try {
			WorkflowCanvas canvas = new WorkflowCanvas();
			canvas.setFile(file);
			canvas.load(new EditorEnvironment.WorkflowDeserializable(), null);
			WorkflowNode node = (WorkflowNode) canvas.getNode(nodeid);
			if (node == null)
				return "";
			return node.name;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static HashMap<String, String> getModelNameAndIds() {
		HashMap<String, String> names = new HashMap<>();
		try {
			traverseModel(new ITraverseDrawNode() {

				@Override
				public boolean onNode(File file, String title, IDrawNode node, Object param) {
					names.put(node.getName(), node.getId());
					return true;
				}
			}, null);
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}

		return names;
	}

	public static File getAppFile(String nodeid, boolean allowNew) {
		try {
			File appFile = EditorEnvironment.getFileAboutWorkflowNode(EditorEnvironment.App_Dir_Name, nodeid,
					EditorEnvironment.Config_App_Key_Name, GetFileNameType.ftApp, allowNew);
			return appFile;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getChildModelRelationName(String nodeid) {
		File file = getChildModelRelationFile(nodeid, false);
		if (file == null)
			return null;

		return getModelRelationName(file);
	}

	/**
	 * 获取模块关系图文件
	 * 
	 * @param childNodeId 子模块id，类型必须为子模块
	 * @return 模块关系文件
	 */
	public static File getChildModelRelationFile(String childNodeId, boolean allowNew) {
		return EditorEnvironment.getFileAboutWorkflowNode(EditorEnvironment.Workflow_Dir_Name, childNodeId,
				EditorEnvironment.Config_SubWorkflow_Key_Name, GetFileNameType.ftWorkflowNode, allowNew);
	}

	public static File getChildModelRelationFile(String projectName, String childNodeId, boolean allowNew) {
		return setFileAboutNode(projectName, Workflow_Dir_Name, Workflow_Dir_Name, childNodeId,
				Config_SubWorkflow_Key_Name, null, GetFileNameType.ftWorkflowNode, false, allowNew);
	}

	public static HashMap<String, String> getFlowRelationNames() {
		HashMap<String, String> names = new HashMap<>();
		File path = getProjectPath(currentProjectName, Flow_Dir_Name);
		for (File file : path.listFiles(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.isFile() && FileHelp.GetExt(f.getName()).compareToIgnoreCase(Relation_File_Extension) == 0;
			}
		})) {

			try {
				FlowCanvas canvas = new FlowCanvas();
				PageConfig pageConfig = canvas.getPageConfig();
				pageConfig.load(file);
				names.put(FileHelp.removeExt(file.getName()), pageConfig.title);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return names;
	}

	public static File getFlowRelationFile(String name) {
		try {
			File flowFile = EditorEnvironment.getProjectFile(EditorEnvironment.Flow_Dir_Name,
					EditorEnvironment.getFlow_FileName(name));

			return flowFile;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static File getRunWorkflowFile(String runWordflowRelationName) {
		File path = EditorEnvironment.getProjectFile(EditorEnvironment.RunFlow_Dir_Name,
				getNodeFileName(runWordflowRelationName));
		return path;
	}

	public static File getToolbarFile(String nodeid, boolean allowNew) {
		try {
			File file = EditorEnvironment.getFileAboutWorkflowNode(EditorEnvironment.Toolbar_Dir_Name, nodeid,
					EditorEnvironment.Config_Toolbar_Key_Name, GetFileNameType.ftWorkflowNode, allowNew);
			return file;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static File getUIFileForUIID(String uiid) {
		File file = EditorEnvironment.getProjectFile(EditorEnvironment.UI_Dir_Name,
				EditorEnvironment.getUI_FileName(uiid));
		return file;
	}

	public static File getUIFile(String nodeid, boolean allowNew) {
		File uiFile = getFileAboutWorkflowNode(UI_Dir_Name, nodeid, Config_UI_Key_Name, GetFileNameType.ftUI, allowNew);
		return uiFile;
	}

	public static File getUIFile(String projectName, String nodeid, boolean allowNew) {
		return setFileAboutNode(projectName, UI_Dir_Name, Workflow_Dir_Name, nodeid, Config_UI_Key_Name, null,
				GetFileNameType.ftUI, false, allowNew);
	}

	public static class SimpleEntry<K, V> implements Entry<K, V> {

		K key;
		V value;

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			this.value = value;
			return value;
		}

		public SimpleEntry(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}

	@SafeVarargs
	public static <K, V> void setMetaInfo(String mainKey, Entry<K, V>... entrys) throws Exception {
		File metaFile = getProjectMetaFile();
		JSONObject data;
		if (metaFile.exists())
			data = (JSONObject) JsonHelp.parseCacheJson(metaFile, null);
		else
			data = new JSONObject();

		JSONObject value = new JSONObject();
		for (Entry<K, V> entry : entrys) {
			value.put(entry.getKey().toString(), entry.getValue());
		}
		data.put(mainKey, value);

		JsonHelp.saveJson(metaFile, data, null);

	}

	public static Object getMetaInfo(String mainKey, String key) throws Exception {
		Map<String, Object> result = getMetaInfo(mainKey, new String[] { key });
		if (result.size() > 0)
			return result.values().iterator().next();
		else
			return null;
	}

	public static Map<String, Object> getMetaInfo(String mainKey, String... keys) throws Exception {
		Map<String, Object> result = new HashMap<>();

		File metaFile = getProjectMetaFile();
		JSONObject data;
		if (metaFile.exists())
			data = (JSONObject) JsonHelp.parseCacheJson(metaFile, null);
		else
			return result;

		if (!data.has(mainKey))
			return result;

		JSONObject value = data.getJSONObject(mainKey);
		for (String key : keys) {
			if (value.has(key)) {
				result.put(key, value.get(key));
			} else
				result.put(key, null);
		}

		return result;
	}

	public static File getProjectMetaFile() {
		return EditorEnvironment.getProjectFile(EditorEnvironment.Config_Dir_Path,
				EditorEnvironment.Main_Meta_FileName);
	}

	public static File getMainNavTreeFile() {
		return EditorEnvironment.getProjectFile(EditorEnvironment.Tree_Dir_Path,
				EditorEnvironment.getTree_FileName(EditorEnvironment.Main_Tree_FileName));
	}

	public static void openHelp(String name) {
		try {
			Desktop.getDesktop().open(getEditorSourcePath("help", name));
		} catch (IOException e) {
		}
	}

	protected static void mergeList(List<String> source, List<String> dest) {
		Map<String, String> idMap = new HashMap<>();
		for (String id : source) {
			idMap.put(id, id);
		}
		for (String id : dest) {
			idMap.put(id, id);
		}

		dest.clear();
		dest.addAll(idMap.values());
	}

	/***
	 * 将源文件与目标文件（当前项目内）合并为一个map
	 * 
	 * @param <T>
	 * @param sourceFile    要合并的源文件，模块图文件或者ui文件
	 * @param destFile      目标文件（当前项目内文件），模块图文件或者ui文件
	 * @param c             要创建的canvas类型
	 * @param sourceNodeMap 返回源文件的所有节点
	 * @param destNodeMap   返回目标文件的所有节点
	 * @param allNodeMap    返回源文件和目标文件包含的所有节点
	 * @return 返回需要处理的canvas对象；如果目标文件不存在，会先将源文件拷贝到目标目录，并建立canvas后返回，否则返回目标canvas
	 * @throws Exception
	 */
	protected static <T extends DrawCanvas> T getMergeMap(File sourceFile, File destFile, Class<T> c,
			Map<String, IDrawNode> sourceNodeMap, Map<String, IDrawNode> destNodeMap, Map<String, IDrawNode> allNodeMap)
			throws Exception {

		if (sourceFile == null || !sourceFile.exists())
			return null;

		T sourceCanvas = loadCanvas(sourceFile, c);
		T destCanvas = (destFile == null || !destFile.exists()) ? null : loadCanvas(destFile, c);

		List<IDrawNode> sourceNodes = sourceCanvas.getNodes();

		for (IDrawNode iDrawNode : sourceNodes) {
			String id = iDrawNode.getId();
			sourceNodeMap.put(id, iDrawNode);
			allNodeMap.put(id, iDrawNode);
		}

		if (destCanvas == null) {
			File copyFile = getModelFile(FileHelp.removeExt(sourceFile.getName()));
			FileHelp.copyFileTo(sourceFile, copyFile);
			destCanvas = loadCanvas(copyFile, c);
		} else {
			List<IDrawNode> destNodes = (destCanvas == null) ? null : destCanvas.getNodes();
			for (IDrawNode iDrawNode : destNodes) {
				String id = iDrawNode.getId();
				destNodeMap.put(id, iDrawNode);
				allNodeMap.put(id, iDrawNode);
			}
		}

		return destCanvas;
	}

	public static void mergeUINode(String nodeid, File sourceUIFile, File destUIFile) throws Exception {
		if (sourceUIFile != null && destUIFile == null) {
			destUIFile = getUIFile(nodeid, true);
//			String uiid = FileHelp.removeExt(destUIFile.getName());
			FileHelp.copyFileTo(sourceUIFile, destUIFile);
			return;
		}

		Map<String, IDrawNode> sourceNodeMap = new HashMap<>();
		Map<String, IDrawNode> destNodeMap = new HashMap<>();
		Map<String, IDrawNode> allNodeMap = new HashMap<>();

		UICanvas canvas = getMergeMap(sourceUIFile, destUIFile, UICanvas.class, sourceNodeMap, destNodeMap, allNodeMap);

		if (canvas == null) {
			return;
		}

		for (IDrawNode node : allNodeMap.values()) {
			UINode sourceNode = (UINode) sourceNodeMap.get(node.getId());
			UINode destNode = (UINode) destNodeMap.get(node.getId());

			if (destNode == null) {
				canvas.addNode(sourceNode, false, false);
			} else if (destNode != null && sourceNode != null) {
				destNode.fromJson(sourceNode.toJson(), new ICreateNodeSerializable() {

					@Override
					public IDrawNode newDrawNode(JSONObject json) {
						return new UINode(canvas);
					}

					@Override
					public IDataSerializable getUserDataSerializable(IDrawNode node) {
						return null;
					}
				});
			}

		}

		canvas.save();
	}

	public static void mergeModelNode(String projectName, WorkflowNode source, WorkflowNode dest, WorkflowCanvas canvas)
			throws Exception {

		if (source == null)
			return;

		if (dest instanceof WorkflowNode.ChildWorkflowNode) {
			File sourceRelationFile = getChildModelRelationFile(projectName, source.getId(), false);
			File destRelationFile = (dest == null ? null : getChildModelRelationFile(dest.getId(), false));
			mergeModelRelationFile(projectName, sourceRelationFile, destRelationFile);
		} else if (dest instanceof WorkflowNode.BeginNode || dest instanceof WorkflowNode.EndNode) {
			return;
		} else {
			File sourceUIFile = getUIFile(projectName, source.getId(), false);
			File destUIFile = (dest == null ? null : getUIFile(dest.getId(), false));

			if (dest == null) {
				canvas.addNode(source, false, false);
			} else {
				mergeList(source.getPrevs(), dest.getPrevs());
				mergeList(source.getNexts(), dest.getNexts());
			}

			if (destUIFile == null && sourceUIFile == null)
				return;

			mergeUINode(dest == null ? source.getId() : dest.getId(), sourceUIFile, destUIFile);
		}
	}

	public static void mergeModelRelationFile(String projectName, File sourceRelationFile, File destRelationFile)
			throws Exception {
		Map<String, IDrawNode> sourceNodeMap = new HashMap<>();
		Map<String, IDrawNode> destNodeMap = new HashMap<>();
		Map<String, IDrawNode> allNodeMap = new HashMap<>();

		WorkflowCanvas canvas = getMergeMap(sourceRelationFile, destRelationFile, WorkflowCanvas.class, sourceNodeMap,
				destNodeMap, allNodeMap);

		if (canvas == null)
			return;
		
		for (IDrawNode node : allNodeMap.values()) {
			try {
				mergeModelNode(projectName, (WorkflowNode) sourceNodeMap.get(node.getId()),
						(WorkflowNode) destNodeMap.get(node.getId()), canvas);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		canvas.save();
	}

	public static WorkflowCanvas loadModelCanvas(File relationFile) throws Exception {
		return loadCanvas(relationFile, WorkflowCanvas.class);
	}

	public static UICanvas loadUICanvas(File relationFile) throws Exception {
		return loadCanvas(relationFile, UICanvas.class);
	}

	public static <T extends DrawCanvas> T loadCanvas(File file, Class<T> c) throws Exception {
		T canvas = c.getConstructor().newInstance();
		canvas.setFile(file);
		canvas.load(new EditorEnvironment.WorkflowDeserializable(), null);
		return canvas;
	}

	public static void backupProject(String projectName, File destProjectBackupPath) throws IOException {
		File destProjectPath = EditorEnvironment.getBasePath(EditorEnvironment.Project_Root_Dir_Name, projectName);

		if (destProjectBackupPath.exists())
			if (!FileHelp.delDir(destProjectBackupPath))
				throw new IOException("删除备份目录[" + destProjectBackupPath.getAbsolutePath() + "]失败！");

		if (!FileHelp.copyFilesTo(destProjectPath, destProjectBackupPath))
			throw new IOException("备份当前项目失败！");

	}

	public static void mergeProject(String projectName, boolean mergeMainMenu, boolean mergeMainNav,
			boolean mergeImage, boolean mergeHeader, boolean mergeFooter, boolean mergeLeftNav, boolean mergeRightNav,
			boolean mergeReport) {
		
		WaitDialog.Show("合并项目", "正在合并，请等待。。。", new IProcess() {

			@Override
			public boolean doProc(WaitDialog waitDialog) throws Exception {

				File destProjectPath = EditorEnvironment.getBasePath(EditorEnvironment.Project_Root_Dir_Name,
						EditorEnvironment.getCurrentProjectName());

				File destProjectBackupPath = new File(destProjectPath.getAbsolutePath() + "_backup");

				backupProject(currentProjectName, destProjectBackupPath);

//				File sourceProjectPath = EditorEnvironment.getBasePath(EditorEnvironment.Project_Root_Dir_Name,
//						projectName);

				try {

					File destMainRelationFile = EditorEnvironment.getProjectFile(EditorEnvironment.Workflow_Dir_Name,
							EditorEnvironment.getMainModelRelationFileName());

					File sourceMainRelationFile = EditorEnvironment.getProjectFile(projectName,
							EditorEnvironment.Workflow_Dir_Name, EditorEnvironment.getMainModelRelationFileName());

					if (mergeMainMenu)
						copyDir(EditorEnvironment.Menu_Dir_Path, projectName);
					if (mergeMainNav)
						copyDir(EditorEnvironment.Tree_Dir_Path, projectName);
					if (mergeImage)
						copyDir(EditorEnvironment.Image_Resource_Path, projectName);
					if (mergeReport)
						copyDir(EditorEnvironment.Report_Dir_Path, projectName);

					if (mergeHeader) {
						File destFrameFile = getFrameModelNodeFile(RegionName.rnTop);
						File sourceFrameFile = getFrameModelNodeFile(projectName, RegionName.rnTop);
						mergeModelRelationFile(projectName, sourceFrameFile, destFrameFile);
					}
					if (mergeFooter) {
						RegionName regionName = RegionName.rnBottom;
						File destFrameFile = getFrameModelNodeFile(regionName);
						File sourceFrameFile = getFrameModelNodeFile(projectName, regionName);
						mergeModelRelationFile(projectName, sourceFrameFile, destFrameFile);
					}
					if (mergeLeftNav) {
						RegionName regionName = RegionName.rnLeft;
						File destFrameFile = getFrameModelNodeFile(regionName);
						File sourceFrameFile = getFrameModelNodeFile(projectName, regionName);
						mergeModelRelationFile(projectName, sourceFrameFile, destFrameFile);
					}
					if (mergeRightNav) {
						RegionName regionName = RegionName.rnRight;
						File destFrameFile = getFrameModelNodeFile(regionName);
						File sourceFrameFile = getFrameModelNodeFile(projectName, regionName);
						mergeModelRelationFile(projectName, sourceFrameFile, destFrameFile);
					}

					mergeModelRelationFile(projectName, sourceMainRelationFile, destMainRelationFile);

					return true;
				} catch (Exception e1) {
					FileHelp.delDir(destProjectPath);
					FileHelp.copyFilesTo(destProjectBackupPath, destProjectPath);
					FileHelp.delDir(destProjectBackupPath);
					throw e1;
				}
			}

			@Override
			public void closed(boolean isok, Throwable e) {
				if (isok) {
					MsgHelper.showMessage(null, "合并项目【" + projectName + "】成功！", "合并项目",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					MsgHelper.showException(e);
				}
			}
		}, null);

	}

	public static void importDispatchProject(String projectName, IDispatchCallback onCallback) {
		String destProjectName = PinyinHelp.getFullSpell(projectName);
		WaitDialog.Show("导入分发项目", "正在导入，请等待。。。", new IProcess() {

			@Override
			public boolean doProc(WaitDialog waitDialog) {
				File dispatchPath = EditorEnvironment.getBasePath(EditorEnvironment.Workflow_Dispatch_Dir_Name,
						projectName);

				File workflowPath = EditorEnvironment.getBasePath(EditorEnvironment.Project_Root_Dir_Name,
						destProjectName);
				try {
					if (workflowPath.exists()) {
						if (MsgHelper.showConfirmDialog(
								"项目【" + workflowPath.getAbsolutePath() + "】已经存在，继续将覆盖同名项目，是否继续？", "导入分发项目",
								JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
							return false;

						if (!FileHelp.delDir(workflowPath)) {
							throw new IOException("delete dir[" + workflowPath.getAbsolutePath() + "] is fail!");
						}
					}
					FileHelp.copyFilesTo(dispatchPath, workflowPath);
					return true;
				} catch (Exception e1) {
					MsgHelper.showException(e1);
					return false;
				}
			}

			@Override
			public void closed(boolean isok, Throwable e) {
				if (isok) {
					if (onCallback != null)
						onCallback.ondo(destProjectName);
				} else {
					MsgHelper.showException(e);
				}
			}
		}, null);

	}

	static {
		setProjectBasePath();
	}
}
