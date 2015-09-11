package me.fru1t.common.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayLists {
	public static <T> List<T> of(T... elements) {
		return new ArrayList<T>(Arrays.asList(elements));
	}
}
