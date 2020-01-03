package wh.callstack.interfaces;

import java.util.Map;

import wh.callstack.interfaces.IAdapter.TaskInfo;

public interface IContext<T> {
	void setExecutorNotify(IExecutorNotify callback);

	IExecutorNotify getExecutorNotify();

	T getWaitObject();

	void fireCheckEvent(TaskInfo taskInfo, Map<String, Object> callStack);
	
	void fireUncaptureEvent(TaskInfo taskInfo, Map<String, Object> callStack);

	void fireWaitEvent(TaskInfo taskInfo, Map<String, Object> callStack);

	void fireReset(TaskInfo taskInfo, Map<String, Object> callStack);


}
