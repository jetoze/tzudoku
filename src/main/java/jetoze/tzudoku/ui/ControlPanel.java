package jetoze.tzudoku.ui;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import com.google.common.collect.ImmutableList;

import jetoze.gunga.UiThread;
import jetoze.gunga.layout.Layouts;
import jetoze.tzudoku.model.CellColor;
import jetoze.tzudoku.model.ValidationResult;
import jetoze.tzudoku.model.Value;

public class ControlPanel {
    private final GridUiModel model;

    private final JToggleButton normalModeButton = new JToggleButton(
            new SetEnterValueModeAction(EnterValueMode.NORMAL, "Normal"));

    private final JToggleButton cornerPencilMarkModeButton = new JToggleButton(
            new SetEnterValueModeAction(EnterValueMode.CORNER_PENCIL_MARK, "Corner"));

    private final JToggleButton centerPencilMarkModeButton = new JToggleButton(
            new SetEnterValueModeAction(EnterValueMode.CENTER_PENCIL_MARK, "Center"));

    private final JToggleButton colorModeButton = new JToggleButton(
            new SetEnterValueModeAction(EnterValueMode.COLOR, "Color"));
    
    private final ImmutableList<EnterValueAction> valueActions;
    
    private final JPanel ui;

    public ControlPanel(GridUiModel model) {
        this.model = requireNonNull(model);
        this.valueActions = Value.ALL.stream()
                .map(EnterValueAction::new)
                .collect(toImmutableList());
        configureValueModeButtons();
        model.addListener(new GridUiModelListener() {

            @Override
            public void onNewEnterValueModeSelected(EnterValueMode newMode) {
                onNewValueMode(newMode);
            }
        });
        this.ui = layoutUi();
    }

    private void configureValueModeButtons() {
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(normalModeButton);
        buttonGroup.add(cornerPencilMarkModeButton);
        buttonGroup.add(centerPencilMarkModeButton);
        buttonGroup.add(colorModeButton);
        UiConstants.makeOverLarge(normalModeButton);
        UiConstants.makeOverLarge(cornerPencilMarkModeButton);
        UiConstants.makeOverLarge(centerPencilMarkModeButton);
        UiConstants.makeOverLarge(colorModeButton);
        onNewValueMode(model.getEnterValueMode());
    }

    private void onNewValueMode(EnterValueMode mode) {
        getValueModeButton(mode).setSelected(true);
        valueActions.forEach(a -> a.update(mode));
    }

    private JToggleButton getValueModeButton(EnterValueMode mode) {
        switch (mode) {
        case NORMAL:
            return normalModeButton;
        case CORNER_PENCIL_MARK:
            return cornerPencilMarkModeButton;
        case CENTER_PENCIL_MARK:
            return centerPencilMarkModeButton;
        case COLOR:
            return colorModeButton;
        default:
            throw new RuntimeException("Unknown mode: " + mode);
        }
    }
    
    public JPanel getUi() {
        return ui;
    }
    
    private JPanel layoutUi() {
        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        top.add(normalModeButton, c);
        c.gridy = 1;
        top.add(cornerPencilMarkModeButton, c);
        c.gridy = 2;
        top.add(centerPencilMarkModeButton, c);
        c.gridy = 3;
        top.add(colorModeButton, c);

        c.gridx = 2;
        c.gridy = 3;
        c.gridwidth = 3;
        top.add(largeButton("Delete", model::delete), c);

        
        
        List<JButton> valueButtons = valueActions.stream()
                .map(UiConstants::createValueButton)
                .collect(toList());
        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        for (JButton b : valueButtons) {
            top.add(b, c);
            ++c.gridx;
            if (c.gridx == 5) {
                c.gridx = 2;
                c.gridy++;
            }
        }

        JPanel bottom = new JPanel(new GridBagLayout());
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;

        c.gridx = 0;
        c.gridy = 0;
        bottom.add(largeButton("Undo", model::undo), c);
        c.gridx = 1;
        c.gridy = 0;
        bottom.add(largeButton("Redo", model::redo), c);
        c.gridx = 0;
        c.gridy = 1;
        bottom.add(largeButton("Restart", model::reset), c);
        c.gridx = 1;
        c.gridy = 1;
        bottom.add(largeButton("Check", this::checkSolution), c);
        
        return Layouts.border(0, 10)
                .north(top)
                .south(bottom)
                .build();
    }

    private void checkSolution() {
        UiThread.offload(model.getGrid()::validate, this::displayResult);
    }
    
    private void displayResult(ValidationResult result) {
        if (result.isSolved()) {
            JOptionPane.showMessageDialog(ui, "Looks good to me! :)", "Solved", JOptionPane.INFORMATION_MESSAGE);
        } else {
            model.decorateInvalidCells(result);
            JOptionPane.showMessageDialog(ui, "Hmm, that doesn't look right. :(", "Not solved", JOptionPane.ERROR_MESSAGE);
            model.removeInvalidCellsDecoration();
        }
    }

    private static JButton largeButton(String text, Runnable work) {
        JButton b = new JButton(createAction(text, work));
        UiConstants.makeOverLarge(b);
        return b;
    }

    private static Action createAction(String name, Runnable action) {
        return new AbstractAction(name) {

            @Override
            public void actionPerformed(ActionEvent e) {
                UiThread.runLater(action);
            }
        };
    }

    private class SetEnterValueModeAction extends AbstractAction {
        private final EnterValueMode mode;

        public SetEnterValueModeAction(EnterValueMode mode, String name) {
            super(name);
            this.mode = mode;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            UiThread.runLater(() -> model.setEnterValueMode(mode));
        }
    }

    private class EnterValueAction extends AbstractAction {
        private final Value value;

        public EnterValueAction(Value value) {
            super(value.toString());
            this.value = value;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            UiThread.runLater(() -> model.enterValue(value));
        }
        
        public void update(EnterValueMode mode) {
            if (mode == EnterValueMode.COLOR) {
                CellColor cellColor = CellColor.fromValue(value);
                Icon icon = UiConstants.getCellColorSelectionIcon(cellColor);
                putValue(Action.NAME, null);
                putValue(Action.SMALL_ICON, icon);
            } else {
                putValue(Action.NAME, value.toString());
                putValue(Action.SMALL_ICON, null);
            }
        }
    }

}
