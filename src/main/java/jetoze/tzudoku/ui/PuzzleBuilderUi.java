package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.GridLayout;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import jetoze.gunga.KeyBindings;
import jetoze.gunga.binding.TextBinding;
import jetoze.gunga.layout.Layouts;
import jetoze.gunga.widget.TextFieldWidget;
import jetoze.gunga.widget.TextFieldWidget.Validator;
import jetoze.gunga.widget.Widget;
import jetoze.tzudoku.model.Puzzle;

public final class PuzzleBuilderUi implements Widget {

    // TODO: When a value has been entered, automatically move to the next cell.
    //       Add an option for this to GridUi(?).
    
    private final PuzzleBuilderModel model;
    private final GridUi gridUi;
    // TODO: Restrict input to valid characters only.
    private final TextFieldWidget nameField = new TextFieldWidget(25);
    private final Runnable saveAction;
    private final Runnable resetAction;
    private final Runnable defineSandwichesAction;
    private final Action addKillerCageAction;
    private final Action deleteKillerCageAction;
    
    // FIXME: Inconsistency between Runnable and Action as input here.
    public PuzzleBuilderUi(PuzzleBuilderModel model, 
                           Runnable saveAction,
                           Runnable resetAction,
                           Runnable defineSandwichesAction,
                           Action addKillerCageAction,
                           Action deleteKillerCageAction) {
        this.model = requireNonNull(model);
        this.saveAction = requireNonNull(saveAction);
        this.resetAction = requireNonNull(resetAction);
        this.defineSandwichesAction = requireNonNull(defineSandwichesAction);
        this.addKillerCageAction = requireNonNull(addKillerCageAction);
        this.deleteKillerCageAction = requireNonNull(deleteKillerCageAction);
        this.gridUi = new GridUi(model.getGridModel());
        nameField.setValidator(new Validator() {

            @Override
            public boolean isRequired() {
                return true;
            }

            @Override
            public boolean isValid(String text) {
                // TODO: Implement me. Compare with existing puzzles in the inventory.
                return true;
            }
        });
        nameField.selectAllWhenFocused();
        // TODO: Do we need to dispose the binding at some point? What is the 
        // lifetime of the model compared to the UI?
        TextBinding.bind(model.getPuzzleNameProperty(), nameField);
    }

    @Override
    public JComponent getUi() {
        JPanel nameFieldPanel = new JPanel();
        nameFieldPanel.add(new JLabel("Name:"));
        nameFieldPanel.add(nameField.getUi());

        JPanel gridWrapper = new JPanel();
        gridWrapper.add(gridUi.getUi());

        JPanel sandwichesPanel = new JPanel();
        sandwichesPanel.setBorder(new TitledBorder("Sandwiches"));
        sandwichesPanel.add(UiLook.makeSmallButton("Sandwiches...", defineSandwichesAction));
        
        JPanel killerCagesPanel = new JPanel(new GridLayout(0, 1));
        killerCagesPanel.setBorder(new TitledBorder("Killer Cages"));
        killerCagesPanel.add(new JButton(addKillerCageAction));
        killerCagesPanel.add(new JButton(deleteKillerCageAction));
        
        JPanel optionsPanel = new JPanel(new GridLayout(0, 1));
        optionsPanel.add(sandwichesPanel);
        optionsPanel.add(killerCagesPanel);
        JPanel optionsPanelWrapper = Layouts.border().north(optionsPanel).build();
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 0));
        JButton resetButton = UiLook.makeLargeButton("Reset", resetAction);
        buttonPanel.add(resetButton);
        // TODO: Bind the save action to the valid state of the model?
        JButton saveButton = UiLook.makeLargeButton("Save", saveAction);
        buttonPanel.add(saveButton);
        
        JPanel ui = Layouts.border()
                .withVerticalGap(8)
                .north(nameFieldPanel)
                .center(gridWrapper)
                .east(optionsPanelWrapper)
                .south(buttonPanel)
                .build();
        ui.setBorder(new EmptyBorder(5, 5, 5, 5));
        KeyBindings keyBindings = KeyBindings.whenAncestorOfFocusedComponent(gridWrapper);
        gridUi.registerDefaultActions(keyBindings);
        CellInputController.forBuilding(model.getGridModel()).registerKeyBindings(keyBindings);
        return ui;
    }
    
    public Puzzle getPuzzle() {
        // TODO: This will throw a RuntimeException if the name is blank.
        // We need validation.
        // TODO: Name validation. We use the name as a filename, so it can't contain
        // certain characters or have certain values. (This is unfortunate. We shouldn't tie
        // the name of the puzzle to the name of the file we store the puzzle in.)
        String name = nameField.getText().strip().replace(' ', '_');
        return new Puzzle(name, model.getGridModel().getGrid());
    }

    @Override
    public void requestFocus() {
        gridUi.requestFocus();
    }

}
