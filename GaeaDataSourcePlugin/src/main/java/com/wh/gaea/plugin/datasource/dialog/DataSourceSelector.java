package com.wh.gaea.plugin.datasource.dialog;

import com.wh.gaea.datasource.DataSource;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.gaea.interfaces.ShowType;
import com.wh.gaea.selector.RadioSelector;
import com.wh.gaea.selector.RadioSelector.Result;

public class DataSourceSelector {
	public static DataSource show(ShowType st, IMainControl mainControl){
		Result result = Result.rtNone;
		switch (st) {
		case stLocal:
			return LocalDataSourceConfig.show(mainControl);
		case stFile:
			return FileDataSourceConfig.show(mainControl);
		case stSql:
			return SqlDataSourceConfig.show(mainControl);
		case stRemote:
			result = RadioSelector.showDialog(new String[]{"Url数据源", "SQL数据源", "文件数据源"});
			break;
		case stAll:
			result = RadioSelector.showDialog(new String[]{"Url数据源", "SQL数据源", "文件数据源", "本地数据源"});
			break;
		}
		switch (result) {
		case rt1:
			return UrlDataSourceConfig.show(mainControl);
		case rt2:
			return SqlDataSourceConfig.show(mainControl);
		case rt3:
			return FileDataSourceConfig.show(mainControl);
		case rt4:
			return LocalDataSourceConfig.show(mainControl);
		default:
			return null;
		}
	}
}
