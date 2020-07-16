package jetoze.tzudoku.ui;

import static java.util.Objects.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import jetoze.gunga.UiThread;
import jetoze.tzudoku.TzudokuApp;
import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Puzzle;

public class PuzzleBuilderController {
    private final JFrame appFrame;
    private final PuzzleBuilderModel model;
    
    public PuzzleBuilderController(JFrame appFrame, PuzzleBuilderModel model, PuzzleBuilderUi ui) {
        this.appFrame = requireNonNull(appFrame);
        this.model = requireNonNull(model);
        ui.setSaveAction(this::createPuzzle);
    }
    
    public boolean isExitAllowed() {
        // TODO: Implement me. If there are unsaved changes, prompt the user to save them.
        return true;
    }
    
    public void reset() {
        // TODO: Check for unsaved changes.
        model.reset();
    }
    
    public void createPuzzle() {
        String name = model.getPuzzleName();
        if (name.isBlank()) {
            showErrorMessage("Please enter a puzzle name.");
            return;
        }
        // TODO: Wait indication.
        UiThread.offload(() -> createPuzzleFromTemplate(name), this::reset);
    }
    
    private void createPuzzleFromTemplate(String name) {
        Grid templateGrid = model.getGridModel().getGrid();
        // TODO: This will need to change once we have sandwiches, thermos,
        // and other restrictions.
        if (templateGrid.isEmpty()) {
            UiThread.run(() -> showErrorMessage("The grid is empty."));
            return;
        }
        ImmutableSet<Position> duplicates = templateGrid.getCellsWithDuplicateValues();
        if (!duplicates.isEmpty()) {
            UiThread.run(() -> showErrorMessage("There are duplicate values."));
            return;
        }
        Map<Position, Cell> cells = new HashMap<>();
        ImmutableMap<Position, Cell> templateCells = templateGrid.getCells();
        templateCells.forEach((p, c) -> {
            Cell cellToUse = c.getValue()
                    .map(Cell::given)
                    .orElse(c);
            cells.put(p, cellToUse);
        });
        Grid grid = new Grid(cells);
        Puzzle puzzle = new Puzzle(name, grid);
        try {
            model.getInventory().addNewPuzzle(puzzle);
        } catch (IOException e) {
            // TODO: Log the exception
            UiThread.run(() -> showErrorMessage("Could not save the puzzle: " + e.getMessage()));
        }
        UiThread.runLater(() -> showPuzzleSavedMessage(puzzle));
    }
    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(
                appFrame, 
                message, 
                "Invalid puzzle", 
                JOptionPane.ERROR_MESSAGE);
    }
    
    private void showPuzzleSavedMessage(Puzzle puzzle) {
        int option = JOptionPane.showConfirmDialog(
                appFrame, 
                "The puzzle has been created. Do you want to solve it?", 
                "Puzzle Saved", 
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
        if (option == JOptionPane.YES_OPTION) {
            UiThread.runLater(() -> launchTzudokuApp(puzzle));
        }
    }
    
    private void launchTzudokuApp(Puzzle puzzle) {
        appFrame.dispose();
        TzudokuApp tzudoku = new TzudokuApp(model.getInventory(), puzzle);
        tzudoku.start();
        // TODO: Interesting dilemma, in that as far as the OS is concerned,
        // it is still the Puzzle Builder App that is running (as seen e.g. in
        // the OSX menu bar).
    }
}
