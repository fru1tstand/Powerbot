package me.fru1t.rsbot.safecracker.actions.modules;

import org.powerbot.script.rt6.ClientContext;

import me.fru1t.annotations.Inject;
import me.fru1t.rsbot.common.util.Random;
import me.fru1t.rsbot.safecracker.Settings;

/**
 * There are people who eat one food, those who eat multiple food, and those who eat until the HP
 * is full.
 * 
 * <p>TODO: Add overeat
 */
public class FoodLogic {
	// Each eat stype is equally as likely to be chosen.
	private enum EatStyle { ONE, MULTIPLE, FULL }
	
	private final ClientContext ctx;
	private final Settings settings;
	private final EatStyle eatStyle;
	
	@Inject
	public FoodLogic(ClientContext ctx, Settings settings) {
		this.ctx = ctx;
		this.settings = settings;
		
		int rnd = Random.nextInt(0, 100);
		if (rnd < 33) {
			eatStyle = EatStyle.ONE;
		} else if (rnd < 66) {
			eatStyle = EatStyle.MULTIPLE;
		} else {
			eatStyle = EatStyle.FULL;
		}
	}
	
	/**
	 * @return The number of food items to eat.
	 */
	public int numberToEat() {
		switch(eatStyle) {
		case FULL:
			return getPossibleFoodConsumptionAmount();
		case MULTIPLE:
			// TODO: Ehh...?
			return Random.nextInt(1, getPossibleFoodConsumptionAmount() + 1);
		case ONE:
		default:
			return 1;
		}
	}
	
	/**
	 * @return The number of food one can eat without overhealing.
	 */
	private int getPossibleFoodConsumptionAmount() {
		// TODO: Is this the correct way to get both EOC and legacy hp?
		return (ctx.combatBar.maximumHealth() - ctx.combatBar.health())
				/ settings.getCurrentFood().healAmount;
	}
}
