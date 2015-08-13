package me.fru1t.rsbot.safecracker.actions.modules;

import me.fru1t.annotations.Inject;
import me.fru1t.rsbot.common.util.Random;
import me.fru1t.rsbot.common.util.Timer;

/**
 * A user doesn't constantly move the mouse every #interact, and vis versa. Depending on a 
 * large number of factors, a player may or may not move the mouse during an interact if the
 * mouse is already hovered over the safe.
 * 
 * <p>TODO: Create multiple smartclick profiles (random, normal(?), disabled)
 */
public class SmartClick {
	private static final int MIN_TIME_RANGE_MIN = 1;
	private static final int MIN_TIME_RANGE_MAX = 20;
	private static final int MAX_TIME_RANGE_MIN = 20;
	private static final int MAX_TIME_RANGE_MAX = 300;
	private static final int SMART_CLICK_ENABLED_PROBABILITY = 80;
	
	private final Timer timer;
	private final boolean isEnabled;
	private int minTime;
	private int maxTime;
	
	@Inject
	public SmartClick(Timer timer) {
		this.timer = timer;
		
		isEnabled = Random.roll(SMART_CLICK_ENABLED_PROBABILITY);
		if (isEnabled) {
			minTime = Random.nextInt(MIN_TIME_RANGE_MIN, MIN_TIME_RANGE_MAX);
			maxTime = Random.nextInt(MAX_TIME_RANGE_MIN, MAX_TIME_RANGE_MAX);
		}
	}
	
	/**
	 * @return If the click should be made without moving the mouse
	 */
	public boolean shouldActivate() {
		if (!isEnabled) {
			return false;
		}
		
		// When the timer is active, smart click
		if (!timer.hasExpired()) {
			return true;
		}
		
		timer.set(1000 * Random.nextInt(minTime, maxTime));
		return false;
	}
}
