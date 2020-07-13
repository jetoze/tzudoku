package jetoze.tzudoku.ui;

import static java.util.Objects.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.common.collect.ImmutableList;

import jetoze.gunga.UiThread;
import jetoze.tzudoku.model.Puzzle;
import jetoze.tzudoku.model.PuzzleInfo;
import jetoze.tzudoku.model.ValidationResult;

public class PuzzleUiController {
    // TODO: Wait-indication (hour-glass on frame) when background work is in progress.
    
    private final JFrame appFrame;
    private final PuzzleUiModel puzzleModel;
    
    public PuzzleUiController(JFrame appFrame, PuzzleUiModel model) {
        this.appFrame = requireNonNull(appFrame);
        this.puzzleModel = requireNonNull(model);
    }
    
    public void selectPuzzle() {
        UiThread.offload(puzzleModel.getInventory()::listAvailablePuzzles, this::displayInventoryUi);
    }
    
    private void displayInventoryUi(ImmutableList<PuzzleInfo> puzzleInfos) {
        InventoryUi inventoryUi = new InventoryUi(puzzleInfos);
        // TODO: Use a fancier dialog here. For example, we want:
        //   + The ok button to be disabled unless a puzzle is selected
        //   + Double-click in the list should select the puzzle
        //   + Request focus to the list when the dialog opens.
        int option = JOptionPane.showConfirmDialog(
                appFrame,
                inventoryUi.getUi(),
                "Select a Puzzle",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            inventoryUi.getSelectedPuzzle().ifPresent(this::loadPuzzle);
        }
    }
    
    private void loadPuzzle(PuzzleInfo puzzleInfo) {
        UiThread.offload(() -> puzzleModel.getInventory().loadPuzzle(puzzleInfo), this::puzzleLoaded);
    }
    
    private void puzzleLoaded(Puzzle puzzle) {
        puzzleModel.setPuzzle(puzzle);
        appFrame.setTitle(puzzle.getName());
    }
    
    public void saveProgress() {
        Puzzle puzzle = puzzleModel.getPuzzle();
        if (!puzzle.isEmpty()) {
            UiThread.offload(() -> {
                puzzleModel.getInventory().saveProgress(puzzle);
                return null;
            }, v -> {});
        }
    }
    
    public void checkSolution() {
        UiThread.offload(puzzleModel::validate, this::displayResult);
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
