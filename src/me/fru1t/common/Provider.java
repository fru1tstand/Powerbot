package me.fru1t.common;

/**
 * Defines an interface for a provider of type T. The provider interface allows a layer of
 * abstraction for injection.
 *
 * @param <T>
 */
public interface Provider<T> {
	/**
	 * Returns an instance of the type.
	 *
	 * @return
	 */
	public T get();
}
