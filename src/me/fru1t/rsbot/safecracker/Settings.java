package me.fru1t.rsbot.safecracker;

import java.util.List;

import me.fru1t.rsbot.common.food.AllFood;

public class Settings {
	private List<Enum<AllFood>> foods;
	private boolean isBankStyleConstant;
	private Enum<Safe> preferredSafe;
	
	public List<Enum<AllFood>> getFoods() {
		return foods;
	}
	public void setFoods(List<Enum<AllFood>> foods) {
		this.foods = foods;
	}
	public boolean isBankStyleConstant() {
		return isBankStyleConstant;
	}
	public void setBankStyleConstant(boolean isBankStyleConstant) {
		this.isBankStyleConstant = isBankStyleConstant;
	}
	public Enum<Safe> getPreferredSafe() {
		return preferredSafe;
	}
	public void setPreferredSafe(Enum<Safe> preferredSafe) {
		this.preferredSafe = preferredSafe;
	}
}
