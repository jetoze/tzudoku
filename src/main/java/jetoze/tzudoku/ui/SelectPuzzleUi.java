package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeListener;

import jetoze.attribut.Property;
import jetoze.gunga.binding.AbstractBinding;
import jetoze.gunga.binding.Binding;
import jetoze.gunga.widget.Widget;
import jetoze.tzudoku.ui.SelectPuzzleModel.Option;

class SelectPuzzleUi implements Widget {

    private final InventoryUi inventoryUi;
    private final PuzzleBuilderUi puzzleBuilderUi;
    private final JTabbedPane tabs = new JTabbedPane();
    
    public SelectPuzzleUi(SelectPuzzleModel model, InventoryUi inventoryUi, PuzzleBuilderUi puzzleBuilderUi) {
        this.inventoryUi = requireNonNull(inventoryUi);
        this.puzzleBuilderUi = requireNonNull(puzzleBuilderUi);
        tabs.add("Select Existing Puzzle", inventoryUi.getUi());
        tabs.add("Build New Puzzle", puzzleBuilderUi.getUi());
        bindOption(model.getOptionProperty());
    }

    // TODO: Do we need to dispose this binding?
    private Binding bindOption(Property<SelectPuzzleModel.Option> optionProperty) {
        // TODO: I should be a gunga utility.
        Binding binding = new AbstractBinding<SelectPuzzleModel.Option>(optionProperty) {

            private final ChangeListener changeListener = e -> {
                if (isUiToModelEnabled()) {
                    syncModel();
                }
            };
            {
                tabs.addChangeListener(changeListener);
            }
            
            @Override
            protected Option getValueFromUi() {
                return tabs.getSelectedIndex() == 0
                        ? Option.SELECT_EXISTING_PUZZLE
                        : Option.BUILD_NEW_PUZZLE;
            }

            @Override
            protected void updateUi(Option value) {
                if (value == Option.SELECT_EXISTING_PUZZLE) {
                    tabs.setSelectedIndex(0);
                } else {
                    tabs.setSelectedIndex(1);
                }
            }

            @Override
            protected void removeUiListener() {
                tabs.removeChangeListener(changeListener);
            }
        };
        binding.syncUi();
        return binding;
    }
    
    @Override
    public JComponent getUi() {
        return tabs;
    }

    @Override
    public void requestFocus() {
        if (inventoryUi.isEmpty()) {
            puzzleBuilderUi.requestFocus();
        } else {
            inventoryUi.requestFocus();
        }
    }
}
