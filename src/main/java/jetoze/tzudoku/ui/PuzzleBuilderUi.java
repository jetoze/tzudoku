package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Action;
import javax.swing.JComponent;
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

    private final PuzzleBuilderModel model;
    private final GridUi gridUi;
    // TODO: Restrict input to valid characters only.
    private final TextFieldWidget nameField = new TextFieldWidget(25);
    private final Runnable defineSandwichesAction;
    private final Action addKillerCageAction;
    private final Action deleteKillerCageAction;
    
    // FIXME: Inconsistency between Runnable and Action as input here.
    public PuzzleBuilderUi(PuzzleBuilderModel model, 
                           Runnable defineSandwichesAction,
                           Action addKillerCageAction,
                           Action deleteKillerCageAction) {
        this.model = requireNonNull(model);
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
        JPanel nameFieldPanel = Layouts.form()
                .addRow("Name:", nameField)
                .build();

        JPanel gridWrapper = new JPanel();
        gridWrapper.add(gridUi.getUi());

        JPanel sandwichesPanel = Layouts.oneColumnGrid()
                .withBorder(new TitledBorder("Sandwiches"))
                .add(UiLook.makeSmallButton("Sandwiches...", defineSandwichesAction))
                .add(" "/*empty space*/)
                .build();
        
        JPanel killerCagesPanel = Layouts.oneColumnGrid()
                .withVerticalGap(5)
                .withBorder(new TitledBorder("Killer Cages"))
                .add(addKillerCageAction)
                .add(deleteKillerCageAction)
                .build();
        
        JPanel optionsPanel = Layouts.oneColumnGrid()
                .add(sandwichesPanel)
                .add(killerCagesPanel)
                .build();
        JPanel optionsPanelWrapper = Layouts.border().north(optionsPanel).build();
        
        
        JPanel ui = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        ui.add(nameFieldPanel, c);
        
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        ui.add(gridWrapper, c);
        
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        ui.add(optionsPanelWrapper, c);

        ui.setBorder(new EmptyBorder(5, 5, 5, 5));
        KeyBindings keyBindings = KeyBindings.whenAncestorOfFocusedComponent(gridWrapper);
        gridUi.registerDefaultActions(keyBindings);
        CellInputController.forBuilding(model.getGridModel()).registerKeyBindings(keyBindings);
        return ui;
    }
    
    public Puzzle getPuzzle() {
        // TODO: This will throw a RuntimeException if the name is blank.
        // We need validation.
        String name = nameField.getText().strip().replace(' ', '_');
        return new Puzzle(name, model.getGridModel().getGrid());
    }

    @Override
    public void requestFocus() {
        gridUi.requestFocus();
    }

}
