package me.fru1t.rsbot.safecracker.actions;

import org.powerbot.script.Area;
import org.powerbot.script.Tile;

import me.fru1t.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.framework.Action;
import me.fru1t.rsbot.common.framework.actions.Rs3Walking;
import me.fru1t.rsbot.common.framework.components.RunState;

/**
 * TODO: Add bank interact while running
 */
public class BankWalk implements Action {
	private static final int RANDOMIZATION_TOLERANCE = 3;
	// TODO: Add correct area
	private static Area destinationArea = new Area(new Tile(0, 0), new Tile(0, 0));
	private static Tile[] fullPath = new Tile[] {
			
	};
	
	private final RunState<RoguesDenSafeCracker.State> state;
	private final Rs3Walking walker;
	
	public BankWalk(
			@Singleton RunState<RoguesDenSafeCracker.State> state,
			Rs3Walking.Factory walkingFactory) {
		this.state = state;
		walker = walkingFactory.create(destinationArea, fullPath, RANDOMIZATION_TOLERANCE);
	}

	@Override
	public boolean run() {
		if (!walker.run()) {
			return false;
		}
		
		state.update(RoguesDenSafeCracker.State.BANK_OPEN);
		return true;
	}
}
