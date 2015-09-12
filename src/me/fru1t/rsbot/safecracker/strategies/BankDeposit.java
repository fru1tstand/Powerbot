package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.slick.util.Provider;
import org.powerbot.script.rt6.ClientContext;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.RoguesDenSafeCracker.State;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.script.rt6.Bank;
import me.fru1t.rsbot.safecracker.strategies.logic.DepositInventoryButton;

/**
 * Does the bank depositing.
 */
public class BankDeposit implements Strategy<RoguesDenSafeCracker.State> {
	private final Provider<ClientContext> ctxProvider;
	private final Bank bank;
	private final DepositInventoryButton depositInventoryButton;

	@Inject
	public BankDeposit(
			Provider<ClientContext> ctxProvider,
			@Singleton Bank bank,
			DepositInventoryButton depositInventoryButton) {
		this.ctxProvider = ctxProvider;
		this.bank = bank;
		this.depositInventoryButton = depositInventoryButton;
	}

	@Override
	public State run() {
		// TODO(v2): More fail-safe catch states
		if (ctxProvider.get().backpack.isEmpty()) {
			return State.BANK_WITHDRAW;
		}

		if (depositInventoryButton.shouldClick()) {
			if (!bank.clickDepositInventory()) {
				return State.BANK_OPEN;
			}
		}

		return State.BANK_WITHDRAW;
	}
}
