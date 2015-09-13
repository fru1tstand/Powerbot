package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.RoguesDenSafeCracker.State;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.framework.util.Callable;
import me.fru1t.rsbot.common.framework.util.Condition;
import me.fru1t.rsbot.common.script.rt6.Camera;
import me.fru1t.rsbot.common.script.rt6.Mouse;
import me.fru1t.rsbot.common.util.Timer;
import me.fru1t.rsbot.safecracker.strategies.logic.Backpack;
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
public class SafeCrack implements Strategy<RoguesDenSafeCracker.State> {
	private final Provider<ClientContext> ctxProvider;
	private final Mouse mouse;
	private final Health health;
	private final Backpack backpack;
	private final SafeLogic safeLogic;
	private final Timer safecrackAnimationTimer;
	private final Camera camera;

	private GameObject wallsafeGameObject;

	@Inject
	public SafeCrack(
			Provider<ClientContext> ctxProvider,
			@Singleton Mouse mouse,
			Health health,
			Backpack backpack,
			SafeLogic safeLogic,
			Timer safecrackAnimationTimer,
			@Singleton Camera camera) {
		this.ctxProvider = ctxProvider;
		this.mouse = mouse;
		this.health = health;
		this.backpack = backpack;
		this.safeLogic = safeLogic;
		this.safecrackAnimationTimer = safecrackAnimationTimer;
		this.camera = camera;

		this.wallsafeGameObject = ctxProvider.get().objects.nil();
	}

	@Override
	public State run() {
		final ClientContext ctx = ctxProvider.get();

		// Bank run?
		// TODO(v2): Add - Gamble (interact even when inventory is full)
		// TODO(v2): Add - Eat food to open inventory space
		// Things to consider: More likely to gamble or eat to clear inventory when near a new
		// level?
		// Bank run?
		if (ctx.backpack.select().count() >= backpack.bankAt()) {
			backpack.newBankAt();
			return State.BANK_WALK;
		}

		// Health low?
		if (ctx.combatBar.health() < health.eatAt()) {
			health.newEatAt();
			return State.SAFE_EAT;
		}

		// Get safe
		if (!safeLogic.getSafe().location.equals(wallsafeGameObject.tile())) {
			wallsafeGameObject = ctx.objects.select()
					.at(safeLogic.getSafe().location)
					.id(RoguesDenSafeCracker.SAFE_OBJECT_ID).poll();
			wallsafeGameObject.bounds(RoguesDenSafeCracker.SAFE_OBJECT_BOUNDS_MODIFIER);

			// Catches nil
			if (!wallsafeGameObject.valid()) {
				return null;
			}
		}

		if (!wallsafeGameObject.inViewport()) {
			camera.face(safeLogic.getSafe().cameraDirection);
			if (!wallsafeGameObject.inViewport()) {
				ctx.camera.turnTo(wallsafeGameObject);
			}
		}

		// Interact with safe
		mouse.click(wallsafeGameObject);

		// Waiting for the player to interact or fail
		if (!Condition.wait(new Callable<Boolean>() {
			@Override
			public Boolean ring() {
				return ctx.players.local().animation()
						== RoguesDenSafeCracker.PLAYER_CRACK_ANIMATION
						|| !ctx.movement.destination().equals(Tile.NIL)
						|| !ctx.players.local().tile().equals(safeLogic.getSafe().playerLocation);
			}
		}, 100, 10)) { // 1000 ms
			return null;
		}

		// Safety check
		if (!ctx.movement.destination().equals(Tile.NIL)
				|| ctx.players.local().inMotion()
				|| !ctx.players.local().tile().equals(safeLogic.getSafe().playerLocation)) {
			return State.SAFE_WALK; // Mistakes were made.
		}

		// Waiting for the player to success or fail
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
			return null;
		}

		// Quick low health check before waiting
		if (ctx.combatBar.health() < health.eatAt()) {
			health.newEatAt();
			return State.SAFE_EAT;
		}

		// Wait for safe reset
		if (!Condition.wait(new Callable<Boolean>() {
			@Override
			public Boolean ring() {
				return wallsafeGameObject.valid();
			}
		}, 300, 7)) { // 2100 ms
			return null;
		}

		return State.SAFE_CRACK;
	}

}
