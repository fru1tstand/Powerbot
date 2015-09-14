package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.framework.components.Status;
import me.fru1t.rsbot.common.util.Random;
import me.fru1t.slick.util.Provider;
import org.powerbot.script.rt6.ClientContext;

/**
 * ???
 */
public class Unknown implements Strategy<RoguesDenSafeCracker.State> {
	private final Provider<ClientContext> ctxProvider;
	private final Provider<Status> statusProvider;

	public Unknown(
			Provider<ClientContext> ctxProvider,
			Provider<Status> statusProvider) {
		this.ctxProvider = ctxProvider;
		this.statusProvider = statusProvider;
	}

	@Override

	public RoguesDenSafeCracker.State run() {
		statusProvider.get().update("Hmm... Seems like something went wrong...");
		// Safe reset if we know we're in rogue's den
		if (!ctxProvider.get().npcs.select().id(OpenBank.BENEDICT_NPC_ID).isEmpty()) {
			return RoguesDenSafeCracker.State.WALK_TO_BANK;
		}

		// Otherwise, hell if we know where we are
		// TODO(v2): Add script resetting via walking/lodestone teleporting/etc
		ctxProvider.get().game.logout(Random.nextBoolean());
		ctxProvider.get().controller.stop();
		return null;
	}
}
