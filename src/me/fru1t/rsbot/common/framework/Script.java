package me.fru1t.rsbot.common.framework;

import java.util.HashMap;
import java.util.Map;

import org.powerbot.script.ClientContext;
import org.powerbot.script.PollingScript;

import me.fru1t.rsbot.common.framework.components.Persona;
import me.fru1t.rsbot.common.framework.components.RunState;
import me.fru1t.rsbot.common.framework.components.Status;
import me.fru1t.slick.Slick;

/**
 * Provides a basic shell of a state-driven script.
 *
 * @param <C> The ClientContext for this script (rt4/rt6).
 * @param <ST> The enum that's used to represent all possible script states.
 * @param <T> The settings that this script uses.
 */
public abstract class Script<
		C extends ClientContext<?>,
		ST extends Enum<ST> & StateInterface<ST>,
		T extends AbstractSettings> extends PollingScript<C> {
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
	 * @return The reset state for the script if something goes horribly wrong.
	 */
	protected abstract ST getResetState();


	private final Map<ST, Strategy<ST>> scriptActions;
	private final Slick slick;
	private final Status status;
	private final RunState<ST> state;

	private int consecutiveFailures;

	/**
	 * Creates a new empty script
	 */
	protected Script() {
		this.scriptActions = new HashMap<ST, Strategy<ST>>();
		this.status = new Status();
		this.state = new RunState<ST>();
		this.slick = new Slick()
				.provide(ctx)
				.provide(state)
				.provide(status)
				.provide(new Persona());

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
		ST returnState = scriptActions.get(state.getCurrent()).run();
		if (returnState == null) {
			consecutiveFailures++;
		} else {
			consecutiveFailures = 0;
			state.update(returnState);
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
	 * @param formClass The class corresponding to the startup form.
	 */
	protected final <F extends AbstractStartupForm<T>> void showStartupForm(Class<F> formClass) {
		status.update("Waiting for user input");
		// Isolate the startup form and callback from our main instance of slick as there's no need
		// to pollute it.
		new Slick()
				// Provide the form's required callback
				.provide(new AbstractSettings.Callback<T>() {
					@Override
					public void call(T settings) {
						setSettings(settings);
						scriptReady();
					}
				})

				// Create the form
				.get(formClass);
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
		slick.provide(settings);
		status.update("Settings have been set.");
	}

	/**
	 * Flips the switch. Tells the script to start running.
	 */
	protected final void scriptReady() {
		// Verify all states are handled
		ST[] states = getResetState().getDeclaringClass().getEnumConstants();
		for (ST state : states) {
			if (state.getControllingClass() == null) {
				throw new RuntimeException(String.format(
						"%s state has no controlling class",
						state.name()));
			}
			scriptActions.put(state, slick.get(state.getControllingClass()));
		}

		//
		if (scriptActions.size() == 0) {
			throw new RuntimeException("The script was told to run, "
					+ "but the script action map has no actions in it.");
		}
		state.update(getResetState());
	}
}
