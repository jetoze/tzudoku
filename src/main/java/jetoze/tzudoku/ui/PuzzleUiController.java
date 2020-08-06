package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jetoze.gunga.UiThread;
import jetoze.tzudoku.hint.BoxLineReduction;
import jetoze.tzudoku.hint.HiddenMultiple;
import jetoze.tzudoku.hint.NakedMultiple;
import jetoze.tzudoku.hint.PointingPair;
import jetoze.tzudoku.hint.SimpleColoring;
import jetoze.tzudoku.hint.Single;
import jetoze.tzudoku.hint.Swordfish;
import jetoze.tzudoku.hint.XWing;
import jetoze.tzudoku.hint.XyWing;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.GridSolver;
import jetoze.tzudoku.model.Puzzle;
import jetoze.tzudoku.model.PuzzleInfo;
import jetoze.tzudoku.model.ValidationResult;
import jetoze.tzudoku.ui.hint.HintDisplay;

public class PuzzleUiController {
    // TODO: Wait-indication (hour-glass on frame) when background work is in progress.
    // TODO: When showing Hint, provide option for Applying the hint. To do this, add methods
    //       apply(Hint) to GridUiModel (one per supported hint), and then update the UiAutoSolver
    //       to call these methods as well.
    // TODO: Add "Look for Hint" option, that goes through all known SolvingTechniques until 
    //       a Hint is found.
    
    private final JFrame appFrame;
    private final PuzzleUiModel puzzleModel;
    private final HintDisplay hintDisplay;
    private final StatusPanel statusPanel;
    
    public PuzzleUiController(JFrame appFrame, PuzzleUiModel model, StatusPanel statusPanel) {
        this.appFrame = requireNonNull(appFrame);
        this.puzzleModel = requireNonNull(model);
        this.statusPanel = requireNonNull(statusPanel);
        // TODO: I should be injected too I think
        this.hintDisplay = new HintDisplay(appFrame, model.getGridModel());
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
    
    public void lookForPointingPair() {
        runHintCheck(PointingPair::findNext, hintDisplay::showPointingPairInfo,  "Did not find any Pointing Pairs :(");
    }
    
    public void lookForBoxLineReduction() {
        runHintCheck(BoxLineReduction::findNext, hintDisplay::showBoxLineReductionInfo, "Did not find any Box Line Reductions :(");
    }
    
    public void lookForHiddenSingle() {
        runHintCheck(Single::findNextHidden, hintDisplay::showSingleInfo, "Did not find any Hidden Singles :(");
    }
    
    public void lookForNakedTriple() {
        runHintCheck(NakedMultiple::findNakedTriple, hintDisplay::showNakedMultipleInfo, "Did not find any Naked Triples :(");
    }
    
    public void lookForNakedQuadruple() {
        runHintCheck(NakedMultiple::findNakedQuadruple, hintDisplay::showNakedMultipleInfo, "Did not find any Naked Quadruples :(");
    }
    
    public void lookForHiddenPair() {
        runHintCheck(HiddenMultiple::findHiddenPair, hintDisplay::showHiddenMultipleInfo, "Did not find any Hidden Pairs :(");
    }
    
    public void lookForHiddenTriple() {
        runHintCheck(HiddenMultiple::findHiddenTriple, hintDisplay::showHiddenMultipleInfo, "Did not find any Hidden Triples :(");
    }
    
    public void lookForHiddenQuadruple() {
        runHintCheck(HiddenMultiple::findHiddenPair, hintDisplay::showHiddenMultipleInfo, "Did not find any Hidden Quadruples :(");
    }
    
    public void lookForXWing() {
        runHintCheck(XWing::findNext, hintDisplay::showXWingInfo, "Did not find any X-Wings :(");
    }
    
    public void lookForXyWing() {
        runHintCheck(XyWing::findNext, hintDisplay::showXyWingInfo, "Did not find any XY-Wings :(");
    }
    
    public void lookForSimpleColoring() {
        runHintCheck(SimpleColoring::findNext, hintDisplay::showSimpleColoringInfo, "Did not find any Simple Coloring hint :(");
    }
    
    public void lookForSwordfish() {
        runHintCheck(Swordfish::findNext, hintDisplay::showSwordfishInfo, "Did not find any Swordfish :(");
    }

    private <T> void runHintCheck(Function<Grid, Optional<T>> hintChecker, Consumer<T> hintUi, String messageWhenNotFound) {
        Callable<Optional<T>> producer = () -> hintChecker.apply(puzzleModel.getGridModel().getGrid());
        Consumer<? super Optional<T>> consumer = o -> {
            o.ifPresentOrElse(hintUi, () -> JOptionPane.showMessageDialog(appFrame, messageWhenNotFound));
        };
        UiThread.offload(producer, consumer);
    }
    
    public void startAutoSolver() {
        UiAutoSolver autoSolver = new UiAutoSolver(appFrame, puzzleModel.getGridModel());
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
}
