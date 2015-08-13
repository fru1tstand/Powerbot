package me.fru1t.rsbot.safecracker.actions.modules;

import me.fru1t.annotations.Inject;
import me.fru1t.collections.Tuple2;
import me.fru1t.rsbot.common.framework.action.modules.SpamClick;


public class SpamClickProxy extends SpamClick.SettingsProxy {
	// Enable/disable probabilities
	private static final int IS_ENABLED_PROBABILITY = 25;
	private static final int DELAY_IS_RANDOM_PROBABILITY = 50;
	private static final int VARIANCE_IS_FOCUS_DEPENDENT_PROBABILITY = 2;

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

	private final SpamClick.Factory spamClickFactory;
	
	@Inject
	public SpamClickProxy(SpamClick.Factory spamClickFactory) {
		this.spamClickFactory = spamClickFactory;
	}

	@Override
	public SpamClick getInstance() {
		return spamClickFactory.create(
				IS_ENABLED_PROBABILITY,
				DELAY_IS_RANDOM_PROBABILITY,
				VARIANCE_IS_FOCUS_DEPENDENT_PROBABILITY,
				Tuple2.of(CLICKS_MEAN_MIN, CLICKS_MEAN_MAX),
				Tuple2.of(CLICKS_VARIANCE_MIN, CLICKS_VARIANCE_MAX),
				Tuple2.of(DELAY_MEAN_MIN, DELAY_MEAN_MAX),
				Tuple2.of(DELAY_VARIANCE_MIN, DELAY_VARIANCE_MAX));
	}
}
