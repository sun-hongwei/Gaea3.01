package com.wh.swing.tools.dialog.input;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.wh.swing.tools.MsgHelper;

public class LoginInput extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();

	boolean isok = false;
	String user;
	String pwd;
	
	private JTextField userView;
	private JLabel label1;
	private JLabel label2;
	private JPasswordField pwdView;
	/**
	 * Create the dialog.
	 */
	public LoginInput() {
		setTitle("登录");
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		setIconImage(Toolkit.getDefaultToolkit().getImage(LoginInput.class.getResource("/image/browser.png")));
		setBounds(100, 100, 711, 314);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		label1 = new JLabel("用户名称");
		label1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label1.setBounds(113, 58, 81, 21);
		contentPanel.add(label1);
		
		userView = new JTextField();
		userView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		userView.setBounds(209, 55, 358, 27);
		contentPanel.add(userView);
		userView.setColumns(10);
		
		label2 = new JLabel("密码");
		label2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label2.setBounds(113, 134, 81, 21);
		contentPanel.add(label2);
		
		pwdView = new JPasswordField();
		pwdView.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		pwdView.setBounds(209, 134, 358, 27);
		contentPanel.add(pwdView);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("确定");
				okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String text = userView.getText();
						if (text == null || text.isEmpty()){
							MsgHelper.showMessage("请输入用户名称重试！");
							return;
						}
						isok = true;
						setVisible(false);
					}
				});
				okButton.setActionCommand("");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("取消");
				cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		setLocationRelativeTo(null);
	}

	public static class Result{
		public String user;
		public String pwd;
	}
	public static Result showDialog(String user){
		LoginInput msg = new LoginInput();
		if (user != null)
			msg.userView.setText(user);
		msg.setModal(true);
		msg.setVisible(true);
		
		Result result = new Result();
		if (msg.isok){
			result.user = msg.userView.getText().trim();
			result.pwd = new String(msg.pwdView.getPassword());
		}else
			result = null;
		msg.dispose();
		
		return result;
	}
}
