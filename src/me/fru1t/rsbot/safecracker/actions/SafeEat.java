package me.fru1t.rsbot.safecracker.actions;

import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Item;

import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.framework.Action;
import me.fru1t.rsbot.safecracker.Persona;
import me.fru1t.rsbot.safecracker.Settings;

public class SafeEat extends Action<ClientContext, RoguesDenSafeCracker, Settings, Persona> {
	public SafeEat(RoguesDenSafeCracker script) {
		super(script);
	}

	@Override
	public boolean run() {
		// Out of food?
		if (script.ctx.backpack.select().id(script.settings.getCurrentFood().id).isEmpty()) {
			script.updateState(RoguesDenSafeCracker.State.BANK_WALK);
			return true;
		}
		
		int eatCount = script.persona.foodToConsume();
		while (!script.ctx.backpack.select().id(script.settings.getCurrentFood().id).isEmpty()
				&& eatCount > 0) {
			Item food = script.ctx.backpack.poll();
			if (!food.inViewport()) {
				script.ctx.backpack.scroll(food);
			}
			
		}
		return true;
	}

}
