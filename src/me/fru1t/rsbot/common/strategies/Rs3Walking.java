package me.fru1t.rsbot.common.strategies;

import java.util.EnumSet;
import java.util.concurrent.Callable;

import org.powerbot.script.Area;
import org.powerbot.script.Tile;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Path.TraversalOption;
import org.powerbot.script.rt6.TilePath;

import me.fru1t.annotations.Inject;
import me.fru1t.annotations.Singleton;
import me.fru1t.collections.Tuple2;
import me.fru1t.rsbot.common.Timer;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.strategies.logic.SpamClick;
import me.fru1t.rsbot.common.util.Condition;
import me.fru1t.rsbot.common.util.Random;

/**
 * Defines a generic Rs3Walking algorithm.
 * 
 * <p> TODO: Figure out if {@link TilePath#traverse()} is good enough. Maybe add traversing
 * algorithms such as:
 * "straight shot" - the player only clicks in one locations on the
 * minimap producing a straight path towards the destination, and corrects for error when the
 * destination is on the minimap.
 * 
 * <p> TODO: Add distance-based interaction alongside time based.
 */
public class Rs3Walking implements Strategy {
	protected enum InteractionAmount { CONSTANT, RANDOM, GAUSS }
	
	/**
	 * WalkingLogic contains the methods that provide delay between clicks to the next location
	 * when walking.
	 */
	@Singleton
	private class WalkingLogic {
		// Interaction enum probabilities
		private static final int CONSTANT_THRESHOLD =  5;
		private static final int RANDOM_THRESHOLD = 45;
		// private static final int GUASS_THRESHOLD = 100;
		
		// General
		private static final int ABSOLUTE_MIN_DELAY = 100;
		private static final int ABSOLUTE_MAX_DELAY = 2000;
		
		// Gauss
		private static final double GAUSS_MIN_VARIANCE = 0.8;
		private static final double GAUSS_MAX_VARIANCE = 7;
		
		private final Timer timer;
		private final InteractionAmount interactionAmount;
		private int gaussMean;
		private double gaussStdev;
		
		@Inject
		private WalkingLogic(Timer timer) {
			this.timer = timer;
			int roll = Random.nextInt(0, 100);
			if (roll < CONSTANT_THRESHOLD) {
				interactionAmount = InteractionAmount.CONSTANT;
			} else if (roll < RANDOM_THRESHOLD) {
				interactionAmount = InteractionAmount.RANDOM;
			} else {
				interactionAmount = InteractionAmount.GAUSS;
			}
		}
		
		/**
		 * Resets the timer according to what logic is enabled. This should be called every time
		 * the run method from walking is called.
		 */
		private void fullReset() {
			switch(interactionAmount) {
			case CONSTANT:
				// Set new constant time
				timer.set(Random.nextInt(ABSOLUTE_MIN_DELAY, ABSOLUTE_MAX_DELAY));
				break;
			case GAUSS:
				// Set new gauss time
				gaussMean = Random.nextInt(ABSOLUTE_MIN_DELAY, ABSOLUTE_MAX_DELAY);
				gaussStdev = Math.sqrt(Random.nextDouble(GAUSS_MIN_VARIANCE, GAUSS_MAX_VARIANCE));
				break;
			case RANDOM:
			default: // Do nothing.
				break;
			}
		}
		
		/**
		 * @return Returns if the player should interact with the walking path.
		 */
		private boolean shouldInteract() {
			if (timer.hasExpired()) {
				switch(interactionAmount) {
				case CONSTANT:
					timer.reset();
					break;

				case RANDOM:
					timer.set(Random.nextInt(ABSOLUTE_MIN_DELAY, ABSOLUTE_MAX_DELAY));
					break;
					
				case GAUSS:
				default:
					timer.set(Random.nextSkewedGaussian(
							Tuple2.of(ABSOLUTE_MIN_DELAY, ABSOLUTE_MAX_DELAY),
							gaussMean,
							gaussStdev));
					break;
				}
				return true;
			}
			return false;
		}
	}
	
	/**
	 * WalkingSpamClick contains the settings for spam clicking while walking.
	 */
	@Singleton
	private class WalkingSpamClick {
		// Enable/disable probabilities
		private static final int IS_ENABLED_PROBABILITY = 35;
		private static final int DELAY_IS_RANDOM_PROBABILITY = 25;
		private static final int VARIANCE_IS_FOCUS_DEPENDENT_PROBABILITY = 80;

		// Click constants
		private static final int CLICKS_MEAN_MIN = 1;
		private static final int CLICKS_MEAN_MAX = 8;
		private static final double CLICKS_VARIANCE_MIN = 1;
		private static final double CLICKS_VARIANCE_MAX = 5;

		// Delay  constants
		private static final int DELAY_MEAN_MIN = 70;
		private static final int DELAY_MEAN_MAX = 200;
		private static final double DELAY_VARIANCE_MIN = 0.5;
		private static final double DELAY_VARIANCE_MAX = 4;
		
		private final SpamClick spamClickInstance;
		
		@Inject
		private WalkingSpamClick(SpamClick.Factory spamClickFactory) {
			this.spamClickInstance = spamClickFactory.create(
					IS_ENABLED_PROBABILITY,
					DELAY_IS_RANDOM_PROBABILITY,
					VARIANCE_IS_FOCUS_DEPENDENT_PROBABILITY,
					Tuple2.of(CLICKS_MEAN_MIN, CLICKS_MEAN_MAX),
					Tuple2.of(CLICKS_VARIANCE_MIN, CLICKS_VARIANCE_MAX),
					Tuple2.of(DELAY_MEAN_MIN, DELAY_MEAN_MAX),
					Tuple2.of(DELAY_VARIANCE_MIN, DELAY_VARIANCE_MAX));
		}
		
		private SpamClick get() {
			return spamClickInstance;
		}
	}
	
	/**
	 * Creates Rs3Walking instances.
	 */
	public class Factory {
		private final ClientContext ctx;
		private final WalkingSpamClick walkingSpamClick;
		private final WalkingLogic walkingLogic;
		
		@Inject
		public Factory(
				@Singleton ClientContext ctx,
				@Singleton WalkingSpamClick walkingSpamClick,
				@Singleton WalkingLogic walkingLogic) {
			this.ctx = ctx;
			this.walkingSpamClick = walkingSpamClick;
			this.walkingLogic = walkingLogic;
		}
		
		public Rs3Walking create(Area destination, Tile[] fullPath, int randomizationTolerance) {
			return new Rs3Walking(
					ctx,
					walkingSpamClick,
					walkingLogic,
					destination,
					fullPath,
					randomizationTolerance);
		}
	}
	
	private static final int CLOSE_ENOUGH_DISTANCE = 2;
	
	private final EnumSet<TraversalOption> traversalOptions;
	private final ClientContext ctx;
	private final SpamClick walkingSpamClick;
	private final WalkingLogic walkingLogic;
	private final Tile[] fullPath;
	private final Area destination;
	private final int randomizationTolerance;
	
	private Rs3Walking(
			@Singleton ClientContext ctx,
			@Singleton WalkingSpamClick walkingSpamClick,
			@Singleton WalkingLogic walkingLogic,
			Area destination,
			Tile[] fullPath,
			int randomizationTolerance) {
		traversalOptions = EnumSet.of(TraversalOption.HANDLE_RUN);
		this.ctx = ctx;
		this.walkingSpamClick = walkingSpamClick.get();
		this.walkingLogic = walkingLogic;
		this.fullPath = fullPath;
		this.destination = destination;
		this.randomizationTolerance = randomizationTolerance;
	}

	@Override
	public boolean run() {
		TilePath tilePath = ctx
				.movement
				.newTilePath(fullPath)
				.randomize(randomizationTolerance, randomizationTolerance);
		walkingLogic.fullReset();
		while (!destination.contains(ctx.players.local())
				|| tilePath.end().distanceTo(ctx.players.local()) < CLOSE_ENOUGH_DISTANCE) {
			// Interact with the path when told to do so by logic
			if (walkingLogic.shouldInteract()) {
				if (!tilePath.traverse(traversalOptions)) {
					break;
				}
				int spamClicks = walkingSpamClick.getClicks();
				while (spamClicks-- > 1) {
					// TODO: Add small mouse movement (+- 1/2/3 px per click) or simply #traverse
					ctx.input.click(true);
					Condition.sleep(walkingSpamClick.getDelay());
				}
			}
			
			// Block until we're moving
			if (!Condition.wait(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					return ctx.players.local().inMotion();
				}
			}, 150))
				return false;
			
			// We're slow and don't react to things instantaneously. Also, this adds some variance
			// to the InteractionAmount.Constant interact model.
			Condition.sleep(Random.nextInt(50, 200));
		}
		
		return true;
	}
}
