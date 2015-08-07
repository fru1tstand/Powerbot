package me.fru1t.rsbot.utils;

public class Random extends org.powerbot.script.Random {
	/**
	 * The rate at which this method returns true will tend toward the given probability after
	 * repeated calls.
	 * 
	 * @param probabilityPercent The pseudo probability that this method should return true. This
	 * value should be between 0 and 100. Anything less than 0 will always return true, anything
	 * above 100 will always return false.
	 * @return What the roll returned.
	 */
	public static boolean roll(int probabilityPercent) {
		return Random.nextDouble(0, 100) < probabilityPercent;
	}
}
