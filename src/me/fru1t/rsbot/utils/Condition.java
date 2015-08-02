package me.fru1t.rsbot.utils;

import java.util.concurrent.Callable;

public class Condition extends org.powerbot.script.Condition {
	public static boolean wait(Callable<Boolean> condition, Timer timer, int pollFrequency) {
		while (!timer.hasExpired()) {
			try {
				if (condition.call())
					return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sleep(pollFrequency);
		}
		return false;
	}
}
