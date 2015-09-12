package me.fru1t.slick.util;

import me.fru1t.slick.SlickException;

/**
 * This class defines a provider who's contents are loaded after program initialization. This is
 * useful for classes that have dependencies that aren't yet loaded at script start (eg. Settings,
 * ClientContext, etc).
 */
public class SlickProvider implements Provider {
	private Object object;

	public SlickProvider() { }

	@Override
	public Object get() {
		if (object == null) {
			throw new SlickException("This provider's object hasn't yet been loaded. If you're "
					+ "using the #get method from a class's constructor, instead, store this "
					+ "Provider as-is and use #get in the method.");
		}

		return object;
	}

	/**
	 * Sets this provider's object with the given parameter.
	 *
	 * @param object The object that this provider provides.
	 */
	public void set(Object object) {
		if (this.object != null) {
			throw new SlickException("This provider already has an object set.");
		}

		this.object = object;
	}
}
