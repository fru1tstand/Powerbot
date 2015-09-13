package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.slick.util.Provider;
import org.powerbot.script.rt6.ClientContext;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.script.rt6.Backpack;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.rsbot.safecracker.strategies.logic.FoodLogic;

public class SafeEat implements Strategy<RoguesDenSafeCracker.State> {
	private final Provider<ClientContext> ctxProvider;
	private final Provider<Settings> settingsProvider;
	private final FoodLogic foodLogic;
	private final Backpack backpackUtil;

	@Inject
	public SafeEat(
			Provider<ClientContext> ctx,
			Provider<Settings> settings,
			@Singleton Backpack backpackUtil,
			FoodLogic foodLogic) {
		this.ctxProvider = ctx;
		this.settingsProvider = settings;
		this.foodLogic = foodLogic;
		this.backpackUtil = backpackUtil;
	}

	@Override
	public RoguesDenSafeCracker.State run() {
		// Out of food?
		if (ctxProvider.get().backpack.select().id(settingsProvider.get().getFood().id).isEmpty()) {
			return RoguesDenSafeCracker.State.WALK_TO_BANK;
		}

		return backpackUtil.clickMultipleItemsWithSingleId(
						settingsProvider.get().getFood().id, foodLogic.numberToEat())
				? RoguesDenSafeCracker.State.SAFE_CRACK : null;
	}

}
