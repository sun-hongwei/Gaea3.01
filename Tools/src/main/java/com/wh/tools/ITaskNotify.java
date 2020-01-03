package com.wh.tools;

public interface ITaskNotify<T>{
	void onNotify(T t, boolean isok);
}