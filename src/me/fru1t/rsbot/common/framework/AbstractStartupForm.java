package me.fru1t.rsbot.common.framework;

import me.fru1t.common.annotations.Provides;
import me.fru1t.slick.Module;
import me.fru1t.slick.util.Provider;
import me.fru1t.slick.util.Providers;

public abstract class AbstractStartupForm<T extends AbstractSettings> {
	public static class StartupFormModule implements Module {
		@Provides
		public Class<AbstractSettings.Callback> provideSettingsCallback() {
			return AbstractSettings.Callback.class;
		}
	}

	public Provider<AbstractSettings.Callback<T>> callbackProvider;

	/**
	 * Available for testing
	 */
	protected AbstractStartupForm() {
		this(Providers.of((AbstractSettings.Callback<T>) new AbstractSettings.Callback<T>() {
			@Override public void call(T settings) { /* Do nothing */ }
		}));
	}

	protected AbstractStartupForm(Provider<AbstractSettings.Callback<T>> callbackProvider) {
		this.callbackProvider = callbackProvider;
	}

	protected void setSettings(T settings) {
		callbackProvider.get().call(settings);
	}
}
