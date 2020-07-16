package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import jetoze.gunga.KeyBindings;
import jetoze.gunga.binding.TextBinding;
import jetoze.gunga.layout.Layouts;
import jetoze.gunga.widget.TextFieldWidget;
import jetoze.gunga.widget.Widget;
import jetoze.tzudoku.model.Puzzle;

public final class PuzzleBuilderUi implements Widget {

    private final PuzzleBuilderModel model;
    private final GridUi gridUi;
    // TODO: Restrict input to valid characters only.
    private final TextFieldWidget nameField = new TextFieldWidget(25);
    private Runnable saveAction = () -> {};
    private Runnable resetAction = () -> {};
    
    public PuzzleBuilderUi(PuzzleBuilderModel model) {
        this.model = requireNonNull(model);
        this.gridUi = new GridUi(model.getGridModel());
        nameField.selectAllWhenFocused();
        // TODO: Do we need to dispose the binding at some point? What is the 
        // lifetime of the model compared to the UI?
        TextBinding.bindAndSyncUi(model.getPuzzleNameProperty(), nameField);
    }
    
    public void setSaveAction(Runnable action) {
        this.saveAction = requireNonNull(action);
    }
    
    public void setResetAction(Runnable action) {
        this.resetAction = requireNonNull(action);
    }
    
    @Override
    public JComponent getUi() {
        JPanel nameFieldPanel = new JPanel();
        nameFieldPanel.add(new JLabel("Name:"));
        nameFieldPanel.add(nameField.getUi());

        JPanel gridWrapper = new JPanel();
        gridWrapper.add(gridUi.getUi());
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 0, 10, 0));
        // TODO: Bind the save action to the valid state of the model?
        JButton saveButton = UiLook.makeLargeButton("Save", saveAction);
        buttonPanel.add(saveButton);
        JButton resetButton = UiLook.makeLargeButton("Reset", resetAction);
        buttonPanel.add(resetButton);
        
        JPanel ui = Layouts.border()
                .withVerticalGap(8)
                .north(nameFieldPanel)
                .center(gridWrapper)
                .south(buttonPanel)
                .build();
        ui.setBorder(new EmptyBorder(5, 5, 5, 5));
        gridUi.registerActions(KeyBindings.whenAncestorOfFocusedComponent(ui));
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
