package me.fru1t.slick;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Nullable;
import me.fru1t.common.annotations.Singleton;

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
// TODO(v1): Update docs on all methods, classes, etc.
public class Slick {
	private final Map<Class<?>, Object> providedInstances;

	public Slick() {
		this.providedInstances = new HashMap<>();
	}

	/**
	 * Gives slick an instance of the class for singleton use by classes being injected.
	 * @param reference
	 */
	public <T> void provide(T reference) {
		unsafeProvide(reference.getClass(), reference);
	}

	/**
	 * Gives slick an instance of the specified class for singleton use by classes being injected.
	 * @param clazz The class type to provide
	 * @param reference The instance of the class
	 */
	public <T> void provide(Class<T> clazz, T reference) {
		unsafeProvide(clazz, reference);
	}

	/**
	 * @see #provide(Class, Object)
	 */
	private void unsafeProvide(Class<?> clazz, Object reference) {
		if (providedInstances.containsKey(reference.getClass())) {
			throw new SlickException(String.format(
					"\nThe provided class '%s' has already been provided to this Slick instance.",
					clazz.getName()));
		}
		providedInstances.put(clazz, reference);
	}

	/**
	 * Attempts to create and return an instance of a class.
	 * @param clazz The class to make an instance of.
	 * @return An instance of the class.
	 */
	public <T> T get(Class<T> clazz) {
		// Grab the class's constructors
		@SuppressWarnings("unchecked") // We're guaranteed this array is of type Constructor<T>
		Constructor<T>[] constructors = (Constructor<T>[]) clazz.getDeclaredConstructors();
		if (constructors.length == 0) {
			throw new SlickException(String.format("\n\t%s has no constructors.", clazz.getName()));
		}

		// Find an @Inject-able constructor
		Constructor<T> injectableConstructor = null;
		for (Constructor<T> constructor : constructors) {
			if (constructor.isAnnotationPresent(Inject.class)) {
				if (injectableConstructor != null) {
					throw new SlickException(String.format(
							"\n\t%s cannot have multiple @Inject-annotated constructors",
							clazz.getName()));
				}
				injectableConstructor = constructor;
			}
		}
		if (injectableConstructor == null) {
			throw new SlickException(String.format(
					"\n\t%s has no constructor with the @Inject annotation.",
					clazz.getName()));
		}

		// Make sure we can see it from here
		if ((injectableConstructor.getModifiers() & (Modifier.PRIVATE | Modifier.PROTECTED)) > 0) {
			injectableConstructor.setAccessible(true);
		}

		// Fulfill dependencies
		Annotation[][] annotations = injectableConstructor.getParameterAnnotations();
		Class<?>[] dependencies = injectableConstructor.getParameterTypes();
		//Type[] types = injectableConstructor.getGenericParameterTypes();
		Object[] fulfillments = new Object[dependencies.length];
		for (int i = 0; i < dependencies.length; i++) {
			boolean foundInProvides = false;
			fulfillments[i] = getFromProvides(dependencies[i]);

			// Recurse
			if (fulfillments[i] == null) {
				// Guaranteed to find or throw exception
				try {
					fulfillments[i] = get(dependencies[i]);
				} catch (SlickException se) {
					throw new SlickException(
							String.format(
									"%s\n\t...Failed to fulfill %s required by %s",
									se.getMessage(),
									dependencies[i].getName(),
									clazz.getName()));
				}
			} else {
				foundInProvides = true;
			}

			// Singleton Check
			boolean isClassSingleton = foundInProvides
					|| dependencies[i].isAnnotationPresent(Singleton.class);
			boolean isParameterSingleton = false;
			for (Annotation annotation : annotations[i]) {
				if (!annotation.annotationType().equals(Singleton.class)) {
					continue;
				}
				if (!isClassSingleton) {
					throw new SlickException(String.format(
							"\n\tThe parameter %s is @Singleton-annotated, "
							+ "but the defining %s class isn't",
							clazz.getName(),
							dependencies[i].getName()));
				}
				if (!foundInProvides) {
					provide(fulfillments[i]);
				}
				isParameterSingleton = true;
				break;
			}
			if (isClassSingleton != isParameterSingleton && isClassSingleton) {
				throw new SlickException(String.format(
						"\n\tThe class %s is @Singleton-annotated, but the parameter %s isn't",
						dependencies[i].getName(),
						clazz.getName()));
			}
		}

		try {
			return injectableConstructor.newInstance(fulfillments);
		} catch (InstantiationException
				| IllegalAccessException
				| IllegalArgumentException
				| InvocationTargetException exception) {
			throw new SlickException(
					String.format(
							"%s\n\t...Failed to instantiate %s",
							exception.getMessage(),
							clazz.getName()),
					exception.getCause());
		}
	}

	/**
	 * Finds the class within the provided and instantiated objects, and returns it if it exists.
	 * This method checks for superclasses and generics when searching.
	 *
	 * @param clazz The class to find.
	 * @param type The type to check against.
	 * @return The instance of the requested class and type if found. Otherwise, null.
	 */
	@Nullable
	private Object getFromProvides(Class<?> clazz) {
		// Direct Request
		if (providedInstances.containsKey(clazz)) {
			return providedInstances.get(clazz);
		}

		// Assignable
		Iterator<Map.Entry<Class<?>, Object>> pIter = providedInstances.entrySet().iterator();
		while (pIter.hasNext()) {
			Map.Entry<Class<?>, Object> entry = pIter.next();
			if (!clazz.isAssignableFrom(entry.getKey())) {
				continue;
			}

			return entry.getValue();

			// TODO: Properly implement generic type checking for injection
			// // Type check
//			Class<?> rollingClass = entry.getKey();
//			while (rollingClass != null) {
//				for (Type t : rollingClass.getGenericInterfaces()) {
//					if (type.equals(t)) {
//						return entry.getValue();
//					}
//				}
//
//				rollingClass = rollingClass.getSuperclass();
//			}
		}
		return null;
	}
}
