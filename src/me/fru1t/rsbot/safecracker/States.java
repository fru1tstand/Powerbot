package me.fru1t.rsbot.safecracker;

/**
 * Contains all possible script states for Rogue's Den Safe Cracker
 */
public enum States {
	// Other
	UNKNOWN,
	
	// Bank
	BANK_WALK,
	BANK_OPEN,
	BANK_INTERACT,
	
	// Safe cracking
	SAFE_WALK,
	SAFE_CRACK,
	SAFE_EAT
}
