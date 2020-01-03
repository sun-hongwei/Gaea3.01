package com.wh.parallel.computing;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import com.wh.parallel.computing.interfaces.IParallelComputingExecutor;
import com.wh.parallel.computing.interfaces.ISimpleActionComputer;
import com.wh.parallel.computing.interfaces.ISimpleComputer;

public class ParallelComputingExecutor<T> implements IParallelComputingExecutor<T> {
	final ForkJoinPool pool;
	final List<T> datas;
	final int threshold;

	public ParallelComputingExecutor(List<T> datas, int threshold) {
		this(null, datas, threshold);
	}
	
	public ParallelComputingExecutor(Integer workerCount, List<T> datas, int threshold) {
		if (workerCount == null)
			pool = new ForkJoinPool();
		else {
			pool = new ForkJoinPool(workerCount);
		}
		
		this.datas = datas;
		this.threshold = threshold;
	}

	@Override
	public T submit(ISimpleComputer<T> computer)
			throws InterruptedException, ExecutionException {
		return pool.submit(new SimpleTask<T>(datas, 0, datas.size(), threshold, computer)).get();
	}

	@Override
	public void execute(ISimpleActionComputer<T> computer)
			throws InterruptedException, ExecutionException {
		pool.execute(new SimpleAction<T>(datas, 0, datas.size(), threshold, computer));
	}

}
