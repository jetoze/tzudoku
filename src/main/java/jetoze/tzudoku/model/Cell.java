package jetoze.tzudoku.model;

import static java.util.Objects.*;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

public class Cell {
    @Nullable
    private final boolean given;
    private Value value;
    private final PencilMarks pencilMarks;
    private CellColor color = CellColor.WHITE;
    
    public static Cell given(Value value) {
        requireNonNull(value);
        return new Cell(true, value, PencilMarks.forGivenCell());
    }
    
    public static Cell empty() {
        return new Cell(false, null, PencilMarks.forUnknownCell());
    }
    
    public static Cell unknownWithValue(Value value) {
        requireNonNull(value);
        return new Cell(false, value, PencilMarks.forUnknownCell());
    }
    
    private Cell(boolean given, @Nullable Value value, PencilMarks pencilMarks) {
        this.given = given;
        this.value = value;
        this.pencilMarks = requireNonNull(pencilMarks);
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
        } else if (!pencilMarks.isEmpty()) {
            pencilMarks.clear();
        } else {
            color = CellColor.WHITE;
        }
    }

    public void reset() {
        if (!given) {
            value = null;
            pencilMarks.clear();
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
        return (value == null) && pencilMarks.isEmpty() && (color == CellColor.WHITE);
    }

    public PencilMarks getPencilMarks() {
        return pencilMarks;
    }
    
    public boolean hasNewInformation() {
        return given
                ? color != CellColor.WHITE
                : (value != null) || !pencilMarks.isEmpty() || color != CellColor.WHITE;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, given, color, pencilMarks);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Cell) {
            Cell that = (Cell) obj;
            return (this.value == that.value) && 
                    (this.given == that.given) &&
                    (this.color == that.color) &&
                    this.pencilMarks.equals(that.pencilMarks);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(getValue().map(Value::toString).orElse("x"));
        if (isGiven()) {
            s.append(" (given)");
        }
        s.append(String.format("[cornerMarks: %s, centerMarks: %s]", 
                pencilMarks.cornerAsString(), pencilMarks.centerAsString()));
        s.append("[").append(color.name()).append("]");
        return s.toString();
    }
    
    
}
