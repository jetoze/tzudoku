package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
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
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;
import jetoze.tzudoku.model.House.Type;

// FIXME: I am extremely similar to PointingPair, including how I am detected.
public class BoxLineReduction implements Hint {

    private final Grid grid;
    private final Value value;
    private final ImmutableSet<Position> positions;
    private final ImmutableSet<Position> targets;
    
    public BoxLineReduction(Grid grid, Value value, Set<Position> positions, Set<Position> targets) {
        this.grid = requireNonNull(grid);
        this.value = requireNonNull(value);
        House.ifInRowOrColumn(positions).orElseThrow(() -> 
            new IllegalArgumentException("positions must be contained to a single row or column"));
        checkArgument(House.allInSameBox(Sets.union(positions, targets)), "positions and targets must all belong to the same box");
        this.positions = ImmutableSet.copyOf(positions);
        this.targets = ImmutableSet.copyOf(targets);
    }

    @Override
    public SolvingTechnique getTechnique() {
        return SolvingTechnique.BOX_LINE_REDUCTION;
    }
    
    /**
     * Returns the Row or Column in which the forcing positions live.
     */
    public House getRowOrColumn() {
        return House.ifInRowOrColumn(positions).orElseThrow();
    }
    
    /**
     * Returns the Box that is the subject to the reduction.
     */
    public House getBox() {
        return House.box(targets.iterator().next().getBox());
    }

    /**
     * Returns the value identified by this box line reduction, i.e. the value that can be
     * eliminated from the target cells.
     */
    public Value getValue() {
        return value;
    }

    /**
     * Returns the positions that make up the reduction.
     * 
     * @return an ImmutableSet containing two or three positions.
     */
    public ImmutableSet<Position> getPositions() {
        return positions;
    }

    /**
     * Returns the targets that are affected by the pointing pair, i.e. the
     * positions in the grid of those cells from which the value can be eliminated.
     * 
     * @return an ImmutableSet of one or more positions.
     */
    public ImmutableSet<Position> getTargets() {
        return targets;
    }
    
    /**
     * Removes the box line reduced value as a candidate from all target cells.
     */
    @Override
    public void apply() {
        HintUtils.eliminateCandidates(grid, targets, Collections.singleton(value));
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
                    .map(targets -> new BoxLineReduction(grid, value, candidates, targets))
                    .orElse(null);
        }
    }
    
}
