package me.fru1t.collections;

/**
 * Holds 2 elements.
 *
 * @param <T1> The type of the first element.
 * @param <T2> The type of the second element.
 */
public class Tuple2<T1, T2> {
	/**
	 * Creates a new tuple with the given elements
	 */
	public static <T1, T2> Tuple2<T1, T2> from(T1 first, T2 second) {
		return new Tuple2<T1, T2>(first, second);
	}
	
	public final T1 first;
	public final T2 second;
	
	public Tuple2(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}
}
