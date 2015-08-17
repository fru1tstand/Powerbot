package me.fru1t.rsbot.safecracker.actions;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.powerbot.script.Condition;
import org.powerbot.script.rt6.ClientContext;

import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.actions.logic.SpamClick;
import me.fru1t.rsbot.common.framework.Action;
import me.fru1t.rsbot.common.framework.components.RunState;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.rsbot.safecracker.actions.logic.DepositInventoryButton;
import me.fru1t.rsbot.safecracker.actions.logic.InteractSpamClickProvider;

public class BankInteract implements Action {
	private final ClientContext ctx;
	private final RunState<RoguesDenSafeCracker.State> state;
	private final Settings settings;
	private final SpamClick spamClick;
	private final DepositInventoryButton depositInventoryButton;
	
	public BankInteract(
			ClientContext ctx,
			RunState<RoguesDenSafeCracker.State> state,
			Settings settings,
			InteractSpamClickProvider spamClickProvider,
			DepositInventoryButton depositInventoryButton) {
		this.ctx = ctx;
		this.state = state;
		this.settings = settings;
		this.spamClick = spamClickProvider.get();
		this.depositInventoryButton = depositInventoryButton;
	}
	
	@Override
	public boolean run() {
		// Deposit
		if (depositInventoryButton.shouldClick()) {
			// Deposit using dep inv button
			spamClick.interact(new SpamClick.Action() {
				@Override
				public void interact() {
					ctx.bank.depositInventory();
				}
			});
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
			return false;
		}
		
		// TODO: Extract to another action
		// Withdraw
		if (settings.isBankStyle(Settings.BankStyle.PRESET_1, Settings.BankStyle.PRESET_2)) {
			spamClick.interact(new SpamClick.Action() {
				@Override
				public void interact() {
					if (settings.isBankStyle(Settings.BankStyle.PRESET_1)) {
						ctx.bank.presetGear1();
					} else {
						ctx.bank.presetGear2();
					}
				}
			});
		} else {
			// TODO: Add possbility of waiting for food to get into inventory
			ctx.bank.withdraw(settings.getFood().id, settings.getFoodQuantity());
		}
		
		state.update(RoguesDenSafeCracker.State.SAFE_WALK);
		return true;
	}

}
