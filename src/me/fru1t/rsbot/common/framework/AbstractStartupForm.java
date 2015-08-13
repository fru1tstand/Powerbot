package me.fru1t.rsbot.common.framework;

public abstract class AbstractStartupForm<T extends AbstractSettings> {
	protected final SettingsCallback<T> callback;
	
	protected AbstractStartupForm(SettingsCallback<T> callback) {
		this.callback = callback;
	}
}
