package me.fru1t.rsbot;

import java.util.HashMap;
import java.util.Map;

import org.powerbot.script.Locatable;
import org.powerbot.script.Script.Manifest;
import org.powerbot.script.Tile;
import org.powerbot.script.rt6.ClientContext;

import me.fru1t.annotations.Nullable;
import me.fru1t.rsbot.common.framework.Action;
import me.fru1t.rsbot.common.framework.Script;
import me.fru1t.rsbot.safecracker.Settings;
import me.fru1t.rsbot.safecracker.StartupForm;
import me.fru1t.rsbot.safecracker.actions.BankWalk;
import me.fru1t.rsbot.safecracker.actions.SafeCrack;
import me.fru1t.rsbot.safecracker.actions.SafeEat;

@Manifest(
		name = "Rogue's Den Safe Cracker",
		description = "Cracks safes in Rogue's Den",
		properties = "client=4;topic=1278748;hidden=false;")
public class RoguesDenSafeCracker extends Script<ClientContext, RoguesDenSafeCracker.State, Settings> {
	public static final int[] SAFE_OBJECT_BOUNDS_MODIFIER = {-244, 244, -1140, 0, -64, 128};
	public static final int SAFE_OBJECT_ID = 7235;
	public static final int SAFE_OPENED_OBJECT_ID = 64296;
	public static final int SAFE_SPIKES_OBJECT_ID = 7227;
	public static final int PLAYER_CRACK_ANIMATION = 15576;
	public static final int PLAYER_CRACK_PRE_HURT_ANIMATION = 15575;
	public static final int PLAYER_HURTING_ANIMATION = 18353;
	public static final String MENU_CRACK_ACTIVE_TEXT = "Crack";
	
	/**
	 * Defines this script's possible states.
	 */
	public enum State {
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
	
	/**
	 * The safes to crack with data associated to each safe.
	 */
	public enum Safe {
		AUTOMATIC(null, null),
		SW(new Tile(3041, 4957), new Tile(3041, 4956)),
		SE(new Tile(3043, 4957), new Tile(3043, 4956)),
		NW(new Tile(3041, 4962), new Tile(3041, 4963)),
		NE(new Tile(3043, 4962), new Tile(3043, 4963));
		
		public final Tile location;
		public final Tile playerLocation;
		private Safe(Tile location, Tile playerLocation) {
			this.location = location;
			this.playerLocation = playerLocation;
		}
		
		/**
		 * Returns the corresponding Safe Enum from the given Locatable.
		 * @param l
		 * @return The Safe object corresponding to the Locatable, or null if one is not found.
		 */
		@Nullable
		public static Safe fromLocation(Locatable l) {
			for (Safe safe : Safe.values()) {
				if (safe.location.equals(l.tile())) {
					return safe;
				}
			}
			return null;
		}
	}

	@Override
	public void init() {
		showStartupForm(StartupForm.class);
	}

	@Override
	protected Map<State, Class<? extends Action>> getActionMap() {
		Map<State, Class<? extends Action>> stateMap = new HashMap<>();
		stateMap.put(State.SAFE_CRACK, SafeCrack.class);
		stateMap.put(State.SAFE_EAT, SafeEat.class);
		stateMap.put(State.BANK_WALK, BankWalk.class);
		return stateMap;
	}
	
	@Override
	protected State getResetState() {
		return State.UNKNOWN;
	}
}
