package me.fru1t.rsbot.safecracker.strategies.logic;

import me.fru1t.slick.util.Provider;
import org.powerbot.script.rt6.ClientContext;

import me.fru1t.common.annotations.Inject;
import me.fru1t.rsbot.common.util.Random;
import me.fru1t.rsbot.safecracker.Settings;

/**
 * Eating habits may vary from person to person. Several of these include eating when hp
 * reaches as low as possible without dying then fully healing, eating whenever health drops
 * below what the food item heals, eating when hp falls below a specific threshold, etc.
 *
 * <p>TODO(v2): Add over-eating possibility
 * <p>TODO(v2): Add norm distribution for constant hp eating
 * <p>TODO(v2): Add dependency on personaProvider's focus
 */
public class Health {
	/**
	 * Defines the different styles of eating
	 */
	// TODO(v1): Convert to probability enum
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


	private final Provider<ClientContext> ctxProvider;
	private final Provider<Settings> settingsProvider;

	private final EatMethod eatMethod;
	private final boolean isConstant;
	private double foodOrientedMeanMultiplier;
	private double foodOrientedVariance;
	private int eatAt;

	@Inject
	public Health(Provider<ClientContext> contextProvider, Provider<Settings> settingsProvider) {
		this.ctxProvider = contextProvider;
		this.settingsProvider = settingsProvider;
		eatAt = -1;

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
	}

	/**
	 * @return The health to eat at
	 */
	public int eatAt() {
		if (eatAt < 0) {
			newEatAt();
		}
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
			if (settingsProvider.get().getFood().healAmount + R_MIN + HEALTH_ABS_MIN
					>= ctxProvider.get().combatBar.maximumHealth()) {
				eatMethod = EatMethod.LOWEST_POSSIBLE;
			}
			break;
		case FOOD_ORIENTED:
		default:
			if (settingsProvider.get().getFood().healAmount * FO_MAX_FOOD_MULTIPLIER + HEALTH_ABS_MIN
					>= ctxProvider.get().combatBar.maximumHealth()) {
				eatMethod = EatMethod.LOWEST_POSSIBLE;
			}
			break;
		}

		// TODO: Find if this value is inaccurate due to EOC vs Legacy settingsProvider
		int maxHealHealthToEatAt = ctxProvider.get().combatBar.maximumHealth()
				- settingsProvider.get().getFood().healAmount;

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
