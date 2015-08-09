package me.fru1t.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a constructor as Inject-able to be used by a dependency injection framework, mainly, in
 * this repo's case, the Slick framework.
 */
@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
}
