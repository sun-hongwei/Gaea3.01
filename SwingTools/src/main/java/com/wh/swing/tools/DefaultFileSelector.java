package com.wh.swing.tools;

import java.io.File;
import java.util.List;

import com.wh.swing.tools.dialog.AbstractFileSelector;
import com.wh.tools.FileHelp;

public class DefaultFileSelector extends AbstractFileSelector {
	
	private static final long serialVersionUID = 1L;

	public static List<FileInfo> show(File initPath, File selected, String title, DialogType dt, boolean mulitSelect,
			Ext[] exts) {
		return AbstractFileSelector.show(initPath, selected, title, dt, mulitSelect, exts, new INewFileSelector() {
			
			@Override
			public AbstractFileSelector createFileSelector() {
				return new DefaultFileSelector();
			}
		});
	}

	@Override
	public File getDefaultPath() {
		return FileHelp.getRootPath();
	}
}
