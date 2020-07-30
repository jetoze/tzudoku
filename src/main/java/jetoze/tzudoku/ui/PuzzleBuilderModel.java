package jetoze.tzudoku.ui;

import static java.util.Objects.*;

import jetoze.attribut.Properties;
import jetoze.attribut.Property;
import jetoze.tzudoku.PuzzleInventory;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Puzzle;
import jetoze.tzudoku.model.Sandwiches;

public class PuzzleBuilderModel {
    private final PuzzleInventory inventory;
    private final GridUiModel gridModel;
    private final Property<String> puzzleNameProperty;
    
    public PuzzleBuilderModel(PuzzleInventory inventory) {
        this.inventory = requireNonNull(inventory);
        this.gridModel = new GridUiModel(Grid.emptyGrid(), Sandwiches.EMPTY, BoardSize.SMALL);
        this.gridModel.setHighlightDuplicateCells(true);
        this.puzzleNameProperty = Properties.newProperty("puzzleName", 
                inventory.getAvailablePuzzleName("New Puzzle"));
    }

    public PuzzleInventory getInventory() {
        return inventory;
    }

    public GridUiModel getGridModel() {
        return gridModel;
    }
    
    public String getPuzzleName() {
        return puzzleNameProperty.get();
    }
    
    public void setPuzzleName(String name) {
        this.puzzleNameProperty.set(name);
    }
    
    public Property<String> getPuzzleNameProperty() {
        return puzzleNameProperty;
    }
    
    public Sandwiches getSandwiches() {
        return gridModel.getSandwiches();
    }
    
    public void setSandwiches(Sandwiches sandwiches) {
        gridModel.setSandwiches(sandwiches);
    }
    
    public void reset() {
        String name = inventory.getAvailablePuzzleName("New Puzzle");
        setPuzzleName(name);
        gridModel.setPuzzle(new Puzzle(name, Grid.emptyGrid()));
    }
    
}
