package jetoze.tzudoku.hint;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

/**
 * Represents a cell with exactly three candidate values.
 */
class TriValueCell {

    private final Position position;
    private final ImmutableSet<Value> candidates;
    
    public TriValueCell(Position position, Set<Value> candidates) {
        this.position = requireNonNull(position);
        checkArgument(candidates.size() == 3);
        this.candidates = ImmutableSet.copyOf(candidates);
    }
    
    /**
     * Checks if the cell at the given position in the grid has no value and exactly
     * three center marks values, and returns its TriValueCell representation if so.
     */
    public static Optional<TriValueCell> examine(Grid grid, Position p) {
        Cell cell = grid.cellAt(requireNonNull(p));
        if (!cell.hasValue()) {
            ImmutableSet<Value> candidates = cell.getCenterMarks().getValues();
            if (candidates.size() == 3) {
                return Optional.of(new TriValueCell(p, candidates));
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
}
