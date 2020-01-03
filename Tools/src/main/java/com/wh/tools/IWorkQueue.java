package com.wh.tools;

import java.io.IOException;

public interface IWorkQueue<T> {

	void init();

	void wakeup() throws InterruptedException;

	void add(T task) throws IOException, InterruptedException;

	T get();

	T get(int timeout) throws InterruptedException;

	void reset();

}