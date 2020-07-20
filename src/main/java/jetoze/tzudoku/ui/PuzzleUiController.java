package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jetoze.gunga.UiThread;
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
