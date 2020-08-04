package jetoze.tzudoku.model;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
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
    
    private final Grid grid;
    private final List<Hint> hints = new ArrayList<>();
    // Boolean flag that tells us if all techniques have been exhausted, meaning there
    // is no point continuing.
    private boolean allTechniquesExhausted;
    
    public GridSolver(Grid grid) {
        this.grid = requireNonNull(grid);
    }

    public Result solve() {
        long startTimeInNanos = System.nanoTime();
        grid.showRemainingCandidates();
        allTechniquesExhausted = false;
        while (!grid.isSolved() && !allTechniquesExhausted) {
            applyTechniques().ifPresentOrElse(
                    this::applyHint, 
                    () -> allTechniquesExhausted = true);
        }
        Duration duration = Duration.ofNanos(System.nanoTime() - startTimeInNanos);
        return new Result(grid, hints, duration);
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
        private final Duration duration;
        
        public Result(Grid grid, List<Hint> hintsApplied, Duration duration) {
            this.grid = grid;
            this.hintsApplied = ImmutableList.copyOf(hintsApplied);
            this.duration = requireNonNull(duration);
        }
        
        /**
         * Returns the grid that was worked on.
         */
        public Grid getGrid() {
            return grid;
        }
        
        /**
         * Checks if the grid was solved or not.
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
        
        /**
         * Returns the time spent on solving the grid.
         */
        public Duration getDuration() {
            return duration;
        }
        
        /**
         * Returns the number of different techniques that were used when solving the grid.
         */
        public int getNumberOfTechniquesUsed() {
            return (int) hintsApplied.stream()
                        .map(Hint::getTechnique)
                        .distinct()
                        .count();
        }
    }
}
