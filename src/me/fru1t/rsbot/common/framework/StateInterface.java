package me.fru1t.rsbot.common.framework;

/**
 * Defines an interface for a script's state enum.
 */
public interface StateInterface<ST extends Enum<ST>> {
	/**
	 * Returns the class that handles this state.
	 * @return The class that handles this state.
	 */
	public Class<? extends Strategy<ST>> getControllingClass();
}
