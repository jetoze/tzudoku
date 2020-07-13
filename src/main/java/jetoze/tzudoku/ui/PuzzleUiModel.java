package jetoze.tzudoku.ui;

import static java.util.Objects.*;

import jetoze.tzudoku.model.Puzzle;
import jetoze.tzudoku.model.PuzzleInventory;

public class PuzzleUiModel {
    private final PuzzleInventory inventory;
    private Puzzle puzzle;
    private final GridUiModel gridModel;
    
    public PuzzleUiModel(PuzzleInventory inventory) {
        this.inventory = requireNonNull(inventory);
        this.puzzle = Puzzle.EMPTY;
        this.gridModel = new GridUiModel(puzzle.getGrid());
    }
    
    public PuzzleInventory getInventory() {
        return inventory;
    }
    
    public Puzzle getPuzzle() {
        return puzzle;
    }

    public void setPuzzle(Puzzle puzzle) {
        this.puzzle = requireNonNull(puzzle);
        this.gridModel.setGrid(puzzle.getGrid());
    }
    
    public GridUiModel getGridModel() {
        return gridModel;
    }
    
}
