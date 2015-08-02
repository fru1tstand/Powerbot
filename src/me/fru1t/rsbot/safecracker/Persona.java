package me.fru1t.rsbot.safecracker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.powerbot.script.Random;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.GameObject;

import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.framework.generics.GenericPersona;

public class Persona extends GenericPersona<ClientContext, Settings> {
	private static final int MAX_FOCUS = 100;
	private static final int MIN_FOCUS = 0;
	
	/**
	 * These values are used arbitrarily and range from 0 to 100, inclusive on both ends.
	 */
	private int attention;
	private int clumsy;

	public Persona() {
		EatMethod[] eatMethods = EatMethod.values();
		eatHealthThreshold_eatMethod = eatMethods[Random.nextInt(0, eatMethods.length)];
		eatHealthThreshold_eatToFull = Random.nextInt(0, 100) < 50;
	}
	
	//TODO: Create algo that models attention and clumsiness.
	protected void init() {
		this.attention = 100;
		this.clumsy = 0;
		
		backpackFillCountBeforeBanking(true);
		eatHealthThreshold(true);
		safeToCrack(true);
	}
	
	/**
	 * @return The person's current focus amount [0-100]
	 */
	private int getFocus() {
		return Math.max(Math.min(attention - clumsy, MAX_FOCUS), MIN_FOCUS);
	}
	
	
	private static final int backpackFillCountBeforeBanking_NOT_28_PROBABILITY = 15;
	private static final int backpackFillCountBeforeBanking_MIN_VALUE = 14;
	private static final int backpackFillCountBeforeBanking_MIN_FOCUS_CUTOFF = 50;
	private int backpackFillCountBeforeBanking_storedValue;
	/**
	 * Description:
	 * A normal person will wait until the inventory is full (count 28), but on occasion a person
	 * may haphazardly bank without a full inventory.
	 * 
	 * Trigger:
	 * Bank Cycle
	 * 
	 * Algorithm:
	 * [MIN_VALUE, 28] The chance the value will not be 28 is cumulatively
	 * [0, NOT_28_PROBABILITY]% correlated linearly on focus with a lower bound of
	 * CORRELATION_MAX_VALUE. When not 28, each valid value is given equal weight.
	 * 
	 * Justification:
	 * A person is lesser likely to randomly bank when not clumsy and more attentive. If a random
	 * banking event does occur, the count of items in the inventory is probably not skewed in
	 * either direction.
	 * 
	 * @param didTrigger If set to true, calculates a new number
	 * @return The number of items in the backpack before banking
	 */
	public int backpackFillCountBeforeBanking(boolean didTrigger) {
		if (didTrigger || backpackFillCountBeforeBanking_storedValue == 0) {
			backpackFillCountBeforeBanking_storedValue = 28;
			if (getFocus() < backpackFillCountBeforeBanking_MIN_FOCUS_CUTOFF) {
				int linearPercent = (int) 1.0
						* backpackFillCountBeforeBanking_NOT_28_PROBABILITY
						/ (MAX_FOCUS - backpackFillCountBeforeBanking_MIN_FOCUS_CUTOFF)
						* getFocus();
				if (Random.nextInt(0, 100) <= linearPercent) {
					backpackFillCountBeforeBanking_storedValue =
							Random.nextInt(backpackFillCountBeforeBanking_MIN_VALUE, 29);
				}
			}
		}
		return backpackFillCountBeforeBanking_storedValue;
	}
	
	public enum EatMethod { LOW_HP, CUTOFF_HP, N_FOOD }
	private static final int eatHealthThreshold_HARD_LOWER_LIMIT = 300;
	private final EatMethod eatHealthThreshold_eatMethod;
	private final boolean eatHealthThreshold_eatToFull;
	private int eatHealthThreshold_helper;
	private int eatHealthThreshold_storedValue;
	/**
	 * Description:
	 * Eating habits may vary from person to person. Several of these include eating when hp
	 * reaches as low as possible without dying then fully healing, eating whenever health drops
	 * below what the food item heals, eating when hp falls below a specific threshold, etc.
	 * 
	 * Trigger:
	 * Food eaten
	 * 
	 * Algorithm:
	 * [100, max_hp - food_heal_amt]
	 * LOW_HP - Threshold is very low HP ~[10-50%] w/ hard limit [HARD_LOWER_LIMIT, FOOD)
	 * CUTOFF_HP - Threshold is constant ~[60-90%] w/ hard limit [HARD_LOWER_LIMIT, FOOD)
	 * N_FOOD - Threshold is about what N number of food would heal w/ hard limit
	 * [HARD_LOWER_LIMIT, N Food)
	 * 
	 * Consider:
	 * The food may heal more than the max hp causing hard lower limit to always trigger. This may
	 * introduce a detectable pattern.
	 * 
	 * Justification:
	 * People have different eating habits. Some are fatter than others. 
	 * 
	 * @param didTrigger
	 * @return The HP threshold to eat at.
	 */
	public int eatHealthThreshold(boolean didTrigger) {
		if (didTrigger || eatHealthThreshold_storedValue == 0) {
			// Temporary: Simply random between 40-90%
			eatHealthThreshold_helper = Random.nextInt(40, 90);
			eatHealthThreshold_storedValue = (int)
					(eatHealthThreshold_helper / 100.0 * script().ctx.combatBar.health());

			// TODO: Implement correctly
//			switch(eatHealthThreshold_eatMethod) {
//			case LOW_HP:
//				eatHealthThreshold_helper = 2;
//				break;
//			case CUTOFF_HP:
//				break;
//			case N_FOOD:
//				break;
//			default:
//				break;
//			}
		}
		return Math.max(eatHealthThreshold_HARD_LOWER_LIMIT, eatHealthThreshold_storedValue);
	}
	
	/**
	 * @return If the player should eat until full HP
	 */
	public boolean eatToFull() {
		return eatHealthThreshold_eatToFull;
	}
	
	private static final int safeToCrack_RANDOM_SAFE_PROBABILITY = 27;
	private static final RoguesDenSafeCracker.Safe[] safeToCrack_ALL_SAFES = {
			RoguesDenSafeCracker.Safe.NW, RoguesDenSafeCracker.Safe.NE,
			RoguesDenSafeCracker.Safe.SW, RoguesDenSafeCracker.Safe.SE };
	private RoguesDenSafeCracker.Safe safeToCrack_storedValue;
	/**
	 * Description:
	 * Automatically selects an optimal safe to crack if settings.getPreferredSafe is set to 
	 * Safe.AUTOMATIC.
	 * 
	 * Trigger:
	 * Bank cycle
	 * 
	 * Algorithm:
	 * RoguesDenSafeCracker.Safe (not including Safe.AUTOMATIC). Closest safe that has no
	 * occupancy. Returns a random empty safe RANDOM_SAFE_PROBABILITY%.
	 * 
	 * Consider:
	 * Bot busters that occupy the same region to attempt to bait out the bot to switch safes.
	 * Maybe only switch safes after x failures when in an occupied location >> What about a
	 * crowded rogue's den?
	 * 
	 * Justification:
	 * No one cracks an occupied safe if there are available ones around it. People also suck at
	 * judging distances perfectly, so a random safe every once in a while doesn't hurt.
	 * @return The safe to crack
	 */
	public RoguesDenSafeCracker.Safe safeToCrack(boolean didTrigger) {
		if (script().settings.getPreferredSafe() != RoguesDenSafeCracker.Safe.AUTOMATIC)
			return script().settings.getPreferredSafe();
		
		List<RoguesDenSafeCracker.Safe> availableSafes = new ArrayList<>();
		if (didTrigger || safeToCrack_storedValue == null) {
			if (Random.nextInt(0, 100) < safeToCrack_RANDOM_SAFE_PROBABILITY) {
				// Grab a random empty safe, or a random safe if none are empty
				for (RoguesDenSafeCracker.Safe safe : safeToCrack_ALL_SAFES)
					if (script().ctx.players.select().at(safe.playerLocation).size() == 0)
						availableSafes.add(safe);
				if (availableSafes.size() == 0)
					safeToCrack_storedValue =
							safeToCrack_ALL_SAFES[Random.nextInt(0, safeToCrack_ALL_SAFES.length)];
				else
					safeToCrack_storedValue =
							availableSafes.get(Random.nextInt(0, availableSafes.size()));
			} else {
				// Grab the nearest empty safe, or a random safe if none are empty
				safeToCrack_storedValue = null;
				Iterator<GameObject> goIter = script().ctx.objects
						.select()
						.id(RoguesDenSafeCracker.SAFE_OBJECT_ID)
						.nearest()
						.iterator();
				while(goIter.hasNext()) {
					GameObject go = goIter.next();
					RoguesDenSafeCracker.Safe safe = RoguesDenSafeCracker.Safe.fromLocation(go);
					if (safe != null 
							&& script().ctx.players.select().at(safe.playerLocation).size() == 0) {
						safeToCrack_storedValue = safe;
						break;
					}
				}
				
				if (safeToCrack_storedValue == null) {
					safeToCrack_storedValue =
							safeToCrack_ALL_SAFES[Random.nextInt(0, safeToCrack_ALL_SAFES.length)];
				}
			}
		}
		return safeToCrack_storedValue;
	}
}
