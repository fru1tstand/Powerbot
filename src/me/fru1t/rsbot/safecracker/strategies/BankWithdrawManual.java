package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.rsbot.common.framework.components.Status;
import me.fru1t.slick.util.Provider;
import org.powerbot.script.rt6.ClientContext;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.RoguesDenSafeCracker.State;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.script.rt6.Bank;
import me.fru1t.rsbot.safecracker.Settings;

public class BankWithdrawManual implements Strategy<RoguesDenSafeCracker.State> {
	private final Provider<ClientContext> ctxProvider;
	private final Provider<Settings> settingsProvider;
	private final Provider<Status> statusProvider;
	private final Bank bankUtil;

	@Inject
	public BankWithdrawManual(
			Provider<ClientContext> ctxProvider,
			Provider<Settings> settingsProvider,
			Provider<Status> statusProvider,
			@Singleton Bank bankUtil) {
		this.ctxProvider = ctxProvider;
		this.settingsProvider = settingsProvider;
		this.statusProvider = statusProvider;
		this.bankUtil = bankUtil;
	}

	@Override
	public State run() {
		statusProvider.get().update("Withdrawing items manually");

		if (!bankUtil.waitForBankToOpen()) {
			statusProvider.get().update("The bank never opened");
			return State.BANK_OPEN;
		}

		if (!ctxProvider.get().bank.withdraw(
				settingsProvider.get().getFood().id,
				settingsProvider.get().getFoodQuantity())) {
			return null;
		}

		return State.SAFE_WALK;
	}
}
