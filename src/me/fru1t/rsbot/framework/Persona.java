package me.fru1t.rsbot.framework;

/**
 * Contains methods to quantify certain behaviors. Used in {@link Action}s to determine how the
 * player should interact with the Runescape world.
 * 
 * Theory:
 * Scripts are written to follow a very strict core set of actions. On top of this, the programming
 * behind it aims to be 100% accurate. This has led to easy, easy detection, as herds of accounts
 * are following an easily discernible pattern and interact with the Runescape world with ~100%
 * accuracy. Bottom line: scripts are too afraid to make mistakes and script writers are too lazy
 * to scatter the script's footprint. 
 * 
 * Persona^tm aims to throw in more humanistic traits to the script in the form of
 * attentiveness, clumsiness, impatience, etc. Each Persona has its own set of characteristics, but
 * also more importantly, has its own unique set of ideologies and methods to complete a specific
 * task. This scatters the deep footprint of a single traditional script to thousands of randomly
 * generated lighter footprints of a Persona driven script. Light footprints equals harder to
 * detect equals less bans.
 */
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
}
