package me.fru1t.rsbot.common.framework.util;

public final class Callables {
	private Callables() { }

	/**
	 * Creates a simple Callable that is of the given parameter's type and that only returns the
	 * given parameter.
	 *
	 * @param response The response that the returning Callable's call method should return.
	 * @return A Callable object that returns the given response.
	 */
	public static <T> Callable<T> of(final T response) {
		return new Callable<T>() {
			@Override
			public T ring() {
				return response;
			}
		};
	}
}
