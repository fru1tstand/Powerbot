package me.fru1t.rsbot;

import org.powerbot.script.PollingScript;
import org.powerbot.script.Script.Manifest;
import org.powerbot.script.rt6.ClientContext;

@Manifest(name = "Rogue's Den Safe Cracker", description = "Cracks safes in Rogue's Den")
public class RoguesDenSafeCracker extends PollingScript<ClientContext> {
	public static ClientContext ctx;
	
	public RoguesDenSafeCracker(ClientContext ctx) {
		RoguesDenSafeCracker.ctx = ctx;
	}
	
	@Override
	public void poll() {
		// TODO Auto-generated method stub
		
	}

	

}
