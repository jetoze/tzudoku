package jetoze.tzudoku.hint;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

/**
 * Utilities shared by the different Hint implementations.
 */
final class HintUtils {

    // TODO: Introduce a CandidateEliminatingHint abstract class instead?

    /**
     * Returns a Predicate that evaluates to true for a cell at a given position
     * in the grid if the given value is a candidate value for that cell.
     */
    static Predicate<Position> isCandidate(Grid grid, Value value) {
        return p -> {
            Cell cell = grid.cellAt(p);
            return !cell.hasValue() && cell.getCenterMarks().contains(value);
        };
    }
    
    /**
     * Returns an immutable Set of those positions in the given House that have the given value
     * as a candidate.
     */
    static ImmutableSet<Position> collectCandidates(Grid grid, Value value, House house) {
        return collectCandidates(grid, value, house.getPositions());
    }
    
    /**
     * Returns an immutable Set of those positions in the Stream that have the given value
     * as a candidate.
     */
    static ImmutableSet<Position> collectCandidates(Grid grid, Value value, Stream<Position> positions) {
        return positions.filter(isCandidate(grid, value))
                .collect(toImmutableSet());
    }
    
    /**
     * Eliminates the given values as candidates from the cells at the given positions.
     */
    static void eliminateCandidates(Grid grid, Collection<Position> targets, Collection<Value> valuesToEliminate) {
        targets.stream()
            .map(grid::cellAt)
            .filter(Predicate.not(Cell::isGiven))
            .map(Cell::getCenterMarks)
            .forEach(m -> {
                valuesToEliminate.forEach(m::remove);
            });
    }
    
    private HintUtils() {/**/}

}
