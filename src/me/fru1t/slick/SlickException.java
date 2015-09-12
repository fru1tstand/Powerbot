package me.fru1t.slick;

public class SlickException extends RuntimeException {
	public SlickException(String message){
		super(message);
	}

	public SlickException(String message, Throwable cause) {
		super(message, cause);
	}
}
