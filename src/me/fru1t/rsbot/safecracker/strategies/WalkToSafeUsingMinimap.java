package me.fru1t.rsbot.safecracker.strategies;

import java.util.concurrent.Callable;

import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.LocalPath;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.RoguesDenSafeCracker.State;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.script.rt6.Backpack;
import me.fru1t.rsbot.common.script.rt6.Walk;
import me.fru1t.rsbot.safecracker.strategies.logic.SafeLogic;

public class WalkToSafeUsingMinimap implements Strategy<RoguesDenSafeCracker.State> {
	private final ClientContext ctx;
	private final Backpack backpack;
	private final SafeLogic safeLogic;
	private final Walk.Factory walkFactory;

	@Inject
	public WalkToSafeUsingMinimap(
			@Singleton ClientContext ctx,
			@Singleton Backpack backpack,
			@Singleton SafeLogic safeLogic,
			Walk.Factory walkFactory) {
		this.ctx = ctx;
		this.backpack = backpack;
		this.safeLogic = safeLogic;
		this.walkFactory = walkFactory;
	}

	@Override
	public State run() {
		LocalPath path = ctx.movement.findPath(safeLogic.getSafe().location);
		Walk walk = walkFactory.create(path);
		if (backpack.isFull()) {
			return State.BANK_WALK;
		}

		if (!walk.walkUntil(new Callable<Boolean>() {
					@Override
					public Boolean call() {
						return ctx.objects
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
