package wh.callstack.interfaces;

import java.util.Map;

public interface ICallStackExecutor<A, T> {

	void registerAdapter(A adapter) throws Exception;

	void unRegisterAdapter(A adapter);

	void cancelPendingTask(String taskId);

	void execute(T taskInfo);

	void execute(T taskInfo, Map<String, Object> callStack);

	IContext<?> getContext();

}