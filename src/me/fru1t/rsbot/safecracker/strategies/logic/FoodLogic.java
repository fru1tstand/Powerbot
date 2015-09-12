package me.fru1t.rsbot.safecracker.strategies.logic;

import me.fru1t.slick.util.Provider;
import org.powerbot.script.rt6.ClientContext;

import me.fru1t.common.annotations.Inject;
import me.fru1t.rsbot.common.util.Random;
import me.fru1t.rsbot.safecracker.Settings;

/**
 * There are people who eat one food, those who eat multiple food, and those who eat until the HP
 * is full.
 *
 * <p>TODO(v2): Add overeat
 */
public class FoodLogic {
	// Each eat style is equally as likely to be chosen.
	// TODO(v1): Use probability enum
	private enum EatStyle { ONE, MULTIPLE, FULL }

	private final Provider<ClientContext> ctxProvider;
	private final Provider<Settings> settingsProvider;
	private final EatStyle eatStyle;

	@Inject
	public FoodLogic(
			Provider<ClientContext> contextProvider,
			Provider<Settings> settingsProvider) {
		this.ctxProvider = contextProvider;
		this.settingsProvider = settingsProvider;

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
			// TODO(v1): Improve algorithm
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
		// TODO(v1): Is this the correct way to get both EOC and legacy hp?
		return (ctxProvider.get().combatBar.maximumHealth() - ctxProvider.get().combatBar.health())
				/ settingsProvider.get().getFood().healAmount;
	}
}
