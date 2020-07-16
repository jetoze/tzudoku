package jetoze.tzudoku.ui;

import static java.util.Objects.*;

import javax.swing.JFrame;

public class PuzzleBuilderController {
    private final JFrame appFrame;
    private final PuzzleBuilderModel model;
    private final PuzzleBuilderUi ui;
    
    public PuzzleBuilderController(JFrame appFrame, PuzzleBuilderModel model, PuzzleBuilderUi ui) {
        this.appFrame = requireNonNull(appFrame);
        this.model = requireNonNull(model);
        this.ui = requireNonNull(ui);
    }
    
    public void prepareUi() {
        ui.setSuggestedName(model.getInventory().getAvailablePuzzleName("New Puzzle"));
    }
    
    public boolean isExitAllowed() {
        // TODO: Implement me. If there are unsaved changes, prompt the user to save them.
        return true;
    }
    
}
