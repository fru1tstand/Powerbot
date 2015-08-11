package me.fru1t.rsbot.util;

import org.powerbot.script.ClientContext;

import me.fru1t.annotations.Inject;

public class Timer {
	private final ClientContext<?> ctx;
	private long expirationTimeInMillis;
	private long lastSetTimeFromNowInMillis;
	
	@Inject
	public Timer(ClientContext<?> ctx) {
		this(ctx, 0);
	}
	
	public Timer(ClientContext<?> ctx, long timeFromNowInMillis) {
		this.ctx = ctx;
		set(timeFromNowInMillis);
	}
	
	/**
	 * @return True if the timer value has expired. Otherwise, false.
	 */
	public boolean hasExpired() {
		return ctx.controller.script().getRuntime() > expirationTimeInMillis;
	}
	
	/**
	 * Sets the timer expiration length to the last set amount of time
	 */
	public void reset() {
		this.expirationTimeInMillis = 
				ctx.controller.script().getRuntime() + lastSetTimeFromNowInMillis;
	}
	
	/**
	 * Sets the timer to expire a set amount of time from now.
	 * @param timeFromNowInMillis The amount of time from now.
	 */
	public void set(long timeFromNowInMillis) {
		this.lastSetTimeFromNowInMillis = timeFromNowInMillis;
		reset();
	}
}