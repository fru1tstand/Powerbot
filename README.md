# Fru1tstand's PB Repository
###### "This is what boredom does to you" - Fru1tstand


## Scripts
#### Rogue's Den Safe Cracker ([Manifest](/fru1tstand/Powerbot/blob/master/src/me/fru1t/rsbot/RoguesDenSafeCracker.java) | [Strategies](fru1tstand/Powerbot/tree/master/src/me/fru1t/rsbot/safecracker/strategies))

   *Rogue's Den Safe Cracker* cracks safes within Rogue's Den in Taverly. You can start this script from virtually anywhere (but within Taverly, or even better, within Rogue's Den is highly encouraged). Cracking safes requires a minimum of level 25 thieving. 
   
   Cracking safes produces a small sum of money, but also produces a large quantity of rubies, sapphires, and emeralds. Cracking safes is a net positive experience gaining method, with food being the only required cost.


## Slick ([source](/fru1tstand/Powerbot/blob/master/src/me/fru1t/slick/Slick.java))
###### a Simple LightweIght dependenCy injection frameworK
   Slick aims for very simple (both in implementation and understanding) [dependency injection](https://en.wikipedia.org/wiki/Dependency_injection) via inversion of control. Modeled after [Guice](https://github.com/google/guice) and [Dagger 2](http://google.github.io/dagger/), Slick provides simple constructor injection with none of the type checking, and all of the errors.

#### Features
##### Constructor Injection
   Yay! Constructor injection! This is the brunt of the idea behind Slick. Instead of messy `new Objects(with, a, lot, of, dependencies)` everywhere in the code, it's a simple `slick.get(WhatIWant.class)`.
   
*Example*
```java
public class ScriptName {
	public ScriptName() {
		Slick slick = new Slick();
		
		// It's as easy as this:
		TheThing myThing = slick.get(TheThing.class);
		myThing.doStuff();
		
		...
	}
}

public class TheThing {
	@Inject
	public TheThing(Dependency A a, DependencyC c) { ... }
	
	public void doStuff() { ... }
	...
}

public class DependencyA {
	@Inject
	public DependencyA(DependencyB b) { ... }
	
	...
}

public class DependencyB {
	@Inject
	public DependencyB() { ... }
	
	...
}

public class DependencyC { ... }

```

##### Allows for non @Inject-able dependencies
*via an inline #provide method (versus using module binding)*

Say there's an external API you need to use and you can't wrap the class you need *(cough cough powerbot)*. Use the #provide method to pass it as a singleton to Slick!
   
*Example*
```java
public class ScriptName extends Script<TextClientCon> {
	public ScriptName() {
		Slick slick = new Slick();
		TextClientCon xtc = getXtc(); // Inherited via Script
		slick.provide(xtc);

		TheThing myThing = slick.get(TheThing.class);
		myThing.doStuff();
	}
}

public class TheThing {
	@Inject
	public TheThing(@Singleton TextClientCon xtc) { ... }
	
	...
}
```
*note:* `TheThing` in the example has an `@Singleton` annotation. Slick enforces singleton policies throughout the code to not only verify constructor-slick contracts, but to also prevent unexpected behavior.

##### Supports singletons
   If a class instance needs to be shared throughout the program, there are two options: #provide the instance beforehand (see the #provide example above), or simply mark it as @Singleton.
   
*Example*
```java
public class ScriptName {
	public ScriptName() {
		Slick slick = new Slick();
		
		Thing1 one = slick.get(Thing1.class); // Prints: 0
		Thing2 two = slick.get(Thing2.class); // Prints: 1
	}
}

public class Thing1 {
	@Inject
	public Thing1(@Singleton DependencyA a) {
		a.doSomething();
	}
}

public class Thing2 {
	@Inject
	public Thing2(@Singleton DependencyA a) {
		a.doSomething();
	}
}

@Singleton
public class DependencyA {
	private int i;
	
	@Inject
	public DependencyA() {
		this.i = 0;
	}
	
	public void doSomething() {
		System.out.println(i++);
	}
}
```


## Persona^tm ([source](/fru1tstand/Powerbot/blob/master/src/me/fru1t/rsbot/common/framework/components/Persona.java))
###### Human-like attention and motivation for scripts
#### What does it do?
   Persona aims to emulate how a person might interact with the game over time. Where a static bot may, without fail, interact with an object, Persona may, over time, become less and less accurate in interaction until an *event* which may pull the attention back up. While the Persona engine doesn't do this automatically, it provides methods for very easily implementing these types of human responses throughout a script.


## Script Utilities
###### Interaction utilities that utilize Persona
#### Available Utilities
+ Backpack ([rt6](/fru1tstand/Powerbot/blob/master/src/me/fru1t/rsbot/common/script/rt6/Backpack.java))
+ Bank ([rt6](/fru1tstand/Powerbot/blob/master/src/me/fru1t/rsbot/common/script/rt6/Bank.java))
+ Camera ([rt6](/fru1tstand/Powerbot/blob/master/src/me/fru1t/rsbot/common/script/rt6/Bank.java))
+ Mouse ([rt6](/fru1tstand/Powerbot/blob/master/src/me/fru1t/rsbot/common/script/rt6/Mouse.java))
+ Walk ([rt6](/fru1tstand/Powerbot/blob/master/src/me/fru1t/rsbot/common/script/rt6/Walk.java))
