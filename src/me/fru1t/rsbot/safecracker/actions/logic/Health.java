package me.fru1t.rsbot.safecracker.actions.logic;

import org.powerbot.script.rt6.ClientContext;

import me.fru1t.annotations.Inject;
import me.fru1t.annotations.Singleton;
import me.fru1t.rsbot.common.util.Random;
import me.fru1t.rsbot.safecracker.Settings;

/**
 * Eating habits may vary from person to person. Several of these include eating when hp
 * reaches as low as possible without dying then fully healing, eating whenever health drops
 * below what the food item heals, eating when hp falls below a specific threshold, etc.
 * 
 * <p>TODO: Add over-eating possibility
 * <p>TODO: Add norm distribution for constant hp eating
 * <p>TODO: Add dependency on persona's focus
 */
public class Health {
	/**
	 * Defines the different styles of eating
	 */
	private enum EatMethod { LOWEST_POSSIBLE, FOOD_ORIENTED, RANDOM }
	
	// General
	private static final int HEALTH_ABS_MIN = 110;
	private static final int IS_CONSTANT_PROBABILITY = 25;
	
	// Constants about the LOWEST_POSSIBLE eat method
	private static final int LP_PROBABILITY = 10;
	private static final int LP_MIN = 120;
	private static final int LP_MAX = 400;

	// Constants about the FOOD_ORIENTED eat method
	private static final int FO_PROBABILITY = 60;
	private static final double FO_MAX_FOOD_MULTIPLIER = 2.5;
	private static final double FO_MIN_VARIANCE = 0.5;
	private static final double FO_MAX_VARIANCE = 5;
	
	// Constants about the RANDOM eat method
	// R_PROBABILITY = 100 - LP_PROBABILITY - FO_PROBABILITY
	private static final int R_MIN = 1000;
	
	
	private final ClientContext ctx;
	private final Settings settings;
	
	private final EatMethod eatMethod;
	private final boolean isConstant;
	private double foodOrientedMeanMultiplier;
	private double foodOrientedVariance;
	private int eatAt;
	
	@Inject
	public Health(@Singleton ClientContext ctx, @Singleton Settings settings) {
		this.ctx = ctx;
		this.settings = settings;
		eatAt = 0;
		
		// Eat method
		int rnd = Random.nextInt(0, 100);
		if (rnd < LP_PROBABILITY) {
			eatMethod = EatMethod.LOWEST_POSSIBLE;
		} else if (rnd < LP_PROBABILITY + FO_PROBABILITY) {
			eatMethod = EatMethod.FOOD_ORIENTED;
			foodOrientedMeanMultiplier = Random.nextDouble(1, FO_MAX_FOOD_MULTIPLIER);
			foodOrientedVariance = Random.nextDouble(FO_MIN_VARIANCE, FO_MAX_VARIANCE);
		} else {
			eatMethod = EatMethod.RANDOM;
		}
		
		// Is a constant hp eater
		isConstant = Random.roll(IS_CONSTANT_PROBABILITY);
		newEatAt();
	}
	
	/**
	 * @return The health to eat at
	 */
	public int eatAt() {
		return eatAt;
	}

	/**
	 * Generate a new health to eat at if applicable
	 */
	public void newEatAt() {
		if (isConstant && eatAt != 0) {
			return;
		}
		
		// Force lowest_possible if current food outheals the healing logic for the given eat method
		EatMethod eatMethod = this.eatMethod;
		switch (eatMethod) {
		case LOWEST_POSSIBLE:
			// Do nothing
			break;
		case RANDOM:
			if (settings.getFood().healAmount + R_MIN + HEALTH_ABS_MIN
					>= ctx.combatBar.maximumHealth()) {
				eatMethod = EatMethod.LOWEST_POSSIBLE;
			}
			break;
		case FOOD_ORIENTED:
		default:
			if (settings.getFood().healAmount * FO_MAX_FOOD_MULTIPLIER + HEALTH_ABS_MIN
					>= ctx.combatBar.maximumHealth()) {
				eatMethod = EatMethod.LOWEST_POSSIBLE;
			}
			break;
		}
		
		// TODO: Find if this value is inaccurate due to EOC vs Legacy settings
		int maxHealHealthToEatAt =
				ctx.combatBar.maximumHealth() - settings.getFood().healAmount;
		
		switch (eatMethod) {
		// Don't heal until very low hp
		case LOWEST_POSSIBLE:
			eatAt = Random.nextInt(LP_MIN, LP_MAX);
			break;

		// Heal at a random health
		case RANDOM:
			eatAt = Random.nextInt(R_MIN, maxHealHealthToEatAt);
			break;

		// Keep near 100% hp
		case FOOD_ORIENTED:
		default:
			eatAt = Random.nextSkewedGaussian(
					HEALTH_ABS_MIN,
					maxHealHealthToEatAt,
					(int) (maxHealHealthToEatAt * foodOrientedMeanMultiplier),
					(int) Math.sqrt(foodOrientedVariance));
			break;
		}
	}
}
