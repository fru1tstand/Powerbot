package me.fru1t.rsbot.common.script.rt6;

import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Interactive;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.common.framework.components.Persona;
import me.fru1t.rsbot.common.framework.util.Condition;

public class MouseUtil extends me.fru1t.rsbot.common.script.MouseUtil<ClientContext> {
	private final ClientContext ctx;

	@Inject
	public MouseUtil(
			@Singleton ClientContext ctx,
			@Singleton Persona persona) {
		super(persona);

		this.ctx = ctx;
	}

	/**
	 * Left clicks on the given interactive object with a human-like interaction.
	 *
	 * <p> Warning: It's the caller's responsibility to have the object on screen.
	 *
	 * @param interactive
	 * @return Returns true if no errors occurred, but does not guarantee that the interaction
	 * completed successfully. Otherwise, false.
	 */
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
			ctx.input.click(true);

			if (clicks > 0) {
				Condition.sleep(persona.getNextSpamDelay());
			}
		}
		return true;
	}
}
