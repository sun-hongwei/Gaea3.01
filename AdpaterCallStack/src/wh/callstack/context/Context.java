package wh.callstack.context;

import java.util.Map;

import wh.callstack.interfaces.IAdapter.TaskInfo;
import wh.callstack.interfaces.IContext;
import wh.callstack.interfaces.IExecutorNotify;

public class Context<T> implements IContext<T>{
	protected IExecutorNotify executorNotify;
	
	@Override
	public void setExecutorNotify(IExecutorNotify callback) {
		synchronized (this) {
			executorNotify = callback;			
		}
	}
	
	@Override
	public IExecutorNotify getExecutorNotify() {
		synchronized (this) {
			return executorNotify;			
		}
	}
	
	@Override
	public void fireUncaptureEvent(TaskInfo taskInfo, Map<String, Object> callStack) {
		IExecutorNotify executorNotify = getExecutorNotify();
		if (executorNotify != null) {
			executorNotify.onUncaptureEvent(taskInfo, callStack);
		}
	}
	
	@Override
	public void fireWaitEvent(TaskInfo taskInfo, Map<String, Object> callStack) {
		IExecutorNotify executorNotify = getExecutorNotify();
		if (executorNotify != null) {
			executorNotify.onWait(taskInfo, callStack);
		}
	}
	
	@Override
	public void fireReset(TaskInfo taskInfo, Map<String, Object> callStack) {
		IExecutorNotify executorNotify = getExecutorNotify();
		if (executorNotify != null) {
			executorNotify.onReset(taskInfo, callStack);
		}
	}

	@Override
	public T getWaitObject() {
		return null;
	}

	@Override
	public void fireCheckEvent(TaskInfo taskInfo, Map<String, Object> callStack) {
		IExecutorNotify executorNotify = getExecutorNotify();
		if (executorNotify != null) {
			executorNotify.onCheck(taskInfo, callStack);
		}
	}
	

}
