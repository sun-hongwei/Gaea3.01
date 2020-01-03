package wh.callstack.interfaces;

import java.util.Map;

import wh.callstack.interfaces.IAdapter.TaskInfo;

public interface IExecutorNotify {
	void onUncaptureEvent(TaskInfo taskInfo, Map<String, Object> callStack);
	void onReset(TaskInfo taskInfo, Map<String, Object> callStack);
	void onWait(TaskInfo taskInfo, Map<String, Object> callStack);	
	void onCheck(TaskInfo taskInfo, Map<String, Object> callStack);	
}
