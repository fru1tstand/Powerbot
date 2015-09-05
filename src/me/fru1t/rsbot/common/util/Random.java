package me.fru1t.rsbot.common.util;

import me.fru1t.common.annotations.Nullable;
import me.fru1t.common.collections.Tuple2;

public class Random extends org.powerbot.script.Random {
	private static final int NEXT_SKEWED_GAUSS_ATTEMPTS = 25;

	/**
	 * Returns a random enum within the set that's given. The probability that a specific enum is
	 * returned is defined within the enum and returned with the getProbability method defined
	 * in the Probability interface.
	 *
	 * @param set The enum class
	 * @return A random enum.
	 */
	// TODO(v1 cleanup): Change usages of self-implementation to use this method
	public static <T extends Enum<T> & Probability> T roll(Class<T> set) {
		int i = nextInt(0, 100);
		int sum = 0;
		T[] choices = set.getEnumConstants();
		for (T choice : choices) {
			sum += choice.getProbability();
			if (sum > i) {
				return choice;
			}
		}

		// TODO(v1): Add logging and throw a warning that the probabilities don't add to 100.
		return choices[choices.length - 1];
	}

	/**
	 * The rate at which this method returns true will tend toward the given probability after
	 * repeated calls.
	 *
	 * @param probabilityPercent The pseudo probability that this method should return true. This
	 * value should be between 0 and 100. Anything less than 0 will always return true, anything
	 * above 100 will always return false. A null probability will always return false.
	 * @return What the roll returned.
	 */
	public static boolean roll(@Nullable Integer probabilityPercent) {
		if (probabilityPercent == null) {
			return false;
		}
		return Random.nextDouble(0, 100) < probabilityPercent;
	}

	/**
	 * Returns a skewed normal distribution using Random.nextGaussian by rerolling any value
	 * that is <= min or >= max. This means that both min and max are exclusive in the range.
	 *
	 * @param min The absolute minimum (non inclusive) the result should be.
	 * @param max The absolute maximum (non inclusive) the result should be.
	 * @param mean The mean of the distribution
	 * @param stdev The standard deviation of the distribution
	 * @param attempts The number of attempts to make before falling back to simply nextInt
	 * @return A number between min and max, with which if repeated multiple times, will tend
	 * towards a unimodal skewed normal distribution about the given mean and with the given
	 * standard deviation.
	 */
	public static int nextSkewedGaussian(int min, int max, int mean, double stdev) {
		int result = min;
		int attemptCount = 0;
		while ((result > max || result < min) && attemptCount < NEXT_SKEWED_GAUSS_ATTEMPTS) {
			attemptCount++;
			result = nextGaussian(min, max, mean, stdev);
		}
		if (result > max || result < min) {
			result = Random.nextInt(min, max);
		}
		return result;
	}

	/**
	 * See {@link #nextSkewedGaussian(int, int, int, double, int)}.
	 */
	public static int nextSkewedGaussian(
			Tuple2<Integer, Integer> range, int mean, double stdev) {
		return nextSkewedGaussian(range.first, range.second, mean, stdev);
	}

	/**
	 * Returns a pseudo-generated random number.
	 * @param minMax
	 * @return A pseudo-generated random number.
	 */
	public static int nextInt(Tuple2<Integer, Integer> minMax) {
		return nextInt(minMax.first, minMax.second);
	}

	/**
	 * Returns the next pseudo-random double.
	 * @param minMax
	 * @return The next pseudo-random double.
	 */
	public static double nextDouble(Tuple2<Double, Double> minMax) {
		return nextDouble(minMax.first, minMax.second);
	}
}
