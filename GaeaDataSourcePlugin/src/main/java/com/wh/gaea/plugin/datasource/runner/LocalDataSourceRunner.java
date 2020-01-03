package com.wh.gaea.plugin.datasource.runner;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.tools.JsonHelp;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;

public class LocalDataSourceRunner {
	public static class RowInfo {
		public boolean curRow = true;
		public int row;
		public String field;
		public String datasetId;
		public String replaceKey;

		public String getKey() {
			return datasetId + "." + field + "." + (curRow ? "cur" : row);
		}
	}

	public static class Dataset {
		public JSONArray columns = new JSONArray();
		public JSONArray rows = new JSONArray();
	}

	public static class SqlRunConfig {
		public String sql;
		public HashMap<String, RowInfo> params = new HashMap<>();
	}

	public static class Parser {

		protected static RowInfo parseRowNumber(String line) {
			Pattern vr = Pattern.compile("([a-zA-Z0-9]+)\\[([\\-0-9]+)\\]\\[([a-zA-Z0-9]+)\\]");
			Matcher vm = vr.matcher(line);
			RowInfo info = new RowInfo();

			if (vm.find()) {
				info.field = vm.group(3);
				try {
					info.row = Integer.parseInt(vm.group(2));
				} catch (Exception e) {
				}
				if (info.row == -1)
					info.curRow = true;
				info.datasetId = vm.group(1);
			}
			return info;
		}

		public static SqlRunConfig parse(String paramSql) {
			SqlRunConfig config = new SqlRunConfig();

			boolean findConfig = false;
			String pattern = "(([#\\$@])\\{([a-zA-Z0-9\\[\\-\\]\\[\\]]+)\\})";
			Pattern r = Pattern.compile(pattern);
			Matcher m = r.matcher((String) paramSql);
			while (m.find()) {
				findConfig = true;
				String configId = m.group(1);
				// String prex = m.group(2);
				String key = m.group(3);

				RowInfo rowInfo = parseRowNumber(key);
				rowInfo.replaceKey = configId;

				config.params.put(configId, rowInfo);
			}
			if (!findConfig)
				return null;
			
			config.sql = paramSql;

			return config;
		}

	}

	public static IDataset query(SqlRunConfig config, JSONArray masterDataset, int curRow, IDBConnection db)
			throws Exception {
		if (config == null)
			return null;

		String runSql = config.sql;
		for (RowInfo rowInfo : config.params.values()) {
			JSONObject row = masterDataset.getJSONObject(rowInfo.curRow ? curRow : rowInfo.row);

			String replaceStr = "";
			if (row.has(rowInfo.field)) {
				replaceStr = JsonHelp.getString(row, rowInfo.field);
			}

			runSql = runSql.replace(rowInfo.replaceKey, replaceStr);
		}

		return db.query(runSql, null);
	}

	public static IDataset execute(String paramSql, JSONArray masterDataset, int curRow, IDBConnection db)
			throws Exception {
		SqlRunConfig config = parseSql(paramSql);
		return query(config, masterDataset, curRow, db);
	}

	public static SqlRunConfig parseSql(String paramSql) throws Exception {
		SqlRunConfig config = Parser.parse(paramSql);
		return config;
	}

}
