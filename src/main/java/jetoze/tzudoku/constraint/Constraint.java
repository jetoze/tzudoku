package jetoze.tzudoku.constraint;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.*;

import java.util.Collection;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;

public interface Constraint {

    // TODO: This approach is very primitive. A more sophisticated approach would allow us
    // to do things like highlight the boundary of a killer cage if the total sum of the 
    // cage is incorrect. In that case we know for sure that one or more cells in the cage
    // are wrong, but we don't know which ones. (We currently consider all the cells in the
    // cage as invalid in this case.)
    //
    // TODO: (Related to previous one) It would also be neat if hovering over an invalid
    // cell in the UI would tell you which constraint (or constraints, it could be more than
    // one) is violated by that cell.

    /**
     * The classic sudoku constraint: duplicate digits are not allowed in a row, column,
     * or box.
     */
    public static final Constraint CLASSIC_SUDOKU = Grid::getCellsWithDuplicateValues;
    
    /**
     * Applies this constraint to the given grid, and returns a set of the 
     * positions of the cells that are currently violating this constraint.
     */
    ImmutableSet<Position> validate(Grid grid);

    static ImmutableSet<Position> validateAll(Grid grid, Collection<? extends Constraint> constraints) {
        return validateAll(grid, constraints.stream());
    }
    
    static ImmutableSet<Position> validateAll(Grid grid, Stream<? extends Constraint> constraints) {
        requireNonNull(grid);
        return constraints
                .flatMap(cage -> cage.validate(grid).stream())
                .collect(toImmutableSet());
    }
    
}
