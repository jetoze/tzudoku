package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class PointingPair extends EliminatingHint {

    private final House box;
    private final House rowOrColumn;
    
    public PointingPair(Grid grid, Set<Position> positions, Value value, Set<Position> targets) {
        super(SolvingTechnique.POINTING_PAIR, grid, positions, value, targets);
        this.box = House.ifInBox(positions).orElseThrow(() -> 
                new IllegalArgumentException("Not contained to a single box"));
        checkArgument(House.allInSameBox(positions), "Not contained to a box: %s", positions);
        this.rowOrColumn = House.ifInRowOrColumn(positions).orElseThrow(() -> 
            new IllegalArgumentException("Not contained to a single row or column"));
        checkArgument(Sets.union(positions, targets).stream().allMatch(this.rowOrColumn::contains),
                "positions and targets must all belong to the same %s", rowOrColumn.getType());
    }

    /**
     * Returns the Box to which the pointing pair belongs.
     */
    public House getBox() {
        return box;
    }

    /**
     * Returns the Row or Column from which a value can be eliminated.
     */
    public House getRowOrColumn() {
        return rowOrColumn;
    }
    
    /**
     * Returns the value that can be eliminated.
     */
    public Value getValue() {
        return getValues().iterator().next();
    }

    @Override
    public String toString() {
        return String.format("Positions: %s (Digit: %s)", getForcingPositions(), getValues());
    }
    
    public static Optional<PointingPair> findNext(Grid grid) {
        requireNonNull(grid);
        return IntStream.rangeClosed(1, 9)
                .mapToObj(House.Type.BOX::createHouse)
                .map(house -> new Detector(grid, house))
                .map(Detector::find)
                .filter(Objects::nonNull)
                .findAny();
    }

    
    private static class Detector {
        private final Grid grid;
        private final House box;
        
        public Detector(Grid grid, House box) {
            this.grid = grid;
            this.box = box;
        }
        
        @Nullable
        public PointingPair find() {
            EnumSet<Value> remainingValues = box.getRemainingValues(grid);
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
            ImmutableSet<Position> candidates = HintUtils.collectCandidates(grid, value, box);
            // Are the candidates in the same row or column? If so, collect candidate cells
            // from the same row or column, outside of this box.
            return House.ifInRowOrColumn(candidates)
                    .map(house -> house.getPositions().filter(Predicate.not(this.box::contains)))
                    .map(positions -> HintUtils.collectCandidates(grid, value, positions))
                    .filter(Predicate.not(Set::isEmpty))
                    .map(targets -> new PointingPair(grid, candidates, value, targets))
                    .orElse(null);
        }
    }
}
