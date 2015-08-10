package me.fru1t.rsbot.safecracker;

import me.fru1t.rsbot.utils.Random;

public class Persona {
	private static final int impatientEatClickCount_OFF_PROB = 80;
	private static final int impatientEatClickCount_MIN_RND_CLICKS = 1;
	private static final int impatientEatClickCount_MAX_RND_CLICKS = 4;
	private static final int impatientEatClickCount_ON_NORM_PROB = 30;
	private static final double impatientEatClickCount_ON_MIN_VAR = 0.5;
	private static final double impatientEatClickCount_ON_MAX_VAR = 1.0;
	private static final int impatientEatClickCount_ON_NORM_MEAN = 2;
	private boolean impatientEatClickCount_isEnabled;
	private double impatientEatClickCount_normVariance;
	/**
	 * Description:
	 * Sometimes people like spam clicking food until it's eaten. This is that.
	 * 
	 * Trigger:
	 * Each food item eaten
	 * 
	 * Algorithm:
	 * "Off" - Never spams (returns 1)
	 * 		OFF_PROB% probability
	 * "On" - ON_NORM_PROB% chance of Norm(2, const rand[ON_MIN_VAR, ON_MAX_VAR])
	 * 		  (100 - ON_NORM_PROB)% chance of 1
	 * 		(100 - OFF_PROB)% probability
	 * 
	 * Justification:
	 * Some people like spam clicking, others don't. Those that do spam click, often don't spam
	 * click 100% of the time. 
	 * 
	 * Consider: Spam clickers may fall back to not spam clicking after some time.
	 * @return Number of times to click a food item.
	 */
	public int impatientEatClickCount() {
		if (!impatientEatClickCount_isEnabled
				|| Random.nextInt(0, 100) > impatientEatClickCount_ON_NORM_PROB) {
			return 1;
		}
		return Random.nextGaussian(
				impatientEatClickCount_MIN_RND_CLICKS,
				impatientEatClickCount_MAX_RND_CLICKS,
				impatientEatClickCount_ON_NORM_MEAN,
				impatientEatClickCount_normVariance);
	}
	private void impatientEatClickCount_setup() {
		impatientEatClickCount_isEnabled =
				(Random.nextInt(0, 100) > impatientEatClickCount_OFF_PROB);
		impatientEatClickCount_normVariance = Random.nextDouble(
				impatientEatClickCount_ON_MIN_VAR,
				impatientEatClickCount_ON_MAX_VAR);
	}
}
