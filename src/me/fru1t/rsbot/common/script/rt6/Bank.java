package me.fru1t.rsbot.common.script.rt6;

import java.awt.Point;

import me.fru1t.slick.util.Provider;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Component;
import org.powerbot.script.rt6.Constants;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.common.collections.Tuple2;
import me.fru1t.rsbot.common.framework.components.Persona;
import me.fru1t.rsbot.common.framework.util.Callable;
import me.fru1t.rsbot.common.framework.util.Condition;
import me.fru1t.rsbot.common.util.Random;
import me.fru1t.rsbot.common.util.Timer;

// TODO(v2): Add impatient return (interacts will return before validating)
// TODO(v1 cleanup): Find instances of BankUtil and convert to Bank.
@Singleton
public class Bank {
	/**
	 * After interacting with the bank to open the widget, before the bank appears, some people
	 * like to hover over where the next widget button will appear.
	 */
	private class WidgetHoverLogic {
		private static final int ENABLED_PROBABILITY = 35;

		// This percent dictates how off one might be to the actual location of the button. This
		// is a percent of the total size of the entire game window.
		private final Tuple2<Integer, Integer> GUESS_PERCENT_TOLERANCE = Tuple2.of(5, 20);

		private final boolean isEnabled;

		@Inject
		public WidgetHoverLogic() {
			this.isEnabled = Random.roll(ENABLED_PROBABILITY);
		}

		/**
		 * @return Returns if the player should try to guess where the button is.
		 */
		public boolean shouldAttempt() {
			// TODO(v2): Doesn't have to be on or off completely.
			return isEnabled;
		}

		/**
		 * @param component The component that the player is trying to tend toward.
		 * @return Returns the location the mouse should move towards as a guess.
		 */
		public Point getPrediction(Component component) {
			int maxPercentOffset =
					personaProvider.get().getLazinessScaledInt(GUESS_PERCENT_TOLERANCE);
			int xMaxOffset =
					ctxProvider.get().game.dimensions().width * maxPercentOffset / 100 / 2;
			int yMaxOffset =
					ctxProvider.get().game.dimensions().height * maxPercentOffset / 100 / 2;
			Point result = new Point();
			result.setLocation(
					component.centerPoint().getX() + Random.nextInt(-1 * xMaxOffset, xMaxOffset),
					component.centerPoint().getY() + Random.nextInt(-1 * yMaxOffset, yMaxOffset));
			return result;
		}
	}

	private static final Tuple2<Integer, Integer> INTERACT_WAIT_RANGE = Tuple2.of(800, 2200);
	private static final Tuple2<Integer, Integer> OPEN_WAIT_RANGE = Tuple2.of(1200, 2500);

	private final Provider<ClientContext> ctxProvider;
	private final Mouse mouseUtil;
	private final Timer bankOpenTimer;
	private final Provider<Persona> personaProvider;

	private final WidgetHoverLogic widgetHoverLogic;

	@Inject
	public Bank(
			Provider<ClientContext> ctxProvider,
			Provider<Persona> personaProvider,
			@Singleton Mouse mouse,
			Timer bankOpenTimer) {
		this.ctxProvider = ctxProvider;
		this.mouseUtil = mouse;
		this.bankOpenTimer = bankOpenTimer;
		this.personaProvider = personaProvider;

		this.widgetHoverLogic = new WidgetHoverLogic();
	}


	/* Other */
	/**
	 * Waits until the bank is opened.
	 *
	 * @return True if the bank opened. False otherwise.
	 */
	public boolean waitForBankToOpen() {
		return Condition.wait(
				new Callable<Boolean>() {
					@Override
					public Boolean ring() {
						return ctxProvider.get().bank.opened();
					}
				},
				new Callable<Boolean>() {
					@Override
					public Boolean ring() {
						return ctxProvider.get().players.local().inMotion();
					}

				},
				bankOpenTimer,
				Random.nextInt(OPEN_WAIT_RANGE),
				150);
	}


	/* Full methods */
	/**
	 * Deposits the entire inventory in the bank. Safe to call before the bank opens.
	 * @return True if the items successfully deposited or the inventory was empty to begin with.
	 */
	public boolean depositInventory() {
		// TODO(v2): Add manual depositing
		return clickDepositInventory();
	}


	/* Button interaction methods */
	/**
	 * Interacts with the given bank component id in a human-like fashion and waits for the given
	 * condition.
	 *
	 * @param bankComponentId
	 * @param condition
	 * @return True if the steps and condition successfully completed. False otherwise.
	 */
	private boolean clickComponent(int bankComponentId, Callable<Boolean> condition) {
		Component component = getBankComponent(bankComponentId);

		if (!ctxProvider.get().bank.opened()) {
			if (widgetHoverLogic.shouldAttempt()) {
				ctxProvider.get().input.move(widgetHoverLogic.getPrediction(getDepositInventoryComponent()));
			}
			if (!waitForBankToOpen()) {
				return false;
			}
		}

		if (!component.valid() || !component.visible() || !component.inViewport()) {
			return false;
		}

		mouseUtil.click(component);
		return Condition.wait(condition, INTERACT_WAIT_RANGE);
	}

	/**
	 * Interacts with the deposit inventory button in a human-like way. Blocks until action is
	 * complete. Safe to call before bank opens.
	 *
	 * @return True if the inventory deposited (or that the inventory was already empty).
	 * Otherwise, false.
	 */
	public boolean clickDepositInventory() {
		return clickComponent(
				Constants.BANK_DEPOSIT_INVENTORY,
				new Callable<Boolean>() {
					@Override
					public Boolean ring() {
						return ctxProvider.get().backpack.isEmpty();
					}
				});
	}

	/**
	 * Interacts with the preset 1 button in a human-like way. Blocks until the action is complete.
	 * The method has no knowledge to verify if the contents of the preset successfully made it to
	 * the inventory or equipment. Safe to call before bank opens.
	 *
	 * @return True if the preset button action completed (or the bank closed by itself).
	 * Otherwise false.
	 */
	public boolean clickPreset1() {
		return clickComponent(
				Constants.BANK_LOAD1,
				new Callable<Boolean>() {
					@Override
					public Boolean ring() {
						return !ctxProvider.get().bank.opened();
					}
				});
	}

	/**
	 * Interacts with the preset 2 button in a human-like way. Blocks until the action is complete.
	 * The method has no knowledge to verify if the contents of the preset successfully made it to
	 * the inventory or equipment. Safe to call before bank opens.
	 *
	 * @return True if the preset button action completed (or the bank closed by itself).
	 * Otherwise false.
	 */
	public boolean clickPreset2() {
		return clickComponent(
				Constants.BANK_LOAD2,
				new Callable<Boolean>() {
					@Override
					public Boolean ring() {
						return !ctxProvider.get().bank.opened();
					}
				});
	}


	/* Component getters */
	/**
	 * Returns the bank component object associated to the given id. Does not safely check if the
	 * component id is valid.
	 *
	 * @param bankComponentId
	 * @return The Component object associated to the passed component id.
	 */
	private Component getBankComponent(int bankComponentId) {
		return ctxProvider.get().widgets.component(Constants.BANK_WIDGET, bankComponentId);
	}

	// TODO(v1 cleanup): is this still needed?
	/**
	 * Returns the deposit inventory component from the banking widget.
	 * @return The deposit inventory component.
	 */
	public Component getDepositInventoryComponent() {
		return getBankComponent(Constants.BANK_DEPOSIT_INVENTORY);
	}
}
