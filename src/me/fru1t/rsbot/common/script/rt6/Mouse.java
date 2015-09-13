package me.fru1t.rsbot.common.script.rt6;

import me.fru1t.slick.util.Provider;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Interactive;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.common.framework.components.Persona;
import me.fru1t.rsbot.common.framework.util.Condition;

// TODO(v1 cleanup): Find usages of MouseUtil and convert to Mouse
@Singleton
public class Mouse extends me.fru1t.rsbot.common.script.Mouse<ClientContext> {
	private final Provider<ClientContext> ctxProvider;

	@Inject
	public Mouse(
			Provider<ClientContext> ctxProvider,
			Provider<Persona> personaProvider) {
		super(personaProvider);

		this.ctxProvider = ctxProvider;
	}

	/**
	 * Left clicks on the given interactive object with a human-like interaction.
	 *
	 * <p> Warning: It's the caller's responsibility to have the object on screen.
	 *
	 * <p> Warning: This method does not check for unexpected objects on the screen that may
	 * disrupt intended actions. It is the caller's responsibility to check if an accidental
	 * interaction occurred.
	 *
	 * @param interactive
	 * @return Returns true if no errors occurred, but does not guarantee that the interaction
	 * completed successfully. Otherwise, false.
	 */
	// TODO(v2): Add right click interacting
	public boolean click(Interactive interactive) {
		if (!interactive.inViewport()) {
			return false;
		}

		int clicks = getClicks();
		boolean isFirstHover = true;
		while (clicks-- > 0) {
			if (isFirstHover || shouldCorrectMouse()) {
				isFirstHover = false;
				interactive.hover();
			}
			ctxProvider.get().input.click(true);

			if (clicks > 0) {
				Condition.sleep(personaProvider.get().getNextSpamDelay());
			}
		}
		return true;
	}
}
