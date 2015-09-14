package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.RoguesDenSafeCracker.State;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.framework.components.Status;
import me.fru1t.rsbot.common.script.rt6.Bank;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.rsbot.safecracker.Settings.BankStyle;
import me.fru1t.slick.util.Provider;

public class WithdrawFromBankWithPresets implements Strategy<RoguesDenSafeCracker.State> {
	private final Bank bankUtil;
	private final Provider<Settings> settingsProvider;
	private final Provider<Status> statusProvider;

	@Inject
	public WithdrawFromBankWithPresets(
			Provider<Settings> settingsProvider,
			Provider<Status> statusProvider,
			@Singleton Bank bankUtil) {
		this.bankUtil = bankUtil;
		this.settingsProvider = settingsProvider;
		this.statusProvider = statusProvider;
	}

	@Override
	public State run() {
		statusProvider.get().update("Withdrawing using preset buttons");
		// TODO(v1 cleanup): Implement convenience methods to clear up this mess

		if (settingsProvider.get().isBankStyle(BankStyle.PRESET_1)) {
			if (!bankUtil.clickPreset1()) {
				return State.OPEN_BANK;
			}
		} else {
			if (!bankUtil.clickPreset2()) {
				return State.OPEN_BANK;
			}
		}

		return State.WALK_TO_SAFE;
	}


}
