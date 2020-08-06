package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.House.Type;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

// FIXME: I am extremely similar to PointingPair, including how I am detected.
public class BoxLineReduction extends EliminatingHint {

    private final House rowOrColumn;
    private final House box;
    
    public BoxLineReduction(Grid grid, Set<Position> positions, Value value, Set<Position> targets) {
        super(SolvingTechnique.BOX_LINE_REDUCTION, grid, positions, value, targets);
        House.ifInRowOrColumn(positions).orElseThrow(() -> 
            new IllegalArgumentException("positions must be contained to a single row or column"));
        checkArgument(House.allInSameBox(Sets.union(positions, targets)), "positions and targets must all belong to the same box");
        this.rowOrColumn = House.ifInRowOrColumn(positions).orElseThrow();
        this.box = House.box(targets.iterator().next().getBox());
    }
    
    /**
     * Returns the Row or Column in which the forcing positions live.
     */
    public House getRowOrColumn() {
        return rowOrColumn;
    }
    
    /**
     * Returns the Box that is the subject to the reduction.
     */
    public House getBox() {
        return box;
    }
    
    /**
     * Returns the value that can be eliminated.
     */
    public Value getValue() {
        return getValues().iterator().next();
    }

    
    public static Optional<BoxLineReduction> findNext(Grid grid) {
        requireNonNull(grid);
        return House.ALL.stream()
            .filter(house -> house.getType() != Type.BOX)
            .map(rowOrColumn -> new Detector(grid, rowOrColumn))
            .map(Detector::find)
            .filter(Objects::nonNull)
            .findAny();
    }
    
    
    private static class Detector {
        private final Grid grid;
        private final House rowOrColumn;
        
        public Detector(Grid grid, House rowOrColumn) {
            this.grid = grid;
            this.rowOrColumn = rowOrColumn;
        }
        
        @Nullable
        public BoxLineReduction find() {
            EnumSet<Value> remainingValues = rowOrColumn.getRemainingValues(grid);
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
        private BoxLineReduction examine(Value value) {
            ImmutableSet<Position> candidates = HintUtils.collectCandidates(grid, value, rowOrColumn);
            // Are the candidates in the same Box? If so, any other candidate cells
            // in the same box are targets.
            return House.ifInBox(candidates)
                    .map(box -> box.getPositions().filter(Predicate.not(candidates::contains)))
                    .map(positions -> HintUtils.collectCandidates(grid, value, positions))
                    .filter(Predicate.not(Set::isEmpty))
                    .map(targets -> new BoxLineReduction(grid, candidates, value, targets))
                    .orElse(null);
        }
    }
    
}
