package me.fru1t.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates the respective {field, method, parameter} as nullable, so YOU SHOULD PERFORM A 
 * NULL CHECK BEFORE USING IT.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface Nullable {

}
