package com.wh.gaea.interfaces.selector;

import org.json.JSONObject;

import com.wh.gaea.control.KeyValue;
import com.wh.gaea.selector.KeyValueSelector.Result;

public interface IWorkflowSelector {
	public static class RunFlowInfo{
		public String id;
		public String name;
		public String memo;
		public JSONObject flowData;
		
		@Override
		public String toString(){
			return name == null || name.isEmpty() ? id : name;
		}
	}
	
	public static class RunFlowResult extends Result{
		public KeyValue<String, String> runFlowInfo;
		public RunFlowResult(KeyValue<String, String> runFlowInfo, boolean isok) {
			this.runFlowInfo = runFlowInfo;
			this.isok = isok;
		}
	}
	
	String selectDecideValue(String decide);
	
	RunFlowResult selectRunFlowInfo() throws Exception;
}
