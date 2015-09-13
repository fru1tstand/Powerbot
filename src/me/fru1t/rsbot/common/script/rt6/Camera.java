package me.fru1t.rsbot.common.script.rt6;

import me.fru1t.slick.util.Provider;
import org.powerbot.script.Locatable;
import org.powerbot.script.rt6.ClientContext;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.common.collections.Tuple2;
import me.fru1t.rsbot.common.framework.components.Persona;
import me.fru1t.rsbot.common.util.Random;
import org.powerbot.script.rt6.Interactive;

/**
 * Provides utility methods that deal with the camera.
 */
@Singleton
public class Camera {
	public enum Direction {
		// TODO(v1): Set these to the correct values

		// Tight values don't overlap and are set at absolute 45 degree angles
		NARROW_N(Tuple2.of(0, 45)),
		NARROW_NW(Tuple2.of(0, 40)),
		NARROW_W(Tuple2.of(0, 40)),
		NARROW_SW(Tuple2.of(0, 40)),
		NARROW_S(Tuple2.of(0, 40)),
		NARROW_SE(Tuple2.of(0, 40)),
		NARROW_E(Tuple2.of(0, 40)),
		NARROW_NE(Tuple2.of(0, 40)),

		// Medium values overlap, and are set at absolute 90 degree angles.
		MEDIUM_N(Tuple2.of(0, 90)),
		MEDIUM_NW(Tuple2.of(0, 40)),
		MEDIUM_W(Tuple2.of(0, 40)),
		MEDIUM_SW(Tuple2.of(0, 40)),
		MEDIUM_S(Tuple2.of(0, 40)),
		MEDIUM_SE(Tuple2.of(0, 40)),
		MEDIUM_E(Tuple2.of(0, 40)),
		MEDIUM_NE(Tuple2.of(0, 40)),

		// Wide values overlap heavily and are set at absolute 180 degree angles.
		WIDE_N(Tuple2.of(0, 180)),
		WIDE_NW(Tuple2.of(0, 40)),
		WIDE_W(Tuple2.of(0, 40)),
		WIDE_SW(Tuple2.of(0, 40)),
		WIDE_S(Tuple2.of(0, 40)),
		WIDE_SE(Tuple2.of(0, 40)),
		WIDE_E(Tuple2.of(0, 40)),
		WIDE_NE(Tuple2.of(0, 40));

		private final Tuple2<Integer, Integer> range;
		Direction(Tuple2<Integer, Integer> range) {
			this.range = range;
		}

		/**
		 * Returns a random angle from within this direction's range.
		 * @return A random angle from this direction's range.
		 */
		// TODO(v2): Gauss angle?
		public int getRandomAngle() {
			return Random.nextInt(range);
		}
	}

	/**
	 * Contains logic for the maybeFace methods.
	 */
	private static class MaybeFaceLogic {
		private static final Tuple2<Integer, Integer> MIN_CUTOFF_RANGE = Tuple2.of(20, 50);
		private static final Tuple2<Integer, Integer> MAX_CUTOFF_RANGE = Tuple2.of(60, 80);

		private final Persona persona;
		private final Tuple2<Integer, Integer> focusCutoff;

		@Inject
		private MaybeFaceLogic(@Singleton Persona persona) {
			this.persona = persona;
			this.focusCutoff =
					Tuple2.of(Random.nextInt(MIN_CUTOFF_RANGE), Random.nextInt(MAX_CUTOFF_RANGE));
		}

		/**
		 * Returns whether or not the camera should face.
		 * @return Whether or not the camera should face.
		 */
		public boolean shouldFace() {
			return Random.roll(persona.getFocusScaledInt(focusCutoff, Persona.MAX_RANGE));
		}
	}

	private final Provider<ClientContext> ctxProvider;
	private final MaybeFaceLogic maybeFaceLogic;

	@Inject
	public Camera(@Singleton Provider<ClientContext> ctxProvider, MaybeFaceLogic maybeFaceLogic) {
		this.ctxProvider = ctxProvider;
		this.maybeFaceLogic = maybeFaceLogic;
	}

	/**
	 * Verifies that the camera is aimed toward the given interactive, locatable object, and
	 * possibly adjusts the camera even if it is.
	 *
	 * @param interactiveLocatable The object to face.
	 */
	public <F extends org.powerbot.script.rt6.Interactive & Locatable> void maybeFace(
			F interactiveLocatable) {
		boolean shouldFace = false;
		if (!interactiveLocatable.inViewport()) {
			shouldFace = true;
		}
		if (maybeFaceLogic.shouldFace()) {
			shouldFace = true;
		}
		if (shouldFace) {
			ctxProvider.get().camera.turnTo(interactiveLocatable);
		}
	}

	/**
	 * Verifies that the camera is aimed toward the given interactive, locatable object, and
	 * possibly adjusts the camera even if it is.
	 *
	 * @param interactiveLocatable The object to face.
	 */
	public <F extends org.powerbot.script.rt4.Interactive & Locatable> void maybeFace(
			F interactiveLocatable) {
		boolean shouldFace = false;
		if (!interactiveLocatable.inViewport()) {
			shouldFace = true;
		}
		if (maybeFaceLogic.shouldFace()) {
			shouldFace = true;
		}
		if (shouldFace) {
			ctxProvider.get().camera.turnTo(interactiveLocatable);
		}
	}

	/**
	 * Forces the camera angle to face towards the given direction. May or may not move the
	 * camera if already facing.
	 *
	 * @param direction The direction to face.
	 */
	public void maybeFace(Direction direction) {
		boolean shouldFace = false;
		if (ctxProvider.get().camera.yaw() < direction.range.first
				|| ctxProvider.get().camera.yaw() > direction.range.second) {
			shouldFace = true;
		}
		if (maybeFaceLogic.shouldFace()) {
			shouldFace = true;
		}
		if (shouldFace) {
			ctxProvider.get().camera.angleTo(direction.getRandomAngle());
		}
	}

	/**
	 * Turns the camera to face the given direction.
	 *
	 * @param direction The direction to face.
	 * @deprecated
	 */
	// TODO(v1 cleanup): Remove once all calls to #face are removed
	@Deprecated
	public void face(Direction direction) {
		// TODO(v2): Mouse camera movement
		if (ctxProvider.get().camera.yaw() > direction.range.first
				&& ctxProvider.get().camera.yaw() < direction.range.second) {
			return;
		}
	}
}
