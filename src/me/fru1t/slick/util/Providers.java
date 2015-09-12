package me.fru1t.slick.util;

/**
 *	Utility methods for Providers
 */
public class Providers {
	private Providers() { }

	/**
	 * Creates a provider that provides the given object.
	 *
	 * @param object The object to provide.
	 * @param <T> The type
	 * @return A provider which provides the object passed.
	 */
	public static <T> Provider<T> of(final T object) {
		return new Provider<T>() {
			@Override
			public T get() {
				return object;
			}
		};
	}
}
