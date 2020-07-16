package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jetoze.gunga.KeyBindings;
import jetoze.gunga.layout.Layouts;
import jetoze.gunga.widget.Widget;
import jetoze.tzudoku.model.Puzzle;

public final class PuzzleBuilderUi implements Widget {

    private final PuzzleBuilderModel model;
    private final GridUi gridUi;
    // TODO: Restrict input to valid characters only.
    private final JTextField nameField = new JTextField(30);
    
    public PuzzleBuilderUi(PuzzleBuilderModel model) {
        this.model = requireNonNull(model);
        this.gridUi = new GridUi(model.getGridModel());
        this.gridUi.setEnabled(true);
    }
    
    public void setSuggestedName(String name) {
        nameField.setText(name);
    }
    
    @Override
    public JComponent getUi() {
        JPanel nameFieldPanel = new JPanel();
        nameFieldPanel.add(new JLabel("Name:"));
        nameFieldPanel.add(nameField);

        JPanel gridWrapper = new JPanel();
        gridWrapper.add(gridUi.getUi());
        gridUi.setEnabled(false);
        
        JPanel ui = Layouts.border()
                .withVerticalGap(8)
                .north(nameFieldPanel)
                .center(gridWrapper)
                .build();
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
