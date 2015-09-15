package me.fru1t.rsbot.common.script.rt6;

import java.util.ArrayList;
import java.util.List;

import me.fru1t.slick.util.Provider;
import org.powerbot.script.rt6.ClientContext;
import org.powerbot.script.rt6.Item;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Singleton;
import me.fru1t.rsbot.common.framework.util.Condition;

/**
 * Provides utility methods for interacting with the RS3 backpack.
 */
// TODO(v1 cleanup): Find instances of BackpackUtil and convert to Backpack.
@Singleton
public class Backpack {
	private static final int MAX_BACKPACK_SIZE = 28;

	private final Provider<ClientContext> ctxProvider;
	private final Mouse mouse;
	private final Condition condition;

	@Inject
	public Backpack(
			Provider<ClientContext> ctxProvider,
			@Singleton Mouse mouse,
			@Singleton Condition condition) {
		this.ctxProvider = ctxProvider;
		this.mouse = mouse;
		this.condition = condition;
	}

	/**
	 * Returns whether or not the backpack is full. Resets the Backpack query.
	 * @return Whether or not the backpack is full.
	 */
	public boolean isFull() {
		return ctxProvider.get().backpack.select().size() == MAX_BACKPACK_SIZE;
	}

	/**
	 * Left clicks {@code n} items within the backpack that are of the given {@code id}. This
	 * method will not fail if less than {@code n} items exist within the backpack, and instead,
	 * simply clicks on the available ones.
	 *
	 * @param id The id of the item to click.
	 * @param n The number of items to click.
	 * @return True once the items have been clicked and are verified to be invalid.
	 * False otherwise.
	 */
	public boolean clickNOf(int id, int n) {
		final ClientContext ctx = ctxProvider.get();
		n = ctx.backpack.select().id(id).count() < n ? ctx.backpack.count() : n;
		List<Item> items = new ArrayList<Item>();
		ctx.backpack.limit(n).addTo(items);

		while (!items.isEmpty()) {
			for (int i = 0; i < items.size(); i++) {
				// TODO(v1): Check if interactions re-open backpack.
				if (ctx.backpack.collapsed()) {
					return false;
				}

				if (!items.get(i).valid()) {
					items.remove(i);
				}

				if (!items.get(i).inViewport()) {
					ctx.backpack.scroll(items.get(i));
				}
				mouse.click(items.get(i));
				condition.sleepForInteractDelay();
			}
			condition.sleepForInteractDelay();
		}
		return true;
	}

	/**
	 * Checks if the backpack contains the item {@code ids}.
	 *
	 * @param ids The item ids to check for.
	 * @return True if the items exist in the backpack. False otherwise.
	 */
	public boolean contains(int... ids) {
		return !ctxProvider.get().backpack.select().id(ids).isEmpty();
	}
}
