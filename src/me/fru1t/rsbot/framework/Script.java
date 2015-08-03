package me.fru1t.rsbot.framework;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.powerbot.script.ClientContext;
import org.powerbot.script.PollingScript;

import me.fru1t.rsbot.framework.generics.GenericPersona;
import me.fru1t.rsbot.framework.generics.GenericSettings;
import me.fru1t.rsbot.framework.generics.GenericStartupForm;

public abstract class Script<
		C extends ClientContext<?>, 
		ST extends Enum<ST>, 
		T extends GenericSettings,
		P extends GenericPersona<C, T>> extends PollingScript<C> {
	private static final int STATUS_HISTORY_SIZE = 20;
	private static final boolean SHOW_CONSOLE_MESSAGES = true;
	private static final int CONSECUTIVE_FAILURE_RESET_THRESHOLD = 20;
	private final Stack<String> statusStack;
	private final Map<ST, Action<C, ?, T, P>> scriptActions;
	public final T settings;
	public final P persona;
	public final C ctx;
	private ST currentState;
	private ST lastState;
	private int consecutiveFailures;
	
	/**
	 * Simple constructor which sets empty class fields.
	 */
	protected Script(T settings, P persona) {
		this.scriptActions = new HashMap<>();
		this.statusStack = new Stack<>();
		this.settings = settings;
		this.persona = persona;
		this.ctx = super.ctx;
		this.currentState = null;
		this.lastState = null;
		this.consecutiveFailures = 0;
	}

	@Override
	public void poll() {
		if (currentState == null
				|| settings == null
				|| scriptActions.size() == 0
				|| persona == null) {
			return;
		}
		
		if (!scriptActions.containsKey(currentState)) {
			updateStatus(String.format(
					"Please spam Fru1tstand with this error: Unhandled state '%s'.",
					currentState.name()));
			stop();
			return;
		}
		
		if (!scriptActions.get(currentState).run()) {
			consecutiveFailures++;
		} else {
			consecutiveFailures = 0;
		}
		if (consecutiveFailures > CONSECUTIVE_FAILURE_RESET_THRESHOLD) {
			updateState(getResetState());
		}
	}
	
	@Override
	public final void start() {
		updateStatus(String.format(
				"Starting %s By Fru1tstand",
				this.getClass().getAnnotation(Manifest.class).name()));
		init();
	}
	
	@Override
	public final void stop() {
		updateStatus(String.format(
				"Stopping %s By Fru1tstand",
				this.getClass().getAnnotation(Manifest.class).name()));
	}
	
	// *** Script configuration
	/**
	 * Callable by the implementing class, this method displays the given startup form and sets
	 * the settings when the form is closed.
	 * @param formClazz The class corresponding to the startup form.
	 */
	protected final <F extends GenericStartupForm<T>> void showStartupForm(Class<F> formClazz) {
		updateStatus("Waiting for user input");
		try {
			Constructor<F> constructor = formClazz.getConstructor(SettingsCallback.class);
			constructor.newInstance(new SettingsCallback<T>() {
				@Override
				public void call(T settings) {
					setSettings(settings);
				}
			});
		} catch (NoSuchMethodException
				| SecurityException
				| InstantiationException
				| IllegalAccessException
				| IllegalArgumentException
				| InvocationTargetException e) {
			System.out.println("Could not create startup form or apply settings.");
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the script's settings and indicates that the script is ready to run.
	 */
	protected final void setSettings(T settings) {
		updateStatus("Settings set, preparing script for run.");
		if (!settings.isValid()) {
			updateStatus("Settings are invalid or incomplete. "
					+ "Please restart the script and try again.");
			return;
		}
		try {
			for (Map.Entry<ST, Class<? extends Action<C, ?, T, P>>>
					entry : getActionMap().entrySet()) {
				Constructor<? extends Action<C, ?, T, P>> actionConstructor =
						entry.getValue().getConstructor(this.getClass());
				scriptActions.put(
						entry.getKey(),
						actionConstructor.newInstance(this));
			}
		} catch (NoSuchMethodException
				| SecurityException
				| InstantiationException
				| IllegalAccessException
				| IllegalArgumentException
				| InvocationTargetException e) {
			System.out.println("Could not create script action.");
			e.printStackTrace();
		}
		this.settings.replace(settings);
		this.persona.setScript(this);
		
		// Set ready
		updateState(getResetState());
	}
	
	// *** Status methods
	/**
	 * Updates the script status (decorational purposes only).
	 * @param status The status to display
	 */
	public void updateStatus(String status) {
		if (SHOW_CONSOLE_MESSAGES) {
			System.out.println(status);
		}
		statusStack.push(status);
		statusStack.setSize(STATUS_HISTORY_SIZE);
	}
	
	/**
	 * @return The current string status.
	 */
	public String getCurrentStatus() {
		return statusStack.peek();
	}
	
	/**
	 * @return The entire status history stack.
	 */
	@SuppressWarnings("unchecked")
	public Stack<String> getStatusStack() {
		return (Stack<String>) statusStack.clone();
	}
	
	// *** State methods
	/**
	 * Sets the current state to the passed new state
	 * @param newState
	 */
	public void updateState(ST newState) {
		this.lastState = this.currentState;
		this.currentState = newState;
	}
	
	/**
	 * @return The current state
	 */
	public ST getCurrentState() {
		return currentState;
	}
	
	/**
	 * @return The previous state
	 */
	public ST getLastState() {
		return lastState;
	}
	
	// *** Abstract
	/**
	 * @return Returns each script state mapped to the class that handles it.
	 */
	protected abstract Map<ST, Class<? extends Action<C, ?, T, P>>> getActionMap();
	
	/**
	 * Synonymous to {@link #start()}
	 */
	protected abstract void init();
	
	/**
	 * @return The reset state for the script if something goes horribly wrong.
	 */
	protected abstract ST getResetState();
}
