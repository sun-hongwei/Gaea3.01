package wh.excel.template;

import wh.excel.model.ExcelModel.RowInfo;

public class ConfigItemTemplate {
	public int row = -1;
	public String name = "item1";
	
	public RowInfo saveRowInfo;
	
	@Override
	public String toString() {
		return name;
	}
}
