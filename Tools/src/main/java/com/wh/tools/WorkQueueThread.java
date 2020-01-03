package com.wh.tools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class WorkQueueThread<T extends ITaskProcessor> extends WorkQueue<T> implements Runnable {
	int threadCount;
	ExecutorService pool;

	public WorkQueueThread(int maxTaskCount) {
		this(1, maxTaskCount);
	}

	public WorkQueueThread(int threadCount, int maxTaskCount) {
		super(maxTaskCount);
		this.threadCount = threadCount;
		pool = Executors.newFixedThreadPool(threadCount);
		pool.execute(this);
	}

	public void stop() {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(60, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public void run() {
		Thread.currentThread().setName("WorkQueueThread");
		T t = get();
		if (t == null)
			return;

		try {
			t.onProcess();
		} finally {
			pool.execute(this);
		}

	}

}
