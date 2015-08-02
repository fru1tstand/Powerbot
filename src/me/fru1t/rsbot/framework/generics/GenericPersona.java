package me.fru1t.rsbot.framework.generics;

import org.powerbot.script.ClientContext;

import me.fru1t.rsbot.framework.Script;

public abstract class GenericPersona<C extends ClientContext<?>, T extends GenericSettings> {
	private Script<C, ?, T, ?> script;
	
	public void setScript(Script<C, ?, T, ?> script) {
		this.script = script;
	}
	
	protected Script<C, ?, T, ?> script() {
		return script;
	}
	
	protected abstract void init();
}
