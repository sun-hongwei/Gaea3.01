package com.wh.tools;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class WorkQueue<T> implements IWorkQueue<T> {
	ArrayBlockingQueue<T> tasks;
	int maxTaskCount;

	public WorkQueue(int maxTaskCount) {
		this.maxTaskCount = maxTaskCount;
		init();
	}

	@Override
	public void init() {
		tasks = new ArrayBlockingQueue<T>(maxTaskCount, true);
	}

	@Override
	public void wakeup() throws InterruptedException {
		tasks.put(null);
	}
	
	@Override
	public void add(T task) throws IOException, InterruptedException {
		tasks.put(task);
	}

	@Override
	public T get() {
		try {
			return tasks.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public T get(int timeout) throws InterruptedException {
		return tasks.poll(timeout, TimeUnit.MILLISECONDS);
	}

	@Override
	public void reset() {
		tasks.clear();
	}

}
