package me.fru1t.rsbot.safecracker.strategies.logic;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.common.util.Random;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.slick.util.Provider;

public class DepositInventoryButton {
	private static final int ENABLED_WHEN_UNNEEDED_PROBABILITY = 25;
	private static final int ENABLED_WHEN_NEEDED_PROBABILITY = 95;

	private final Provider<Settings> settingsProvider;
	private boolean isEnabled;
	private boolean hasSet;

	@Inject
	public DepositInventoryButton(Provider<Settings> settingsProvider) {
		this.settingsProvider = settingsProvider;
		this.hasSet = false;
	}

	/**
	 * @return If the user should click the deposit inventory button in the bank interface.
	 */
	public boolean shouldClick() {
		// TODO(v2): Improve algorithm
		setEnabledIfNotSet();
		return isEnabled;
	}

	private void setEnabledIfNotSet() {
		if (!hasSet) {
			isEnabled = (settingsProvider.get()
					.isBankStyle(Settings.BankStyle.PRESET_1, Settings.BankStyle.PRESET_2))
					? Random.roll(ENABLED_WHEN_UNNEEDED_PROBABILITY)
					: Random.roll(ENABLED_WHEN_NEEDED_PROBABILITY);
		}
	}
}
