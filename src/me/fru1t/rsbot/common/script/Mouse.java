package me.fru1t.rsbot.common.script;

import me.fru1t.slick.util.Provider;
import org.powerbot.script.rt6.ClientContext;

import me.fru1t.common.annotations.Singleton;
import me.fru1t.common.collections.Tuple2;
import me.fru1t.rsbot.common.framework.components.Persona;
import me.fru1t.rsbot.common.util.Random;

/**
 * The most impatient people will click more than 1 time. Someone sporadically
 * clicking will not know how many times they've clicked (or care to click a
 * consistent amount every time). The delay in clicks tends toward a unimodal
 * symmetric normal distribution (n = 300). However, because people are
 * different, the mean and variance of these curve are too.
 *
 * <p> Consider: Someone may become impatient, or fall out of impatience. Also, as
 * time wears on, fatigue may build up reducing both click count and click delay
 * mean.
 */
@Singleton
public class Mouse<C extends ClientContext> {
	// Enable/disable probabilities
	private static final int IS_ENABLED_PROBABILITY = 25;
	private static final int CLICK_COUNT_IS_RANDOM_PROBABILITY = 25;
	private static final int VARIANCE_IS_FOCUS_DEPENDENT_PROBABILITY = 80;
	// TODO: Add click count is focus dependent probability.

	// Click constants
	private static final Tuple2<Integer, Integer> CLICKS_MEAN = Tuple2.of(1, 5);
	private static final Tuple2<Double, Double> CLICKS_VARIANCE = Tuple2.of(1d, 5d);

	protected final Provider<Persona> personaProvider;

	private final boolean isSpamClickEnabled;
	private final boolean isClickCountRandom;
	private final boolean isVarianceFocusDependent;
	private final int clickCountMean;
	private int interactProbability;

	protected Mouse(Provider<Persona> personaProvider) {
		this.personaProvider = personaProvider;

		this.isSpamClickEnabled = Random.roll(IS_ENABLED_PROBABILITY);
		this.isClickCountRandom = Random.roll(CLICK_COUNT_IS_RANDOM_PROBABILITY);
		this.isVarianceFocusDependent = Random.roll(VARIANCE_IS_FOCUS_DEPENDENT_PROBABILITY);

		this.clickCountMean = Random.nextInt(CLICKS_MEAN);
		newInteractProbability();
	}

	/**
	 * Returns the number of times to click on an interact event. This should be called and stored
	 * a single time when interacting.
	 * @return The number of times to click.
	 */
	public int getClicks() {
		newInteractProbability();
		if (!isSpamClickEnabled) {
			return 1;
		}

		if (isClickCountRandom) {
			return Random.nextInt(CLICKS_MEAN);
		}

		return Random.nextSkewedGaussian(
				CLICKS_MEAN,
				clickCountMean,
				isVarianceFocusDependent
						? personaProvider.get().getFocusScaledDouble(null, CLICKS_VARIANCE)
						: Random.nextDouble(CLICKS_VARIANCE));
	}

	/**
	 * Returns if the player should re-interact with whatever.
	 * @return If the player should re-interact with whatever.
	 */
	public boolean shouldCorrectMouse() {
		return Random.roll(interactProbability);
	}

	private void newInteractProbability() {
		interactProbability = Random.nextInt(0, 100);
	}
}
