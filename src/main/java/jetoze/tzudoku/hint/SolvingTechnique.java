package jetoze.tzudoku.hint;

import java.util.Optional;
import java.util.function.Function;

import jetoze.tzudoku.model.Grid;

/**
 * A solving technique that can be used to solve a sudoku puzzle.
 * <p>
 * Given a sudoku grid, a solving technique can return a corresponding Hint that
 * can be applied to take the grid one step closer to its solution.
 */
public enum SolvingTechnique {
    
    NAKED_SINGLE("Naked Single", Single::findNextNaked),
    
    HIDDEN_SINGLE("Hidden Single", Single::findNextHidden),
    
    NAKED_PAIR("Naked Pair", NakedMultiple::findNakedPair),
    
    POINTING_PAIR("Pointing Pair", PointingPair::analyze),
    
    BOX_LINE_REDUCTION("Box Line Reduction", BoxLineReduction::analyze),
    
    NAKED_TRIPLE("Naked Triple", NakedMultiple::findNakedTriple),
    
    NAKED_QUADRUPLE("Naked Quadruple", NakedMultiple::findNakedQuadruple),
    
    HIDDEN_PAIR("Hidden Pair", HiddenMultiple::findHiddenPair),
    
    HIDDEN_TRIPLE("Hidden Triple", HiddenMultiple::findHiddenTriple),
    
    HIDDEN_QUADRUPLE("Hidden Triple", HiddenMultiple::findHiddenQuadruple),
    
    X_WING("X-Wing", XWing::analyze),
    
    Y_WING("Y-Wing", YWing::analyze),
    
    XYZ_WING("XYZ-Wing", XyzWing::analyze),
    
    SIMPLE_COLORING("Simple Coloring", SimpleColoring::analyze),
    
    SWORDFISH("Swordfish", Swordfish::analyze);
    
    // TODO: W-Wing
    // TODO: Two String Kite.
    // TODO: X-Cycle. See puzzle "SudokuWiki.org 2020-08-13" for an example.

    private final String name;
    private final Function<Grid, Optional<? extends Hint>> analyzer;
    
    private SolvingTechnique(String name, Function<Grid, Optional<? extends Hint>> analyzer) {
        this.name = name;
        this.analyzer = analyzer;
    }
    
    /**
     * Returns the name of this technique.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Does this solving technique require candidates to have been filled into all empty cells
     * in the grid?
     * <p>
     * If this method returns {@code true}, this technique can return false positives on a grid
     * without candidates in all cells, meaning it is not safe to run. If it returns {@code true},
     * it is safe to apply the technique - any hint it finds is valid and can be applied to the 
     * grid without breaking it - but the technique might not be able to run an exhaustive check. 
     */
    public boolean requiresCandidatesInAllCells() {
        // It's possible Simple Coloring can be made safe if we only consider complete Houses
        // when we collect the conjugate pairs, but for now I'd rather play it safe and simply
        // not allow this technique to run on a grid if there are cells without candidates.
        return this == SIMPLE_COLORING;
    }

    /**
     * Applies this technique to the given grid.
     * 
     * @return an Optional containing a Hint that can be applied to the Grid, or an
     *         empty Optional if this technique is not applicable to the Grid.
     */
    public Optional<? extends Hint> analyze(Grid grid) {
        return analyzer.apply(grid);
    }

    @Override
    public String toString() {
        return getName();
    }
}