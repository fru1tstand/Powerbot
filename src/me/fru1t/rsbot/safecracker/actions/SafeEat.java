package me.fru1t.rsbot.safecracker.actions;

import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Item;

import me.fru1t.annotations.Inject;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.framework.Action;
import me.fru1t.rsbot.framework.components.RunState;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.rsbot.safecracker.actions.safeeat.FoodLogic;

public class SafeEat implements Action {
	private final ClientContext ctx;
	private final RunState<RoguesDenSafeCracker.State> state;
	private final Settings settings;
	private final FoodLogic foodLogic;
	
	@Inject
	public SafeEat(
			ClientContext ctx,
			RunState<RoguesDenSafeCracker.State> state,
			Settings settings,
			FoodLogic foodLogic) {
		this.ctx = ctx;
		this.state = state;
		this.settings = settings;
		this.foodLogic = foodLogic;
	}

	@Override
	public boolean run() {
		// Out of food?
		if (ctx.backpack.select().id(settings.getCurrentFood().id).isEmpty()) {
			state.update(RoguesDenSafeCracker.State.BANK_WALK);
			return true;
		}
		
		int eatCount = foodLogic.numberToEat();
		while (!ctx.backpack.select().id(settings.getCurrentFood().id).isEmpty()
				&& eatCount-- > 0) {
			Item food = ctx.backpack.poll();
			if (!food.inViewport()) {
				ctx.backpack.scroll(food);
			}
			food.click();
		}
		return true;
	}

}
