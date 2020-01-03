package wh.callstack.adapter;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import wh.callstack.interfaces.IAsyncAdapter;

public abstract class BaseAdapter implements IAsyncAdapter {
	
	AtomicReference<String> id = new AtomicReference<String>(UUID.randomUUID().toString());

	@Override
	public String getId() {
		return id.get();
	}

	@Override
	public abstract AdapterType getAdapterType();
	
	/**
	 * 执行当前adapter，此方法不应该使用任何全局未同步资源，但BaseAdapter的上下适配器对象除外
	 * @param info 要执行的任务信息
	 * @return 如果当前任务此适配器可以处理返回true，其他返回false
	 */
	protected abstract ExecuteState executeTask(TaskInfo info);
	
}
