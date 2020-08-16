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
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.GridSolver;
import jetoze.tzudoku.model.Puzzle;
import jetoze.tzudoku.model.PuzzleInfo;
import jetoze.tzudoku.model.ValidationResult;
import jetoze.tzudoku.ui.hint.HintUiFactory;

public class PuzzleUiController {
    // TODO: Wait-indication (hour-glass on frame) when background work is in progress.
    // TODO: When showing Hint, provide option for Applying the hint. To do this, add methods
    //       apply(Hint) to GridUiModel (one per supported hint), and then update the UiAutoSolver
    //       to call these methods as well.
    // TODO: Add "Look for Hint" option, that goes through all known SolvingTechniques until 
    //       a Hint is found.
    
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

    // TODO: Move me to the HintController?
    public void startAutoSolver() {
        UiAutoSolver autoSolver = new UiAutoSolver(appFrame, puzzleModel.getGridModel(), new HintUiFactory());
        autoSolver.start();
    }
    
    public void analyze() {
        // TODO: Hour-glass while the solver is running.
        // TODO: Give an error message if not a classic sudoku puzzle?
        Callable<GridSolver.Result> analyzer = () -> {
            Grid copyOfGrid = Grid.copyOf(puzzleModel.getGridModel().getGrid());
            GridSolver solver = new GridSolver(copyOfGrid);
            return solver.solve();
        };
        UiThread.offload(analyzer, this::showAnalyzerResult);
    }
    
    private void showAnalyzerResult(GridSolver.Result result) {
        AnalyzerResultUi ui = new AnalyzerResultUi(result);
        JOptionPane.showMessageDialog(appFrame, ui.getUi(), "Analyzer Result", JOptionPane.INFORMATION_MESSAGE);
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
    
    public void restart() {
        int option = JOptionPane.showConfirmDialog(
                appFrame, "Are you sure?", "Restart the puzzle", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            // TODO: This should really reload the existing puzzle, to guarantee correct behavior
            // e.g. around cells that are colored by the puzzle itself. When we do that we must also
            // truncate the current undo-redo history.
            puzzleModel.getGridModel().reset();
        }
    }
    
    public void buildNewPuzzle() {
        PuzzleBuilderModel model = new PuzzleBuilderModel(puzzleModel.getInventory());
        PuzzleBuilderController controller = new PuzzleBuilderController(appFrame, model);
        PuzzleBuilderUi ui = new PuzzleBuilderUi(model,
                controller::createPuzzle,
                controller::reset,
                controller::defineSandwiches,
                controller.getAddKillerCageAction(),
                controller.getDeleteKillerCageAction());
        // TODO: I will likely need a more advanced dialog than this -- this is just to
        // get the code up and running and get a sense of user experience.
        JOptionPane.showConfirmDialog(appFrame, ui.getUi(), "Build New Puzzle", JOptionPane.OK_CANCEL_OPTION);
    }
}
