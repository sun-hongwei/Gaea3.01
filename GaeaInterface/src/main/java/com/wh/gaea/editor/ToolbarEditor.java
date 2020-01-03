package com.wh.gaea.editor;

import java.awt.BorderLayout;
import java.awt.Component;
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

import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.draw.IUINode;
import com.wh.gaea.interfaces.Config;
import com.wh.gaea.interfaces.ICreateNodeSerializable;
import com.wh.gaea.interfaces.IDataSerializable;
import com.wh.gaea.interfaces.IDrawCanvas;
import com.wh.gaea.interfaces.IDrawInfoDefines;
import com.wh.gaea.interfaces.IDrawNode;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.swing.tools.MsgHelper;
import com.wh.tools.JsonHelp;
public class ToolbarEditor extends JDialog{
	private boolean isEdit = false;
	private static final long serialVersionUID = 4251405285974410412L;
	public IDrawCanvas canvas = GlobalInstance.instance().createUICanvas();

	void oncancel(){
		if (isEdit){
			if (MsgHelper.showConfirmDialog("关闭将导致所有得修改丢失，是否继续？", "关闭", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
				return;
		}
		
		dispose();
	}
	
	public ToolbarEditor(IMainControl mainControl) {
		setTitle("工具栏编辑");
		addWindowListener(new WindowAdapter(){
			
			@Override
			public void windowClosing(WindowEvent e) {
				oncancel();
			}
		});
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setModal(true);
		setIconImage(Toolkit.getDefaultToolkit().getImage(ToolbarEditor.class.getResource("/image/browser.png")));
		setBounds(100, 100, 1006, 712);
		getContentPane().setLayout(new BorderLayout());
		
		canvas.getPageConfig().setConfig(new Config[]{});
		IUINode node = GlobalInstance.instance().createUINode(canvas);
		GlobalInstance.instance().createDrawInfo(IDrawInfoDefines.Toolbar_Name, node);
		canvas.addNode(node, false, false);
		getContentPane().add((Component) canvas, BorderLayout.CENTER);
		
		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new BorderLayout(0, 0));
		JPanel panel = new JPanel();
		buttonPane.add(panel, BorderLayout.CENTER);
		
		JLabel label = new JLabel("      ");
		panel.add(label);
		
		JButton button_3 = new JButton("保存");
		button_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				save();
				MsgHelper.showMessage(null, "保存成功！");
			}
		});
		panel.add(button_3);
		
		JButton button_4 = new JButton("装载");
		button_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (isEdit){
					if (MsgHelper.showConfirmDialog("装载将导致所有得修改丢失，是否继续？", "关闭", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
						return;
				}
				load();
			}
		});
		panel.add(button_4);
		
		JButton button_1 = new JButton("编辑");
		getContentPane().add(button_1, BorderLayout.EAST);
		button_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
				
		setLocationRelativeTo(null);
	}

	File file;
	
	public void save() {
		if (!isEdit)
			return;
		
		try {
			IUINode node = (IUINode) canvas.getNodes().get(0);
			JsonHelp.saveJson(file, node.toJson(), null);
			isEdit = false;
			GlobalInstance.instance().lockFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showMessage(null, "保存失败！", "保存", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void load() {
		try {
			if (!file.exists())
				return;
			
			IUINode node = (IUINode) canvas.getNodes().get(0);
			JSONObject value = (JSONObject) JsonHelp.parseCacheJson(file, null);
			node.fromJson(value, new ICreateNodeSerializable() {
				
				@Override
				public IDrawNode newDrawNode(JSONObject json) {
					return null;
				}
				
				@Override
				public IDataSerializable getUserDataSerializable(IDrawNode node) {
					return null;
				}
			});
			isEdit = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void showDialog(IMainControl mainControl, File file){
		if (file.exists())
			if (!GlobalInstance.instance().lockFile(file)){
				MsgHelper.showMessage("文件【" + file.getAbsolutePath() + "】已经被其他用户锁定，请稍后再试！");
				return;
			}
		
		ToolbarEditor editor = new ToolbarEditor(mainControl);
		editor.file = file;
		editor.load();
		editor.setModal(true);
		editor.setVisible(true);
		
		if (file != null)
			GlobalInstance.instance().unlockFile(file);

	}
}
