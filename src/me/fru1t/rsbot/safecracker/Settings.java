package me.fru1t.rsbot.safecracker;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Nullable;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.framework.AbstractSettings;
import me.fru1t.rsbot.common.items.Food;

@Singleton
public class Settings extends AbstractSettings {
	public enum BankStyle { CONSTANT, AUTOMATIC, PRESET_1, PRESET_2 }

	private BankStyle bankStyle;
	private Food food;
	@Nullable private RoguesDenSafeCracker.Safe preferredSafe;
	private int foodQuantity;

	@Inject
	public Settings() {
		this.bankStyle = BankStyle.AUTOMATIC;
		this.food = Food.ANCHOVIES;
		this.preferredSafe = null;
		this.foodQuantity = -1;
	}

	@Override
	public boolean isValid() {
		return bankStyle != null && foodQuantity > -1 && food != null && preferredSafe != null;
	}

	@Nullable
	public RoguesDenSafeCracker.Safe getPreferredSafe() {
		return preferredSafe;
	}
	public void setPreferredSafe(@Nullable RoguesDenSafeCracker.Safe preferredSafe) {
		this.preferredSafe = preferredSafe;
	}
	public Food getFood() {
		return food;
	}
	public void setFood(Food food) {
		this.food = food;
	}
	public BankStyle getBankStyle() {
		return bankStyle;
	}
	public void setBankStyle(BankStyle bankStyle) {
		this.bankStyle = bankStyle;
	}
	public int getFoodQuantity() {
		return foodQuantity;
	}
	public void setFoodQuantity(int foodQuantity) {
		this.foodQuantity = foodQuantity;
	}

	/**
	 * Checks if the bank style is one of any of the given styles.
	 *
	 * @param styles The BankStyles to check for
	 * @return True if the bank styles are any of the provided. Otherwise, false.
	 */
	public boolean isBankStyle(BankStyle... styles) {
		for (BankStyle style : styles) {
			if (bankStyle == style) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns if the bank style is using a preset.
	 *
	 * @return True if the bank style is using a preset. Otherwise, false.
	 */
	public boolean isBankStyleUsingPresets() {
		return isBankStyle(BankStyle.PRESET_1, BankStyle.PRESET_2);
	}
}
