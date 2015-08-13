package me.fru1t.rsbot.common.framework.components;

import me.fru1t.annotations.Nullable;
import me.fru1t.annotations.Singleton;
import me.fru1t.collections.Tuple2;
import me.fru1t.rsbot.common.framework.Action;

/**
 * Contains methods to quantify certain behaviors. Used in {@link Action}s to determine how the
 * player should interact with the Runescape world.
 * 
 * <p>Theory:
 * Scripts are written to follow a very strict core set of actions. On top of this, the programming
 * behind it aims to be 100% accurate. This has led to easy, easy detection, as herds of accounts
 * are following an easily discernible pattern and interact with the Runescape world with ~100%
 * accuracy. Bottom line: scripts are too afraid to make mistakes and script writers are too lazy
 * to scatter the script's footprint. 
 * 
 * <p>Persona^tm aims to throw in more humanistic traits to the script in the form of
 * attentiveness, clumsiness, impatience, etc. Each Persona has its own set of characteristics, but
 * also more importantly, has its own unique set of ideologies and methods to complete a specific
 * task. This scatters the deep footprint of a single traditional script to thousands of randomly
 * generated lighter footprints of a Persona driven script. Light footprints equals harder to
 * detect equals less bans.
 */
@Singleton
public class Persona {
	public static int MAX_FOCUS = 100;
	public static int MIN_FOCUS = 0;
	
	private int attentiveness;
	private int clumsiness;
	
	public Persona() {
		attentiveness = 100;
		clumsiness = 0;
	}
	
	/**
	 * Focus is a Persona's ability to complete a task (logically).
	 * A lower focus produces more mistakes, afk-esque breaks, mistakes (failed actions or wrong
	 * actions), etc. A higher focus produces faster APM, more accurate actions, etc.
	 * 
	 * Focus (usually) decreases over time, but depending on the instance, can trend sinusoidally,
	 * logarithmically, or rationally. Things that can affect focus are: random anomalies, failed
	 * actions, leveling (or close to leveling), "high risk" actions, etc.
	 * 
	 * @return The amount of focus the Persona currently has [0, 100]
	 */
	public int focus() {
		return Math.min(MAX_FOCUS, Math.max(MIN_FOCUS, attentiveness - clumsiness));
	}
	
	/**
	 * A convenience method for (100 - {@link #focus()}) that cleans up some calculations.
	 * @return 100 - {@link #focus()}
	 */
	public int laziness() {
		return 100 - focus();
	}
	
	/**
	 * Returns a value between minValue and maxValue that is linearly proportional to the current
	 * focus. If the focus is below focusFloor, minValue will always be returned. If the focus
	 * is above focusRoof, maxValue will always be returned.
	 * 
	 * @param focusFloor [0, 100] Focus below this level will always return minValue
	 * @param focusRoof [0, 100] Focus above this level will always return maxValue
	 * @param minValue The lowest value to return
	 * @param maxValue The highest value to return
	 * @return A value between minValue and maxValue that is linearly proportional to the current
	 * focus level.
	 */
	public double getFocusScaledDouble(
			int focusFloor, int focusRoof, double minValue, double maxValue) {
		if (focusFloor >= focusRoof 
				|| focusFloor < MIN_FOCUS
				|| focusRoof > MAX_FOCUS
				|| minValue >= maxValue) {
			throw new RuntimeException("Invalid call to #getFocusScaledValue.");
		}
		if (focus() <= focusFloor) {
			return minValue;
		}
		if (focus() >= focusRoof) {
			return maxValue;
		}

		// Will not go out of bounds due to exception check.
		return 1.0 * (focusRoof - focusFloor) / (MAX_FOCUS - MIN_FOCUS) // Scale
				* (maxValue - minValue) // Max delta
				+ minValue;
	}
	
	/**
	 * See {@link #getFocusScaledDouble(int, int, double, double)}
	 */
	public double getFocusScaledDouble(double minValue, double maxValue) {
		return getFocusScaledDouble(MIN_FOCUS, MAX_FOCUS, minValue, maxValue);
	}
	
	/**
	 * See {@link #getFocusScaledDouble(int, int, double, double)}.
	 * @param focus @Nullable
	 * @param value
	 */
	public double getFocusScaledDouble(
			@Nullable Tuple2<Integer, Integer> focus,
			Tuple2<Double, Double> value) {
		return (focus == null)
				? getFocusScaledDouble(MIN_FOCUS, MAX_FOCUS, value.first, value.second)
				: getFocusScaledDouble(focus.first, focus.second, value.first, value.second);
	}
	
	/**
	 * See {@link #getFocusScaledDouble(int, int, double, double)}
	 */
	public int getFocusScaledInt(int focusFloor, int focusRoof, int minValue, int maxValue) {
		return (int) getFocusScaledDouble(focusFloor, focusRoof, minValue, maxValue);
	}
	
	/**
	 * See {@link #getFocusScaledDouble(int, int, double, double)}
	 */
	public int getFocusScaledInt(int minValue, int maxValue) {
		return (int) getFocusScaledDouble(MIN_FOCUS, MAX_FOCUS, minValue, maxValue);
	}
	
	/**
	 * See {@link #getFocusScaledDouble(int, int, double, double)}
	 * @param focus @Nullable
	 * @param value
	 */
	public int getFocusScaledInt(
			@Nullable Tuple2<Integer, Integer> focus,
			Tuple2<Integer, Integer> value) {
		return (int) ((focus == null)
				? getFocusScaledDouble(MIN_FOCUS, MAX_FOCUS, value.first, value.second)
				: getFocusScaledDouble(focus.first, focus.second, value.first, value.second));
	}
}
