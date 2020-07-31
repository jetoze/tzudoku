package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import com.google.common.collect.ImmutableSet;

import jetoze.gunga.UiThread;
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
    
    // TODO: Place the app frame in blocking wait-state while the auto solver is running.
    // TODO: Allow the auto-solver to be stopped.
    // TODO: Show information about the hints are being processed. This can be done in a small
    //       modal dialog, that also has a Stop button.
    // TODO: When applying a hint that eliminates candidates, color the cells involved,
    //       using a different color for the target cells than the generating cells.
    // TODO: Change the timer behavior, so that we introduce a delay only when we have
    //       found and applied a hint, before we move to the next hint. That will allow
    //       the solver to sift through non-productive hints faster. Or at least use a 
    //       smaller delay after a non-productive hint.

    // TODO: Create a non-UI version of this auto solver. It can analyze the current puzzle,
    //       and provide the following information without actually showing the solution:
    //         1. Could the puzzle be solved?
    //         2. If it could be solved or not.
    //         3. Statistics on what types of hints were used, in particular advanced 
    //            (triples(?), X-Wings, XY-Wings) hints.
    //         4. For puzzles that could be solved, the hint statistics can be used to
    //            rate the difficulty of the puzzle.

    private static final Duration DELAY = Duration.ofMillis(750L);
    
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
        
        public Controller(JFrame appFrame, GridUiModel model) {
            this.appFrame = appFrame;
            this.model = model;
        }

        public GridUiModel getModel() {
            return model;
        }
        
        public void start() {
            // TODO: Restore the original setting when we are done?
            model.getEliminateCandidatesProperty().set(true);
            new FillInCandidates().run(this);
        }
        
        public void stop() {
            UiThread.throwIfNotUiThread();
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
        
        public void runNextStep(Step step) {
            UiThread.throwIfNotUiThread();
            requireNonNull(step);
            if (model.getGrid().isSolved()) {
                showSuccessMessage();
            } else {
                runStep(step);
            }
        }

        private void runStep(Step step) {
            Timer timer = new Timer((int) DELAY.toMillis(), e -> step.run(this));
            timer.setRepeats(false);
            timer.start();
        }

        public void updateUi(Hint hint) {
            // FIXME: Refactor the Step implementation so that we can pass Hints of
            // specific types, without having to cast.
            UiThread.run(() -> {
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
    
    
    private interface Step {
        
        void run(Controller controller);
        
    }
    
    
    private static class FillInCandidates implements Step {

        @Override
        public void run(Controller controller) {
            controller.getModel().showRemainingCandidates();
            controller.runNextStep(HintStep.NAKED_SINGLE);
        }
    }
    
    
    private static enum HintStep implements Step {
        
        NAKED_SINGLE(Single::findNextNaked) {

            @Override
            protected Optional<HintStep> getNextStep() {
                return Optional.of(HIDDEN_SINGLE);
            }
        },
        
        HIDDEN_SINGLE(Single::findNextHidden) {

            @Override
            protected Optional<HintStep> getNextStep() {
                return Optional.of(NAKED_PAIR);
            }

        },
        
        NAKED_PAIR(Multiple::findNextPair) {

            @Override
            protected Optional<HintStep> getNextStep() {
                return Optional.of(POINTING_PAIR);
            }
        },
        
        POINTING_PAIR(PointingPair::findNext) {

            @Override
            protected Optional<HintStep> getNextStep() {
                return Optional.of(TRIPLE);
            }
        },
        
        TRIPLE(Multiple::findNextTriple) {

            @Override
            protected Optional<HintStep> getNextStep() {
                return Optional.of(X_WING);
            }
        },
        
        X_WING(XWing::findNext) {

            @Override
            protected Optional<HintStep> getNextStep() {
                return Optional.of(XY_WING);
            }
        },
        
        XY_WING(XyWing::findNext) {

            @Override
            protected Optional<HintStep> getNextStep() {
                // This is the last step.
                return Optional.empty();
            }
        };
        
        private final Function<Grid, Optional<? extends Hint>> hintFinder;
        
        private HintStep(Function<Grid, Optional<? extends Hint>> hintFinder) {
            this.hintFinder = hintFinder;
        }

        @Override
        public final void run(Controller controller) {
            Callable<Optional<HintStep>> job = () -> {
                Optional<? extends Hint> opt = hintFinder.apply(controller.getModel().getGrid());
                if (opt.isPresent()) {
                    controller.updateUi(opt.get());
                    return Optional.of(NAKED_SINGLE);
                } else {
                    return getNextStep();
                }
            };
            UiThread.offload(job, next -> next.ifPresentOrElse(controller::runNextStep, controller::stop));
        }
        
        protected abstract Optional<HintStep> getNextStep();
        
    }
    
}
