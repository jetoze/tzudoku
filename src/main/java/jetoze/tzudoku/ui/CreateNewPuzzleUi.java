package jetoze.tzudoku.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jetoze.gunga.KeyBindings;
import jetoze.gunga.layout.Layouts;
import jetoze.gunga.widget.Widget;
import jetoze.tzudoku.model.Grid;

final class CreateNewPuzzleUi implements Widget {

    private final GridUiModel model;
    private final GridUi gridUi;
    private final JTextField nameField = new JTextField(30);
    
    public CreateNewPuzzleUi() {
        Grid grid = Grid.emptyGrid();
        model = new GridUiModel(grid, GridSize.SMALL);
        gridUi = new GridUi(model);
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
                .center(gridWrapper)
                .south(nameFieldPanel)
                .build();
        gridUi.registerActions(KeyBindings.whenAncestorOfFocusedComponent(ui));
        return ui;
    }

    @Override
    public void requestFocus() {
        gridUi.requestFocus();
    }
    
    public void setEnabled(boolean enabled) {
        gridUi.setEnabled(enabled);
        nameField.setEnabled(enabled);
    }

}
