package me.fru1t.rsbot.common.framework.components;

import me.fru1t.rsbot.common.framework.AbstractSettings;

/**
 * Provides Javascript style callback that accepts a Settings parameter.
 * 
 * @param <T> The Settings to accept
 */
public abstract class SettingsCallback<T extends AbstractSettings> {
	public abstract void call(T settings);
}
