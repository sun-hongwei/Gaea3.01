package com.wh.gaea.editor;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.UUID;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.draw.DrawInfo;
import com.wh.gaea.draw.IUINode;
import com.wh.gaea.interfaces.IDrawCanvas;
import com.wh.gaea.interfaces.IDrawInfoDefines;

public class CreateControlDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField idField;
	private JComboBox<String> comboBox;

	/**
	 * Create the dialog.
	 */
	
	boolean isok = false;
	private JTextField valueField;
	
	public void close(){
		isok = false;
		setVisible(false);
	}
	
	public CreateControlDialog() {
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				close();
			}
		});
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setIconImage(Toolkit.getDefaultToolkit().getImage(CreateControlDialog.class.getResource("/image/browser.png")));
		setFont(new Font("微软雅黑", Font.PLAIN, 12));
		setTitle("新建控件");
		setBounds(100, 100, 450, 307);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JLabel label = new JLabel("控件类型");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label.setBounds(90, 48, 54, 15);
		contentPanel.add(label);
		
		comboBox = new JComboBox<String>();
		comboBox.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		comboBox.setModel(new DefaultComboBoxModel<>(IDrawInfoDefines.TypeNames));
		comboBox.setSelectedIndex(0);
		comboBox.setBounds(156, 45, 183, 21);
		contentPanel.add(comboBox);
		
		JLabel lblid = new JLabel("控件id");
		lblid.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		lblid.setBounds(90, 91, 54, 15);
		contentPanel.add(lblid);
		
		idField = new JTextField();
		idField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		idField.setBounds(156, 88, 183, 21);
		contentPanel.add(idField);
		idField.setColumns(10);
		
		JLabel label_1 = new JLabel("值");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_1.setBounds(90, 140, 54, 15);
		contentPanel.add(label_1);
		
		valueField = new JTextField();
		valueField.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		valueField.setBounds(156, 137, 183, 21);
		contentPanel.add(valueField);
		valueField.setColumns(10);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("确定");
				okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						isok = true;
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
						close();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		setLocationRelativeTo(null);
	}
	
	public static IUINode showDialog(IDrawCanvas canvas, String controlTypeName, boolean fix){
		CreateControlDialog dialog = new CreateControlDialog();
		if (controlTypeName != null && !controlTypeName.isEmpty()){
			DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)dialog.comboBox.getModel(); 
			if (fix && model.getIndexOf(controlTypeName) != -1){
				dialog.comboBox.setModel(new DefaultComboBoxModel<>(new String[]{controlTypeName}));
				dialog.comboBox.setSelectedIndex(0);
			}else
				dialog.comboBox.setSelectedItem(controlTypeName);
		}
		dialog.setModal(true);
		dialog.setVisible(true);
		if (dialog.isok){
			String typename = dialog.comboBox.getSelectedItem().toString();
			String id = dialog.idField.getText();
			if (id == null || id.isEmpty())
				id = UUID.randomUUID().toString();
			String value = dialog.valueField.getText();
			dialog.dispose();
			
			IUINode node = GlobalInstance.instance().createUINode(canvas);
			node.setTitle("新" + typename);
			DrawInfo info = GlobalInstance.instance().createDrawInfo(typename, node);
			node.setDrawInfo(info);
			node.setId(id);
			node.setName(id);
			node.getDrawInfo().value = value;
			return node;
		}
		return null;
	}
}
