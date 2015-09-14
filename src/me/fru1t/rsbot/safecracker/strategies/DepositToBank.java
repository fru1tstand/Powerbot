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
import me.fru1t.rsbot.safecracker.strategies.logic.DepositInventoryButton;

/**
 * Does the bank depositing.
 */
public class DepositToBank implements Strategy<RoguesDenSafeCracker.State> {
	private final Provider<ClientContext> ctxProvider;
	private final Provider<Status> statusProvider;
	private final Bank bank;
	private final DepositInventoryButton depositInventoryButton;

	@Inject
	public DepositToBank(
			Provider<ClientContext> ctxProvider,
			Provider<Status> statusProvider,
			@Singleton Bank bank,
			DepositInventoryButton depositInventoryButton) {
		this.ctxProvider = ctxProvider;
		this.statusProvider = statusProvider;
		this.bank = bank;
		this.depositInventoryButton = depositInventoryButton;
	}

	@Override
	public State run() {
		// Note: Bank may not be open at this point
		statusProvider.get().update("Depositing items from the backpack.");

		// Check if inventory is already empty
		if (ctxProvider.get().backpack.isEmpty()) {
			statusProvider.get().update("The backpack is already empty.");
			return State.WITHDRAW;
		}

		// Deposit
		if (depositInventoryButton.shouldClick() && !bank.depositInventory()) {
			return State.OPEN_BANK;
		}

		return State.WITHDRAW;
	}
}
