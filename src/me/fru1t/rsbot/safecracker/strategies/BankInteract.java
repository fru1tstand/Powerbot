package me.fru1t.rsbot.safecracker.strategies;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.powerbot.script.Condition;
import org.powerbot.script.rt6.Bank;
import org.powerbot.script.rt6.ClientContext;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.rt6.InteractUtil;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.rsbot.safecracker.strategies.logic.DepositInventoryButton;

public class BankInteract implements Strategy<RoguesDenSafeCracker.State> {
	private final ClientContext ctx;
	private final Settings settings;
	private final InteractUtil spamClickUtil;
	private final DepositInventoryButton depositInventoryButton;

	@Inject
	public BankInteract(
			@Singleton ClientContext ctx,
			@Singleton Settings settings,
			@Singleton InteractUtil spamClickUtil,
			DepositInventoryButton depositInventoryButton) {
		this.ctx = ctx;
		this.settings = settings;
		this.spamClickUtil = spamClickUtil;
		this.depositInventoryButton = depositInventoryButton;
	}

	@Override
	public RoguesDenSafeCracker.State run() {
		// Deposit
		if (depositInventoryButton.shouldClick()) {
			// Deposit using dep inv button
			spamClickUtil.click(ctx.widgets
					.component(Bank.WIDGET, Bank.COMPONENT_BUTTON_DEPOSIT_INVENTORY));
		} else {
			// Deposit manually -- This method shoud most likely go away.
			ctx.backpack.select();
			Set<Integer> backpackSet = new HashSet<>();
			while (!ctx.backpack.isEmpty()) {
				backpackSet.add(ctx.backpack.poll().id());
			}
		}

		// TODO: Possibly not wait for this?
		if (Condition.wait(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return ctx.backpack.select().isEmpty()
							|| settings.isBankStyle(
									Settings.BankStyle.PRESET_1,
									Settings.BankStyle.PRESET_2);
				}
			}, 150)) {
			return null;
		}

		// TODO: Extract to another action
		// Withdraw
		if (settings.isBankStyle(Settings.BankStyle.PRESET_1, Settings.BankStyle.PRESET_2)) {
			spamClickUtil.click(ctx.widgets.component(Bank.WIDGET,
					settings.isBankStyle(Settings.BankStyle.PRESET_1)
					? Bank.COMPONENT_LOADOUT_1 : Bank.COMPONENT_LOADOUT_2));
		} else {
			// TODO: Add possbility of waiting for food to get into inventory
			ctx.bank.withdraw(settings.getFood().id, settings.getFoodQuantity());
		}

		return RoguesDenSafeCracker.State.SAFE_WALK;
	}
}
