package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

/**
 * Represents a cell with exactly two candidate values.
 */
class BiValueCell {

    private final Position position;
    private final ImmutableSet<Value> candidates;
    
    public BiValueCell(Position position, Set<Value> candidates) {
        this.position = requireNonNull(position);
        checkArgument(candidates.size() == 2);
        this.candidates = ImmutableSet.copyOf(candidates);
    }
    
    /**
     * Checks if the cell at the given position in the grid is a BiValueCell,
     * and returns its BiValueCell representation if it is. 
     */
    public static Optional<BiValueCell> examine(Grid grid, Position p) {
        Cell cell = grid.cellAt(requireNonNull(p));
        if (!cell.hasValue()) {
            ImmutableSet<Value> candidates = cell.getCenterMarks().getValues();
            if (candidates.size() == 2) {
                return Optional.of(new BiValueCell(p, candidates));
            }
        }
        return Optional.empty();
    }

    public Position getPosition() {
        return position;
    }

    public ImmutableSet<Value> getCandidates() {
        return candidates;
    }

    /**
     * If this BiValueCell shares exactly one candidate value with the other BiValueCell,
     * that candidate value is returned.
     */
    public Optional<Value> getSingleSharedValue(BiValueCell other) {
        Set<Value> intersection = Sets.intersection(this.candidates, other.candidates);
        return intersection.size() == 1
                ? Optional.of(intersection.iterator().next())
                : Optional.empty();
    }
}
