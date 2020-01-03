package com.wh.tools;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public abstract class MessageDialog {

	public static void showMessage(Object message) {
		if (SwingUtilities.isEventDispatchThread()) {
			JOptionPane.showMessageDialog(null, message, "消息", JOptionPane.OK_OPTION);
		} else
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						showMessage(message);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}
