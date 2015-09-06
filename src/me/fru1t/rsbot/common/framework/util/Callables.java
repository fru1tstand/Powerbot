package me.fru1t.rsbot.common.framework.util;

import java.util.concurrent.Callable;

public final class Callables {
	private Callables() { }

	public static <T> Callable<T> of(final T response) {
		return new Callable<T>() {
			@Override
			public T call() throws Exception {
				return response;
			}
		};
	}
}
