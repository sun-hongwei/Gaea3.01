package com.wh.gaea.draws.drawinfo.control;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wh.gaea.draw.control.StatckTreeElement;
import com.wh.gaea.draws.UINode;
import com.wh.gaea.draws.drawinfo.DivInfo;
import com.wh.gaea.draws.drawinfo.DivInfo.DivType;
import com.wh.gaea.interfaces.IDrawCanvas;
import com.wh.gaea.interfaces.IDrawNode;

public class StackTreeManager {

	public static final int Min_Index = 0;
	public static final int Max_Index = 1;

	public HashMap<String, StatckTreeElement> roots = new HashMap<>();
	public HashMap<String, StatckTreeElement> elements = new HashMap<>();

	public IDrawCanvas canvas;

	public StackTreeManager(IDrawCanvas canvas) {
		this.canvas = canvas;
	}

	public boolean inPath(String elementid, String checkid) {
		List<String> ids = new ArrayList<>();
		ids.add(checkid);
		return inPath(elementid, ids);
	}

	public boolean isVisiable(IDrawNode parent, IDrawNode node) {
		if (parent == null)
			return canvas.getClipRect().intersects(node.getRect());

		if (!isVisiable(parent.getId()))
			return false;

		if (!parent.getRect().intersects(node.getRect()))
			return false;

		return isVisiable(getRoot(parent.getId()), node);
	}

	public boolean isVisiable(String id) {
		IDrawNode node = (IDrawNode) canvas.getNode(id);
		return isVisiable(node);
	}

	public boolean isVisiable(IDrawNode node) {
		if (node == null)
			return false;
		IDrawNode parent = getRoot(node.getId());
		return isVisiable(parent, node);
	}

	public boolean inPath(String elementid, Collection<String> checkids) {
		if (!elements.containsKey(elementid))
			return false;

		HashMap<String, String> keys = new HashMap<>();
		for (String id : checkids) {
			keys.put(id, id);
		}

		List<String> parents = new ArrayList<>();
		getParents(elementid, parents);
		for (String pid : parents) {
			if (keys.containsKey(pid))
				return true;
		}

		return false;
	}

	public int[] getMinAndMaxOrder(String id, List<IDrawNode> result) {
		TreeMap<Integer, List<IDrawNode>> orders = new TreeMap<>();
		HashMap<String, Integer> indexs = new HashMap<>();
		int[] mms = getMinAndMaxOrder(id, orders, indexs);
		if (orders.size() > 0) {
			result.addAll(orders.get(orders.firstKey()));
		}
		return mms;
	}

	public int[] getMinAndMaxOrder(String id) {
		TreeMap<Integer, List<IDrawNode>> result = new TreeMap<>();
		HashMap<String, Integer> indexs = new HashMap<>();
		return getMinAndMaxOrder(id, result, indexs);
	}

	public int getParentZOrder(String id) {
		List<String> parents = new ArrayList<>();
		getParents(id, parents);
		if (parents.size() > 0) {
			return canvas.getNode(parents.get(0)).getZOrder();
		} else
			return canvas.getNode(id).getZOrder();
	}

	public IDrawNode getRoot(String id) {
		if (!elements.containsKey(id))
			return null;

		String parentid = null;
		String linkid = elements.get(id).parentid;
		while (linkid != null && !linkid.isEmpty()) {
			parentid = linkid;
			linkid = elements.get(linkid).parentid;
		}
		return (IDrawNode) canvas.getNode(parentid);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void addNodeAndAllParentsToHashmap(HashMap hashMap, String id) {
		List<String> parents = new ArrayList<>();
		getParents(id, parents);
		for (String pid : parents) {
			hashMap.put(pid, id);
		}
		hashMap.put(id, id);
	}

	public IDrawNode NodeOfPoint(Point pt) {
		TreeMap<Integer, HashMap<String, String>> sortNodes = new TreeMap<>();
		for (StatckTreeElement treeNode : new ArrayList<>(elements.values())) {
			IDrawNode node = (IDrawNode) canvas.getNode(treeNode.id);
			if (!isVisiable(node.getId()))
				continue;

			if (node.isPoint(pt)) {
				HashMap<String, String> hashMap;
				int z = getParentZOrder(node.getId());
				if (sortNodes.containsKey(z)) {
					hashMap = sortNodes.get(z);
				} else {
					hashMap = new HashMap<>();
					sortNodes.put(z, hashMap);
				}
				if (hashMap.containsKey(node.getId()))
					continue;

				addNodeAndAllParentsToHashmap(hashMap, node.getId());
			}
		}
		if (sortNodes.size() == 0)
			return null;
		else {
			int max = -999;
			IDrawNode result = null;
			for (String id : sortNodes.get(sortNodes.lastKey()).values()) {
				IDrawNode node = (IDrawNode) canvas.getNode(id);
				if (node.getZOrder() > max) {
					max = node.getZOrder();
					result = node;
				}
			}
			return result;
		}
	}

	int[] getMinAndMaxOrder(String id, TreeMap<Integer, List<IDrawNode>> result, HashMap<String, Integer> indexs) {
		if (!elements.containsKey(id))
			throw new NullPointerException("未发现id：" + id + "的层叠节点！");

		IDrawNode cur = (IDrawNode) canvas.getNode(id);
		IDrawNode parent = getParent(id);
		int min = Integer.MAX_VALUE;

		String parentid = cur.getId();
		if (parent == null)
			min = canvas.getZOrderMinAndMax()[0];
		else
			parentid = parent.getId();

		int max = 0;

		getSortedChilds(parentid, result, indexs);

		if (result.size() > 0) {
			boolean needMin = parent != null;
			for (IDrawNode node : result.get(result.firstKey())) {
				if (needMin) {
					if (node.getZOrder() < min)
						min = node.getZOrder();
				}

				if (node.getZOrder() > max)
					max = node.getZOrder();
			}
		}

		return new int[] { min, max };
	}

	public void getSortedChilds(String id, TreeMap<Integer, List<IDrawNode>> result,
			HashMap<String, Integer> indexs) {
		if (!elements.containsKey(id)) {
			return;
		}

		StatckTreeElement drawTree = elements.get(id);
		int index = result.size();
		List<IDrawNode> childs = new ArrayList<>();
		result.put(index, childs);
		for (String childid : drawTree.childs.values()) {
			childs.add((IDrawNode)canvas.getNode(childid));
			indexs.put(childid, index);
		}
		for (String childid : drawTree.childs.values()) {
			getSortedChilds(childid, result, indexs);
		}
	}

	/**
	 * 获取指定id的node的所有子控件
	 * @param id 要获取的节点id
	 * @param result 所有子控件列表
	 */
	public void getChilds(String id, List<StatckTreeElement> result) {
		if (!elements.containsKey(id)) {
			return;
		}

		StatckTreeElement drawTree = elements.get(id);
		for (String childid : new ArrayList<>(drawTree.childs.values())) {
			if (canvas.containsNode(childid)) {
				result.add(elements.get(childid));
				getChilds(childid, result);
			} else {
				elements.remove(childid);
				roots.remove(childid);
			}
		}
	}

	public boolean bringToTop(IDrawNode node) {
		if (elements.containsKey(node.getId()) && !roots.containsKey(node.getId())) {
			int[] minAndMax = getMinAndMaxOrder(node.getId());
			node.setZOrder(minAndMax[Max_Index] + 1);
			return true;
		}
		return false;
	}

	public boolean sendToBack(IDrawNode node) {
		if (elements.containsKey(node.getId()) && !roots.containsKey(node.getId())) {
			List<IDrawNode> result = new ArrayList<>();
			int[] minAndMax = getMinAndMaxOrder(node.getId(), result);
			node.setZOrder(minAndMax[Min_Index] - 1);
			if (node.getZOrder() < 0) {
				int fixOrder = Math.abs(node.getZOrder()) + 1;
				for (IDrawNode n : result) {
					n.setZOrder(n.getZOrder() + fixOrder);
				}
				node.setZOrder(0);
			}
			return true;
		}
		return false;
	}

	public JSONObject toJson() throws JSONException {
		JSONObject json = new JSONObject();
		JSONArray nodedatas = new JSONArray();
		for (StatckTreeElement treeNode : elements.values()) {
			JSONObject data = treeNode.toJson();
			nodedatas.put(data);
		}

		json.put("nodes", nodedatas);

		JSONArray rootDatas = new JSONArray();
		for (String id : roots.keySet()) {
			rootDatas.put(id);
		}
		json.put("roots", rootDatas);
		return json;
	}

	public void fromJson(JSONObject json) throws JSONException {
		JSONArray nodes = json.getJSONArray("nodes");
		this.elements.clear();
		this.roots.clear();

		for (int i = 0; i < nodes.length(); i++) {
			JSONObject treeData = nodes.getJSONObject(i);
			StatckTreeElement drawTree = new StatckTreeElement(canvas);
			drawTree.fromJson(treeData);
			this.elements.put(drawTree.id, drawTree);
		}

		List<IDrawNode> IDrawNodes = new ArrayList<>();
		JSONArray rootdatas = json.getJSONArray("roots");
		for (int i = 0; i < rootdatas.length(); i++) {
			String id = rootdatas.getString(i);
			roots.put(id, this.elements.get(id));
			IDrawNodes.add((IDrawNode)canvas.getNode(id));
		}

		// SwingUtilities.invokeLater(new Runnable() {
		//
		// @Override
		// public void run() {
		// resetTree(IDrawNodes);
		// }
		// });
	}

	public IDrawNode getParent(String id) {
		IDrawNode cur = (IDrawNode) canvas.getNode(id);
		if (cur == null) {
			throw new NullPointerException("未找到此节点！");
		}

		IDrawNode parentNode = getTop(cur);
		if (parentNode != null)
			return parentNode;

		for (IDrawNode node : canvas.getNodes()) {
			if (node.isParent(cur)) {
				if (parentNode == null) {
					if (node.isDrawTreeRoot()) {
						parentNode = (IDrawNode) node;
					}
				} else {
					if (parentNode.getZOrder() < node.getZOrder()) {
						parentNode = (IDrawNode) node;
					}

				}
			}
		}

		return parentNode;
	}

	protected void getParents(String id, List<String> parents) {
		IDrawNode parent = getParent(id);
		if (parent == null)
			return;

		parents.add(0, parent.getId());
		getParents(parent.getId(), parents);
	}

	public void resetTree(Collection<IDrawNode> nodes) {
		resetTree(nodes, false);
	}
	
	public void resetTree(Collection<IDrawNode> nodes, boolean autoAdd) {
		HashMap<String, String> checkNodes = new HashMap<>();
		HashMap<String, String> treeNodes = new HashMap<>();
		for (IDrawNode inode : nodes) {
			IDrawNode node = (IDrawNode) inode;
			StatckTreeElement olDrawTree = null;
			String oldParentID = "";
			if (this.elements.containsKey(node.getId())) {
				olDrawTree = this.elements.get(node.getId());
				oldParentID = olDrawTree.parentid == null ? "" : olDrawTree.parentid;
			}

			StatckTreeElement treeNode = autoAdd ? add(node.getId()) : elements.get(node.getId());
			if (treeNode == null) {
				if (olDrawTree != null) {
					if (olDrawTree.childs.size() == 0) {
						remove(node.getId());
						continue;
					} else {
						if (olDrawTree.parentid != null && !olDrawTree.parentid.isEmpty()) {
							StatckTreeElement parent = this.elements.get(olDrawTree.parentid);
							parent.childs.remove(olDrawTree.id);
							olDrawTree.parentid = null;
						}
						roots.put(olDrawTree.id, olDrawTree);
						treeNode = olDrawTree;
					}
				} else {
					continue;
				}
			} else {
				String curParentID = treeNode.parentid == null ? "" : treeNode.parentid;
				if (oldParentID.compareTo(curParentID) != 0 && !oldParentID.isEmpty()) {
					StatckTreeElement parent = this.elements.get(oldParentID);
					parent.childs.remove(olDrawTree.id);
				}

				if (curParentID != null && !curParentID.isEmpty()) {
					if (roots.containsKey(treeNode.id)) {
						roots.remove(treeNode.id);
					}
				}
			}

			checkNodes.put(treeNode.id, treeNode.id);
			treeNodes.put(treeNode.id, treeNode.id);
		}

		resetTree(treeNodes, checkNodes, false);
	}

	void resetTree(HashMap<String, String> treeNodes, HashMap<String, String> checkNodes, boolean needRelocation) {
		for (String treeid : treeNodes.values()) {
			StatckTreeElement treeNode = this.elements.get(treeid);
			if (needRelocation) {
				IDrawNode IDrawNode = canvas.getNode(treeNode.id);
				IDrawNode parentNode = canvas.getNode(treeNode.parentid);
				StatckTreeElement parentTreeNode = this.elements.get(treeNode.parentid);
				Rectangle r = parentNode.getRect();
				int x = r.x, y = r.y;

				if (!checkNodes.containsKey(treeNode.id)) {
					x = r.x + (IDrawNode.getRect().x - parentTreeNode.location.x);
					y = r.y + (IDrawNode.getRect().y - parentTreeNode.location.y);
					Rectangle rectangle = new Rectangle(x, y, IDrawNode.getRect().width, IDrawNode.getRect().height);
					IDrawNode.setRect(rectangle);
				}
			}

			if (treeNode.childs.size() > 0) {
				for (String id : new ArrayList<>(treeNode.childs.keySet())) {
					StatckTreeElement node = this.elements.get(id);
					if (node == null || node.parentid == null || node.parentid.isEmpty())
						remove(id);
				}

				treeNode = this.elements.get(treeNode.id);
				if (treeNode != null) {
					resetTree(treeNode.childs, checkNodes, true);
				}
			}
		}

		for (String id : treeNodes.values()) {
			IDrawNode IDrawNode = canvas.getNode(id);
			StatckTreeElement treeNode = this.elements.get(id);
			treeNode.location = IDrawNode.getRect().getLocation();
		}
		canvas.repaint();
	}

	public void getTopForRoot(IDrawNode root, IDrawNode cur, TreeMap<Integer, IDrawNode> nodes) {
		if (root.isParent(cur)) {
			nodes.put(-1, root);
		}
		TreeMap<Integer, List<IDrawNode>> result = new TreeMap<>();
		HashMap<String, Integer> indexs = new HashMap<>();
		getSortedChilds(root.getId(), result, indexs);
		for (String id : indexs.keySet()) {
			IDrawNode node = (IDrawNode) canvas.getNode(id);
			if (node == null)
				continue;
			if (node.isParent(cur)) {
				int key = indexs.get(id);
				if (!nodes.containsKey(key)) {
					nodes.put(key, node);
				} else {
					if (nodes.get(key).getZOrder() < node.getZOrder()) {
						nodes.put(key, node);
					}
				}
			}
		}
	}

	public IDrawNode getTop(IDrawNode cur) {
		TreeMap<Integer, IDrawNode> nodes = new TreeMap<>();
		for (String rootid : new ArrayList<>(roots.keySet())) {
			IDrawNode rootNode = (IDrawNode) canvas.getNode(rootid);
			getTopForRoot(rootNode, cur, nodes);
		}

		if (nodes.size() == 0)
			return null;
		else {
			return nodes.get(nodes.lastKey());
		}
	}

	StatckTreeElement add(String nodeid) {
		List<String> parents = new ArrayList<>();
		getParents(nodeid, parents);
		if (parents.size() == 0) {
			return null;
		}

		for (int i = parents.size() - 1; i >= 0; i--) {
			String id = parents.get(i);
			IDrawNode iDrawNode = canvas.getNode(id);
			
			if (iDrawNode instanceof UINode){
				UINode uiNode = (UINode)iDrawNode;
				if (uiNode.getDrawInfo() instanceof DivInfo){
					DivInfo divInfo = (DivInfo)uiNode.getDrawInfo();
					if (divInfo.divType != DivType.dtDiv){
						parents.remove(i);
					}
				}
			}
		}
		
		parents.add(nodeid);

		String pid = null;
		for (int i = 0; i < parents.size(); i++) {
			String id = parents.get(i);
			StatckTreeElement node;
			if (!elements.containsKey(id)) {
				node = new StatckTreeElement(canvas, id, pid);
				if (i == 0) {
					roots.put(node.id, node);
				}
				elements.put(node.id, node);
			} else {
				node = elements.get(id);
			}
			node.parentid = pid;
			if (i > 0) {
				elements.get(pid).childs.put(node.id, node.id);
			}

			pid = id;
		}
		StatckTreeElement drawTree = elements.get(nodeid);
		return drawTree;
	}

	public void clear() {
		roots.clear();
		elements.clear();
	}

	public void removeTree(String id, boolean removeChild) {
		removeTree(id, removeChild, true);
	}
	
	public void removeTree(String id, boolean removeChild, boolean recursive) {
		if (!elements.containsKey(id))
			return;

		if (roots.containsKey(id))
			roots.remove(id);

		StatckTreeElement node = elements.remove(id);
		if (node.parentid != null && !node.parentid.isEmpty()) {
			if (elements.containsKey(node.parentid)) {
				elements.get(node.parentid).childs.remove(node.id);
			}
		}

		node.parentid = null;

		if (removeChild) {
			for (String childid : node.childs.keySet()) {
				removeTree(childid, recursive ? true : false, recursive);
			}
			node.childs.clear();
		} else {
			if (node.childs.size() > 0) {
				roots.put(id, node);
				elements.put(id, node);
			}
		}
	}

	public void remove(String id) {
		removeTree(id, false);
	}

	public void checkNodes() {
		List<StatckTreeElement> nodes = new ArrayList<>(elements.values());
		for (StatckTreeElement node : nodes) {
			if (!canvas.containsNode(node.id))
				removeTree(node.id, false);
			else
				for (String id : node.childs.keySet()) {
					if (!canvas.containsNode(id))
						removeTree(node.id, false);
				}
		}
	}

}

