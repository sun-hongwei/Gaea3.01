package com.wh.gaea.draws;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.json.JSONObject;

import com.wh.gaea.control.EditorEnvironment;
import com.wh.gaea.draws.WorkflowNode.BeginNode;
import com.wh.gaea.draws.WorkflowNode.EndNode;
import com.wh.gaea.interfaces.IDataSerializable;
import com.wh.gaea.interfaces.IDrawNode;
import com.wh.swing.tools.MsgHelper;
import com.wh.tools.FileHelp;
import com.wh.tools.JsonHelp;

public class WorkflowCanvas extends DrawCanvas {

	private static final long serialVersionUID = 1L;

	protected boolean allowPaste(IDrawNode node) {
		return node instanceof WorkflowNode;
	}

	@Override
	protected void endPaste(List<IDrawNode> nodes) {
		if (nodes == null)
			return;

		for (IDrawNode iDrawNode : nodes) {
			DrawNode drawNode = (DrawNode) iDrawNode;
			String filename = drawNode.copy_filename;
			if (filename == null)
				continue;
			File source = new File(filename);

			if (source.exists()) {

				File dest = EditorEnvironment.getModelNodeFile(drawNode.id);

				if (dest.equals(source))
					continue;

				try {
					if (!dest.exists())
						FileHelp.copyFileTo(source, dest);
					JSONObject nodeData = (JSONObject) JsonHelp.parseCacheJson(dest, null);
					if (nodeData.has("ui")) {
						source = new File(source.getParentFile().getParentFile(),
								"ui/" + EditorEnvironment.getUI_FileName(nodeData.getString("ui")));
						if (!source.exists())
							continue;

						dest = EditorEnvironment.getProjectFile(EditorEnvironment.UI_Dir_Name, source.getName());

						if (!dest.exists())
							FileHelp.copyFileTo(source, dest);
					}
					
					if (nodeData.has("app")){
						source = new File(source.getParentFile().getParentFile(),
								"app/" + EditorEnvironment.getApp_FileName(nodeData.getString("app")));
						if (!source.exists())
							continue;

						dest = EditorEnvironment.getProjectFile(EditorEnvironment.App_Dir_Name, source.getName());

						if (!dest.exists())
							FileHelp.copyFileTo(source, dest);
						
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					MsgHelper.showException(e1);
					drawNode.userData = null;

					return;
				}
			} else
				drawNode.userData = null;

		}

	}

	protected void changeNodeId(String newId, String oldId) throws IOException {
		File file = EditorEnvironment.getModelFile(oldId);
		if (file != null && file.exists()) {
			File destFile = new File(file.getParentFile(), EditorEnvironment.getNodeFileName(newId));
			FileHelp.copyFileTo(file, destFile);
		}
	}

	protected void changePasteNode(DrawNode node) throws IOException {
		String oldId = node.id;
		super.changePasteNode(node);
		String newId = node.id;
		changeNodeId(newId, oldId);
	}

	protected void updateCanvasSize(Rectangle oldUseRect) {
		if (useRect.width == 0 || useRect.height == 0) {
			useRect = oldUseRect;
		}

	}

	protected void paintNodes(Graphics g, Collection<IDrawNode> nodes, boolean needCheckViewport) {
		for (IDrawNode node : nodes) {
			node.drawLins(g);
		}

        for (IDrawNode inode : nodes) {
        	DrawNode node = (DrawNode) inode;
			Font oldfont = g.getFont();

			g.setFont(node.font);

			node.draw(g, needCheckViewport);

			g.setFont(oldfont);
		}
	}

	public WorkflowNode add(String name, String title, Class<? extends WorkflowNode> c, Object userData,
			IDataSerializable dataSerializable) {
		WorkflowNode node = (WorkflowNode) add(name, title, new Rectangle(0, 0, 100, 100), userData,
				new IDataSerializable() {

					@Override
					public String save(Object userData) {
						return dataSerializable.save(userData);
					}

					@Override
					public DrawNode newDrawNode(Object userdata) {
						try {
							WorkflowNode node = IDrawNode.newInstance(c, WorkflowCanvas.this);
							return node;
						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}

					@Override
					public Object load(String value) {
						return dataSerializable.load(value);
					}

					@Override
					public void initDrawNode(IDrawNode node) {
						dataSerializable.initDrawNode(node);
					}
				});

		if (node instanceof BeginNode || node instanceof EndNode) {
			node.getRect().height = 30;
		}
		return node;
	}

}
