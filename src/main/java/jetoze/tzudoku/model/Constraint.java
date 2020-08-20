package jetoze.tzudoku.model;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.*;

import java.util.Collection;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

public interface Constraint {

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
