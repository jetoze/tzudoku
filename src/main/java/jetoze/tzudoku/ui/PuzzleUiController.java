package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import jetoze.gunga.UiThread;
import jetoze.tzudoku.hint.Single;
import jetoze.tzudoku.hint.Multiple;
import jetoze.tzudoku.hint.PointingPair;
import jetoze.tzudoku.hint.XyWing;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Puzzle;
import jetoze.tzudoku.model.PuzzleInfo;
import jetoze.tzudoku.model.ValidationResult;

public class PuzzleUiController {
    // TODO: Wait-indication (hour-glass on frame) when background work is in progress.
    
    private final JFrame appFrame;
    private final PuzzleUiModel puzzleModel;
    private final StatusPanel statusPanel;
    
    public PuzzleUiController(JFrame appFrame, PuzzleUiModel model, StatusPanel statusPanel) {
        this.appFrame = requireNonNull(appFrame);
        this.puzzleModel = requireNonNull(model);
        this.statusPanel = requireNonNull(statusPanel);
    }
    
    public void selectPuzzle() {
        InventoryUiModel model = new InventoryUiModel(puzzleModel.getInventory());
        InventoryUi inventoryUi = new InventoryUi(model);
        // TODO: Use a utility for this.
        JButton ok = UiLook.createOptionDialogButton("Select", () -> {
            inventoryUi.getSelectedPuzzle().ifPresent(this::loadPuzzle);
        });
        JButton cancel = UiLook.createOptionDialogButton("Cancel", () -> {});
        JOptionPane optionPane = new JOptionPane(
                inventoryUi.getUi(), 
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.YES_NO_OPTION,
                null, 
                new JButton[] {ok, cancel}, 
                ok);
        JDialog dialog = new JDialog(appFrame, "Select a Puzzle");
        dialog.setContentPane(optionPane);
        dialog.pack();
        dialog.setLocationRelativeTo(appFrame);
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {
                inventoryUi.requestFocus();
            }
        });
        inventoryUi.setPuzzleLoader(pi -> {
            dialog.dispose();
            loadPuzzle(pi);
        });
        inventoryUi.addValidationListener(ok::setEnabled);
        dialog.setVisible(true);
    }
    
    private void loadPuzzle(PuzzleInfo puzzleInfo) {
        UiThread.offload(() -> puzzleModel.getInventory().loadPuzzle(puzzleInfo), this::loadPuzzle);
    }
    
    public void loadPuzzle(Puzzle puzzle) {
        puzzleModel.setPuzzle(puzzle);
        appFrame.setTitle(puzzle.getName());
    }
    
    public void saveProgress() {
        Puzzle puzzle = puzzleModel.getPuzzle();
        if (puzzle.isEmpty()) {
            return;
        }
        Callable<Void> work = () -> {
            puzzleModel.getInventory().saveProgress(puzzle);
            return null;
        };
        Runnable whenDone = () -> statusPanel.setStatus("Puzzle was saved.");
        Consumer<? super Throwable> exceptionHandler = e -> {
            // TODO: Log the exception somewhere.
            String errorMessage = "Failed to save the puzzle: " + e.getMessage();
            statusPanel.setStatus(errorMessage, 10);
        };
        UiThread.offload(work,  whenDone, exceptionHandler);
    }
    
    public void lookForXyWing() {
        runHintCheck(XyWing::findNext, this::showXyWingInfo, "Did not find any XY-wings :(");
    }
    
    private void showXyWingInfo(XyWing xyWing) {
        // TODO: This can obviously be done in a fancier way.
        StringBuilder s = new StringBuilder("<html>Found an XY-wing:<br>");
        s.append(xyWing.getCenter());
        xyWing.getWings().forEach(w -> s.append("<br>").append(w));
        s.append("<br><br>").append(xyWing.getValueThatCanBeEliminated().toInt())
            .append(" can be eliminated from these cells:");
        xyWing.getTargets().forEach(t -> s.append("<br>").append(t));
        s.append("</html>");
        JOptionPane.showMessageDialog(appFrame, new JLabel(s.toString()));
    }
    
    public void lookForPointingPair() {
        runHintCheck(PointingPair::findNext, this::showPointingPairInfo,  "Did not find any Pointing Pairs :(");
    }
    
    private void showPointingPairInfo(PointingPair pointingPair) {
        // TODO: This can obviously be done in a fancier way.
        String s = "<html>Found a Pointing Pair:<br>" + pointingPair + "</html>";
        JOptionPane.showMessageDialog(appFrame, new JLabel(s));
    }
    
    public void lookForHiddenSingle() {
        runHintCheck(Single::findNextHidden, this::showSingleInfo, "Did not find any Hidden Singles :(");
    }
    
    private void showSingleInfo(Single single) {
        // TODO: This can obviously be done in a fancier way.
        String s = "<html>Found a Hidden Single:<br>" + single.getPosition() + 
                "<br>Value: " + single.getValue() + "</html>";
        JOptionPane.showMessageDialog(appFrame, new JLabel(s));
    }
    
    public void lookForTriple() {
        runHintCheck(Multiple::findNextTriple, this::showTripleInfo, "Did not find any Triples :(");
    }
    
    private void showTripleInfo(Multiple multiple) {
        assert multiple.getPositions().size() == 3;
        StringBuilder s = new StringBuilder("<html>Found a triple:<br>");
        multiple.getPositions().forEach(p -> s.append(p).append("<br>"));
        s.append("Values: ").append(multiple.getValues()).append("</html>");
        JOptionPane.showMessageDialog(appFrame, new JLabel(s.toString()));
    }

    private <T> void runHintCheck(Function<Grid, Optional<T>> hintChecker, Consumer<T> hintUi, String messageWhenNotFound) {
        Callable<Optional<T>> producer = () -> hintChecker.apply(puzzleModel.getGridModel().getGrid());
        Consumer<? super Optional<T>> consumer = o -> {
            o.ifPresentOrElse(hintUi, () -> JOptionPane.showMessageDialog(appFrame, messageWhenNotFound));
        };
        UiThread.offload(producer, consumer);
    }
    
    public void startAutoSolver() {
        UiAutoSolver autoSolver = new UiAutoSolver(appFrame, puzzleModel.getGridModel());
        autoSolver.start();
    }
    
    public void checkSolution() {
        UiThread.offload(this::validatePuzzle, this::displayResult);
    }
    
    private ValidationResult validatePuzzle() {
        ValidationResult result = puzzleModel.validate();
        if (result.isSolved()) {
            puzzleModel.getInventory().markAsCompleted(puzzleModel.getPuzzle());
        }
        return result;
    }
    
    private void displayResult(ValidationResult result) {
        if (result.isSolved()) {
            JOptionPane.showMessageDialog(
                    appFrame, 
                    "Looks good to me! :)", 
                    "Solved", 
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            puzzleModel.getGridModel().decorateInvalidCells(result);
            JOptionPane.showMessageDialog(
                    appFrame, 
                    "Hmm, that doesn't look right. :(", 
                    "Not solved", 
                    JOptionPane.ERROR_MESSAGE);
            puzzleModel.getGridModel().removeInvalidCellsDecoration();
        }
    }
}
