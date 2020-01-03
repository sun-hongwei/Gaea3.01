package com.wh.parallel.computing;

import java.util.List;
import java.util.concurrent.RecursiveTask;

import com.wh.parallel.computing.interfaces.ISimpleComputer;

public class SimpleTask<T> extends RecursiveTask<T> {
	private static final long serialVersionUID = 1L;

	ISimpleComputer<T> computer;
	List<T> datas;
	int start, end, threshold;

	public SimpleTask(List<T> datas, int start, int end, int threshold, ISimpleComputer<T> computer) {
		this.datas = datas;
		this.start = start;
		this.end = end;
		this.threshold = threshold;
		this.computer = computer;
	}

	protected T compute() {
		T result = null;
		if (end - start <= threshold) {
			int i = 0;
			while (i < end) {
				result = computer.join(computer.compute(datas.get(i++)), computer.compute(datas.get(i++)));
			}
		}else {
			int mid = (start + end) / 2;
			SimpleTask<T> leftTask = new SimpleTask<>(datas, start, mid - 1, threshold, computer);
			SimpleTask<T> rightTask = new SimpleTask<>(datas, mid, end, threshold, computer);
			result = computer.join(leftTask.join(), rightTask.join());
		}
		
		return result;
	}
}