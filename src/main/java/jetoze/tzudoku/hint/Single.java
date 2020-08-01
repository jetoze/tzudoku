package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.PencilMarks;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class Single implements Hint {

    private final SolvingTechnique technique;
    private final Grid grid;
    private final Value value;
    private final House house; // XXX: Superfluous, really, since we have the position too.
    private final Position position;
    
    // TODO: Replace with factory methods naked() and hidden().
    public Single(Grid grid, Value value, House house, Position position, SolvingTechnique technique) {
        this.grid = requireNonNull(grid);
        this.value = requireNonNull(value);
        this.house = requireNonNull(house);
        this.position = requireNonNull(position);
        this.technique = requireNonNull(technique);
        checkArgument(technique == SolvingTechnique.NAKED_SINGLE || technique == SolvingTechnique.HIDDEN_SINGLE);
    }
    
    @Override
    public SolvingTechnique getTechnique() {
        return technique;
    }

    /**
     * The value of this single, i.e. the value that can be filled into the cell.
     */
    public Value getValue() {
        return value;
    }

    /**
     * The position in the grid of this single.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * The house in which this single was found.
     */
    public House getHouse() {
        return house;
    }

    /**
     * Returns true if this was a naked single, false if it was a hidden one.
     */
    public boolean isNaked() {
        return technique == SolvingTechnique.NAKED_SINGLE;
    }
    
    /**
     * Updates the single cell with its value, and removes the value as a candidate from
     * all cells seen by the updated cell.
     */
    @Override
    public void apply() {
        grid.cellAt(position).setValue(value);
        position.seenBy()
            .map(grid::cellAt)
            .filter(Predicate.not(Cell::hasValue))
            .map(Cell::getCenterMarks)
            .forEach(m -> m.remove(value));
    }

    @Override
    public String toString() {
        return String.format("%s in %s: %s", value, house, position);
    }
    
    public static Optional<Single> findNextNaked(Grid grid) {
        return House.ALL.stream()
                .map(house -> new NakedSingleDetector(grid, house))
                .map(NakedSingleDetector::find)
                .flatMap(Optional::stream)
                .findAny();
                
    }
    
    public static Optional<Single> findNextHidden(Grid grid) {
        return House.ALL.stream()
                .map(house -> new HiddenSingleDetector(grid, house))
                .map(HiddenSingleDetector::findNext)
                .filter(Objects::nonNull)
                .findAny();
    }

    
    
    private static class NakedSingleDetector {
        private final Grid grid;
        private final House house;
        
        public NakedSingleDetector(Grid grid, House house) {
            this.grid = grid;
            this.house = house;
        }
        
        public Optional<Single> find() {
            return house.getPositions().map(this::check).filter(Objects::nonNull).findAny();
        }
        
        @Nullable
        private Single check(Position p) {
            Cell cell = grid.cellAt(p);
            if (cell.hasValue()) {
                return null;
            }
            Set<Value> valuesSeenByCell = p.seenBy().map(grid::cellAt)
                .map(Cell::getValue)
                .flatMap(Optional::stream)
                .collect(toSet());
            if (valuesSeenByCell.size() == 8) {
                Value missingValue = Sets.difference(Value.ALL, valuesSeenByCell).iterator().next();
                return new Single(grid, missingValue, house, p, SolvingTechnique.NAKED_SINGLE);
            }
            Set<Value> centerMarks = cell.getCenterMarks().getValues();
            if (centerMarks.size() == 1) {
                Value missingValue = centerMarks.iterator().next();
                return new Single(grid, missingValue, house, p, SolvingTechnique.NAKED_SINGLE);
            }
            return null;
        }
    }
    

    private static class HiddenSingleDetector {
        private final Grid grid;
        private final House house;
        
        public HiddenSingleDetector(Grid grid, House house) {
            this.grid = grid;
            this.house = house;
        }
        
        @Nullable
        public Single findNext() {
            EnumSet<Value> remainingValues = house.getRemainingValues(grid);
            if (remainingValues.size() < 2) {
                // We are only looking for hidden singles, not naked ones.
                return null;
            }
            boolean allCellsHaveCenterPencilMarks = house.getPositions()
                    .map(grid::cellAt)
                    .filter(Predicate.not(Cell::hasValue))
                    .map(Cell::getCenterMarks)
                    .noneMatch(PencilMarks::isEmpty);
            if (!allCellsHaveCenterPencilMarks) {
                return null;
            }
            for (Value value : remainingValues) {
                Set<Position> candidates = house.getPositions()
                        .filter(p -> {
                            Cell cell = grid.cellAt(p);
                            return !cell.hasValue() && cell.getCenterMarks().contains(value);
                        }).collect(toSet());
                if (candidates.size() == 1) {
                    Position position = candidates.iterator().next();
                    return new Single(grid, value, house, position, SolvingTechnique.HIDDEN_SINGLE);
                }
            }
            return null;
        }
    }
    
}
