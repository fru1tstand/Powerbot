package me.fru1t.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a constructor as the entry point for the Slick framework.
 *
 * <p>Only a single constructor should be marked @Inject-able per class.
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
	/**
	 * Set to false to display the {@link Inject#reason()} below as a slick exception when an
	 * attempt to inject this class is made.
	 *
	 * @return Whether or not this class can be injected
	 */
	boolean allow() default true;

	/**
	 * When {@link Inject#allow()} is set to false, this message will be shown as a SlickException.
	 *
	 * @return The message to throw.
	 */
	String reason() default "";
}
