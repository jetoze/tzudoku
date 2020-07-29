package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

public class PointingPair {

    private final Value value;
    private final Position p1;
    private final Position p2;
    
    public PointingPair(Value value, Position p1, Position p2) {
        this.value = requireNonNull(value);
        this.p1 = requireNonNull(p1);
        this.p2 = requireNonNull(p2);
        checkArgument(p1.getRow() == p2.getRow() || p1.getColumn() == p2.getColumn() ||
                p1.getBox() == p2.getBox());
    }
    
    public Value getValue() {
        return value;
    }

    public Position getFirstPosition() {
        return p1;
    }
    
    public Position getSecondPosition() {
        return p2;
    }
    
    public String toString() {
        return String.format("%s and %s (Digit: %s)", p1, p2, value);
    }
    
    public static Optional<PointingPair> findNext(Grid grid) {
        requireNonNull(grid);
        return House.ALL.stream()
                // Since we are only interested in candidates in the same row or column, 
                // we do not have to look in Boxes.
                .filter(house -> house.getType() != House.Type.BOX)
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
        public PointingPair findNext() {
            EnumSet<Value> remainingValues = house.getRemainingValues(grid);
            if (remainingValues.size() < 2) {
                // We need at least two positions to form a pointing pair.
                return null;
            }
            return remainingValues.stream()
                    .map(this::examine)
                    .filter(Objects::nonNull)
                    .findAny()
                    .orElse(null);
        }
        
        @Nullable
        private PointingPair examine(Value value) {
            // Find the candidates for the given value in the House.
            ImmutableSet<Position> candidates = house.getPositions()
                    .filter(p -> {
                        Cell cell = grid.cellAt(p);
                        return !cell.hasValue() && cell.getPencilMarks().containsCenterMark(value);
                    }).collect(toImmutableSet());
            // Are the candidates in the same line?
            if (!isLine(candidates)) {
                return null;
            }
            // We now have a pointing line of candidates. Check if they are contained
            // within the same Box.
            if (isContainedInHouse(candidates, Position::getBox)) {
                int boxNumber = candidates.iterator().next().getBox();
                // We can now rule out value as a candidate from all the cells 
                // in this row/column that are in a different box, as well as 
                // the other cells in this box. Are there any such candidates?
                Stream<Position> otherBoxes = house.getPositions()
                        .filter(p -> p.getBox() != boxNumber);
                Stream<Position> sameBox = new House(House.Type.BOX, boxNumber).getPositions()
                        .filter(Predicate.not(candidates::contains));
                
                boolean targetCellExists = Stream.concat(otherBoxes, sameBox)
                        .map(grid::cellAt)
                        .filter(Predicate.not(Cell::hasValue))
                        .map(Cell::getPencilMarks)
                        .anyMatch(pm -> pm.containsCenterMark(value));
                if (targetCellExists) {
                    Iterator<Position> it = candidates.iterator();
                    return new PointingPair(value, it.next(), it.next());
                }
            }
            return null;
        }
        
        @Nullable
        private boolean isLine(ImmutableSet<Position> candidates) {
            if (candidates.size() < 2) {
                // Need at least two candidates to form a line.
                return false;
            }
            return isContainedInHouse(candidates, Position::getRow) ||
                    isContainedInHouse(candidates, Position::getColumn);
        }
        
        // TODO: Move this to the Position class, as a static utility mehtod? Perhaps take a House.Type
        // as input? Or even better, return a concrete House.
        private boolean isContainedInHouse(ImmutableSet<Position> candidates, ToIntFunction<Position> f) {
            return (candidates.stream()
                    .mapToInt(f)
                    .distinct()
                    .count() == 1L);
        }
        
    }
}
