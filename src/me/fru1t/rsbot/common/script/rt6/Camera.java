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
		N(Tuple2.of(0, 40)),
		NW(Tuple2.of(0, 40)),
		W(Tuple2.of(0, 40)),
		SW(Tuple2.of(0, 40)),
		S(Tuple2.of(0, 40)),
		SE(Tuple2.of(0, 40)),
		E(Tuple2.of(0, 40)),
		NE(Tuple2.of(0, 40));

		private final Tuple2<Integer, Integer> range;
		private Direction(Tuple2<Integer, Integer> range) {
			this.range = range;
		}
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
