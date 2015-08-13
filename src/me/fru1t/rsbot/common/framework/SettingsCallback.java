package me.fru1t.rsbot.common.framework;

/**
 * Provides Javascript style callback that accepts a Settings parameter.
 * 
 * @param <T> The Settings to accept
 */
public abstract class SettingsCallback<T extends AbstractSettings> {
	public abstract void call(T settings);
}
