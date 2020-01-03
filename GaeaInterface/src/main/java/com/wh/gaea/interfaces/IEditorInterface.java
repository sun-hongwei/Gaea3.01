package com.wh.gaea.interfaces;

import java.io.File;
import java.util.HashMap;
import java.util.Map.Entry;

public interface IEditorInterface {
	public enum RegionName {
		rnLeft, rnTop, rnBottom, rnRight
	}

	public enum GetFileNameType {
		ftWorkflowNode, ftUI, ftApp, ftToolbar
	}

	public static class NodeDescInfo {
		public String workflowRelationName;
		public String id;
		public String title;
		public String memo;
	}

	public interface IPublish {
		public void publishContents() throws Exception;
	}

	public interface ITraverseDrawNode {
		public boolean onNode(File relationFile, String title, IDrawNode node, Object param);
	}

	public interface ICheckCallBack {
		public boolean onRepeat(String title, File file, IDrawNode node, File repeatFile, IDrawNode repeatNode,
				Object param);
	}

	public interface ITraverseUIFile {
		/**
		 * @param file 当前ui的界面信息保存文件
		 * @parram canvas 当前ui的canvas
		 * @return 返回true表示已经修改文件，需要保存变动到文件，返回false表示不需要修改文件
		 */
		boolean callback(File uiFile, IDrawCanvas canvas, Object userObject);
	}

	public static class ModelNodeInfo {
		public IDrawNode node;
		public String title;
		public HashMap<String, IDrawNode> nodes = new HashMap<>();
	}

	public enum CheckType {
		ctName, ctID
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

	public interface IDispatchCallback {
		void ondo(String newProjectName);
	}

}