package com.wh.gaea.datasource;

public class FileDataSource extends DataSource{

	private static final long serialVersionUID = 1L;
	public static final String FILE_KEY = "file";

	@Override
	public String getType() {
		return FILE_KEY;
	}

}
