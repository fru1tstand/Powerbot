package me.fru1t.rsbot.safecracker.strategies;

import org.powerbot.script.rt6.ClientContext;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.RoguesDenSafeCracker.State;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.script.rt6.Bank;
import me.fru1t.rsbot.safecracker.Settings;

public class BankWithdrawManual implements Strategy<RoguesDenSafeCracker.State> {
	private final ClientContext ctx;
	private final Bank bankUtil;
	private final Settings settings;

	@Inject
	public BankWithdrawManual(
			@Singleton ClientContext ctx,
			@Singleton Bank bankUtil,
			@Singleton Settings settings) {
		this.ctx = ctx;
		this.bankUtil = bankUtil;
		this.settings = settings;
	}

	@Override
	public State run() {
		if (!bankUtil.waitForBankToOpen()) {
			return State.BANK_OPEN;
		}

		if (!ctx.bank.withdraw(settings.getFood().id, settings.getFoodQuantity())) {
			return null;
		}

		return State.SAFE_WALK;
	}
}
