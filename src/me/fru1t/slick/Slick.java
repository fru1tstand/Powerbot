package me.fru1t.slick;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import me.fru1t.annotations.Inject;

/**
 * Slick: Simple Lightweight dependency InjeCtion frameworK
 * 
 * <p>Slick aims to be a Guice/Dagger-like runtime dependency injection framework. Super simplistic,
 * none of the setup, all of the errors. Only supports constructor injections (versus member
 * injection). But hey, it's "easy to use".
 * 
 * <p>**Does not map interfaces to implementations** Maybe a todo? This functionality is not needed
 * for its current application.
 */
public class Slick {
	private final Map<Class<?>, Object> providedInstances;
	
	public Slick() {
		this.providedInstances = new HashMap<>();
	}
	
	/**
	 * Adds an injectable instance of a class to the stored list of injectable classes.
	 * @param clazz The class type to provide
	 * @param reference The instance of the class
	 */
	public void provide(Class<?> clazz, Object reference) {
		if (providedInstances.containsKey(clazz))
			throw new SlickException(String.format(
					"The provided class '%s' has already been provided to this Slick instance.",
					clazz.getName()));
		providedInstances.put(clazz, reference);
	}
	
	/**
	 * Attempts to create and return an instance of a class.
	 * @param type The class to make an instance of.
	 * @return An instance of the class.
	 */
	public <T> T get(Class<T> type) {
		// Abstract or interface
		if ((type.getModifiers() & (Modifier.ABSTRACT | Modifier.INTERFACE)) > 0) {
			throw new SlickException(String.format(
					"Slick does not handle the inversion of control style injection attempted "
					+ "when given '%s'.",
					type.getName()));
		}
		
		// grab the class's constructors
		@SuppressWarnings("unchecked") // We're guaranteed this array is of type Constructor<T>
		Constructor<T>[] constructors = (Constructor<T>[]) type.getConstructors();
		if (constructors.length == 0) {
			throw new SlickException(String.format(
					"%s has no public injectable constructors.",
					type.getName()));
		}
		
		// Find an @Inject-able constructor
		Constructor<T> injectableConstructor = null;
		for (Constructor<T> constructor : constructors) {
			Annotation[] annotations = constructor.getAnnotations();
			for (Annotation annotation : annotations) {
				if (!annotation.getClass().equals(Inject.class)) {
					continue;
				}
				if (injectableConstructor != null) {
					throw new SlickException(String.format(
							"%s cannot have multiple @Inject annotated constructors",
							type.getName()));
				}
				injectableConstructor = constructor;
				break;
			}
		}
		if (injectableConstructor == null) {
			throw new SlickException(String.format(
					"%s has no constructor with the @Inject annotation.",
					type.getName()));
		}
		
		// Fulfill the constructor's parameters
		Class<?>[] constructorRequirements = injectableConstructor.getParameterTypes();
		Object[] constructorFulfillments = new Object[constructorRequirements.length];
		for (int i = 0; i < constructorRequirements.length; i++) {
			constructorFulfillments[i] = null;
			
			// Find directly in provides
			if (providedInstances.containsKey(constructorRequirements[i])) {
				constructorFulfillments[i] = providedInstances.get(constructorRequirements[i]);
			}
			
			// Find assignable
			if (constructorFulfillments[i] == null) {
				for (Map.Entry<Class<?>, Object> entry : providedInstances.entrySet()) {
					if (entry.getKey().isAssignableFrom(constructorRequirements[i])) {
						constructorFulfillments[i] = entry.getValue();
						break;
					}
				}
			}
			
			// Lastly, try recursive injection
			if (constructorFulfillments[i] == null) {
				// This will either retrieve the required object or throw an exception.
				constructorFulfillments[i] = get(constructorRequirements[i]);
			}
		}

		try {
			return injectableConstructor.newInstance(constructorFulfillments);
		} catch (InstantiationException
				| IllegalAccessException
				| IllegalArgumentException
				| InvocationTargetException e) {
			throw new SlickException(e.getMessage());
		}
	}
}
