package jetoze.tzudoku.model;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

public class HiddenSingle implements Hint {

    private final Grid grid;
    private final Value value;
    private final House.Type houseType;
    private final Position position;
    
    public HiddenSingle(Grid grid, Value value, House.Type houseType, Position position) {
        this.grid = requireNonNull(grid);
        this.value = requireNonNull(value);
        this.houseType = requireNonNull(houseType);
        this.position = requireNonNull(position);
    }
    
    public Value getValue() {
        return value;
    }

    public House.Type getHouseType() {
        return houseType;
    }

    public Position getPosition() {
        return position;
    }
    
    /**
     * Updates the hidden single cell with its value.
     */
    @Override
    public void apply() {
        grid.cellAt(position).setValue(value);
        position.seenBy().map(grid::cellAt)
            .filter(Predicate.not(Cell::hasValue))
            .map(Cell::getCenterMarks)
            .forEach(m -> m.remove(value));
    }

    @Override
    public String toString() {
        return String.format("%s in %s: %s", value, houseType, position);
    }
    
    public static Optional<HiddenSingle> findNext(Grid grid) {
        return House.ALL.stream()
                .map(house -> new Detector(grid, house))
                .map(Detector::findNext)
                .filter(Objects::nonNull)
                .findAny();
    }


    private static class Detector {
        private final Grid grid;
        private final House house;
        
        public Detector(Grid grid, House house) {
            this.grid = grid;
            this.house = house;
        }
        
        @Nullable
        public HiddenSingle findNext() {
            EnumSet<Value> remainingValues = house.getRemainingValues(grid);
            if (remainingValues.isEmpty()) {
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
                    return new HiddenSingle(grid, value, house.getType(), candidates.iterator().next());
                }
            }
            return null;
        }
    }
    
}
