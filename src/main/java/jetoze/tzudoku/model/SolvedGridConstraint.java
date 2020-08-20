package jetoze.tzudoku.model;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.*;

import com.google.common.collect.ImmutableSet;

/**
 * Constraint that uses a solved version of the grid to detect invalid cells.
 */
public class SolvedGridConstraint implements Constraint {

    // TODO: Optionally validate candidates too? Mark a cell invalid if it has one or more
    // center pencil marks, none of which match the digit in the solved grid.
    
    private final Grid solvedGrid;
    
    public SolvedGridConstraint(Grid solvedGrid) {
        this.solvedGrid = requireNonNull(solvedGrid);
    }

    @Override
    public ImmutableSet<Position> validate(Grid grid) {
        requireNonNull(grid);
        return Position.all()
                .filter(p -> isInvalid(p, grid))
                .collect(toImmutableSet());
    }
    
    private boolean isInvalid(Position p, Grid grid) {
        Cell solvedCell = solvedGrid.cellAt(p);
        if (!solvedCell.hasValue()) {
            // We use our own auto solver to solve the grid --> there are puzzles it
            // can't solve so it's not certain all cells in the solved grid have values.
            return false;
        }
        Cell cell = grid.cellAt(p);
        return cell.hasValue() && !cell.getValue().equals(solvedCell.getValue());
    }

}
