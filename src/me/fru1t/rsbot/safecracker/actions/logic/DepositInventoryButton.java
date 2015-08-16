package me.fru1t.rsbot.safecracker.actions.logic;

import me.fru1t.annotations.Singleton;
import me.fru1t.rsbot.common.util.Random;
import me.fru1t.rsbot.safecracker.Settings;

public class DepositInventoryButton {
	private static final int ENABLED_WHEN_UNNEEDED_PROBABILITY = 25;
	private static final int ENABLED_WHEN_NEEDED_PROBABILITY = 95;
	private static final int RE_INTERACT_PROBABILITY = 33;
	
	private final boolean isEnabled;
	
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
	
	/**
	 * @return If, when spam clicking, the user should move the mouse before clicking again.
	 */
	public boolean shouldReInteract() {
		return Random.roll(RE_INTERACT_PROBABILITY);
	}
}
