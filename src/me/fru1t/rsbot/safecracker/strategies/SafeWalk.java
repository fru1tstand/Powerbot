package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.rsbot.common.framework.components.Status;
import me.fru1t.rsbot.common.framework.util.Callables;
import me.fru1t.slick.util.Provider;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.script.rt6.Backpack;
import me.fru1t.rsbot.common.script.rt6.Walk;
import me.fru1t.rsbot.safecracker.strategies.logic.SafeLogic;
import me.fru1t.rsbot.safecracker.strategies.logic.WalkLogic;

/**
 * Completes most (if not all) of a traverse to the safe to crack. Does not guarantee the player
 * has completed this traverse.
 */
public class SafeWalk implements Strategy<RoguesDenSafeCracker.State> {
	private final Provider<Status> statusProvider;
	private final Backpack backpack;
	private final WalkLogic walkLogic;
	private final SafeLogic safeLogic;
	private final Walk.Factory walkFactory;

	@Inject
	public SafeWalk(
			Provider<Status> statusProvider,
			@Singleton Backpack backpack,
			@Singleton SafeLogic safeLogic,
			@Singleton WalkLogic walkLogic,
			Walk.Factory walkFactory) {
		this.statusProvider = statusProvider;
		this.backpack = backpack;
		this.walkLogic = walkLogic;
		this.safeLogic = safeLogic;
		this.walkFactory = walkFactory;
	}

	@Override
	public RoguesDenSafeCracker.State run() {
		statusProvider.get().update("Walking to the safe");

		if (backpack.isFull()) {
			statusProvider.get().update("The inventory is full");
			return RoguesDenSafeCracker.State.WALK_TO_BANK;
		}

		safeLogic.newSafe();
		if (safeLogic.getSafeGameObject().inViewport()) {
			statusProvider.get().update("The safe is already within view");
			return RoguesDenSafeCracker.State.SAFE_CRACK;
		}

		Walk walk = walkFactory.createUsingLocalPath(safeLogic.getSafe().location);
		if (walk.isCloseEnoughOrOnTheWay()) {
			statusProvider.get().update("We're already at or walking towards the safe");
			return RoguesDenSafeCracker.State.SAFE_CRACK;
		}

		if (!walk.walkUntil(
				walkLogic.getWalkMethod(),
				Callables.inViewport(safeLogic.getSafeGameObject()))) {
			return null;
		}

		return RoguesDenSafeCracker.State.SAFE_CRACK;
	}

}
