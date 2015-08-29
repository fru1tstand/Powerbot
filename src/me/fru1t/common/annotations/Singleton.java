package me.fru1t.common.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes that the {parameter, type} should only ever be instantiated once per Slick instance.
 * 
 * 
 * <p>Both parameter and type *must* have this annotation, otherwise Slick will throw an exception.
 * This behavior is a design choice intended to make the slick-program contract more transparent.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE})
public @interface Singleton { }
