package me.fru1t.rsbot.framework;

import java.util.HashMap;
import java.util.Map;

import org.powerbot.script.ClientContext;
import org.powerbot.script.PollingScript;

import me.fru1t.rsbot.framework.components.RunState;
import me.fru1t.rsbot.framework.components.Status;
import me.fru1t.rsbot.framework.generics.GenericSettings;
import me.fru1t.rsbot.framework.generics.GenericStartupForm;
import me.fru1t.slick.Slick;

public abstract class Script<
		C extends ClientContext<?>,
		ST extends Enum<ST>,
		T extends GenericSettings> extends PollingScript<C> {
	/**
	 * The number of failures to endure before resetting the script state
	 */
	private static final int CONSECUTIVE_FAILURE_RESET_THRESHOLD = 20;
	
	/**
	 * Synonymous to {@link #start()}
	 * This is where the GUI or settings setup should take place.
	 */
	protected abstract void init();
	
	/**
	 * @return Returns each script state mapped to the class that handles it.
	 */
	protected abstract Map<ST, Class<? extends Action>> getActionMap();
	
	/**
	 * @return The reset state for the script if something goes horribly wrong.
	 */
	protected abstract ST getResetState();
	
	/**
	 * This is used at runtime to verify that all states have a handling class when
	 * {@link #setUpActions()} is called.
	 * @return The entire state enum as a class.
	 */
	protected abstract Class<ST> getStateClass();
	

	private final Map<ST, Action> scriptActions;
	private final Slick slick;
	private final Status status;
	private final RunState<ST> state;
	
	private int consecutiveFailures;
	
	/**
	 * Creates a new empty script
	 */
	protected Script() {
		slick = new Slick();
		status = new Status();
		state = new RunState<>();
		
		slick.provide(ctx.getClass(), ctx);
		slick.provide(Status.class, status);
		slick.provide(Persona.class, new Persona());
		slick.provide(RunState.class, state);
		
		this.scriptActions = new HashMap<>();
	}

	@Override
	public void poll() {
		if (consecutiveFailures > CONSECUTIVE_FAILURE_RESET_THRESHOLD) {
			consecutiveFailures = 0;
			state.update(getResetState());
		}
		
		if (state.getCurrent() == null) {
			return;
		}
		
		// We're guaranteed all states are handled from the setup method
		if (!scriptActions.get(state.getCurrent()).run()) {
			consecutiveFailures++;
		} else {
			consecutiveFailures = 0;
		}
	}
	
	@Override
	public final void start() {
		status.update(String.format(
				"Starting %s By Fru1tstand", getClass().getAnnotation(Manifest.class).name()));
		init();
	}
	
	@Override
	public final void stop() {
		status.update(String.format(
				"Stopping %s By Fru1tstand", getClass().getAnnotation(Manifest.class).name()));
	}
	
	
	/**
	 * Callable by the implementing class, this method displays the given startup form and sets
	 * the settings when the form is closed.
	 * @param formClazz The class corresponding to the startup form.
	 */
	protected final <F extends GenericStartupForm<T>> void showStartupForm(Class<F> formClazz) {
		status.update("Waiting for user input");
		slick.provide(SettingsCallback.class, new SettingsCallback<T>() {
			@Override
			public void call(T settings) {
				setSettings(settings);
				setUpActions();
				scriptReady();
			}
		});
		
		// The form should self-instantiate and become visible.
		slick.get(formClazz);
	}
	
	/**
	 * Sets the script's settings and indicates that the script is ready to run.
	 */
	protected final void setSettings(T settings) {
		if (!settings.isValid()) {
			// TODO: Generate a more user friendly response. Sometimes !isValid is a user-specific
			// error.
			throw new RuntimeException(String.format(
					"%s: Something about the settings was not right. "
							+ "Please post this error in the forum along with your settings so "
							+ "that Fru1tstand can fix this issue.",
					getClass().getAnnotation(Manifest.class).name()));
		}
		slick.provide(settings.getClass(), settings);
		status.update("Settings have been set.");
	}
	
	/**
	 * Instantiates the actions for the script. This must be called after the settings have been
	 * set, and all dependencies that each Action may have are provided to the slick instance.
	 */
	protected final void setUpActions() {
		status.update("Preparing script to run...");
		for (Map.Entry<ST, Class<? extends Action>> entry : getActionMap().entrySet()) {
			scriptActions.put(entry.getKey(), slick.get(entry.getValue()));
		}
		ST[] states = getStateClass().getEnumConstants();
		for (ST state : states) {
			if (scriptActions.containsKey(state)) {
				throw new RuntimeException(
						String.format("%s state has no handling class.", state.name()));
			}
		}
	}
	
	/**
	 * Flips the switch. Tells the script to start running.
	 */
	protected final void scriptReady() {
		// Script action map will only fully populate when dependencies are met and all states are
		// verified to be handled.
		if (scriptActions.size() == 0) {
			throw new RuntimeException("The script was told to run, "
					+ "but the script action map has no actions in it.");
		}
		state.update(getResetState());
	}
}
