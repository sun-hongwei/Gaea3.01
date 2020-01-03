package com.wh.parallel.computing;

import java.util.List;
import java.util.concurrent.RecursiveAction;

import com.wh.parallel.computing.interfaces.ISimpleActionComputer;

public class SimpleAction<T> extends RecursiveAction {
	private static final long serialVersionUID = 1L;

	ISimpleActionComputer<T> computer;
	List<T> datas;
	int start, end, threshold;

	public SimpleAction(List<T> datas, int start, int end, int threshold, ISimpleActionComputer<T> computer) {
		this.datas = datas;
		this.start = start;
		this.end = end;
		this.threshold = threshold;
		this.computer = computer;
	}

	protected void compute() {
		if (end - start < threshold) {
			for (int i = start; i < end; i++) {
				try {
					computer.compute(datas.get(i));
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}else {
			int mid = (start + end) >>> 1;
			SimpleAction<T> leftTask = new SimpleAction<>(datas, start, mid, threshold, computer);
			SimpleAction<T> rightTask = new SimpleAction<>(datas, mid, end, threshold, computer);
			invokeAll(leftTask, rightTask);
		}
	}
}