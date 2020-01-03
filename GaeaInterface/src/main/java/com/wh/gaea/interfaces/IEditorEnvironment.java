package com.wh.gaea.interfaces;

public interface IEditorEnvironment {
	public static final String FrameModelName = "frame_model_canvas_name";
	public static final String Excel_Export_Map_Template_Name = "export_to_excel_map_template.xlsx";
	public static final String Excel_Export_Data_Template_Name = "export_to_excel_data_template.xlsx";
	public static final String Excel_Import_Name = "import_from_excel.xlsx";

	public static final String META_MAINTREE_KEY = "maintree";
	public static final String META_MAINTREE_UIID_KEY = "uiid";
	public static final String META_MAINTREE_CONTROLID_KEY = "cid";
	public static final String META_MAINTREE_CONTROLNAME_KEY = "cname";

	public static final String Project_File_Extension = "whx";
	public static final String Menu_File_Extension = "menu";
	public static final String Tree_File_Extension = "tree";
	public static final String Requirement_File_Extension = "rml";
	public static final String MasterData_Data_File_Extension = "mdd";
	public static final String MasterData_Type_File_Extension = "mdt";
	public static final String Report_File_Extension = "rpt";
	public static final String Toolbar_File_Extension = "wht";
	public static final String UI_File_Extension = "whu";
	public static final String Relation_File_Extension = "whr";
	public static final String Node_File_Extension = "whn";
	public static final String App_File_Extension = "wha";
	public static final String Flow_File_Extension = "whl";
	public static final String RunFlow_File_Extension = "rwf";
	public static final String Excel_Import_DB_File_Extension = "xcl";
	public static final String DB_Export_Excel_File_Extension = "ecl";
	public static final String UI_Publish_File_Extension = "js";
	public static final String AppWorkflow_File_Extension = "app";

	public static final String Remote_Dir_Name = "remote";
	public static final String DataSource_Dir_Name = "datasource";
	public static final String Codes_Dir_Name = "codes";
	public static final String Publish_Config_Dir_Name = "config";
	public static final String Publish_UI_Dir_Name = "business";
	public static final String AppWorkflow_Dir_Name = "appw";
	public static final String UI_Dir_Name = "ui";
	public static final String App_Dir_Name = "app";
	public static final String Workflow_Dir_Name = "workflow";
	public static final String Project_Root_Dir_Name = "project";
	public static final String Workflow_Dispatch_Dir_Name = "dispatch";
	public static final String Flow_Dir_Name = "flow";
	public static final String RunFlow_Dir_Name = "run";
	public static final String Toolbar_Dir_Name = "toolbar";
	public static final String Image_Resource_Path = "image";
	public static final String Template_Path = "template";
	public static final String Temp_Path = "temp";
	public static final String Cache_Path = "cache";
	public static final String CSS_Dir_Name = "css";
	public static final String Export_Dir_Name = "exports";
	public static final String Control_Dir_Name = "control";
	public static final String Requirement_Dir_Name = "requirement";
	public static final String MasterData_Dir_Name = "masterdata";

	public static final String Image_Icons_Path = "icons";
	public static final String Frame_Dir_Path = "frame";
	public static final String Menu_Dir_Path = "menu";
	public static final String Report_Dir_Path = "report";
	public static final String Download_Dir_Path = "download";
	public static final String Tree_Dir_Path = "tree";
	public static final String User_JavaScript_Dir_Path = "userjs";
	public static final String Service_Dir_Path = "Services";
	public static final String Client_Dir_Path = "client";
	public static final String User_PHP_Dir_Path = "tasks";
	public static final String User_Command_Dir_Path = "commands";
	public static final String Config_Dir_Path = "configure";
	public static final String Lock_Dir_Path = "lock";

	public static final String Remote_Auth_FileName = "auth.js";

	public static final String Config_SubWorkflow_Key_Name = "subworkflow";
	public static final String Config_SubFlow_Key_Name = "subflow";
	public static final String Config_Flow_Key_Name = "flow";
	public static final String Config_App_Key_Name = "app";
	public static final String Config_UI_Key_Name = "ui";
	public static final String Config_Toolbar_Key_Name = "toolbar";
	public static final String Project_ConfigFileName = "main." + Project_File_Extension;
	public static final String Main_Workflow_Relation_FileName = "main";
	public static final String System_FileName = "system-0000-000-UIOP";
	public static final String Main_Menu_FileName = "main";
	public static final String Main_Tree_FileName = "main";
	public static final String Main_Meta_FileName = "main.meta";
	public static final String Main_DB_Config_FileName = "main.config";
	public static final String Publish_UI_Names_FileName = "main.dat";
}
