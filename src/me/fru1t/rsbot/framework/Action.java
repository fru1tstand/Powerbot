package me.fru1t.rsbot.framework;

/**
 * Defines a container class for a script Action that holds the ClientContext and Settings for a
 * script.
 */
public interface Action {
	/**
	 * The action to perform.
	 * @return If the run completed successfully.
	 */
	public abstract boolean run();
}
