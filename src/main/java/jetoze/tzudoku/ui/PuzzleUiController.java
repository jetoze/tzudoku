package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.House.Type;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Puzzle;
import jetoze.tzudoku.model.PuzzleInfo;
import jetoze.tzudoku.model.ValidationResult;
import jetoze.tzudoku.model.Value;

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
    
    public void lookForPointingPair() {
        runHintCheck(PointingPair::findNext, this::showPointingPairInfo,  "Did not find any Pointing Pairs :(");
    }
    
    private void showPointingPairInfo(PointingPair pointingPair) {
        String s = "<html>Found a Pointing Pair:<br><br>" +
                "The digit " + pointingPair.getValue() + " in " +
                pointingPair.getBox() + " is confined to positions " +
                pointingPair.getForcingPositions().stream()
                    .sorted(pointingPair.getRowOrColumn().getType().positionOrder())
                    .map(Object::toString)
                    .collect(joining(" ")) + " in " + pointingPair.getRowOrColumn() + ".<br>" +
                pointingPair.getValue() + " can therefore be eliminated from " +
                pointingPair.getTargetPositions().stream()
                    .sorted(pointingPair.getRowOrColumn().getType().positionOrder())
                    .map(Object::toString)
                    .collect(joining(" ")) + " in " + pointingPair.getRowOrColumn() + ".</html>";
        JOptionPane.showMessageDialog(appFrame, new JLabel(s));
    }
    
    public void lookForBoxLineReduction() {
        runHintCheck(BoxLineReduction::findNext, this::showBoxLineReductionInfo, "Did not find any Box Line Reductions :(");
    }

    private void showBoxLineReductionInfo(BoxLineReduction boxLineReduction) {
        House rowOrColumn = boxLineReduction.getRowOrColumn();
        String s = "<html>Found a Box Line Reduction:<br><br>" +
                "The digit " + boxLineReduction.getValue() + " in " +
                rowOrColumn + " is confined to positions " +
                boxLineReduction.getForcingPositions().stream()
                    .sorted(rowOrColumn.getType().positionOrder())
                    .map(Object::toString)
                    .collect(joining(" ")) +
                " in " + boxLineReduction.getBox() + ".<br>" +
                boxLineReduction.getValue() + " can therefore be eliminated from " +
                boxLineReduction.getTargetPositions().stream()
                    .sorted(Type.BOX.positionOrder())
                    .map(Object::toString)
                    .collect(joining(" ")) + " in " + boxLineReduction.getBox() + ".</html>";
        JOptionPane.showMessageDialog(appFrame, new JLabel(s));
    }
    
    public void lookForHiddenSingle() {
        runHintCheck(Single::findNextHidden, this::showSingleInfo, "Did not find any Hidden Singles :(");
    }
    
    private void showSingleInfo(Single single) {
        String s = "<html>Found a Hidden Single:<br>" + single.getPosition() + 
                "<br>Value: " + single.getValue() + "</html>";
        JOptionPane.showMessageDialog(appFrame, new JLabel(s));
    }
    
    public void lookForNakedTriple() {
        runHintCheck(NakedMultiple::findNakedTriple, this::showNakedMultipleInfo, "Did not find any Naked Triples :(");
    }
    
    public void lookForNakedQuadruple() {
        runHintCheck(NakedMultiple::findNakedQuadruple, this::showNakedMultipleInfo, "Did not find any Naked Quadruples :(");
    }
    
    private void showNakedMultipleInfo(NakedMultiple multiple) {
        StringBuilder s = new StringBuilder("<html>Found a ")
                .append(multiple.getTechnique().getName())
                .append(":<br>");
        multiple.getForcingPositions().forEach(p -> s.append(p).append("<br>"));
        s.append("Values: ").append(multiple.getValues()).append("</html>");
        JOptionPane.showMessageDialog(appFrame, new JLabel(s.toString()));
    }
    
    public void lookForHiddenPair() {
        runHintCheck(HiddenMultiple::findHiddenPair, this::showHiddenMultipleInfo, "Did not find any Hidden Pairs :(");
    }
    
    public void lookForHiddenTriple() {
        runHintCheck(HiddenMultiple::findHiddenTriple, this::showHiddenMultipleInfo, "Did not find any Hidden Triples :(");
    }
    
    public void lookForHiddenQuadruple() {
        runHintCheck(HiddenMultiple::findHiddenPair, this::showHiddenMultipleInfo, "Did not find any Hidden Quadruples :(");
    }
    
    private void showHiddenMultipleInfo(HiddenMultiple multiple) {
        StringBuilder s = new StringBuilder("<html>Found a ")
                .append(multiple.getTechnique().getName())
                .append(" of ")
                .append(sortedStringOfValues(multiple.getHiddenValues()))
                .append("<br><br>Values that can be eliminated:<br>");
        for (Position t : multiple.getTargets()) {
            s.append(t)
                .append(": ")
                .append(sortedStringOfValues(multiple.getValuesToEliminate(t)))
                .append("<br>");
        }
        JOptionPane.showMessageDialog(appFrame, new JLabel(s.toString()));
    }
    
    private static String sortedStringOfValues(Collection<Value> values) {
        return values.stream().sorted().map(Object::toString).collect(joining(" "));
    }
    
    public void lookForXWing() {
        runHintCheck(XWing::findNext, this::showXWingInfo, "Did not find any X-Wings :(");
    }
    
    private void showXWingInfo(XWing xwing) {
        StringBuilder s = new StringBuilder("<html>Found an X-Wing:<br><br>");
        s.append("Positions: ");
        s.append(xwing.getPositions().stream().map(Object::toString).collect(Collectors.joining(" ")));
        s.append("<br><br>");
        s.append(xwing.getValue()).append(" can be eliminated from:<br>");
        s.append(xwing.getTargets().stream().map(Object::toString).collect(Collectors.joining(" ")));
        s.append("</html>");
        JOptionPane.showMessageDialog(appFrame, new JLabel(s.toString()));
    }
    
    public void lookForXyWing() {
        runHintCheck(XyWing::findNext, this::showXyWingInfo, "Did not find any XY-Wings :(");
    }
    
    private void showXyWingInfo(XyWing xyWing) {
        StringBuilder s = new StringBuilder("<html>Found an XY-Wing:<br>");
        s.append(xyWing.getCenter());
        xyWing.getWings().forEach(w -> s.append("<br>").append(w));
        s.append("<br><br>").append(xyWing.getValue().toInt())
            .append(" can be eliminated from these cells:");
        xyWing.getTargets().forEach(t -> s.append("<br>").append(t));
        s.append("</html>");
        JOptionPane.showMessageDialog(appFrame, new JLabel(s.toString()));
    }
    
    public void lookForSimpleColoring() {
        runHintCheck(SimpleColoring::findNext, this::showSimpleColoringInfo, "Did not find any Simple Coloring hint :(");
    }
    
    private void showSimpleColoringInfo(SimpleColoring simpleColoring) {
        // TODO: I need to include more info
        String s = "<html>Simple Coloring eliminates the value " + simpleColoring.getValue() + 
                " from these cells:<br><br>" + simpleColoring.getTargets().stream().map(Object::toString).collect(joining(" ")) +
                "</html>";
        JOptionPane.showMessageDialog(appFrame, new JLabel(s));
    }
    
    public void lookForSwordfish() {
        runHintCheck(Swordfish::findNext, this::showSwordfishInfo, "Did not find any Swordfish :(");
    }
    
    private void showSwordfishInfo(Swordfish swordfish) {
        String s = "<html>A Swordfish in " +
                String.format("%s %d, %d, and %d ", (swordfish.getHouseType() == Type.ROW ? "rows" : "columns"), 
                        swordfish.getHouses().get(0).getNumber(),
                        swordfish.getHouses().get(1).getNumber(),
                        swordfish.getHouses().get(2).getNumber()) +
                "eliminates the value " + swordfish.getValue() + 
                " from these cells:<br><br>" + swordfish.getTargets().stream().map(Object::toString).collect(joining(" ")) +
                "</html>";
        JOptionPane.showMessageDialog(appFrame, new JLabel(s));
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
