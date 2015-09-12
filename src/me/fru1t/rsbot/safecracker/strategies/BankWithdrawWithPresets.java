package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.RoguesDenSafeCracker.State;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.script.rt6.Bank;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.rsbot.safecracker.Settings.BankStyle;
import me.fru1t.slick.util.Provider;

public class BankWithdrawWithPresets implements Strategy<RoguesDenSafeCracker.State> {
	private final Bank bankUtil;
	private final Provider<Settings> settingsProvider;

	@Inject
	public BankWithdrawWithPresets(
			@Singleton Bank bankUtil,
			Provider<Settings> settingsProvider) {
		this.bankUtil = bankUtil;
		this.settingsProvider = settingsProvider;
	}

	@Override
	public State run() {
		// TODO(v2): Are there missing prerequisites that may lurk?

		// TODO(v1 cleanup): Implement convenience methods to clear up this mess
		if (settingsProvider.get().isBankStyle(BankStyle.PRESET_1)) {
			if (!bankUtil.clickPreset1()) {
				return State.BANK_OPEN;
			}
		} else {
			if (!bankUtil.clickPreset2()) {
				return State.BANK_OPEN;
			}
		}

		return State.SAFE_WALK;
	}


}
