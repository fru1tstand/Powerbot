package me.fru1t.slick.util;

/**
 * Defines an interface for a provider of type T. The provider interface allows a layer of
 * abstraction for injection.
 *
 * <p>Note: Providers aren't singleton though they are treated like them in Slick because,
 * depending on the implementation of the provider, can produce non-singleton objects. To
 * prevent confusion, Providers themselves, aren't singleton.</p>
 *
 * @param <T>
 */
public interface Provider<T> {
	/**
	 * Returns an instance of the type.
	 *
	 * @return The instance of the type.
	 */
	T get();
}
