package jetoze.tzudoku;

import static java.util.Objects.*;

import java.util.EnumSet;
import java.util.stream.Collectors;

public class PencilMarks {

	private final EnumSet<Value> corner = EnumSet.noneOf(Value.class);
	private final EnumSet<Value> center = EnumSet.noneOf(Value.class);

	public boolean isEmpty() {
		return corner.isEmpty() && center.isEmpty();
	}
	
	public boolean hasCornerMarks() {
		return !corner.isEmpty();
	}
	
	public boolean hasCenterMarks() {
		return !center.isEmpty();
	}
	
	public void toggleCorner(Value value) {
		toggle(value, corner);
	}
	
	public void toggleCenter(Value value) {
		toggle(value, center);
	}
	
	public void clear() {
		corner.clear();
		center.clear();
	}
	
	public String cornerAsString() {
		return asString(corner);
	}
	
	public String centerAsString() {
		return asString(center);
	}
	
	public Iterable<Value> iterateOverCornerMarks() {
		return corner;
	}
	
	public Iterable<Value> iterateOverCenterMarks() {
		return center;
	}
	
	private void toggle(Value value, EnumSet<Value> set) {
		requireNonNull(value);
		if (set.contains(value)) {
			set.remove(value);
		} else {
			set.add(value);
		}
	}
	
	private String asString(EnumSet<Value> set) {
		return set.stream()
				.map(Object::toString)
				.collect(Collectors.joining());
	}
	
}
