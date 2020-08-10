package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import com.google.common.collect.ImmutableSet;

import jetoze.gunga.UiThread;
import jetoze.gunga.layout.Layouts;
import jetoze.tzudoku.hint.BoxLineReduction;
import jetoze.tzudoku.hint.EliminatingHint;
import jetoze.tzudoku.hint.HiddenMultiple;
import jetoze.tzudoku.hint.Hint;
import jetoze.tzudoku.hint.NakedMultiple;
import jetoze.tzudoku.hint.PointingPair;
import jetoze.tzudoku.hint.SimpleColoring;
import jetoze.tzudoku.hint.Single;
import jetoze.tzudoku.hint.Swordfish;
import jetoze.tzudoku.hint.XWing;
import jetoze.tzudoku.hint.XyWing;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.GridSolver;
import jetoze.tzudoku.model.GridSolver.Result;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;
import jetoze.tzudoku.ui.hint.HintCellDecorator;
import jetoze.tzudoku.ui.hint.HintCellDecorators;

/**
 * Attempts to auto-solve the puzzle currently loaded into the UI, giving visual feedback
 * of each individual step. A UiAutoSolver uses a {@link GridSolver} to solve the puzzle.
 * See the documentation of that class for current limitations.
 */
public class UiAutoSolver {
    
    /**
     * The delay between updating the UI with the next completed hint.
     */
    private static final Duration HINT_DELAY = Duration.ofMillis(750L);
    
    private final JFrame appFrame;
    private final GridUiModel gridModel;
    
    public UiAutoSolver(JFrame appFrame, GridUiModel gridModel) {
        this.appFrame = requireNonNull(appFrame);
        this.gridModel = requireNonNull(gridModel);
    }

    public void start() {
        Controller controller = new Controller(appFrame, gridModel);
        controller.start();
    }
    
    
    // XXX: What purpose does the controller class serve now? Perhaps lift out [Timer, Result, hintIndex] to
    // separate class called something like HintDisplayer, and the lift everything out to the UiAutoSolver
    // class itself.
    private static class Controller {
        
        private final JFrame appFrame;
        private final GridUiModel model;
        private final HintCellDecorators cellDecorators;
        private ProgressDialog progressDialog;
        private Timer timer;
        private Result result;
        private int hintIndex;
        private boolean cancelRequested;
        
        public Controller(JFrame appFrame, GridUiModel model) {
            this.appFrame = appFrame;
            this.model = model;
            this.cellDecorators = new HintCellDecorators(model);
        }
        
        public void start() {
            cancelRequested = false;
            // Offload the start of the solver so that we can open the modal progress dialog.
            UiThread.runLater(() -> {
                setStatus("Filling in candidates");
                // TODO: Reset the eliminateCandidatesProperty to its original value
                // when we are done?
                model.getEliminateCandidatesProperty().set(true);
                UiThread.offload(this::solveGrid, this::replayResult);
            });
            progressDialog = new ProgressDialog(() -> {
                cancelRequested = true;
                model.clearHighlightColors();
            });
            progressDialog.open(appFrame);
        }
        
        private Result solveGrid() {
            GridSolver solver = new GridSolver(Grid.copyOf(model.getGrid()));
            return solver.solve();
        }
        
        private void setStatus(String text) {
            UiThread.throwIfNotUiThread();
            progressDialog.setStatus(text);
        }

        private void replayResult(Result result) {
            // TODO: If we reach a point here only Naked and Hidden singles 
            // remain in the replay list, speed up the timer.
            if (cancelRequested) {
                return;
            }
            this.result = result;
            this.hintIndex = 0;
            model.showRemainingCandidates();
            timer = new Timer((int) HINT_DELAY.toMillis(), e -> applyNextHint());
            timer.start();
        }
        
        private void applyNextHint() {
            if (cancelRequested) {
                return;
            }
            Hint hint = result.getHintsApplied().get(hintIndex);
            updateUi(hint);
            ++hintIndex;
            if (hintIndex == result.getHintsApplied().size()) {
                stop();
            }
        }
        
        private void stop() {
            UiThread.throwIfNotUiThread();
            timer.stop();
            progressDialog.close();
            model.clearHighlightColors();
            if (model.getGrid().isSolved()) {
                showSuccessMessage();
            } else {
                JOptionPane.showMessageDialog(appFrame, "This puzzle proved too difficult for me to solve :(", 
                        "No Solution Found", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void showSuccessMessage() {
            JOptionPane.showMessageDialog(appFrame, "Ta-da!", "Solved", JOptionPane.INFORMATION_MESSAGE);
        }
        
        public void updateUi(Hint hint) {
            // XXX: Ugly casts here, of course.
            UiThread.run(() -> {
                model.clearHighlightColors();
                HintCellDecorator cellDecorator = cellDecorators.getDecorator(hint);
                cellDecorator.decorate();
                if (hint instanceof Single) {
                    applyHint((Single) hint);
                } else if (hint instanceof PointingPair) {
                    applyHint((PointingPair) hint);
                } else if (hint instanceof BoxLineReduction) {
                    applyHint((BoxLineReduction) hint);
                } else if (hint instanceof NakedMultiple) {
                    applyHint((NakedMultiple) hint);
                } else if (hint instanceof HiddenMultiple) {
                    applyHint((HiddenMultiple) hint);
                } else if (hint instanceof XWing) {
                    applyHint((XWing) hint);
                } else if (hint instanceof XyWing) {
                    applyHint((XyWing) hint);
                } else if (hint instanceof SimpleColoring) {
                    applyHint((SimpleColoring) hint);
                } else if (hint instanceof Swordfish) {
                    applyHint((Swordfish) hint);
                } else {
                    throw new RuntimeException("Unknown hint: " + hint);
                }
            });
        }
        
        private void applyHint(Single single) {
            setStatus(single.getTechnique().getName() + ": " + single.getValue());
            model.enterValue(single.getPosition(), single.getValue());
        }
        
        private void applyHint(EliminatingHint hint) {
            String valueString = hint.getValues().size() == 1
                    ? hint.getValues().iterator().next().toString()
                    : valuesAsSortedString(hint.getValues());
            setStatus(hint.getTechnique().getName() + ": " + valueString);
            model.removeCandidatesFromCells(hint.getTargetPositions(), hint.getValues());
        }
        
        private String valuesAsSortedString(Set<Value> values) {
            return values.stream()
                    .sorted()
                    .map(Object::toString)
                    .collect(joining(" "));
        }
        
        private void applyHint(HiddenMultiple multiple) {
            setStatus(multiple.getTechnique().getName() + ": " + valuesAsSortedString(multiple.getHiddenValues()));
            // XXX: This messes up Undo-Redo, since we can't apply this change
            // as an atomic operation at the moment.
            for (Position target : multiple.getTargets()) {
                model.removeCandidatesFromCells(Collections.singleton(target), multiple.getValuesToEliminate(target));
            }
        }
        
        private void applyHint(SimpleColoring simpleColoring) {
            setStatus(simpleColoring.getTechnique().getName() + ": " + simpleColoring.getValue());
            model.removeCandidatesFromCells(simpleColoring.getTargets(), ImmutableSet.of(simpleColoring.getValue()));
        }
    }
    
    
    private static class ProgressDialog {
        private final JLabel statusLabel = new JLabel(" ".repeat(60));
        private final JButton cancelButton = new JButton("Stop");
        private final Runnable cancelHandler;
        private JDialog dialog;
        
        public ProgressDialog(Runnable cancelHandler) {
            this.cancelHandler = cancelHandler;
            cancelButton.addActionListener(e -> cancelHandler.run());
        }
        
        public void setStatus(String text) {
            statusLabel.setText(text);
        }
        
        public void open(JFrame appFrame) {
            dialog = new JDialog(appFrame, "Auto-solve Progress", true);
            JPanel buttonPanel = Layouts.border().east(cancelButton).build();
            dialog.setContentPane(Layouts.border(0, 10)
                    .center(statusLabel)
                    .south(buttonPanel)
                    .withBorder(new EmptyBorder(10, 10, 10, 10))
                    .build());
            dialog.pack();
            Point appFrameLocation = appFrame.getLocationOnScreen();
            Point dialogLocation = new Point(
                    appFrameLocation.x + appFrame.getWidth() - dialog.getWidth() - 12,
                    appFrameLocation.y + appFrame.getHeight() - dialog.getHeight() - 40);
            dialog.setLocation(dialogLocation);
            dialog.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent e) {
                    cancelHandler.run();
                }
            });
            cancelButton.addActionListener(e -> UiThread.runLater(dialog::dispose));
            dialog.setVisible(true);
        }
        
        public void close() {
            if (dialog != null) {
                dialog.dispose();
            }
        }
    }

}
