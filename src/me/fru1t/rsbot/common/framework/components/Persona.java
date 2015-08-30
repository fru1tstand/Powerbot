package me.fru1t.rsbot.common.framework.components;

import me.fru1t.common.annotations.Nullable;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.common.collections.Tuple2;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.framework.util.Random;

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

	public static int MAX_FOCUS = 100;
	public static int MIN_FOCUS = 0;

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
	 * Returns the time the player should wait before doing another interaction. This should be used
	 * when interacting with multiple items, objects, etc. (eg. right clicking a menu and selecting
	 * an option; clicking different items within an inventory)
	 *
	 * @return The time in ms.
	 */
	public int getNextInteractDelay() {
		return Random.nextSkewedGaussian(
				INTERACT_DELAY_RANGE,
				getFocusScaledInt(null, INTERACT_DELAY_RANGE),
				getFocusScaledDouble(null, INTERACT_DELAY_VARIANCE));
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
				getFocusScaledInt(null, SPAM_DELAY_RANGE),
				getFocusScaledDouble(null, SPAM_DELAY_VARIANCE));
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
