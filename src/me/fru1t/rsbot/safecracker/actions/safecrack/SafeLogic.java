package me.fru1t.rsbot.safecracker.actions.safecrack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.GameObject;

import me.fru1t.annotations.Inject;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.rsbot.utils.Random;

/**
 * Automatically selects an optimal safe to crack if settings.getPreferredSafe is set to 
 * Safe.AUTOMATIC.
 * 
 * <p>Consider:
 * Bot busters that occupy the same region to attempt to bait out the bot to switch safes.
 * Maybe only switch safes after x failures when in an occupied location >> What about a
 * crowded rogue's den?
 */
public class SafeLogic {
	/**
	 * The probability that the player will choose a random safe instead of a calculated one.
	 */
	private static final int RANDOM_SAFE_PROBABILITY = 27;
	
	/**
	 * All real safes
	 */
	private static final RoguesDenSafeCracker.Safe[] ALL_SAFES = {
			RoguesDenSafeCracker.Safe.NW, RoguesDenSafeCracker.Safe.NE,
			RoguesDenSafeCracker.Safe.SW, RoguesDenSafeCracker.Safe.SE };
	
	
	private final ClientContext ctx;
	private final Settings settings;
	private RoguesDenSafeCracker.Safe safe;

	@Inject
	public SafeLogic(ClientContext ctx, Settings settings) {
		this.ctx = ctx;
		this.settings = settings;
		newSafe();
	}
	
	/**
	 * @return The safe to crack.
	 */
	public RoguesDenSafeCracker.Safe getSafe() {
		return safe;
	}
	
	/**
	 * Generates a new optimal safe to crack. This should be called every bank cycle.
	 * 
	 * <p>Algorithm:
	 * RoguesDenSafeCracker.Safe (not including Safe.AUTOMATIC). Closest safe that has no
	 * occupancy. Returns a random empty safe RANDOM_SAFE_PROBABILITY%.
	 */
	public void newSafe() {
		if (settings.getPreferredSafe() != RoguesDenSafeCracker.Safe.AUTOMATIC) {
			safe = settings.getPreferredSafe();
			return;
		}
		
		if (Random.roll(RANDOM_SAFE_PROBABILITY)) {
			// Grab a random empty safe, or a random safe if none are empty
			List<RoguesDenSafeCracker.Safe> availableSafes = new ArrayList<>();
			for (RoguesDenSafeCracker.Safe safe : ALL_SAFES) {
				if (ctx.players.select().at(safe.playerLocation).size() == 0) {
					availableSafes.add(safe);
				}
			}
			safe = (availableSafes.size() == 0)
					? getRandomSafe()
					: availableSafes.get(Random.nextInt(0, availableSafes.size()));
		} else {
			// Grab the nearest empty safe, or a random safe if none are empty
			safe = null;
			Iterator<GameObject> goIter = ctx.objects
					.select()
					.id(RoguesDenSafeCracker.SAFE_OBJECT_ID)
					.nearest()
					.iterator();
			while(goIter.hasNext()) {
				GameObject go = goIter.next();
				RoguesDenSafeCracker.Safe s = RoguesDenSafeCracker.Safe.fromLocation(go);
				if (s != null && ctx.players.select().at(s.playerLocation).size() == 0) {
					safe = s;
					break;
				}
			}
			if (safe == null) {
				safe = getRandomSafe();
			}
		}
	}
	
	/**
	 * Simply retrieves a random safe.
	 * @return
	 */
	private RoguesDenSafeCracker.Safe getRandomSafe() {
		return ALL_SAFES[Random.nextInt(0, ALL_SAFES.length)];
	}
}
