package me.fru1t.rsbot.common.framework;

public abstract class AbstractStartupForm<T extends AbstractSettings> {
	protected final AbstractSettings.Callback<T> callback;

	protected AbstractStartupForm(AbstractSettings.Callback<T> callback) {
		this.callback = callback;
	}
}
