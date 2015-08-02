package me.fru1t.rsbot.framework;

import me.fru1t.rsbot.framework.generics.GenericSettings;

/**
 * Provides Javascript style callback that accepts a Settings parameter.
 * 
 * @param <T> The Settings to accept
 */
public abstract class SettingsCallback<T extends GenericSettings> {
	public abstract void call(T settings);
}
