package me.fru1t.rsbot.common.framework.components;

import me.fru1t.common.annotations.Singleton;
import me.fru1t.common.collections.Tuple2;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.util.Random;

/**
 * Contains methods to quantify certain behaviors. Used in {@link Strategy}s to determine how the
 * player should interact with the Runescape world.
 *
 * <p>Theory:
 * Scripts are written to follow a very strict core set of actions. On top of this, the programming
 * behind it aims to be 100% accurate. This has led to easy, easy detection, as herds of accounts
 * are following an easily discernible pattern and interact with the Runescape world with ~100%
 * accuracy. Bottom line: scripts are too afraid to make mistakes and script writers are too lazy
 * to make them do so.
 *
 * <p>Persona^tm aims to throw in more humanistic traits to the script in the form of
 * attentiveness, clumsiness, impatience, etc. Each Persona has its own set of characteristics, but
 * also more importantly, has its own unique set of ideologies and methods to complete a specific
 * task. This scatters the deep footprint of a single traditional script to thousands of randomly
 * generated lighter footprints of a Persona driven script. Light footprints equals harder to
 * detect equals less bans.
 *
 * <p>Things to consider: While persona-driven scripting introduces more humanistic traits, there
 * is nothing more revealing that a bot is a bot than artificial breaks within a set of commands.
 * Where it might be natural for a script to be programmed with discrete bundles of commands
 * (eg. walking, banking, walking, mining), humans don't naturally stop to make sure they're within
 * a bank to begin banking. They may interact with the banker while still traveling. This is
 * something scripts need to mimic, that is, the blending between states.
 */
@Singleton
public class Persona {
	/**
	 * Interact delay range is a range of times that a player should wait before interacting with
	 * different objects in a scene. See {@link #getNextInteractDelay()}
	 */
	public static Tuple2<Integer, Integer> INTERACT_DELAY_RANGE = Tuple2.of(100, 300);
	public static Tuple2<Double, Double> INTERACT_DELAY_VARIANCE = Tuple2.of(1d, 5d);

	/**
	 * Spam delay range is a range of times that a player should wait before spamming an action
	 * (eg. clicking the same spot repeatedly).
	 */
	public static Tuple2<Integer, Integer> SPAM_DELAY_RANGE = Tuple2.of(80, 175);
	public static Tuple2<Double, Double> SPAM_DELAY_VARIANCE = Tuple2.of(0.5d, 4d);

	/**
	 * Min/Max focus
	 */
	public static Tuple2<Integer, Integer> FOCUS_RANGE = Tuple2.of(0, 100);

	private int attentiveness;
	private int clumsiness;

	/**
	 * Creates a new persona.
	 *
	 * <p>TODO: Implement better constructor and decay algorithms. Most likely going to require a
	 * Timer for the {@link #focus()} method.
	 */
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
		return Math.min(
				FOCUS_RANGE.second, Math.max(FOCUS_RANGE.first, attentiveness - clumsiness));
	}

	/**
	 * A convenience method for (100 - {@link #focus()}) that cleans up some calculations.
	 * @return 100 - {@link #focus()}
	 */
	public int laziness() {
		return 100 - focus();
	}

	/**
	 * Returns the time the player should wait before doing another interaction. This should be used
	 * when interacting with multiple items, objects, etc. (eg. right clicking a menu and selecting
	 * an option; clicking different items within an inventory)
	 *
	 * @return The time in ms.
	 */
	public int getNextInteractDelay() {
		return Random.nextSkewedGaussian(
				INTERACT_DELAY_RANGE,
				getLazinessScaledInt(INTERACT_DELAY_RANGE),
				getLazinessScaledDouble(INTERACT_DELAY_VARIANCE));
	}

	/**
	 * Returns the time the player should wait before doing a spammy action. This should be used
	 * when repeatedly interacting with the same object. (eg. imaptiently clicking on an object to
	 * use or interact with)
	 *
	 * @return The time to wait in ms.
	 */
	public int getNextSpamDelay() {
		return Random.nextSkewedGaussian(
				SPAM_DELAY_RANGE,
				getLazinessScaledInt(SPAM_DELAY_RANGE),
				getLazinessScaledDouble(SPAM_DELAY_VARIANCE));
	}

	/**
	 * Returns a value between minValue and maxValue that is linearly proportional to the passed
	 * value as a percentage. If the value is below the floor, min will always be returned. If the
	 * value is above the roof, max will always be returned.
	 *
	 * @param cutoff The cutoff to use.
	 * @param range The range of return values.
	 * @param scale The value to use as scale.
	 * @return A value within the range.
	 */
	public double getScaledDouble(
			Tuple2<Integer, Integer> cutoff, Tuple2<Double, Double> range, int scale) {
		if (cutoff.first >= cutoff.second
				|| cutoff.first < FOCUS_RANGE.first
				|| cutoff.second > FOCUS_RANGE.second
				|| range.first >= range.second) {
			throw new RuntimeException("Invalid call to #getFocusScaledValue.");
		}
		if (scale <= cutoff.first) {
			return range.first;
		}
		if (scale >= cutoff.second) {
			return range.second;
		}

		// Will not go out of bounds due to exception check.
		return 1.0
				* (cutoff.second - cutoff.first) / (FOCUS_RANGE.second - FOCUS_RANGE.first) // Scale
				* (range.second - range.first) // Max delta
				+ range.first;
	}

	/**
	 * @see #getScaledDouble
	 */
	public int getScaledInt(Tuple2<Integer, Integer> cutoff, Tuple2<Integer, Integer> range, int scale) {
		return (int) Math.round(getScaledDouble(
				cutoff,
				new Tuple2<Double, Double>((double) range.first, (double) range.second),
				scale));
	}

	/**
	 * Returns a value within the given range that's linearly scaled to the current focus.
	 *
	 * @param returnRange The range of return values.
	 * @return A value within the range.
	 */
	public double getFocusScaledDouble(Tuple2<Double, Double> returnRange) {
		return getScaledDouble(FOCUS_RANGE, returnRange, focus());
	}

	/**
	 * Returns a value within the given range that's linearly scaled to the current focus between
	 * the given cutoff. If the focus is less than the lower bound of the cutoff, the lower end
	 * of the range is returned. If the focus is greater than the upper bound of the cutoff, the
	 * higher end of the range is returned.
	 *
	 * @param cutoff The cutoff to use.
	 * @param returnRange The range of return values.
	 * @return A value within the range.
	 */
	public double getFocusScaledDouble(
			Tuple2<Integer, Integer> cutoff, Tuple2<Double, Double> returnRange) {
		return getScaledDouble(cutoff, returnRange, focus());
	}

	/**
	 * Returns value within the given range that's linearly scaled to the current focus.
	 *
	 * @param returnRange The range of return values.
	 * @return A value within the range.
	 */
	public int getFocusScaledInt(Tuple2<Integer, Integer> returnRange) {
		return getScaledInt(FOCUS_RANGE, returnRange, focus());
	}

	/**
	 * Returns a value within the given range that's linearly scaled to the current focus between
	 * the given cutoff. If the focus is less than the lower bound of the cutoff, the lower end
	 * of the range is returned. If the focus is greater than the upper bound of the cutoff, the
	 * higher end of the range is returned.
	 *
	 * @param cutoff The cutoff to use.
	 * @param returnRange The range of return values.
	 * @return A value within the range.
	 */
	public int getFocusScaledInt(
			Tuple2<Integer, Integer> cutoff, Tuple2<Integer, Integer> returnRange) {
		return getScaledInt(cutoff, returnRange, focus());
	}

	/**
	 * Returns a value within the given range that's linearly scaled to the current laziness.
	 *
	 * @param returnRange The range of return values.
	 * @return A value within the range.
	 */
	public double getLazinessScaledDouble(Tuple2<Double, Double> returnRange) {
		return getScaledDouble(FOCUS_RANGE, returnRange, laziness());
	}

	/**
	 * Returns a value within the given range that's linearly scaled to the current laziness between
	 * the given cutoff. If the lasiness is less than the lower bound of the cutoff, the lower end
	 * of the range is returned. If the laziness is greater than the upper bound of the cutoff, the
	 * higher end of the range is returned.
	 *
	 * @param cutoff The cutoff to use.
	 * @param returnRange The range of return values.
	 * @return A value within the range.
	 */
	public double getLazinessScaledDouble(
			Tuple2<Integer, Integer> cutoff, Tuple2<Double, Double> returnRange) {
		return getScaledDouble(cutoff, returnRange, laziness());
	}

	/**
	 * Returns a value within the given range that's linearly scaled to the current laziness.
	 *
	 * @param returnRange The range of return values.
	 * @return A value within the range.
	 */
	public int getLazinessScaledInt(Tuple2<Integer, Integer> returnRange) {
		return getScaledInt(FOCUS_RANGE, returnRange, laziness());
	}

	/**
	 * Returns a value within the given range that's linearly scaled to the current laziness between
	 * the given cutoff. If the lasiness is less than the lower bound of the cutoff, the lower end
	 * of the range is returned. If the laziness is greater than the upper bound of the cutoff, the
	 * higher end of the range is returned.
	 *
	 * @param cutoff The cutoff to use.
	 * @param returnRange The range of return values.
	 * @return A value within the range.
	 */
	public int getLazinessScaledInt(Tuple2<Integer, Integer> cutoff, Tuple2<Integer, Integer> returnRange) {
		return getScaledInt(cutoff, returnRange, laziness());
	}
}
