package me.fru1t.rsbot.safecracker.strategies.logic;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.common.framework.util.Random;
import me.fru1t.rsbot.safecracker.Settings;

public class DepositInventoryButton {
	private static final int ENABLED_WHEN_UNNEEDED_PROBABILITY = 25;
	private static final int ENABLED_WHEN_NEEDED_PROBABILITY = 95;
	
	private final boolean isEnabled;
	
	@Inject
	public DepositInventoryButton(@Singleton Settings settings) {
		isEnabled = (settings.isBankStyle(Settings.BankStyle.PRESET_1, Settings.BankStyle.PRESET_2))
				? Random.roll(ENABLED_WHEN_UNNEEDED_PROBABILITY)
				: Random.roll(ENABLED_WHEN_NEEDED_PROBABILITY);
	}
	
	/**
	 * @return If the user should click the deposit inventory button in the bank interface.
	 */
	public boolean shouldClick() {
		if (isEnabled) {
			return true;
		}
		return false;
	}
}
