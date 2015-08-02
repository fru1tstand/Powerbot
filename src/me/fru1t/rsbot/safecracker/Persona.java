package me.fru1t.rsbot.safecracker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.powerbot.script.Random;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.GameObject;

import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.framework.generics.GenericPersona;
import me.fru1t.rsbot.utils.Timer;

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
	
	// TODO: Create algo that models attention and clumsiness that is sinusoidally dependent over
	// time.
	protected void init() {
		this.attention = 100;
		this.clumsy = 0;
		
		backpackFillCountBeforeBanking(true);
		eatHealthThreshold(true);
		safeToCrack(true);
		smartClick();
		safeMisclick();
		safeClickCount(true);
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
				
			// TODO: Implement correct + Add food data in AllFoods
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
	
	private static final int smartClick_ABS_MIN = 0;
	private static final int smartClick_ABS_MAX = 300; // 5 minutes in seconds
	private static final int smartClick_MAX_MEAN = 45;
	private static final int smartClick_MIN_VAR = 1;
	private static final int smartClick_MAX_VAR = 10;
	private Timer smartClick_timer;
	/**
	 * Description:
	 * Re-engage crack safe without moving the mouse. (ie. Mouse.click instead of object.interact)
	 * 
	 * Trigger:
	 * Timer
	 * 
	 * Algorithm:
	 * Normal distribution. Mean [0, MAX_MEAN] proportional to focus. Variance [MIN_VAR, MAX_VAR]
	 * proportional to focus.
	 * 
	 * Justification:
	 * A user doesn't constantly move the mouse every #interact, and vis versa, don't constantly
	 * NOT move the mouse. Depending on a large number of factors, a player may or may not move
	 * the mouse during an interact if the mouse is already hovered over the safe.
	 * @return Whether or not the user should smart click
	 */
	public boolean smartClick() {
		if (smartClick_timer == null) {
			smartClick_timer = new Timer(script().ctx);
		}
		if (smartClick_timer.hasExpired()) {
			// Gauss gets the value in seconds * 1000 ms/s
			smartClick_timer.set(1000 * Random.nextGaussian(
					smartClick_ABS_MIN,
					smartClick_ABS_MAX,
					smartClick_MAX_MEAN / 100 * getFocus(),
					Math.sqrt((smartClick_MAX_VAR - smartClick_MIN_VAR) / 100 * getFocus())));
			return false;
		}
		return true;
	}
	
	private static final int safeMisclick_ABS_MIN = 0;
	private static final int safeMisclick_ABS_MAX = 120;
	private static final int safeMisclick_MAX_VAR = 15;
	private static final int safeMisclick_MIN_VAR = 3;
	private static final int safeMisclick_MAX_MEAN = 50;
	private Timer safeMisclick_timer;
	/**
	 * Description:
	 * Mis-click safe when cracking. Increases attention when true.
	 * 
	 * Trigger:
	 * Timer
	 * 
	 * Algorithm:
	 * Normal distribution. Mean [0, MAX_MEAN] inversely proportional to focus. Variances
	 * [MIN_VAR, MAX_VAR] inversely proportional to focus.
	 * 
	 * Justification:
	 * Humans aren't perfect. The less focused you are, the more likely you are of mis-clicking and
	 * correcting the mis-click which would increase attention a little.
	 * @return
	 */
	public boolean safeMisclick() {
		if (safeMisclick_timer == null) {
			safeMisclick_timer = new Timer(script().ctx);
		}
		if (smartClick_timer.hasExpired()) {
			smartClick_timer.set(1000 * Random.nextGaussian(
					safeMisclick_ABS_MIN,
					safeMisclick_ABS_MAX,
					safeMisclick_MAX_MEAN * 100 / getFocus(),
					Math.sqrt((safeMisclick_MAX_VAR - safeMisclick_MIN_VAR) * 100 / getFocus())));
			return false;
		}
		return true;
	}
	
	private static final int misclickInstantRecovery_UPPER_BOUND = 75;
	/**
	 * Description:
	 * Skip waiting for the player to react when misclicking
	 * 
	 * Trigger:
	 * Misclick event
	 * 
	 * Algorithm:
	 * Linearly proportional to focus with an upper bound of UPPER_BOUND.
	 * 
	 * Justification:
	 * The lesser focused someone is, the less they'll catch a misclick.
	 * @return If the recovery should be instant.
	 */
	public boolean misclickInstantRecovery() {
		return Random.nextInt(0, misclickInstantRecovery_UPPER_BOUND) <= getFocus();
	}
	
	private static final int safeClickCount_MAX_CLICKS = 5;
	private static final int safeClickCount_ENABLE_PROBABILITY = 25;
	private boolean safeClickCount_isEnabled;
	private double safeClickCount_variance;
	private int safeClickCount_mean;
	private static final int safeClickCount_MIN_CLICK_DELAY = 50; // ms
	private static final int safeClickCount_MAX_CLICK_DELAY = 300; // ms
	private static final double safeClickCount_MAX_VAR = 0.8;
	private static final double safeClickCount_MIN_VAR = 0.15;
	private static final int safeClickCount_MAX_MEAN = 175;
	/**
	 * Description:
	 * The most impatient ones will click more than 1 time.
	 * 
	 * Trigger:
	 * Never (script start)
	 * 
	 * Algorithm:
	 * Number of clicks is [1, MAX_CLICKS] where each value is equally weighted random. The delay
	 * between the clicks is a normal distribution with randomly chosen mean of
	 * [MIN_CLICK_DELAY, MAX_MEAN] and a randomly chosen variance of [MIN_VAR, MAX_VAR]
	 * 
	 * Justification:
	 * Someone sporadically clicking will not know how many times they've clicked (or care to
	 * click a consistent amount every time). The delay tends toward a unimodal symmetric normal
	 * distribution (n = 300). However, because people are different, the mean and variance of
	 * these curve are different for each person, thus the randomly generated distribution. 
	 * 
	 * Consider:
	 * Someone may become impatient, or fall out of impatience. Also, as time wears on, fatigue may
	 * build up reducing both click count and click delay mean.
	 * @return
	 */
	public int safeClickCount(boolean didTrigger) {
		if (didTrigger) {
			safeClickCount_isEnabled = 
					(Random.nextInt(0, 100) > safeClickCount_ENABLE_PROBABILITY);
			safeClickCount_variance =
					Random.nextDouble(safeClickCount_MIN_VAR, safeClickCount_MAX_VAR);
			safeClickCount_mean = Random.nextInt(1, safeClickCount_MAX_MEAN);
		}
		if (!safeClickCount_isEnabled)
			return 1;
		return Random.nextInt(1, safeClickCount_MAX_CLICKS);
	}
	
	/**
	 * @see #safeClickCount(boolean)
	 * @return The delay between clicks
	 */
	public int safeClickCountDelay() {
		return Random.nextGaussian(
				safeClickCount_MIN_CLICK_DELAY,
				safeClickCount_MAX_CLICK_DELAY,
				safeClickCount_mean,
				safeClickCount_variance);
	}
}
