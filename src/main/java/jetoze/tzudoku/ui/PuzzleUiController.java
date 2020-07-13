package jetoze.tzudoku.ui;

import static java.util.Objects.*;

import javax.swing.JFrame;

import com.google.common.collect.ImmutableList;

import jetoze.gunga.UiThread;
import jetoze.tzudoku.model.PuzzleInfo;

public class PuzzleUiController {
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
        // TODO: Complete me.
    }
}
