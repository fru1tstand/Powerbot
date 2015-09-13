package me.fru1t.rsbot.safecracker.strategies.logic;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.common.script.rt6.Walk;
import me.fru1t.rsbot.common.util.Probability;
import me.fru1t.rsbot.common.util.Random;

/**
 * Provides logic for the walking methods.
 */
// TODO(v2): Possibly move this logic over to the underlying util
@Singleton
public class WalkLogic {
	public enum WalkMethod implements Probability {
		// The user clicks on the minimap to traverse to the safe location
		MINIMAP(50, Walk.Method.MINIMAP),

		// The user clicks on the viewport to traverse to the safe location.
		// In some instances, will simply skip the traverse all together and interact with the safe
		// directly.
		VIEWPORT(50, Walk.Method.VIEWPORT);

		private final int probability;
		private final Walk.Method walkMethod;
		private WalkMethod(int probability, Walk.Method walkMethod) {
			this.probability = probability;
			this.walkMethod = walkMethod;
		}

		@Override
		public int getProbability() {
			return probability;
		}
	}

	private final WalkMethod walkMethod;

	@Inject
	public WalkLogic() {
		this.walkMethod = Random.roll(WalkMethod.class);
	}

	public Walk.Method getWalkMethod() {
		return walkMethod.walkMethod;
	}
}
