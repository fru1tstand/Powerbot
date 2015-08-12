package me.fru1t.rsbot.common.framework;

/**
 * An abstract Settings file containing common settings
 */
public abstract class AbstractSettings {
	/**
	 * @return If all required settings are set and valid.
	 */
	public abstract boolean isValid();
	
	public abstract void replace(AbstractSettings other);
}
