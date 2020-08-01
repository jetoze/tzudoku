package jetoze.tzudoku.model;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import javax.annotation.Nullable;

public class Cell {
    @Nullable
    private final boolean given;
    private Value value;
    private final PencilMarks cornerMarks;
    private final PencilMarks centerMarks;
    private CellColor color = CellColor.WHITE;
    
    public static Cell given(Value value) {
        requireNonNull(value);
        return new Cell(true, value, PencilMarks.forGivenCell(), PencilMarks.forGivenCell());
    }
    
    public static Cell empty() {
        return new Cell(false, null, PencilMarks.forUnknownCell(), PencilMarks.forUnknownCell());
    }
    
    public static Cell unknownWithValue(Value value) {
        requireNonNull(value);
        return new Cell(false, value, PencilMarks.forUnknownCell(), PencilMarks.forUnknownCell());
    }
    
    /**
     * Creates a deep copy of the given Cell.
     */
    public static Cell copyOf(Cell original) {
        if (original.isGiven()) {
            Cell copy = Cell.given(original.value);
            copy.color = original.color;
            return copy;
        } else {
            Cell copy = Cell.empty();
            copy.value = original.value;
            copy.color = original.color;
            copy.cornerMarks.setValues(original.cornerMarks.getValues());
            copy.centerMarks.setValues(original.centerMarks.getValues());
            return copy;
        }
    }
    
    private Cell(boolean given, @Nullable Value value, PencilMarks cornerMarks, PencilMarks centerMarks) {
        this.given = given;
        this.value = value;
        this.cornerMarks = requireNonNull(cornerMarks);
        this.centerMarks = requireNonNull(centerMarks);
    }

    public Optional<Value> getValue() {
        return Optional.ofNullable(value);
    }

    public void setValue(Value value) {
        if (given) {
            throw new UnsupportedOperationException();
        }
        this.value = requireNonNull(value);
    }

    public void clearContent() {
        if (given) {
            color = CellColor.WHITE;
        } else {
            clearContentOfNonGivenCell();
        }
    }

    private void clearContentOfNonGivenCell() {
        if (value != null) {
            value = null;
        } else if (hasPencilMarks()) {
            cornerMarks.clear();
            centerMarks.clear();
        } else {
            color = CellColor.WHITE;
        }
    }
    
    public void reset() {
        if (!given) {
            value = null;
            cornerMarks.clear();
            centerMarks.clear();
        }
        color = CellColor.WHITE;
    }

    public CellColor getColor() {
        return color;
    }

    public void setColor(CellColor color) {
        this.color = requireNonNull(color);
    }

    public boolean hasValue() {
        return value != null;
    }

    public boolean isGiven() {
        return given;
    }

    public boolean isEmpty() {
        return (value == null) && !hasPencilMarks() && (color == CellColor.WHITE);
    }

    public PencilMarks getCornerMarks() {
        return cornerMarks;
    }
    
    public PencilMarks getCenterMarks() {
        return centerMarks;
    }

    public boolean hasPencilMarks() {
        return !cornerMarks.isEmpty() || !centerMarks.isEmpty();
    }

    public boolean hasNewInformation() {
        return given
                ? color != CellColor.WHITE
                : (value != null) || hasPencilMarks() || color != CellColor.WHITE;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(getValue().map(Value::toString).orElse("x"));
        if (isGiven()) {
            s.append(" (given)");
        }
        s.append(String.format("[cornerMarks: %s, centerMarks: %s]", 
                PencilMarks.valuesAsString(cornerMarks), PencilMarks.valuesAsString(centerMarks)));
        s.append("[").append(color.name()).append("]");
        return s.toString();
    }
}
