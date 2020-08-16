package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.event.KeyEvent;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import jetoze.gunga.Actions;
import jetoze.gunga.KeyBindings;
import jetoze.gunga.KeyStrokes;
import jetoze.gunga.UiThread;
import jetoze.tzudoku.hint.Hint;
import jetoze.tzudoku.hint.SolvingTechnique;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.ui.hint.HintCellDecorator;
import jetoze.tzudoku.ui.hint.HintUi;
import jetoze.tzudoku.ui.hint.HintUiFactory;

public class HintController { // TODO: Or "HintEngine"?

    private final JFrame appFrame;
    private final GridUiModel model;
    private final HintUiFactory hintUiFactory;
    private boolean userHasGreenlightedHintCheckWithoutAllCandidates;

    public HintController(JFrame appFrame, GridUiModel model, HintUiFactory hintUiFactory) {
        this.appFrame = requireNonNull(appFrame);
        this.model = requireNonNull(model);
        this.hintUiFactory = requireNonNull(hintUiFactory);
    }
    
    /**
     * Look for any hint.
     */
    public void lookForHint() {
        boolean allCellsHaveCandidates = allCellsHaveCandidates();
        if (!allCellsHaveCandidates) {
            IncompleteGridChoice choice = askUserToOptionallyAutoFillCandidates();
            if (choice == IncompleteGridChoice.CANCEL) {
                return;
            } else if (choice == IncompleteGridChoice.FILL_IN_CANDIDATES) {
                model.showRemainingCandidates();
                allCellsHaveCandidates = true;
            }
        }
        Predicate<SolvingTechnique> filter = allCellsHaveCandidates
                ? t -> true
                : Predicate.not(SolvingTechnique::requiresCandidatesInAllCells);
        Callable<Optional<? extends Hint>> producer = () -> {
            return Stream.of(SolvingTechnique.values())
                    .filter(filter)
                    .map(t -> t.analyze(model.getGrid()))
                    .flatMap(Optional::stream)
                    .findFirst();
        };
        runHintCheck(producer, "Sorry, I have no hint for you. You are on your own :(");
    }
    
    /**
     * Look for a specific hint.
     * 
     * @param technique the SolvingTechnique to apply to the grid.
     */
    public void lookForHint(SolvingTechnique technique) {
        if (!allCellsHaveCandidates()) {
            IncompleteGridChoice choice = getIncompleteGridChoice(technique);
            if (choice == IncompleteGridChoice.CANCEL) {
                return;
            }
            if (choice == IncompleteGridChoice.FILL_IN_CANDIDATES) {
                model.showRemainingCandidates();
            }
            if (choice == IncompleteGridChoice.CONTINUE) {
                assert !technique.requiresCandidatesInAllCells();
            }
        }
        runHintCheck(() -> technique.analyze(model.getGrid()), "No " + technique.getName() + " found :(");
    }
    
    /**
     * Checks whether all cells in the grid have either a value, or candidates penciled into
     * its center pencil marks.
     */
    private boolean allCellsHaveCandidates() {
        Grid grid = model.getGrid();
        return grid.allCellsHaveValueOrCandidates(Position.all());
    }
    
    private IncompleteGridChoice getIncompleteGridChoice(SolvingTechnique technique) {
        if (technique.requiresCandidatesInAllCells()) {
            IncompleteGridChoice[] choices = {IncompleteGridChoice.FILL_IN_CANDIDATES, IncompleteGridChoice.CANCEL};
            int input = JOptionPane.showOptionDialog(appFrame, 
                    "This requires all cells to have candidates. Do you want to auto-fill all remaining candidates?", 
                    "Candidates missing", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, 
                    null, 
                    choices, 
                    IncompleteGridChoice.CANCEL);
            return translateUserInput(choices, input);
        } else if (userHasGreenlightedHintCheckWithoutAllCandidates) {
            return IncompleteGridChoice.CONTINUE;
        } else {
            return askUserToOptionallyAutoFillCandidates();
        }
    }

    private IncompleteGridChoice askUserToOptionallyAutoFillCandidates() {
        // TODO: Should the message say something about how the check will be incomplete
        //       without all candidates filled in?
        IncompleteGridChoice[] choices = IncompleteGridChoice.values();
        int input = JOptionPane.showOptionDialog(appFrame, 
                "<html>Looking for hints works best if all cells have candidate values.<br>"
                        + "Do you want to auto-fill all remaining candidates first?</html>", 
                "Candidates missing", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                choices, 
                IncompleteGridChoice.CONTINUE);
        IncompleteGridChoice choice = translateUserInput(choices, input);
        if (choice == IncompleteGridChoice.CONTINUE) {
            userHasGreenlightedHintCheckWithoutAllCandidates = true;
        }
        return choice;
    }
    
    private IncompleteGridChoice translateUserInput(IncompleteGridChoice[] choices, int input) {
        return (input >= 0) // will be -1 if the user escapes out of the dialog
                ? choices[input]
                : IncompleteGridChoice.CANCEL;
    }
    
    private void runHintCheck(Callable<Optional<? extends Hint>> hintProducer, String messageIfNotFound) {
        Consumer<? super Optional<? extends Hint>> consumer = o -> {
            o.ifPresentOrElse(this::display, () -> showNoHintFoundMessage(messageIfNotFound));
        };
        UiThread.offload(hintProducer, consumer);
    }
    
    private void showNoHintFoundMessage(String message) {
        JOptionPane.showMessageDialog(
                appFrame, 
                message, 
                "No Hint Found", 
                JOptionPane.INFORMATION_MESSAGE);
    }

    
    private void display(Hint hint) {
        requireNonNull(hint);
        model.clearSelection();
        HintUi hintUi = hintUiFactory.getUi(hint);
        HintCellDecorator decorator = hintUi.getCellDecorator(model);
        decorator.decorate();
        try {
            boolean apply = showHintInfo(hint, hintUi);
            if (apply) {
                UiThread.runLater(() -> hintUi.apply(model));
            }
        } finally {
            decorator.clear();
        }
    }
    
    /**
     * Displays the HTML information provided by the HintUi in a popup dialog, 
     * with the choice of also applying the hint to the puzzle.
     * 
     * @return {@code true} if the user chose to apply the hint, {@code false} otherwise.
     */
    private boolean showHintInfo(Hint hint, HintUi hintUi) {
        String htmlInfo = hintUi.getHtmlInfo();
        Object[] choices = {"Apply", "Close"};
        Object defaultChoice = choices[1];
        int choice = JOptionPane.showOptionDialog(appFrame,
                     new JLabel(htmlInfo),
                     hint.getTechnique().getName(),
                     JOptionPane.YES_NO_OPTION,
                     JOptionPane.INFORMATION_MESSAGE,
                     null,
                     choices,
                     defaultChoice);
        return choice == JOptionPane.YES_OPTION;
    }
    

    public void registerKeyBindings(KeyBindings keyBindings) {
        keyBindings.add(KeyStrokes.commandShiftDown(KeyEvent.VK_H), "give-a-hint", this::lookForHint);
        keyBindings.add(KeyStrokes.ctrlDown(KeyEvent.VK_H), "show-hints-menu", this::showHintsPopupMenu);
    }
    
    private void showHintsPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu("Available Hints");
        Stream.of(SolvingTechnique.values())
            .map(t -> Actions.toAction(t.getName(), () -> lookForHint(t)))
            .map(JMenuItem::new)
            .forEach(popupMenu::add);
        // FIXME: Come up with a reliable way of finding a proper location for the menu.
        popupMenu.show(appFrame, 500, 275);
    }

    
    /**
     * The choices presented to the user when they request a hint on a grid that does not
     * have all candidates filled in.
     */
    private enum IncompleteGridChoice {
        /**
         * Auto-fill the missing candidates, and continue with the hint check.
         */
        FILL_IN_CANDIDATES,
        /**
         * Continue with the hint check without auto-filling the candidates. This choice is
         * only available if the solving technique {@link SolvingTechnique#requiresCandidatesInAllCells() allows it}.
         */
        CONTINUE,
        /**
         * Cancel the hint request.
         */
        CANCEL;
        
        @Override
        public String toString() {
            switch (this) {
            case FILL_IN_CANDIDATES:
                return "Fill in candidates";
            case CONTINUE:
                return "Continue without candidates";
            case CANCEL:
                return "Cancel";
            default:
                throw new RuntimeException("Unexpected choice: " + this.name());
            }
        }
    }
}
