package me.fru1t.slick;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import me.fru1t.annotations.Inject;
import me.fru1t.annotations.Singleton;

/**
 * Slick: Simple Lightweight dependency InjeCtion frameworK
 * 
 * <p>Slick aims to be a Guice/Dagger-like runtime dependency injection framework. Super simplistic,
 * none of the setup, all of the errors. Only supports constructor injections (versus member
 * injection). But hey, it's "easy to use".
 * 
 * <p>**Does not map interfaces to implementations** Maybe a todo? Inversion of control is not
 * needed for the current usage of Slick so this was left out.
 * 
 * <p>Design note: I opted to force the @Inject constructor instead of allowing non-annotated
 * or no constructor classes because explicit is better than implicit.
 */
public class Slick {
	private final Map<Class<?>, Object> providedInstances;
	
	public Slick() {
		this.providedInstances = new HashMap<>();
	}
	
	/**
	 * Gives Slick an instance of the given class to use as a singleton if the class is required
	 * in another.
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
		// grab the class's constructors
		@SuppressWarnings("unchecked") // We're guaranteed this array is of type Constructor<T>
		Constructor<T>[] constructors = (Constructor<T>[]) type.getDeclaredConstructors();
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
				if (!annotation.annotationType().equals(Inject.class)) {
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
		
		// Make sure we can see it from here
		if ((injectableConstructor.getModifiers() & (Modifier.PRIVATE | Modifier.PROTECTED)) > 0) {
			injectableConstructor.setAccessible(true);
		}
		
		// Fulfill the constructor's parameters
		Class<?>[] constructorRequirements = injectableConstructor.getParameterTypes();
		Annotation[][] parameterAnnotations = injectableConstructor.getParameterAnnotations();
		Object[] constructorFulfillments = new Object[constructorRequirements.length];
		for (int i = 0; i < constructorRequirements.length; i++) {
			boolean foundInProvides = false;
			constructorFulfillments[i] = null;
			
			// Find directly in provides
			if (providedInstances.containsKey(constructorRequirements[i])) {
				constructorFulfillments[i] = providedInstances.get(constructorRequirements[i]);
				foundInProvides = true;
			}
			
			// Find assignable
			if (constructorFulfillments[i] == null) {
				for (Map.Entry<Class<?>, Object> entry : providedInstances.entrySet()) {
					if (constructorRequirements[i].isAssignableFrom(entry.getKey())) {
						constructorFulfillments[i] = entry.getValue();
						break;
					}
				}
			}
			
			// Find recursive
			if (constructorFulfillments[i] == null) {
				// Guaranteed to find or throw exception
				constructorFulfillments[i] = get(constructorRequirements[i]);
			}
			
			// Singleton Check
			boolean isClassSingleton = foundInProvides
					|| constructorRequirements[i].isAnnotationPresent(Singleton.class);
			boolean isParameterSingleton = false;
			for (Annotation annotation : parameterAnnotations[i]) {
				if (annotation.annotationType().equals(Singleton.class)) {
					if (!isClassSingleton) {
						throw new SlickException(String.format(
								"%s is not annotated @Singleton, "
								+ "but the injected parameter in %s is.",
								constructorRequirements[i].getName(),
								type.getName()));
					}
					isParameterSingleton = true;
					if (!foundInProvides) {
						provide(constructorRequirements[i], constructorFulfillments[i]);
					}
					break;
				}
			}
			if (isClassSingleton != isParameterSingleton) {
				if (isClassSingleton) {
					throw new SlickException(String.format(
							"%s is annotated @Singleton, but the injected parameter in %s is not.",
							constructorRequirements[i].getName(),
							type.getName()));
				}
			}
		}

		try {
			return injectableConstructor.newInstance(constructorFulfillments);
		} catch (InstantiationException
				| IllegalAccessException
				| IllegalArgumentException
				| InvocationTargetException e) {
			throw new SlickException(
					String.format("Failed to instantiate %s", type.getName()), 
					e.getCause());
		}
	}
}
