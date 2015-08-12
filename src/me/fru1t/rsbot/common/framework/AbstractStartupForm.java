package me.fru1t.rsbot.common.framework;

import me.fru1t.rsbot.common.framework.components.SettingsCallback;

public abstract class AbstractStartupForm<T extends AbstractSettings> {
	protected final SettingsCallback<T> callback;
	
	protected AbstractStartupForm(SettingsCallback<T> callback) {
		this.callback = callback;
	}
}
