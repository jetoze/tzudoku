package jetoze.tzudoku;

import static java.util.Objects.*;

import java.util.Optional;

import com.google.common.collect.ImmutableSet;

public final class GivenCell implements Cell {
	private final Value value;
	
	public static GivenCell of(Value value) {
		return new GivenCell(value);
	}
	
	public GivenCell(Value value) {
		this.value = requireNonNull(value);
	}

	@Override
	public Optional<Value> getValue() {
		return Optional.of(value);
	}

	@Override
	public boolean hasValue() {
		return true;
	}

	@Override
	public boolean isGiven() {
		return true;
	}

	@Override
	public ImmutableSet<Value> getCenterPencilMarks() {
		return ImmutableSet.of();
	}

	@Override
	public ImmutableSet<Value> getCornerPencilMarks() {
		return ImmutableSet.of();
	}
}
