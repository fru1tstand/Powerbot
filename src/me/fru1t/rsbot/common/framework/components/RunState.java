package me.fru1t.rsbot.common.framework.components;

import me.fru1t.annotations.Singleton;

/**
 * Tracks the script's state
 * @param <ST>
 */
@Singleton
public class RunState<ST extends Enum<ST>> {
	private ST currentState;
	private ST lastState;
	
	public RunState() {
		currentState = null;
		lastState = null;
	}
	
	/**
	 * Sets the current state to the passed new state
	 * @param newState
	 */
	public void update(ST newState) {
		this.lastState = this.currentState;
		this.currentState = newState;
	}
	
	/**
	 * @return The current state
	 */
	public ST getCurrent() {
		return currentState;
	}
	
	/**
	 * @return The previous state
	 */
	public ST getPrevious() {
		return lastState;
	}
}
