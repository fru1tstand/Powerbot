package me.fru1t.rsbot.framework.generics;

/**
 * An abstract Settings file containing common settings
 */
public abstract class GenericSettings {
	/**
	 * @return If all required settings are set and valid.
	 */
	public abstract boolean isValid();
	
	public abstract void replace(GenericSettings other);
}
