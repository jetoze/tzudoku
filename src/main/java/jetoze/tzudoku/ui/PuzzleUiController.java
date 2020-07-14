package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

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
    private final StatusPanel statusPanel;
    
    public PuzzleUiController(JFrame appFrame, PuzzleUiModel model, StatusPanel statusPanel) {
        this.appFrame = requireNonNull(appFrame);
        this.puzzleModel = requireNonNull(model);
        this.statusPanel = requireNonNull(statusPanel);
    }
    
    public void selectPuzzle() {
        // TODO: Allow input of a new Puzzle, using a Grid of size SMALL.
        UiThread.offload(puzzleModel.getInventory()::listAvailablePuzzles, this::displaySelectNewPuzzleUi);
    }
    
    private void displaySelectNewPuzzleUi(ImmutableList<PuzzleInfo> puzzleInfos) {
        InventoryUi inventoryUi = new InventoryUi(puzzleInfos);
        CreateNewPuzzleUi createPuzzleUi = new CreateNewPuzzleUi();
        SelectPuzzleUi selectPuzzleUi = new SelectPuzzleUi(inventoryUi, createPuzzleUi);
        SelectPuzzleController controller = new SelectPuzzleController(appFrame, puzzleModel, selectPuzzleUi);
        controller.openUi();
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
            }, v -> statusPanel.setStatus("Puzzle was saved."), true);
        }
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
