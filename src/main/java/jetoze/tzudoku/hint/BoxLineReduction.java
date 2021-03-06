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

/**
 * BoxLineReduction is the case where the only candidates for a given value in
 * a Row or Column are all confined to a single Box. That value can then be eliminated
 * as a candidate from all other cells in that Box.
 * <p>
 * For example, the digit 7 in Row 3 can only go into r3c4 or r3c5. Both these cells
 * are in Box 2, so 7 can now be removed as a candidate from r1c456 and r2c456.
 */
public class BoxLineReduction extends EliminatingHint {
    // FIXME: I am extremely similar to PointingPair, including how I am detected.

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

    
    public static Optional<BoxLineReduction> analyze(Grid grid) {
        requireNonNull(grid);
        return House.ALL.stream()
            .filter(house -> house.getType() != Type.BOX)
            .filter(house -> HintUtils.allCellsHaveCandidates(grid, house))
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
            if (candidates.size() == 1) {
                // This is in fact a Naked or Hidden Single.
                return null;
            }
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
