package com.wh.gaea.plugin.db;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.swing.tools.MsgHelper;
import com.wh.tools.JsonHelp;

import wh.interfaces.IConnectionFactory;
import wh.interfaces.IDBConnection;
import wh.interfaces.IDBConnection.DBConnectionInfo;

public class DBConnectionStringCreator extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField server;
	private JTextField dbname;
	private JTextField user;
	private JPasswordField pwd;
	private JTextField port;

	public JSONObject result;
	
	protected void docancel() {
		if (MsgHelper.showConfirmDialog("是否放弃所有修改？", "退出", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
			return;

		setVisible(false);
	}

	/**
	 * Create the dialog.
	 */
	public DBConnectionStringCreator() {
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setModalityType(ModalityType.APPLICATION_MODAL);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				docancel();
			}
		});
		setTitle("配置数据库连接");
		setIconImage(
				Toolkit.getDefaultToolkit().getImage(DBConnectionStringCreator.class.getResource("/image/browser.png")));
		setModal(true);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		JLabel label = new JLabel("服务器");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label.setBounds(63, 22, 41, 15);
		contentPanel.add(label);

		server = new JTextField();
		server.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		server.setText("localhost");
		server.setBounds(132, 12, 212, 27);
		contentPanel.add(server);
		server.setColumns(10);

		JLabel label_1 = new JLabel("端口");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_1.setBounds(63, 59, 54, 15);
		contentPanel.add(label_1);

		JLabel lblNewLabel = new JLabel("数据库名称");
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		lblNewLabel.setBounds(63, 96, 66, 15);
		contentPanel.add(lblNewLabel);

		dbname = new JTextField();
		dbname.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		dbname.setText("DefaultDBModel");
		dbname.setBounds(132, 90, 212, 27);
		contentPanel.add(dbname);
		dbname.setColumns(10);

		JLabel label_2 = new JLabel("登录用户");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_2.setBounds(63, 133, 54, 15);
		contentPanel.add(label_2);

		user = new JTextField();
		user.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		user.setText("sa");
		user.setBounds(132, 129, 135, 27);
		contentPanel.add(user);
		user.setColumns(10);

		JLabel label_3 = new JLabel("密码");
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_3.setBounds(63, 170, 54, 15);
		contentPanel.add(label_3);

		pwd = new JPasswordField();
		pwd.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		pwd.setToolTipText("");
		pwd.setBounds(132, 168, 135, 27);
		contentPanel.add(pwd);

		port = new JTextField();
		port.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		port.setBounds(132, 51, 105, 27);
		contentPanel.add(port);
		port.setColumns(10);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);

			JButton button = new JButton("测试连接");
			button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					testConnection();
				}
			});
			buttonPane.add(button);

			JLabel lblNewLabel_1 = new JLabel("         ");
			buttonPane.add(lblNewLabel_1);
			{
				JButton okButton = new JButton("确定");
				okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if (!checkValues())
							return;
						try {
							save();
							isok = true;
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("取消");
				cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						docancel();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}

		setLocationRelativeTo(null);
	}

	protected String getValue(JTextField textField) {
		String text = textField.getText();
		if (text == null || text.isEmpty()) {
			return null;
		} else
			return text.trim();
	}

	protected boolean checkValues() {
		if (getValue(server) == null) {
			MsgHelper.showMessage(null, "服务器不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		try {
			if (getValue(port) != null)
				Integer.parseInt(port.getText());
		} catch (Exception e) {
			MsgHelper.showMessage(null, "端口号格式不正确！", "提示", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		if (getValue(dbname) == null) {
			MsgHelper.showMessage(null, "数据库名称不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		if (getValue(user) == null) {
			MsgHelper.showMessage(null, "用户不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		return true;
	}

	protected void testConnection() {
		if (!checkValues())
			return;

		DBConnectionInfo connectionInfo = IDBConnection.getMSSQLServerConnectionString(getValue(server), port.getText(),
				getValue(dbname), getValue(user), getValue(pwd));
		IDBConnection db;
		try {
			db = IConnectionFactory.getConnection(connectionInfo);
			db.close();
			MsgHelper.showMessage(null, "恭喜，连接测试成功！", "测试连接", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showMessage(null, "连接测试失败，请检您的输入！", "测试连接", JOptionPane.ERROR_MESSAGE);
		}
	}

	boolean isok = false;
	@SuppressWarnings("deprecation")
	protected void save() throws Exception {
		result = new JSONObject();
		String text = server.getText();
		if (text != null && !text.isEmpty()) {
			result.put("server", text);
		}

		text = port.getText();
		if (text != null && !text.isEmpty()) {
			result.put("port", text);
		}

		text = dbname.getText();
		if (text != null && !text.isEmpty()) {
			result.put("dbname", text);
		}

		text = user.getText();
		if (text != null && !text.isEmpty()) {
			result.put("user", text);
		}

		text = pwd.getText();
		if (text != null && !text.isEmpty()) {
			result.put("pwd", text);
		}

		if (file != null) {
			JsonHelp.saveJson(file, result, null);
			GlobalInstance.instance().lockFile(file);
		}
	}

	File file;

	public static IDBConnection getDBConnection(File file) {
		if (!file.exists())
			return null;

		try {
			JSONObject jsonObject = (JSONObject) JsonHelp.parseCacheJson(file, null);
			if (jsonObject.isEmpty())
				return null;
			
			return getDBConnection(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static IDBConnection getDBConnection(JSONObject jsonObject) throws Exception {
		String host = null, port = null, dbname = null, user = null, pwd = null;
		if (jsonObject.has("server"))
			host = jsonObject.getString("server");
		if (jsonObject.has("port"))
			port = JsonHelp.getString(jsonObject, "port");
		if (jsonObject.has("dbname"))
			dbname = jsonObject.getString("dbname");

		if (jsonObject.has("user"))
			user = jsonObject.getString("user");

		if (jsonObject.has("pwd"))
			pwd = jsonObject.getString("pwd");

		IDBConnection db = IConnectionFactory
				.getConnection(IDBConnection.getMSSQLServerConnectionString(host, port, dbname, user, pwd));

		return db;
	}

	protected void load(File file) {
		this.file = file;
		if (!file.exists())
			return;

		try {
			JSONObject jsonObject = (JSONObject) JsonHelp.parseCacheJson(file, null);
			load(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void load(JSONObject jsonObject) {
		if (jsonObject.has("server"))
			server.setText(jsonObject.getString("server"));
		if (jsonObject.has("port"))
			port.setText(jsonObject.getString("port"));
		if (jsonObject.has("dbname"))
			dbname.setText(jsonObject.getString("dbname"));
		if (jsonObject.has("user"))
			user.setText(jsonObject.getString("user"));
		if (jsonObject.has("pwd"))
			pwd.setText(jsonObject.getString("pwd"));
	}

	public static boolean showDialog(File file) {
		if (file.exists())
			if (!GlobalInstance.instance().lockFile(file)) {
				MsgHelper.showMessage("文件【" + file.getAbsolutePath() + "】已经被其他用户锁定，请稍后再试！");
				return false;
			}

		DBConnectionStringCreator dialog = new DBConnectionStringCreator();
		dialog.load(file);
		dialog.setVisible(true);

		if (file != null)
			GlobalInstance.instance().unlockFile(file);
		
		return dialog.isok;
	}

	public static JSONObject showDialog(JSONObject connectInfo) {
		DBConnectionStringCreator dialog = new DBConnectionStringCreator();
		dialog.load(connectInfo);
		dialog.setVisible(true);
		JSONObject result = dialog.result;
		dialog.dispose();
		return result;
	}
}
