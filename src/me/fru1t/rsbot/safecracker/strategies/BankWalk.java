package me.fru1t.rsbot.safecracker.strategies;

import org.powerbot.script.Area;
import org.powerbot.script.Tile;

import me.fru1t.common.annotations.Inject;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.script.rt6.Walk;

/**
 * TODO: Add bank interact while running
 */
public class BankWalk implements Strategy<RoguesDenSafeCracker.State> {
	private static final int RANDOMIZATION_TOLERANCE = 3;
	// TODO: Add correct area
	private static Area destinationArea = new Area(new Tile(0, 0), new Tile(0, 0));
	private static Tile[] fullPath = new Tile[] {

	};

	private final Walk walkUtil;

	@Inject
	public BankWalk(Walk.Factory walkingFactory) {
		walkUtil = walkingFactory.create(destinationArea, fullPath, RANDOMIZATION_TOLERANCE);
	}

	@Override
	public RoguesDenSafeCracker.State run() {
		return walkUtil.walk() ? RoguesDenSafeCracker.State.BANK_OPEN : null;
	}
}
