package me.fru1t.rsbot.safecracker.actions.logic;

import me.fru1t.annotations.Inject;
import me.fru1t.annotations.Singleton;
import me.fru1t.collections.Tuple2;
import me.fru1t.rsbot.common.actions.logic.SpamClick;

@Singleton
public class InteractSpamClickProvider {
	// Enable/disable probabilities
	private static final int IS_ENABLED_PROBABILITY = 25;
	private static final int DELAY_IS_RANDOM_PROBABILITY = 50;
	private static final int VARIANCE_IS_FOCUS_DEPENDENT_PROBABILITY = 80;

	// Click constants
	private static final int CLICKS_MEAN_MIN = 1;
	private static final int CLICKS_MEAN_MAX = 5;
	private static final double CLICKS_VARIANCE_MIN = 1;
	private static final double CLICKS_VARIANCE_MAX = 5;

	// Delay  constants
	private static final int DELAY_MEAN_MIN = 90;
	private static final int DELAY_MEAN_MAX = 175;
	private static final double DELAY_VARIANCE_MIN = 0.5;
	private static final double DELAY_VARIANCE_MAX = 4;

	private final SpamClick spamClickInstance;
	
	@Inject
	public InteractSpamClickProvider(SpamClick.Factory spamClickFactory) {
		this.spamClickInstance = spamClickFactory.create(
				IS_ENABLED_PROBABILITY,
				DELAY_IS_RANDOM_PROBABILITY,
				VARIANCE_IS_FOCUS_DEPENDENT_PROBABILITY,
				Tuple2.of(CLICKS_MEAN_MIN, CLICKS_MEAN_MAX),
				Tuple2.of(CLICKS_VARIANCE_MIN, CLICKS_VARIANCE_MAX),
				Tuple2.of(DELAY_MEAN_MIN, DELAY_MEAN_MAX),
				Tuple2.of(DELAY_VARIANCE_MIN, DELAY_VARIANCE_MAX));
	}

	/**
	 * @return The instance of SpamClick stored.
	 */
	public SpamClick get() {
		return spamClickInstance;
	}
}
