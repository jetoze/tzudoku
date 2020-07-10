package jetoze.tzudoku.model;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import javax.annotation.Nullable;

public final class UnknownCell implements Cell {
    @Nullable
    private Value value;
    private final PencilMarks pencilMarks = new PencilMarks();
    private CellColor color = CellColor.WHITE;

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

    public void clearContent() {
        if (value != null) {
            value = null;
        } else if (!pencilMarks.isEmpty()) {
            pencilMarks.clear();
        } else {
            color = CellColor.WHITE;
        }
    }

    public void reset() {
        value = null;
        pencilMarks.clear();
        color = CellColor.WHITE;
    }

    @Override
    public CellColor getColor() {
        return color;
    }

    public void setColor(CellColor color) {
        this.color = requireNonNull(color);
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
        return (value == null) && pencilMarks.isEmpty() && (color == CellColor.WHITE);
    }

    public PencilMarks getPencilMarks() {
        return pencilMarks;
    }
}
