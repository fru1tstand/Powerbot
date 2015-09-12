package me.fru1t.slick;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.fru1t.common.annotations.Inject;
import me.fru1t.common.annotations.Nullable;
import me.fru1t.slick.util.SlickProvider;
import me.fru1t.slick.util.Provider;

/**
 * Slick: Simple Lightweight dependency InjeCtion frameworK
 *
 * <p>Slick aims for very simple (both in implementation and understanding) dependency injection.
 * Modeled after Guice and Dagger 2, Slick provides simple constructor injection with none of the
 * type checking, and all of the errors.</p>
 *
 * <p>Slick currently does not offer inversion of control style modules, but may in the future.
 * This feature was not implemented as, I mean, we're scripters. Who does unit testing anyway?
 * It'd be a really nice thing to have for abstraction between rt4 and rt6 interfaces if there
 * were a linking interface between the two *hint hint wink wink*.</p>
 *
 * <p>TODO(v3): Warning: Slick currently doesn't handle nested non-static inner class construction.
 * It will generate a recursive stack trace.</p>
 *
 * <p>TODO(v2): Warning: Slick currently doesn't warn again cyclic dependencies.</p>
 */
public class Slick {
	private static final String PROVIDER_FULLY_QUALIFIED = Provider.class.getName();
	private final Map<Class<?>, Object> singletonInstances;
	private final Map<Class<?>, SlickProvider> providers;

	/**
	 * Instantiates Slick with the given modules.
	 *
	 * @param modules The modules to be used for this Slick instance.
	 */
	public Slick(Module... modules) {
		this.singletonInstances = new HashMap<Class<?>, Object>();
		this.providers = new HashMap<Class<?>, SlickProvider>();
		addAllProvidersFromModules(modules);
	}

	/**
	 * Gives slick an object for providing. This wraps the passed object within a pass-through
	 * provider, effectively creating a singleton provider.
	 *
	 * @param object The object to provide.
	 * @return Returns this instance of slick for method chaining.
	 */
	@SuppressWarnings("unchecked")
	public <T> Slick provide(T object) {
		SlickProvider provider = null;

		if (providers.containsKey(object.getClass())) {
			provider = providers.get(object.getClass());
		}

		if (provider == null) {
			for (Map.Entry<Class<?>, SlickProvider> entry : providers.entrySet()) {
				if (entry.getKey().isAssignableFrom(object.getClass())) {
					provider = entry.getValue();
					break;
				}
			}
		}

		if (provider == null) {
			throw new SlickException(String.format(
					"\n\t%s was never indicated to be provided...",
					object.getClass().getName()));
		}

		// Suppressed cast warning. We're certain this provider is of the given class.
		provider.set(object);

		return this;
	}

	/**
	 * Attempts to create and return an instance of a class.
	 *
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
		Inject injectAnnotation = null;
		for (Constructor<T> constructor : constructors) {
			if (constructor.isAnnotationPresent(Inject.class)) {
				if (injectableConstructor != null) {
					throw new SlickException(String.format(
							"\n\t%s cannot have multiple @Inject-annotated constructors",
							clazz.getName()));
				}
				injectableConstructor = constructor;
				injectAnnotation = constructor.getAnnotation(Inject.class);
			}
		}

		// None found
		if (injectableConstructor == null) {
			throw new SlickException(String.format(
					"\n\t%s has no constructor with the @Inject annotation.",
					clazz.getName()));
		}

		// Allow = false
		if (!injectAnnotation.allow()) {
			throw new SlickException(String.format(
					"\n\t%s explicitly marks itself as non-injectable with reason: %s",
					clazz.getName(),
					injectAnnotation.reason()));
		}

		// Make sure we can see it from here
		if ((injectableConstructor.getModifiers() & (Modifier.PRIVATE | Modifier.PROTECTED)) > 0) {
			injectableConstructor.setAccessible(true);
		}

		// Grab necessary information
		Class<?>[] dependencies = injectableConstructor.getParameterTypes();
		Annotation[][] annotations = injectableConstructor.getParameterAnnotations();
		Type[] types = injectableConstructor.getGenericParameterTypes();
		Object[] fulfillments = new Object[dependencies.length];

		// Fulfill dependencies
		for (int i = 0; i < dependencies.length; i++) {

			// Must match singleton states
			boolean isClassSingleton = isSingletonAnnotated(dependencies[i]);
			boolean isParameterSingleton = containsSingletonAnnotation(annotations[i]);

			if (!isClassSingleton && isParameterSingleton) {
				throw new SlickException(String.format(
						"\n\t%s depends on %s but annotates it as @Singleton, "
								+ "while the definition isn't a singleton.",
						clazz.getName(), dependencies[i].getName()));
			}
			if (!isParameterSingleton && isClassSingleton) {
				throw new SlickException(String.format(
						"\n\t%s depends on %s but doesn't annotate it as @Singleton, "
								+ "while the definition is a singleton.",
						clazz.getName(), dependencies[i].getName()));
			}

			// Is a provider?
			if (dependencies[i].equals(Provider.class)) {
				Class<?> providedClass = getProvidedClassFromType(types[i]);
				fulfillments[i] = getInstanceFromProviders(providedClass);
				continue;
			}

			// Is a singleton?
			if (isClassSingleton) {
				fulfillments[i] = getInstanceFromSingleton(dependencies[i]);
			}

			// Recurse
			if (fulfillments[i] == null) {
				try {
					fulfillments[i] = get(dependencies[i]);
				} catch (SlickException se) {
					// Catch and rethrow for easier deubgging
					throw new SlickException(
							String.format(
									"%s\n\t...Failed to fulfill %s required by %s",
									se.getMessage(),
									dependencies[i].getName(),
									clazz.getName()));
				}

				// Place into singleton if applicable
				if (isClassSingleton) {
					singletonInstances.put(dependencies[i], fulfillments[i]);
				}
			}
		}

		try {
			return injectableConstructor.newInstance(fulfillments);
		} catch (SlickException se) {
			throw new SlickException(
					String.format(
							"%s\n\t...Failed to instantiate %s",
							se.getMessage(),
							clazz.getName()),
					se.getCause());
		} catch (InvocationTargetException ite) {
			throw new RuntimeException(String.format(
					"Slick couldn't create an instance of %s",
					clazz.getName()),
					ite);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Finds the class within the provided and instantiated objects, and returns it if it exists.
	 * This method checks for superclasses and generics when searching.
	 *
	 * @param clazz The class to find.
	 * @return The instance of the requested class and type.
	 */
	private Provider<?> getInstanceFromProviders(Class<?> clazz) {
		// Direct Request
		if (providers.containsKey(clazz)) {
			return providers.get(clazz);
		}

		// Assignable
		for (Map.Entry<Class<?>, SlickProvider> entry : providers.entrySet()) {
			if (clazz.isAssignableFrom(entry.getKey())) {
				return entry.getValue();
			}
		}

		throw new SlickException(String.format(
				"\n\t%s was requested in a Provider, but is not declared as a provided class.",
				clazz.getName()));
	}

	/**
	 * Finds an instance of the given class within the stored singletons and returns it. Otherwise,
	 * returns null if it wasn't found.
	 *
	 * @param clazz The class to get.
	 * @return The instance of the class, or null if not found.
	 */
	@Nullable
	private Object getInstanceFromSingleton(Class<?> clazz) {
		// Direct
		if (singletonInstances.containsKey(clazz)) {
			return singletonInstances.get(clazz);
		}

		// Assignable
		for (Map.Entry<Class<?>, Object> entry : singletonInstances.entrySet()) {
			if (clazz.isAssignableFrom(entry.getKey())) {
				return entry.getValue();
			}
		}

		return null;
	}

	/**
	 * Returns the class that represents the object the provider provides.
	 *
	 * <p>Yeah. I know. Don't judge. Strings. Types. Ugh. This mess. Why java. Why...</p>
	 *
	 * @param providerType The provider to check.
	 * @return The class the provider provides an instance of.
	 */
	private Class<?> getProvidedClassFromType(Type providerType) {
		String typeString = providerType.toString();
		if (!typeString.contains(PROVIDER_FULLY_QUALIFIED)) {
			throw new SlickException(String.format(
					"\n\t%s is not a provider, but was used as one.",
					typeString));
		}
		String fullyQualifiedGeneric = typeString.substring(
				typeString.lastIndexOf('<') + 1,
				typeString.lastIndexOf('>'));

		try {
			return Class.forName(fullyQualifiedGeneric);
		} catch (ClassNotFoundException e) {
			throw new SlickException(String.format(
					"\n\tClass %s not found for %s",
					fullyQualifiedGeneric,
					typeString
			));
		}
	}

	/**
	 * Retrieves all methods that are @Provides annotated from a given module.
	 * @param module The module to reflect.
	 * @return A list of all methods that are @Provides annotated.
	 */
	private List<Method> getProviderMethodsFromModule(Module module) {
		ArrayList<Method> providerMethods = new ArrayList<Method>();
		Method[] allModuleMethods = module.getClass().getDeclaredMethods();
		for (Method method : allModuleMethods) {
			if (method.getAnnotation(me.fru1t.common.annotations.Provides.class) != null) {
				if (method.getParameterTypes().length != 0) {
					throw new SlickException(String.format(
							"\n\t@Provides annotated method %s in %s cannot contain parameters.",
							method.getName(),
							module.getClass().getName()));
				}
				providerMethods.add(method);
			}
		}
		return providerMethods;
	}

	/**
	 * Adds all providers from the given modules to this slick instance.
	 *
	 * @param modules The modules to use.
	 */
	private void addAllProvidersFromModules(Module[] modules) {
		for (Module module : modules) {
			for (Method method : getProviderMethodsFromModule(module)) {
				try {
					Object dirtyClass = method.invoke(module);
					if (!(dirtyClass instanceof Class<?>)) {
						throw new SlickException(String.format(
								"\n\t%s in %s is @Provides annotated, but doesn't return a class",
								method.getName(),
								module.getClass().getName()
						));
					}

					Class<?> clazz = (Class<?>) dirtyClass;
					providers.put(clazz, new SlickProvider());
				} catch (Exception e) {
					throw new SlickException(String.format(
							"%s\n\tAttempted to invoke %s in %s.",
							e.getMessage(),
							method.getName(),
							module.getClass().getName()
					));
				}
			}
		}
	}

	/**
	 * Checks if an AnnotatedElement (accessible object, class, constructor, field, method,
	 * package) is Singleton-annotated.
	 *
	 * @param element The element to check.
	 * @return True if the element is singleton annotated. False otherwise.
	 */
	private boolean isSingletonAnnotated(AnnotatedElement element) {
		return containsSingletonAnnotation(element.getDeclaredAnnotations());
	}

	/**
	 * Checks if an array of annotations contains the @Inject annotation
	 *
	 * @param annotations The array of annotations to check.
	 * @return True if the array contains the annotation. False otherwise.
	 */
	private boolean containsSingletonAnnotation(Annotation[] annotations) {
		for (Annotation annotation : annotations) {
			if (annotation instanceof Inject) {
				return true;
			}
		}
		return false;
	}
}
