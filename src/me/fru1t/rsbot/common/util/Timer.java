package me.fru1t.rsbot.common.util;

import me.fru1t.slick.util.Provider;
import org.powerbot.script.ClientContext;

import me.fru1t.common.annotations.Inject;

/**
 * This class represents a passive timer that uses the script's run time to track the passage of
 * time. This class does not generate interrupts, and instead requires a check if the timer has
 * expired (hence, passive). Due to the timer using the script run time as the base, this timer
 * does not sync with real world time as this timer will "pause" when the script is paused.
 *
 * <p>This timer is useful for tracking script events that should be run in accordance to the
 * script's time. Eg, strategy triggers, anti-pattern procedures, etc.</p>
 */
public class Timer {
	private final Provider<ClientContext> ctxProvider;
	private long expirationTimeInMillis;
	private long lastSetTimeFromNowInMillis;

	@Inject
	public Timer(Provider<ClientContext> ctxProvider) {
		this.ctxProvider = ctxProvider;
	}

	/**
	 * @return True if the timer value has expired. Otherwise, false.
	 */
	public boolean hasExpired() {
		return ctxProvider.get().controller.script().getRuntime() > expirationTimeInMillis;
	}

	/**
	 * Sets the timer expiration length to the last set amount of time
	 */
	public void reset() {
		this.expirationTimeInMillis =
				ctxProvider.get().controller.script().getRuntime() + lastSetTimeFromNowInMillis;
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
