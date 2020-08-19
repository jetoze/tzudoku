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
    
    public void selectPuzzle() {
        PuzzleSelector puzzleSelector = new PuzzleSelector(puzzleModel);
        puzzleSelector.open();
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
    
    private void showInvalidNewPuzzleMessage(Throwable e, Runnable runWhenDismissed) {
        String title = (e instanceof PuzzleBuilderException)
                ? "Invalid or Incomplete Puzzle"
                : "Unexpected Error";
        JOptionPane.showMessageDialog(
                appFrame, 
                e.getMessage(), 
                title, 
                JOptionPane.ERROR_MESSAGE);
        UiThread.runLater(runWhenDismissed);
    }

    
    private class PuzzleSelector {
        private final SelectPuzzleModel selectPuzzleModel;
        private final PuzzleBuilderController puzzleBuilderController;
        private final InventoryUi inventoryUi;
        private final SelectPuzzleUi selectPuzzleUi;
        
        public PuzzleSelector(PuzzleUiModel masterModel) {
            selectPuzzleModel = new SelectPuzzleModel(masterModel);
            inventoryUi = new InventoryUi(selectPuzzleModel.getInventoryUiModel());
            PuzzleBuilderModel puzzleBuilderModel = selectPuzzleModel.getPuzzleBuilderModel();
            puzzleBuilderController = new PuzzleBuilderController(appFrame, puzzleBuilderModel);
            PuzzleBuilderUi puzzleBuilderUi = new PuzzleBuilderUi(puzzleBuilderModel,
                    puzzleBuilderController::defineSandwiches,
                    puzzleBuilderController.getAddKillerCageAction(),
                    puzzleBuilderController.getDeleteKillerCageAction());
            selectPuzzleUi = new SelectPuzzleUi(selectPuzzleModel, inventoryUi, puzzleBuilderUi);
        }
        
        public void open() {
            JButton ok = UiLook.createOptionDialogButton("OK", this::loadPuzzle);
            JButton cancel = UiLook.createOptionDialogButton("Cancel", () -> {});
            
            inventoryUi.setPuzzleLoader(pi -> ok.doClick());
            
            Consumer<Boolean> validationListener = ok::setEnabled;
            selectPuzzleModel.addValidationListener(validationListener);
            JOptionPane optionPane = new JOptionPane(
                    selectPuzzleUi.getUi(),
                    JOptionPane.PLAIN_MESSAGE,
                    JOptionPane.OK_CANCEL_OPTION,
                    null,
                    new JButton[] {ok, cancel},
                    ok);
            JDialog dialog = new JDialog(appFrame, "Select a Puzzle", true);
            dialog.setContentPane(optionPane);
            dialog.pack();
            dialog.setLocationRelativeTo(appFrame);
            dialog.addWindowListener(new WindowAdapter() {

                @Override
                public void windowOpened(WindowEvent e) {
                    selectPuzzleUi.requestFocus();
                }
            });
            KeyBindings.whenAncestorOfFocusedComponent(dialog.getRootPane())
                .add(KeyStrokes.ESCAPE, "escape", () -> dialog.setVisible(false));
            dialog.setVisible(true);
            selectPuzzleModel.removeValidationListener(validationListener);
        }
        
        private void loadPuzzle() {
            switch (selectPuzzleModel.getSelectedOption()) {
            case SELECT_EXISTING_PUZZLE:
                selectPuzzleModel.getInventoryUiModel().getSelectedPuzzle()
                .ifPresent(this::loadExistingPuzzle);
                break;
            case BUILD_NEW_PUZZLE:
                puzzleBuilderController.createPuzzle(PuzzleUiController.this::loadPuzzle, e -> {
                    showInvalidNewPuzzleMessage(e, this::open);
                });
                break;
            }
        }
        
        private void loadExistingPuzzle(PuzzleInfo puzzleInfo) {
            UiThread.offload(() -> puzzleModel.getInventory().loadPuzzle(puzzleInfo), PuzzleUiController.this::loadPuzzle);
        }
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
            JButton ok = UiLook.createOptionDialogButton("OK", () -> {
                // TODO: Wait indication.
                // TODO: Prompt to save changes made to existing puzzle before overwriting it.
                controller.createPuzzle(PuzzleUiController.this::loadPuzzle, e -> {
                    showInvalidNewPuzzleMessage(e, this::launch);
                });
            });
            JButton cancel = UiLook.createOptionDialogButton("Cancel", () -> {});
            Consumer<Boolean> validationListener = ok::setEnabled;
            model.addValidationListener(validationListener);

            JOptionPane optionPane = new JOptionPane(
                    ui.getUi(),
                    JOptionPane.PLAIN_MESSAGE,
                    JOptionPane.OK_CANCEL_OPTION,
                    null,
                    new JButton[] {ok, cancel},
                    ok);
            JDialog dialog = new JDialog(appFrame, "Select a Puzzle", true);
            dialog.setContentPane(optionPane);
            dialog.pack();
            dialog.setLocationRelativeTo(appFrame);
            dialog.addWindowListener(new WindowAdapter() {

                @Override
                public void windowOpened(WindowEvent e) {
                    ui.requestFocus();
                }
            });
            KeyBindings.whenAncestorOfFocusedComponent(dialog.getRootPane())
                .add(KeyStrokes.ESCAPE, "escape", () -> dialog.setVisible(false));
            dialog.setVisible(true);
            model.removeValidationListener(validationListener);
        }
    }

}
