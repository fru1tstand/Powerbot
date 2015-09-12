package me.fru1t.rsbot.common.script.rt6;

import java.util.EnumSet;

import me.fru1t.slick.util.Provider;
import org.powerbot.script.Locatable;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Path;
import org.powerbot.script.rt6.Path.TraversalOption;
import org.powerbot.script.rt6.TileMatrix;
import org.powerbot.script.rt6.TilePath;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Nullable;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.common.collections.Tuple2;
import me.fru1t.rsbot.common.framework.util.Callable;
import me.fru1t.rsbot.common.framework.util.Callables;
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
// TODO(v1): Add early-quitting for script pause/stop
// TODO(v1): Rename this class to something like "Path" or extend Path as it has evolved into
// more of a Path-like class instead of a walk util.
public class Walk {
	protected enum InteractionAmount { CONSTANT, RANDOM, GAUSS }
	public enum Method { VIEWPORT, MINIMAP }

	/**
	 * WalkingLogic contains the methods that provide delay between clicks to the next location
	 * when walking.
	 */
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
	 * An injectable factory that creates Walk instances.
	 */
	public static class Factory {
		private final Provider<ClientContext> ctxProvider;
		private final Mouse mouseUtil;
		private final WalkingLogic walkingLogic;
		private final Condition condition;
		private final Timer waitTimer;

		@Inject
		public Factory(
				Provider<ClientContext> ctxProvider,
				@Singleton Mouse mouseUtil,
				WalkingLogic walkingLogic,
				@Singleton Condition condition,
				Timer waitTimer) {
			this.ctxProvider = ctxProvider;
			this.mouseUtil = mouseUtil;
			this.walkingLogic = walkingLogic;
			this.condition = condition;
			this.waitTimer = waitTimer;
		}

		/**
		 * Creates a new Walk object with the given path.
		 * @param path
		 * @return A new walk object.
		 */
		public Walk create(Path path) {
			return new Walk(
					ctxProvider,
					mouseUtil,
					condition,
					waitTimer,
					walkingLogic,
					path);
		}

		/**
		 * Creates a new Walk object with the given locatable, generating a path using localpath.
		 *
		 * @param locatable The object to walk towards.
		 * @return A new walk object.
		 */
		public Walk createUsingLocalPath(Locatable locatable) {
			return create(ctxProvider.get().movement.findPath(locatable));
		}
	}

	private static final String WALK_INTERACT_TEXT = "Walk here";
	private static final EnumSet<TraversalOption> TRAVERSAL_OPTIONS =
			EnumSet.of(TraversalOption.HANDLE_RUN);
	// TODO(v2): Find a more dynamic approach to this.
	private static final int CLOSE_ENOUGH_DISTANCE = 4;
	private static final Tuple2<Integer, Integer> WAIT_TIMER_DURATION_RANGE = Tuple2.of(400, 800);
	private static final int WAIT_TIMER_POLL_FREQUENCY = 150;

	private final Provider<ClientContext> ctxProvider;
	private final Mouse mouse;
	private final WalkingLogic walkingLogic;
	private final Condition condition;
	private final Timer waitTimer;

	private final Path path;

	private Walk(
			Provider<ClientContext> ctxProvider,
			@Singleton Mouse mouse,
			@Singleton Condition condition,
			Timer waitTimer,
			WalkingLogic walkingLogic,
			Path path) {
		this.ctxProvider = ctxProvider;
		this.mouse = mouse;
		this.walkingLogic = walkingLogic;
		this.condition = condition;
		this.path = path;
		this.waitTimer = waitTimer;
	}

	/**
	 * The player navigates the path using the given walk method. This method does not guarantee
	 * the player will be at the end location of the path before it exits, but does guarantee that
	 * over time, the player will be at the location. This method will also return if the given
	 * release evaluates to true.
	 *
	 * @param walkMethod The method of walking to use.
	 * @param release The condition to check against.
	 * @return True if the traversing was successful. False otherwise.
	 */
	public boolean walkUntil(Method walkMethod, @Nullable Callable<Boolean> release) {
		return (walkMethod == Method.MINIMAP)
				? walkWithMinimapUntil(release) : walkWithViewportUntil(release);
	}

	/**
	 * Synonymous to {@link #walk(Method, Callable)} passing null as the condition.
	 *
	 * @see #walk(Method, Callable)
	 * @param walkMethod The method of walking to use.
	 * @return True of the traversing was successful. False otherwise.
	 */
	public boolean walk(Method walkMethod) {
		return walkUntil(walkMethod, null);
	}

	/**
	 * Performs the majority of this path and releases control when the player or the destination
	 * of the player is within the "close enough" distance of {@value #CLOSE_ENOUGH_DISTANCE}
	 * units, or until the given condition evaluates to true. This method interfaces with the
	 * minimap to traverse the path.
	 *
	 * @param release The condition to validate with.
	 * @return True if the traversing has been successful. False otherwise.
	 */
	public boolean walkWithMinimapUntil(@Nullable Callable<Boolean> release) {
		return genericWalk(new Callable<Boolean>() {
					@Override
					public Boolean ring() {
						if (!path.traverse(TRAVERSAL_OPTIONS)) {
							return false;
						}
						int spamClicks = mouse.getClicks();
						while (spamClicks-- > 1) {
							// TODO: Add small mouse movement (+- 1/2/3 px per click)
							ctxProvider.get().input.click(true);
							condition.sleepForSpamDelay();
						}
						return true;
					}
				},
				release);
	}

	/**
	 * Synonymous to {@link #walkUntil(Callable)} passing null as the condition.
	 *
	 * @see #walkUntil(Callable)
	 * @return True if the traversing has been successful. False otherwise.
	 */
	public boolean walkWithMinimap() {
		return walkWithMinimapUntil(null);
	}

	/**
	 * Performs the majority of this path and releases control when the player or the destination
	 * of the player is within the "close enough" distance of {@value #CLOSE_ENOUGH_DISTANCE}
	 * units, or until the given condition evaluates to true. This method forces the player to use
	 * the viewport to navigate as opposed to the traditional minimap walking.
	 *
	 * @return True if the traversing was successful. False otherwise.
	 */
	public boolean walkWithViewportUntil(@Nullable Callable<Boolean> release) {
		return genericWalk(new Callable<Boolean>() {
					@Override
					public Boolean ring() {
						// TODO(v1): Does path#next grab the correct next tile to traverse to?
						TileMatrix tile = path.next().matrix(ctxProvider.get());
						if (!tile.inViewport()) {
							ctxProvider.get().camera.turnTo(tile);
						}
						if (!tile.interact(WALK_INTERACT_TEXT)) {
							return false;
						}
						int spamClicks = mouse.getClicks();
						while (spamClicks-- > 1) {
							if (ctxProvider.get().menu.items()[0].equals(WALK_INTERACT_TEXT)) {
								ctxProvider.get().input.click(true);
								condition.sleepForSpamDelay();
							}
						}
						return true;
					}
				},
				release);
	}

	/**
	 * Synonymous to {@link #walkWithViewportUntil(Callable)} passing null as the condition.
	 *
	 * @see #walkWithViewportUntil(Callable)
	 * @return True of the traversing has been successful. False otherwise.
	 */
	public boolean walkWithViewport() {
		return walkWithViewportUntil(null);
	}

	/**
	 * Waits for the player to fully complete the path. This ideally should be called right after
	 * a walk method has been called if the strategy requires an absolute location to work with.
	 *
	 * @return True if the player is at or near the destination of this path.
	 */
	public boolean waitUntilDestinationIsReached() {
		return Condition.wait(
				new Callable<Boolean>() { /* Condition */
					@Override
					public Boolean ring() {
						return ctxProvider.get().players.local().tile().distanceTo(path.end())
								<= CLOSE_ENOUGH_DISTANCE;
					}
				},
				new Callable<Boolean>() { /* Timer Condition */
					@Override
					public Boolean ring() {
						return ctxProvider.get().players.local().inMotion();
					}
				},
				waitTimer,
				Random.nextInt(WAIT_TIMER_DURATION_RANGE),
				WAIT_TIMER_POLL_FREQUENCY);
	}

	/**
	 * Returns if the player or the player's destination is close enough to the destination.
	 *
	 * @return True if the player is close enough or on the way. False otherwise.
	 */
	public boolean isCloseEnoughOrOnTheWay() {
		return ctxProvider.get().movement.destination().distanceTo(path.end()) <= CLOSE_ENOUGH_DISTANCE
				|| ctxProvider.get().players.local().tile().distanceTo(path.end()) <=
				CLOSE_ENOUGH_DISTANCE;
	}

	/**
	 * Performs the path walking using the given interact method when it needs to interact.
	 */
	private boolean genericWalk(Callable<Boolean> interact, @Nullable Callable<Boolean> nRelease) {
		Callable<Boolean> release = (nRelease == null) ? Callables.of(false) : nRelease;
		walkingLogic.fullReset();

		while (!release.ring()
				&& !isCloseEnoughOrOnTheWay()) {
			if (walkingLogic.shouldInteract()) {
				if (!interact.ring()) {
					return false;
				}
			}

			// Block until we're moving
			if (!Condition.wait(new Callable<Boolean>() {
					@Override
					public Boolean ring() {
						return ctxProvider.get().players.local().inMotion();
					}
				}, 150)) {
				return false;
			}

			condition.sleepForInteractDelay();
		}
		return true;
	}
}
