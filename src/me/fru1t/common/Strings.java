package me.fru1t.common;

/**
 * Contains string utilities
 */
public class Strings {
	public static boolean isWhitespaceEmptyOrNull(String s) {
		return (s == null) || (s.equals("") || s.trim().equals(""));
	}
}
