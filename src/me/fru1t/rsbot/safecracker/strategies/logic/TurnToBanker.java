package me.fru1t.rsbot.safecracker.strategies.logic;

import org.powerbot.script.rt6.Npc;

import me.fru1t.annotations.Inject;
import me.fru1t.rsbot.common.util.Random;

/**
 * The banker must be on screen, but even if the banker is on screen, there is a chance that a
 * someone will change the camera angle to make the banker more in view.
 */
public class TurnToBanker {
	private static final int IS_ENABLED_PROBABILITY = 50;
	private static final int ALWAYS_PROBABILITY = 20;
	
	private final boolean isEnabled;
	private final boolean isAlways;
	private final int probability;
	
	@Inject
	public TurnToBanker() {
		isEnabled = Random.roll(IS_ENABLED_PROBABILITY);
		isAlways = Random.roll(ALWAYS_PROBABILITY);
		probability = Random.nextInt(0, 100);
	}
	
	/**
	 * @param banker The banker NPC.
	 * @return Returns whether or not the player should change the camera angle to have the banker
	 * on screen.
	 */
	public boolean should(Npc banker) {
		// Banker MUST be on screen
		if (!banker.inViewport()) {
			return true;
		}
		
		if (!isEnabled) {
			return false;
		}
		
		if (isAlways || Random.roll(probability)) {
			return true;
		}
		
		return false;
	}
}
