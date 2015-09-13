package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.common.annotations.Singleton;
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
 * TODO: Add bank interact while running
 */
public class BankWalk implements Strategy<RoguesDenSafeCracker.State> {
	private static final int RANDOMIZATION_TOLERANCE = 3;
	// TODO(v1): Add correct area
	private static Area destinationArea = new Area(new Tile(0, 0), new Tile(0, 0));
	private static Tile[] fullPath = new Tile[] {

	};

	private final Walk.Factory walkFactory;
	private final Provider<ClientContext> ctxProvider;
	private final WalkLogic walkLogic;

	@Inject
	public BankWalk(
			Provider<ClientContext> ctxProvider,
			Walk.Factory walkingFactory,
			@Singleton WalkLogic walkLogic) {
		this.ctxProvider = ctxProvider;
		this.walkFactory = walkingFactory;
		this.walkLogic = walkLogic;
	}

	@Override
	public RoguesDenSafeCracker.State run() {
		Npc banker = ctxProvider.get().npcs.select().id(OpenBank.BANKER_NPC_ID).poll();
		if (!banker.valid()) {
			return null;
		}

		if (banker.inViewport()) {
			return RoguesDenSafeCracker.State.BANK_OPEN;
		}

		Walk walk = walkFactory.createUsingLocalPath(banker);
		if (walk.isCloseEnoughOrOnTheWay()) {
			return RoguesDenSafeCracker.State.BANK_OPEN;
		}

		// Try to walk until the banker is in viewport or "close enough"
		if (!walk.walkUntil(walkLogic.getWalkMethod(), Callables.inViewport(banker))) {
			return null;
		}

		return RoguesDenSafeCracker.State.BANK_OPEN;
	}
}
