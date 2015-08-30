package me.fru1t.rsbot.common.script.rt6;

import java.util.concurrent.Callable;

import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Component;
import org.powerbot.script.rt6.Constants;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.common.collections.Tuple2;
import me.fru1t.rsbot.common.framework.util.Condition;
import me.fru1t.rsbot.common.script.MouseUtil;

// TODO: Add impatient return (interacts will return before validating)
@Singleton
public class BankUtil {
	private static final Tuple2<Integer, Integer> INTERACT_WAIT_RANGE = Tuple2.of(800, 2200);

	private final ClientContext ctx;
	private final MouseUtil interactUtil;

	@Inject
	public BankUtil(
			@Singleton ClientContext ctx,
			@Singleton MouseUtil interactUtil) {
		this.ctx = ctx;
		this.interactUtil = interactUtil;
	}

	/**
	 * Interacts with the deposit inventory button in a human-like way. Blocks until action is
	 * complete.
	 *
	 * @return True if the inventory deposited (or that the inventory was already empty).
	 * Otherwise, false.
	 */
	public boolean depositInventory() {
		return interact(
				Constants.BANK_DEPOSIT_INVENTORY,
				new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						return ctx.backpack.isEmpty();
					}
				});
	}

	/**
	 * Interacts with the preset 1 button in a human-like way. Blocks until the action is complete.
	 * The method has no knowledge to verify if the contents of the preset successfully made it to
	 * the inventory or equipment.
	 *
	 * @return True if the preset button action completed (or the bank closed by itself).
	 * Otherwise false.
	 */
	public boolean preset1() {
		return interact(
				Constants.BANK_LOAD1,
				new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						return !ctx.bank.opened();
					}
				});
	}

	/**
	 * Interacts with the preset 2 button in a human-like way. Blocks until the action is complete.
	 * The method has no knoweldge to verify if the contents of the preset successfully made it to
	 * the inventory or equipment.
	 *
	 * @return True if the preset button action completed (or the bank closed by itself).
	 * Otherwise false.
	 */
	public boolean preset2() {
		return interact(
				Constants.BANK_LOAD2,
				new Callable<Boolean>() {
					@Override
					public Boolean call() throws Exception {
						return !ctx.bank.opened();
					}
				});
	}

	private boolean interact(int bankComponentId, Callable<Boolean> condition) {
		if (!ctx.bank.opened()) {
			return false;
		}

		Component component = ctx.widgets.component(Constants.BANK_WIDGET, bankComponentId);
		if (!component.valid() || !component.visible() || !component.inViewport()) {
			return false;
		}

		interactUtil.click(component);
		return Condition.wait(condition, INTERACT_WAIT_RANGE);
	}
}
