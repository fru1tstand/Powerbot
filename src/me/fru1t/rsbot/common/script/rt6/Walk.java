package me.fru1t.rsbot.common.script.rt6;

import java.util.EnumSet;
import java.util.concurrent.Callable;

import org.powerbot.script.Area;
import org.powerbot.script.Tile;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Path.TraversalOption;
import org.powerbot.script.rt6.TilePath;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.common.collections.Tuple2;
import me.fru1t.rsbot.common.framework.components.Persona;
import me.fru1t.rsbot.common.framework.util.Condition;
import me.fru1t.rsbot.common.util.Random;
import me.fru1t.rsbot.common.util.Timer;

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
// TODO(v1 cleanup): Find usages of WalkUtil and rename to Walk
public class Walk {
	protected enum InteractionAmount { CONSTANT, RANDOM, GAUSS }

	/**
	 * WalkingLogic contains the methods that provide delay between clicks to the next location
	 * when walking.
	 */
	@Singleton
	private static class WalkingLogic {
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
	 * Creates Rs3Walking instances.
	 */
	public static class Factory {
		private final ClientContext ctx;
		private final Mouse mouseUtil;
		private final WalkingLogic walkingLogic;
		private final Persona persona;

		@Inject
		public Factory(
				@Singleton ClientContext ctx,
				@Singleton Mouse mouseUtil,
				@Singleton WalkingLogic walkingLogic,
				@Singleton Persona persona) {
			this.ctx = ctx;
			this.mouseUtil = mouseUtil;
			this.walkingLogic = walkingLogic;
			this.persona = persona;
		}

		public Walk create(
				Area destination,
				Tile[] fullPath,
				int randomizationTolerance) {
			return new Walk(
					ctx,
					mouseUtil,
					walkingLogic,
					persona,
					destination,
					fullPath,
					randomizationTolerance);
		}
	}

	private static final int CLOSE_ENOUGH_DISTANCE = 2;

	private final EnumSet<TraversalOption> traversalOptions;
	private final ClientContext ctx;
	private final Mouse mouseUtil;
	private final WalkingLogic walkingLogic;
	private final Tile[] fullPath;
	private final Area destination;
	private final int randomizationTolerance;
	private final Persona persona;

	private Walk(
			@Singleton ClientContext ctx,
			@Singleton Mouse mouseUtil,
			@Singleton WalkingLogic walkingLogic,
			@Singleton Persona persona,
			Area destination,
			Tile[] fullPath,
			int randomizationTolerance) {
		traversalOptions = EnumSet.of(TraversalOption.HANDLE_RUN);
		this.ctx = ctx;
		this.mouseUtil = mouseUtil;
		this.walkingLogic = walkingLogic;
		this.fullPath = fullPath;
		this.destination = destination;
		this.randomizationTolerance = randomizationTolerance;
		this.persona = persona;
	}

	/**
	 * Performs the entire walking interaction until the player reaches the destination area or
	 * some failure occurs.
	 *
	 * @return True if the player has reached the destination. Otherwise, false for any failure.
	 */
	public boolean walk() {
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
				int spamClicks = mouseUtil.getClicks();
				while (spamClicks-- > 1) {
					// TODO: Add small mouse movement (+- 1/2/3 px per click) or simply #traverse
					ctx.input.click(true);
					Condition.sleep(persona.getNextSpamDelay());
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
