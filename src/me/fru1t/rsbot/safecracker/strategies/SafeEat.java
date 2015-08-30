package me.fru1t.rsbot.safecracker.strategies;

import org.powerbot.script.rt6.ClientContext;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.script.rt6.BackpackUtil;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.rsbot.safecracker.strategies.logic.FoodLogic;

public class SafeEat implements Strategy<RoguesDenSafeCracker.State> {
	private final ClientContext ctx;
	private final Settings settings;
	private final FoodLogic foodLogic;
	private final BackpackUtil backpackUtil;

	@Inject
	public SafeEat(
			@Singleton ClientContext ctx,
			@Singleton Settings settings,
			@Singleton BackpackUtil backpackUtil,
			FoodLogic foodLogic) {
		this.ctx = ctx;
		this.settings = settings;
		this.foodLogic = foodLogic;
		this.backpackUtil = backpackUtil;
	}

	@Override
	public RoguesDenSafeCracker.State run() {
		// Out of food?
		if (ctx.backpack.select().id(settings.getFood().id).isEmpty()) {
			return RoguesDenSafeCracker.State.BANK_WALK;
		}

		return backpackUtil
				.clickMultipleItemsWithSingleId(settings.getFood().id, foodLogic.numberToEat())
				? RoguesDenSafeCracker.State.SAFE_CRACK : null;
	}

}
