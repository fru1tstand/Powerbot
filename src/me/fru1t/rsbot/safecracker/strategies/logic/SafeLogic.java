package me.fru1t.rsbot.safecracker.strategies.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.fru1t.common.annotations.Nullable;
import me.fru1t.slick.util.Provider;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.GameObject;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.util.Random;
import me.fru1t.rsbot.safecracker.Settings;
import org.powerbot.script.rt6.Objects;

/**
 * Automatically selects an optimal safe to crack if settingsProvider.getPreferredSafe is set to
 * null
 *
 * <p>Consider:
 * Bot busters that occupy the same region to attempt to bait out the bot to switch safes.
 * Maybe only switch safes after x failures when in an occupied location >> What about a
 * crowded rogue's den?
 */
@Singleton
public class SafeLogic {
	/**
	 * The probability that the player will choose a random safe instead of a calculated one.
	 */
	private static final int RANDOM_SAFE_PROBABILITY = 27;

	private final Provider<ClientContext> ctxProvider;
	private final Provider<Settings> settingsProvider;
	@Nullable
	private RoguesDenSafeCracker.Safe safe;
	@Nullable
	private GameObject safeGameObject;

	@Inject
	public SafeLogic(
			Provider<ClientContext> contextProvider,
			Provider<Settings> settingsProvider) {
		this.ctxProvider = contextProvider;
		this.settingsProvider = settingsProvider;
		this.safeGameObject = null;
	}

	/**
	 * @return The safe to crack.
	 */
	public RoguesDenSafeCracker.Safe getSafe() {
		if (safe == null) {
			newSafe();
		}
		return safe;
	}

	/**
	 * Returns the safe that should be used for interaction. Potentially returns GameObject.nil
	 * if the safe couldn't be retrieved.
	 *
	 * @return The safe GameObject.
	 */
	public GameObject getSafeGameObject() {
		if (safeGameObject == null
				|| !safeGameObject.valid()
				|| !safeGameObject.tile().equals(getSafe().location)) {
			safeGameObject = ctxProvider.get().objects.select()
					.id(RoguesDenSafeCracker.SAFE_OBJECT_ID)
					.at(getSafe().location)
					.poll();
			safeGameObject.bounds(RoguesDenSafeCracker.SAFE_OBJECT_BOUNDS_MODIFIER);
		}
		return safeGameObject;
	}

	/**
	 * Returns if the player is standing at the current safe.
	 *
	 * @return If the player is standing at the current safe.
	 */
	public boolean isAtSafe() {
		return safe.playerLocation.equals(ctxProvider.get().players.local().tile());
	}

	/**
	 * Returns if the player is standing at or the player's destination is at the current safe.
	 *
	 * @return If the player is standing at or the player's destination is at the current safe.
	 */
	public boolean isAtOrMovingTowardsSafe() {
		return isAtOrMovingTowardsSafeWithTolerance(0);
	}

	/**
	 * Returns if the player's distance or the destination's distance to the safe is within the
	 * given distance.
	 *
	 * @param distance The distance tolerance to use. This value is inclusive. Eg. If passed 0 and
	 * the player is exactly on the tile, this method would return true.
	 * @return If the player's distance or the destination's distance to the safe is within the
	 * given distance.
	 */
	public boolean isAtOrMovingTowardsSafeWithTolerance(int distance) {
		if (safe.playerLocation.distanceTo(ctxProvider.get().players.local()) <= distance) {
			return true;
		}
		if (safe.playerLocation.distanceTo(ctxProvider.get().movement.destination()) <= distance) {
			return true;
		}

		return false;
	}

	/**
	 * Generates a new optimal safe to crack. This should be called every bank cycle.
	 *
	 * <p>Algorithm:
	 * RoguesDenSafeCracker.Safe (not including Safe.AUTOMATIC). Closest safe that has no
	 * occupancy. Returns a random empty safe RANDOM_SAFE_PROBABILITY%.
	 */
	public void newSafe() {
		if (settingsProvider.get().getPreferredSafe() != null) {
			safe = settingsProvider.get().getPreferredSafe();
			return;
		}

		if (Random.roll(RANDOM_SAFE_PROBABILITY)) {
			// Grab a random empty safe, or a random safe if none are empty
			List<RoguesDenSafeCracker.Safe> availableSafes =
					new ArrayList<RoguesDenSafeCracker.Safe>();
			for (RoguesDenSafeCracker.Safe safe : RoguesDenSafeCracker.Safe.values()) {
				if (ctxProvider.get().players.select().at(safe.playerLocation).size() == 0) {
					availableSafes.add(safe);
				}
			}
			safe = (availableSafes.size() == 0)
					? getRandomSafe()
					: availableSafes.get(Random.nextInt(0, availableSafes.size()));
		} else {
			// Grab the nearest empty safe, or a random safe if none are empty
			safe = null;
			Iterator<GameObject> goIter = ctxProvider.get().objects
					.select()
					.id(RoguesDenSafeCracker.SAFE_OBJECT_ID)
					.nearest()
					.iterator();
			while(goIter.hasNext()) {
				GameObject go = goIter.next();
				RoguesDenSafeCracker.Safe s = RoguesDenSafeCracker.Safe.fromLocation(go);
				if (s != null && ctxProvider.get().players.select().at(s.playerLocation).size() == 0) {
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
		return RoguesDenSafeCracker.Safe
				.values()[Random.nextInt(0, RoguesDenSafeCracker.Safe.values().length)];
	}
}
