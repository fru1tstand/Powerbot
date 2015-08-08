package me.fru1t.rsbot.safecracker.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
	/**
	 * Defines the different styles of eating
	 */
	private enum EatMethod { LOWEST_POSSIBLE, FOOD_ORIENTED, RANDOM }
	private GameObject cachedSafeObject;
	private RoguesDenSafeCracker.Safe safeToCrack;
	private int bankThreshold;
	private int healingThreshold;
	private EatMethod healingThreshold_eatMethod;
	private boolean healingThreshold_isConstant;
	private Timer smartClickTimer;
	private int safecrackClickCount;
	
	public SafeCrack(RoguesDenSafeCracker script) {
		super(script);
		safeToCrack = null;
		
		// Health
		healingThreshold_eatMethod = EatMethod.RANDOM;
		int rnd = Random.nextInt(0, 100);
		if (rnd < healingThreshold_ALGO_LOWEST_PROB)
			healingThreshold_eatMethod = EatMethod.LOWEST_POSSIBLE;
		else if (rnd < healingThreshold_ALGO_LOWEST_PROB + healingThreshold_ALGO_FOOD_PROB)
			healingThreshold_eatMethod = EatMethod.FOOD_ORIENTED;
		if (script.settings.getCurrentFood().healAmount >= script.ctx.combatBar.maximumHealth())
			healingThreshold_eatMethod = EatMethod.LOWEST_POSSIBLE;
		healingThreshold_isConstant = Random.nextInt(0, 100) < healingThreshold_IS_CONSTANT_PROB;
		
		smartClickTimer = new Timer(script.ctx);
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
		if (script.ctx.combatBar.health() < healingThreshold) {
			newHealingThreshold();
			script.updateState(RoguesDenSafeCracker.State.SAFE_EAT);
			return true;
		}
		
		// Interact with safe
		if (smartClick()
				&& script.ctx.menu.items()[0].equals(RoguesDenSafeCracker.MENU_CRACK_ACTIVE_TEXT)) {
			// Consider: Do we always want to check that the menu is to crack the safe?
			script.ctx.input.click(true);
		} else {
			if (cachedSafeObject == null || !safeToCrack.location.equals(cachedSafeObject.tile())) {
				cachedSafeObject = script.ctx.objects
						.select()
						.at(safeToCrack.location)
						.id(RoguesDenSafeCracker.SAFE_OBJECT_ID)
						.poll();
				cachedSafeObject.bounds(RoguesDenSafeCracker.SAFE_OBJECT_BOUNDS_MODIFIER);
			}
			// Already cracked safe? Other issues?
			if (cachedSafeObject == null || !cachedSafeObject.valid()) {
				return false;
			}
			
			// TODO: Implement misclick
//			if (script.persona.safeMisclick()) { }
//			if (!script.persona.misclickInstantRecovery()) { }
			
			cachedSafeObject.click();
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
				&& script.ctx.players.local().tile().equals(cachedSafeObject.tile())) {
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
	
	private static final int healingThreshold_IS_CONSTANT_PROB = 25;
	private static final int healingThreshold_LOWEST_MIN = 110;
	private static final int healingThreshold_LOWEST_MAX = 300;
	private static final int healingThreshold_ALGO_LOWEST_PROB = 10;
	private static final int healingThreshold_ALGO_FOOD_PROB = 50;
	private static final double healingThreshold_FOOD_VAR_MIN = 0.5;
	private static final double healingThreshold_FOOD_VAR_MAX = 5;
	private static final int healingThreshold_ABS_MIN = 500;
	/**
	 * Eating habits may vary from person to person. Several of these include eating when hp
	 * reaches as low as possible without dying then fully healing, eating whenever health drops
	 * below what the food item heals, eating when hp falls below a specific threshold, etc.
	 * 
	 * Trigger:
	 * Food eat event
	 * 
	 * Justification:
	 * People have different eating habits. Some are fatter than others. 
	 */
	private void newHealingThreshold() {
		// This could possibly be incorrect depending on EOC or 
		int maxHealHealth =
				script.ctx.combatBar.maximumHealth() - script.settings.getCurrentFood().healAmount;
		switch (healingThreshold_eatMethod) {
		case LOWEST_POSSIBLE:
			// Don't heal until very low hp
			healingThreshold =
					Random.nextInt(healingThreshold_LOWEST_MIN, healingThreshold_LOWEST_MAX);
			break;
		case RANDOM:
			// Heal at a random health
			healingThreshold = Random.nextInt(healingThreshold_ABS_MIN, maxHealHealth);
			break;
		case FOOD_ORIENTED:
		default:
			// Keep near 100% hp
			healingThreshold = 0;
			// Theoretically this could go on forever...
			while (healingThreshold > maxHealHealth
					|| healingThreshold < healingThreshold_ABS_MIN) {
				healingThreshold = Random.nextGaussian(
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
	
	private static final int smartClick_ABS_MIN = 0;
	private static final int smartClick_ABS_MAX = 300; // 5 minutes in seconds
	private static final int smartClick_MAX_MEAN = 45;
	private static final int smartClick_MIN_VAR = 1;
	private static final int smartClick_MAX_VAR = 10;
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
		// When the timer is active, smart click
		if (!smartClickTimer.hasExpired())
			return true;
		
		// Gauss gets the value in seconds * 1000 ms/s
		smartClickTimer.set(1000 * Random.nextGaussian(
				smartClick_ABS_MIN,
				smartClick_ABS_MAX,
				smartClick_MAX_MEAN / 100 * script.persona.focus(),
				Math.sqrt((smartClick_MAX_VAR - smartClick_MIN_VAR) / 100 * script.persona.focus())));
		return false;
		return true;
	}
	
	private static final int safeToCrack_RANDOM_SAFE_PROBABILITY = 27;
	private static final RoguesDenSafeCracker.Safe[] safeToCrack_ALL_SAFES = {
			RoguesDenSafeCracker.Safe.NW, RoguesDenSafeCracker.Safe.NE,
			RoguesDenSafeCracker.Safe.SW, RoguesDenSafeCracker.Safe.SE };
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
	public void newSafeToCrack() {
		List<RoguesDenSafeCracker.Safe> availableSafes = new ArrayList<>();
		if (safeToCrack == null) {
			if (Random.nextInt(0, 100) < safeToCrack_RANDOM_SAFE_PROBABILITY) {
				// Grab a random empty safe, or a random safe if none are empty
				for (RoguesDenSafeCracker.Safe safe : safeToCrack_ALL_SAFES)
					if (script.ctx.players.select().at(safe.playerLocation).size() == 0)
						availableSafes.add(safe);
				if (availableSafes.size() == 0)
					safeToCrack =
							safeToCrack_ALL_SAFES[Random.nextInt(0, safeToCrack_ALL_SAFES.length)];
				else
					safeToCrack =
							availableSafes.get(Random.nextInt(0, availableSafes.size()));
			} else {
				// Grab the nearest empty safe, or a random safe if none are empty
				safeToCrack = null;
				Iterator<GameObject> goIter = script.ctx.objects
						.select()
						.id(RoguesDenSafeCracker.SAFE_OBJECT_ID)
						.nearest()
						.iterator();
				while(goIter.hasNext()) {
					GameObject go = goIter.next();
					RoguesDenSafeCracker.Safe safe = RoguesDenSafeCracker.Safe.fromLocation(go);
					if (safe != null
							&& script.ctx.players.select().at(safe.playerLocation).size() == 0) {
						safeToCrack = safe;
						break;
					}
				}
				
				if (safeToCrack == null) {
					safeToCrack =
							safeToCrack_ALL_SAFES[Random.nextInt(0, safeToCrack_ALL_SAFES.length)];
				}
			}
		}
	}
	
	private static final int safeClickCount_MAX_CLICKS = 5;
	private static final int safeClickCount_ENABLE_PROBABILITY = 25;
	private boolean safeClickCount_isEnabled;
	private static final int safeClickCount_MAX_MEAN = 175;
	/**
	 * Description:
	 * The most impatient ones will click more than 1 time.
	 * 
	 * Trigger:
	 * Never (script start)
	 * 
	 * Algorithm:
	 * Number of clicks is [1, MAX_CLICKS] where each value is equally weighted random. 
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
	public void newSafecrackClickCount() {
		safecrackClickCount = 1;
		
		if (!safecrackClickCount_isEnabled)
			return;
		safecrackClickCount = Random.nextInt(1, safeClickCount_MAX_CLICKS);
		if (didTrigger) {
			safeClickCount_isEnabled = Random.nextInt(0, 100) > safeClickCount_ENABLE_PROBABILITY;
		}
		if (!safeClickCount_isEnabled)
			return 1;
		return Random.nextInt(1, safeClickCount_MAX_CLICKS);
	}
}
