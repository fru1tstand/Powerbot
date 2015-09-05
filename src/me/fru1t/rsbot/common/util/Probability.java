package me.fru1t.rsbot.common.util;

public interface Probability {
	/**
	 * Gets the probability of this option as a percent [0, 100].
	 * @return The probability that this item should be chosen.
	 */
	public int getProbability();
}
