package com.wh.gaea.plugin.datasource.runner;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.DefaultTableModel;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.datasource.DataSource;
import com.wh.gaea.interfaces.IMainControl;
import com.wh.gaea.selector.KeyValueSelector;
import com.wh.gaea.selector.KeyValueSelector.ModelResult;
import com.wh.swing.tools.MsgHelper;
import com.wh.tools.HttpHelp;
import com.wh.tools.HttpHelp.ExecuteResult;
import com.wh.tools.HttpHelp.HttpResultState;
import com.wh.tools.HttpHelp.IHttpResult;
import com.wh.tools.HttpHelp.RequestInfo;

public class SqlDataSourceRunner {

	public static class Parser {

		public static Map<String, String> parse(String paramSql) {
			Map<String, String> result = new HashMap<>();
			String pattern = "(([#\\$])\\{([a-zA-Z0-9\\-]+)\\})";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher((String) paramSql);
			while (m.find()) {
				String configId = m.group(1);
				// String prex = m.group(2);
				String key = m.group(3);

				result.put(configId, key);
			}

			return result;
		}

	}

	public static void execute(DataSource dataSource, IMainControl mainControl) throws Exception {

		if (dataSource == null)
			throw new Exception("数据源为空!");

		if (dataSource.params == null || !dataSource.params.has("sql"))
			throw new Exception("sql参数为空!");

		String url = dataSource.url;
		String oldUrl = url;
		try {
			if (!url.toLowerCase().startsWith("http://"))
				url = GlobalInstance.instance().getDataServiceRoot() + url;

			dataSource.url = url;

			Map<String, String> params = Parser.parse(dataSource.params.getString("sql"));

			JSONObject queryParams = new JSONObject();
			Object[][] rows = new Object[dataSource.params.has("order") ? params.size() + 2 : params.size()][2];
			int index = 0;
			if (dataSource.params.has("order")) {
				queryParams.put("order", dataSource.params.getString("order"));
				rows[index++][0] = "start";
				if (dataSource.params.has("start"))
					rows[index - 1][1] = dataSource.params.get("start");

				rows[index++][0] = "size";
				if (dataSource.params.has("size"))
					rows[index - 1][1] = dataSource.params.get("size");
			}
			if (params.size() > 0) {
				for (String key : params.values()) {
					rows[index][0] = key;
					rows[index][1] = "";
					index++;
				}

			}

			if (rows.length > 0) {
				ModelResult result = KeyValueSelector.show(null, mainControl, null, null, rows,
						new Object[] { "查询参数", "值" }, null, new int[] { 0 }, false);

				DefaultTableModel model = null;
				if (result.isok)
					model = result.model;

				if (model == null)
					return;

				for (int i = 0; i < model.getRowCount(); i++) {
					queryParams.put((String) model.getValueAt(i, 0), model.getValueAt(i, 1));
				}
			}
			dataSource.params.put("param", queryParams);
			dataSource.loadDataset("");
		} finally {
			dataSource.url = oldUrl;
			if (dataSource.params.has("param"))
				dataSource.params.remove("param");
		}
	}

	public interface IQueryFieldsCallback{
		void onCallback(JSONArray result);
	}
	
	public static void queryFields(String tablename, IQueryFieldsCallback onQueryFieldsCallback) throws Exception {

		if (tablename == null || tablename.isEmpty())
			throw new Exception("必须设置表名!");

		String url = GlobalInstance.instance().getDataServiceRoot() + "/jsonarray/service/query/fields.do";

		JSONObject command = new JSONObject();
		JSONObject queryParams = new JSONObject();
		queryParams.put("tablename", tablename);
		command.put("command", queryParams.toString());
		LinkedHashMap<String, String> params = HttpHelp.JsonToMap(command);
		HttpHelp.HttpPost(url, params, new IHttpResult() {

			@Override
			public void OnHttpResult(RequestInfo request, ExecuteResult httpResult) {
				if (httpResult.state == HttpResultState.hrsMessage) {
					JSONObject resultdata = httpResult.json;
					if (resultdata.getInt("ret") == 0) {
						try {
							onQueryFieldsCallback.onCallback(resultdata.getJSONArray("data"));
							return;
						} catch (Exception e) {
							MsgHelper.showException(e, "异常", resultdata.toString());
						}
					}
					MsgHelper.showException("服务端未正确应答！");
				}
			}
		});
		
	}

}
