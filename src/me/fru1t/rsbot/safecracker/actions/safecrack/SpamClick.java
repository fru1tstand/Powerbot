package me.fru1t.rsbot.safecracker.actions.safecrack;

import me.fru1t.annotations.Inject;
import me.fru1t.rsbot.common.Random;


public class SpamClick {
	private static final int ABSOLUTE_MAX_CLICKS = 5;
	private static final int ENABLE_PROBABILITY = 25;

	// Delay specific constants
	private static final int DELAY_MEAN_MAX = 175;
	private static final int DELAY_MEAN_MIN = 90;
	private static final double DELAY_VARIANCE_MIN = 0.5;
	private static final double DELAY_VARIANCE_MAX = 4;
	private static final int DELAY_IS_RANDOM_PROBABILTY = 50;

	private final boolean isEnabled;
	private boolean isDelayRandom;
	private int delayMean;
	private double delayVariance;
	private int clicks;

	@Inject
	public SpamClick() {
		isEnabled = Random.roll(ENABLE_PROBABILITY);
		if (isEnabled) {
			isDelayRandom = Random.roll(DELAY_IS_RANDOM_PROBABILTY);
			if (!isDelayRandom) {
				delayMean = Random.nextInt(DELAY_MEAN_MIN, DELAY_MEAN_MAX);
				delayVariance = Random.nextDouble(DELAY_VARIANCE_MIN, DELAY_VARIANCE_MAX);
			}
		}
	}

	/**
	 * @return The number of clicks to perform.
	 */
	public int getClicks() {
		return clicks;
	}

	/**
	 * @return The delay between a click.
	 */
	public int getDelay() {
		return (isDelayRandom) 
				? Random.nextInt(DELAY_MEAN_MIN, DELAY_MEAN_MAX)
				: Random.nextSkewedGaussian(
						DELAY_MEAN_MIN,
						DELAY_MEAN_MAX,
						delayMean,
						(int) Math.sqrt(delayVariance),
						0);
	}

	/**
	 * Generates a new number of clicks to interact with the safe. This should
	 * be called every time the player is about to crack a safe.
	 */
	public void newClicks() {
		if (!isEnabled) {
			clicks = 1;
			return;
		}

		clicks = Random.nextInt(1, ABSOLUTE_MAX_CLICKS);
	}
}
