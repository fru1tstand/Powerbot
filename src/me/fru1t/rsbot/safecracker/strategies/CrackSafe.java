package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.RoguesDenSafeCracker.State;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.framework.components.Status;
import me.fru1t.rsbot.common.framework.util.Callable;
import me.fru1t.rsbot.common.framework.util.Condition;
import me.fru1t.rsbot.common.script.rt6.Camera;
import me.fru1t.rsbot.common.script.rt6.Mouse;
import me.fru1t.rsbot.common.util.Timer;
import me.fru1t.rsbot.safecracker.strategies.logic.BackpackLogic;
import me.fru1t.rsbot.safecracker.strategies.logic.Health;
import me.fru1t.rsbot.safecracker.strategies.logic.SafeLogic;
import me.fru1t.slick.util.Provider;
import org.powerbot.script.Tile;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.GameObject;

/**
 * Defines the safe cracking portion of the script.
 *
 * <p>TODO(v1): Add break points if the script get stopped before the entire method completes.
 * Consider
 * splitting this method up into more states.
 *
 * <p>TODO(v2): Add human behavior between actions and waiting.
 */
public class CrackSafe implements Strategy<RoguesDenSafeCracker.State> {
	private final Provider<ClientContext> ctxProvider;
	private final Provider<Status> statusProvider;
	private final Mouse mouse;
	private final Health health;
	private final BackpackLogic backpackLogic;
	private final SafeLogic safeLogic;
	private final Timer safecrackAnimationTimer;
	private final Camera camera;

	@Inject
	public CrackSafe(
			Provider<ClientContext> ctxProvider,
			Provider<Status> statusProvider,
			@Singleton Mouse mouse,
			@Singleton Camera camera,
			@Singleton SafeLogic safeLogic,
			Health health,
			BackpackLogic backpackLogic,
			Timer safecrackAnimationTimer) {
		this.ctxProvider = ctxProvider;
		this.statusProvider = statusProvider;
		this.mouse = mouse;
		this.health = health;
		this.backpackLogic = backpackLogic;
		this.safeLogic = safeLogic;
		this.safecrackAnimationTimer = safecrackAnimationTimer;
		this.camera = camera;
	}

	@Override
	public State run() {
		statusProvider.get().update("Cracking safe");
		final ClientContext ctx = ctxProvider.get();

		// Bank run?
		// TODO(v2): Add - Gamble (interact even when inventory is full)
		// TODO(v2): Add - Eat food to open inventory space
		// Things to consider: More likely to gamble or eat to clear inventory when near a new
		// level?
		// Bank run?
		if (ctx.backpack.select().count() >= backpackLogic.bankAt()) {
			statusProvider.get().update("Backpack is full");
			backpackLogic.newBankAt();
			return State.WALK_TO_BANK;
		}

		// Health low?
		if (ctx.combatBar.health() < health.eatAt()) {
			statusProvider.get().update("Health is low");
			health.newEatAt();
			return State.EAT;
		}

		// Check if safe is valid. We also have to cache this gameobject as we'll be using it when
		// it's invalid.
		final GameObject wallsafeGameObject = safeLogic.getSafeGameObject();
		if (!wallsafeGameObject.valid()) {
			statusProvider.get().update("404: Safe not found");
			return null;
		}

		// Verify turn towards safe
		statusProvider.get().update("Turning camera towards safe");
		camera.maybeFace(wallsafeGameObject);

		// Interact with safe
		statusProvider.get().update("Interacting with safe");
		if (!mouse.click(wallsafeGameObject)) {
			return null;
		}

		// Waiting for the player to interact or fail
		statusProvider.get().update("Waiting for player interaction animation");
		if (!Condition.wait(new Callable<Boolean>() {
			@Override
			public Boolean ring() {
				return ctx.players.local().animation()
						== RoguesDenSafeCracker.PLAYER_CRACK_ANIMATION
						|| !ctx.movement.destination().equals(Tile.NIL)
						|| !ctx.players.local().tile().equals(safeLogic.getSafe().playerLocation);
			}
		}, 100, 10)) { // 1000 ms
			statusProvider.get().update("The player never interacted with the safe");
			return null;
		}

		// Safety check
		if (!ctx.movement.destination().equals(Tile.NIL)
				|| ctx.players.local().inMotion()
				|| !ctx.players.local().tile().equals(safeLogic.getSafe().playerLocation)) {
			statusProvider.get().update("Mistakes were made");
			return State.WALK_TO_SAFE;
		}

		// Waiting for the player to success or fail
		statusProvider.get().update("Waiting for results");
		if (!Condition.wait(
				new Callable<Boolean>() {
					@Override
					public Boolean ring() {
						return !wallsafeGameObject.valid()
								|| ctx.players.local().animation()
										== RoguesDenSafeCracker.PLAYER_CRACK_PRE_HURT_ANIMATION
								|| ctx.players.local().animation()
										== RoguesDenSafeCracker.PLAYER_HURTING_ANIMATION;
					}},
				new Callable<Boolean>() {
					@Override
					public Boolean ring() {
						return ctx.players.local().animation()
								== RoguesDenSafeCracker.PLAYER_CRACK_ANIMATION;
					}},
				safecrackAnimationTimer,
				2000,
				150)) {
			statusProvider.get().update("Couldn't determine the result");
			return null;
		}

		// Quick low health check before waiting
		if (ctx.combatBar.health() < health.eatAt()) {
			health.newEatAt();
			return State.EAT;
		}

		// Wait for safe reset
		statusProvider.get().update("Waiting for wallsafe to reset");
		if (!Condition.wait(new Callable<Boolean>() {
			@Override
			public Boolean ring() {
				return wallsafeGameObject.valid();
			}
		}, 300, 7)) { // 2100 ms
			return null;
		}

		return State.CRACK_SAFE;
	}

}
