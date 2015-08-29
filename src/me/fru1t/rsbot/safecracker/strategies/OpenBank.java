package me.fru1t.rsbot.safecracker.strategies;

import java.util.concurrent.Callable;

import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Npc;

import me.fru1t.annotations.Inject;
import me.fru1t.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.Timer;
import me.fru1t.rsbot.common.framework.Strategy;
import me.fru1t.rsbot.common.strategies.logic.SpamClick;
import me.fru1t.rsbot.common.util.Condition;
import me.fru1t.rsbot.common.util.Random;
import me.fru1t.rsbot.safecracker.strategies.logic.InteractSpamClickProvider;
import me.fru1t.rsbot.safecracker.strategies.logic.TurnToBanker;

public class OpenBank implements Strategy<RoguesDenSafeCracker.State> {
	// TODO: Set correct banker id
	private static final int BANKER_ID = -1;
	private static final int MAX_WAIT_TIME = 2500;
	private static final int MIN_WAIT_TIME = 1200;

	private final ClientContext ctx;
	private final SpamClick spamClick;
	private final TurnToBanker turnToBanker;
	private final Timer bankOpenTimer;

	@Inject
	public OpenBank(
			ClientContext ctx,
			@Singleton InteractSpamClickProvider spamClickProvider,
			TurnToBanker turnToBanker,
			Timer bankOpenTimer) {
		this.ctx = ctx;
		this.turnToBanker = turnToBanker;
		this.spamClick = spamClickProvider.get();
		this.bankOpenTimer = bankOpenTimer;
	}

	@Override
	public RoguesDenSafeCracker.State run() {
		if (ctx.npcs.select().id(BANKER_ID).size() == 0) {
			return null;
		}

		if (ctx.bank.opened()) {
			return RoguesDenSafeCracker.State.BANK_INTERACT;
		}

		Npc banker = ctx.npcs.poll();
		if (turnToBanker.should(banker)) {
			ctx.camera.turnTo(banker);
		}

		// TODO: Sometimes right click to interact
		// TODO: Add misclicks

		int spamClickCount = spamClick.getClicks();
		while (spamClickCount-- > 0) {
			banker.click();
		}

		if (!Condition.wait(
				new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						return ctx.bank.opened();
					}
				},
				new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						return ctx.players.local().inMotion();
					}

				},
				bankOpenTimer,
				Random.nextInt(MIN_WAIT_TIME, MAX_WAIT_TIME),
				150)) {
			return null;
		}

		return RoguesDenSafeCracker.State.BANK_INTERACT;
	}


}
