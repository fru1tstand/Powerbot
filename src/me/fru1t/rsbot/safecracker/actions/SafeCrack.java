package me.fru1t.rsbot.safecracker.actions;

import java.util.concurrent.Callable;

import org.powerbot.script.Tile;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.GameObject;

import me.fru1t.annotations.Inject;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.framework.Action;
import me.fru1t.rsbot.common.framework.action.modules.SpamClick;
import me.fru1t.rsbot.common.framework.components.RunState;
import me.fru1t.rsbot.common.util.Condition;
import me.fru1t.rsbot.common.util.Timer;
import me.fru1t.rsbot.safecracker.actions.modules.Backpack;
import me.fru1t.rsbot.safecracker.actions.modules.Health;
import me.fru1t.rsbot.safecracker.actions.modules.SafeLogic;
import me.fru1t.rsbot.safecracker.actions.modules.SmartClick;
import me.fru1t.rsbot.safecracker.actions.modules.SpamClickProxy;

/**
 * Defines the safe cracking portion of the script.
 * 
 * TODO: Add break points if the script get stopped before the entire method completes.
 */
public class SafeCrack implements Action {
	private final ClientContext ctx;
	private final RunState<RoguesDenSafeCracker.State> state;
	private final Health health;
	private final Backpack backpack;
	private final SmartClick smartClick;
	private final SafeLogic safeLogic;
	private final Timer safecrackAnimationTimer;

	private final SpamClick spamClick;
	private GameObject safeGameObject;
	
	@Inject
	public SafeCrack(
			ClientContext ctx,
			RunState<RoguesDenSafeCracker.State> state,
			Health health,
			Backpack backpack,
			SmartClick smartClick,
			SafeLogic safeLogic,
			Timer safecrackAnimationTimer,
			SpamClickProxy spamClickProxy) {
		this.ctx = ctx;
		this.state = state;
		this.health = health;
		this.backpack = backpack;
		this.smartClick = smartClick;
		this.safeLogic = safeLogic;
		this.safecrackAnimationTimer = safecrackAnimationTimer;
		
		this.spamClick = spamClickProxy.getInstance();
		safeGameObject = null;
	}

	@Override
	public boolean run() {
		// Bank run?
		// TODO: Add - Gamble (interact even when inventory is full)
		// TODO: Add - Eat food to open inventory space
		// Things to consider: More likely to gamble or eat to clear inventory when near a new
		// level?
		if (ctx.backpack.select().count() >= backpack.bankAt()) {
			backpack.newBankAt();
			// TODO: Move this somewhere more... appropriate.
			safeLogic.newSafe();
			state.update(RoguesDenSafeCracker.State.BANK_WALK);
			return true;
		}
		
		// Health low?
		if (ctx.combatBar.health() < health.eatAt()) {
			health.newEatAt();
			state.update(RoguesDenSafeCracker.State.SAFE_EAT);
			return true;
		}
		
		// Interact with safe
		if (smartClick.shouldActivate()
				&& ctx.menu.items()[0].equals(RoguesDenSafeCracker.MENU_CRACK_ACTIVE_TEXT)) {
			// TODO: Add smart-misclick (eg. even if the menu item isn't the correct interaction)
			ctx.input.click(true);
		} else {
			if (safeGameObject == null
					|| !safeLogic.getSafe().location.equals(safeGameObject.tile())) {
				safeGameObject = ctx.objects
						.select()
						.at(safeLogic.getSafe().location)
						.id(RoguesDenSafeCracker.SAFE_OBJECT_ID)
						.poll();
				safeGameObject.bounds(RoguesDenSafeCracker.SAFE_OBJECT_BOUNDS_MODIFIER);
			}
			
			// Already cracked safe? Other issues?
			if (safeGameObject == null || !safeGameObject.valid()) {
				return false;
			}
			
			// In view
			if (!safeGameObject.inViewport()) {
				ctx.camera.turnTo(safeGameObject);
			}
			
			// TODO: Implement misclick
			safeGameObject.click();
		}
		
		// Impatient clicking
		int spamClicks = spamClick.getClicks();
		while (spamClicks-- > 1 // First click already happened
				&& ctx.menu.items()[0].equals(RoguesDenSafeCracker.MENU_CRACK_ACTIVE_TEXT)) {
			// TODO: Again, smart mis-click?
			ctx.input.click(true);
			Condition.sleep(spamClick.getDelay());
		}
		
		// Safety check
		if (ctx.movement.destination() != Tile.NIL
				&& ctx.players.local().tile().equals(safeGameObject.tile())) {
			state.update(RoguesDenSafeCracker.State.SAFE_WALK); // Mistakes were made.
			return false;
		}
		
		// TODO: Add human factor
		
		// Waiting for the player to interact
		if (!Condition.wait(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return ctx.players.local().animation()
						== RoguesDenSafeCracker.PLAYER_CRACK_ANIMATION;
			}
		}, 100, 10)) // 1000 ms
			return false;
		
		// TODO: Add human factor
		
		// Waiting for the player to success or fail
		if (!Condition.wait(
				new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						return !safeGameObject.valid()
								|| ctx.players.local().animation() 
										== RoguesDenSafeCracker.PLAYER_CRACK_PRE_HURT_ANIMATION
								|| ctx.players.local().animation()
										== RoguesDenSafeCracker.PLAYER_HURTING_ANIMATION;
					}},
				new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						return ctx.players.local().animation()
								== RoguesDenSafeCracker.PLAYER_CRACK_ANIMATION;
					}},
				safecrackAnimationTimer,
				2000,
				150))
			return false;
		
		// TODO: Add human factor
		
		// Wait for safe reset
		if (!Condition.wait(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return safeGameObject.valid();
			}
		}, 300, 7)) // 2100 ms
			return false;
		
		return true;
	}

}
