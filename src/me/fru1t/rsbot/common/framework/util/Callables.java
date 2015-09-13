package me.fru1t.rsbot.common.framework.util;

public final class Callables {
	private Callables() { }

	/**
	 * Creates a simple Callable that is of the given parameter's type and that only returns the
	 * given parameter.
	 *
	 * @param response The response that the returning Callable's call method should return.
	 * @return A Callable object that returns the given response.
	 */
	public static <T> Callable<T> of(final T response) {
		return new Callable<T>() {
			@Override
			public T ring() {
				return response;
			}
		};
	}

	/**
	 * Creates a boolean callable that checks if the passed interactive (actor, component,
	 * gameobject, grounditem, item, tilematrix) is in the viewport.
	 *
	 * @param interactive The interactive to check.
	 * @return True if the interactive is in the viewport. False otherwise.
	 */
	public static Callable<Boolean> inViewport(
			final org.powerbot.script.rt6.Interactive interactive) {
		return new Callable<Boolean>() {
			@Override
			public Boolean ring() {
					return interactive.inViewport();
				}
			};
	}

	/**
	 * Creates a boolean callable that checks if the passed interactive (actor, component,
	 * gameboject, grounditem, item, tilematrix) is in the viewport.
	 *
	 * @param interactive The interactive to check.
	 * @return True if the interactive is in the viewport. False otherwise.
	 */
	public static Callable<Boolean> inViewport(
			final org.powerbot.script.rt4.Interactive interactive) {
		return new Callable<Boolean>() {
			@Override
			public Boolean ring() {
				return interactive.inViewport();
			}
		};
	}
}
