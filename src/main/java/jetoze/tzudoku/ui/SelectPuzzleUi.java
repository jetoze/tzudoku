package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import jetoze.gunga.widget.Widget;

final class SelectPuzzleUi implements Widget {
    // TODO: Disable the option that is not currently selected.
    
    private JRadioButton selectExistingPuzzleOption = new JRadioButton("Select existing puzzle", true);
    private JRadioButton createNewPuzzleOption = new JRadioButton("Create new puzzle");
    private final InventoryUi inventoryUi;
    private final CreateNewPuzzleUi createPuzzleUi;
    
    public SelectPuzzleUi(InventoryUi inventoryUi, CreateNewPuzzleUi createPuzzleUi) {
        this.inventoryUi = requireNonNull(inventoryUi);
        this.createPuzzleUi = requireNonNull(createPuzzleUi);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(selectExistingPuzzleOption);
        buttonGroup.add(createNewPuzzleOption);
        ItemListener itemListener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                Object source = e.getSource();
                if (source == selectExistingPuzzleOption) {
                    createPuzzleUi.setEnabled(false);
                    inventoryUi.setEnabled(true);
                    inventoryUi.requestFocus();
                } else {
                    inventoryUi.setEnabled(false);
                    createPuzzleUi.setEnabled(true);
                    createPuzzleUi.requestFocus();
                }
            }
        };
        selectExistingPuzzleOption.addItemListener(itemListener);
        createNewPuzzleOption.addItemListener(itemListener);
    }

    @Override
    public JComponent getUi() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.gridx = 0;
        
        c.gridy = 0;
        p.add(selectExistingPuzzleOption, c);
        c.gridy = 1;
        p.add(inventoryUi.getUi(), c);
        
        c.gridy = 2;
        p.add(createNewPuzzleOption, c);
        JPanel createNewPuzzleUiWrapper = new JPanel();
        createNewPuzzleUiWrapper.add(createPuzzleUi.getUi());
        c.gridy = 3;
        p.add(createNewPuzzleUiWrapper, c);
        
        return p;
    }

    @Override
    public void requestFocus() {
        selectExistingPuzzleOption.requestFocus();
    }
}
