package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import jetoze.gunga.UiThread;
import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Puzzle;
import jetoze.tzudoku.model.PuzzleInfo;

public class SelectPuzzleController {
    private final JFrame appFrame;
    private final PuzzleUiModel puzzleModel;
    private final SelectPuzzleUi ui;
    
    public SelectPuzzleController(JFrame appFrame, 
                                  PuzzleUiModel puzzleModel,
                                  SelectPuzzleUi ui) {
        this.appFrame = requireNonNull(appFrame);
        this.puzzleModel = requireNonNull(puzzleModel);
        this.ui = requireNonNull(ui);
    }
    
    public void openUi() {
        int option = JOptionPane.showConfirmDialog(
                appFrame,
                ui.getUi(),
                "Select a Puzzle",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            handleSelection();
        }
    }
    
    private void handleSelection() {
        switch (ui.getMode()) {
        case EXISTING:
            ui.getSelectedExistingPuzzle().ifPresentOrElse(this::loadExistingPuzzle, this::showSelectPuzzleError);
            break;
        case NEW:
            Puzzle template = ui.getNewPuzzleTemplate();
            loadPuzzleFromTemplate(template);
            break;
        default:
            throw new RuntimeException("Unexpected mode: " + ui.getMode());
        }
    }
    
    private void loadExistingPuzzle(PuzzleInfo puzzleInfo) {
        UiThread.offload(() -> puzzleModel.getInventory().loadPuzzle(puzzleInfo), this::puzzleLoaded);
    }
    
    private void puzzleLoaded(Puzzle puzzle) {
        if (puzzle != null) {
            puzzleModel.setPuzzle(puzzle);
            appFrame.setTitle(puzzle.getName());
        }
    }
    
    private void showSelectPuzzleError() {
        UiThread.run(() -> {
            JOptionPane.showMessageDialog(
                    appFrame, 
                    "Please select a puzzle.", 
                    "No puzzle selected", 
                    JOptionPane.ERROR_MESSAGE);
            UiThread.runLater(this::openUi);
        });
    }
    
    private void loadPuzzleFromTemplate(Puzzle template) {
        UiThread.offload(() -> createPuzzleFromTemplate(template), this::puzzleLoaded);
    }
    
    private Puzzle createPuzzleFromTemplate(Puzzle template) {
        // The template contains only user-inputted values. We must convert it to
        // a grid of given values.
        Grid templateGrid = template.getGrid();
        ImmutableSet<Position> duplicates = templateGrid.getCellsWithDuplicateValues();
        if (!duplicates.isEmpty()) {
            UiThread.run(this::showInvalidNewPuzzleMessage);
            return null;
        }
        Map<Position, Cell> cells = new HashMap<>();
        ImmutableMap<Position, Cell> templateCells = template.getGrid().getCells();
        templateCells.forEach((p, c) -> {
            Cell cellToUse = c.getValue()
                    .map(Cell::given)
                    .orElse(c);
            cells.put(p, cellToUse);
        });
        Grid grid = new Grid(cells);
        Puzzle puzzle = new Puzzle(template.getName(), grid);
        addNewPuzzleToInventory(puzzle);
        return puzzle;
    }
    
    private void showInvalidNewPuzzleMessage() {
        // TODO: Decorate invalid cells when we reopen the selection UI.
        JOptionPane.showMessageDialog(
                appFrame, 
                "The new puzzle contains duplicate values.", 
                "Invalid puzzle", 
                JOptionPane.ERROR_MESSAGE);
        UiThread.runLater(this::openUi);
    }

    private void addNewPuzzleToInventory(Puzzle puzzle) {
        try {
            puzzleModel.getInventory().addNewPuzzle(puzzle);
        } catch (IOException e) {
            // TODO: Handle me.
            e.printStackTrace();
        }
    }
}
