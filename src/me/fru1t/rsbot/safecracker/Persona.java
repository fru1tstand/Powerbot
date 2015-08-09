package me.fru1t.rsbot.safecracker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.powerbot.script.Random;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.GameObject;

import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.framework.generics.GenericPersona;
import me.fru1t.rsbot.utils.Timer;


public class Persona {
	private static final int MAX_FOCUS = 100;
	private static final int MIN_FOCUS = 0;

	
	// TODO: Create algo that models attention and clumsiness that is sinusoidally dependent over
	// time.
	protected void init() {
		this.attention = 100;
		this.clumsy = 0;
		
		healingThreshold_setup();
		clickSpamDelay_setup();
		impatientEatClickCount_setup();
		
		backpackFillCountBeforeBanking(true);
		safeToCrack(true);
		smartClick();
		safeMisclick();
		safeClickCount(true);
	}

	/**
	 * Description:
	 * Sometimes people are happy to just eat 1 food. Others, not so much.
	 * @return
	 */
	public int foodToConsume() {
		return 0;
	}
	
	
	private static final int safeMisclick_ABS_MIN = 0;
	private static final int safeMisclick_ABS_MAX = 120;
	private static final int safeMisclick_MAX_VAR = 15;
	private static final int safeMisclick_MIN_VAR = 3;
	private static final int safeMisclick_MAX_MEAN = 50;
	private Timer safeMisclick_timer;
	/**
	 * Description:
	 * Mis-click safe when cracking. Increases attention when true.
	 * 
	 * Trigger:
	 * Timer
	 * 
	 * Algorithm:
	 * Normal distribution. Mean [0, MAX_MEAN] inversely proportional to focus. Variances
	 * [MIN_VAR, MAX_VAR] inversely proportional to focus.
	 * 
	 * Justification:
	 * Humans aren't perfect. The less focused you are, the more likely you are of mis-clicking and
	 * correcting the mis-click which would increase attention a little.
	 * @return
	 */
	public boolean safeMisclick() {
		if (safeMisclick_timer == null) {
			safeMisclick_timer = new Timer(script().ctx);
		}
		if (smartClick_timer.hasExpired()) {
			smartClick_timer.set(1000 * Random.nextGaussian(
					safeMisclick_ABS_MIN,
					safeMisclick_ABS_MAX,
					safeMisclick_MAX_MEAN * 100 / getFocus(),
					Math.sqrt((safeMisclick_MAX_VAR - safeMisclick_MIN_VAR) * 100 / getFocus())));
			return false;
		}
		return true;
	}
	
	private static final int misclickInstantRecovery_UPPER_BOUND = 75;
	/**
	 * Description:
	 * Skip waiting for the player to react when misclicking
	 * 
	 * Trigger:
	 * Misclick event
	 * 
	 * Algorithm:
	 * Linearly proportional to focus with an upper bound of UPPER_BOUND.
	 * 
	 * Justification:
	 * The lesser focused someone is, the less they'll catch a misclick.
	 * @return If the recovery should be instant.
	 */
	public boolean misclickInstantRecovery() {
		return Random.nextInt(0, misclickInstantRecovery_UPPER_BOUND) <= getFocus();
	}
	
	

	
	
	private static final int clickSpamDelay_MIN_CLICK_DELAY = 50; // ms
	private static final int clickSpamDelay_MAX_CLICK_DELAY = 300; // ms
	private static final double clickSpamDelay_MAX_VAR = 0.8;
	private static final double clickSpamDelay_MIN_VAR = 0.15;
	private double clickSpamDelay_variance;
	private int clickSpamDelay_mean;
	/**
	 * Description:
	 * There's a delay between click spamming
	 * 
	 * Algorithm:
	 * Normal distribution with randomly chosen mean of [MIN_CLICK_DELAY, MAX_MEAN] and a randomly
	 * chosen variance of [MIN_VAR, MAX_VAR]
	 * @return The delay between clicks
	 */
	public int clickSpamDelay() {
		return Random.nextGaussian(
				clickSpamDelay_MIN_CLICK_DELAY,
				clickSpamDelay_MAX_CLICK_DELAY,
				clickSpamDelay_mean,
				Math.sqrt(clickSpamDelay_variance));
	}
	private void clickSpamDelay_setup() {
		clickSpamDelay_variance = Random.nextDouble(clickSpamDelay_MIN_VAR, clickSpamDelay_MAX_VAR);
		clickSpamDelay_mean = Random.nextInt(1, safeClickCount_MAX_MEAN);
	}
	
	
	private static final int impatientEatClickCount_OFF_PROB = 80;
	private static final int impatientEatClickCount_MIN_RND_CLICKS = 1;
	private static final int impatientEatClickCount_MAX_RND_CLICKS = 4;
	private static final int impatientEatClickCount_ON_NORM_PROB = 30;
	private static final double impatientEatClickCount_ON_MIN_VAR = 0.5;
	private static final double impatientEatClickCount_ON_MAX_VAR = 1.0;
	private static final int impatientEatClickCount_ON_NORM_MEAN = 2;
	private boolean impatientEatClickCount_isEnabled;
	private double impatientEatClickCount_normVariance;
	/**
	 * Description:
	 * Sometimes people like spam clicking food until it's eaten. This is that.
	 * 
	 * Trigger:
	 * Each food item eaten
	 * 
	 * Algorithm:
	 * "Off" - Never spams (returns 1)
	 * 		OFF_PROB% probability
	 * "On" - ON_NORM_PROB% chance of Norm(2, const rand[ON_MIN_VAR, ON_MAX_VAR])
	 * 		  (100 - ON_NORM_PROB)% chance of 1
	 * 		(100 - OFF_PROB)% probability
	 * 
	 * Justification:
	 * Some people like spam clicking, others don't. Those that do spam click, often don't spam
	 * click 100% of the time. 
	 * 
	 * Consider: Spam clickers may fall back to not spam clicking after some time.
	 * @return Number of times to click a food item.
	 */
	public int impatientEatClickCount() {
		if (!impatientEatClickCount_isEnabled
				|| Random.nextInt(0, 100) > impatientEatClickCount_ON_NORM_PROB) {
			return 1;
		}
		return Random.nextGaussian(
				impatientEatClickCount_MIN_RND_CLICKS,
				impatientEatClickCount_MAX_RND_CLICKS,
				impatientEatClickCount_ON_NORM_MEAN,
				impatientEatClickCount_normVariance);
	}
	private void impatientEatClickCount_setup() {
		impatientEatClickCount_isEnabled =
				(Random.nextInt(0, 100) > impatientEatClickCount_OFF_PROB);
		impatientEatClickCount_normVariance = Random.nextDouble(
				impatientEatClickCount_ON_MIN_VAR,
				impatientEatClickCount_ON_MAX_VAR);
	}
}
