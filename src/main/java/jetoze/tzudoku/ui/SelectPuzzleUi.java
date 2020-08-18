package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JPanel;

import jetoze.gunga.widget.RadioButtonWidget;
import jetoze.gunga.widget.Widget;

class SelectPuzzleUi implements Widget {

    private final RadioButtonWidget existingPuzzleChoice = new RadioButtonWidget("Open existing puzzle", true);
    private final RadioButtonWidget newPuzzleChoice = new RadioButtonWidget("Build a new puzzle");
    private final InventoryUi inventoryUi;
    private final JComponent ui;
    
    public SelectPuzzleUi(InventoryUi inventoryUi) {
        this.inventoryUi = requireNonNull(inventoryUi);
        existingPuzzleChoice.addChangeListener(inventoryUi::setEnabled);
        RadioButtonWidget.makeExclusive(existingPuzzleChoice, newPuzzleChoice);
        this.ui = layoutUi();
    }
    
    private JComponent layoutUi() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.weightx = 1.0;
        
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(existingPuzzleChoice.getUi(), c);
        
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.insets = new Insets(12, 32, 32, 0);
        p.add(inventoryUi.getUi(), c);
        
        c.gridy = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weighty = 0.0;
        c.insets = new Insets(0, 0, 0, 0);
        p.add(newPuzzleChoice.getUi(), c);
        
        return p;
    }

    @Override
    public JComponent getUi() {
        return ui;
    }

    @Override
    public void requestFocus() {
        existingPuzzleChoice.requestFocus();
    }
    
    public boolean isSelectExistingPuzzleSelected() {
        return existingPuzzleChoice.isSelected();
    }

}
