package com.wh.swing.tools;

import java.awt.Component;
import java.awt.Font;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.wh.swing.tools.dialog.input.TextInput;

public class MsgHelper {

	public static void showException(Throwable e) {
		showException(e, "异常");
	}

	public static void showException(Throwable e, String title) {
		showException(e, title, "");
	}

	public static void showException(Throwable e, String title, String msgs) {
		if (e == null && (msgs == null || msgs.isEmpty()))
			return;
		
		String msg = e.toString();
		if (msg == null || msg.isEmpty())
			msg = e.getClass().getName();

		if (msgs != null && !msgs.isEmpty())
			msg += ":" + msgs;
		showMessage(null, msg, title, JOptionPane.ERROR_MESSAGE);
	}

	public static void showException(String msg) {
		showException("失败", msg);
	}

	public static void showException(String title, String msg) {
		showMessage(null, msg, title, JOptionPane.ERROR_MESSAGE);
	}

	public static void showWarn(String msg) {
		showWarn("提醒", msg);
	}

	public static void showWarn(String title, String msg) {
		showMessage(null, msg, title, JOptionPane.WARNING_MESSAGE);
	}

	protected static Object[][] initMsgButtons(int buttonOptions) {
		JOptionPane.setDefaultLocale(Locale.CHINA);

		// 设置标题字体
		UIManager.put("OptionPane.font", new FontUIResource(new Font("微软雅黑", Font.PLAIN, 13)));
		// 设置按钮字体
		UIManager.put("OptionPane.buttonFont", new FontUIResource(new Font("微软雅黑", Font.PLAIN, 12)));
		// 设置文本显示效果
		UIManager.put("OptionPane.messageFont", new FontUIResource(new Font("微软雅黑", Font.PLAIN, 12)));

		Object[][] result = new Object[][] { null, new Object[] { null } };
		switch (buttonOptions) {
		case JOptionPane.YES_NO_CANCEL_OPTION:
			result[0] = new Object[] { "是", "否", "取消" };
			result[1][0] = result[0][0];
			break;
		case JOptionPane.OK_CANCEL_OPTION:
			result[0] = new Object[] { "确定", "取消" };
			result[1][0] = result[0][0];
			break;
		case JOptionPane.YES_NO_OPTION:
			result[0] = new Object[] { "是", "否" };
			result[1][0] = result[0][0];
			break;
		case JOptionPane.DEFAULT_OPTION:
			result[0] = new Object[] { "确定" };
			result[1][0] = result[0][0];
			break;
		default:
			break;
		}
		return result;
	}

	public static void throwMessage(Object message) throws Exception {
		throw new Exception(message.toString());
	}

	public static void showMessage(Object message) {
		showMessage(null, message, "消息");
	}

	public static void showMessage(Component parentComponent, Object message) {
		showMessage(parentComponent, message, "消息");
	}

	public static void showMessage(Component parentComponent, Object message, String title) {
		showMessage(parentComponent, message, title, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void showMessage(Component parentComponent, Object message, String title, int messageType) {
		if (SwingUtilities.isEventDispatchThread()) {
			Object[][] options = initMsgButtons(JOptionPane.DEFAULT_OPTION);
			JOptionPane.showOptionDialog(parentComponent, message, title, JOptionPane.DEFAULT_OPTION, messageType, null,
					options[0], options[1][0]);
		} else
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						Object[][] options = initMsgButtons(JOptionPane.DEFAULT_OPTION);
						JOptionPane.showOptionDialog(parentComponent, message, title, JOptionPane.DEFAULT_OPTION,
								messageType, null, options[0], options[1][0]);
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public static int showQuestionDialog(Object message) {
		return showConfirmDialog(message, "提示", JOptionPane.YES_NO_OPTION);
	}

	public static int showConfirmDialog(Object message, int buttons) {
		return showConfirmDialog(message, "选择", buttons);
	}

	public static int showConfirmDialog(Object message, String title, int buttons) {
		return showConfirmDialog(message, title, buttons, JOptionPane.QUESTION_MESSAGE);
	}

	public static int showConfirmDialog(Object message, String title, int buttons, int messageType) {
		AtomicInteger button = new AtomicInteger(JOptionPane.CANCEL_OPTION);
		if (SwingUtilities.isEventDispatchThread()) {
			Object[][] options = initMsgButtons(buttons);
			button.set(JOptionPane.showOptionDialog(null, message, title, buttons, messageType, null, options[0],
					options[1][0]));
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						Object[][] options = initMsgButtons(buttons);
						button.set(JOptionPane.showOptionDialog(null, message, title, buttons, messageType, null,
								options[0], options[1][0]));
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return button.get();
	}

	public static String showInputDialog(Object message) {
		return showInputDialog(message, "");
	}

	public static String showInputDialog(Object message, Object initValue) {
		AtomicReference<String> result = new AtomicReference<String>(null);
		if (SwingUtilities.isEventDispatchThread()) {
			result.set(TextInput.showDialog(message.toString(), initValue == null ? null : initValue.toString()));
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {

					@Override
					public void run() {
						result.set(TextInput.showDialog(message.toString(),
								initValue == null ? null : initValue.toString()));
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		String name = result.get();
		if (name == null)
			name = "";
		return name;
	}
}
