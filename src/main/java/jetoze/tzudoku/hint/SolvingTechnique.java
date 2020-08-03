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
    
    POINTING_PAIR("Pointing Pair", PointingPair::findNext),
    
    NAKED_TRIPLE("Naked Triple", NakedMultiple::findNakedTriple),
    
    NAKED_QUADRUPLE("Naked Quadruple", NakedMultiple::findNakedQuadruple),
    
    X_WING("X-Wing", XWing::findNext),
    
    XY_WING("XY-Wing", XyWing::findNext),
    
    SIMPLE_COLORING("Simple Coloring", SimpleColoring::findNext),
    
    SWORDFISH("Swordfish", Swordfish::findNext);

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
     * Applies this technique to the given grid.
     * 
     * @return an Optional containing a Hint that can be applied to the Grid, or an
     *         empty Optional if this technique is not applicable to the Grid.
     */
    public Optional<? extends Hint> analyze(Grid grid) {
        return analyzer.apply(grid);
    }
}