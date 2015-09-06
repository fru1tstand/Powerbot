package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.RoguesDenSafeCracker.State;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.script.rt6.Backpack;
import me.fru1t.rsbot.safecracker.strategies.logic.WalkLogic;

/**
 * This strategy simply decides which method of walking to use.
 */
public class SafeWalk implements Strategy<RoguesDenSafeCracker.State> {
	private final Backpack backpack;
	private final WalkLogic walkLogic;

	@Inject
	public SafeWalk(
			@Singleton Backpack backpack,
			WalkLogic walkLogic) {
		this.backpack = backpack;
		this.walkLogic = walkLogic;
	}

	@Override
	public RoguesDenSafeCracker.State run() {
		if (backpack.isFull()) {
			return State.BANK_WALK;
		}

		switch(walkLogic.getWalkMethod()) {
		default:
		case MINIMAP:
			return State.SAFE_WALK_USING_MINIMAP;

		case VIEWPORT:
			return State.SAFE_WALK_USING_VIEWPORT;
		}
	}

}
