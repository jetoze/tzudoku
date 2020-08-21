package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.function.Consumer;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.google.common.collect.ImmutableSet;

import jetoze.gunga.KeyBindings;
import jetoze.gunga.binding.AbstractBinding;
import jetoze.gunga.binding.TextBinding;
import jetoze.gunga.binding.UiListeners;
import jetoze.gunga.layout.Layouts;
import jetoze.gunga.widget.CheckBoxWidget;
import jetoze.gunga.widget.TextFieldWidget;
import jetoze.gunga.widget.TextFieldWidget.Validator;
import jetoze.gunga.widget.Widget;
import jetoze.tzudoku.constraint.ChessConstraint;
import jetoze.tzudoku.model.Puzzle;

public final class PuzzleBuilderUi implements Widget {

    private final PuzzleBuilderModel model;
    private final GridUi gridUi;
    // TODO: Restrict input to valid characters only.
    private final TextFieldWidget nameField = new TextFieldWidget(25);
    private final Runnable defineSandwichesAction;
    private final Action addKillerCageAction;
    private final Action deleteKillerCageAction;
    private final CheckBoxWidget kingsMoveCheckBox = new CheckBoxWidget("Kings Move");
    private final CheckBoxWidget knightsMoveCheckBox = new CheckBoxWidget("Knights Move");
    
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
        // TODO: Do we need to dispose the bindings at some point? What is the 
        // lifetime of the model compared to the UI?
        TextBinding.bind(model.getPuzzleNameProperty(), nameField);
        new ChessConstraintsBinding(model).syncUi();
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
        
        JPanel chessConstraintsPanel = Layouts.oneColumnGrid()
                .withVerticalGap(5)
                .withBorder(new TitledBorder("Chess Constraints"))
                .add(kingsMoveCheckBox)
                .add(knightsMoveCheckBox)
                .build();
        
        JPanel optionsPanel = Layouts.oneColumnGrid()
                .withVerticalGap(8)
                .add(sandwichesPanel)
                .add(killerCagesPanel)
                .add(chessConstraintsPanel)
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
    
    
    // TODO: I should be a gunga utility. For example, turn the current EnumBinding into an
    // ExclusiveEnumBinding.
    private class ChessConstraintsBinding extends AbstractBinding<ImmutableSet<ChessConstraint>> {

        private final Consumer<Boolean> uiListener;
        
        public ChessConstraintsBinding(PuzzleBuilderModel model) {
            super(model.getChessConstraintsProperty());
            uiListener = UiListeners.selectableListener(this);
            kingsMoveCheckBox.addChangeListener(uiListener);
            knightsMoveCheckBox.addChangeListener(uiListener);
        }

        @Override
        protected void updateUi(ImmutableSet<ChessConstraint> value) {
            kingsMoveCheckBox.setSelected(value.contains(ChessConstraint.KINGS_MOVE));
            knightsMoveCheckBox.setSelected(value.contains(ChessConstraint.KNIGHTS_MOVE));
        }

        @Override
        protected ImmutableSet<ChessConstraint> getValueFromUi() {
            ImmutableSet.Builder<ChessConstraint> builder = ImmutableSet.builder();
            if (kingsMoveCheckBox.isSelected()) {
                builder.add(ChessConstraint.KINGS_MOVE);
            }
            if (knightsMoveCheckBox.isSelected()) {
                builder.add(ChessConstraint.KNIGHTS_MOVE);
            }
            return builder.build();
        }

        @Override
        protected void removeUiListener() {
            kingsMoveCheckBox.removeChangeListener(uiListener);
            knightsMoveCheckBox.removeChangeListener(uiListener);
        }        
    }

}
