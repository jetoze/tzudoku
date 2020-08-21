package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableSet;

import jetoze.attribut.Properties;
import jetoze.attribut.Property;
import jetoze.tzudoku.PuzzleInventory;
import jetoze.tzudoku.constraint.ChessConstraint;
import jetoze.tzudoku.constraint.KillerCages;
import jetoze.tzudoku.constraint.Sandwiches;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Puzzle;

public class PuzzleBuilderModel {
    private final PuzzleInventory inventory;
    private final GridUiModel gridModel;
    private final Property<String> puzzleNameProperty;
    private final List<Consumer<Boolean>> validationListeners = new ArrayList<>();
    private final Property<ImmutableSet<ChessConstraint>> chessConstraints = Properties.newProperty(
            "chessConstraints", ImmutableSet.of());

    public PuzzleBuilderModel(PuzzleInventory inventory) {
        this.inventory = requireNonNull(inventory);
        this.gridModel = new GridUiModel(Grid.emptyGrid(), Sandwiches.EMPTY, KillerCages.EMPTY, BoardSize.SMALL);
        this.gridModel.setNavigationMode(NavigationMode.TRAVERSE);
        this.gridModel.setDecorateDuplicateCells(true);
        this.puzzleNameProperty = Properties.newProperty("puzzleName", 
                inventory.getAvailablePuzzleName("New Puzzle"));
        addInternalListeners();
    }
    
    private void addInternalListeners() {
        puzzleNameProperty.addListener(e -> notifyValidationListeners());
        gridModel.addListener(new GridUiModelListener() {

            @Override
            public void onCellStateChanged() {
                notifyValidationListeners();
            }
        });
    }
    
    private void notifyValidationListeners() {
        boolean valid = isValid();
        validationListeners.forEach(lst -> lst.accept(valid));
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
    
    public KillerCages getKillerCages() {
        return gridModel.getKillerCages();
    }
    
    public void setKillerCages(KillerCages cages) {
        gridModel.setKillerCages(cages);
    }
    
    public ImmutableSet<ChessConstraint> getChessConstraints() {
        return chessConstraints.get();
    }
    
    public Property<ImmutableSet<ChessConstraint>> getChessConstraintsProperty() {
        return chessConstraints;
    }

    public boolean isEmpty() {
        return gridModel.getGrid().isEmpty() && getSandwiches().isEmpty() && getKillerCages().isEmpty();
    }
    
    public boolean isValid() {
        return !isEmpty() &&
                (!puzzleNameProperty.get().isBlank() && !inventory.containsPuzzle(puzzleNameProperty.get())) &&
                gridModel.getGrid().getCellsWithDuplicateValues().isEmpty();
    }
    
    public void addValidationListener(Consumer<Boolean> listener) {
        listener.accept(isValid());
        validationListeners.add(listener);
    }
    
    public void removeValidationListener(Consumer<Boolean> listener) {
        validationListeners.remove(requireNonNull(listener));
    }
    
    // TODO: Add documentation that explains that the listener may be notified
    // even if the validation state has not changed, i.e. it is possible for the 
    // listener to get several valid=true or valid=false notifications in a row.
    public void addGridListener(GridUiModelListener listener) {
        gridModel.addListener(listener);
    }
    
    public void removeGridListener(GridUiModelListener listener) {
        gridModel.addListener(listener);
    }
    
    public void reset() {
        String name = inventory.getAvailablePuzzleName("New Puzzle");
        setPuzzleName(name);
        gridModel.setPuzzle(new Puzzle(name, Grid.emptyGrid()));
        gridModel.setSandwiches(Sandwiches.EMPTY);
        gridModel.setKillerCages(KillerCages.EMPTY);
        gridModel.clearSelection();
    }
    
}
