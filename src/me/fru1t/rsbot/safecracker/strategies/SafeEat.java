package me.fru1t.rsbot.safecracker.strategies;

import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Item;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.framework.components.RunState;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.rsbot.safecracker.strategies.logic.FoodLogic;
import me.fru1t.rsbot.safecracker.strategies.logic.InteractSpamClickProvider;

public class SafeEat implements Strategy<RoguesDenSafeCracker.State> {
	private final ClientContext ctx;
	private final Settings settings;
	private final FoodLogic foodLogic;

	@Inject
	public SafeEat(
			@Singleton ClientContext ctx,
			@Singleton RunState<RoguesDenSafeCracker.State> state,
			@Singleton Settings settings,
			@Singleton InteractSpamClickProvider spamClickProvider,
			FoodLogic foodLogic) {
		this.ctx = ctx;
		this.settings = settings;
		this.foodLogic = foodLogic;
	}

	@Override
	public RoguesDenSafeCracker.State run() {
		// Out of food?
		if (ctx.backpack.select().id(settings.getFood().id).isEmpty()) {
			return RoguesDenSafeCracker.State.BANK_WALK;
		}

		// TODO: Add spam click
		int eatCount = foodLogic.numberToEat();
		while (!ctx.backpack.select().id(settings.getFood().id).isEmpty()
				&& eatCount-- > 0) {
			Item food = ctx.backpack.poll();
			if (!food.inViewport()) {
				ctx.backpack.scroll(food);
			}
			food.click();
		}
		return RoguesDenSafeCracker.State.SAFE_CRACK;
	}

}
