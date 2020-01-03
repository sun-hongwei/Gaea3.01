package com.wh.gaea.interfaces;

import java.io.File;

public class ResultModelRelationInfo {
	public File modelRelationFile;
	public IDrawNode containChildNode;

	public ResultModelRelationInfo(File modelRelationFile, IDrawNode containChildNode) {
		this.modelRelationFile = modelRelationFile;
		this.containChildNode = containChildNode;
	}
}