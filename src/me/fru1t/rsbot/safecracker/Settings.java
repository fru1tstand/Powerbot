package me.fru1t.rsbot.safecracker;

import java.util.ArrayList;
import java.util.List;

import me.fru1t.annotations.Inject;
import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.framework.AbstractSettings;
import me.fru1t.rsbot.common.items.Food;

public class Settings extends AbstractSettings {
	private List<Food> foods;
	private boolean isBankStyleConstant;
	private RoguesDenSafeCracker.Safe preferredSafe;
	private int currentFoodPointer;
	
	/**
	 * Simple constructor which sets default values to class fields.
	 */
	@Inject
	public Settings() {
		this.foods = null;
		this.isBankStyleConstant = false;
		this.preferredSafe = null;
		this.currentFoodPointer = 0;
	}
	
	/**
	 * TODO: Abstract with reflection
	 */
	@Override
	public void replace(AbstractSettings settings) {
		if (!(settings instanceof Settings))
			return;
		Settings other = (Settings) settings;
		
		this.foods = other.foods;
		this.isBankStyleConstant = other.isBankStyleConstant;
		this.preferredSafe = other.preferredSafe;
	}
	
	@Override
	public boolean isValid() {
		return foods != null && foods.size() != 0 && preferredSafe != null;
	}
	
	/**
	 * Increments the food pointer and returns if there is more food queued in the food list.
	 * @return If more food is still available.
	 */
	public boolean ranOutOfFood() {
		currentFoodPointer++;
		return currentFoodPointer >= foods.size();
	}
	
	/**
	 * @return The current food to withdraw from the bank.
	 */
	public Food getCurrentFood() {
		return foods.get(currentFoodPointer);
	}
	
	/**
	 * @return A list of all the food items set in the GUI.
	 */
	public List<Food> getFoods() {
		ArrayList<Food> listCopy = new ArrayList<Food>();
		listCopy.addAll(foods);
		return listCopy;
	}
	
	/**
	 * Sets the food to the given list of food items.
	 * @param foods
	 */
	public void setFoods(List<Food> foods) {
		ArrayList<Food> listCopy = new ArrayList<Food>();
		listCopy.addAll(foods);
		this.foods = listCopy;
	}
	
	/**
	 * @return Returns if the food to withdraw is a constant number.
	 */
	public boolean isBankStyleConstant() {
		return isBankStyleConstant;
	}
	
	/**
	 * Sets if the bank style is constant.
	 * @param isBankStyleConstant
	 */
	public void setBankStyleConstant(boolean isBankStyleConstant) {
		this.isBankStyleConstant = isBankStyleConstant;
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
}
