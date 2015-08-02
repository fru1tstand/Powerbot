package me.fru1t.rsbot.safecracker.actions;

import org.powerbot.script.rt6.ClientContext;

import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.framework.Action;
import me.fru1t.rsbot.safecracker.Persona;
import me.fru1t.rsbot.safecracker.Settings;

public class CrackSafe extends Action<ClientContext, RoguesDenSafeCracker, Settings, Persona> {
	public CrackSafe(RoguesDenSafeCracker script) {
		super(script);
	}

	@Override
	public void run() {
		if (script.ctx.backpack.count() 
				== script.persona.backpackFillCountBeforeBanking(false)) {
			script.persona.backpackFillCountBeforeBanking(true);
			script.updateState(RoguesDenSafeCracker.State.BANK_WALK);
			return;
		}
		if (script.ctx.combatBar.health() < script.persona.eatHealthThreshold(false)) {
			script.persona.eatHealthThreshold(true);
			script.updateState(RoguesDenSafeCracker.State.SAFE_EAT);
			return;
		}
		
		RoguesDenSafeCracker.Safe safe = script.persona.safeToCrack(false);
		
	}
}
