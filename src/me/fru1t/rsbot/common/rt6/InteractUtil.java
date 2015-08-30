package me.fru1t.rsbot.common.rt6;

import org.powerbot.script.ClientContext;
import org.powerbot.script.rt6.Interactive;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.common.collections.Tuple2;
import me.fru1t.rsbot.common.framework.components.Persona;
import me.fru1t.rsbot.common.framework.util.Condition;
import me.fru1t.rsbot.common.framework.util.Random;

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
public class InteractUtil {
	// Enable/disable probabilities
	private static final int IS_ENABLED_PROBABILITY = 25;
	private static final int CLICK_COUNT_IS_RANDOM_PROBABILITY = 25;
	private static final int DELAY_IS_RANDOM_PROBABILITY = 50;
	private static final int VARIANCE_IS_FOCUS_DEPENDENT_PROBABILITY = 80;
	// TODO: Add click count is focus dependent probability.

	// Click constants
	private static final Tuple2<Integer, Integer> CLICKS_MEAN = Tuple2.of(1, 5);
	private static final Tuple2<Double, Double> CLICKS_VARIANCE = Tuple2.of(1d, 5d);

	// Delay  constants
	private static final Tuple2<Integer, Integer> DELAY_MEAN = Tuple2.of(90, 175);
	private static final Tuple2<Double, Double> DELAY_VARIANCE = Tuple2.of(0.5d, 4d);

	private final Persona persona;
	private final ClientContext<?> ctx;

	private final boolean isEnabled;
	private final boolean isDelayRandom;
	private final boolean isClickCountRandom;
	private final boolean isVarianceFocusDependent;
	private final int clickCountMean;
	private final int delayMean;
	private int interactProbability;

	@Inject
	public InteractUtil(
			@Singleton ClientContext<?> ctx,
			Persona persona) {
		this.ctx = ctx;
		this.persona = persona;

		this.isEnabled = Random.roll(IS_ENABLED_PROBABILITY);
		this.isDelayRandom = Random.roll(DELAY_IS_RANDOM_PROBABILITY);
		this.isClickCountRandom = Random.roll(CLICK_COUNT_IS_RANDOM_PROBABILITY);
		this.isVarianceFocusDependent = Random.roll(VARIANCE_IS_FOCUS_DEPENDENT_PROBABILITY);

		this.clickCountMean = Random.nextInt(CLICKS_MEAN);
		this.delayMean = Random.nextInt(DELAY_MEAN);
		newInteractProbability();
	}

	/**
	 * Left clicks on the given interactive object with a human-like interaction.
	 * @param interactive
	 */
	public void click(Interactive interactive) {
		int clicks = isEnabled ? getClicks() : 1;
		boolean isFirstHover = true;
		while (clicks-- > 0) {
			if (isFirstHover || shouldCorrectMouse()) {
				isFirstHover = false;
				interactive.hover();
			}
			ctx.input.click(true);

			if (clicks > 0) {
				Condition.sleep(getDelay());
			}
		}
	}

	/**
	 * Returns the number of times to click on an interact event. This should be called and stored
	 * a single time when interacting.
	 * @return The number of times to click.
	 */
	public int getClicks() {
		newInteractProbability();
		return getConditionalRandomOrGauss(
				isClickCountRandom, clickCountMean, CLICKS_MEAN, CLICKS_VARIANCE);
	}

	/**
	 * Returns the delay between each click on an interact event. This should be called every click
	 * event to determine delay.
	 * @return The delay, in milliseconds, between each click.
	 */
	public int getDelay() {
		return getConditionalRandomOrGauss(
				isDelayRandom, delayMean, DELAY_MEAN, DELAY_VARIANCE);
	}

	/**
	 * Returns if the player should re-interact with whatever.
	 * @return If the player should re-interact with whatever.
	 */
	private boolean shouldCorrectMouse() {
		return Random.roll(interactProbability);
	}

	private int getConditionalRandomOrGauss(
			boolean isRandom,
			int mean,
			Tuple2<Integer, Integer> meanRange,
			Tuple2<Double, Double> varianceRange) {
		return isRandom
				? Random.nextInt(meanRange)
				: Random.nextSkewedGaussian(
						meanRange,
						mean,
						isVarianceFocusDependent
								? persona.getFocusScaledDouble(null, varianceRange)
								: Random.nextDouble(varianceRange));
	}

	private void newInteractProbability() {
		interactProbability = Random.nextInt(0, 100);
	}
}
