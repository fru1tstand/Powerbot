package me.fru1t.rsbot.common.script.rt6;

import org.powerbot.script.rt6.ClientContext;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.common.collections.Tuple2;
import me.fru1t.rsbot.common.framework.components.Persona;
import me.fru1t.rsbot.common.util.Random;

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

		MEDIUM_N(Tuple2.of(0, 90)),
		MEDIUM_NW(Tuple2.of(0, 40)),
		MEDIUM_W(Tuple2.of(0, 40)),
		MEDIUM_SW(Tuple2.of(0, 40)),
		MEDIUM_S(Tuple2.of(0, 40)),
		MEDIUM_SE(Tuple2.of(0, 40)),
		MEDIUM_E(Tuple2.of(0, 40)),
		MEDIUM_NE(Tuple2.of(0, 40)),

		WIDE_N(Tuple2.of(0, 180)),
		WIDE_NW(Tuple2.of(0, 40)),
		WIDE_W(Tuple2.of(0, 40)),
		WIDE_SW(Tuple2.of(0, 40)),
		WIDE_S(Tuple2.of(0, 40)),
		WIDE_SE(Tuple2.of(0, 40)),
		WIDE_E(Tuple2.of(0, 40)),
		WIDE_NE(Tuple2.of(0, 40));

		private final Tuple2<Integer, Integer> range;
		private Direction(Tuple2<Integer, Integer> range) {
			this.range = range;
		}

		/**
		 * Returns a random angle from within this direction's range.
		 * @return A random angle from this direction's range.
		 */
		public int getRandomAngle() {
			return Random.nextInt(range);
		}

		// TODO(v2): Gauss angle?
	}

	/**
	 * Contains logic for the {@link Camera#maybeFace(Direction)} method.
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

	private final ClientContext ctx;
	private final MaybeFaceLogic maybeFaceLogic;

	@Inject
	public Camera(@Singleton ClientContext ctx, MaybeFaceLogic maybeFaceLogic) {
		this.ctx = ctx;
		this.maybeFaceLogic = maybeFaceLogic;
	}

	/**
	 * Sometimes turns the camera to the given direction.
	 * @param direction The direction to face.
	 */
	public void maybeFace(Direction direction) {
		if (maybeFaceLogic.shouldFace()) {
			face(direction);
		}
	}

	/**
	 * Turns the camera to face the given direction.
	 * @param direction The direction to face.
	 */
	public void face(Direction direction) {
		// TODO(v2): Mouse camera movement
		if (ctx.camera.yaw() > direction.range.first && ctx.camera.yaw() < direction.range.second) {
			return;
		}

		ctx.camera.angleTo(Random.nextInt(direction.range));
	}
}
