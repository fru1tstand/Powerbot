package me.fru1t.rsbot.common.random;

import me.fru1t.rsbot.common.Random;
import me.fru1t.rsbot.util.Tuple2;

/**
 * Contains normal (gaussian) distribution parameters alongside convenience methods.
 */
public class RandomNormalDistribution {
	private final Tuple2<Integer, Integer> absoluteMinMax;
	private final Tuple2<Double, Double> varianceMinMax;
	private final Tuple2<Integer, Integer> meanMinMax;
	private Double lockedStdev;
	private Integer lockedMean;
	
	public RandomNormalDistribution(
			Tuple2<Integer, Integer> absoluteMinMax,
			Tuple2<Integer, Integer> meanMinMax,
			Tuple2<Double, Double> varianceMinMax) {
		this.absoluteMinMax = absoluteMinMax;
		this.varianceMinMax = varianceMinMax;
		this.meanMinMax = meanMinMax;
		
		this.lockedMean = null;
		this.lockedStdev = null;
	}
	
	public RandomNormalDistribution(
			int absoluteMin, int absoluteMax,
			int minMean, int maxMean,
			double minVariance, double maxVariance) {
		this(Tuple2.from(absoluteMin, absoluteMax),
				Tuple2.from(minMean, maxMean),
				Tuple2.from(minVariance, maxVariance));
	}
	
	/**
	 * Locks a random mean and variance given on object construct, and will stick with the same
	 * values through multiple calls.
	 * @return A pseudo random value.
	 */
	public int getLockedValue() {
		prepareLockedValues();
		return Random.nextGaussian(
				absoluteMinMax.first,
				absoluteMinMax.second,
				lockedMean,
				lockedStdev);
	}
	
	/**
	 * Locks a random mean and variance given on object construct, and will stick with the same
	 * values through mutiple calls.
	 * @return A pseudo random value.
	 */
	public int getLockedSkewedValue() {
		prepareLockedValues();
		return Random.nextSkewedGaussian(
				absoluteMinMax.first,
				absoluteMinMax.second,
				lockedMean,
				lockedStdev,
				0);
	}
	
	/**
	 * Generates a pseudo random number from a gaussian distribution modeled by a random mean and
	 * variance from the ranges given.
	 * @return
	 */
	public int getValue() {
		return Random.nextGaussian(
				absoluteMinMax.first,
				absoluteMinMax.second,
				Random.nextInt(meanMinMax),
				Math.sqrt(Random.nextDouble(varianceMinMax)));
	}
	
	/**
	 * Generates a pseudo random number from a skewed guassian distribution modeled by a random
	 * mean and variance from the ranges given.
	 * @return
	 */
	public int getSkewedValue() {
		return Random.nextSkewedGaussian(
				absoluteMinMax.first,
				absoluteMinMax.second,
				Random.nextInt(meanMinMax),
				Math.sqrt(Random.nextDouble(varianceMinMax)),
				0);
	}
	
	/**
	 * Returns a pseudo random number generated between the given range.
	 * @return A pseudo random number within the mean range.
	 */
	public int getRandomMean() {
		return Random.nextInt(meanMinMax);
	}
	
	/**
	 * Returns a pseudo random number generated between the given absolute range.
	 * @return A pseudo random number within the absolute range.
	 */
	public int getRandomAbsolute() {
		return Random.nextInt(absoluteMinMax);
	}
	
	/**
	 * Verifies that both stdev and mean locked values are set.
	 */
	private void prepareLockedValues() {
		if (lockedStdev == null) {
			lockedStdev = Math.sqrt(Random.nextDouble(varianceMinMax));
		}
		if (lockedMean == null) {
			lockedMean = Random.nextInt(meanMinMax);
		}
	}
}
