package jetoze.tzudoku.model;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import jetoze.tzudoku.hint.Hint;
import jetoze.tzudoku.hint.SolvingTechnique;

/**
 * A GridSolver tries to solve a sudoku grid by using a set of known solving techniques.
 * Current limitations:
 * <ul>
 * <li>Supports only classic sudoku puzzles;</li>
 * <li>The number of techniques used by the solver is currently somewhat limited, and 
 * will not be able to solve puzzles that require more advanced techniques.
 * </li>
 * </ul>
 * <p>
 * The current solving techniques are currently implemented:
 * <ul>
 * <li>Naked and Hidden Singles</li>
 * <li>Naked Pairs</li>
 * <li>Pointing Pairs</li>
 * <li>Triples and Quadruples</li>
 * <li>X-Wings</li>
 * <li>XY-Wings</li>
 * </ul> 
 *
 */
public class GridSolver {

    // TODO: This class obviously has a lot in common with the UiAutoSolver. Rewrite the UI solver
    // to use a GridSolver internally.
    
    // TODO: Pass the grid into the constructor, or into the solve() method?
    
    // TODO: Optional progress report mechanism (some sort of callback).
    
    // TODO: Option to cancel the solver.
    
    // TODO: When used in the UiAutoSolver we must provide a way to intercept a Hint so that
    //       it can be applied to the UI model rather than directly on the grid itself.
    
    private final Grid grid;
    private final List<Hint> hints = new ArrayList<>();
    // Boolean flag that tells us if all techniques have been exhausted, meaning there
    // is no point continuing.
    private boolean allTechniquesExhausted;
    
    public GridSolver(Grid grid) {
        this.grid = requireNonNull(grid);
    }

    public Result solve() {
        grid.showRemainingCandidates();
        allTechniquesExhausted = false;
        while (!grid.isSolved() && !allTechniquesExhausted) {
            applyTechniques().ifPresentOrElse(
                    this::applyHint, 
                    () -> allTechniquesExhausted = true);
        }
        return new Result(grid, hints);
    }
    
    /**
     * Goes through the techniques in order, until it finds a technique that could
     * be applied successfully.
     * 
     * @return an Optional containing a Hint that can be applied to the Grid, or an
     *         empty Optional if we exhausted the available techniques without
     *         finding a Hint.
     */
    private Optional<? extends Hint> applyTechniques() {
        return Stream.of(SolvingTechnique.values())
                .map(t -> t.analyze(grid))
                .flatMap(Optional::stream)
                .findFirst();
    }
    
    private void applyHint(Hint hint) {
        hint.apply();
        hints.add(hint);
    }
    
    
    /**
     * The result of the solving process.
     */
    public static class Result {

        private final Grid grid;
        /**
         * The Hints that were applied, in order.
         */
        private final ImmutableList<Hint> hintsApplied;
        
        public Result(Grid grid, List<Hint> hintsApplied) {
            this.grid = grid;
            this.hintsApplied = ImmutableList.copyOf(hintsApplied);
        }
        
        public Grid getGrid() {
            return grid;
        }
        
        /**
         * Checks if the puzzle was solved or not.
         */
        public boolean isSolved() {
            return grid.isSolved();
        }
        
        /**
         * Returns the Hints that were applied, in order. 
         */
        public ImmutableList<Hint> getHintsApplied() {
            return hintsApplied;
        }
    }
}
