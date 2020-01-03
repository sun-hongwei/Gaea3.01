package com.wh.parallel.computing.interfaces;

import java.util.concurrent.ExecutionException;

public interface IParallelComputingExecutor<T> {

	T submit(ISimpleComputer<T> computer) throws InterruptedException, ExecutionException;

	void execute(ISimpleActionComputer<T> computer) throws InterruptedException, ExecutionException;

}