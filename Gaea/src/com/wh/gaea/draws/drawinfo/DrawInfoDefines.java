package com.wh.gaea.draws.drawinfo;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.wh.gaea.control.ControlTreeManager.TreeInfo;
import com.wh.gaea.interfaces.IDrawInfoDefines;

public abstract class DrawInfoDefines implements IDrawInfoDefines {
	public static HashMap<String, JSONObject> getControlSimpleClassNameForJSONObject(){
		HashMap<String, JSONObject> controls = new HashMap<>();

		try {
			controls.put(SpinnerInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + SpinnerInfo.class.getName() + "\"}"));
			controls.put(LabelInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + LabelInfo.class.getName() + "\"}"));
			controls.put(TextInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + TextInfo.class.getName() + "\"}"));
			controls.put(ImageInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + ImageInfo.class.getName() + "\"}"));
			controls.put(ComboInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + ComboInfo.class.getName() + "\"}"));
			controls.put(ComboTreeInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + ComboTreeInfo.class.getName() + "\"}"));
			controls.put(RadioInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + RadioInfo.class.getName() + "\"}"));
			controls.put(CheckInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + CheckInfo.class.getName() + "\"}"));
			controls.put(DateInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + DateInfo.class.getName() + "\"}"));
			controls.put(TimeInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + TimeInfo.class.getName() + "\"}"));
			controls.put(TreeInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + TreeInfo.class.getName() + "\"}"));
			controls.put(GridInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + GridInfo.class.getName() + "\"}"));
			controls.put(ButtonInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + ButtonInfo.class.getName() + "\"}"));
			controls.put(PasswordInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + PasswordInfo.class.getName() + "\"}"));
			controls.put(TextAreaInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + TextAreaInfo.class.getName() + "\"}"));
			controls.put(ListBoxInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + ListBoxInfo.class.getName() + "\"}"));
			controls.put(ScrollBarInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + ScrollBarInfo.class.getName() + "\"}"));
			controls.put(ReportInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + ReportInfo.class.getName() + "\"}"));
			controls.put(ChartInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + ChartInfo.class.getName() + "\"}"));
			controls.put(MainMenuInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + MainMenuInfo.class.getName() + "\"}"));
			controls.put(ListViewInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + ListViewInfo.class.getName() + "\"}"));
			controls.put(ProgressBarInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + ProgressBarInfo.class.getName() + "\"}"));
			controls.put(UpLoadInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + UpLoadInfo.class.getName() + "\"}"));
			controls.put(DivInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + DivInfo.class.getName() + "\"}"));
			controls.put(SubUIInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + SubUIInfo.class.getName() + "\"}"));
			controls.put(ToolbarInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + ToolbarInfo.class.getName() + "\"}"));
			controls.put(TimerInfo.class.getSimpleName(), new JSONObject("{" + IDrawInfoDefines.Full_TypeName_Key + ":\"" + TimerInfo.class.getName() + "\"}"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return controls;
	}
	
	public static<T> HashMap<String, T> getControlSimpleClassName(Class<T> c){
		HashMap<String, T> controls = new HashMap<>();

		
		try {
			controls.put(SpinnerInfo.class.getSimpleName(), c.newInstance());
			controls.put(LabelInfo.class.getSimpleName(), c.newInstance());
			controls.put(TextInfo.class.getSimpleName(), c.newInstance());
			controls.put(ImageInfo.class.getSimpleName(), c.newInstance());
			controls.put(ComboInfo.class.getSimpleName(), c.newInstance());
			controls.put(ComboTreeInfo.class.getSimpleName(), c.newInstance());
			controls.put(RadioInfo.class.getSimpleName(), c.newInstance());
			controls.put(CheckInfo.class.getSimpleName(), c.newInstance());
			controls.put(DateInfo.class.getSimpleName(), c.newInstance());
			controls.put(TimeInfo.class.getSimpleName(), c.newInstance());
			controls.put(TreeInfo.class.getSimpleName(), c.newInstance());
			controls.put(GridInfo.class.getSimpleName(), c.newInstance());
			controls.put(ButtonInfo.class.getSimpleName(), c.newInstance());
			controls.put(PasswordInfo.class.getSimpleName(), c.newInstance());
			controls.put(TextAreaInfo.class.getSimpleName(), c.newInstance());
			controls.put(ListBoxInfo.class.getSimpleName(), c.newInstance());
			controls.put(ScrollBarInfo.class.getSimpleName(), c.newInstance());
			controls.put(ReportInfo.class.getSimpleName(), c.newInstance());
			controls.put(ChartInfo.class.getSimpleName(), c.newInstance());
			controls.put(MainMenuInfo.class.getSimpleName(), c.newInstance());
			controls.put(ListViewInfo.class.getSimpleName(), c.newInstance());
			controls.put(ProgressBarInfo.class.getSimpleName(), c.newInstance());
			controls.put(UpLoadInfo.class.getSimpleName(), c.newInstance());
			controls.put(DivInfo.class.getSimpleName(), c.newInstance());
			controls.put(SubUIInfo.class.getSimpleName(), c.newInstance());
			controls.put(ToolbarInfo.class.getSimpleName(), c.newInstance());
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return controls;
	}
	
	public static String getControlChineseName(String controlName){
		String name = controlName;
		if (SpinnerInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.Spinner_Name;
		else if (LabelInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.Label_Name;
		else if (TextInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.TextBox_Name;
		else if (ImageInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.Image_Name;
		else if (ComboInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.ComboBox_Name;
		else if (ComboTreeInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.ComboTreeBox_Name;
		else if (RadioInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.RadioBox_Name;
		else if (CheckInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.CheckBox_Name;
		else if (DateInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.DateBox_Name;
		else if (TimeInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.TimeBox_Name;
		else if (TreeInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.Tree_Name;
		else if (GridInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.Grid_Name;
		else if (ButtonInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.Button_Name;
		else if (PasswordInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.Password_Name;
		else if (TextAreaInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.TextArea_Name;
		else if (ListBoxInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.ListBox_Name;
		else if (ScrollBarInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.ScrollBar_Name;
		else if (ReportInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.Report_Name;
		else if (ChartInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.Chart_Name;
		else if (MainMenuInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.MainMenu_Name;
		else if (ListViewInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.ListView_Name;
		else if (ProgressBarInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.ProgressBar_Name;
		else if (UpLoadInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.UpLoad_Name;
		else if (DivInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.Div_Name;
		else if (SubUIInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.SubUI_Name;
		else if (ToolbarInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.Toolbar_Name;		
		else if (TimerInfo.class.getSimpleName().compareToIgnoreCase(controlName) == 0)
			name = IDrawInfoDefines.Timer_Name;
		return name;
	}
	
}
