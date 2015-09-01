package me.fru1t.rsbot.safecracker.strategies;

import me.fru1t.common.annotations.Inject;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.RoguesDenSafeCracker.State;
import me.fru1t.rsbot.common.framework.Strategy;

public class BankDeposit implements Strategy<RoguesDenSafeCracker.State> {
	@Inject
	public BankDeposit() {

	}

	@Override
	public State run() {
		// We aren't certain if the bank is open at this time.
		
		
		// TODO Auto-generated method stub
		return null;
	}
}
