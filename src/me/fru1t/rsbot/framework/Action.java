package me.fru1t.rsbot.framework;

import org.powerbot.script.ClientContext;

import me.fru1t.rsbot.framework.generics.GenericSettings;

/**
 * Defines a container class for a script Action that holds the ClientContext and Settings for a
 * script.
 *
 * @param <C> The ClientContext version
 * @param <S> The script that's running
 * @param <T> The Settings type
 */
public abstract class Action<
		C extends ClientContext<?>,
		S extends Script<C, ?, T>,
		T extends GenericSettings> {
	protected final S script;
	
	/**
	 * Creates a script action storing the script.
	 * @param script
	 */
	public Action(S script) {
		this.script = script;
	}
	
	/**
	 * The action to perform.
	 * @return If the run completed successfully.
	 */
	public abstract boolean run();
}
