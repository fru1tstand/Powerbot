package me.fru1t.rsbot.safecracker.actions;

import java.util.concurrent.Callable;

import org.powerbot.script.Tile;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.GameObject;

import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.framework.Action;
import me.fru1t.rsbot.safecracker.Persona;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.rsbot.utils.Condition;
import me.fru1t.rsbot.utils.Timer;

public class SafeCrack extends Action<ClientContext, RoguesDenSafeCracker, Settings> {
	private GameObject cachedSafeToCrack;
	
	public SafeCrack(RoguesDenSafeCracker script) {
		super(script);
		cachedSafeToCrack = null;
	}

	@Override
	public boolean run() {
		// Bank run?
		// TODO: Add - Gamble (interact even when inventory is full)
		// TODO: Add - Eat food to open inventory space
		// Things to consider: More likely to gamble or eat to clear inventory when near a new
		// level?
		if (script.ctx.backpack.select().count() 
				== script.persona.backpackFillCountBeforeBanking(false)) {
			script.persona.backpackFillCountBeforeBanking(true);
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
}
