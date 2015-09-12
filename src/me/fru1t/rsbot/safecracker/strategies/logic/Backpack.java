package me.fru1t.rsbot.safecracker.strategies.logic;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.common.framework.components.Persona;
import me.fru1t.rsbot.common.util.Random;
import me.fru1t.slick.util.Provider;

/**
 * A normal person will wait until the inventory is full (count 28), but on occasion a person
 * may haphazardly bank without a full inventory.
 *
 * Consider:
 * A person is less likely to randomly bank when not clumsy and more attentive. If a random
 * banking event does occur, the count of items in the inventory is probably not skewed in
 * either direction.
 */
public class Backpack {
	/**
	 * The minimum amount of focus a player can have before starting to randomly bank.
	 */
	private static final int ALWAYS_FULL_FOCUS_CUTOFF = 50;
	/**
	 * The probability that the player will bank without reaching a full inventory.
	 */
	private static final int NOT_28_PROBABILITY = 15;
	/**
	 * The absolute minimum number of items a player can be holding before banking.
	 */
	private static final int ABSOLUTE_MINIMUM_ITEM_COUNT = 14;


	private final Provider<Persona> personaProvider;
	private int bankAmount;

	@Inject
	public Backpack(@Singleton Provider<Persona> persona) {
		this.personaProvider = persona;
		newBankAt();
	}

	/**
	 * @return The inventory count at which the player should bank at.
	 */
	public int bankAt() {
		return bankAmount;
	}

	/**
	 * Generates a new bank amount. This should be called every bank event.
	 */
	public void newBankAt() {
		bankAmount = 28;

		// Focus cutoff
		if (personaProvider.get().focus() > ALWAYS_FULL_FOCUS_CUTOFF)
			return;

		// Not 28 probability roll
		if (!Random.roll(NOT_28_PROBABILITY))
			return;

		// Focus roll ( < FOCUS_CUTOFF% )
		if (Random.roll(personaProvider.get().focus()))
			return;

		bankAmount = Random.nextInt(ABSOLUTE_MINIMUM_ITEM_COUNT, 29);
	}
}
