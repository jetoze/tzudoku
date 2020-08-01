package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

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
import jetoze.tzudoku.hint.Hint;
import jetoze.tzudoku.hint.Multiple;
import jetoze.tzudoku.hint.PointingPair;
import jetoze.tzudoku.hint.Single;
import jetoze.tzudoku.hint.XWing;
import jetoze.tzudoku.hint.XyWing;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

/**
 * Attempts to auto-solve the puzzle currently loaded into the UI, giving visual feedback
 * of each individual step.
 * <p>
 * This auto solver only applies classic sudoku techniques.
 */
public class UiAutoSolver {
    
    // TODO: When applying a hint that eliminates candidates, color the cells involved,
    //       using a different color for the target cells than the generating cells.

    // TODO: Create a non-UI version of this auto solver. It can analyze the current puzzle,
    //       and provide the following information without actually showing the solution:
    //         1. Could the puzzle be solved?
    //         2. If it could be solved or not.
    //         3. Statistics on what types of hints were used, in particular advanced 
    //            (triples(?), X-Wings, XY-Wings) hints.
    //         4. For puzzles that could be solved, the hint statistics can be used to
    //            rate the difficulty of the puzzle.

    // TODO: Clean me up, I've become messy. Especially the code around the delays.
    
    /**
     * The delay after a completed hint is displayed in the UI before the process continues.
     */
    private static final Duration HINT_DELAY = Duration.ofMillis(750L);
    /**
     * The delay between steps if the previous step did not find anything. This is purely
     * for visual purposes, to provide a more pleasant progress display.
     */
    private static final Duration NEXT_STEP_DELAY = Duration.ofMillis(50L);
    
    private final JFrame appFrame;
    private final GridUiModel gridModel;
    
    public UiAutoSolver(JFrame appFrame, GridUiModel gridModel) {
        this.appFrame = requireNonNull(appFrame);
        this.gridModel = requireNonNull(gridModel);
    }

    public void start() {
        // First fill in all candidates.
        // Then find and apply hints in this order:
        //   1. Naked Single
        //   2. Hidden Single
        //   3. Naked Pair
        //   4. Pointing Pair
        //   5. Triple
        //   6. X-Wing
        //   6. XY-Wing
        //
        // If a hint is found, the next hint will *always* be Naked Single. If no hint is
        // found, move to the next hint. If we reach the end of the hint list without finding
        // anything, we give up.
        //
        // We run this on a timer, that fires every second or so, for a more pleasant 
        // user experience.
        Controller controller = new Controller(appFrame, gridModel);
        controller.start();
    }
    
    
    private static class Controller {
        
        private final JFrame appFrame;
        private final GridUiModel model;
        private ProgressDialog progressDialog;
        private boolean cancelRequested;
        
        public Controller(JFrame appFrame, GridUiModel model) {
            this.appFrame = appFrame;
            this.model = model;
        }

        public GridUiModel getModel() {
            return model;
        }
        
        public void start() {
            cancelRequested = false;
            UiThread.runLater(() -> {
                // TODO: Restore the original setting when we are done?
                setStatus("Filling in candidates");
                model.getEliminateCandidatesProperty().set(true);
                new FillInCandidates().run(this);
            });
            progressDialog = new ProgressDialog(() -> cancelRequested = true);
            progressDialog.open(appFrame);
        }
        
        public void setStatus(String text) {
            UiThread.throwIfNotUiThread();
            progressDialog.setStatus(text);
        }
        
        public void stop() {
            UiThread.throwIfNotUiThread();
            progressDialog.close();
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
        
        public void runNextStepAfterHintNotAvailable(Step step) {
            runNextStepAfterDelayImpl(step, NEXT_STEP_DELAY);
        }
        
        public void runNextStepAfterCompletedHint(Step step) {
            runNextStepAfterDelayImpl(step, HINT_DELAY);
        }
        
        private void runNextStepAfterDelayImpl(Step step, Duration delay) {
            UiThread.throwIfNotUiThread();
            if (cancelRequested) {
                return;
            }
            Timer timer = new Timer((int) delay.toMillis(), e -> this.runNextStepImpl(step));
            timer.setRepeats(false);
            timer.start();
        }

        private void runNextStepImpl(Step step) {
            UiThread.throwIfNotUiThread();
            if (cancelRequested) {
                return;
            }
            requireNonNull(step);
            if (model.getGrid().isSolved()) {
                progressDialog.close();
                showSuccessMessage();
            } else {
                step.run(this);
            }
        }
        
        

        public void updateUi(Hint hint, String name) {
            // FIXME: Refactor the Step implementation so that we can pass Hints of
            // specific types, without having to cast.
            UiThread.run(() -> {
                setStatus("Found " + name + ".");
                if (hint instanceof Single) {
                    applyHint((Single) hint);
                } else if (hint instanceof PointingPair) {
                    applyHint((PointingPair) hint);
                } else if (hint instanceof Multiple) {
                    applyHint((Multiple) hint);
                } else if (hint instanceof XWing) {
                    applyHint((XWing) hint);
                } else if (hint instanceof XyWing) {
                    applyHint((XyWing) hint);
                } else {
                    throw new RuntimeException("Unknown hint: " + hint);
                }
            });
        }
        
        private void applyHint(Single single) {
            model.setEnterValueMode(EnterValueMode.NORMAL);
            model.selectCellAt(single.getPosition());
            model.enterValue(single.getValue());
        }
        
        private void applyHint(PointingPair pointingPair) {
            removeCandidates(pointingPair.getTargets(), ImmutableSet.of(pointingPair.getValue()));
        }
        
        private void applyHint(Multiple multiple) {
            removeCandidates(multiple.getTargets(), multiple.getValues());
        }
        
        private void applyHint(XWing xwing) {
            removeCandidates(xwing.getTargets(), ImmutableSet.of(xwing.getValue()));
        }
        
        private void applyHint(XyWing xyWing) {
            removeCandidates(xyWing.getTargets(), ImmutableSet.of(xyWing.getValue()));
        }
        
        private void removeCandidates(ImmutableSet<Position> targets, ImmutableSet<Value> values) {
            model.selectCellsAt(targets);
            model.removeCandidatesFromCells(targets, values);
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
    
    
    private interface Step {
        
        void run(Controller controller);
        
    }
    
    
    private static class FillInCandidates implements Step {

        @Override
        public void run(Controller controller) {
            controller.getModel().showRemainingCandidates();
            controller.runNextStepAfterCompletedHint(HintStep.NAKED_SINGLE);
        }
    }
    
    
    private static enum HintStep implements Step {
        
        NAKED_SINGLE("Naked Single", Single::findNextNaked) {

            @Override
            protected Optional<HintStep> getNextStep() {
                return Optional.of(HIDDEN_SINGLE);
            }
        },
        
        HIDDEN_SINGLE("Hidden Single", Single::findNextHidden) {

            @Override
            protected Optional<HintStep> getNextStep() {
                return Optional.of(NAKED_PAIR);
            }

        },
        
        NAKED_PAIR("Naked Pair", Multiple::findNextPair) {

            @Override
            protected Optional<HintStep> getNextStep() {
                return Optional.of(POINTING_PAIR);
            }
        },
        
        POINTING_PAIR("Pointing Pair", PointingPair::findNext) {

            @Override
            protected Optional<HintStep> getNextStep() {
                return Optional.of(TRIPLE);
            }
        },
        
        TRIPLE("Triple", Multiple::findNextTriple) {

            @Override
            protected Optional<HintStep> getNextStep() {
                return Optional.of(X_WING);
            }
        },
        
        X_WING("X-Wing", XWing::findNext) {

            @Override
            protected Optional<HintStep> getNextStep() {
                return Optional.of(XY_WING);
            }
        },
        
        XY_WING("XY-Wing", XyWing::findNext) {

            @Override
            protected Optional<HintStep> getNextStep() {
                // This is the last step.
                return Optional.empty();
            }
        };
        
        private final String name;
        private final Function<Grid, Optional<? extends Hint>> hintFinder;
        
        private HintStep(String name, Function<Grid, Optional<? extends Hint>> hintFinder) {
            this.name = name;
            this.hintFinder = hintFinder;
        }

        @Override
        public final void run(Controller controller) {
            Callable<Optional<? extends Hint>> job = () -> hintFinder.apply(controller.getModel().getGrid());
            Consumer<Optional<? extends Hint>> resultHandler = opt -> {
                if (opt.isPresent()) {
                    applyHint(controller, opt.get());
                } else {
                    getNextStep().ifPresentOrElse(controller::runNextStepAfterHintNotAvailable, controller::stop);
                }
            };
            controller.setStatus("Looking for " + name);
            UiThread.offload(job, resultHandler);
        }
        
        private void applyHint(Controller controller, Hint hint) {
            controller.updateUi(hint, name);
            // Always go back to Naked Single after each successful hint.
            controller.runNextStepAfterCompletedHint(NAKED_SINGLE);
        }
        
        protected abstract Optional<HintStep> getNextStep();
        
    }
    
}
