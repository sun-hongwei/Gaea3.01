package com.wh.gaea.plugin.datasource.runner;

import java.util.LinkedHashMap;

import org.json.JSONObject;

import com.wh.encrypt.Encryption.MD5Util;
import com.wh.gaea.datasource.DataSource;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.swing.tools.MsgHelper;
import com.wh.tools.HttpHelp;
import com.wh.tools.HttpHelp.ExecuteResult;
import com.wh.tools.HttpHelp.HttpResultState;
import com.wh.tools.HttpHelp.IHttpResult;
import com.wh.tools.HttpHelp.RequestInfo;

public class UrlDataSourceRunner {

	public static void execute(DataSource dataSource, IMainControl mainControl, String baseUrl) throws Exception {

		if (dataSource == null)
			throw new Exception("数据源为空!");

		if (dataSource.url == null || dataSource.url.isEmpty())
			throw new Exception("url参数为空!");

		if (dataSource.url.toLowerCase().startsWith("http://"))
			baseUrl = "";

		dataSource.loadDataset(baseUrl);
	}

	public static void login(String url, String user, String pwd) {
		JSONObject command = new JSONObject();

		command.put("userid", user);
		command.put("pwd", MD5Util.MD5(pwd));
		command.put("dynamic_code", "");
		command.put("client_token", "");
		command.put("host", "");
		command.put("_responseCrypted", false);
		LinkedHashMap<String, String> requestData = HttpHelp.JsonToMap(command);
		HttpHelp.HttpPost(url, requestData, new IHttpResult() {

			@Override
			public void OnHttpResult(RequestInfo request, ExecuteResult result) {
				if (result.state == HttpResultState.hrsMessage) {
					JSONObject resultdata = result.json;
					if (resultdata.getInt("ret") == 0 && resultdata.getJSONObject("data").has("sessionid")) {
						DataSource.SESSION = resultdata.getJSONObject("data").getString("sessionid");
						MsgHelper.showMessage("登录成功,session【" + DataSource.SESSION + "】！");
					}else {
						MsgHelper.showException("输入的登录名\\密码无效！");
					}
				} else {
					if (result.exception != null)
						MsgHelper.showException(result.exception);
					else if (result.errMsg == null || result.errMsg.isEmpty()) {
						MsgHelper.showException("errorcode : " + result.httpstate);
					} else {
						MsgHelper.showException(result.errMsg);
					}
				}
			}
		});
	}

}
