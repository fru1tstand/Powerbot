package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.common.framework.components.Status;
import me.fru1t.rsbot.common.framework.util.Callables;
import me.fru1t.rsbot.safecracker.strategies.logic.WalkLogic;
import me.fru1t.slick.util.Provider;
import org.powerbot.script.Area;
import org.powerbot.script.Tile;

import me.fru1t.common.annotations.Inject;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.script.rt6.Walk;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Npc;

/**
 * Navigates the player from anywhere within rogue's den towards the banker.
 */
public class WalkToBank implements Strategy<RoguesDenSafeCracker.State> {
	private final Provider<ClientContext> ctxProvider;
	private final Provider<Status> statusProvider;
	private final Walk.Factory walkFactory;
	private final WalkLogic walkLogic;

	@Inject
	public WalkToBank(
			Provider<ClientContext> ctxProvider,
			Provider<Status> statusProvider,
			Walk.Factory walkingFactory,
			@Singleton WalkLogic walkLogic) {
		this.ctxProvider = ctxProvider;
		this.statusProvider = statusProvider;
		this.walkFactory = walkingFactory;
		this.walkLogic = walkLogic;
	}

	@Override
	public RoguesDenSafeCracker.State run() {
		statusProvider.get().update("Banking: Walking to Benedict");

		// Are we in rogue's den?
		Npc banker = ctxProvider.get().npcs.select().id(OpenBank.BANKER_NPC_ID).poll();
		if (!banker.valid()) {
			statusProvider.get().update("Banking: 404 - Banker not found");
			return null;
		}

		// Is the banker already in the viewport?
		if (banker.inViewport()) {
			statusProvider.get().update("Banking: Benedict is already in view");
			return RoguesDenSafeCracker.State.BANK_OPEN;
		}

		// Are we already headed towards the banker?
		Walk walk = walkFactory.createUsingLocalPath(banker);
		if (walk.isCloseEnoughOrOnTheWay()) {
			statusProvider.get().update("Banking: Already moving towards Benedict");
			return RoguesDenSafeCracker.State.BANK_OPEN;
		}

		// Try to walk until the banker is in viewport or "close enough"
		if (!walk.walkUntil(walkLogic.getWalkMethod(), Callables.inViewport(banker))) {
			return null;
		}

		return RoguesDenSafeCracker.State.BANK_OPEN;
	}
}
