package jetoze.tzudoku.model;

import com.google.common.collect.ImmutableSet;

/**
 * The classic sudoku constraint: duplicate digits are not allowed in a row, column,
 * or box.
 */
public class ClassicSudokuConstraint implements Constraint {

    @Override
    public ImmutableSet<Position> validate(Grid grid) {
        // TODO: Or should we move the getCellsWithDuplicateValues() implementation from Grid to this class?
        return grid.getCellsWithDuplicateValues();
    }

}
