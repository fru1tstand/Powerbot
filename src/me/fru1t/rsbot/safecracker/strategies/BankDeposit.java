package me.fru1t.rsbot.safecracker.strategies;

import org.powerbot.script.rt6.ClientContext;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.RoguesDenSafeCracker.State;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.script.rt6.BankUtil;
import me.fru1t.rsbot.safecracker.strategies.logic.DepositInventoryButton;

/**
 * Does the bank depositing.
 */
public class BankDeposit implements Strategy<RoguesDenSafeCracker.State> {
	private final ClientContext ctx;
	private final BankUtil bankUtil;
	private final DepositInventoryButton depositInventoryButton;

	@Inject
	public BankDeposit(
			@Singleton ClientContext ctx,
			@Singleton BankUtil bankUtil,
			DepositInventoryButton depositInventoryButton) {
		this.ctx = ctx;
		this.bankUtil = bankUtil;
		this.depositInventoryButton = depositInventoryButton;
	}

	@Override
	public State run() {
		// TODO(v2): More fail-safe catch states
		if (ctx.backpack.isEmpty()) {
			return State.BANK_WITHDRAW;
		}

		if (depositInventoryButton.shouldClick()) {
			if (!bankUtil.clickDepositInventory()) {
				return State.BANK_OPEN;
			}
		}

		return State.BANK_WITHDRAW;
	}
}
