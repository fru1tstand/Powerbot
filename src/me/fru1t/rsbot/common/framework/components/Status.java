package me.fru1t.rsbot.common.framework.components;

import java.util.Stack;

import me.fru1t.common.annotations.Singleton;

/**
 * Keeps track of a script's status messages. These should only be used for decoration
 * purposes (script paint, debug) only. To track status of a script, use {@link RunState} instead.
 *
 * <p>All framework components are provided through Slick. To use this component, inject it with a
 * Provider in the form of Provider&lt;Persona&gt;</p>
 */
@Singleton
public class Status {
	/**
	 * The default size of the status history stack
	 */
	private static final int DEFAULT_SIZE = 20;

	/**
	 * Show status messages in the console
	 */
	private static final boolean SHOW_STATUS_IN_CONSOLE = true;

	private final Stack<String> statusStack;
	private final int stackSize;

	/**
	 * Creates a status with the default status size.
	 */
	public Status() {
		this(DEFAULT_SIZE);
	}

	/**
	 * Creates a status that holds the given number of messages.
	 * @param size The size of the status stack.
	 */
	public Status(int size) {
		this.statusStack = new Stack<String>();
		this.stackSize = size;
	}

	/**
	 * Updates the script status.
	 * @param status The status to display
	 */
	public void update(String status) {
		if (SHOW_STATUS_IN_CONSOLE) {
			System.out.println(status);
		}
		statusStack.push(status);
		statusStack.setSize(stackSize);
	}

	/**
	 * @return The current string status.
	 */
	public String getCurrent() {
		return statusStack.peek();
	}

	/**
	 * @return The entire status history stack.
	 */
	@SuppressWarnings("unchecked")
	public Stack<String> getAll() {
		return (Stack<String>) statusStack.clone();
	}
}
