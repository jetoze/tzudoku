package jetoze.tzudoku.ui;

import static java.util.Objects.*;

import jetoze.tzudoku.PuzzleInventory;
import jetoze.tzudoku.model.Grid;

public class PuzzleBuilderModel {
    private final PuzzleInventory inventory;
    private final GridUiModel gridModel;
    
    public PuzzleBuilderModel(PuzzleInventory inventory) {
        this.inventory = requireNonNull(inventory);
        this.gridModel = new GridUiModel(Grid.emptyGrid(), GridSize.SMALL);
        this.gridModel.setHighlightDuplicateCells(true);
    }

    public PuzzleInventory getInventory() {
        return inventory;
    }

    public GridUiModel getGridModel() {
        return gridModel;
    }
    
}
