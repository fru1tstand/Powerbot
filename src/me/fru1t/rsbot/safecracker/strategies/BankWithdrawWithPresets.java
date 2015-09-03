package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.RoguesDenSafeCracker.State;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.script.rt6.BankUtil;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.rsbot.safecracker.Settings.BankStyle;

public class BankWithdrawWithPresets implements Strategy<RoguesDenSafeCracker.State> {
	private final BankUtil bankUtil;
	private final Settings settings;
	
	@Inject
	public BankWithdrawWithPresets(
			@Singleton BankUtil bankUtil,
			@Singleton Settings settings) {
		this.bankUtil = bankUtil;
		this.settings = settings;
	}
	
	@Override
	public State run() {
		// TODO(v2): Are there missing prerequisites that may lurk?
		
		// TODO(v1 cleanup): Implement convenience methods to clear up this mess
		if (settings.isBankStyle(BankStyle.PRESET_1)) {
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
