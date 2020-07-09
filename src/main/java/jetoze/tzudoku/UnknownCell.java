package jetoze.tzudoku;

import static java.util.Objects.*;

import java.util.Optional;

import javax.annotation.Nullable;

public final class UnknownCell implements Cell {
	@Nullable
	private Value value;
	private final PencilMarks pencilMarks = new PencilMarks();
	
	public static UnknownCell empty() {
		return new UnknownCell(null);
	}
	
	public static UnknownCell withValue(Value value) {
		requireNonNull(value);
		return new UnknownCell(value);
	}
	
	private UnknownCell(@Nullable Value value) {
		this.value = value;
	}
	
	@Override
	public Optional<Value> getValue() {
		return Optional.ofNullable(value);
	}
	
	public void setValue(Value value) {
		this.value = requireNonNull(value);
	}
	
	public void clearValue() {
		if (value != null) {
			value = null;
		} else {
			pencilMarks.clear();
		}
	}

	public void clearPencilMarks() {
		pencilMarks.clear();
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
		return (value == null) && pencilMarks.isEmpty();
	}

	public PencilMarks getPencilMarks() {
		return pencilMarks;
	}
}
