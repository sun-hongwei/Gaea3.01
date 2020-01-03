package com.wh.tools;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

public class ClipboardHelper {
	public static void copy(Object obj) {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();// 获取系统剪切板
		StringSelection selection = new StringSelection(obj.toString());// 构建String数据类型
		clipboard.setContents(selection, selection);// 添加文本到系统剪切板
	}
	
	public static String paste() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();// 获取系统剪切板
		Transferable content = clipboard.getContents(null);// 从系统剪切板中获取数据
		if (content.isDataFlavorSupported(DataFlavor.stringFlavor)) {// 判断是否为文本类型
			String text;
			try {
				text = (String) content.getTransferData(DataFlavor.stringFlavor);
				// 从数据中获取文本值
				if (text == null || text.isEmpty()) {
					return null;
				}

				return text;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;

	}
}
