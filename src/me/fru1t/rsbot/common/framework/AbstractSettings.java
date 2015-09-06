package me.fru1t.rsbot.common.framework;

/**
 * This file provides an outline for what a Settings file should have.
 */
public abstract class AbstractSettings {
	/**
	 * Allows for a Javascript-like callback for when the settings are set.
	 * @param <T> The implementing setting's class.
	 */
	public static interface Callback<T extends AbstractSettings> {
		public void call(T settings);
	}

	/**
	 * @return If all required settings are set and valid.
	 */
	public abstract boolean isValid();
}
