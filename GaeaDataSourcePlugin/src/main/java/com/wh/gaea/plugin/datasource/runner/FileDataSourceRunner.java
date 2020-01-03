package com.wh.gaea.plugin.datasource.runner;

import java.util.LinkedHashMap;

import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.datasource.DataSource;
import com.wh.swing.tools.MsgHelper;
import com.wh.tools.HttpHelp;
import com.wh.tools.HttpHelp.ExecuteResult;
import com.wh.tools.HttpHelp.HttpResultState;
import com.wh.tools.HttpHelp.IHttpResult;
import com.wh.tools.HttpHelp.RequestInfo;

public class FileDataSourceRunner {
	public static void execute(DataSource dataSource) throws Exception {

		if (dataSource == null)
			throw new Exception("数据源为空!");

		if (dataSource.params == null || !dataSource.params.has("name"))
			throw new Exception("文件名称为空!");

		dataSource.loadDataset(GlobalInstance.instance().getDataServiceRoot());
	}

	public static void publish(DataSource dataSource) throws Exception{
		if (dataSource == null)
			throw new Exception("请先选择一个数据源！");
		
		String dataRootUrl = GlobalInstance.instance().getDataServiceRoot();
		if (dataRootUrl == null || dataRootUrl.isEmpty()){
			MsgHelper.showMessage("请先设置【数据服务器地址】！");
			return;
		}
		
		String url = dataRootUrl + "/filemodel/service/save.do";
		JSONObject command = new JSONObject();
		JSONObject tmp = new JSONObject();
		tmp.put("data", dataSource.dataset.toString());
		tmp.put("name", dataSource.params.getString("name"));
		command.put("command", tmp.toString());
		LinkedHashMap<String, String> params = HttpHelp.JsonToMap(command);
		HttpHelp.HttpPost(url, params, new IHttpResult() {

			@Override
			public void OnHttpResult(RequestInfo request, ExecuteResult result) {
				if (result.state == HttpResultState.hrsMessage) {
					JSONObject resultdata = result.json;
					if (resultdata.getInt("ret") == 0) {
						MsgHelper.showMessage("成功发布！");
						return;
					}
				}
				MsgHelper.showException("发布失败！");				
			}
		});

	}
}
