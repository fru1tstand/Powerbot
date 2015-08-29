package me.fru1t.rsbot.common.framework;

/**
 * A strategy is a very loose term for a set of consecutive actions a player should perform. These
 * are the core blocks of a script, defining the steps to complete a goal. Strategies can be as
 * large as defining the steps of completing an agility course, or as small as a simple button
 * click. It is up to the script writer to define the scope of a strategy. Because each strategy
 * coincides with a state, a good way to think about a strategy is the method a player will take
 * to get from a given state to the next progressive state.
 * 
 * <p>I've found that the most human-like scripts don't define absolute bounds between strategies,
 * but more, allow for the next strategy to start executing, and allow that strategy to correct for
 * possible errors. For example, a banking strategy may withdraw items and update the script state.
 * The next strategy may start running towards a destination and in the split second the player
 * starts running, checks if the items exist in the backpack. If not (due to misclick or other
 * error), the run strategy will take corrective measure by updating the state to a known fix.
 * 
 * @param ST The script state enum
 */
public interface Strategy<ST extends Enum<?>> {
	/**
	 * The action to perform.
	 * @return If the run completed successfully.
	 */
	public abstract ST run();
}
