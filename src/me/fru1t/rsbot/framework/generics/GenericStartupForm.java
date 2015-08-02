package me.fru1t.rsbot.framework.generics;

import me.fru1t.rsbot.framework.SettingsCallback;

public abstract class GenericStartupForm<T extends GenericSettings> {
	protected final SettingsCallback<T> callback;
	
	protected GenericStartupForm(SettingsCallback<T> callback) {
		this.callback = callback;
	}
}
