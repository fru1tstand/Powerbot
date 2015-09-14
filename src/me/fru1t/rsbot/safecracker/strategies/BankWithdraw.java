package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.common.annotations.Inject;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.slick.util.Provider;

/**
 * Chooses which bank style to use
 */
public class BankWithdraw implements Strategy<RoguesDenSafeCracker.State> {
	private final Provider<Settings> settingsProvider;

	@Inject
	public BankWithdraw(Provider<Settings> settingsProvider) {
		this.settingsProvider = settingsProvider;
	}

	@Override
	public RoguesDenSafeCracker.State run() {
		return (settingsProvider.get().isBankStyleUsingPresets())
				? RoguesDenSafeCracker.State.BANK_WITHDRAW_WITH_PRESETS
				: RoguesDenSafeCracker.State.BANK_WITHDRAW_MANUALLY;
	}
}
