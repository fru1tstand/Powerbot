package me.fru1t.rsbot.common.framework.util;

import java.util.concurrent.Callable;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.common.collections.Tuple2;
import me.fru1t.rsbot.common.framework.components.Persona;
import me.fru1t.rsbot.common.util.Random;
import me.fru1t.rsbot.common.util.Timer;

@Singleton
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

	/**
	 * Wait until either the condition is true or a random time within the given range has elapsed.
	 *
	 * @param condition
	 * @param waitRange
	 * @return True if the condition returned true. Otherwise, false.
	 */
	public static boolean wait(Callable<Boolean> condition, Tuple2<Integer, Integer> waitRange) {
		return wait(condition, Random.nextInt(waitRange));
	}

	private final Persona persona;

	@Inject
	public Condition(@Singleton Persona persona) {
		this.persona = persona;
	}

	/**
	 * Sleeps the current thread for the next interact delay amount of time.
	 */
	// TODO(v1 cleanup): Find and remove instances of Condition#Sleep(Persona.getNextInteractDelay)
	public void sleepForInteractDelay() {
		sleep(persona.getNextInteractDelay());
	}

	/**
	 * Sleeps the current thread for the next spam delay amount of time.
	 */
	// TODO(v1 cleanup): Find and remove instances of Condition#Sleep(Persona.getNextSpamDelay)
	public void sleepForSpamDelay() {
		sleep(persona.getNextSpamDelay());
	}
}
