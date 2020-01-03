package com.wh.logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalLogger{
	
	static ExecutorService pools = Executors.newFixedThreadPool(2);
	
	protected static void execute(Runnable runnable){
		pools.execute(runnable);
	}
	
	public static void error(Class<?> c, Object msg, Throwable e) {
		getLogger(c).error(msg == null ? "" : msg.toString(), e);
	}
	
	public static void info(Class<?> c, Object msg) {
		getLogger(c).info(msg == null ? "" : msg.toString());
	}
	
	public static void warn(Class<?> c, Object msg) {
		warn(c, msg, null);
	}
	
	public static void warn(Class<?> c, Object msg, Throwable t) {
		getLogger(c).warn(msg == null ? "" : msg.toString(), t);
	}
	
	static Logger getLogger(Class<?> c) {
		Logger logger = LoggerFactory.getLogger(c);
		return logger;
	}
}
