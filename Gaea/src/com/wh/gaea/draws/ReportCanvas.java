package com.wh.gaea.draws;

import java.awt.Font;
import java.awt.Graphics;
import java.util.Collection;

import com.wh.gaea.draws.DrawCanvas;
import com.wh.gaea.interfaces.IDrawNode;

public class ReportCanvas extends DrawCanvas {

	private static final long serialVersionUID = 1L;

	protected boolean allowPaste(IDrawNode node){
		return node instanceof UINode;
	}
	
	protected void paintNodes(Graphics g, Collection<IDrawNode> nodes, boolean needCheckViewport){
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

}
