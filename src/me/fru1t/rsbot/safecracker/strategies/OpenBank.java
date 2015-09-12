package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.slick.util.Provider;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Npc;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.RoguesDenSafeCracker.State;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.script.rt6.Mouse;
import me.fru1t.rsbot.safecracker.strategies.logic.TurnToBanker;

public class OpenBank implements Strategy<RoguesDenSafeCracker.State> {
	// TODO(v1): Set correct banker id
	private static final int BANKER_ID = -1;

	private final Provider<ClientContext> ctxProvider;
	private final Mouse mouseUtil;
	private final TurnToBanker turnToBanker;

	@Inject
	public OpenBank(
			Provider<ClientContext> ctxProvider,
			@Singleton Mouse mouseUtil,
			TurnToBanker turnToBanker) {
		this.ctxProvider = ctxProvider;
		this.turnToBanker = turnToBanker;
		this.mouseUtil = mouseUtil;
	}

	@Override
	public State run() {
		if (ctxProvider.get().npcs.select().id(BANKER_ID).size() == 0) {
			return null;
		}

		if (ctxProvider.get().bank.opened()) {
			return State.BANK_DEPOSIT;
		}

		Npc banker = ctxProvider.get().npcs.poll();
		if (turnToBanker.should(banker)) {
			ctxProvider.get().camera.turnTo(banker);
		}

		// TODO(v2): Sometimes right click to interact
		mouseUtil.click(banker);

		return State.BANK_DEPOSIT;
	}


}
