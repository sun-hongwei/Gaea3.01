package com.wh.tools;

import java.lang.reflect.Method;

public class Magic {
	public static Object call(Class<?> superClass, Object instance, String methodOfParentToExec, Object... args) throws Exception {
		Class<?>[] cs = args == null || args.length == 0 ? null : new Class[args.length];
		if (cs != null) {
			for (int i = 0; i < cs.length; i++) {
				cs[i] = args[i].getClass();
			}
		}
		
		Method method = superClass.getDeclaredMethod(methodOfParentToExec, cs);
		method.setAccessible(true);
		return method.invoke(instance, args);
	}
}
