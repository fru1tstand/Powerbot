package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.framework.components.Status;
import me.fru1t.rsbot.common.script.rt6.Backpack;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.rsbot.safecracker.strategies.logic.FoodLogic;
import me.fru1t.slick.util.Provider;

public class Eat implements Strategy<RoguesDenSafeCracker.State> {
	private final Provider<Settings> settingsProvider;
	private final Provider<Status> statusProvider;
	private final FoodLogic foodLogic;
	private final Backpack backpack;

	@Inject
	public Eat(
			Provider<Settings> settingsProvider,
			Provider<Status> statusProvider,
			@Singleton Backpack backpack,
			FoodLogic foodLogic) {
		this.statusProvider = statusProvider;
		this.settingsProvider = settingsProvider;
		this.foodLogic = foodLogic;
		this.backpack = backpack;
	}

	@Override
	public RoguesDenSafeCracker.State run() {
		statusProvider.get().update("Eating food");

		if (!backpack.contains(settingsProvider.get().getFood().id)) {
			statusProvider.get().update("No food left in inventory");
			return RoguesDenSafeCracker.State.WALK_TO_BANK;
		}

		return backpack.clickNOf(settingsProvider.get().getFood().id, foodLogic.numberToEat())
				? RoguesDenSafeCracker.State.CRACK_SAFE : null;
	}

}
