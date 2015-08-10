package me.fru1t.rsbot.utils;

import java.util.concurrent.Callable;

public class Condition extends org.powerbot.script.Condition {
	/**
	 * Conditionally wait until either the given timer expires, or the condition has completed.
	 * 
	 * @param condition The condition to wait for.
	 * @param timerCondition The condition that the timer should be reset.
	 * @param timer The timing timer.
	 * @param timerDuration The amount of time before the timer expires
	 * @param pollFrequency The amount of time between checking the condition and timer condition
	 * @return If the condition was met within the given time. Otherwise, false.
	 */
	public static boolean wait(
			Callable<Boolean> condition,
			Callable<Boolean> timerCondition,
			Timer timer,
			int timerDuration,
			int pollFrequency) {
		timer.set(timerDuration);
		while (!timer.hasExpired()) {
			try {
				if (timerCondition.call()) {
					timer.reset();
				}
				if (condition.call()) {
					return true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			sleep(pollFrequency);
		}
		return false;
	}
}
