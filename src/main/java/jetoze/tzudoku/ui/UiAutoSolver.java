package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import jetoze.gunga.UiThread;
import jetoze.gunga.layout.Layouts;
import jetoze.tzudoku.hint.Hint;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.GridSolver;
import jetoze.tzudoku.model.GridSolver.Result;
import jetoze.tzudoku.ui.hint.HintCellDecorator;
import jetoze.tzudoku.ui.hint.HintUi;
import jetoze.tzudoku.ui.hint.HintUiFactory;

/**
 * Attempts to auto-solve the puzzle currently loaded into the UI, giving visual feedback
 * of each individual step. A UiAutoSolver uses a {@link GridSolver} to solve the puzzle.
 * See the documentation of that class for current limitations.
 */
public class UiAutoSolver {
    
    // TODO: Have a preferences page somewhere that allows the user to control
    // which techniques should be used by the auto-solver. For example, by disabling
    // the more advanced techniques you can then solve a puzzle up to the point where
    // an advanced technique is necessary, which could be good practice.
    
    /**
     * The delay between updating the UI with the next completed hint.
     */
    private static final Duration HINT_DELAY = Duration.ofMillis(750L);
    
    private final JFrame appFrame;
    private final GridUiModel gridModel;
    private final HintUiFactory hintUiFactory;
    
    public UiAutoSolver(JFrame appFrame, GridUiModel gridModel, HintUiFactory hintUiFactory) {
        this.appFrame = requireNonNull(appFrame);
        this.gridModel = requireNonNull(gridModel);
        this.hintUiFactory = requireNonNull(hintUiFactory);
    }

    public void start() {
        Controller controller = new Controller(appFrame, gridModel, hintUiFactory);
        controller.start();
    }
    
    
    // XXX: What purpose does the controller class serve now? Perhaps lift out [Timer, Result, hintIndex] to
    // separate class called something like HintDisplayer, and the lift everything out to the UiAutoSolver
    // class itself.
    private static class Controller {
        
        private final JFrame appFrame;
        private final GridUiModel model;
        private final HintUiFactory hintUiFactory;
        private ProgressDialog progressDialog;
        private Timer timer;
        private Result result;
        private int hintIndex;
        private boolean cancelRequested;
        
        public Controller(JFrame appFrame, GridUiModel model, HintUiFactory hintUiFactory) {
            this.appFrame = appFrame;
            this.model = model;
            this.hintUiFactory = hintUiFactory;
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
            UiThread.run(() -> {
                model.clearSelection();
                model.clearHighlightColors();
                HintUi hintUi = hintUiFactory.getUi(hint);
                HintCellDecorator cellDecorator = hintUi.getCellDecorator(model);
                cellDecorator.decorate();
                String description = hintUi.getShortDescription();
                setStatus(description);
                hintUi.apply(model);
            });
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
