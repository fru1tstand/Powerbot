package me.fru1t.rsbot.safecracker.actions;

import java.util.concurrent.Callable;

import org.powerbot.script.Tile;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.GameObject;

import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.framework.Action;
import me.fru1t.rsbot.safecracker.Persona;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.rsbot.safecracker.Persona.EatMethod;
import me.fru1t.rsbot.utils.Condition;
import me.fru1t.rsbot.utils.Random;
import me.fru1t.rsbot.utils.Timer;

public class SafeCrack extends Action<ClientContext, RoguesDenSafeCracker, Settings> {
	private GameObject cachedSafeToCrack;
	private int bankThreshold;
	private int healingThreshold;
	
	public SafeCrack(RoguesDenSafeCracker script) {
		super(script);
		cachedSafeToCrack = null;
		newBankThreshold();
	}

	@Override
	public boolean run() {
		// Bank run?
		// TODO: Add - Gamble (interact even when inventory is full)
		// TODO: Add - Eat food to open inventory space
		// Things to consider: More likely to gamble or eat to clear inventory when near a new
		// level?
		if (script.ctx.backpack.select().count() >= bankThreshold) {
			newBankThreshold();
			script.updateState(RoguesDenSafeCracker.State.BANK_WALK);
			return true;
		}
		
		// Health low?
		if (script.ctx.combatBar.health() < script.persona.healingThreshold(false)) {
			script.persona.healingThreshold(true);
			script.updateState(RoguesDenSafeCracker.State.SAFE_EAT);
			return true;
		}
		
		// Interact with safe
		RoguesDenSafeCracker.Safe safe = script.persona.safeToCrack(false);
		if (script.persona.smartClick()
				&& script.ctx.menu.items()[0].equals(RoguesDenSafeCracker.MENU_CRACK_ACTIVE_TEXT)) {
			script.ctx.input.click(true);
		} else {
			if (cachedSafeToCrack == null || !safe.location.equals(cachedSafeToCrack.tile())) {
				cachedSafeToCrack = script.ctx.objects
						.select()
						.at(safe.location)
						.id(RoguesDenSafeCracker.SAFE_OBJECT_ID)
						.poll();
				cachedSafeToCrack.bounds(RoguesDenSafeCracker.SAFE_OBJECT_BOUNDS_MODIFIER);
			}
			// Already cracked safe? Other issues?
			if (cachedSafeToCrack == null || !cachedSafeToCrack.valid()) {
				return false;
			}
			// TODO: Implement misclick
			if (script.persona.safeMisclick()) { }
			if (!script.persona.misclickInstantRecovery()) { }
			
			cachedSafeToCrack.click();
		}
		
		// Impatient clicking
		int impatientClicking = script.persona.safeClickCount(false);
		while (impatientClicking > 1 // First click already happened
				&& script.ctx.menu.items()[0].equals(RoguesDenSafeCracker.MENU_CRACK_ACTIVE_TEXT)) {
			script.ctx.input.click(true);
			Condition.sleep(script.persona.clickSpamDelay());
		}
		
		// Safety check
		if (script.ctx.movement.destination() != Tile.NIL
				&& script.ctx.players.local().tile().equals(cachedSafeToCrack.tile())) {
			script.updateState(RoguesDenSafeCracker.State.SAFE_WALK); // Oops.
			return false;
		}
		
		// TODO: Add human factor
		
		// Waiting for the player to interact
		if (!Condition.wait(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return script.ctx.players.local().animation()
						== RoguesDenSafeCracker.PLAYER_CRACK_ANIMATION;
			}
		}, 100, 10)) // 1000 ms
			return false;
		
		// TODO: Add human factor
		
		// Waiting for the player to success or fail
		Timer safecrackAnimationTimer = new Timer(script.ctx, 2000);
		if (!Condition.wait(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				// If we know we're doing something, go ahead and wait longer
				if (script.ctx.players.local().animation()
						== RoguesDenSafeCracker.PLAYER_CRACK_ANIMATION)
					safecrackAnimationTimer.reset();
				return !cachedSafeToCrack.valid()
						|| script.ctx.players.local().animation() 
								== RoguesDenSafeCracker.PLAYER_CRACK_PRE_HURT_ANIMATION
						|| script.ctx.players.local().animation()
								== RoguesDenSafeCracker.PLAYER_HURTING_ANIMATION;
			}
		}, safecrackAnimationTimer, 150)) // "2000 ms"
			return false;
		
		// TODO: Add human factor
		
		// Wait for safe reset
		if (!Condition.wait(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return cachedSafeToCrack.valid();
			}
		}, 300, 7)) // 2100 ms
			return false;
		
		return true;
	}
	
	
	/**
	 * The minimum amount of focus a player can have before starting to randomly bank.
	 */
	private static final int bankThreshold_FOCUS_CUTOFF = 50;
	/**
	 * The probability that the player will bank without reaching a full inventory.
	 */
	private static final int bankThreshold_NOT_28_PROBABILITY = 15;
	/**
	 * The minimum number of items a player can be holding before banking.
	 */
	private static final int bankThreshold_MIN_VALUE = 14;
	/**
	 * A normal person will wait until the inventory is full (count 28), but on occasion a person
	 * may haphazardly bank without a full inventory.
	 * 
	 * Change trigger:
	 * Bank event
	 * 
	 * Consider:
	 * A person is less likely to randomly bank when not clumsy and more attentive. If a random
	 * banking event does occur, the count of items in the inventory is probably not skewed in
	 * either direction.
	 */
	private void newBankThreshold() {
		bankThreshold = 28;
		
		// Focus cutoff
		if (script.persona.focus() > bankThreshold_FOCUS_CUTOFF)
			return;
		
		// Not 28 probability roll
		if (!Random.roll(bankThreshold_NOT_28_PROBABILITY))
			return;
		
		// Focus roll ( < FOCUS_CUTOFF% )
		if (Random.roll(script.persona.focus()))
			return;
		
		bankThreshold = Random.nextInt(bankThreshold_MIN_VALUE, 29);
	}
	
	private enum EatMethod { LOWEST_POSSIBLE, FOOD_ORIENTED, RANDOM }
	private static final int healingThreshold_IS_CONSTANT_PROB = 25;
	private static final int healingThreshold_LOWEST_MIN = 110;
	private static final int healingThreshold_LOWEST_MAX = 300;
	private static final int healingThreshold_ALGO_LOWEST_PROB = 10;
	private static final int healingThreshold_ALGO_FOOD_PROB = 50;
	private static final double healingThreshold_FOOD_VAR_MIN = 0.5;
	private static final double healingThreshold_FOOD_VAR_MAX = 5;
	private static final int healingThreshold_ABS_MIN = 500;
	private EatMethod healingThreshold_eatMethod;
	private boolean healingThreshold_isConstant;
	/**
	 * Description:
	 * Eating habits may vary from person to person. Several of these include eating when hp
	 * reaches as low as possible without dying then fully healing, eating whenever health drops
	 * below what the food item heals, eating when hp falls below a specific threshold, etc.
	 * 
	 * Trigger:
	 * Food eat event
	 * 
	 * Configure:
	 * (Algorithm never changes within a single script run)
	 * NEVER (NEVER_CONFIGURE)% || ALWAYS (100 - NEVER_CONFIGURE)%
	 * 
	 * Algorithms:
	 * "Lowest Possible" - Random [LOWEST_MIN, LOWEST_MAX] HP
	 * 		ALGO_LOWEST_PROB% Probability
	 * 		Force enabled when food healing > player's max HP
	 * 
	 * "Food Oriented" - Unimodal skewed left (tends to heal closer to 100%) from the normal dist
	 * N(maxHealHealth, random(FOOD_VAR_MIN, FOOD_VAR_MAX)). Anything not contained within the
	 * range (ABS_MIN, maxHealHealth) is rerolled, creating a skew. With variance at least 0.5,
	 * exact mean will never be chosen > 60% of the time.
	 * 		ALGO_FOOD_PROB% Probability
	 * 
	 * "Random" - Plain ol' random range [ABS_MIN, MAX_HP - FOOD_HEAL]
	 * 		(100 - lowest - food)% Probability
	 * 
	 * Justification:
	 * People have different eating habits. Some are fatter than others. 
	 */
	private void newHealingThreshold() {
		int foodHealAmt = script().settings.getCurrentFood().healAmount;
		int maxHealHealth = script().ctx.combatBar.maximumHealth() - foodHealAmt;
		switch (healingThreshold_eatMethod) {
		case LOWEST_POSSIBLE:
			healingThreshold_storedValue =
					Random.nextInt(healingThreshold_LOWEST_MIN, healingThreshold_LOWEST_MAX);
			break;
		case RANDOM:
			healingThreshold_storedValue =
					Random.nextInt(healingThreshold_ABS_MIN, maxHealHealth);
			break;
		case FOOD_ORIENTED:
		default:
			healingThreshold_storedValue = 0;
			// Theoretically this could go on forever...
			while (healingThreshold_storedValue > maxHealHealth
					|| healingThreshold_storedValue < healingThreshold_ABS_MIN) {
				healingThreshold_storedValue = Random.nextGaussian(
						healingThreshold_ABS_MIN,
						maxHealHealth, // Anything larger is floored to this and rerolled
						maxHealHealth,
						(int) Math.sqrt(Random.nextDouble(
								healingThreshold_FOOD_VAR_MIN,
								healingThreshold_FOOD_VAR_MAX)));
			}
			break;
		}
	}
	private void healingThreshold_setup() {
		healingThreshold_eatMethod = EatMethod.RANDOM;
		int rnd = Random.nextInt(0, 100);
		if (rnd < healingThreshold_ALGO_LOWEST_PROB)
			healingThreshold_eatMethod = EatMethod.LOWEST_POSSIBLE;
		else if (rnd < healingThreshold_ALGO_LOWEST_PROB + healingThreshold_ALGO_FOOD_PROB)
			healingThreshold_eatMethod = EatMethod.FOOD_ORIENTED;
		
		if (script().settings.getCurrentFood().healAmount > script().ctx.combatBar.maximumHealth())
			healingThreshold_eatMethod = EatMethod.LOWEST_POSSIBLE;
		
		healingThreshold_isConstant = false;
		healingThreshold(true); // Set before isConstant
		rnd = Random.nextInt(0, 100);
		if (rnd < healingThreshold_IS_CONSTANT_PROB)
			healingThreshold_isConstant = true;
	}
}
