package com.wh.tools;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public abstract class IdHelper {
	static ThreadLocal<AtomicLong> counterVar = new ThreadLocal<AtomicLong>() {
		@Override
		protected AtomicLong initialValue() {
			return new AtomicLong();
		}
	};

	public static final String SPLITCHAR = "-";

	public static String genOrderID(Long index, Object... prexs) {
		long time = new Date().getTime();

		if (index == null) {
			AtomicLong counter = counterVar.get();
			if (counter == null)
				return null;

			counter.compareAndSet(Long.MAX_VALUE, 0);
			index = counter.incrementAndGet();
		}
		
		String prex = null;
		if (prexs != null)
			for (Object object : prexs) {
				if (object != null) {
					if (prex == null || prex.isEmpty())
						prex = object.toString();
					else {
						prex += SPLITCHAR + object.toString();
					}
				}
			}
		return ((prex == null || prex.isEmpty()) ? "id" : prex) + SPLITCHAR + time + SPLITCHAR
				+ index;
	}

	public static String genID(Object... prexs) {
		return genOrderID(null, prexs);
	}

	public static void reset() {
		counterVar.get().set(0);
	}
}
