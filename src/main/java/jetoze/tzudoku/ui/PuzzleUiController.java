package jetoze.tzudoku.ui;

import static java.util.Objects.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.common.collect.ImmutableList;

import jetoze.gunga.UiThread;
import jetoze.tzudoku.model.PuzzleInfo;

public class PuzzleUiController {
    // TODO: Wait-indication (hour-glass on frame) when background work is in progress.
    
    private final JFrame appFrame;
    private final PuzzleUiModel puzzleModel;
    
    public PuzzleUiController(JFrame appFrame, PuzzleUiModel model) {
        this.appFrame = requireNonNull(appFrame);
        this.puzzleModel = requireNonNull(model);
    }
    
    public void openInventoryUi() {
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
        UiThread.offload(() -> puzzleModel.getInventory().loadPuzzle(puzzleInfo), puzzleModel::setPuzzle);
    }
}
