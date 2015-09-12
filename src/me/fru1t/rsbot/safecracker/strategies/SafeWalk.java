package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.slick.util.Provider;
import org.powerbot.script.rt6.ClientContext;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.RoguesDenSafeCracker.State;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.framework.util.Callable;
import me.fru1t.rsbot.common.script.rt6.Backpack;
import me.fru1t.rsbot.common.script.rt6.Walk;
import me.fru1t.rsbot.safecracker.strategies.logic.SafeLogic;
import me.fru1t.rsbot.safecracker.strategies.logic.WalkLogic;

/**
 * Completes most (if not all) of a traverse to the safe to crack. Does not guarantee the player
 * has completed this traverse.
 */
public class SafeWalk implements Strategy<RoguesDenSafeCracker.State> {
	private final Provider<ClientContext> ctxProvider;
	private final Backpack backpack;
	private final WalkLogic walkLogic;
	private final SafeLogic safeLogic;
	private final Walk.Factory walkFactory;

	@Inject
	public SafeWalk(
			@Singleton Provider<ClientContext> ctxProvider,
			@Singleton Backpack backpack,
			@Singleton SafeLogic safeLogic,
			WalkLogic walkLogic,
			Walk.Factory walkFactory) {
		this.backpack = backpack;
		this.walkLogic = walkLogic;
		this.safeLogic = safeLogic;
		this.ctxProvider = ctxProvider;
		this.walkFactory = walkFactory;
	}

	@Override
	public RoguesDenSafeCracker.State run() {
		// Preconditions check
		if (backpack.isFull()) {
			return State.BANK_WALK;
		}

		// Seed a new safe to crack
		safeLogic.newSafe();
		Walk walk = walkFactory.createUsingLocalPath(safeLogic.getSafe().location);

		if (walk.isCloseEnoughOrOnTheWay()) {
			return State.SAFE_CRACK;
		}

		if (!walk.walkUntil(
				walkLogic.getWalkMethod(),
				new Callable<Boolean>() { /* Condition */
					@Override
					public Boolean ring() {
						return ctxProvider.get().objects
								.select()
								.id(RoguesDenSafeCracker.SAFE_OBJECT_ID)
								.at(safeLogic.getSafe().location)
								.poll()
								.inViewport();
					}
				})) {
			return null;
		}

		return State.SAFE_CRACK;
	}

}
