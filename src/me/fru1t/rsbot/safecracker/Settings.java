package me.fru1t.rsbot.safecracker;

import java.util.List;

import me.fru1t.rsbot.RoguesDenSafeCracker;
import me.fru1t.rsbot.common.food.AllFood;
import me.fru1t.rsbot.framework.generics.GenericSettings;

public class Settings extends GenericSettings {
	private List<AllFood> foods;
	private boolean isBankStyleConstant;
	private RoguesDenSafeCracker.Safe preferredSafe;
	private int currentFoodPointer;
	
	/**
	 * Simple constructor which sets default values to class fields.
	 */
	public Settings() {
		this.foods = null;
		this.isBankStyleConstant = false;
		this.preferredSafe = null;
		this.currentFoodPointer = 0;
	}
	
	/**
	 * Increments the food pointer and returns if there is more food queued in the foodlist.
	 * @return
	 */
	public boolean ranOutOfFood() {
		currentFoodPointer++;
		return currentFoodPointer >= foods.size();
	}
	
	public AllFood getCurrentFood() {
		return foods.get(currentFoodPointer);
	}
	
	/**
	 * TODO: Abstract with reflection
	 */
	@Override
	public void replace(GenericSettings settings) {
		if (!(settings instanceof Settings))
			return;
		Settings other = (Settings) settings;
		
		this.foods = other.foods;
		this.isBankStyleConstant = other.isBankStyleConstant;
		this.preferredSafe = other.preferredSafe;
	}
	
	@Override
	public boolean isValid() {
		return foods != null && preferredSafe != null;
	}
	
	public List<AllFood> getFoods() {
		return foods;
	}
	
	public void setFoods(List<AllFood> foods) {
		this.foods = foods;
	}
	
	public boolean isBankStyleConstant() {
		return isBankStyleConstant;
	}
	
	public void setBankStyleConstant(boolean isBankStyleConstant) {
		this.isBankStyleConstant = isBankStyleConstant;
	}
	
	public RoguesDenSafeCracker.Safe getPreferredSafe() {
		return preferredSafe;
	}
	
	public void setPreferredSafe(RoguesDenSafeCracker.Safe preferredSafe) {
		this.preferredSafe = preferredSafe;
	}
}
