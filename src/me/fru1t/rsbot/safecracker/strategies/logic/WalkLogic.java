package me.fru1t.rsbot.safecracker.strategies.logic;

import me.fru1t.common.annotations.Inject;
import me.fru1t.rsbot.common.util.Probability;
import me.fru1t.rsbot.common.util.Random;

public class WalkLogic {
	public enum WalkMethod implements Probability {
		// The user clicks on the minimap to traverse to the safe location
		MINIMAP(50),

		// The user clicks on the viewport to traverse to the safe location.
		// In some instances, will simply skip the traverse all together and interact with the safe
		// directly.
		VIEWPORT(50);

		private final int probability;
		private WalkMethod(int probability) {
			this.probability = probability;
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

	public WalkMethod getWalkMethod() {
		return walkMethod;
	}
}
