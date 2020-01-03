package com.wh.http.Interface;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.logger.GlobalLogger;

public abstract class BaseHttp {
	public static final String Result_Data_Key = "data";
	public static final String Result_State_Key = "ret";
	public static final int Result_State_OK = 0;
	public static final int Result_State_Fail = -1;

	protected void writeLog(Object msg) {
		GlobalLogger.info(this.getClass(), msg);
	}

	protected void writeWarn(Object msg) {
		GlobalLogger.warn(this.getClass(), msg);
	}

	protected abstract void setContentType(String contentType);
	protected abstract void setHeader(String header, Object value);
	protected abstract void setCharacterEncoding(String encoding);
	protected abstract void setStatus(int state);
	protected abstract void setBufferSize(int bufferSize);
	protected abstract void clear();
	protected abstract PrintWriter getWriter() throws IOException;
	protected abstract String getOrigin() throws IOException;
	
	protected void addResponseHeader() {
		setContentType("application/json");
		setHeader("Access-Control-Allow-Credentials", "true");
		try {
			setHeader("Access-Control-Allow-Origin", getOrigin());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		setHeader("Pragma", "No-cache");
		setHeader("Cache-Control", "no-cache");
		setCharacterEncoding("UTF-8");
	}

	protected static void addStateToResponse(int ret, JSONObject responseJson) throws Exception {
		addToJson(Result_State_Key, ret, responseJson);
	}

	@SuppressWarnings("rawtypes")
	protected static void addToJson(String id, Object data, Object resultObject){
		Class<?> c = data.getClass();
		if (data instanceof byte[]) {
			String baseData = Base64.encodeBase64URLSafeString((byte[])data);
			((JSONObject) resultObject).put(id, baseData);			
		} else if (c.isArray()) {
			JSONArray values = new JSONArray();
			int len = Array.getLength(c);
			for (int i = 0; i < len; i++) {
				addToJson(null, Array.get(data, i), values);
			}
			addToJson(id, values, resultObject);
		} else if (data instanceof List) {
			List list = (List) data;
			JSONArray values = new JSONArray();
			for (Object value : list) {
				addToJson(null, value, values);
			}
			addToJson(id, values, resultObject);
		} else if (data instanceof IResultData) {
			Object value = data;
			IResultData resultData = (IResultData) data;
			try {
				value = resultData.getResult();
				if (resultObject instanceof JSONArray) {
					((JSONArray) resultObject).put(value);
				} else if (resultObject instanceof JSONObject) {
					if (id == null || id.isEmpty())
						throw new RuntimeException("key is null!");
					((JSONObject) resultObject).put(id, value);
				} else {
					throw new RuntimeException("resultObjece type not supported!");
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

		} else if (data instanceof Map) {
			Map map = (Map) data;
			JSONObject values = new JSONObject();
			for (Object key : map.keySet()) {
				addToJson(key.toString(), map.get(key), values);
			}
			addToJson(id, values, resultObject);
		} else {
			Object value = data;

			if (resultObject instanceof JSONArray) {
				((JSONArray) resultObject).put(value);
			} else if (resultObject instanceof JSONObject) {
				if (id == null || id.isEmpty())
					throw new RuntimeException("key is null!");
				((JSONObject) resultObject).put(id, value);
			} else {
				throw new RuntimeException("resultObjece type not supported!");
			}
		}
	}

	protected void fillResponse(JSONObject responseJson)  {
		String utfString = responseJson.toString();
		try {
			addResponseHeader();
			setStatus(200);
			setBufferSize(utfString.length());
			PrintWriter writer = getWriter();
			writer.write(utfString);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	protected void setResponse(Object value)  {
		setResponse(Result_Data_Key, Result_State_OK, value);
	}

	public static JSONObject getFailResultPackage(Object error) {
		JSONObject data = new JSONObject();
		data.put(Result_State_Key, Result_State_Fail);
		if (error != null) {
			data.put(Result_Data_Key, error.toString());
		}
		
		return data;
	}
	
	protected void setResponseError(Object value)  {
		setResponseError(Result_State_Fail, value);
	}

	protected void setResponseError(int ret, Object value)  {
		if (ret > 0)
			ret = -ret;
		else if (ret == 0)
			ret = Result_State_Fail;
		setResponse(Result_Data_Key, ret, value);
	}

	protected void setResponse(String id, int ret, Object value) {
		JSONObject responseJson = new JSONObject();
		clear();
		try {
			addToJson(Result_State_Key, ret, responseJson);
			if (value != null)
				if (!(value instanceof String && ((String) value).isEmpty()))
					addToJson(id, value, responseJson);
			fillResponse(responseJson);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
