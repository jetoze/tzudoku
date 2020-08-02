package jetoze.tzudoku.hint;

import java.util.Set;
import java.util.function.Predicate;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

/**
 * Utilities shared by the different Hint implementations.
 */
final class HintUtils {

    // TODO: Introduce a CandidateEliminatingHint abstract class instead?
    
    static void eliminateCandidates(Grid grid, Set<Position> targets, Set<Value> valuesToEliminate) {
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
