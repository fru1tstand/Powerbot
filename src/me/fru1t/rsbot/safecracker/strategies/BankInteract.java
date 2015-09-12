package me.fru1t.rsbot.safecracker.strategies;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import me.fru1t.slick.util.Provider;
import org.powerbot.script.Condition;
import org.powerbot.script.rt6.ClientContext;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.RoguesDenSafeCracker.State;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.script.rt6.Bank;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.rsbot.safecracker.strategies.logic.DepositInventoryButton;

public class BankInteract implements Strategy<RoguesDenSafeCracker.State> {
	private final Provider<ClientContext> ctxProvider;
	private final Provider<Settings> settingsProvider;
	private final Bank bankUtil;
	private final DepositInventoryButton depositInventoryButton;

	@Inject
	public BankInteract(
			Provider<ClientContext> ctxProvider,
			Provider<Settings> settingsProvider,
			@Singleton Bank bank,
			DepositInventoryButton depositInventoryButton) {
		this.ctxProvider = ctxProvider;
		this.settingsProvider = settingsProvider;
		this.bankUtil = bank;
		this.depositInventoryButton = depositInventoryButton;
	}

	@Override
	public State run() {
		// Deposit. Note: We don't know if the bank is open at this stage.
		if (depositInventoryButton.shouldClick()) {
			// Deposit using button
			if (!bankUtil.clickDepositInventory()) {
				return State.BANK_OPEN;
			}
		} else {
			// Deposit manually
			// TODO(v1 cleanup): Is this method necessary?
			Set<Integer> backpackSet = new HashSet<Integer>();
//			ctx.backpack.select().addt;
			// TODO(v1): Complete
			while (!ctxProvider.get().backpack.isEmpty()) {
				backpackSet.add(ctxProvider.get().backpack.poll().id());
			}
		}

		// TODO: Possibly not wait for this?
		if (Condition.wait(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return ctxProvider.get().backpack.select().isEmpty()
							|| settingsProvider.get().isBankStyle(
							Settings.BankStyle.PRESET_1,
							Settings.BankStyle.PRESET_2);
				}
			}, 150)) {
			return null;
		}

		// TODO: Extract to another action
		// Withdraw
		if (settingsProvider.get()
				.isBankStyle(Settings.BankStyle.PRESET_1, Settings.BankStyle.PRESET_2)) {
			if (!(settingsProvider.get().isBankStyle(Settings.BankStyle.PRESET_1)
					? bankUtil.clickPreset1() : bankUtil.clickPreset2())) {
				return null;
			}
		} else {
			// TODO: Add possbility of waiting for food to get into inventory
			ctxProvider.get().bank.withdraw(settingsProvider.get().getFood().id,
					settingsProvider.get().getFoodQuantity());
		}

		return RoguesDenSafeCracker.State.SAFE_WALK;
	}
}
