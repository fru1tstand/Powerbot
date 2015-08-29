package me.fru1t.slick;

import me.fru1t.annotations.Inject;

public interface Provider<T> {
	public static class asdf {
		public static void main(String[] args) {
			Slick slick = new Slick();
			slick.provide(StringThingProvider.class, new StringThingProvider());
			slick.provide(IntegerProvider.class, new IntegerProvider());
			
			InjectMe ic = slick.get(InjectMe.class);
			ic.doit();
		}
	}
	
	public static class StringProvider implements Provider<String> {
		public String get() { return "aaaaaaaaa"; }
	}
	
	public static class IntegerProvider implements Provider<Integer> {
		public Integer get() { return 3; }
	}
	public static class StringThingProvider extends StringProvider implements Thing<Long> {

		@Override
		public void doNothing(Long thing) {
			// TODO Auto-generated method stub
			
		}
		
	}
	public static class Whatever {
		@Inject
		public Whatever() {
			
		}
	}
	
	public static interface Thing<F> {
		public void doNothing(F thing);
	}
	
	public static class InjectMe {
		private Provider<String> stringPro;
		
		@Inject
		public InjectMe(
				Provider<String> stringPro,
				Whatever whatever,
				String asdf) {
			this.stringPro = stringPro;
		}
		
		public void doit() {
			System.out.println(stringPro.get());
		}
	}
	
	public T get();
}
