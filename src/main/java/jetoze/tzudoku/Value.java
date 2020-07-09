package jetoze.tzudoku;

import java.util.Arrays;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public enum Value {
	
	ONE(1),
	TWO(2),
	THREE(3),
	FOUR(4),
	FIVE(5),
	SIX(6),
	SEVEN(7),
	EIGHT(8),
	NINE(9);
	
	public static final ImmutableSet<Value> ALL = Sets.immutableEnumSet(Arrays.asList(values()));
	
	private final int intVal;
	
	private Value(int intVal) {
		this.intVal = intVal;
	}
	
	public int toInt() {
		return intVal;
	}
	
	public static Value of(int val) {
		switch (val) {
		case 1:
			return ONE;
		case 2:
			return TWO;
		case 3:
			return THREE;
		case 4:
			return FOUR;
		case 5:
			return FIVE;
		case 6:
			return SIX;
		case 7:
			return SEVEN;
		case 8:
			return EIGHT;
		case 9:
			return NINE;
		default:
			throw new IllegalArgumentException("Invalid value: " + val);
		}
	}
	
	@Override
	public String toString() {
		return Integer.toString(intVal);
	}
}
