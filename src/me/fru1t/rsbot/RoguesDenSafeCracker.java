package me.fru1t.rsbot;

import org.powerbot.script.PollingScript;
import org.powerbot.script.Script.Manifest;
import org.powerbot.script.rt6.ClientContext;

import me.fru1t.rsbot.safecracker.Startup;

@Manifest(name = "Rogue's Den Safe Cracker", description = "Cracks safes in Rogue's Den")
public class RoguesDenSafeCracker extends PollingScript<ClientContext> {

	@Override
	public void start() {
		new Startup();
		System.out.println("Starting Rogue's Den Safecracker by Fru1tstand");
		super.start();
	}
	
	@Override
	public void stop() {
		System.out.println("Stopping Rogue's Den Safecracker by Fru1tstand");
		super.stop();
	}
	
	@Override
	public void poll() {
		// TODO Auto-generated method stub
		
	}
}
