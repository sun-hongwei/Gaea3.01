package com.wh.gaea.interfaces.selector;

import java.util.Map;

import com.wh.gaea.datasource.DataSource;
import com.wh.gaea.interfaces.ShowType;

public interface IDataSourceSelector {
	DataSource dataSourceSelector(ShowType st);
	void showLocalView();
	void showSQLView();
	void showFileView();
	void showUrlView();
	Map<String, String> parseSQL(String sql);
}
