package me.fru1t.rsbot.common.framework.util;

/**
 * A wrapper for the Callable interface that doesn't throw an exception. This is used to clean up
 * code that we can verify doesn't throw exceptions.
 */
// TODO(v1 cleanup): Remove instances of Callable from Concurrent package and large clunky try
// catch statements.
public abstract class Callable<T> implements java.util.concurrent.Callable<T> {
	/**
	 * Synonymous to {@link #call()}.
	 * @return
	 */
	public abstract T ring();

	/**
	 * Computes a result.
	 *
	 * @return Computed result.
	 */
	@Override
	public T call() throws Exception {
		return ring();
	}
}
