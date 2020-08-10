package jetoze.tzudoku.ui;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import jetoze.gunga.UiThread;
import jetoze.gunga.binding.Binding;
import jetoze.gunga.binding.BooleanBinding;
import jetoze.gunga.binding.EnumBinding;
import jetoze.gunga.layout.Layouts;
import jetoze.gunga.widget.PopupMenuButton;
import jetoze.gunga.widget.Selectable;
import jetoze.gunga.widget.ToggleButtonWidget;
import jetoze.tzudoku.model.CellColor;
import jetoze.tzudoku.model.Value;

public class ControlPanel {
    // TODO: Move more things to the controller.
    // TODO: The ControlPanel installs some Bindings on model properties. Is there a chance that
    //       the model will outlive the ControlPanel? If so, we must dispose the bindings when we 
    //       no longer need the ControlPanel.
    
    private final GridUiModel model;
    private final PuzzleUiController controller;

    private final ToggleButtonWidget normalModeButton = new ToggleButtonWidget("Normal");

    private final ToggleButtonWidget cornerPencilMarkModeButton = new ToggleButtonWidget("Corner");

    private final ToggleButtonWidget centerPencilMarkModeButton = new ToggleButtonWidget("Center");

    private final ToggleButtonWidget colorModeButton = new ToggleButtonWidget("Color");
    
    private final ImmutableList<EnterValueAction> valueActions;
    
    private final JPanel ui;

    public ControlPanel(GridUiModel model, PuzzleUiController controller) {
        this.model = requireNonNull(model);
        this.controller = requireNonNull(controller);
        this.valueActions = Value.ALL.stream()
                .map(EnterValueAction::new)
                .collect(toImmutableList());
        configureValueModeButtons();
        Binding.oneWayBinding(model.getEnterValueModeProperty(), mode -> valueActions.forEach(a -> a.update(mode)));
        this.ui = layoutUi();
    }

    private void configureValueModeButtons() {
        UiLook.makeOverLarge(normalModeButton);
        UiLook.makeOverLarge(cornerPencilMarkModeButton);
        UiLook.makeOverLarge(centerPencilMarkModeButton);
        UiLook.makeOverLarge(colorModeButton);
        EnumBinding.bindAndSyncUi(model.getEnterValueModeProperty(), ImmutableMap.of(
                EnterValueMode.NORMAL, normalModeButton,
                EnterValueMode.CORNER_PENCIL_MARK, cornerPencilMarkModeButton,
                EnterValueMode.CENTER_PENCIL_MARK, centerPencilMarkModeButton,
                EnterValueMode.COLOR, colorModeButton));
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
        top.add(normalModeButton.getUi(), c);
        c.gridy = 1;
        top.add(cornerPencilMarkModeButton.getUi(), c);
        c.gridy = 2;
        top.add(centerPencilMarkModeButton.getUi(), c);
        c.gridy = 3;
        top.add(colorModeButton.getUi(), c);

        c.gridx = 2;
        c.gridy = 3;
        c.gridwidth = 3;
        top.add(largeButton("Delete", model::delete), c);

        
        
        List<JButton> valueButtons = valueActions.stream()
                .map(UiLook::createValueButton)
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

        JPanel bottom = new JPanel(new GridLayout(0, 2));
        bottom.add(largeButton("Undo", model::undo));
        bottom.add(largeButton("Redo", model::redo));
        bottom.add(largeButton("Restart", model::reset));
        bottom.add(largeButton("Check", controller::checkSolution));        
        
        PopupMenuButton optionsButton = createOptionsButton();
        bottom.add(optionsButton.getUi());
        
        PopupMenuButton hintsButton = createHintsButton();
        bottom.add(hintsButton.getUi());
        
        bottom.add(largeButton("Save", controller::saveProgress));
        bottom.add(largeButton("Load", controller::selectPuzzle));
        
        return Layouts.border(0, 10)
                .north(top)
                .south(bottom)
                .build();
    }

    private PopupMenuButton createOptionsButton() {
        JCheckBoxMenuItem showDuplicates = new JCheckBoxMenuItem("Show Duplicates");
        BooleanBinding.bindAndSyncUi(model.getHighlightDuplicateCellsProperty(), 
                Selectable.of(showDuplicates));
        JCheckBoxMenuItem eliminateCandidatesChoice = new JCheckBoxMenuItem("Eliminate Candidates");
        BooleanBinding.bindAndSyncUi(model.getEliminateCandidatesProperty(), 
                Selectable.of(eliminateCandidatesChoice));
        PopupMenuButton optionsButton = new PopupMenuButton("Options...", 
                showDuplicates, 
                eliminateCandidatesChoice,
                new JSeparator(),
                new JMenuItem(createAction("Analyze...", controller::analyze)));
        UiLook.makeOverLarge(optionsButton);
        return optionsButton;
    }

    private PopupMenuButton createHintsButton() {
        PopupMenuButton hintsButton = new PopupMenuButton("Hints...", 
                new JMenuItem(createAction("Fill in Candidates", model::showRemainingCandidates)),
                new JSeparator(),
                new JMenuItem(createAction("Look for Hidden Single", controller::lookForHiddenSingle)),
                new JMenuItem(createAction("Look for Pointing Pair", controller::lookForPointingPair)),
                new JMenuItem(createAction("Look for Box Line Reduction", controller::lookForBoxLineReduction)),
                new JMenuItem(createAction("Look for Naked Triple", controller::lookForNakedTriple)),
                new JMenuItem(createAction("Look for Naked Quadruple", controller::lookForNakedQuadruple)),
                new JMenuItem(createAction("Look for Hidden Pair", controller::lookForHiddenPair)),
                new JMenuItem(createAction("Look for Hidden Triple", controller::lookForHiddenTriple)),
                new JMenuItem(createAction("Look for Hidden Quadruple", controller::lookForHiddenQuadruple)),
                new JMenuItem(createAction("Look for X-Wing", controller::lookForXWing)),
                new JMenuItem(createAction("Look for XY-Wing", controller::lookForXyWing)),
                new JMenuItem(createAction("Look for Simple Coloring", controller::lookForSimpleColoring)),
                new JMenuItem(createAction("Look for Swordfish", controller::lookForSwordfish)),
                new JSeparator(),
                new JMenuItem(createAction("Auto-solve", controller::startAutoSolver)));
        UiLook.makeOverLarge(hintsButton);
        return hintsButton;
    }


    private static JButton largeButton(String text, Runnable work) {
        JButton b = new JButton(createAction(text, work));
        UiLook.makeOverLarge(b);
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
                Icon icon = UiLook.getCellColorSelectionIcon(cellColor);
                putValue(Action.NAME, null);
                putValue(Action.SMALL_ICON, icon);
            } else {
                putValue(Action.NAME, value.toString());
                putValue(Action.SMALL_ICON, null);
            }
        }
    }

}
