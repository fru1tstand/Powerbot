package me.fru1t.rsbot.safecracker;

import me.fru1t.annotations.Inject;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.framework.AbstractSettings;
import me.fru1t.rsbot.common.items.Food;

public class Settings extends AbstractSettings {
	public enum BankStyle { CONSTANT, AUTOMATIC, PRESET_1, PRESET_2 }
	
	private BankStyle bankStyle;
	private int foodQuantity;
	private Food food;
	private RoguesDenSafeCracker.Safe preferredSafe;
	
	/**
	 * Simple constructor which sets default values to class fields.
	 */
	@Inject
	public Settings() {
		this.bankStyle = null;
		this.preferredSafe = null;
		this.foodQuantity = -1;
		this.food = null;
	}
	
	@Override
	public boolean isValid() {
		return bankStyle != null && foodQuantity > -1 && food != null && preferredSafe != null;
	}
	
	/**
	 * @return The current food to withdraw from the bank.
	 */
	public Food getFood() {
		return food;
	}
	
	/**
	 * Sets the food to the given list of food items.
	 * @param food
	 */
	public void setFood(Food food) {
		this.food = food;
	}
	
	/**
	 * @return The preferred safe to crack.
	 */
	public RoguesDenSafeCracker.Safe getPreferredSafe() {
		return preferredSafe;
	}
	
	/**
	 * Sets the preferred safe to crack.
	 * @param preferredSafe
	 */
	public void setPreferredSafe(RoguesDenSafeCracker.Safe preferredSafe) {
		this.preferredSafe = preferredSafe;
	}

	/**
	 * @return The bank style.
	 */
	public BankStyle getBankStyle() {
		return bankStyle;
	}
	
	/**
	 * Checks if the bank style is one of any of the given styles.
	 * @param styles 
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
	 * Sets the bank style
	 * @param bankStyle
	 */
	public void setBankStyle(BankStyle bankStyle) {
		this.bankStyle = bankStyle;
	}

	/**
	 * @return The food amount to withdraw (if BankStyle is constant).
	 */
	public int getFoodQuantity() {
		return foodQuantity;
	}

	/**
	 * Sets the food amount to withdraw. Only used if the bank style is constant.
	 * @param foodQuantity
	 */
	public void setFoodQuantity(int foodQuantity) {
		this.foodQuantity = foodQuantity;
	}
}
