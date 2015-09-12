package me.fru1t.rsbot.common.framework.components;

import me.fru1t.common.annotations.Nullable;
import me.fru1t.common.annotations.Singleton;

/**
 * This component tracks the script's state. It contains the current and previous states of the
 * script.
 *
 * <p>All framework components are provided through Slick. To use this component, inject it with a
 * Provider in the form of Provider&lt;Persona&gt;</p>
 *
 * @param <ST> The driving script state.
 */
@Singleton
public class RunState<ST extends Enum<ST>> {
	@Nullable
	private ST currentState;
	@Nullable
	private ST lastState;

	public RunState() {
		this.currentState = null;
		this.lastState = null;
	}

	/**
	 * Sets the current state to the passed new state
	 * @param newState
	 */
	public void update(@Nullable ST newState) {
		this.lastState = this.currentState;
		this.currentState = newState;
	}

	/**
	 * @return The current state
	 */
	@Nullable
	public ST getCurrent() {
		return currentState;
	}

	/**
	 * @return The previous state
	 */
	@Nullable
	public ST getPrevious() {
		return lastState;
	}
}
