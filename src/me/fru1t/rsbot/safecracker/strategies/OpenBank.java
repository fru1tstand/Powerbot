package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.rsbot.common.framework.components.Status;
import me.fru1t.rsbot.common.script.rt6.Camera;
import me.fru1t.slick.util.Provider;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Npc;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.RoguesDenSafeCracker.State;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.script.rt6.Mouse;

public class OpenBank implements Strategy<RoguesDenSafeCracker.State> {
	public static final int BENEDICT_NPC_ID = 14707;

	private final Provider<ClientContext> ctxProvider;
	private final Provider<Status> statusProvider;
	private final Camera camera;
	private final Mouse mouse;

	@Inject
	public OpenBank(
			Provider<ClientContext> ctxProvider,
			Provider<Status> statusProvider,
			@Singleton Mouse mouseUtil,
			@Singleton Camera camera) {
		this.ctxProvider = ctxProvider;
		this.statusProvider = statusProvider;
		this.mouse = mouseUtil;
		this.camera = camera;
	}

	@Override
	public State run() {
		statusProvider.get().update("Banking: Interacting with Benedict");

		// Check if already open
		if (ctxProvider.get().bank.opened()) {
			statusProvider.get().update("Banking: Bank already open");
			return State.BANK_DEPOSIT;
		}

		// Check if present
		Npc banker = ctxProvider.get().npcs.select().id(BENEDICT_NPC_ID).poll();
		if (!banker.valid()) {
			statusProvider.get().update("Banking: 404 - Banker not found");
			return null;
		}

		// Face banker
		camera.maybeFace(banker);

		// Open
		if (!mouse.click(banker)) {
			return null;
		}

		return State.BANK_DEPOSIT;
	}
}
