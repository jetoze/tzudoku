package jetoze.tzudoku.constraint;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static tzeth.preconds.MorePreconditions.checkInRange;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class Sandwich implements Constraint {
    // TODO: Is "position" a confusing name for this property, seeing how
    // it represents a row or a column, and we have a Position class representing
    // a row and a column.
    private final House house;
    private final int sum;
    
    public Sandwich(House house, int sum) {
        checkArgument(house.getType() != House.Type.BOX, "Only rows and columns allowed");
        this.house = house;
        this.sum = checkInRange(sum, 0, 35);
    }

    public House getHouse() {
        return house;
    }

    public int getSum() {
        return sum;
    }
    
    @Override
    public ImmutableSet<Position> validate(Grid grid) {
        EnumSet<Value> oneAndNine = EnumSet.noneOf(Value.class);
        boolean foundStart = false;
        Map<Position, Cell> sandwichedCells = new HashMap<>();
        for (Position p : house.toList()) {
            Cell cell = grid.cellAt(p);
            ifBoundaryDigit(cell).ifPresent(oneAndNine::add);
            if (oneAndNine.size() == 2) {
                break;
            }
            if (!foundStart && oneAndNine.size() == 1) {
                foundStart = true;
                continue;
            }
            sandwichedCells.put(p, cell);
        }
        return oneAndNine.size() == 2
                ? validateSandwichedCells(sandwichedCells)
                : ImmutableSet.of();
    }
    
    private Optional<Value> ifBoundaryDigit(Cell cell) {
        return (cell.hasValue(Value.ONE) || cell.hasValue(Value.NINE))
                ? cell.getValue()
                : Optional.empty();
    }

    private ImmutableSet<Position> validateSandwichedCells(Map<Position, Cell> sandwichedCells) {
        // Three cases:
        //   1. Sum is 0 --> sandwichedCells must be empty.
        //   2. Sum is > 0, all sandwiched cells have values --> sum must equal sandwich sum
        //   3. Sum is > 0, any sandwiched cell with a digit >= sum is invalid
        if (sum == 0) {
            // TODO: Implement me. The problem is what cells to consider to be invalid. Clearly at least one
            // of the 1 or 9 is invalid, but I don't know which one.
            return ImmutableSet.of();
        } else {
            int totalSum = sandwichedCells.values().stream()
                    .map(Cell::getValue)
                    .flatMap(Optional::stream)
                    .mapToInt(Value::toInt)
                    .sum();
            boolean allSandwichedCellsHaveValues = sandwichedCells.values().stream().allMatch(Cell::hasValue);
            if (allSandwichedCellsHaveValues) {
                if (totalSum != this.sum) {
                    return ImmutableSet.copyOf(sandwichedCells.keySet());
                }
            } else {
                return sandwichedCells.entrySet().stream()
                        .filter(e -> {
                            Cell c = e.getValue();
                            return c.hasValue() && c.getValue().get().toInt() >= this.sum;
                        }).map(e -> e.getKey()).collect(toImmutableSet());
            }
        }
        return ImmutableSet.of();
    }

    @Override
    public String toString() {
        return String.format("%s - Sum: %d", house, sum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(house, sum);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Sandwich) {
            Sandwich that = (Sandwich) obj;
            return this.house.equals(that.house) && (this.sum == that.sum);
        }
        return false;
    }
}
