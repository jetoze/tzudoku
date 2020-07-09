package jetoze.tzudoku;

import static java.util.Objects.*;

import java.util.EnumSet;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

public final class UnknownCell implements Cell {
	@Nullable
	private Value value;
	private EnumSet<Value> cornerPencilMarks = EnumSet.noneOf(Value.class);
	private EnumSet<Value> centerPencilMarks = EnumSet.noneOf(Value.class);
	
	public static UnknownCell empty() {
		return new UnknownCell();
	}
	
	private UnknownCell() {
		/* use empty() to create an empty cell */
	}
	
	@Override
	public Optional<Value> getValue() {
		return Optional.ofNullable(value);
	}
	
	public void setValue(Value value) {
		this.value = requireNonNull(value);
	}
	
	public void clearValue() {
		if (this.value != null) {
			this.value = null;
		} else {
			clearPencilMarks();
		}
	}
	
	private void clearPencilMarks() {
		cornerPencilMarks.clear();
		centerPencilMarks.clear();
	}

	@Override
	public boolean hasValue() {
		return value != null;
	}

	@Override
	public boolean isGiven() {
		return false;
	}
	
	public boolean isEmpty() {
		return (value == null) && cornerPencilMarks.isEmpty() && centerPencilMarks.isEmpty();
	}

	@Override
	public ImmutableSet<Value> getCornerPencilMarks() {
		return ImmutableSet.copyOf(cornerPencilMarks);
	}

	@Override
	public ImmutableSet<Value> getCenterPencilMarks() {
		return ImmutableSet.copyOf(centerPencilMarks);
	}

	public void toggleCornerPencilMark(Value value) {
		toggle(value, cornerPencilMarks);
	}
	
	public void toggleCenterPencilMark(Value value) {
		toggle(value, centerPencilMarks);
	}

	private void toggle(Value value, EnumSet<Value> set) {
		requireNonNull(value);
		if (set.contains(value)) {
			set.remove(value);
		} else {
			set.add(value);
		}
	}
}
