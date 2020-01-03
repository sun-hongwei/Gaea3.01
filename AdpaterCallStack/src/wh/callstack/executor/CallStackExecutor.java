package wh.callstack.executor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import com.wh.logger.GlobalLogger;

import wh.callstack.context.Context;
import wh.callstack.interfaces.IAdapter.ExecuteState;
import wh.callstack.interfaces.IAdapter.IPendingCallback;
import wh.callstack.interfaces.IAdapter.TaskInfo;
import wh.callstack.interfaces.IAsyncAdapter;
import wh.callstack.interfaces.ICallStackExecutor;
import wh.callstack.interfaces.IContext;

public abstract class CallStackExecutor<A extends IAsyncAdapter, T extends TaskInfo> implements ICallStackExecutor<A, T> {

	class LazyInfo {
		public A adapter;
		public T taskInfo;
		public int maxRetry = 2;
	}

	List<A> adapters = Collections.synchronizedList(new ArrayList<>());
	Map<String, A> adapterMap = new HashMap<>();

	Map<String, T> pendingTasks = new ConcurrentHashMap<>();

	protected abstract void configure() throws Exception;
	
	IContext<?> context;
	
	@Override
	public IContext<?> getContext() {
		synchronized (this) {
			if (context == null)
				context = new Context<>();
			return context;
		}
	}
	
	protected void initExecutor() throws Exception {
		configure();		
	}
	
	public CallStackExecutor() throws Exception {
		initExecutor();
	}

	@Override
	public void registerAdapter(A adapter) throws Exception {
		synchronized (this) {
			if (adapters.indexOf(adapter) != -1)
				throw new Exception("adapter already existed!");
			if (adapterMap.containsKey(adapter.getId())) {
				throw new Exception("adapter already existed!");
			}

			adapters.add(adapter);
			adapterMap.put(adapter.getId(), adapter);
		}
	}

	@Override
	public void unRegisterAdapter(A adapter) {
		synchronized (this) {
			if (!adapterMap.containsKey(adapter.getId()))
				return;

			adapters.remove(adapter);
			adapterMap.remove(adapter.getId());
		}
	}

	@Override
	public void cancelPendingTask(String taskId) {
		pendingTasks.remove(taskId);
	}

	protected boolean allowAdapterExecute(A adapter, T info, Map<String, Object> callStack) {
		return true;
	}
	
	protected ExecuteState executeActions(T info, Map<String, Object> callStack, List<LazyInfo> lazyInfos)
			throws Throwable {
		List<A> prepareAdapters = new ArrayList<>(adapters);
		for (int i = prepareAdapters.size() - 1; i >= 0; i--) {
			A a = prepareAdapters.get(i);
			if (!a.checkTask(info)) {
				prepareAdapters.remove(i);
			}			
		}
		
		List<A> executeAdapters;
		if (info.ordered) {
			executeAdapters = new ArrayList<>();
			for (Object code: info.orderTaskCode) {
				for (A a : prepareAdapters) {
					if (a.checkCode(code)) {
						executeAdapters.add(a);
					}
				}
			}
		}else {
			executeAdapters = prepareAdapters;
		}
		
		ExecuteState lastES = ExecuteState.esNotSupported;
		for (A a : executeAdapters) {
			if (!allowAdapterExecute(a, info, callStack))
				continue;
			
			ExecuteState es = a.frame_execute(info, callStack, getContext());
			switch (es) {
			case esPendingPart:
			case esPending:
				info.pendingCallback = new IPendingCallback() {

					@Override
					public void callback(String taskId, JSONObject result) {
						TaskInfo pendingTask = pendingTasks.remove(taskId);

						if (pendingTask == null)
							return;

						pendingTask.result = result;

						if (pendingTask.resultCallback != null)
							pendingTask.resultCallback.callback(pendingTask, callStack);
					}
				};
				pendingTasks.put(info.taskId, info);
				if (es == ExecuteState.esPending)
					return es;
				else {
					lastES = es;
					break;
				}
			case esPart:
				lastES = es;
				break;
			case esComplete:
				return es;
			case esNotSupported:
				break;
			case esLazy:
				LazyInfo notCompleteInfo = new LazyInfo();
				notCompleteInfo.adapter = a;
				notCompleteInfo.taskInfo = info;
				lazyInfos.add(notCompleteInfo);
				lastES = es;
				break;
			}

			if (info.result != null)
				callStack.put(info.taskId, info.result);

		}

		return lastES;
	}

	@Override
	public void execute(T taskInfo) {
		Map<String, Object> callStack = new HashMap<>();	
		execute(taskInfo, callStack);
	}
	
	@Override
	public void execute(T taskInfo, Map<String, Object> callStack) {
		try {
			List<LazyInfo> lazys = new ArrayList<>();

			ExecuteState es = executeActions(taskInfo, callStack, lazys);
			if (es == ExecuteState.esNotSupported) {
				Exception e = new Exception("not support task【" + taskInfo.taskId + "】");
				throw new RuntimeException(e);
			}

			while (lazys.size() > 0) {
				LazyInfo info = lazys.remove(0);
				es = info.adapter.execute(taskInfo, callStack, getContext());
				switch (es) {
				case esLazy:
					if (info.maxRetry-- > 0)
						lazys.add(info);
					break;
				default:
					break;
				}
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
			GlobalLogger.error(CallStackExecutor.class, "CallStackExecutor execute error！", e);
			throw new RuntimeException(e);
		}
	}

}
