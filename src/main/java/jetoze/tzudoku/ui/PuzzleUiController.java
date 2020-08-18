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

import jetoze.gunga.KeyBindings;
import jetoze.gunga.KeyStrokes;
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
    
    public void launchOpeningScreen() { // TODO: I need a better name.
        SelectPuzzleModel selectPuzzleModel = new SelectPuzzleModel(puzzleModel);
        InventoryUi inventoryUi = new InventoryUi(selectPuzzleModel.getInventoryUiModel());
        PuzzleBuilderModel puzzleBuilderModel = selectPuzzleModel.getPuzzleBuilderModel();
        PuzzleBuilderController puzzleBuilderController = new PuzzleBuilderController(appFrame, puzzleBuilderModel);
        PuzzleBuilderUi puzzleBuilderUi = new PuzzleBuilderUi(puzzleBuilderModel,
                puzzleBuilderController::defineSandwiches,
                puzzleBuilderController.getAddKillerCageAction(),
                puzzleBuilderController.getDeleteKillerCageAction());
        SelectPuzzleUi2 os = new SelectPuzzleUi2(selectPuzzleModel, inventoryUi, puzzleBuilderUi);
        selectPuzzleModel.addValidationListener(System.out::println);
        int input = JOptionPane.showConfirmDialog(appFrame, os.getUi(), "Pick a Puzzle", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
        if (input == JOptionPane.OK_OPTION) {
            
        }
    }
    
    public void selectPuzzle() {
        // TODO: Use a utility for this type of dialog use.
        InventoryUiModel model = new InventoryUiModel(puzzleModel.getInventory());
        InventoryUi inventoryUi = new InventoryUi(model);
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
        KeyBindings.whenAncestorOfFocusedComponent(dialog.getRootPane())
            .add(KeyStrokes.ESCAPE, "escape", () -> dialog.setVisible(false));
        inventoryUi.setPuzzleLoader(pi -> {
            dialog.dispose();
            loadPuzzle(pi);
        });
        model.addValidationListener(ok::setEnabled);
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
        PuzzleBuilder puzzleBuilder = new PuzzleBuilder();
        puzzleBuilder.launch();
    }
    
    
    private class PuzzleBuilder {
        private final PuzzleBuilderModel model;
        private final PuzzleBuilderController controller;
        private final PuzzleBuilderUi ui;
        
        public PuzzleBuilder() {
            model = new PuzzleBuilderModel(puzzleModel.getInventory());
            controller = new PuzzleBuilderController(appFrame, model);
            ui = new PuzzleBuilderUi(model,
                    controller::defineSandwiches,
                    controller.getAddKillerCageAction(),
                    controller.getDeleteKillerCageAction());
        }
        
        public void launch() {
            int input = JOptionPane.showConfirmDialog(appFrame, ui.getUi(), "Build New Puzzle", 
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
            if (input == JOptionPane.OK_OPTION) {
                // TODO: Wait indication.
                // TODO: If the puzzle is invalid, the current behavior is to dismiss the dialog, 
                //       show an error dialog explaining what's wrong, and then reopen the 
                //       puzzle builder dialog again, with the same model and UI meaning the user 
                //       input is retained. It would be better to show the error dialog while the
                //       puzzle builder dialog is still open.
                // TODO: Prompt to save changes made to existing puzzle before overwriting it.
                controller.createPuzzle(PuzzleUiController.this::loadPuzzle, this::showInvalidPuzzleMessage);
            }
        }
        
        private void showInvalidPuzzleMessage(Throwable e) {
            String title = (e instanceof PuzzleBuilderException)
                    ? "Invalid or Incomplete Puzzle"
                    : "Unexpected Error";
            JOptionPane.showMessageDialog(
                    appFrame, 
                    e.getMessage(), 
                    title, 
                    JOptionPane.ERROR_MESSAGE);
            launch();
        }
    }

}
