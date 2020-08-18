package jetoze.tzudoku.ui;

import static java.util.Objects.*;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import jetoze.attribut.Properties;
import jetoze.attribut.Property;

public class SelectPuzzleModel {

    private final InventoryUiModel inventoryModel;
    private final PuzzleBuilderModel puzzleBuilderModel;
    private final Property<Option> option;
    private final List<Consumer<Boolean>> validationListeners = new ArrayList<>();
    
    public SelectPuzzleModel(PuzzleUiModel masterModel) {
        this.inventoryModel = new InventoryUiModel(masterModel.getInventory());
        this.puzzleBuilderModel = new PuzzleBuilderModel(masterModel.getInventory());
        this.option = Properties.newProperty("Option", inventoryModel.isEmpty()
                ? Option.BUILD_NEW_PUZZLE
                : Option.SELECT_EXISTING_PUZZLE);
        installInternalListeners();
    }
    
    private void installInternalListeners() {
        PropertyChangeListener internalChangeListener = e -> notifyValidationListeners();
        inventoryModel.getSelectedPuzzleProperty().addListener(internalChangeListener);
        option.addListener(internalChangeListener);
        // TODO: Add proper change notification to the PuzzleBuilderModel, so that we can listen to it here.
        // For example, entering duplicate values in the grid should turn valid -> false.
    }
    
    private void notifyValidationListeners() {
        boolean valid = isValid();
        validationListeners.forEach(lst -> lst.accept(valid));
    }
    
    public InventoryUiModel getInventoryUiModel() {
        return inventoryModel;
    }
    
    public PuzzleBuilderModel getPuzzleBuilderModel() {
        return puzzleBuilderModel;
    }
    
    public Property<Option> getOptionProperty() {
        return option;
    }

    public boolean isValid() {
        switch (option.get()) {
        case SELECT_EXISTING_PUZZLE:
            return inventoryModel.getSelectedPuzzle().isPresent();
        case BUILD_NEW_PUZZLE:
            return puzzleBuilderModel.isEmpty();
        default:
            throw new RuntimeException("Unknown option: " + option.get());
        }
    }
    
    public void addValidationListener(Consumer<Boolean> listener) {
        listener.accept(isValid());
        validationListeners.remove(listener);
    }
    
    public void removeValidationListener(Consumer<Boolean> listener) {
        validationListeners.remove(requireNonNull(listener));
    }
    
    
    public enum Option {
        SELECT_EXISTING_PUZZLE,
        BUILD_NEW_PUZZLE
    }
    
}
