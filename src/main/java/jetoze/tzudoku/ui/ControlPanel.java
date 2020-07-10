package jetoze.tzudoku.ui;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.requireNonNull;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import jetoze.gunga.UiThread;
import jetoze.gunga.layout.Layouts;
import jetoze.tzudoku.model.Position;
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
    
    private final ImmutableMap<Position, JButton> valueButtons;
    
    private final JPanel ui;

    public ControlPanel(GridUiModel model) {
        this.model = requireNonNull(model);
        selectModeButton(model.getEnterValueMode());
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(normalModeButton);
        buttonGroup.add(cornerPencilMarkModeButton);
        buttonGroup.add(centerPencilMarkModeButton);
        buttonGroup.add(colorModeButton);
        UiConstants.makeOverLarge(normalModeButton);
        UiConstants.makeOverLarge(cornerPencilMarkModeButton);
        UiConstants.makeOverLarge(centerPencilMarkModeButton);
        UiConstants.makeOverLarge(colorModeButton);
        model.addListener(new GridUiModelListener() {

            @Override
            public void onNewEnterValueModeSelected(EnterValueMode newMode) {
                selectModeButton(newMode);
            }
        });
        this.valueActions = Value.ALL.stream()
                .map(EnterValueAction::new)
                .collect(toImmutableList());
        ImmutableMap.Builder<Position, JButton> valueButtonsBuilder = ImmutableMap.builder();
        for (int r = 1; r <= 3; ++r) {
            for (int c = 1; c <= 3; ++c) {
                JButton btn = new JButton();
                UiConstants.makeOverSmall(btn);
                valueButtonsBuilder.put(new Position(r, c), btn);
            }
        }
        this.valueButtons = valueButtonsBuilder.build();
        this.ui = layoutUi();
    }

    private void selectModeButton(EnterValueMode mode) {
        getValueModeButton(mode).setSelected(true);
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

        
        
        List<JButton> valueButtons = Value.ALL.stream().map(EnterValueAction::new).map(ControlPanel::smallButton)
                .collect(Collectors.toList());
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
        boolean solved = model.getGrid().isSolved();
        // TODO: add a UI for this
        System.out.println(solved ? "Solved! :)" : "Not solved :(");
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

    private static JButton smallButton(Action action) {
        JButton b = new JButton(action);
        UiConstants.makeOverSmall(b);
        return b;
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
    }

}
