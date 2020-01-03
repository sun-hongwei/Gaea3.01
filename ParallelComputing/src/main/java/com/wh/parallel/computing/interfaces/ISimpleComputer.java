package com.wh.parallel.computing.interfaces;

public interface ISimpleComputer<T> {
	T compute(T t1);

	T join(T t1, T t2);
}