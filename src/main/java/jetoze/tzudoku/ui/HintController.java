package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

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

    public HintController(JFrame appFrame, GridUiModel model, HintUiFactory hintUiFactory) {
        this.appFrame = requireNonNull(appFrame);
        this.model = requireNonNull(model);
        this.hintUiFactory = requireNonNull(hintUiFactory);
    }
    
    public void lookForHint(SolvingTechnique technique) {
        // TODO: Even if the technique is safe to run in an incomplete grid,
        // ask the user if they want to auto-fill candidates first. Only difference
        // is that if the user declines, we still go on. The question must be phrased
        // differently, and if the user chooses to continue we should not ask them again
        // in the same session.
        if (technique.requiresCandidatesInAllCells() && !allCellsHaveCandidates()) {
            int choice = JOptionPane.showConfirmDialog(appFrame, 
                    "This requires all cells to have candidates. Do you want to auto-fill all remaining candidates?", 
                    "Candidates missing", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.NO_OPTION) {
                return;
            }
            model.showRemainingCandidates();
        }
        runHintCheck(technique);
    }
    
    /**
     * Checks whether all cells in the grid have either a value, or candidates penciled into
     * its center pencil marks.
     */
    private boolean allCellsHaveCandidates() {
        Grid grid = model.getGrid();
        return grid.allCellsHaveValueOrCandidates(Position.all());
    }
    
    private void runHintCheck(SolvingTechnique technique) {
        Callable<Optional<? extends Hint>> producer = () -> technique.analyze(model.getGrid());
        Consumer<? super Optional<? extends Hint>> consumer = o -> {
            o.ifPresentOrElse(this::display, () -> showNoHintFoundMessage(technique));
        };
        UiThread.offload(producer, consumer);
    }
    
    private void showNoHintFoundMessage(SolvingTechnique technique) {
        JOptionPane.showMessageDialog(
                appFrame, 
                "No " + technique.getName() + " found :(", 
                "No Hint Found", 
                JOptionPane.INFORMATION_MESSAGE);
    }

    
    private void display(Hint hint) {
        requireNonNull(hint);
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
}
